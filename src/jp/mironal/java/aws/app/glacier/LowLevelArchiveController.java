
package jp.mironal.java.aws.app.glacier;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.mironal.java.aws.app.glacier.JobRestoreParam.Builder;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeJobRequest;
import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.GlacierJobDescription;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;
import com.amazonaws.services.glacier.model.ListJobsRequest;
import com.amazonaws.services.glacier.model.ListJobsResult;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;

/**
 * 一つのインスタンスにつき、一回リクエストを行える.<br>
 * マルチスレッド非対応
 * 
 * @author yama
 */
public class LowLevelArchiveController extends GlacierTools {

    class SqsSetupResult {
        private final String queueUrl;
        private final String queueArn;

        public SqsSetupResult(String sqsQueueUrl, String sqsQueueArn) {
            this.queueUrl = sqsQueueUrl;
            this.queueArn = sqsQueueArn;
        }
    }

    class SnsSetupResult {
        private final String topicArn;
        private final String subscriptionArn;

        public SnsSetupResult(String snsTopicArn, String snsSubscriptionArn) {
            this.topicArn = snsTopicArn;
            this.subscriptionArn = snsSubscriptionArn;
        }
    }

    /**
     * @param jobId
     * @param sqsQueueUrl
     * @param sqsClient
     * @return
     * @throws JsonParseException
     * @throws IOException
     */
    public static CheckJobResult checkJobToComplete(String jobId, String sqsQueueUrl,
            AmazonSQSClient sqsClient) throws JsonParseException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();

        ReceiveMessageRequest messageRequest = new ReceiveMessageRequest(sqsQueueUrl)
                .withMaxNumberOfMessages(10);
        List<Message> msgs = sqsClient.receiveMessage(messageRequest).getMessages();

        System.out.println("get message : msg.size=" + msgs.size());

        boolean messageFound = false;
        boolean jobSuccessful = false;
        if (msgs.size() > 0) {
            for (Message m : msgs) {
                JsonParser jpMessage = factory.createJsonParser(m.getBody());
                JsonNode JobmessageNode = mapper.readTree(jpMessage);
                String jobMessage = JobmessageNode.get("Message").getTextValue();

                JsonParser jpDesc = factory.createJsonParser(jobMessage);
                JsonNode jobDescNode = mapper.readTree(jpDesc);
                String retrievedJobId = jobDescNode.get("JobId").getTextValue();

                String statusCode = jobDescNode.get("StatusCode").getTextValue();
                System.out.println("retrievedJobId=" + retrievedJobId);
                System.out.println("StatusCode=" + statusCode);
                if (retrievedJobId.equals(jobId)) {
                    messageFound = true;
                    if (statusCode.equals("Succeeded")) {
                        jobSuccessful = true;
                    }
                }
            }
        }
        return new CheckJobResult(messageFound, jobSuccessful);
    }

    /**
     * 完了したJobをダウンロードする.
     * 
     * @param jobId
     * @param vaultName
     * @param saveFile
     * @param client
     */
    public static void downloadJobOutput(String jobId, String vaultName, File saveFile,
            AmazonGlacierClient client) {
        GetJobOutputRequest getJobOutputRequest = new GetJobOutputRequest()
                .withVaultName(vaultName).withJobId(jobId);
        GetJobOutputResult getJobOutputResult = client.getJobOutput(getJobOutputRequest);

        InputStream in = new BufferedInputStream(getJobOutputResult.getBody());
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(saveFile));
            byte[] buffer = new byte[1024 * 1024];
            int byteRead = 0;
            do {
                byteRead = in.read(buffer);
                if (byteRead <= 0) {
                    break;
                }
                out.write(buffer, 0, byteRead);
            } while (byteRead > 0);
        } catch (IOException e) {
            throw new AmazonClientException("Unable to save archive", e);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {

            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    private AmazonSQSClient sqsClient;
    private AmazonSNSClient snsClient;

    private boolean alreadyInitiate = false;

    private SqsSetupResult sqsSetupResult;
    private SnsSetupResult snsSetupResult;

    private String jobId;
    private String vaultName;
    private String archiveId;

    private void setVaultNameAndArchiveId(String vaultName, String archiveId) {
        if (vaultName == null) {
            throw new NullPointerException("vaultName is null.");
        }
        if (archiveId == null) {
            throw new NullPointerException("archiveId is null.");
        }

        this.vaultName = vaultName;
        this.archiveId = archiveId;
    }

    public LowLevelArchiveController() throws IOException {
        super();
    }

    public LowLevelArchiveController(Region endpoint, File awsProperties) throws IOException {
        super(endpoint, awsProperties);
        sqsClient = new AmazonSQSClient(credentials);
        sqsClient.setEndpoint(GlacierTools.makeUrl(AwsService.Sqs, endpoint));
        snsClient = new AmazonSNSClient(credentials);
        snsClient.setEndpoint(GlacierTools.makeUrl(AwsService.Sns, endpoint));
    }

    /**
     * Jobの復元に必要なパラメータを取得する.
     * 
     * @return Instance of JobRestoreParam.
     */
    public JobRestoreParam getJobRestoreParam() {
        Builder builder = new Builder();
        builder.setVaultName(this.vaultName).setJobId(this.jobId)
                .setSnsSubscriptionArn(snsSetupResult.subscriptionArn)
                .setSnsTopicArn(snsSetupResult.topicArn).setSqsQueueUrl(sqsSetupResult.queueUrl);
        return builder.build();
    }

    /**
     * Jobを復元する.
     * 
     * @param param
     */
    public void restoreJob(JobRestoreParam param) {
        if (param == null) {
            throw new NullPointerException("param is null.");
        }
        this.vaultName = param.getVaultName();
        this.jobId = param.getJobId();
        this.snsSetupResult = new SnsSetupResult(param.getSnsTopicArn(),
                param.getSnsSubscriptionArn());
        this.sqsSetupResult = new SqsSetupResult(param.getSqsQueueUrl(), "");
    }

    /**
     * inventory-retrievalのJobを開始する.<br>
     * inventory-retrievalはVaultの情報取得要求をするJob
     * 
     * @param vaultName 情報を取得したいVaultの名前.
     * @return jobId
     */
    public String initiateInventoryJob(String vaultName) {
        setVaultNameAndArchiveId(vaultName, "");

        // setup the SQS and SNS
        setupJob();

        JobParameters jobParameters = new JobParameters().withType("inventory-retrieval")
                .withDescription("inventory-retrieval_job").withFormat("JSON")
                .withSNSTopic(snsSetupResult.topicArn);

        return executeInitiateJob(jobParameters);
    }

    /**
     * archive-retrievalのJobを開始する.<br>
     * archive-retrievalはVaultの中にあるArchiveの取得要求をするJob.
     * 
     * @param vaultName 取得したいArchiveが所属しているVaultの名前
     * @param archiveId 取得したいArchiveのID
     * @return jobId
     */
    public String initiateArchiveJob(String vaultName, String archiveId) {
        setVaultNameAndArchiveId(vaultName, archiveId);

        // setup the SQS and SNS
        setupJob();

        JobParameters jobParameters = new JobParameters().withType("archive-retrieval")
                .withArchiveId(archiveId).withSNSTopic(snsSetupResult.topicArn);

        return executeInitiateJob(jobParameters);
    }

    /**
     * 指定したVaultの進行中、および最近完了したJobの一覧を取得する.<br>
     * 
     * @see <a href=
     *      "http://docs.amazonwebservices.com/amazonglacier/latest/dev/api-jobs-get.html"
     *      >List Jobs (GET jobs)<a>
     * @param vaultName
     * @return Jobの一覧
     */
    public List<GlacierJobDescription> listJobs(String vaultName) {
        ListJobsRequest request = new ListJobsRequest().withVaultName(vaultName);
        ListJobsResult result = client.listJobs(request);
        return result.getJobList();
    }

    /**
     * 指定したVaultの進行中、および最近完了したJobの一覧を取得する.<br>
     * 引数に関しても下記を参照して下さい.
     * 
     * @see <a href=
     *      "http://docs.amazonwebservices.com/amazonglacier/latest/dev/api-jobs-get.html"
     *      >List Jobs (GET jobs)<a>
     * @param vaultName
     * @param completed
     * @param limit
     * @param marker
     * @param statuscode
     * @return Jobの一覧
     */
    public List<GlacierJobDescription> listJobs(String vaultName, String completed, String limit,
            String marker, String statuscode) {

        ListJobsRequest request = new ListJobsRequest().withVaultName(vaultName)
                .withCompleted(completed).withLimit(limit).withMarker(marker)
                .withStatuscode(statuscode);
        ListJobsResult result = client.listJobs(request);
        return result.getJobList();
    }

    /**
     * 指定したVaultにあるJobIdで指定したJobの詳細を取得する.<br>
     * 
     * @see <a
     *      href="http://docs.amazonwebservices.com/amazonglacier/latest/dev/api-describe-job-get.html">Describe
     *      Job</a>
     * @param vaultName Name of Vault.
     * @param jobId Id of Job.
     * @return DescribeJobResult
     */
    public DescribeJobResult describeJob(String vaultName, String jobId) {
        if (vaultName == null) {
            throw new NullPointerException("vaultName is null.");
        }
        if (jobId == null) {
            throw new NullPointerException("jobId is null.");
        }

        DescribeJobRequest describeJobRequest = new DescribeJobRequest(vaultName, jobId);
        DescribeJobResult result = client.describeJob(describeJobRequest);
        return result;
    }

    /**
     * Jobが完了したかをチェックする.<br>
     * SQSを使ってメッセージが届いたかをチェックし、更にJobが成功したかをチェックする.<br>
     * メッセージが到達し、Jobが成功していればダウンロードが開始できる.
     * 
     * @return Jobの完了状態を示すCheckJobResultインスタンス
     * @throws JsonParseException
     * @throws IOException
     */
    public CheckJobResult checkJobToComplete() throws JsonParseException, IOException {
        // まだJobが開始されていない場合にこの関数を呼んだら例外を投げる
        if (!alreadyInitiate) {
            throw new IllegalStateException("Job has not yet started");
        }
        return LowLevelArchiveController.checkJobToComplete(this.jobId, sqsSetupResult.queueUrl,
                sqsClient);
    }

    /**
     * Jobが完了するまで待つ.
     * 
     * @return
     * @throws JsonParseException
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean waitForJobToComplete() throws JsonParseException, IOException,
            InterruptedException {
        CheckJobResult result = null;
        do {
            System.out.println("loop");
            Thread.sleep(1000 * 60 * 30);
            result = checkJobToComplete();
            System.out.println(result.toString());
        } while (!result.isMessageFound());
        System.out.println("exit loop");
        return result.isJobSuccessful();
    }

    public void printJobOutput() {
        GetJobOutputRequest getJobOutputRequest = new GetJobOutputRequest()
                .withVaultName(vaultName).withJobId(jobId);
        GetJobOutputResult getJobOutputResult = client.getJobOutput(getJobOutputRequest);

        BufferedReader br = new BufferedReader(new InputStreamReader(getJobOutputResult.getBody()));

        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new AmazonClientException("Unable to save archive", e);
        } finally {
            try {
                br.close();
            } catch (IOException ignore) {

            }
        }
    }

    public void downloadJobOutput(File saveFile) {
        LowLevelArchiveController.downloadJobOutput(this.jobId, this.vaultName, saveFile,
                this.client);
    }

    public void cleanUp() {
        snsClient.unsubscribe(new UnsubscribeRequest(snsSetupResult.subscriptionArn));
        snsClient.deleteTopic(new DeleteTopicRequest(snsSetupResult.topicArn));
        sqsClient.deleteQueue(new DeleteQueueRequest(sqsSetupResult.queueUrl));
    }

    /**
     * SQSを初期化する.
     * 
     * @param queueName
     * @return SetupSqsResult
     */
    private SqsSetupResult setupSQS(String queueName) {
        CreateQueueRequest request = new CreateQueueRequest().withQueueName(queueName);
        CreateQueueResult result = sqsClient.createQueue(request);

        String sqsQueueUrl = result.getQueueUrl();

        GetQueueAttributesRequest qRequest = new GetQueueAttributesRequest().withQueueUrl(
                sqsQueueUrl).withAttributeNames("QueueArn");

        GetQueueAttributesResult qResult = sqsClient.getQueueAttributes(qRequest);

        String sqsQueueArn = qResult.getAttributes().get("QueueArn");

        Policy sqsPolicy = new Policy().withStatements(new Statement(Effect.Allow)
                .withPrincipals(Principal.AllUsers).withActions(SQSActions.SendMessage)
                .withResources(new Resource(sqsQueueArn)));

        Map<String, String> queueAttributes = new HashMap<String, String>();
        queueAttributes.put("Policy", sqsPolicy.toJson());
        sqsClient.setQueueAttributes(new SetQueueAttributesRequest(sqsQueueUrl, queueAttributes));

        return new SqsSetupResult(sqsQueueUrl, sqsQueueArn);
    }

    /**
     * @param snsTopicName
     * @param sqsQueueArn
     * @return SetupSnsResult
     */
    private SnsSetupResult setupSNS(String snsTopicName, String sqsQueueArn) {
        CreateTopicRequest request = new CreateTopicRequest().withName(snsTopicName);
        CreateTopicResult result = snsClient.createTopic(request);

        String snsTopicArn = result.getTopicArn();

        SubscribeRequest subscribeRequest = new SubscribeRequest().withTopicArn(snsTopicArn)
                .withEndpoint(sqsQueueArn).withProtocol("sqs");
        SubscribeResult subscribeResult = snsClient.subscribe(subscribeRequest);

        return new SnsSetupResult(snsTopicArn, subscribeResult.getSubscriptionArn());

    }

    /**
     * Amazon Glacierに要求を行う
     * 
     * @param jobParameters
     * @return jobId
     */
    private String executeInitiateJob(JobParameters jobParameters) {
        InitiateJobRequest request = new InitiateJobRequest().withVaultName(vaultName)
                .withJobParameters(jobParameters);
        InitiateJobResult result = client.initiateJob(request);
        this.jobId = result.getJobId();
        return jobId;
    }

    /**
     * SQSとSNSの初期化を行う.
     */
    private void setupJob() {
        if (alreadyInitiate) {
            throw new IllegalStateException("initiate job request is already node.");
        }
        alreadyInitiate = true;

        String sqsQueueName = "initiate-job_sqs";
        sqsSetupResult = setupSQS(sqsQueueName);

        String snsTopicName = "initiate-job_sns";
        snsSetupResult = setupSNS(snsTopicName, sqsSetupResult.queueArn);
    }

}

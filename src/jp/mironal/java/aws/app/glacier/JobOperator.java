
package jp.mironal.java.aws.app.glacier;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;

import jp.mironal.java.aws.app.glacier.JobRestoreParam.Builder;

import org.codehaus.jackson.JsonParseException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;

/**
 * 一つのインスタンスにつき、一回リクエストを行える.<br>
 * マルチスレッド非対応<br>
 * Jobに待ち方はAmazonの推奨しているSNS、SQSを使わず、Describe Jobをポーリングする方法を採用した.<br>
 * 理由は処理を簡潔にするためである.<br>
 * SNS、SQSを使用する方法は別途提供する.
 * 
 * @author yama
 */
public class JobOperator extends StateLessJobOperator {

    public static final String STATUS_INPROGRESS = "InProgress";
    public static final String STATUS_SUCCEEDED = "Succeeded";
    public static final String STATUS_FAILED = "Failed";

    private boolean alreadyInitiate = false;

    private String jobId;
    private String vaultName;

    public JobOperator() throws IOException {
        super();
    }

    public JobOperator(Region endpoint, File awsProperties) throws IOException {
        super(endpoint, awsProperties);
    }

    /**
     * Jobの復元に必要なパラメータを取得する.<br>
     * まだJobが開始されていない場合はIllegalStateException()を投げる.
     * 
     * @return Instance of JobRestoreParam.
     */
    public JobRestoreParam getJobRestoreParam() {
        if (!alreadyInitiate) {
            throw new IllegalStateException("Job has not yet started");
        }
        Builder builder = new Builder();
        builder.setVaultName(this.vaultName).setJobId(this.jobId);
        return builder.build();
    }

    /**
     * Jobを復元する.<br>
     * すでにこのインスタンスでJobが実行されていた場合はIllegalStateException()を投げる.
     * 
     * @param param JobRestoreParamのインスタンス
     */
    public void restoreJob(JobRestoreParam param) {
        if (param == null) {
            throw new NullPointerException("param is null.");
        }
        if (alreadyInitiate) {
            throw new IllegalStateException("initiate job request is already node.");
        }
        this.vaultName = param.getVaultName();
        this.jobId = param.getJobId();

        alreadyInitiate = true;
    }

    /**
     * inventory-retrievalのJobを開始する.<br>
     * inventory-retrievalはVaultの情報取得要求をするJob
     * 
     * @param vaultName 情報を取得したいVaultの名前.
     * @return jobId
     */
    public String initiateInventoryJob(String vaultName) {

        JobParameters jobParameters = new JobParameters().withType("inventory-retrieval")
                .withDescription("inventory-retrieval_job").withFormat("JSON");

        return executeInitiateJob(jobParameters, vaultName);
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

        JobParameters jobParameters = new JobParameters().withType("archive-retrieval")
                .withArchiveId(archiveId);

        return executeInitiateJob(jobParameters, vaultName);
    }

    /**
     * Jobのステータスコードを取得する.<br>
     * STATUS_INPROGRESS(InProgress):Jobが完了していない(まだダウンロード出来ない).<br>
     * STATUS_SUCCEEDED(Succeeded):Jobが完了し、成功した(ダウンロード可能になった).<br>
     * STATUS_FAILED(Failed):Jobの完了に失敗した(ダウンロード不可能).<br>
     * 
     * @return ステータスコードを示す文字列.
     */
    public String getStausCode() {
        if (!alreadyInitiate) {
            throw new IllegalStateException("Job has not yet started");
        }
        DescribeJobResult result = describeJob(vaultName, jobId);
        return result.getStatusCode();
    }

    /**
     * Jobが完了するまで待つ.
     * 
     * @return true:Jobが完了し成功した. false:Jobが完了したが成功しなかった.
     * @throws InterruptedException
     */
    public boolean waitForJobToComplete() throws InterruptedException {
        if (!alreadyInitiate) {
            throw new IllegalStateException("Job has not yet started");
        }

        boolean inProgress = true;
        String statusCode = "";
        do {
            System.out.println("loop");
            Thread.sleep(1000 * 60 * 30);

            statusCode = getStausCode();
            inProgress = statusCode.equals(STATUS_INPROGRESS);

        } while (inProgress);
        return statusCode.equals(STATUS_SUCCEEDED);
    }

    /**
     * inventory-retrievalのJobをダウンロードする.
     * 
     * @return InventoryRetrievalResultのインスタンス.
     * @throws JsonParseException
     * @throws IOException
     * @throws ParseException
     */
    public InventoryRetrievalResult downloadInventoryJobOutput() throws JsonParseException,
            IOException, ParseException {
        if (!alreadyInitiate) {
            throw new IllegalStateException("Job has not yet started");
        }

        GetJobOutputRequest getJobOutputRequest = new GetJobOutputRequest()
                .withAccountId(vaultName).withJobId(jobId);
        GetJobOutputResult result = client.getJobOutput(getJobOutputRequest);
        return new InventoryRetrievalResult(result.getBody());
    }

    /**
     * archive-retrievalで開始したJobのArchiveをダウンロードする.
     * 
     * @param saveFile ダウンロードしたファイルの保存先.
     */
    public void downloadArchiveJobOutput(File saveFile) {
        if (!alreadyInitiate) {
            throw new IllegalStateException("Job has not yet started");
        }

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

    /**
     * Amazon Glacierに要求を行う
     * 
     * @param jobParameters
     * @return jobId
     */
    private String executeInitiateJob(JobParameters jobParameters, String vaultName) {

        InitiateJobRequest request = new InitiateJobRequest().withVaultName(vaultName)
                .withJobParameters(jobParameters);
        InitiateJobResult result = client.initiateJob(request);
        this.vaultName = vaultName;
        this.jobId = result.getJobId();
        return jobId;
    }

}

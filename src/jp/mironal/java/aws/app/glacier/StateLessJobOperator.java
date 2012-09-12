
package jp.mironal.java.aws.app.glacier;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.amazonaws.services.glacier.model.DescribeJobRequest;
import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.GlacierJobDescription;
import com.amazonaws.services.glacier.model.ListJobsRequest;
import com.amazonaws.services.glacier.model.ListJobsResult;

/**
 * 状態を持たないJob Operationsを行うクラス
 * 
 * @author mironal
 */
public class StateLessJobOperator extends GlacierTools {

    /**
     * デフォルトコンストラクタ<br>
     * デフォルトのリージョンとカレントディレクトリにあるAwsCredentials.propertiesを使用してインスタンスを作成
     * 
     * @throws IOException
     */
    public StateLessJobOperator() throws IOException {
        super();
    }

    /**
     * 指定したリージョンとAwsCredentials.propertiesファイルでインスタンスを作成
     * 
     * @param endpoint リージョン
     * @param awsProperties AwsCredentials.propertiesファイルのインスタンス
     * @throws IOException
     */
    public StateLessJobOperator(Region endpoint, File awsProperties) throws IOException {
        super(endpoint, awsProperties);
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
        if (vaultName == null) {
            throw new NullPointerException("vaultName is null.");
        }

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
        if (vaultName == null) {
            throw new NullPointerException("vaultName is null.");
        }
        if (completed == null) {
            throw new NullPointerException("completed is null.");
        }
        if (limit == null) {
            throw new NullPointerException("limit is null.");
        }
        if (marker == null) {
            throw new NullPointerException("marker is null.");
        }
        if (statuscode == null) {
            throw new NullPointerException("statuscode is null.");
        }

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

}

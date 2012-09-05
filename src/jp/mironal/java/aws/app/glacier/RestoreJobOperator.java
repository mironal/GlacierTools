
package jp.mironal.java.aws.app.glacier;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;

import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.GlacierJobDescription;

public class RestoreJobOperator extends StateLessJobOperator {

    public static final String ACTION_ARCHIVE_RETRIEVAL = "ArchiveRetrieval";
    public static final String ACTION_INVENTORY_RETRIEVAL = "InventoryRetrieval";

    public static RestoreJobOperator restoreJob(JobRestoreParam param, File awsProperties)
            throws IOException {
        RestoreJobOperator operator = new RestoreJobOperator(param, awsProperties);
        return operator;
    }

    private final JobOperator operator;
    private final JobRestoreParam jobRestoreParam;

    private String action = null;

    private RestoreJobOperator(JobRestoreParam param, File awsProperties) throws IOException {
        super(param.getRegion(), awsProperties);
        operator = JobOperator.restore(param, awsProperties);
        jobRestoreParam = param;
    }

    public DescribeJobResult describeJob() {
        return operator.describeJob(jobRestoreParam.getVaultName(), jobRestoreParam.getJobId());
    }

    public List<GlacierJobDescription> listJobs() {
        return operator.listJobs(jobRestoreParam.getVaultName());
    }

    /**
     * Jobが完了して成功しているか?
     * 
     * @return true:Jobが完了して成功している.false:それ以外.
     */
    public boolean checkJobSucceeded() {
        DescribeJobResult result = describeJob();
        return result.getStatusCode().equals(JobOperator.STATUS_SUCCEEDED);
    }

    public String getStatusCode() {
        return describeJob().getStatusCode();
    }

    public String updateJobKind() {

        DescribeJobResult result = describeJob();
        this.action = result.getAction();
        return this.action;
    }

    public String getAction() {
        if (action == null) {
            updateJobKind();
        }
        return action;
    }

    public boolean waitForJobComplete() throws InterruptedException {
        return operator.waitForJobToComplete();
    }

    public void downloadArchiveJobOutput(File saveFile) {
        if (!getAction().equals(ACTION_ARCHIVE_RETRIEVAL)) {
            throw new IllegalStateException("This job is " + getAction());
        }
        operator.downloadArchiveJobOutput(saveFile);
    }

    public InventoryRetrievalResult downloadInventoryJobOutput() throws JsonParseException,
            IOException, ParseException {
        if (!getAction().equals(ACTION_INVENTORY_RETRIEVAL)) {
            throw new IllegalStateException("This job is " + getAction());
        }
        return operator.downloadInventoryJobOutput();
    }
}

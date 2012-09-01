
package jp.mironal.java.aws.app.glacier;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class RestoreJobController {

    @SuppressWarnings("serial")
    class JobRestoreParamNotFoundException extends RuntimeException {
        public JobRestoreParamNotFoundException(String msg) {
            super(msg);
        }
    }

    String jobId;
    String vaultname;
    String snsSubscriptionArn;
    String snsTopicArn;
    String sqsQueueUrl;

    public RestoreJobController(String[] args) {

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("--vault")) {
                if ((i + 1) < args.length) {
                    i++;
                    vaultname = args[i];
                }
            }

            if (arg.equals("--job")) {
                if ((i + 1) < arg.length()) {
                    i++;
                    jobId = args[i];
                }
            }

            if (arg.equals("--snssub")) {
                if ((i + 1) < args.length) {
                    i++;
                    snsSubscriptionArn = args[i];
                }
            }

            if (arg.equals("--snstopic")) {
                if ((i + 1) < args.length) {
                    i++;
                    snsTopicArn = args[i];
                }
            }

            if (arg.equals("--sqsurl")) {
                if ((i + 1) < args.length) {
                    i++;
                    sqsQueueUrl = args[i];
                }
            }
        }
    }

    private void loadJobRestoreParam(String filename) throws FileNotFoundException, IOException {
        if (filename == null) {
            throw new NullPointerException("filename is null.");
        }

        Properties properties = new Properties();
        properties.load(new FileReader(filename));

        if (properties.contains("JobId")) {
            throw new JobRestoreParamNotFoundException("JobId not found.");
        }
        if (properties.contains("VaultName")) {
            throw new JobRestoreParamNotFoundException("VaultName not found.");
        }
        if (properties.contains("SnsSubscriptionArn")) {
            throw new JobRestoreParamNotFoundException("SnsSubscriptionArn not found.");
        }
        if (properties.contains("SnsTopicArn")) {
            throw new JobRestoreParamNotFoundException("SnsTopicArn not found.");
        }
        if (properties.contains("SqsQueueUrl")) {
            throw new JobRestoreParamNotFoundException("SqsQueueUrl not found.");
        }

        jobId = properties.getProperty("JobId");
        vaultname = properties.getProperty("VaultName");
        snsSubscriptionArn = properties.getProperty("SnsSubscriptionArn");
        snsTopicArn = properties.getProperty("SnsTopicArn");
        sqsQueueUrl = properties.getProperty("SqsQueueUrl");
    }

    /**
     * * java -jar lowlevelControl.jar [restore] [jobType] {syncType}<br>
     * [--restore restore_prop_filename]<br>
     * <br>
     * [jobType] = inventory-retrieval | archive-retrieval | list | desc<br>
     * [syncType] = sync | async<br>
     * [endpoint] = ushogehoge<br>
     * <br>
     * <br>
     * restore_prop_filename(filename.properties)の中身は<br>
     * JobId=jobID VaultName=vaultname<br>
     * SnsSubscriptionArn=snsSubscriptionArn<br>
     * SnsTopicArn=snsTopicArn<br>
     * SqsQueueUrl=sqsQueueUrl<br>
     * の用になっていてJavaのPropertiesで読める形にしておく<br>
     * <br>
     * こうすることでAsyncでコマンドを実行する際にその出力をhoge.propertiesにリダイレクトすることで、<br>
     * Jobの復元を容易に行うことが出来る. <br>
     * <br>
     * * Restore Job<br>
     * java -jar lowlevelControl.jar [restore] [jobType] {syncType}<br>
     * [--job jobId]<br>
     * [--vault vaultname]<br>
     * [--snssub snsSubscriptionArn]<br>
     * [--snstopic snsTopicArn]<br>
     * [--sqsurl sqsQueueUrl]<br>
     * <br>
     * 
     * @param args
     */
    public static void main(String[] args) {

    }

}

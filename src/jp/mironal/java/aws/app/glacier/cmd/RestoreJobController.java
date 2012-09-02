
package jp.mironal.java.aws.app.glacier.cmd;

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

        jobId = properties.getProperty("JobId");
        vaultname = properties.getProperty("VaultName");
    }

    /**
     * * java -jar lowlevelControl.jar [restore] [jobType] {syncType}<br>
     * [--restore restore_prop_filename]<br>
     * <br>
     * [jobType] = download | check<br>
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

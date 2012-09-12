
package jp.mironal.java.aws.app.glacier.cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import jp.mironal.java.aws.app.glacier.InventoryRetrievalResult;
import jp.mironal.java.aws.app.glacier.JobRestoreParam;
import jp.mironal.java.aws.app.glacier.JobRestoreParam.Builder;
import jp.mironal.java.aws.app.glacier.RestoreJobOperator;

import com.amazonaws.services.glacier.model.DescribeJobResult;

public class RestoreJobCmd extends CmdUtils {

    enum RestoreJobCmdKind {
        Bad, Download, Check, Desc, Help
    }

    @SuppressWarnings("serial")
    class JobRestoreException extends RuntimeException {
        public JobRestoreException(String msg) {
            super(msg);
        }
    }

    JobRestoreParam jobRestoreParam = null;
    RestoreJobCmdKind cmdKind = RestoreJobCmdKind.Bad;
    String filename;

    /**
     * @param args
     * @throws IOException
     * @throws JobRestoreException
     */
    public RestoreJobCmd(String[] args) throws IOException {
        super(args);
        String restorePropFilename = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("download")) {
                setCmdKind(RestoreJobCmdKind.Download);
            }
            if (arg.equals("check")) {
                setCmdKind(RestoreJobCmdKind.Check);
            }
            if (arg.equals("desc")) {
                setCmdKind(RestoreJobCmdKind.Desc);
            }

            if (arg.equals("-h") || arg.equals("--help") || arg.equals("help")) {
                setCmdKind(RestoreJobCmdKind.Help);
            }
            if (arg.equals("--file")) {
                if ((i + 1) < args.length) {
                    i++;
                    filename = args[i];
                }
            }

            if (arg.equals("--restore")) {
                if ((i + 1) < args.length) {
                    i++;
                    restorePropFilename = args[i];
                }
            }

        }

        /* helpの時は無視する. */
        if (cmdKind != RestoreJobCmdKind.Help) {
            // プロパティーファイルが無かったらcmdをBadにする.
            restoreJobParam(restorePropFilename);
        }
    }

    private void setCmdKind(RestoreJobCmdKind cmd) {
        if (this.cmdKind == RestoreJobCmdKind.Bad) {
            this.cmdKind = cmd;
        } else {
            /* オプション違反 */
            this.cmdKind = RestoreJobCmdKind.Bad;
        }
    }

    private void restoreJobParam(String filename) throws IOException {
        if (filename == null) {
            throw new JobRestoreException("--restore option is not specified");
        }
        File restoreFile = new File(filename);
        if (!restoreFile.exists()) {
            throw new JobRestoreException(restoreFile.getAbsolutePath() + " does not exist.");
        }
        FileReader reader = null;
        try {
            reader = new FileReader(filename);
            Properties properties = new Properties();
            properties.load(reader);

            if (!properties.containsKey("JobId")) {
                throw new JobRestoreException("JobId not found.");
            }
            if (!properties.containsKey("VaultName")) {
                throw new JobRestoreException("VaultName not found.");
            }
            if (!properties.containsKey("Region")) {
                throw new JobRestoreException("Region not found.");
            }

            // region周りの処理がうまくないが、目をつぶる.
            setRegion(properties.getProperty("Region"));
            Builder builder = new Builder();
            builder.setJobId(properties.getProperty("JobId"));
            builder.setVaultName(properties.getProperty("VaultName"));
            builder.setRegion(this.region);
            jobRestoreParam = builder.build();

        } catch (FileNotFoundException ignore) {
            // 無視するが念のためStackTraceは出す.
            ignore.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Override
    void onAwsCredentialsPropertiesFileNotFound(String filename, Throwable e) {
        setCmdKind(RestoreJobCmdKind.Bad);
    }

    @Override
    void onRegionNotFound(String endpointStr, Throwable e) {
        setCmdKind(RestoreJobCmdKind.Bad);
    }

    private void execDownload() throws IOException, InterruptedException, ParseException {
        if (!isDebug()) {
            RestoreJobOperator operator = RestoreJobOperator.restoreJob(jobRestoreParam,
                    awsPropFile);

            // 完了状態をチェック
            // 完了してなからったら待つ
            if (operator.waitForJobComplete()) {
                // 完了してたらダウンロード
                // Jobの種類(archive, inventory)のチェック
                operator.updateJobKind();

                if (operator.getAction().equals(RestoreJobOperator.ACTION_ARCHIVE_RETRIEVAL)) {
                    if (filename == null) {
                        System.out.println("--filename option is not specified");
                        System.exit(-1);
                    }
                    operator.downloadArchiveJobOutput(new File(filename));
                } else if (operator.getAction().equals(
                        RestoreJobOperator.ACTION_INVENTORY_RETRIEVAL)) {
                    InventoryRetrievalResult result = operator.downloadInventoryJobOutput();
                    System.out.println(result.toString());
                }
            } else {
                System.out.println("job fault.");
            }

        } else {
            System.out.println("execute download");
        }
    }

    private void execCheck() throws IOException {
        if (!isDebug()) {
            RestoreJobOperator operator = RestoreJobOperator.restoreJob(jobRestoreParam,
                    awsPropFile);
            System.out.println(operator.getStatusCode());
        } else {
            System.out.println("execute check");
        }
    }

    private void execDesc() throws IOException {
        if (!isDebug()) {

            RestoreJobOperator operator = RestoreJobOperator.restoreJob(jobRestoreParam,
                    awsPropFile);
            DescribeJobResult result = operator.describeJob();
            printDescribeJob(result);
        } else {
            System.out.println("execute describe");
        }
    }

    @Override
    void onExecCommand() throws Exception {
        switch (cmdKind) {
            case Download:
                execDownload();
                break;
            case Check:
                execCheck();
                break;
            case Desc:
                execDesc();
                break;
            case Help:
                printHelp();
                break;
            case Bad:
                printHelp();
                break;

            default:
                throw new IllegalStateException();
        }
    }

    private void printHelp() {
        System.out
                .println("java -jar restorejob.jar cmd [--restore restorepropfilename] [--file filename] [--properties propfilename]");

        System.out.println("cmd          : download | check | desc");
        System.out.println("--restore    : job_operator.jarの結果が記述されているファイル.");
        System.out
                .println("--file       : When the download of Archive is the name of the saved file");
        System.out
                .println("--properties : If you want to specify explicitly AwsCredentials.properties");

    }

    @Override
    void onExecInvalidParam() {
    }

    @Override
    boolean validateParam() {
        if (cmdKind == RestoreJobCmdKind.Help) {
            return true;
        }
        boolean ok = true;
        if (region == null) {
            debugPrint("region is null.");
            ok = false;
        }
        if (awsPropFile == null) {
            debugPrint("awsPropFile is null.");
            ok = false;
        }

        if (jobRestoreParam == null) {
            debugPrint("jobRestoreParam is null.");
            ok = false;
        }

        return ok;
    }

    /**
     * @param args
     * @throws Exception
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, Exception {
        try {
            new RestoreJobCmd(args).exec();
        } catch (JobRestoreException e) {
            System.out.println(e.getMessage());
        }
    }
}

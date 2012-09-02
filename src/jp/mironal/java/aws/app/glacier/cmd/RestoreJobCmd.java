
package jp.mironal.java.aws.app.glacier.cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

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

    String jobId;
    String vaultname;
    RestoreJobCmdKind cmdKind;

    public RestoreJobCmd(String[] args) throws IOException {
        String restorePropFilename = null;
        String propertiesName = null;
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

            if (arg.equals("--restore")) {
                if ((i + 1) < args.length) {
                    i++;
                    restorePropFilename = args[i];
                }
            }

            if (arg.equals("--properties")) {
                if ((i + 1) < args.length) {
                    i++;
                    propertiesName = args[i];
                }
            }

        }

        // プロパティーファイルが無かったらcmdをBadにする.
        setAwsCredentialsPropertiesFile(propertiesName);

        restoreJobParam(restorePropFilename);
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
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(filename));
        } catch (FileNotFoundException ignore) {
            // 無視するが念のためStackTraceは出す.
            ignore.printStackTrace();
        }

        if (!properties.contains("JobId")) {
            throw new JobRestoreException("JobId not found.");
        }
        if (!properties.contains("VaultName")) {
            throw new JobRestoreException("VaultName not found.");
        }
        if (!properties.contains("Region")) {
            throw new JobRestoreException("Region not found.");
        }

        this.jobId = properties.getProperty("JobId");
        this.vaultname = properties.getProperty("VaultName");
        setRegion(properties.getProperty("Region"));
    }

    @Override
    void onAwsCredentialsPropertiesFileNotFound(String filename, Throwable e) {

    }

    @Override
    void onRegionNotFound(String endpointStr, Throwable e) {

    }

    @Override
    void onExecCommand() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    void onExecInvalidParam() {
        // TODO Auto-generated method stub

    }

    @Override
    boolean validateParam() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

    }
}

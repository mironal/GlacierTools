
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

    String jobId = null;
    String vaultname = null;
    RestoreJobCmdKind cmdKind = RestoreJobCmdKind.Bad;

    /**
     * @param args
     * @throws IOException
     * @throws JobRestoreException
     */
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

        /* helpの時は無視する. */
        if (cmdKind != RestoreJobCmdKind.Help) {
            // プロパティーファイルが無かったらcmdをBadにする.
            setAwsCredentialsPropertiesFile(propertiesName);
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

            this.jobId = properties.getProperty("JobId");
            this.vaultname = properties.getProperty("VaultName");
            setRegion(properties.getProperty("Region"));
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
        System.err.println(filename + " is not found.");
        setCmdKind(RestoreJobCmdKind.Bad);
    }

    @Override
    void onRegionNotFound(String endpointStr, Throwable e) {
        System.err.println(e.getMessage() + " is not found.");
        setCmdKind(RestoreJobCmdKind.Bad);
    }

    private void execDownload() throws IOException {
        // Jobの種類(archive, inventory)のチェック
        // 完了状態をチェック
        // 完了してなからったら待つ
        // 完了してたらダウンロード

    }

    @Override
    void onExecCommand() throws Exception {
        switch (cmdKind) {
            case Download:
                break;
            case Check:
                break;
            case Desc:
                break;
            case Help:
                break;
            case Bad:
                break;

            default:
                throw new IllegalStateException();
        }
    }

    @Override
    void onExecInvalidParam() {
        // TODO Auto-generated method stub

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

        if (jobId == null) {
            debugPrint("jobId is null.");
            ok = false;
        }

        if (vaultname == null) {
            debugPrint("vaultname is null.");
            ok = false;
        }
        return ok;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

    }
}

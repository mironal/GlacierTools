
package jp.mironal.java.aws.app.glacier.cmd;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import jp.mironal.java.aws.app.glacier.ArchiveController;
import jp.mironal.java.aws.app.glacier.InventoryRetrievalResult;
import jp.mironal.java.aws.app.glacier.JobOperator;

import com.amazonaws.services.glacier.transfer.UploadResult;

public class ArchiveControllerCmd extends CmdUtils {

    enum ArchiveCmdKind {
        Bad, Upload, Download, Delete, List, Help,
    }

    ArchiveCmdKind cmdKind = ArchiveCmdKind.Bad;

    String vaultName = null;
    String filename = null;
    String archiveId = null;
    boolean force = false; /* アーカイブダウンロード時に同名のファイルが既に有った場合、強制的に上書きする */
    boolean printArchiveIdOnly = false;

    ArchiveControllerCmd(String[] args) {
        String propertiesName = null;
        String endpointStr = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            /*
             * すでに別のオプションが割り当てられていたら不正な引数とみなす.
             */
            if (arg.equals("upload")) {
                setCmdKind(ArchiveCmdKind.Upload);
            }

            if (arg.equals("download")) {
                setCmdKind(ArchiveCmdKind.Download);
            }

            if (arg.equals("delete")) {
                setCmdKind(ArchiveCmdKind.Delete);
            }

            if (arg.equals("list")) {
                setCmdKind(ArchiveCmdKind.List);
            }

            if (arg.equals("-h") || arg.equals("--help") || arg.equals("help")) {
                setCmdKind(ArchiveCmdKind.Help);
            }

            if (arg.equals("--vault")) {
                if ((i + 1) < args.length) {
                    i++;
                    vaultName = args[i];
                }
            }

            if (arg.equals("--file")) {
                if ((i + 1) < args.length) {
                    i++;
                    filename = args[i];
                }
            }

            if (arg.equals("--archive")) {
                if ((i + 1) < args.length) {
                    i++;
                    archiveId = args[i];
                }
            }

            if (arg.equals("--region")) {
                if ((i + 1) < args.length) {
                    i++;
                    endpointStr = args[i];
                }
            }

            if (arg.equals("--properties")) {
                if ((i + 1) < args.length) {
                    i++;
                    propertiesName = args[i];
                }
            }

            if (arg.equals("--idonly")) {
                printArchiveIdOnly = true;
            }

            if (arg.equals("-h") || arg.equals("--help")) {
                System.exit(0);
            }

            if (arg.equals("--force")) {
                force = true;
            }

            if (arg.equals("--debug")) {
                debug = true;
            }
        }

        setAwsCredentialsPropertiesFile(propertiesName);

        /* endpoint */
        setRegion(endpointStr);

    }

    @Override
    boolean validateParam() {
        // trueを代入するロジックは初期化のところだけとする.
        boolean ok = true;
        if (region == null) {
            debugPrint("region is null.");
            ok = false;
        }
        if (awsPropFile == null) {
            debugPrint("awsPropFile is null.");
            ok = false;
        }
        switch (cmdKind) {
            case Upload:
                if (vaultName == null) {
                    debugPrint("vaultName is null.");
                    ok = false;
                }
                if (filename == null) {
                    debugPrint("filename is null.");
                    ok = false;
                }

                break;
            case Download:
                if (vaultName == null) {
                    debugPrint("vaultName is null.");
                    ok = false;
                }
                if (archiveId == null) {
                    debugPrint("archiveId is null.");
                    ok = false;
                }
                if (filename == null) {
                    debugPrint("filename is null.");
                    ok = false;
                }
                break;

            case Delete:
                if (vaultName == null) {
                    debugPrint("vaultName is null.");
                    ok = false;
                }
                if (archiveId == null) {
                    debugPrint("archiveId is null.");
                    ok = false;
                }
                break;
            case List:
                if (vaultName == null) {
                    debugPrint("vaultName is null.");
                    ok = false;
                }
                break;
            case Help:
                break;
            case Bad:
                break;
            default:
                throw new IllegalStateException();
        }
        return ok;
    }

    private void setCmdKind(ArchiveCmdKind cmd) {
        if (this.cmdKind == ArchiveCmdKind.Bad) {
            this.cmdKind = cmd;
        } else {
            /* オプション違反 */
            this.cmdKind = ArchiveCmdKind.Bad;
        }
    }

    private void execUpload() throws IOException {
        ArchiveController archiveController = new ArchiveController(region, awsPropFile);
        File uploadFile = new File(filename);
        String description = uploadFile.getName();
        UploadResult uploadResult = archiveController.upload(vaultName, description, uploadFile);
        if (printArchiveIdOnly) {
            System.out.println(uploadResult.getArchiveId());
        } else {
            System.out.println("Archive ID:" + uploadResult.getArchiveId());
        }
    }

    private void execDownload() throws IOException {
        ArchiveController archiveController = new ArchiveController(region, awsPropFile);
        archiveController.download(vaultName, archiveId, new File(filename));
        System.out.println("download success!");
    }

    private void execDelete() throws IOException {
        ArchiveController archiveController = new ArchiveController(region, awsPropFile);
        archiveController.delete(vaultName, archiveId);
        System.out.println("delete success!");
    }

    private void execList() throws IOException, InterruptedException, ParseException {
        JobOperator operator = new JobOperator(region, awsPropFile);
        operator.initiateInventoryJob(vaultName);
        if (operator.waitForJobToComplete()) {
            InventoryRetrievalResult result = operator.downloadInventoryJobOutput();
            System.out.println(result.toString());
        } else {
            System.out.println("Fault : get list.");
        }
    }

    boolean checkFileExist(String filename) {
        File file = new File(filename);
        return file.exists() && file.isFile();
    }

    /**
     * Downloadの時に使用する.特殊な動きをするので注意して使うこと。
     * 
     * @param filename 消したいファイル名.
     * @return 
     *         true:filenameで指定したファイルは存在しないか、削除済み(結果的に存在していない).false:指定したファイルは削除できなかった
     *         、もしくはオプションで保護されている.
     */
    boolean deleteFile(String filename) {
        if (checkFileExist(filename)) {
            if (force) {
                return new File(filename).delete();
            } else {
                return false;
            }
        } else {
            /* ファイルが無い=ファイルを削除済みとする */
            return true;
        }
    }

    @Override
    void onExecCommand() throws Exception {
        switch (cmdKind) {
            case Upload:
                /* 指定したファイルが存在してなかったり、ファイルじゃなかったりしたらエラー吐いて終了 */
                if (!checkFileExist(filename)) {
                    System.err.println(filename + " not found.");
                    System.exit(-1);
                }

                // vaultが存在しなかったらエラー吐いて終了.
                if (existVault(vaultName)) {
                    execUpload();
                } else {
                    System.err.println(vaultName + " is not exist.");
                }

                break;
            case Download:
                /*
                 * 指定したファイルがすでに存在していたらエラー吐いて終了. <br>
                 * ただし、forceフラグが立っていたら、ファイルを削除して作成する
                 * (消さなくてもAPI側で上書きしてくれるのかも).<br>
                 * ファイルが存在していて、実はファイルじゃなかった場合も適切なエラーを吐いて終了する.
                 */
                if (!deleteFile(filename)) {
                    System.err.println(filename + " is exist.");
                    System.exit(-1);
                }

                if (existVault(vaultName)) {
                    execDownload();

                } else {
                    System.err.println(vaultName + " is not exist.");
                }

                break;
            case Delete:
                if (existVault(vaultName)) {
                    execDelete();
                } else {
                    System.err.println(vaultName + " is not exist.");
                }

                break;
            case List:
                execList();
                break;
            case Bad:
                printHelp();
                break;

            default:
                throw new IllegalStateException("Unknown command.");
        }

        if (debug) {
            System.out.println("cmdKind : " + cmdKind);
            System.out.println("vaultName : " + vaultName);
            System.out.println("filename : " + filename);
            System.out.println("archiveId : " + archiveId);
            System.out.println("force : " + force);
        }
    }

    private void printListHelp() {
        System.out.println("Get Archive lsit");
        System.out.print("    ");
        System.out.println("java -jar list --vault vaultname");
    }

    private void printUploadHelp() {
        System.out.println("Upload Archive");
        System.out.print("    ");
        System.out
                .println("java -jar archive_controller.jar upload --vault vaultname --file filename");
        printHelpHelper("create", " --vault vaultname --file filename");
    }

    private void printDownloadHelp() {
        System.out.println("Download Archive");
        System.out.print("    ");
        System.out
                .println("java -jar archive_controller.jar download --vault vaultname --archive archiveId --file filename");
        printHelpHelper("create", "--vault vaultname --archive archiveId --file filename");
        System.out
                .println("If there is a file with the same name at the time of download, Force overwrite.");
        System.out
                .println("java -jar archive_controller.jar download --vault vaultname --archive archiveId --file filename -force");

    }

    private void printDeleteHelp() {
        System.out.println("Delete Archive");
        System.out.print("    ");
        System.out
                .println("java -jar archive_controller.jar delete --vault vaultname --archive archiveId");
        printHelpHelper("delete", " --vault vaultname --archive archiveId");
    }

    private void printHelpHelper(String kind, String opt) {
        System.out.println("Specify the region.");
        System.out.print("    ");
        System.out
                .println("java -jar archive_controller.jar " + kind + opt + " --region us-west-2");
        System.out.println("Specifies the AwsCredentials.properties file.");
        System.out.print("    ");
        System.out.println("java -jar archive_controller.jar " + kind + opt
                + " --properties myAwsPropFile.properties");
        System.out.println("Specify the region and AwsCredentials.properties.");
        System.out.print("    ");
        System.out.println("java -jar archive_controller.jar " + kind + opt
                + " --region us-west-2 --properties myAwsPropFile.properties");
    }

    private void printHelp() {
        System.out
                .println("java -jar archive_controller.jar cmd [--vault vaultname] [--archive archiveId] [--file filename] [--force] [--region region] [--properties prop_filename]");
        System.out.println();

        System.out.println("cmd          : upload | donwload | delete | list");
        System.out.println("--vault      : The name of the Vault.");
        System.out.println("--archive    : The ID of the archive.");
        System.out
                .println("--file       : Specifies the name of a file that is uploaded when the upload. When the download is the name of the saved file.");
        System.out
                .println("--force      : If there is a file with the same name at the time of download, Force overwrite.");
        System.out
                .println("--region     : us-east-1 | us-west-1 | us-west-2 | eu-west-1 | ap-northeast-1");
        System.out
                .println("--properties : If you want to specify explicitly AwsCredentials.properties");
        System.out.println();
        printUploadHelp();
        System.out.println();
        printDownloadHelp();
        System.out.println();
        printDeleteHelp();
        System.out.println();
        printListHelp();
        System.out.println();
        printRegion();

    }

    @Override
    void onExecInvalidParam() {
        switch (cmdKind) {
            case Upload:
                printUploadHelp();
                break;

            case Download:
                printDownloadHelp();
                break;

            case Delete:
                printDeleteHelp();
                break;

            case Bad:
                printHelp();
                break;

            default:
                throw new IllegalStateException();
        }

    }

    @Override
    void onAwsCredentialsPropertiesFileNotFound(String filename, Throwable e) {
        System.err.println(filename + " is not found.");
        setCmdKind(ArchiveCmdKind.Bad);
    }

    @Override
    void onRegionNotFound(String endpointStr, Throwable e) {
        System.err.println(e.getMessage() + " is not found.");
        setCmdKind(ArchiveCmdKind.Bad);
    }

    /**
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new ArchiveControllerCmd(args).exec();
    }

}


package jp.mironal.java.aws.app.glacier;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import jp.mironal.java.aws.app.glacier.AwsTools.Region;

import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.transfer.UploadResult;

public class ArchiveControllerCmd {

    static class Util {
        private Util() {
        }

        private static void printSpace4() {
            jp.mironal.java.aws.app.glacier.VaultControllerCmd.Util.printSpace4();
        }

        public static void printEndpointHelp() {
            jp.mironal.java.aws.app.glacier.VaultControllerCmd.Util.printEndpointHelp();

        }

        public static void printPropertiesHelp() {
            jp.mironal.java.aws.app.glacier.VaultControllerCmd.Util.printPropertiesHelp();
        }

        public static void printFileNotFoundError(String filename) {
            System.out.println(filename + " is not found.");
        }

        public static void printUploadHelp() {
            System.out.println("Upload archive");
            System.out.println();
            printSpace4();
            System.out
                    .println("java -jar ArchiveController.jar upload --vault vaultname --file filename");
            printSpace4();
            System.out.println("with endpoint");
            printSpace4();
            System.out
                    .println("java -jar ArchiveController.jar upload --vault vaultname --file filename --endpoint endpoint");
        }

        public static void printDownloadHelp() {
            System.out.println("Download archive");
            printSpace4();
            System.out
                    .println("java -jar ArchiveController.jar download --vault vaultname --archive archiveId --file filename");
            printSpace4();
            System.out.println("with endpoint");
            printSpace4();
            System.out
                    .println("java -jar ArchiveController.jar download --vault vaultname --archive archiveId --file filename --endpoint endpoint");
            printSpace4();
            System.out.println("force download");
            printSpace4();
            printSpace4();
            System.out.println("add --force option");
        }

        public static void printDeleteHelp() {
            System.out.println("Delete archive");
            printSpace4();
            System.out
                    .println("java -jar ArchiveController.jar delete --vault vaultname --archive archiveId");
            printSpace4();
            System.out.println("with endpoint");
            printSpace4();
            System.out
                    .println("java -jar ArchiveController.jar delete --vault vaultname --archive archiveId --endpoint endpoint");

        }

        public static void printHelp() {
            printUploadHelp();
            System.out.println();
            printDownloadHelp();
            System.out.println();
            printDeleteHelp();
            System.err.println();
            printEndpointHelp();
        }

        public static void printUnknownCommand() {
            jp.mironal.java.aws.app.glacier.VaultControllerCmd.Util.printUnKnownCommand();
        }

        public static void printFileIsNotFileError(String filename) {
            System.out.println(filename + " is not file.");
        }

        public static void printFileExistError(String filename) {
            System.out.println(filename + " is exist.");
        }

        public static boolean existVault(String vaultName, VaultController vaultController) {
            List<DescribeVaultOutput> describeVaultOutputs = vaultController.listVaults();
            for (DescribeVaultOutput output : describeVaultOutputs) {
                if (output.getVaultName().equals(vaultName)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean checkVaultAndPrintErr(String vaultName,
                VaultController vaultController) {
            if (!existVault(vaultName, vaultController)) {
                System.err.println(vaultName + " is not exist.");
                return false;
            }
            return true;
        }
    }

    enum Kind {
        Bad, Upload, Download, Delete
    }

    /**
     * java -jar ArchiveController.jar upload --vault vaultname --file filename
     * --endpoint endpoint<br>
     * java -jar ArchiveController.jar download --vault vaultname --archive
     * archiveId --file filename<br>
     * java -jar ArchiveController.jar download --vault vaultname --archive
     * archiveId --file filename --force java -jar ArchiveController.jar delete
     * --vault vaultname --archive archiveId<br>
     * 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        Kind cmdKind = Kind.Bad;
        String endpointStr = null;
        String vaultName = null;
        String filename = null;
        String archiveId = null;
        String propertiesName = null;
        boolean debug = false;
        boolean force = false; /* アーカイブダウンロード時に同名のファイルが既に有った場合、強制的に上書きする */
        boolean printArchiveIdOnly = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            /*
             * すでに別のオプションが割り当てられていたら不正な引数とみなす.
             */
            if (arg.equals("upload")) {
                if (cmdKind == Kind.Bad) {
                    cmdKind = Kind.Upload;
                } else {
                    /* オプション違反 */
                    cmdKind = Kind.Bad;
                    break;
                }
            }

            if (arg.equals("download")) {
                if (cmdKind == Kind.Bad) {
                    cmdKind = Kind.Download;
                } else {
                    cmdKind = Kind.Bad;
                    break;
                }
            }

            if (arg.equals("delete")) {
                if (cmdKind == Kind.Bad) {
                    cmdKind = Kind.Delete;
                } else {
                    cmdKind = Kind.Bad;
                    break;
                }
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

            if (arg.equals("--endpoint")) {
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
                Util.printHelp();
                System.exit(0);
            }

            if (arg.equals("--force")) {
                force = true;
            }

            if (arg.equals("--debug")) {
                debug = true;
            }
        }

        if (propertiesName == null) {
            propertiesName = VaultController.AWS_PROPERTIES_FILENAME;
        }

        File propFile = new File(propertiesName);
        if (propFile.isDirectory()) {
            propFile = new File(propertiesName + File.separator
                    + VaultController.AWS_PROPERTIES_FILENAME);
        }
        if (!propFile.exists()) {
            /* 設定ファイルがなかったらhelpを表示して終了. */
            Util.printPropertiesHelp();
            System.exit(-1);
        }

        Region region = null;
        /* endpoint */
        if (endpointStr == null) {
            region = ArchiveController.getDefaultEndpoint();
        } else {
            if (ArchiveController.containEndpoint(endpointStr)) {
                region = ArchiveController.convertToRegion(endpointStr);
            } else {
                Util.printEndpointHelp();
                System.exit(-1);
            }
        }

        ArchiveController archiveController = new ArchiveController(region, propFile);
        VaultController vaultController = new VaultController(region, propFile);

        switch (cmdKind) {
            case Upload:
                /* オプションが揃ってるかチェック */
                if ((vaultName == null) || (filename == null)) {
                    Util.printUploadHelp();
                    System.exit(-1);
                }

                /* 指定したファイルが存在してなかったり、ファイルじゃなかったりしたらエラー吐いて終了 */
                File uploadFile = new File(filename);
                if (!uploadFile.exists()) {
                    Util.printFileNotFoundError(filename);
                    System.exit(-1);
                }
                if (!uploadFile.isFile()) {
                    Util.printFileIsNotFileError(filename);
                    System.exit(-1);
                }

                /* vaultの存在確認. なかったらエラー吐いて終了 */
                if (!Util.checkVaultAndPrintErr(vaultName, vaultController)) {
                    System.exit(-1);
                }

                String description = new Date().toString();
                UploadResult uploadResult = archiveController.upload(vaultName, description,
                        uploadFile);
                if (printArchiveIdOnly) {
                    System.out.println(uploadResult.getArchiveId());
                } else {
                    System.out.println("Archive ID:" + uploadResult.getArchiveId());
                }
                break;
            case Download:
                /* オプションが揃ってるかチェック */
                if ((vaultName == null) || (filename == null) || (archiveId == null)) {
                    Util.printDownloadHelp();
                    System.exit(-1);
                }
                /*
                 * 指定したファイルがすでに存在していたらエラー吐いて終了. <br>
                 * ただし、forceフラグが立っていたら、ファイルを削除して作成する
                 * (ぶっちゃけ消さなくてもAPI側で上書きしてくれるのかも).<br>
                 * ファイルが存在していて、実はファイルじゃなかった場合も適切なエラーを吐いて終了する.
                 */
                File downloadFile = new File(filename);
                if (downloadFile.exists()) {
                    if (downloadFile.isFile()) {
                        if (force) {
                            downloadFile.delete();
                        } else {
                            Util.printFileExistError(filename);
                            System.exit(-1);
                        }
                    } else {
                        Util.printFileIsNotFileError(filename);
                        System.exit(-1);
                    }
                }

                /* vaultの存在確認. なかったらエラー吐いて終了 */
                if (!Util.checkVaultAndPrintErr(vaultName, vaultController)) {
                    System.exit(-1);
                }

                archiveController.download(vaultName, archiveId, downloadFile);
                System.out.println("download success!");

                break;
            case Delete:
                /* オプションが揃ってるかチェック */
                if ((vaultName == null) || (archiveId == null)) {
                    Util.printDeleteHelp();
                    System.exit(-1);
                }

                /* vaultの存在確認. なかったらエラー吐いて終了 */
                if (!Util.checkVaultAndPrintErr(vaultName, vaultController)) {
                    System.exit(-1);
                }

                archiveController.delete(vaultName, archiveId);
                System.out.println("delete success!");
                break;
            case Bad:
                Util.printHelp();
                break;
            default:
                Util.printUnknownCommand();
                break;
        }

        if (debug) {
            System.out.println("cmdKind : " + cmdKind);
            System.out.println("endpoint : " + endpointStr);
            System.out.println("vaultName : " + vaultName);
            System.out.println("filename : " + filename);
            System.out.println("archiveId : " + archiveId);
            System.out.println("force : " + force);
        }
    }
}

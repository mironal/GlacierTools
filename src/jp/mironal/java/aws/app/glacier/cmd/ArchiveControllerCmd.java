
package jp.mironal.java.aws.app.glacier.cmd;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import jp.mironal.java.aws.app.glacier.ArchiveController;
import jp.mironal.java.aws.app.glacier.AwsTools.AwsService;
import jp.mironal.java.aws.app.glacier.AwsTools.Region;
import jp.mironal.java.aws.app.glacier.GlacierTools;
import jp.mironal.java.aws.app.glacier.VaultController;

import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.transfer.UploadResult;

public class ArchiveControllerCmd extends CmdUtils {

    enum ArchiveCmdKind {
        Bad, Upload, Download, Delete
    }

    ArchiveCmdKind cmdKind = ArchiveCmdKind.Bad;

    String vaultName = null;
    String filename = null;
    String archiveId = null;
    boolean force = false; /* アーカイブダウンロード時に同名のファイルが既に有った場合、強制的に上書きする */
    boolean printArchiveIdOnly = false;

    File uploadFile = null;

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

    boolean existVault(String vaultName, VaultController vaultController) {
        List<DescribeVaultOutput> describeVaultOutputs = vaultController.listVaults();
        for (DescribeVaultOutput output : describeVaultOutputs) {
            if (output.getVaultName().equals(vaultName)) {
                return true;
            }
        }
        return false;
    }

    boolean checkVaultAndPrintErr(String vaultName) throws IOException {
        VaultController controller = new VaultController(region, awsPropFile);
        if (!existVault(vaultName, controller)) {
            System.err.println(vaultName + " is not exist.");
            return false;
        }
        return true;
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

            default:
                break;
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
        String description = new Date().toString();
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

    private void execBad() {

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

    private void printInvalidParam() {
        if (awsPropFile == null) {
            System.err.println(ArchiveController.AWS_PROPERTIES_FILENAME + " file is not found.");
        }
        if (region == null) {
            System.err.println("Region is not found.");
            for (Region r : GlacierTools.getGlacierRegions().values()) {
                System.err.print("    ");
                System.err.println(r.getEndpoint() + " => "
                        + VaultController.makeUrl(AwsService.Glacier, r));
            }
        }

        switch (cmdKind) {
            case Upload:

                break;
            case Download:
                break;
            case Delete:

            default:
                break;
        }
    }

    @Override
    void onExecCommand() throws Exception {
        switch (cmdKind) {
            case Upload:

                /* 指定したファイルが存在してなかったり、ファイルじゃなかったりしたらエラー吐いて終了 */
                if (!checkFileExist(filename)) {
                    System.exit(-1);
                }

                execUpload();
                break;
            case Download:

                /*
                 * 指定したファイルがすでに存在していたらエラー吐いて終了. <br>
                 * ただし、forceフラグが立っていたら、ファイルを削除して作成する
                 * (ぶっちゃけ消さなくてもAPI側で上書きしてくれるのかも).<br>
                 * ファイルが存在していて、実はファイルじゃなかった場合も適切なエラーを吐いて終了する.
                 */
                if (!deleteFile(filename)) {
                    System.exit(-1);
                }

                execDownload();

                break;
            case Delete:

                execDelete();
                break;

            case Bad:
                execBad();
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

    @Override
    void onExecInvalidParam() {
        System.exit(-1);
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
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new ArchiveControllerCmd(args).exec();
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
}

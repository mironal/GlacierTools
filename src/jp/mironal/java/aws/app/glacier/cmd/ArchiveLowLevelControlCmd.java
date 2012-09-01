
package jp.mironal.java.aws.app.glacier.cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import jp.mironal.java.aws.app.glacier.ArchiveController;
import jp.mironal.java.aws.app.glacier.AwsTools.Region;
import jp.mironal.java.aws.app.glacier.InventoryRetrievalResult;
import jp.mironal.java.aws.app.glacier.JobRestoreParam;
import jp.mironal.java.aws.app.glacier.LowLevelArchiveController;
import jp.mironal.java.aws.app.glacier.StateLessJobOperations;
import jp.mironal.java.aws.app.glacier.VaultController;

import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.GlacierJobDescription;

/**
 * テストしやすいようにフィールドやメソッドのスコープをデフォルトにしている.<br>
 * このクラスのインスタタンスはこのパッケージでしか作れないのでとりあえず安全である.
 * 
 * @author yama
 */
public class ArchiveLowLevelControlCmd {

    enum Kind {
        Bad, Inventory, Archive, List, Describe,
    }

    enum Sync {
        Sync, Async,
    }

    Kind cmdKind = Kind.Bad;
    Sync syncType = Sync.Sync;
    String vaultname = null;
    String archiveId = null;

    String jobId = null;
    String saveFile = null;
    Region region = null;
    File awsPropFile;
    boolean debug = false;

    private void setCmdKind(Kind cmd) {
        if (this.cmdKind == Kind.Bad) {
            this.cmdKind = cmd;
        } else {
            /* オプション違反 */
            this.cmdKind = Kind.Bad;
        }
    }

    ArchiveLowLevelControlCmd(String[] args) {
        String endpointStr = null;
        String propertiesName = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("inventory-retrieval")) {
                setCmdKind(Kind.Inventory);
            }

            if (arg.equals("archive-retrieval")) {
                setCmdKind(Kind.Archive);
            }

            if (arg.equals("list")) {
                setCmdKind(Kind.List);
            }

            if (arg.equals("desc")) {
                setCmdKind(Kind.Describe);
            }

            if (arg.equals("async")) {
                syncType = Sync.Async;
            }

            if (arg.equals("--vault")) {
                if ((i + 1) < args.length) {
                    i++;
                    vaultname = args[i];
                }
            }

            if (arg.equals("--archive")) {
                if ((i + 1) < arg.length()) {
                    i++;
                    archiveId = args[i];
                }
            }

            if (arg.equals("--job")) {
                if ((i + 1) < arg.length()) {
                    i++;
                    jobId = args[i];
                }
            }

            if (arg.equals("--endpoint")) {
                if ((i + 1) < args.length) {
                    i++;
                    endpointStr = args[i];
                }
            }
            if (arg.equals("--file")) {
                if ((i + 1) < args.length) {
                    i++;
                    saveFile = args[i];
                }
            }

            if (arg.equals("--properties")) {
                if ((i + 1) < args.length) {
                    i++;
                    propertiesName = args[i];
                }
            }
            if (arg.equals("--debug")) {
                debug = true;
            }
        }

        try {
            awsPropFile = getAwsCredentialsPropertiesFile(propertiesName);
        } catch (FileNotFoundException e) {
            if (debug) {
                System.out.println(e.getMessage());
            }
            cmdKind = Kind.Bad;
        }

        /* endpoint */
        try {
            region = getRegion(endpointStr);
        } catch (InvalidRegionException e) {
            if (debug) {
                System.out.println(e.getMessage() + " is not found.");
            }
            cmdKind = Kind.Bad;
        }
    }

    File getAwsCredentialsPropertiesFile(String propertiesName) throws FileNotFoundException {
        if (propertiesName == null) {
            propertiesName = VaultController.AWS_PROPERTIES_FILENAME;
        }

        File propFile = new File(propertiesName);
        if (propFile.isDirectory()) {
            propFile = new File(propertiesName + File.separator
                    + VaultController.AWS_PROPERTIES_FILENAME);
        }
        if (!propFile.exists()) {
            throw new FileNotFoundException(propFile.getAbsolutePath() + "is not found.");
        }

        return propFile;
    }

    @SuppressWarnings("serial")
    class InvalidRegionException extends RuntimeException {

        public InvalidRegionException(String msg) {
            super(msg);
        }
    }

    Region getRegion(String endpointStr) throws InvalidRegionException {
        Region region = null;
        if (endpointStr == null) {
            region = ArchiveController.getDefaultEndpoint();
        } else {
            if (ArchiveController.containEndpoint(endpointStr)) {
                region = ArchiveController.convertToRegion(endpointStr);
            } else {
                throw new InvalidRegionException(endpointStr);
            }
        }
        return region;
    }

    private void printUnknownCmd() {

    }

    /**
     * エラーなどでプログラムを終了させるときはこの関数で終了させるようにする.
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws ParseException
     */
    private void exec() throws IOException, InterruptedException, ParseException {

        if (!validateInventoryParam()) {
            System.exit(-1);
        }
        switch (cmdKind) {
            case Inventory:

                execInventory();
                break;
            case List:
                execList();
                break;
            case Archive:
                execArchive();
                break;
            case Describe:
                execDescribe();
                break;
            case Bad:
                execBad();
                break;
            default:
                throw new IllegalStateException("Unkonwn cmd");
        }
    }

    private void execBad() {

    }

    private void execDescribe() throws IOException {
        StateLessJobOperations controller = new StateLessJobOperations(region, awsPropFile);
        DescribeJobResult result = controller.describeJob(vaultname, jobId);
        printDescribeJob(result);
    }

    private void execArchiveSync() throws IOException, InterruptedException {
        LowLevelArchiveController controller = new LowLevelArchiveController(region, awsPropFile);
        controller.initiateArchiveJob(vaultname, archiveId);
        boolean successful = controller.waitForJobToComplete();
        if (successful) {
            controller.downloadArchiveJobOutput(new File(saveFile));
        } else {
            System.out.println("download fault.");
        }
    }

    private void execArchiveAsync() throws IOException {
        LowLevelArchiveController controller = new LowLevelArchiveController(region, awsPropFile);
        controller.initiateArchiveJob(vaultname, archiveId);
        printJobRestoreParam(controller);
    }

    private void execArchive() throws IOException, InterruptedException {

        switch (syncType) {
            case Sync:
                execArchiveSync();
                break;
            case Async:
                execArchiveAsync();
                break;
            default:
                throw new IllegalStateException("Unknown type");
        }

    }

    private void execList() throws IOException {

        StateLessJobOperations controller = new StateLessJobOperations(region, awsPropFile);
        List<GlacierJobDescription> jobs = controller.listJobs(vaultname);
        if (jobs.size() > 0) {
            for (GlacierJobDescription job : jobs) {
                System.out.println();
                printGlacierJobDescriptionf(job);
            }
        } else {
            System.out.println("There is no Job.");
        }
    }

    private void execInventorySync() throws IOException, InterruptedException, ParseException {
        LowLevelArchiveController controller = new LowLevelArchiveController(region, awsPropFile);

        String jobId = controller.initiateInventoryJob(vaultname);
        System.out.println("JobID=" + jobId);
        boolean successful = controller.waitForJobToComplete();
        if (successful) {
            InventoryRetrievalResult result = controller.downloadInventoryJobOutput();
            System.out.println(result.toString());
        } else {
            System.out.println("Error : Job complete failed.");
        }
    }

    private void execInventoryAsync() throws IOException {

        LowLevelArchiveController controller = new LowLevelArchiveController(region, awsPropFile);
        controller.initiateInventoryJob(vaultname);
        printJobRestoreParam(controller);

    }

    /**
     * @return
     */
    boolean validateInventoryParam() {
        // trueを代入するロジックは初期化のところだけとする.
        boolean ok = true;
        if (region == null) {
            ok = false;
        }
        if (awsPropFile == null) {
            ok = false;
        }
        switch (cmdKind) {
            case Archive:
                if (vaultname == null) {
                    ok = false;
                }
                if (archiveId == null) {
                    ok = false;
                }
                switch (syncType) {
                    case Sync:
                        if (saveFile == null) {
                            ok = false;
                        }
                        break;

                    default:
                        break;
                }
                break;
            case Inventory:
                if (vaultname == null) {
                    ok = false;
                }

                break;
            case List:
                if (vaultname == null) {
                    ok = false;
                }
                break;
            case Describe:
                if (vaultname == null) {
                    ok = false;
                }
                if (jobId == null) {
                    ok = false;
                }
                break;

            default:
                break;
        }

        return ok;
    }

    private void execInventory() throws IOException, InterruptedException, ParseException {
        switch (syncType) {
            case Sync:
                execInventorySync();
                break;

            case Async:
                execInventoryAsync();
                break;

            default:
                break;
        }
    }

    private void printJobRestoreParam(LowLevelArchiveController controller) {
        JobRestoreParam param = controller.getJobRestoreParam();
        System.out.println("JobId=" + param.getJobId());
        System.out.println("VaultName=" + param.getVaultName());

    }

    private void printDescribeJob(DescribeJobResult result) {
        System.out.println("Action               : " + result.getAction());
        System.out.println("ArchiveId            : " + result.getArchiveId());
        System.out.println("ArchiveSizeInBytes   : " + result.getArchiveSizeInBytes());
        System.out.println("Completed            : " + result.getCompleted());
        System.out.println("CompletionDate       : " + result.getCompletionDate());
        System.out.println("CreationDate         : " + result.getCreationDate());
        System.out.println("InventorySizeInBytes : " + result.getInventorySizeInBytes());
        System.out.println("JobDescription       : " + result.getJobDescription());
        System.out.println("JobId                : " + result.getJobId());
        System.out.println("SHA256TreeHash       : " + result.getSHA256TreeHash());
        System.out.println("SNSTopic             : " + result.getSNSTopic());
        System.out.println("StatusCode           : " + result.getStatusCode());
        System.out.println("StatusMessage        : " + result.getStatusMessage());
        System.out.println("VaultARN             : " + result.getVaultARN());
    }

    private void printGlacierJobDescriptionf(GlacierJobDescription description) {
        System.out.println("Action               : " + description.getAction());
        System.out.println("ArchiveId            : " + description.getArchiveId());
        System.out.println("ArchiveSizeInBytes   : " + description.getArchiveSizeInBytes());
        System.out.println("Completed            : " + description.getCompleted());
        System.out.println("CompletionDate       : " + description.getCompletionDate());
        System.out.println("CreationDate         : " + description.getCreationDate());
        System.out.println("InventorySizeInBytes : " + description.getInventorySizeInBytes());
        System.out.println("JobDescription       : " + description.getJobDescription());
        System.out.println("JobId                : " + description.getJobId());
        System.out.println("SHA256TreeHash       : " + description.getSHA256TreeHash());
        System.out.println("SNSTopic             : " + description.getSNSTopic());
        System.out.println("StatusCode           : " + description.getStatusCode());
        System.out.println("StatusMessage        : " + description.getStatusMessage());
        System.out.println("VaultARN             : " + description.getVaultARN());
    }

    /**
     * [] is must.<br>
     * {} is option.<br>
     * <br>
     * java -jar lowlevelControl.jar [jobType] {syncType}<br>
     * {--vault vaultname}<br>
     * {--archive archiveId}<br>
     * {--job jobId}<br>
     * {--endpoint endpoint}<br>
     * {--properties properties_filename}<br>
     * <br>
     * <br>
     * <br>
     * 
     * @param args
     * @throws IOException
     * @throws InterruptedException
     * @throws ParseException
     */
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        new ArchiveLowLevelControlCmd(args).exec();
    }

}

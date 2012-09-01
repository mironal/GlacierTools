
package jp.mironal.java.aws.app.glacier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import jp.mironal.java.aws.app.glacier.AwsTools.Region;

import org.codehaus.jackson.JsonParseException;

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
    String endpointStr = null;
    String propertiesName = null;
    String jobId = null;

    private void setCmdKind(Kind cmd) {
        if (this.cmdKind == Kind.Bad) {
            this.cmdKind = cmd;
        } else {
            /* オプション違反 */
            this.cmdKind = Kind.Bad;
        }
    }

    ArchiveLowLevelControlCmd(String[] args) {

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

            if (arg.equals("--properties")) {
                if ((i + 1) < args.length) {
                    i++;
                    propertiesName = args[i];
                }
            }

        }
    }

    File getAwsCredentialsPropertiesFile() throws FileNotFoundException {
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
    class InvalidRegionException extends Exception {

        public InvalidRegionException(String msg) {
            super(msg);
        }
    }

    Region getRegion() throws InvalidRegionException {
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

    private void exec() throws IOException, InterruptedException {

        File awsPropFile = getAwsCredentialsPropertiesFile();

        /* endpoint */
        Region region = null;
        try {
            region = getRegion();
        } catch (InvalidRegionException e) {
            System.out.println(e.getMessage() + " is not found.");
            System.exit(-1);
        }

        LowLevelArchiveController controller = new LowLevelArchiveController(region, awsPropFile);

        switch (cmdKind) {
            case Inventory:
                switch (syncType) {
                    case Sync:
                        execInventory(controller);
                        break;
                    case Async:
                        execInventoryASync(controller);
                        break;
                    default:
                        printUnknownCmd();
                        break;
                }
                break;
            case List:
                if (vaultname == null) {
                    System.exit(-1);
                }
                execListJobs(controller);
                break;
            default:
                break;
        }
    }

    private void execInventory(LowLevelArchiveController controller) throws JsonParseException,
            IOException, InterruptedException {
        try {
            String jobId = controller.initiateInventoryJob(vaultname);
            System.out.println("JobID=" + jobId);

            if (controller.waitForJobToComplete()) {
                System.out.println("job complete!");
                // controller.printJobOutput();
            } else {
                System.out.println("job fault");
            }
        } finally {
            controller.cleanUp();
        }

    }

    private void execInventoryASync(LowLevelArchiveController controller) {

    }

    private void printJobRestoreParam(LowLevelArchiveController controller) {
        JobRestoreParam param = controller.getJobRestoreParam();
        System.out.println("JobId=" + param.getJobId());
        System.out.println("VaultName=" + param.getVaultName());
        System.out.println("SnsSubscriptionArn=" + param.getSnsSubscriptionArn());
        System.out.println("SnsTopicArn=" + param.getSnsTopicArn());
        System.out.println("SqsQueueUrl=" + param.getSqsQueueUrl());
    }

    private void execListJobs(LowLevelArchiveController controller) {
        /*
         * List<GlacierJobDescription> jobs = controller.listJobs(vaultname); if
         * (jobs.size() > 0) { for (GlacierJobDescription job : jobs) {
         * System.out.println(); printGlacierJobDescriptionf(job); } } else {
         * System.out.println("There is no Job."); }
         */
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
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        new ArchiveLowLevelControlCmd(args).exec();
    }

}

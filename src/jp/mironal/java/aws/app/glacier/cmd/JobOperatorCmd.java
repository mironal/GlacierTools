
package jp.mironal.java.aws.app.glacier.cmd;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import jp.mironal.java.aws.app.glacier.InventoryRetrievalResult;
import jp.mironal.java.aws.app.glacier.JobOperator;
import jp.mironal.java.aws.app.glacier.JobRestoreParam;
import jp.mironal.java.aws.app.glacier.StateLessJobOperator;

import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.GlacierJobDescription;

/**
 * テストしやすいようにフィールドやメソッドのスコープをデフォルトにしている.<br>
 * このクラスのインスタタンスはこのパッケージでしか作れないのでとりあえず安全である.
 * 
 * @author yama
 */
public class JobOperatorCmd extends CmdUtils {

    enum JobOperatorCmdKind {
        Bad, Inventory, Archive, List, Describe, Help

    }

    enum Sync {
        Sync, Async,
    }

    JobOperatorCmdKind cmdKind = JobOperatorCmdKind.Bad;
    Sync syncType = Sync.Sync;
    String vaultname = null;
    String archiveId = null;

    String jobId = null;
    String filename = null;

    JobOperatorCmd(String[] args) {
        super(args);
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("inventory-retrieval")) {
                setCmdKind(JobOperatorCmdKind.Inventory);
            }

            if (arg.equals("archive-retrieval")) {
                setCmdKind(JobOperatorCmdKind.Archive);
            }

            if (arg.equals("list")) {
                setCmdKind(JobOperatorCmdKind.List);
            }

            if (arg.equals("desc")) {
                setCmdKind(JobOperatorCmdKind.Describe);
            }

            if (arg.equals("-h") || arg.equals("--help") || arg.equals("help")) {
                setCmdKind(JobOperatorCmdKind.Help);
            }

            if (arg.equals("--async")) {
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

            if (arg.equals("--file")) {
                if ((i + 1) < args.length) {
                    i++;
                    filename = args[i];
                }
            }

            if (arg.equals("--debug")) {
                debug = true;
            }
        }
    }

    private void setCmdKind(JobOperatorCmdKind cmd) {
        if (this.cmdKind == JobOperatorCmdKind.Bad) {
            this.cmdKind = cmd;
        } else {
            /* オプション違反 */
            this.cmdKind = JobOperatorCmdKind.Bad;
        }
    }

    private void execDescribe() throws IOException {
        StateLessJobOperator controller = new StateLessJobOperator(region, awsPropFile);
        DescribeJobResult result = controller.describeJob(vaultname, jobId);
        printDescribeJob(result);
    }

    private void execArchiveSync() throws IOException, InterruptedException {
        JobOperator controller = new JobOperator(region, awsPropFile);
        controller.initiateArchiveJob(vaultname, archiveId);
        boolean successful = controller.waitForJobToComplete();
        if (successful) {
            controller.downloadArchiveJobOutput(new File(filename));
        } else {
            System.out.println("download fault.");
        }
    }

    private void execArchiveAsync() throws IOException {
        JobOperator controller = new JobOperator(region, awsPropFile);
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

        StateLessJobOperator controller = new StateLessJobOperator(region, awsPropFile);
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
        JobOperator controller = new JobOperator(region, awsPropFile);

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
        JobOperator controller = new JobOperator(region, awsPropFile);
        controller.initiateInventoryJob(vaultname);
        printJobRestoreParam(controller);
    }

    /**
     * @return
     */
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
            case Archive:
                if (vaultname == null) {
                    ok = false;
                }
                if (archiveId == null) {
                    ok = false;
                }
                switch (syncType) {
                    case Sync:
                        if (filename == null) {
                            ok = false;
                        }
                        break;

                    default:
                        break;
                }
                break;

            case Inventory:
                if (vaultname == null) {
                    debugPrint("vaultname is null.");
                    ok = false;
                }

                break;

            case List:
                if (vaultname == null) {
                    debugPrint("vaultname is null.");
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
            case Help:
                break;
            case Bad:
                break;
            default:
                throw new IllegalStateException("Unknown type." + cmdKind);
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
                throw new IllegalStateException("Unknown type.");
        }
    }

    private void printJobRestoreParam(JobOperator controller) {
        JobRestoreParam param = controller.getJobRestoreParam();
        System.out.println("JobId=" + param.getJobId());
        System.out.println("VaultName=" + param.getVaultName());
        System.out.println("Region=" + param.getRegion().getEndpoint());

    }

    @Override
    void onAwsCredentialsPropertiesFileNotFound(String filename, Throwable e) {
        System.err.println(filename + " is not found.");
        setCmdKind(JobOperatorCmdKind.Bad);
    }

    @Override
    void onRegionNotFound(String endpointStr, Throwable e) {
        System.err.println(e.getMessage() + " is not found.");
        setCmdKind(JobOperatorCmdKind.Bad);
    }

    @Override
    void onExecCommand() throws Exception {
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
                printHelp();
                break;
            default:
                throw new IllegalStateException("Unkonwn cmd.");
        }
    }

    private void printInventoryHelp() {
        System.out.println("Get Vault inventory.");
        System.out.print("    ");
        System.out.println("java -jar job_operator.jar inventory --vault vaultname");
        printHelpHelper("inventory", " --vault vaultname");
    }

    private void printArchiveHelp() {
        System.out.println("Download Archive.");
        System.out.print("    ");
        System.out
                .println("java -jar job_operator.jar archive --vault vaultname --archive archiveId --file filename");
        printHelpHelper("archive", " --vault vaultname --archive archiveId --file filename");
    }

    private void printListHelp() {
        System.out.println("Get Job list.");
        System.out.print("    ");
        System.out.println("java -jar job_operator.jar list --vault vaultname");
        printHelpHelper("list", " --vault vaultname");
    }

    private void printDescriveHelp() {
        System.out.println("Get Job Describe.");
        System.out.print("    ");
        System.out.println("java -jar job_operator.jar desc --vault vaultname --job jobId");
        printHelpHelper("desc", " --vault vaultname --job jobId");
    }

    private void printHelp() {
        System.out
                .println("java -jar job_operator.jar cmd [--vault vaultname]　[--archive archiveId] [--file filename] [--job jobId] [--region region] [--properties prop_filename] [--async]");
        System.out.println();
        System.out.println("cmd          : inventory | archive | list | desc | help");
        System.out.println("--vault      : The name of the Vault.");
        System.out.println("--archive    : The ID of the Archive.");
        System.out.println("--job        : The ID of the Job.");
        System.out.println("--file       : Save file name.");
        System.out
                .println("--region     : us-east-1 | us-west-1 | us-west-2 | eu-west-1 | ap-northeast-1");
        System.out
                .println("--properties : If you want to specify explicitly AwsCredentials.properties.");
        System.out.println();
        printInventoryHelp();
        System.out.println();
        printArchiveHelp();
        System.out.println();
        printListHelp();
        System.out.println();
        printDescriveHelp();
        System.out.println();
        printRegion();
    }

    private void printHelpHelper(String kind, String opt) {
        System.out.println("Specify the region.");
        System.out.print("    ");
        System.out.println("java -jar job_operator.jar " + kind + opt + " --region us-west-2");
        System.out.println("Specifies the AwsCredentials.properties file.");
        System.out.print("    ");
        System.out.println("java -jar job_operator.jar " + kind + opt
                + " --properties myAwsPropFile.properties");
        System.out.println("Specify the region and AwsCredentials.properties.");
        System.out.print("    ");
        System.out.println("java -jar job_operator.jar " + kind + opt
                + " --region us-west-2 --properties myAwsPropFile.properties");
    }

    @Override
    void onExecInvalidParam() {
        switch (cmdKind) {
            case Inventory:
                printInventoryHelp();
                break;
            case Archive:
                printArchiveHelp();
                break;
            case List:
                printListHelp();
                break;
            case Describe:
                printDescriveHelp();
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new JobOperatorCmd(args).exec();
    }

}

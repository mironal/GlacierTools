
package jp.mironal.java.aws.app.glacier.cmd;

import java.io.IOException;
import java.util.List;

import jp.mironal.java.aws.app.glacier.VaultController;

import com.amazonaws.services.glacier.model.CreateVaultResult;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.DescribeVaultResult;

public class VaultControllerCmd extends CmdUtils {

    enum VaultCmdKind {
        Bad, Create, Describe, List, Delete, Help,
    }

    VaultCmdKind cmdKind = VaultCmdKind.Bad;
    String vaultName = null;

    VaultControllerCmd(String[] args) {
        super(args);

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("create")) {
                setCmdKind(VaultCmdKind.Create);
            }

            if (arg.equals("desc")) {
                setCmdKind(VaultCmdKind.Describe);
            }

            if (arg.equals("list")) {
                setCmdKind(VaultCmdKind.List);
            }

            if (arg.equals("delete")) {
                setCmdKind(VaultCmdKind.Delete);
            }

            if (arg.equals("-h") || arg.equals("--help") || arg.equals("help")) {
                setCmdKind(VaultCmdKind.Help);
            }

            if (arg.equals("--vault")) {
                if ((i + 1) < args.length) {
                    i++;
                    vaultName = args[i];
                }
            }
        }
    }

    private void setCmdKind(VaultCmdKind cmd) {
        if (this.cmdKind == VaultCmdKind.Bad) {
            this.cmdKind = cmd;
        } else {
            /* オプション違反 */
            this.cmdKind = VaultCmdKind.Bad;
        }
    }

    @Override
    void onAwsCredentialsPropertiesFileNotFound(String filename, Throwable e) {
        setCmdKind(VaultCmdKind.Bad);
    }

    @Override
    void onRegionNotFound(String endpointStr, Throwable e) {
        setCmdKind(VaultCmdKind.Bad);
    }

    @Override
    boolean validateParam() {
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
            case Create:
                if (vaultName == null) {
                    debugPrint("vaultName is null.");
                    ok = false;
                }

                break;
            case Describe:
                if (vaultName == null) {
                    debugPrint("vaultName is null.");
                    ok = false;
                }

                break;
            case List:
                break;

            case Delete:
                if (vaultName == null) {
                    debugPrint("vaultName is null.");
                    ok = false;
                }
                break;
            case Bad:
                break;
            case Help:
                break;

            default:
                throw new IllegalStateException("Unknown command.");
        }

        return ok;
    }

    private void execCreate() throws IOException {
        if (!isDebug()) {
            VaultController controller = new VaultController(region, awsPropFile);
            CreateVaultResult createVaultResult = controller.createVault(vaultName);
            System.out.println("Success!");
            System.out.println("Location:" + createVaultResult.getLocation());
        } else {
            System.out.println("execute create");
        }
    }

    private void execDescribe() throws IOException {
        if (!isDebug()) {
            VaultController controller = new VaultController(region, awsPropFile);
            DescribeVaultResult describeVaultResult = controller.describeVault(vaultName);
            System.out.println("Describing the vault: " + vaultName);
            System.out.print("CreationDate: " + describeVaultResult.getCreationDate()
                    + "\nLastInventoryDate: " + describeVaultResult.getLastInventoryDate()
                    + "\nNumberOfArchives: " + describeVaultResult.getNumberOfArchives()
                    + "\nSizeInBytes: " + describeVaultResult.getSizeInBytes() + "\nVaultARN: "
                    + describeVaultResult.getVaultARN() + "\nVaultName: "
                    + describeVaultResult.getVaultName());
            System.out.println();
        } else {
            System.out.println("execute describe");
        }
    }

    private void execList() throws IOException {
        if (!isDebug()) {
            VaultController controller = new VaultController(region, awsPropFile);
            List<DescribeVaultOutput> vaultList = controller.listVaults();
            System.out.println("\nDescribing all vaults (vault list):");
            for (DescribeVaultOutput vault : vaultList) {
                System.out.println("\nCreationDate: " + vault.getCreationDate()
                        + "\nLastInventoryDate: " + vault.getLastInventoryDate()
                        + "\nNumberOfArchives: " + vault.getNumberOfArchives() + "\nSizeInBytes: "
                        + vault.getSizeInBytes() + "\nVaultARN: " + vault.getVaultARN()
                        + "\nVaultName: " + vault.getVaultName());
            }
        } else {
            System.out.println("execute list");
        }
    }

    private void execDelete() throws IOException {
        if (!isDebug()) {
            VaultController controller = new VaultController(region, awsPropFile);
            controller.deleteVault(vaultName);
            System.out.println("Deleted vault: " + vaultName);
        } else {
            System.out.println("execute delete");
        }
    }

    @Override
    void onExecCommand() throws Exception {
        switch (cmdKind) {
            case Create:
                execCreate();
                break;
            case Describe:
                if (existVault(vaultName)) {
                    execDescribe();
                } else {
                    System.err.println(vaultName + " is not exist.");
                }
                break;
            case List:
                execList();
                break;
            case Delete:
                if (existVault(vaultName)) {
                    execDelete();
                } else {
                    System.err.println(vaultName + " is not exist.");
                }
                break;
            case Bad:
                printHelp();
                break;
            default:
                throw new IllegalStateException();
        }

        if (debug) {
            System.out.println("vaultName : " + vaultName);
        }

    }

    private void printCreateHelp() {
        System.out.println("Create a Vault named vaultname.");
        System.out.print("    ");
        System.out.println("java -jar vault_controller.jar create --vault vaultname");
        printHelpHelper("create", " --vault vaultname");
    }

    private void printDescribeHelp() {
        System.out.println("Get the describe of Vault named vaultname.");
        System.out.print("    ");
        System.out.println("java -jar vault_controller.jar desc --vault vaultname");
        printHelpHelper("desc", " --vault vaultname");
    }

    private void printHelpHelper(String kind, String opt) {
        System.out.println("Specify the region.");
        System.out.print("    ");
        System.out.println("java -jar vault_controller.jar " + kind + opt + " --region us-west-2");
        System.out.println("Specifies the AwsCredentials.properties file.");
        System.out.print("    ");
        System.out.println("java -jar vault_controller.jar " + kind + opt
                + " --properties myAwsPropFile.properties");
        System.out.println("Specify the region and AwsCredentials.properties.");
        System.out.print("    ");
        System.out.println("java -jar vault_controller.jar " + kind + opt
                + " --region us-west-2 --properties myAwsPropFile.properties");
    }

    private void printListHelp() {
        System.out.println("Gets a list Vault.");
        System.out.print("    ");
        System.out.println("java -jar vault_controller.jar list");
        printHelpHelper("list", "");
    }

    private void printDeleteHelp() {
        System.out.println("Delete Vault.");
        System.out.print("    ");
        System.out.println("java -jar vault_controller.jar delete --vault vaultname.");
        printHelpHelper("delete", " --vault vaultname");
    }

    private void printHelp() {
        System.out
                .println("java -jar vault_controller.jar cmd [--vault vaultname] [--region region] [--properties prop_filename]");
        System.out.println();
        System.out.println("cmd           : create | desc | list | delete | help");
        System.out.println("vaultname     : The name of the vault.");
        System.out
                .println("region        : us-east-1 | us-west-1 | us-west-2 | eu-west-1 | ap-northeast-1");
        System.out
                .println("prop_filename : If you want to specify explicitly AwsCredentials.properties");
        System.out.println();
        printCreateHelp();
        System.out.println();
        printDescribeHelp();
        System.out.println();
        printListHelp();
        System.out.println();
        printDeleteHelp();
        System.out.println();
        printRegion();
    }

    @Override
    void onExecInvalidParam() {
        switch (cmdKind) {
            case Create:
                printCreateHelp();
                break;
            case Describe:
                printDescribeHelp();
                break;
            case List:
                printListHelp();
                break;
            case Delete:
                printDeleteHelp();
                break;
            case Bad:
                printHelp();
                break;
            case Help:
                printHelp();
                break;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * java -jar VaultController.jar create vaultname<br>
     * with endpoint<br>
     * java -jar VaultController.jar create vaultname -endpoint eu_west_1<br>
     * java -jar VaultController.jar describe vaultname<br>
     * java -jar VaultController.jar list<br>
     * java -jar VaultController.jar delete vaultname
     * 
     * @throws Exception
     */

    public static void main(String[] args) throws Exception {
        if (!new VaultControllerCmd(args).exec()) {
            System.exit(-1);
        }
    }

}

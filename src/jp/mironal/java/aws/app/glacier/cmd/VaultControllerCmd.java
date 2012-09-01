
package jp.mironal.java.aws.app.glacier.cmd;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jp.mironal.java.aws.app.glacier.AwsTools.AwsService;
import jp.mironal.java.aws.app.glacier.AwsTools.Region;
import jp.mironal.java.aws.app.glacier.GlacierTools;
import jp.mironal.java.aws.app.glacier.VaultController;

import com.amazonaws.services.glacier.model.CreateVaultResult;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.DescribeVaultResult;

public class VaultControllerCmd {
    private VaultControllerCmd() {
    }

    static class Util {
        private Util() {
        }

        public static void printSpace4() {
            System.out.print("    ");
        }

        public static boolean checkUseVaultName(Kind cmdKind) {
            return (cmdKind == Kind.Create) || (cmdKind == Kind.Describe)
                    || (cmdKind == Kind.Delete);
        }

        public static void printEndpointHelp() {
            System.out.println("endpoints are");
            for (Region r : Region.values()) {
                printSpace4();
                System.out.println(r.toString() + " => "
                        + VaultController.makeUrl(AwsService.Glacier, r));
            }
        }

        public static void printPropertiesHelp() {
            System.out.println(VaultController.AWS_PROPERTIES_FILENAME + " is not found.");
            System.out.println("specify AwsCredentials.properties directory or filename");
            System.out.println("--properties path or filename");
            System.out
                    .println("see : http://docs.amazonwebservices.com/amazonglacier/latest/dev/using-aws-sdk-for-java.html");
        }

        public static void printVaultNameHelp() {
            System.out.println("Illegal vault name.");
            System.out
                    .println("see : http://docs.amazonwebservices.com/amazonglacier/latest/dev/api-vault-put.html");
        }

        public static void printUnKnownCommand() {
            System.out.println("unknown comannd...");
            printHelp();
        }

        public static void printHelp() {
            System.out.println("Command example.");
            System.out.println();
            printSpace4();
            System.out.println("java -jar VaultController.jar create vaultname");
            printSpace4();
            System.out.println("java -jar VaultController.jar describe vaultname");
            printSpace4();
            System.out.println("java -jar VaultController.jar list");
            printSpace4();
            System.out.println("java -jar VaultController.jar delete vaultname");
            printSpace4();
            System.out.println();
            printSpace4();
            System.out.println("with endpoint use --endpoint option.");
            printSpace4();
            System.out
                    .println("java -jar VaultController.jar create vaultname --endpoint eu_west_1");
            System.out.println();
            printSpace4();
            System.out.println("specify AwsCredentials.properties directory or filename");
            System.out.println("--properties path or filename");
            System.out.println();
            printEndpointHelp();
        }

        public static void checkHope(boolean check, boolean hope) {
            if (check == hope) {
                System.out.println("OK");
            } else {
                System.out.println("!!!!!!!!!!!! NG !!!!!!!!!!!!!!!!!");
            }
        }

        public static void testValidate() {
            StringBuffer toolong = new StringBuffer();
            for (int i = 0; i < 300; i++) {
                toolong.append("a");
            }

            Util.checkHope(VaultController.validateVaultName(""), false);
            Util.checkHope(VaultController.validateVaultName(toolong.toString()), false);
            Util.checkHope(VaultController.validateVaultName("aaaaaa000022334444^"), false);
            Util.checkHope(VaultController.validateVaultName("aaa"), true);
            Util.checkHope(VaultController.validateVaultName("aaa_sfdafd-dfadsf.fdfa"), true);
        }

    }

    enum Kind {
        Bad, Create, Describe, List, Delete
    }

    /**
     * java -jar VaultController.jar create vaultname<br>
     * with endpoint<br>
     * java -jar VaultController.jar create vaultname -endpoint eu_west_1<br>
     * java -jar VaultController.jar describe vaultname<br>
     * java -jar VaultController.jar list<br>
     * java -jar VaultController.jar delete vaultname
     * 
     * @throws IOException
     */

    public static void main(String[] args) throws IOException {

        String endpointStr = null; /**/
        Kind cmdKind = Kind.Bad;
        String vaultName = "";
        boolean debug = false;
        String propertiesName = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("create")) {
                if ((i + 1) < args.length) {
                    i++;
                    vaultName = args[i];

                    /* すでに別のオプションが割り当てられていたら不正な引数とみなす */
                    if (cmdKind == Kind.Bad) {
                        cmdKind = Kind.Create;
                    } else {
                        /* オプション違反 */
                        cmdKind = Kind.Bad;
                        break;
                    }

                }
            }

            if (arg.equals("describe")) {
                if ((i + 1) < args.length) {
                    i++;
                    vaultName = args[i];
                    if (cmdKind == Kind.Bad) {
                        cmdKind = Kind.Describe;
                    } else {
                        cmdKind = Kind.Bad;
                        break;
                    }
                }
            }

            if (arg.equals("list")) {
                if (cmdKind == Kind.Bad) {
                    cmdKind = Kind.List;
                } else {
                    cmdKind = Kind.Bad;
                    break;
                }
            }

            if (arg.equals("delete")) {
                if ((i + 1) < args.length) {
                    i++;
                    vaultName = args[i];
                    if (cmdKind == Kind.Bad) {
                        cmdKind = Kind.Delete;
                    } else {
                        cmdKind = Kind.Bad;
                        break;
                    }
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

            if (arg.equals("-h") || arg.equals("--help")) {
                Util.printHelp();
                System.exit(0);
            }
            if (arg.equals("--debug")) {
                debug = true;
            }
        }

        Region region = null;
        /* endpoint */
        if (endpointStr == null) {
            region = VaultController.getDefaultEndpoint();
        } else {
            if (VaultController.containEndpoint(endpointStr)) {
                region = VaultController.convertToRegion(endpointStr);
            } else {
                /* endpointの指定が正しくない場合はhelpを表示して終了. */
                Util.printEndpointHelp();
                System.exit(-1);
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

        /*
         * vaultNameが
         * @see <a href=
         * "http://docs.amazonwebservices.com/amazonglacier/latest/dev/api-vault-put.html"
         * > <br> に違反してたらhelpを表示して終了.
         */
        if (Util.checkUseVaultName(cmdKind) && !VaultController.validateVaultName(vaultName)) {
            Util.printVaultNameHelp();
            System.exit(-1);
        }

        VaultController controller = new VaultController(region, propFile);

        switch (cmdKind) {
            case Create:
                CreateVaultResult createVaultResult = controller.createVault(vaultName);
                System.out.println("Success!");
                System.out.println("Location:" + createVaultResult.getLocation());
                break;
            case Describe:
                DescribeVaultResult describeVaultResult = controller.describeVault(vaultName);
                System.out.println("Describing the vault: " + vaultName);
                System.out.print("CreationDate: " + describeVaultResult.getCreationDate()
                        + "\nLastInventoryDate: " + describeVaultResult.getLastInventoryDate()
                        + "\nNumberOfArchives: " + describeVaultResult.getNumberOfArchives()
                        + "\nSizeInBytes: " + describeVaultResult.getSizeInBytes() + "\nVaultARN: "
                        + describeVaultResult.getVaultARN() + "\nVaultName: "
                        + describeVaultResult.getVaultName());
                System.out.println();
                break;
            case List:
                List<DescribeVaultOutput> vaultList = controller.listVaults();
                System.out.println("\nDescribing all vaults (vault list):");
                for (DescribeVaultOutput vault : vaultList) {
                    System.out.println("\nCreationDate: " + vault.getCreationDate()
                            + "\nLastInventoryDate: " + vault.getLastInventoryDate()
                            + "\nNumberOfArchives: " + vault.getNumberOfArchives()
                            + "\nSizeInBytes: " + vault.getSizeInBytes() + "\nVaultARN: "
                            + vault.getVaultARN() + "\nVaultName: " + vault.getVaultName());
                }
                break;
            case Delete:
                controller.deleteVault(vaultName);
                System.out.println("Deleted vault: " + vaultName);
                break;
            case Bad:
                Util.printHelp();
                break;
            default:
                Util.printUnKnownCommand();
                break;
        }

        if (debug) {
            System.out.println("endpoint : " + endpointStr);
            System.out.println("vaultName : " + vaultName);
        }
    }

}

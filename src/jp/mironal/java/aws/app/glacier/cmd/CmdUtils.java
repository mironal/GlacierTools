
package jp.mironal.java.aws.app.glacier.cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import jp.mironal.java.aws.app.glacier.AwsTools;
import jp.mironal.java.aws.app.glacier.AwsTools.AwsService;
import jp.mironal.java.aws.app.glacier.AwsTools.Region;
import jp.mironal.java.aws.app.glacier.GlacierTools;
import jp.mironal.java.aws.app.glacier.VaultController;

import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.GlacierJobDescription;

public abstract class CmdUtils {

    boolean debug = false;
    File awsPropFile = null;
    Region region = null;

    /**
     * AwsCredentials.propertiesの存在確認を行った後、そのファイルを保持する.<br>
     * 存在しない場合は
     * {@link #onAwsCredentialsPropertiesFileNotFound(String, Throwable)}が呼ばれる.
     * 
     * @param filename AwsCredentials.propertiesへのパス
     */
    void setAwsCredentialsPropertiesFile(String filename) {
        try {
            awsPropFile = setAwsCredentialsPropertiesFileOrThrow(filename);
        } catch (FileNotFoundException e) {
            onAwsCredentialsPropertiesFileNotFound(filename, e);
        }
    }

    void onAwsCredentialsPropertiesFileNotFound(String filename, Throwable e) {
        if (filename == null) {
            System.err.println(AwsTools.AWS_PROPERTIES_FILENAME + " is not found.");
        } else {
            System.err.println(filename + " is not found.");
        }
        if (isDebug()) {
            e.printStackTrace();
        }
    }

    public CmdUtils(String[] args) {

        String endpointStr = null; /**/
        String propertiesName = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("--debug")) {
                debug = true;
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
        }
        setAwsCredentialsPropertiesFile(propertiesName);

        setRegion(endpointStr);
    }

    /**
     * @param propertiesName
     * @return
     * @throws FileNotFoundException
     */
    private File setAwsCredentialsPropertiesFileOrThrow(String propertiesName)
            throws FileNotFoundException {
        if (propertiesName == null) {
            propertiesName = GlacierTools.AWS_PROPERTIES_FILENAME;
        }

        File propFile = new File(propertiesName);
        if (propFile.isDirectory()) {
            propFile = new File(propertiesName + File.separator
                    + GlacierTools.AWS_PROPERTIES_FILENAME);
        }
        if (!propFile.exists()) {
            throw new FileNotFoundException(propFile.getAbsolutePath() + "is not found.");
        }

        return propFile;
    }

    void setRegion(String endpointStr) {
        try {
            region = setRegionOrThrow(endpointStr);
        } catch (InvalidRegionException e) {
            onRegionNotFound(endpointStr, e);
        }
    }

    void onRegionNotFound(String endpointStr, Throwable e) {
        System.err.println(e.getMessage() + " is not found.");
        if (isDebug()) {
            e.printStackTrace();
        }
    }

    /**
     * @param endpointStr
     * @return
     * @throws InvalidRegionException
     */
    private Region setRegionOrThrow(String endpointStr) throws InvalidRegionException {
        Region region = null;
        if (endpointStr == null) {
            region = GlacierTools.getDefaultEndpoint();
        } else {
            if (GlacierTools.containEndpoint(endpointStr)) {
                region = GlacierTools.convertToRegion(endpointStr);
            } else {
                throw new InvalidRegionException(endpointStr);
            }
        }
        return region;
    }

    void debugPrint(String msg) {
        if (debug) {
            System.err.println(msg);
        }
    }

    boolean exec() throws Exception {
        if (validateParam()) {
            onExecCommand();
            return true;
        }
        onExecInvalidParam();
        return false;

    }

    void printRegion() {
        System.out.println("Region");
        for (Region r : GlacierTools.getGlacierRegions().values()) {
            System.out.print("    ");
            System.out.println(r.getEndpoint() + " => "
                    + VaultController.makeUrl(AwsService.Glacier, r));
        }

    }

    boolean existVault(String vaultName) throws IOException {
        VaultController controller = new VaultController(region, awsPropFile);
        List<DescribeVaultOutput> describeVaultOutputs = controller.listVaults();
        for (DescribeVaultOutput output : describeVaultOutputs) {
            if (output.getVaultName().equals(vaultName)) {
                return true;
            }
        }
        return false;
    }

    protected void printDescribeJob(DescribeJobResult result) {
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

    protected void printGlacierJobDescriptionf(GlacierJobDescription description) {
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

    abstract void onExecCommand() throws Exception;

    abstract void onExecInvalidParam();

    abstract boolean validateParam();

    /**
     * デバッグモードか否かを取得する.
     * 
     * @return true:デバッグモード
     */
    public boolean isDebug() {
        return debug;
    }
}

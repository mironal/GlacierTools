
package jp.mironal.java.aws.app.glacier.cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.GlacierJobDescription;

import jp.mironal.java.aws.app.glacier.AwsTools.AwsService;
import jp.mironal.java.aws.app.glacier.AwsTools.Region;
import jp.mironal.java.aws.app.glacier.GlacierTools;
import jp.mironal.java.aws.app.glacier.VaultController;

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

    abstract void onAwsCredentialsPropertiesFileNotFound(String filename, Throwable e);

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

    abstract void onRegionNotFound(String endpointStr, Throwable e);

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
}

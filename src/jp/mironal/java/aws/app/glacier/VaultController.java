package jp.mironal.java.aws.app.glacier;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.glacier.model.CreateVaultRequest;
import com.amazonaws.services.glacier.model.CreateVaultResult;
import com.amazonaws.services.glacier.model.DeleteVaultRequest;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.DescribeVaultRequest;
import com.amazonaws.services.glacier.model.DescribeVaultResult;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;

/**
 * コメントいらないっしょｗｗｗ
 * 
 * @author yama
 * 
 */
public class VaultController extends GlacierTools {

    public static boolean validateVaultName(String vaultName) {
        if ((vaultName.length() >= 1) && (vaultName.length() <= 255)) {
            String regex = "[^a-zA-Z0-9_\\-\\.]";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(vaultName);
            return !m.find();
        }
        return false;
    }

    public VaultController() throws IOException {
        super();
    }

    /**
     * 
     * @param endpoint
     *            endpointの省略形　{@link GlacierTools#endpointSet()}
     * @throws IOException
     */
    public VaultController(Region endpoint, File awsProperties) throws IOException {
        super(endpoint, awsProperties);
    }

    /**
     * 
     * @param vaultName
     * @return
     */
    public CreateVaultResult createVault(String vaultName) {
        CreateVaultRequest createVaultRequest = new CreateVaultRequest().withVaultName(vaultName);
        CreateVaultResult createVaultResult = client.createVault(createVaultRequest);
        return createVaultResult;
    }

    /**
     * 
     * @param vaultName
     * @return
     */
    public DescribeVaultResult describeVault(String vaultName) {
        DescribeVaultRequest describeVaultRequest = new DescribeVaultRequest()
                .withVaultName(vaultName);
        DescribeVaultResult describeVaultResult = client.describeVault(describeVaultRequest);
        return describeVaultResult;
    }

    /**
     * 
     */
    public List<DescribeVaultOutput> listVaults() {
        ListVaultsRequest listVaultsRequest = new ListVaultsRequest();
        ListVaultsResult listVaultsResult = client.listVaults(listVaultsRequest);
        return listVaultsResult.getVaultList();
    }

    /**
     * 
     * @param vaultName
     */
    public void deleteVault(String vaultName) {
        DeleteVaultRequest deleteVaultRequest = new DeleteVaultRequest().withVaultName(vaultName);
        client.deleteVault(deleteVaultRequest);
    }

}
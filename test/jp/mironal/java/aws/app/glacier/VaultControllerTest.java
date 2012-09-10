
package jp.mironal.java.aws.app.glacier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import jp.mironal.java.aws.app.glacier.AwsTools.Region;

import org.junit.Test;

import com.amazonaws.services.glacier.model.CreateVaultResult;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.DescribeVaultResult;

public class VaultControllerTest {

    private static final String VAULT_NAME = "test_vault";

    @Test
    public void test_CreateVault() throws IOException {

        VaultController controller = new VaultController();
        assertNotNull(controller);
        assertNotNull(controller.client);
        assertNotNull(controller.credentials);
        assertNotNull(controller.region);

        assertEquals(Region.US_EAST_1, controller.region);

        CreateVaultResult result = controller.createVault(VAULT_NAME);
        assertNotNull(result);

        assertNotNull(result.getLocation());
        String location = result.getLocation();
        assertTrue(location.endsWith(VAULT_NAME));

        DescribeVaultResult describeVaultResult = controller.describeVault(VAULT_NAME);

        assertNotNull(describeVaultResult);
        assertEquals(VAULT_NAME, describeVaultResult.getVaultName());

        List<DescribeVaultOutput> describeVaultOutputs = controller.listVaults();
        assertNotNull(describeVaultOutputs);

        controller.deleteVault(VAULT_NAME);

    }
}

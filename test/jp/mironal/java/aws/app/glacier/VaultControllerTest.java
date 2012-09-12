
package jp.mironal.java.aws.app.glacier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

    private static final String VAULT_NAME = "vault_controller_test";

    @Test
    public void test_Constract() throws IOException {
        VaultController controller = new VaultController();
        assertNotNull(controller);
        assertNotNull(controller.client);
        assertNotNull(controller.credentials);
        assertNotNull(controller.region);

        assertEquals(Region.US_EAST_1, controller.region);
    }

    @Test
    public void test_Validate() {
        // OK
        assertTrue(VaultController.validateVaultName("asb"));
        assertTrue(VaultController.validateVaultName("asd-dd"));
        assertTrue(VaultController.validateVaultName("asd-aa12"));
        assertTrue(VaultController.validateVaultName("1234"));
        assertTrue(VaultController.validateVaultName("1234-23..s___aa"));
        assertTrue(VaultController.validateVaultName("____....."));
        assertTrue(VaultController.validateVaultName("ADSFGa"));

        // NG
        assertFalse(VaultController.validateVaultName("asd ggg"));
        assertFalse(VaultController.validateVaultName("dfa^^"));
        assertFalse(VaultController.validateVaultName("????"));
        assertFalse(VaultController.validateVaultName("   "));
        assertFalse(VaultController.validateVaultName("fdasf__335^^--\\//.//"));
    }

    @Test
    public void test_CreateVault() throws IOException {
        VaultController controller = new VaultController();

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

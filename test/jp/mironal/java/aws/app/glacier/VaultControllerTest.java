package jp.mironal.java.aws.app.glacier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import jp.mironal.java.aws.app.glacier.AwsTools.Region;

import org.junit.Test;

public class VaultControllerTest {

	@Test
	public void test_Constract() throws IOException {
		VaultController controller = new VaultController();
		assertNotNull(controller);
		assertNotNull(controller.GlacierClient);
		assertNotNull(controller.SQSClient);
		assertNotNull(controller.SNSClient);
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

}

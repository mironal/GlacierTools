package jp.mironal.java.aws.app.glacier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import jp.mironal.java.aws.app.glacier.AwsTools.Region;

import org.junit.Test;

public class ArchiveControllerTest {

	@Test
	public void testConstract() throws IOException {
		ArchiveController controller = new ArchiveController();
		assertNotNull(controller);
		assertNotNull(controller.client);
		assertNotNull(controller.credentials);
		assertNotNull(controller.region);

		assertEquals(Region.US_EAST_1, controller.region);
	}
}
package jp.mironal.java.aws.app.glacier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import jp.mironal.java.aws.app.glacier.AwsTools.Region;

import org.junit.Test;

public class JobOperatorTest {

	@Test
	public void test_CreateInstance() throws IOException {
		JobOperator jobOperator = new JobOperator();
		assertNotNull(jobOperator);
		assertNotNull(jobOperator.GlacierClient);
		assertNotNull(jobOperator.SQSClient);
		assertNotNull(jobOperator.SNSClient);
		assertNotNull(jobOperator.credentials);
		assertNotNull(jobOperator.region);
		assertEquals(Region.US_EAST_1, jobOperator.region);

	}
}

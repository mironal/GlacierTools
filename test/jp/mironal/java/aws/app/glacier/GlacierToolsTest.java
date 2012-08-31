package jp.mironal.java.aws.app.glacier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jp.mironal.java.aws.app.glacier.AwsTools.Region;

import org.junit.Test;

public class GlacierToolsTest {

    @Test
    public void test_containEndpoint() {
        assertTrue(GlacierTools.containEndpoint("us-east-1"));
        assertTrue(GlacierTools.containEndpoint("us-west-1"));
        assertTrue(GlacierTools.containEndpoint("us-west-2"));
        assertTrue(GlacierTools.containEndpoint("eu-west-1"));
        assertTrue(GlacierTools.containEndpoint("ap-northeast-1"));

        assertFalse(GlacierTools.containEndpoint("aa"));
        assertFalse(GlacierTools.containEndpoint("sa-east-1"));
        assertFalse(GlacierTools.containEndpoint("ap-southeast-1"));
    }

    @Test
    public void test_getDefaultEndpoint() {
        assertEquals(GlacierTools.getDefaultEndpoint(), Region.US_EAST_1);
    }

    @Test
    public void test_convertToRegion() {
        assertEquals(GlacierTools.convertToRegion("us-east-1"), Region.US_EAST_1);
        assertEquals(GlacierTools.convertToRegion("us-west-1"), Region.US_WEST_1);
        assertEquals(GlacierTools.convertToRegion("us-west-2"), Region.US_WEST_2);
        assertEquals(GlacierTools.convertToRegion("eu-west-1"), Region.EU_WEST_1);
        assertEquals(GlacierTools.convertToRegion("ap-northeast-1"), Region.AP_NORTHEAST_1);
    }

}

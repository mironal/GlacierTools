
package jp.mironal.java.aws.app.glacier;

import static org.junit.Assert.*;
import jp.mironal.java.aws.app.glacier.AwsTools.AwsService;
import jp.mironal.java.aws.app.glacier.AwsTools.Region;

import org.junit.Test;

public class AwsToolsTest {

    @Test
    public void testMakeUrl_Glacier_US_EAST_1() {
        assertEquals("https://glacier.us-east-1.amazonaws.com",
                AwsTools.makeUrl(AwsService.Glacier, Region.US_EAST_1));
    }

    @Test
    public void testMakeUrl_Glacier_EU_WEST_1() {
        assertEquals("https://glacier.eu-west-1.amazonaws.com",
                AwsTools.makeUrl(AwsService.Glacier, Region.EU_WEST_1));
    }

    @Test
    public void testMakeUrl_Sqs_US_WEST_1() {
        assertEquals("https://sqs.us-west-1.amazonaws.com",
                AwsTools.makeUrl(AwsService.Sqs, Region.US_WEST_1));

    }

    @Test
    public void testMakeUrl_Sqs_US_WEST_2() {
        assertEquals("https://sqs.us-west-2.amazonaws.com",
                AwsTools.makeUrl(AwsService.Sqs, Region.US_WEST_2));
    }

    @Test
    public void testMakeUrl_Sns_AP_NORTHEAST_1() {
        assertEquals("https://sns.ap-northeast-1.amazonaws.com",
                AwsTools.makeUrl(AwsService.Sns, Region.AP_NORTHEAST_1));
    }

    @Test
    public void testPropFileName() {
        assertEquals("AwsCredentials.properties", AwsTools.AWS_PROPERTIES_FILENAME);
    }

}

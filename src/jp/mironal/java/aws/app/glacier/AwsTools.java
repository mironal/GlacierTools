
package jp.mironal.java.aws.app.glacier;

public class AwsTools {

    enum AwsService {
        Glacier, Sqs, Sns,
    }

    protected static final String HTTPS = "https://";

    protected static final String URL_TAIL = ".amazonaws.com";

    enum Region {
        /**
         * US East (Northern Virginia) Region
         */
        US_EAST_1("us-east-1"),

        /**
         * US West (Northern California) Region
         */
        US_WEST_1("us-west-1"),
        /**
         * US West (Oregon) Region
         */
        US_WEST_2("us-west-2"),
        /**
         * EU (Ireland) Region
         */
        EU_WEST_1("eu-west-1"),
        /**
         * Asia Pacific (Tokyo) Region
         */
        AP_NORTHEAST_1("ap-northeast-1"), ;
        private final String endpoint;

        private Region(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getEndpoint() {
            return this.endpoint;
        }
    }

    protected static String makeUrl(AwsService service, Region region) {
        if (service == null) {
            throw new NullPointerException("service is null.");
        }
        if (region == null) {
            throw new NullPointerException("region is null.");
        }
        return HTTPS + service.toString().toLowerCase() + "." + region.getEndpoint() + URL_TAIL;
    }

    public static final String AWS_PROPERTIES_FILENAME = "AwsCredentials.properties";

}

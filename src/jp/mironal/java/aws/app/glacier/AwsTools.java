
package jp.mironal.java.aws.app.glacier;

/**
 * @author mironal
 */
public class AwsTools {

    /**
     * AWSの種類を定義する列挙体.<br>
     * サービス接続に使うURLの作成に使用する.
     */
    // @formatter:off
    public enum AwsService {
        Glacier,
        Sqs,
        Sns,
    }
    // @formatter:on

    protected static final String HTTPS = "https://";

    protected static final String URL_TAIL = ".amazonaws.com";

    /**
     * AWSのリージョンを定義する列挙体.<br>
     * サービス接続に使うURLの作成に使用する.
     */
    // @formatter:off
    public enum Region {
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
        AP_NORTHEAST_1("ap-northeast-1"),
        
        ;
        
        //リージョンを識別するURLの部分.
        private final String endpoint;

        private Region(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getEndpoint() {
            return this.endpoint;
        }
    }
    // @formatter:on

    public static final String AWS_PROPERTIES_FILENAME = "AwsCredentials.properties";

    /**
     * 指定されたサービスとリージョンから接続用URLを生成する.
     * 
     * @param service AWSの種類
     * @param region リージョン
     * @return URLを示す文字列.
     */
    public static String makeUrl(AwsService service, Region region) {
        if (service == null) {
            throw new NullPointerException("service is null.");
        }
        if (region == null) {
            throw new NullPointerException("region is null.");
        }
        return HTTPS + service.toString().toLowerCase() + "." + region.getEndpoint() + URL_TAIL;
    }
}

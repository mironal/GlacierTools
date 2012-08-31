
package jp.mironal.java.aws.app.glacier;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;

public class GlacierTools extends AwsTools {

    /**
     * 文字列をRegionに変換するためのテーブル<br>
     * コマンドラインオプションの変換等に使う
     */
    private static final HashMap<String, Region> ENDPOINTS;
    static {
        ENDPOINTS = new HashMap<String, AwsTools.Region>();
        // リージョンは色々あるが、Glacierが対応しているリージョンのみを含める
        // 2012/08/29現在
        ENDPOINTS.put(Region.US_EAST_1.getEndpoint(), Region.US_EAST_1);
        ENDPOINTS.put(Region.US_WEST_1.getEndpoint(), Region.US_WEST_1);
        ENDPOINTS.put(Region.US_WEST_2.getEndpoint(), Region.US_WEST_2);
        ENDPOINTS.put(Region.EU_WEST_1.getEndpoint(), Region.EU_WEST_1);
        ENDPOINTS.put(Region.AP_NORTHEAST_1.getEndpoint(), Region.AP_NORTHEAST_1);
    }

    /**
     * regionを示す文字列に対応する列挙体があるか調べる.
     * 
     * @param key regionを示す文字列.
     * @return true:keyに対応するRegionが存在する.
     */
    public static boolean containEndpoint(String key) {
        return ENDPOINTS.containsKey(key);
    }

    /**
     * US EAST 1のRegionを返す
     * 
     * @return
     */
    public static Region getDefaultEndpoint() {
        return Region.US_EAST_1;
    }

    /**
     * regionの文字列表現をRegion列挙体に変換する.
     * 
     * @param key regionを示す文字列.
     * @return Region列挙体
     */
    public static Region convertToRegion(String key) {
        return ENDPOINTS.get(key);
    }

    protected AmazonGlacierClient client;
    protected AWSCredentials credentials;

    public GlacierTools() throws IOException {
        this(getDefaultEndpoint(), new File(AWS_PROPERTIES_FILENAME));
    }

    public GlacierTools(Region endpoint, File awsProperties) throws IOException {
        credentials = new PropertiesCredentials(awsProperties);
        client = new AmazonGlacierClient(credentials);
        client.setEndpoint(makeUrl(AwsService.Glacier, endpoint));
    }

}

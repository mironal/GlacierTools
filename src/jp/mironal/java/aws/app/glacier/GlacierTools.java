
package jp.mironal.java.aws.app.glacier;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;

/**
 * @author mironal
 */
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
        // http://docs.amazonwebservices.com/general/latest/gr/rande.html#glacier_region
        ENDPOINTS.put(Region.US_EAST_1.getEndpoint(), Region.US_EAST_1);
        ENDPOINTS.put(Region.US_WEST_1.getEndpoint(), Region.US_WEST_1);
        ENDPOINTS.put(Region.US_WEST_2.getEndpoint(), Region.US_WEST_2);
        ENDPOINTS.put(Region.EU_WEST_1.getEndpoint(), Region.EU_WEST_1);
        ENDPOINTS.put(Region.AP_NORTHEAST_1.getEndpoint(), Region.AP_NORTHEAST_1);
    }

    /**
     * Glacierが使用できるRegionの一覧を取得する.<br>
     * コマンドライン引数からRegion列挙体に変換する為に使用しやすい形になっている.
     * 
     * @return Regionを示す文字列とRegion列挙体のMap
     */
    public static Map<String, Region> getGlacierRegions() {
        return Collections.unmodifiableMap(ENDPOINTS);
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
     * @return Region.US_EAST_1
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

    protected final AmazonGlacierClient client;
    protected final AWSCredentials credentials;
    protected final Region region;

    private boolean isDebug = false;

    /**
     * デフォルトコンストラクタ<br>
     * デフォルトのリージョンとカレントディレクトリにあるAwsCredentials.propertiesを使用してインスタンスを作成
     * 
     * @throws IOException
     */
    public GlacierTools() throws IOException {
        this(getDefaultEndpoint(), new File(AWS_PROPERTIES_FILENAME));
    }

    /**
     * 指定したリージョンとAwsCredentials.propertiesファイルでインスタンスを作成
     * 
     * @param endpoint リージョン
     * @param awsProperties AwsCredentials.propertiesファイルのインスタンス
     * @throws IOException
     */
    public GlacierTools(Region endpoint, File awsProperties) throws IOException {
        this.region = endpoint;
        this.credentials = new PropertiesCredentials(awsProperties);
        this.client = new AmazonGlacierClient(credentials);
        this.client.setEndpoint(makeUrl(AwsService.Glacier, endpoint));
    }

    /**
     * デバッグモードを設定する.<br>
     * デフォルトでは無効になっている
     * 
     * @param debug true:デバッグモードにする.
     */
    protected void setDebugMode(boolean debug) {
        isDebug = debug;
    }

    /**
     * デバッグモードかどうか.
     * 
     * @return true:デバッグモード
     */
    protected boolean isDebug() {
        return isDebug;
    }

    /**
     * デバッグモード時のみmsgを出力する.
     * 
     * @param msg 出力したいメッセージ.
     */
    protected void debugPrint(String msg) {
        if (isDebug()) {
            System.out.println(msg);
        }
    }

    /**
     * デバッグモード時のみmsgを出力する.<br>
     * 先頭にタイムスタンプが付く.
     * 
     * @param msg 出力したいメッセージ.
     */
    protected void debugPrintWithTime(String msg) {
        debugPrint(new Date().toString() + " " + msg);
    }

}

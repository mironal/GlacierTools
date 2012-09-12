
package jp.mironal.java.aws.app.glacier;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.glacier.model.CreateVaultRequest;
import com.amazonaws.services.glacier.model.CreateVaultResult;
import com.amazonaws.services.glacier.model.DeleteVaultRequest;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.DescribeVaultRequest;
import com.amazonaws.services.glacier.model.DescribeVaultResult;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;

/**
 * @author mironal
 */
public class VaultController extends GlacierTools {

    /**
     * Vaultの名前に問題が無いかバリデーションを行う<br>
     * 
     * @see <a href=
     *      "http://docs.amazonwebservices.com/amazonglacier/latest/dev/creating-vaults.html"
     *      >Creating a Vault in Amazon Glacier<a/>
     * @param vaultName バリデーションを行う文字列
     * @return true:問題無し、false:問題有り
     */
    public static boolean validateVaultName(String vaultName) {
        if ((vaultName.length() >= 1) && (vaultName.length() <= 255)) {
            String regex = "[^a-zA-Z0-9_\\-\\.]";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(vaultName);
            return !m.find();
        }
        return false;
    }

    /**
     * デフォルトコンストラクタ<br>
     * デフォルトのリージョンとカレントディレクトリにあるAwsCredentials.propertiesを使用してインスタンスを作成
     * 
     * @throws IOException
     */
    public VaultController() throws IOException {
        super();
    }

    /**
     * 指定したリージョンとAwsCredentials.propertiesファイルでインスタンスを作成
     * 
     * @param endpoint リージョン
     * @param awsProperties AwsCredentials.propertiesファイルのインスタンス
     * @throws IOException
     */
    public VaultController(Region endpoint, File awsProperties) throws IOException {
        super(endpoint, awsProperties);
    }

    /**
     * Vaultを作成する.
     * 
     * @param vaultName 作成するVaultの名前
     * @return 結果
     */
    public CreateVaultResult createVault(String vaultName) {
        CreateVaultRequest createVaultRequest = new CreateVaultRequest().withVaultName(vaultName);
        CreateVaultResult createVaultResult = client.createVault(createVaultRequest);
        return createVaultResult;
    }

    /**
     * Vaultの詳細を取得する.
     * 
     * @param vaultName 詳細を取得したいVaultの名前
     * @return Vaultの詳細
     */
    public DescribeVaultResult describeVault(String vaultName) {
        DescribeVaultRequest describeVaultRequest = new DescribeVaultRequest()
                .withVaultName(vaultName);
        DescribeVaultResult describeVaultResult = client.describeVault(describeVaultRequest);
        return describeVaultResult;
    }

    /**
     * リージョンに存在しているVaultの一覧を取得する.
     * 
     * @return リージョンの一覧
     */
    public List<DescribeVaultOutput> listVaults() {
        ListVaultsRequest listVaultsRequest = new ListVaultsRequest();
        ListVaultsResult listVaultsResult = client.listVaults(listVaultsRequest);
        return listVaultsResult.getVaultList();
    }

    /**
     * Vaultを削除する.
     * 
     * @param vaultName 削除したいVaultの名前
     */
    public void deleteVault(String vaultName) {
        DeleteVaultRequest deleteVaultRequest = new DeleteVaultRequest().withVaultName(vaultName);
        client.deleteVault(deleteVaultRequest);
    }

}

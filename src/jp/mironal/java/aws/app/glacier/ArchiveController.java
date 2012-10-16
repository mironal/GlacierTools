
package jp.mironal.java.aws.app.glacier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.amazonaws.services.glacier.model.DeleteArchiveRequest;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;

/**
 * @author mironal
 */
public class ArchiveController extends GlacierTools {

    /**
     * デフォルトコンストラクタ<br>
     * デフォルトのリージョンとカレントディレクトリにあるAwsCredentials.propertiesを使用してインスタンスを作成
     * 
     * @throws IOException
     */
    public ArchiveController() throws IOException {
        super();
    }

    /**
     * 指定したリージョンとAwsCredentials.propertiesファイルでインスタンスを作成
     * 
     * @param endpoint リージョン
     * @param awsProperties AwsCredentials.propertiesファイルのインスタンス
     * @throws IOException
     */
    public ArchiveController(Region endpoint, File awsProperties) throws IOException {
        super(endpoint, awsProperties);
    }

    /**
     * アーカイブのアップロード
     * 
     * @param vaultName The name of the vault to upload to.
     * @param description The description of the new archive being uploaded.
     * @param file The file to upload to Amazon Glacier.
     * @return The result of the upload, including the archive ID needed to
     *         access the upload later.
     * @throws FileNotFoundException
     */
    public UploadResult upload(String vaultName, String description, File file)
            throws FileNotFoundException {
        ArchiveTransferManager atm = new ArchiveTransferManager(GlacierClient, SQSClient, SNSClient);

        UploadResult uploadResult = atm.upload(vaultName, description, file);
        return uploadResult;

    }

    /**
     * アーカイブのダウンロード
     * 
     * @param vaultName The name of the vault to download the archive from.
     * @param archiveId The unique ID of the archive to download.
     * @param file The file save the archive to.
     */
    public void download(String vaultName, String archiveId, File file) {
        ArchiveTransferManager atm = new ArchiveTransferManager(GlacierClient, SQSClient, SNSClient);
        atm.download(vaultName, archiveId, file);
    }

    /**
     * アーカイブの削除
     * 
     * @param vaultName The name of the vault to delete the archive from.
     * @param archiveId The unique ID of the archive to delete.
     */
    public void delete(String vaultName, String archiveId) {
        DeleteArchiveRequest dar = new DeleteArchiveRequest().withVaultName(vaultName)
                .withArchiveId(archiveId);
        GlacierClient.deleteArchive(dar);

    }

}

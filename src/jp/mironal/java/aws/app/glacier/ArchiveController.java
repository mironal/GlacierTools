
package jp.mironal.java.aws.app.glacier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.amazonaws.services.glacier.model.DeleteArchiveRequest;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;

public class ArchiveController extends GlacierTools {

    public ArchiveController() throws IOException {
        super();
    }

    public ArchiveController(Region endpoint, File awsProperties) throws IOException {
        super(endpoint, awsProperties);
    }

    /**
     * アーカイブのアップロード
     * 
     * @param vaultName
     * @param description
     * @param file アップロードするファイル.
     * @return
     * @throws FileNotFoundException
     */
    public UploadResult upload(String vaultName, String description, File file)
            throws FileNotFoundException {
        ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials);

        UploadResult uploadResult = atm.upload(vaultName, description, file);
        return uploadResult;

    }

    /**
     * アーカイブのダウンロード
     * 
     * @param vaultName
     * @param archiveId
     * @param file ダウンロードしたファイルの保存先.
     */
    public void download(String vaultName, String archiveId, File file) {
        ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials);
        atm.download(vaultName, archiveId, file);
    }

    /**
     * アーカイブの削除
     */
    public void delete(String vaultName, String archiveId) {
        DeleteArchiveRequest dar = new DeleteArchiveRequest().withVaultName(vaultName)
                .withArchiveId(archiveId);
        client.deleteArchive(dar);

    }

}

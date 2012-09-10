
package jp.mironal.java.aws.app.glacier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import jp.mironal.java.aws.app.glacier.AwsTools.Region;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.glacier.transfer.UploadResult;

public class ArchiveControllerTest {

    private static final String VAULT_NAME = "test_vault";

    private static final String FILENAME = "hogehoge.txt";
    private static final String data = "aaaaaaaabsdfasあああ";

    private static final String DOWN_FILENAME = "down.txt";

    @BeforeClass
    public static void setupRestoreFile() throws IOException {

        VaultController controller = new VaultController();
        controller.createVault(VAULT_NAME);

        File file = new File(FILENAME);
        PrintWriter pw = new PrintWriter(file);
        pw.append(data);
        pw.close();
    }

    @AfterClass
    public static void deleteRestoreFile() throws IOException {

        VaultController controller = new VaultController();
        controller.deleteVault(VAULT_NAME);

        File file = new File(FILENAME);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IllegalStateException("can not delete.");
            }
        }
    }

    @Test
    public void test() throws IOException {
        ArchiveController controller = new ArchiveController();
        assertNotNull(controller);
        assertNotNull(controller.client);
        assertNotNull(controller.credentials);
        assertNotNull(controller.region);

        assertEquals(Region.US_EAST_1, controller.region);

        UploadResult result = controller.upload(VAULT_NAME, "desc", new File(FILENAME));
        assertNotNull(result);

        String archiveId = result.getArchiveId();
        assertNotNull(archiveId);

        File downfile = new File(DOWN_FILENAME);
        assertFalse(downfile.exists());

        // 4時間待
        controller.download(VAULT_NAME, archiveId, downfile);

        assertNotNull(downfile);
        assertTrue(checkSameFile(downfile));

        controller.delete(VAULT_NAME, archiveId);

    }

    private boolean checkSameFile(File downfile) throws IOException {
        BufferedReader upfileReader = null;
        BufferedReader downfileReader = null;
        try {
            upfileReader = new BufferedReader(new FileReader(FILENAME));
            downfileReader = new BufferedReader(new FileReader(downfile));
            String upfileLine = null;
            String downfileLine = null;
            while (((upfileLine = upfileReader.readLine()) != null)
                    && (downfileLine = downfileReader.readLine()) != null) {
                if (!upfileLine.equals(downfileLine)) {
                    return false;
                }
            }
            return ((upfileLine == null) && (downfileLine == null));
        } finally {
            if (upfileReader != null) {
                try {
                    upfileReader.close();
                } catch (IOException ignore) {
                }
            }
            if (downfileReader != null) {
                try {
                    downfileReader.close();
                } catch (IOException ignore) {

                }
            }
        }

    }
}

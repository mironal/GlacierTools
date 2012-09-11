
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
import java.util.Date;

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
    public static void setupR() throws IOException {

        VaultController controller = new VaultController();
        controller.createVault(VAULT_NAME);

        File file = new File(FILENAME);
        PrintWriter pw = new PrintWriter(file);
        pw.append(data);
        pw.close();
    }

    @AfterClass
    public static void delete() throws IOException {

        VaultController controller = new VaultController();
        controller.deleteVault(VAULT_NAME);

        File file = new File(FILENAME);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IllegalStateException("can not delete. : " + FILENAME);
            }
        }
        file = new File(DOWN_FILENAME);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IllegalStateException("can not delete. : " + DOWN_FILENAME);
            }
        }
    }

    @Test
    public void testConstract() throws IOException {
        ArchiveController controller = new ArchiveController();
        assertNotNull(controller);
        assertNotNull(controller.client);
        assertNotNull(controller.credentials);
        assertNotNull(controller.region);

        assertEquals(Region.US_EAST_1, controller.region);
    }

    @Test
    public void test() throws IOException {
        // 非常に時間がかかるテストのため情報の取りこぼしを最小限に抑えたいので色々printする.

        ArchiveController controller = new ArchiveController();

        printWithTime("upload start");
        UploadResult result = controller.upload(VAULT_NAME, "desc", new File(FILENAME));
        printWithTime("upload end");
        assertNotNull(result);

        printWithTime(result.toString());
        String archiveId = result.getArchiveId();
        assertNotNull(archiveId);

        File downfile = new File(DOWN_FILENAME);
        assertFalse(downfile.exists());

        // 4時間待
        printWithTime("download start");
        controller.download(VAULT_NAME, archiveId, downfile);
        printWithTime("download end");
        assertNotNull(downfile);
        assertTrue(checkSameFile(downfile));

        printWithTime("delete start");
        controller.delete(VAULT_NAME, archiveId);
        printWithTime("delete end");
    }

    private void printWithTime(String msg) {
        System.out.println(new Date().toString() + ":" + msg);
    }

    /**
     * checkSameFile()が正しく動作していることをテストする.
     * 
     * @throws IOException
     */
    @Test
    public void sameTest() throws IOException {

        assertTrue(checkSameFile(new File(DOWN_FILENAME)));
    }

    private boolean checkSameFile(File downfile) throws IOException {
        BufferedReader upfileReader = null;
        BufferedReader downfileReader = null;
        try {
            upfileReader = new BufferedReader(new FileReader(FILENAME));
            downfileReader = new BufferedReader(new FileReader(downfile));
            String upfileLine = null;
            String downfileLine = null;
            while (true) {
                upfileLine = upfileReader.readLine();
                downfileLine = downfileReader.readLine();

                if ((upfileLine != null) && (downfileLine != null)) {
                    if (!upfileLine.equals(downfileLine)) {
                        return false;
                    }
                } else {
                    // どちらかのファイルが終端に達したらループを抜ける.
                    break;
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


package jp.mironal.java.aws.app.glacier;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import jp.mironal.java.aws.app.glacier.AwsTools.Region;
import jp.mironal.java.aws.app.glacier.InventoryRetrievalResult.ArchiveInfo;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.GlacierJobDescription;
import com.amazonaws.services.glacier.transfer.UploadResult;

public class JobOperatorTest {

    private static final String VAULT_NAME = "job_operator_vault";

    private static final String FILENAME = "piyopiyo.txt";
    private static final String SAVE_FILENAME = "piyopiyo_save.txt";
    private static final String data = "aaaaaaaabsdfasあああ";

    private static String archvieId;

    @BeforeClass
    public static void setup() throws IOException {

        VaultController controller = new VaultController();
        controller.createVault(VAULT_NAME);

        File file = new File(FILENAME);
        PrintWriter pw = new PrintWriter(file);
        pw.append(data);
        pw.close();

        ArchiveController archiveController = new ArchiveController();
        UploadResult result = archiveController.upload(VAULT_NAME, "desc", file);
        archvieId = result.getArchiveId();
    }

    @AfterClass
    public static void delete() throws IOException {
        ArchiveController archiveController = new ArchiveController();
        archiveController.delete(VAULT_NAME, archvieId);

        VaultController controller = new VaultController();
        controller.deleteVault(VAULT_NAME);

        File file = new File(FILENAME);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IllegalStateException("can not delete. " + FILENAME);
            }
        }

        file = new File(SAVE_FILENAME);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IllegalStateException("can not delete. " + SAVE_FILENAME);
            }
        }

    }

    @Test
    public void test_CreateInstance() throws IOException {
        JobOperator jobOperator = new JobOperator();
        assertNotNull(jobOperator);
        assertNotNull(jobOperator.client);
        assertNotNull(jobOperator.credentials);
        assertNotNull(jobOperator.region);
        assertEquals(Region.US_EAST_1, jobOperator.region);

    }

    @Test
    public void testInventoryJob() throws IOException, InterruptedException {
        JobOperator jobOperator = new JobOperator();
        printWithTime("initiate inventory job start");
        String jobId = jobOperator.initiateInventoryJob(VAULT_NAME);
        printWithTime("initiate inventory job end");
        assertNotNull(jobId);
        System.out.println("jobId : " + jobId);

        printWithTime("describe job start");
        DescribeJobResult describeJobResult = jobOperator.describeJob(VAULT_NAME, jobId);
        printWithTime("describe job end");
        assertNotNull(describeJobResult);
        assertEquals(archvieId, describeJobResult.getArchiveId());
        assertEquals(jobId, describeJobResult.getJobId());
        assertEquals("InventoryRetrieval", describeJobResult.getAction());
        System.out.println(describeJobResult.toString());

        printWithTime("list jobs start");
        List<GlacierJobDescription> descriptions = jobOperator.listJobs(VAULT_NAME);
        printWithTime("list jobs end");
        assertNotNull(descriptions);
        assertTrue(descriptions.size() > 0);
        for (GlacierJobDescription desc : descriptions) {
            System.out.println(desc.toString());
        }
        printWithTime("wait for job to complete start");
        if (jobOperator.waitForJobToComplete()) {
            printWithTime("job complete success");
            printWithTime("download start");
            InventoryRetrievalResult inventoryRetrievalResult = jobOperator
                    .downloadInventoryJobOutput();
            printWithTime("download end");
            assertNotNull(inventoryRetrievalResult);
            System.out.println(inventoryRetrievalResult.toString());

            List<ArchiveInfo> archiveInfos = inventoryRetrievalResult.getArchiveList();
            assertNotNull(archiveInfos);
            assertTrue(archiveInfos.size() > 0);
            assertEquals(archvieId, archiveInfos.get(0).getArchiveId());
        } else {
            printWithTime("job complete fault.");
            fail();
        }

    }

    @Test
    public void testArchiveJob() throws IOException, InterruptedException {
        JobOperator jobOperator = new JobOperator();
        printWithTime("initiate archive job start");
        String jobId = jobOperator.initiateArchiveJob(VAULT_NAME, archvieId);
        printWithTime("initiate archive job end");
        assertNotNull(jobId);
        System.out.println("jobId : " + jobId);

        printWithTime("describe job start");
        DescribeJobResult describeJobResult = jobOperator.describeJob(VAULT_NAME, jobId);
        printWithTime("describe job end");
        assertNotNull(describeJobResult);
        assertEquals(archvieId, describeJobResult.getArchiveId());
        assertEquals(jobId, describeJobResult.getJobId());
        assertEquals("ArchiveRetrieval", describeJobResult.getAction());
        System.out.println(describeJobResult.toString());

        printWithTime("list jobs start");
        List<GlacierJobDescription> descriptions = jobOperator.listJobs(VAULT_NAME);
        printWithTime("list jobs end");
        assertNotNull(descriptions);
        assertTrue(descriptions.size() > 0);
        for (GlacierJobDescription desc : descriptions) {
            System.out.println(desc.toString());
        }
        printWithTime("wait for job to complete start");
        if (jobOperator.waitForJobToComplete()) {
            printWithTime("job complete success");
            File saveFile = new File(SAVE_FILENAME);
            printWithTime("download start");
            jobOperator.downloadArchiveJobOutput(saveFile);
            printWithTime("download end");
            assertTrue(checkSameFile(saveFile));
        } else {
            printWithTime("job complete fault.");
            fail();
        }

    }

    private void printWithTime(String msg) {
        System.out.println(new Date().toString() + ":" + msg);
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

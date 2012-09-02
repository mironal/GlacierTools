
package jp.mironal.java.aws.app.glacier.cmd;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import jp.mironal.java.aws.app.glacier.AwsTools.Region;
import jp.mironal.java.aws.app.glacier.cmd.RestoreJobCmd.JobRestoreException;
import jp.mironal.java.aws.app.glacier.cmd.RestoreJobCmd.RestoreJobCmdKind;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RestoreJobCmdTest {

    private static final String RESTORE_FILENAME = "RestoreJobParam.properties";

    private static File restore_file;

    @BeforeClass
    public static void setupRestoreFile() throws FileNotFoundException {
        restore_file = new File(RESTORE_FILENAME);
        PrintWriter pw = new PrintWriter(restore_file);
        pw.println("JobId=hogehoge");
        pw.println("VaultName=piyopiyo");
        pw.print("Region=us-west-2");
        pw.close();
        System.out.println("つくりますた");
    }

    @AfterClass
    public static void deleteRestoreFile() {
        if (restore_file.exists()) {

            if (restore_file.delete()) {
                System.out.println("さくじょすますた");
            }
        }

    }

    @Test
    public void test_Download() throws IOException {
        RestoreJobCmd cmd = new RestoreJobCmd(new String[] {
                "download", "--restore", RESTORE_FILENAME
        });

        assertEquals(cmd.region, Region.US_WEST_2);
        assertEquals(cmd.jobId, "hogehoge");
        assertEquals(cmd.vaultname, "piyopiyo");
        assertEquals(cmd.cmdKind, RestoreJobCmdKind.Download);
        assertTrue(cmd.validateParam());
    }

    @Test(expected = JobRestoreException.class)
    public void test_Download_InvalidParam() throws IOException {
        /* RestoreJobCmd cmd = */new RestoreJobCmd(new String[] {
            "download",
        });
    }

    @Test
    public void test_Check() throws IOException {
        RestoreJobCmd cmd = new RestoreJobCmd(new String[] {
                "check", "--restore", RESTORE_FILENAME
        });

        assertEquals(cmd.region, Region.US_WEST_2);
        assertEquals(cmd.jobId, "hogehoge");
        assertEquals(cmd.vaultname, "piyopiyo");
        assertEquals(cmd.cmdKind, RestoreJobCmdKind.Check);
        assertTrue(cmd.validateParam());
    }

    @Test(expected = JobRestoreException.class)
    public void test_Check_InvalidParam() throws IOException {
        /* RestoreJobCmd cmd = */new RestoreJobCmd(new String[] {
            "check"
        });
    }

    @Test
    public void test_Desc() throws IOException {
        RestoreJobCmd cmd = new RestoreJobCmd(new String[] {
                "desc", "--restore", RESTORE_FILENAME
        });

        assertEquals(cmd.region, Region.US_WEST_2);
        assertEquals(cmd.jobId, "hogehoge");
        assertEquals(cmd.vaultname, "piyopiyo");
        assertEquals(cmd.cmdKind, RestoreJobCmdKind.Desc);
        assertTrue(cmd.validateParam());
    }

    @Test(expected = JobRestoreException.class)
    public void test_Desc_InvalidParam() throws IOException {
        /* RestoreJobCmd cmd = */new RestoreJobCmd(new String[] {
            "desc"
        });
    }

    @Test
    public void test_Help() throws IOException {
        RestoreJobCmd cmd = new RestoreJobCmd(new String[] {
            "help"
        });

        assertNull(cmd.region);
        assertNull(cmd.jobId);
        assertNull(cmd.vaultname);
        assertEquals(cmd.cmdKind, RestoreJobCmdKind.Help);
        assertTrue(cmd.validateParam());

        cmd = new RestoreJobCmd(new String[] {
            "-h"
        });

        assertNull(cmd.region);
        assertNull(cmd.jobId);
        assertNull(cmd.vaultname);
        assertEquals(cmd.cmdKind, RestoreJobCmdKind.Help);
        assertTrue(cmd.validateParam());

        cmd = new RestoreJobCmd(new String[] {
            "--help"
        });

        assertNull(cmd.region);
        assertNull(cmd.jobId);
        assertNull(cmd.vaultname);
        assertEquals(cmd.cmdKind, RestoreJobCmdKind.Help);
        assertTrue(cmd.validateParam());
    }
}


package jp.mironal.java.aws.app.glacier.cmd;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RestoreJobCmdTest {

    private static final String RESTORE_FILENAME = "RestoreJobParam.properties";

    @BeforeClass
    public static void setupRestoreFile() throws FileNotFoundException {
        File file = new File(RESTORE_FILENAME);
        PrintWriter pw = new PrintWriter(file);
        pw.println("JobId=hogehoge");
        pw.println("VaultName=piyopiyo");
        pw.print("Region=us-west-2");
        pw.close();
    }

    @AfterClass
    public static void deleteRestoreFile() {
        File file = new File(RESTORE_FILENAME);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void test_Download() {

    }

}

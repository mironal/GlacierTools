
package jp.mironal.java.aws.app.glacier.cmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import jp.mironal.java.aws.app.glacier.AwsTools.Region;
import jp.mironal.java.aws.app.glacier.cmd.ArchiveControllerCmd.ArchiveCmdKind;

import org.junit.Test;

public class ArchiveControllerCmdTest {

    @Test
    public void test_Upload() {
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename"
        });
        assertEquals(cmd.cmdKind, ArchiveCmdKind.Upload);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, "filename");
        assertTrue(cmd.validateParam());
    }

    @Test
    public void test_Upload_InvalidParam() {
        // vault無し.
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--file", "filename"
        });
        assertEquals(cmd.cmdKind, ArchiveCmdKind.Upload);
        assertEquals(cmd.vaultName, null);
        assertEquals(cmd.filename, "filename");
        assertFalse(cmd.validateParam());

        // file無し
        cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge",
        });

        assertEquals(cmd.cmdKind, ArchiveCmdKind.Upload);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, null);
        assertFalse(cmd.validateParam());
    }

    @Test
    public void test_Download() {
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "download", "--vault", "hugahuga", "--archive", "archiveId", "--file", "savefile"
        });

        assertEquals(cmd.cmdKind, ArchiveCmdKind.Download);
        assertEquals(cmd.vaultName, "hugahuga");
        assertEquals(cmd.archiveId, "archiveId");
        assertEquals(cmd.filename, "savefile");
        assertTrue(cmd.validateParam());
    }

    @Test
    public void test_Download_InvalidParam() {

        // vault無し
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "download", "--archive", "archiveId", "--file", "savefile"
        });

        assertEquals(cmd.cmdKind, ArchiveCmdKind.Download);
        assertEquals(cmd.vaultName, null);
        assertEquals(cmd.archiveId, "archiveId");
        assertEquals(cmd.filename, "savefile");
        assertFalse(cmd.validateParam());

        // archive無し
        cmd = new ArchiveControllerCmd(new String[] {
                "download", "--vault", "hugahuga", "--file", "savefile"
        });

        assertEquals(cmd.cmdKind, ArchiveCmdKind.Download);
        assertEquals(cmd.vaultName, "hugahuga");
        assertEquals(cmd.archiveId, null);
        assertEquals(cmd.filename, "savefile");
        assertFalse(cmd.validateParam());

        // file無し
        cmd = new ArchiveControllerCmd(new String[] {
                "download", "--vault", "hugahuga", "--archive", "archiveId"
        });

        assertEquals(cmd.cmdKind, ArchiveCmdKind.Download);
        assertEquals(cmd.vaultName, "hugahuga");
        assertEquals(cmd.archiveId, "archiveId");
        assertEquals(cmd.filename, null);
        assertFalse(cmd.validateParam());
    }

    @Test
    public void test_List() {
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "list", "--vault", "hoge"
        });

        assertEquals(cmd.cmdKind, ArchiveCmdKind.List);
        assertEquals(cmd.vaultName, "hoge");
        assertNull(cmd.filename);
        assertTrue(cmd.validateParam());
    }

    @Test
    public void test_List_InvalidParam() {
        // vault無し.
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
            "list"
        });

        assertEquals(cmd.cmdKind, ArchiveCmdKind.List);
        assertNull(cmd.vaultName);
        assertNull(cmd.filename);
        assertFalse(cmd.validateParam());
    }

    @Test
    public void test_Delete() {
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "delete", "--vault", "hogehoge", "--archive", "archiveId"
        });
        assertEquals(cmd.cmdKind, ArchiveCmdKind.Delete);
        assertEquals(cmd.archiveId, "archiveId");
        assertEquals(cmd.vaultName, "hogehoge");

        assertTrue(cmd.validateParam());
    }

    @Test
    public void test_Delete_InvalidParam() {
        // vault無し
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "delete", "--archive", "archiveId"
        });
        assertEquals(cmd.cmdKind, ArchiveCmdKind.Delete);
        assertEquals(cmd.vaultName, null);
        assertEquals(cmd.archiveId, "archiveId");
        assertFalse(cmd.validateParam());

        // archive無し
        cmd = new ArchiveControllerCmd(new String[] {
                "delete", "--vault", "hogehoge",
        });

        assertEquals(cmd.cmdKind, ArchiveCmdKind.Delete);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.archiveId, null);
        assertFalse(cmd.validateParam());

    }

    @Test
    public void test_Region() {
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename", "--region", "us-east-1"
        });

        assertEquals(cmd.cmdKind, ArchiveCmdKind.Upload);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, "filename");
        assertEquals(cmd.region, Region.US_EAST_1);

        cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename", "--region", "us-west-1"
        });

        assertEquals(cmd.cmdKind, ArchiveCmdKind.Upload);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, "filename");
        assertEquals(cmd.region, Region.US_WEST_1);

        cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename", "--region", "us-west-2"
        });

        assertEquals(cmd.region, Region.US_WEST_2);

        cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename", "--region", "eu-west-1"
        });

        assertEquals(cmd.cmdKind, ArchiveCmdKind.Upload);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, "filename");
        assertEquals(cmd.region, Region.EU_WEST_1);

        cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename", "--region", "ap-northeast-1"
        });

        assertEquals(cmd.cmdKind, ArchiveCmdKind.Upload);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, "filename");
        assertEquals(cmd.region, Region.AP_NORTHEAST_1);
        assertTrue(cmd.validateParam());
    }

    @Test()
    public void test_Region_Invalid() throws InvalidRegionException {
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "create", "--vault", "hogehoge", "--file", "filename", "--region", "aa", "--debug"
        });
        assertNull(cmd.region);
        assertEquals(cmd.cmdKind, ArchiveCmdKind.Bad);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, "filename");
        assertFalse(cmd.validateParam());
    }

    @Test
    public void test_Help() {
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
            "help"
        });
        assertEquals(cmd.cmdKind, ArchiveCmdKind.Help);
        assertNull(cmd.filename);
        assertNull(cmd.vaultName);
        assertNull(cmd.archiveId);
        assertTrue(cmd.validateParam());
    }
}

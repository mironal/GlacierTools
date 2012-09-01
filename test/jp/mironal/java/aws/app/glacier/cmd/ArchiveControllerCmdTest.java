
package jp.mironal.java.aws.app.glacier.cmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jp.mironal.java.aws.app.glacier.AwsTools.Region;
import jp.mironal.java.aws.app.glacier.cmd.ArchiveControllerCmd.Kind;

import org.junit.Test;

public class ArchiveControllerCmdTest {

    @Test
    public void test_Upload() {
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename"
        });
        assertEquals(cmd.cmdKind, Kind.Upload);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, "filename");
        assertTrue(cmd.validateInventoryParam());
    }

    @Test
    public void test_Upload_InvalidParam() {
        // vault無し.
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--file", "filename"
        });
        assertEquals(cmd.cmdKind, Kind.Upload);
        assertEquals(cmd.vaultName, null);
        assertEquals(cmd.filename, "filename");
        assertFalse(cmd.validateInventoryParam());

        // file無し
        cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge",
        });

        assertEquals(cmd.cmdKind, Kind.Upload);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, null);
        assertFalse(cmd.validateInventoryParam());
    }

    @Test
    public void test_Download() {
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "download", "--vault", "hugahuga", "--archive", "archiveId", "--file", "savefile"
        });

        assertEquals(cmd.cmdKind, Kind.Download);
        assertEquals(cmd.vaultName, "hugahuga");
        assertEquals(cmd.archiveId, "archiveId");
        assertEquals(cmd.filename, "savefile");
        assertTrue(cmd.validateInventoryParam());
    }

    @Test
    public void test_Download_InvalidParam() {

        // vault無し
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "download", "--archive", "archiveId", "--file", "savefile"
        });

        assertEquals(cmd.cmdKind, Kind.Download);
        assertEquals(cmd.vaultName, null);
        assertEquals(cmd.archiveId, "archiveId");
        assertEquals(cmd.filename, "savefile");
        assertFalse(cmd.validateInventoryParam());

        // archive無し
        cmd = new ArchiveControllerCmd(new String[] {
                "download", "--vault", "hugahuga", "--file", "savefile"
        });

        assertEquals(cmd.cmdKind, Kind.Download);
        assertEquals(cmd.vaultName, "hugahuga");
        assertEquals(cmd.archiveId, null);
        assertEquals(cmd.filename, "savefile");
        assertFalse(cmd.validateInventoryParam());

        // file無し
        cmd = new ArchiveControllerCmd(new String[] {
                "download", "--vault", "hugahuga", "--archive", "archiveId"
        });

        assertEquals(cmd.cmdKind, Kind.Download);
        assertEquals(cmd.vaultName, "hugahuga");
        assertEquals(cmd.archiveId, "archiveId");
        assertEquals(cmd.filename, null);
        assertFalse(cmd.validateInventoryParam());
    }

    @Test
    public void test_Delete() {
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "delete", "--vault", "hogehoge", "--archive", "archiveId"
        });
        assertEquals(cmd.cmdKind, Kind.Delete);
        assertEquals(cmd.archiveId, "archiveId");
        assertEquals(cmd.vaultName, "hogehoge");

        assertTrue(cmd.validateInventoryParam());
    }

    @Test
    public void test_Delete_InvalidParam() {
        // vault無し
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "delete", "--archive", "archiveId"
        });
        assertEquals(cmd.cmdKind, Kind.Delete);
        assertEquals(cmd.vaultName, null);
        assertEquals(cmd.archiveId, "archiveId");
        assertFalse(cmd.validateInventoryParam());

        // archive無し
        cmd = new ArchiveControllerCmd(new String[] {
                "delete", "--vault", "hogehoge",
        });

        assertEquals(cmd.cmdKind, Kind.Delete);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.archiveId, null);
        assertFalse(cmd.validateInventoryParam());

    }

    @Test
    public void test_Region() {
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename", "--endpoint", "us-east-1"
        });

        assertEquals(cmd.cmdKind, Kind.Upload);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, "filename");
        assertEquals(cmd.region, Region.US_EAST_1);

        cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename", "--endpoint", "us-west-1"
        });

        assertEquals(cmd.cmdKind, Kind.Upload);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, "filename");
        assertEquals(cmd.region, Region.US_WEST_1);

        cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename", "--endpoint", "us-west-2"
        });

        try {
            assertEquals(cmd.region, Region.US_WEST_2);
        } catch (InvalidRegionException e) {
            fail();
            e.printStackTrace();
        }

        cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename", "--endpoint", "eu-west-1"
        });

        assertEquals(cmd.cmdKind, Kind.Upload);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, "filename");
        assertEquals(cmd.region, Region.EU_WEST_1);

        cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename", "--endpoint",
                "ap-northeast-1"
        });

        assertEquals(cmd.cmdKind, Kind.Upload);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, "filename");
        assertEquals(cmd.region, Region.AP_NORTHEAST_1);
        assertTrue(cmd.validateInventoryParam());

    }

    @Test()
    public void test_Region_Invalid() throws InvalidRegionException {
        ArchiveControllerCmd cmd = new ArchiveControllerCmd(new String[] {
                "upload", "--vault", "hogehoge", "--file", "filename", "--endpoint", "aa",
                "--debug"
        });
        assertNull(cmd.region);
        assertEquals(cmd.cmdKind, Kind.Bad);
        assertEquals(cmd.vaultName, "hogehoge");
        assertEquals(cmd.filename, "filename");
        assertFalse(cmd.validateInventoryParam());
    }
}

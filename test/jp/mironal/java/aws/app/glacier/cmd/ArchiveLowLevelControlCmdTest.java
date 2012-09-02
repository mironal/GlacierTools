
package jp.mironal.java.aws.app.glacier.cmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jp.mironal.java.aws.app.glacier.AwsTools.Region;
import jp.mironal.java.aws.app.glacier.cmd.ArchiveLowLevelControlCmd.ArchiveLowLevelKind;
import jp.mironal.java.aws.app.glacier.cmd.ArchiveLowLevelControlCmd.Sync;

import org.junit.Test;

public class ArchiveLowLevelControlCmdTest {

    @Test
    public void test_inventoryRetrieval() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "inventory-retrieval", "--vault", "hogehoge", "--debug"
        });

        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Inventory);
        assertEquals(cmd.vaultname, "hogehoge");

        assertTrue(cmd.validateParam());
    }

    @Test
    public void test_inventoryRetrieval_InvalidParam() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "inventory-retrieval", "--debug"
        });

        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Inventory);
        assertEquals(cmd.vaultname, null);

        assertFalse(cmd.validateParam());
    }

    @Test
    public void test_inventoryRetrievalWithEndpoint() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "inventory-retrieval", "--vault", "hogehoge", "--endpoint", "us-east-1"
        });

        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Inventory);
        assertEquals(cmd.vaultname, "hogehoge");
        assertEquals(cmd.region, Region.US_EAST_1);
        assertTrue(cmd.validateParam());
    }

    @Test
    public void test_inventoryRetrievalWithSync() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "inventory-retrieval", "--vault", "hogehoge", "--debug"
        });

        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Inventory);
        assertEquals(cmd.vaultname, "hogehoge");
        assertEquals(cmd.syncType, Sync.Sync);
        assertTrue(cmd.validateParam());

        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "inventory-retrieval", "async", "--vault", "hogehoge",
        });
        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Inventory);
        assertEquals(cmd.vaultname, "hogehoge");
        assertEquals(cmd.syncType, Sync.Async);
        assertTrue(cmd.validateParam());

    }

    @Test
    public void test_archiveRetrieval() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "archive-retrieval", "--vault", "homuhomu", "--archive", "archiveId", "--file",
                "hoge.zip"
        });

        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Archive);
        assertEquals(cmd.vaultname, "homuhomu");
        assertEquals(cmd.archiveId, "archiveId");
        assertEquals(cmd.saveFile, "hoge.zip");
        assertTrue(cmd.validateParam());
    }

    @Test
    public void test_archiveRetrieval_InvalidParam() {
        // vault無し
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "archive-retrieval", "--archive", "archiveId", "--file", "hoge.zip"
        });

        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Archive);
        assertEquals(cmd.vaultname, null);
        assertEquals(cmd.archiveId, "archiveId");
        assertEquals(cmd.saveFile, "hoge.zip");
        assertFalse(cmd.validateParam());

        // archive無し
        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "archive-retrieval", "--vault", "homuhomu", "--file", "hoge.zip"
        });

        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Archive);
        assertEquals(cmd.vaultname, "homuhomu");
        assertEquals(cmd.archiveId, null);
        assertEquals(cmd.saveFile, "hoge.zip");
        assertFalse(cmd.validateParam());

        // file無し
        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "archive-retrieval", "--vault", "homuhomu", "--archive", "archiveId",
        });

        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Archive);
        assertEquals(cmd.vaultname, "homuhomu");
        assertEquals(cmd.archiveId, "archiveId");
        assertEquals(cmd.saveFile, null);
        assertFalse(cmd.validateParam());
    }

    @Test
    public void test_list() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "list", "--vault", "aaaaaa", "--debug"
        });
        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.List);
        assertEquals(cmd.vaultname, "aaaaaa");
        assertTrue(cmd.validateParam());
    }

    @Test
    public void test_list_InvalidParam() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
            "list",
        });
        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.List);
        assertEquals(cmd.vaultname, null);
        assertFalse(cmd.validateParam());
    }

    @Test
    public void test_desc() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId"
        });
        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Describe);
        assertEquals(cmd.vaultname, "bbbbb");
        assertEquals(cmd.jobId, "jobId");
        assertTrue(cmd.validateParam());
    }

    @Test
    public void test_desc_InvalidParam() {
        // vault無し
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--job", "jobId"
        });
        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Describe);
        assertEquals(cmd.vaultname, null);
        assertEquals(cmd.jobId, "jobId");
        assertFalse(cmd.validateParam());

        // job無し
        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb",
        });
        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Describe);
        assertEquals(cmd.vaultname, "bbbbb");
        assertEquals(cmd.jobId, null);
        assertFalse(cmd.validateParam());

    }

    @Test
    public void test_Region() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId", "--endpoint", "us-east-1"
        });

        assertEquals(cmd.region, Region.US_EAST_1);

        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId", "--endpoint", "us-west-1"
        });

        assertEquals(cmd.region, Region.US_WEST_1);

        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId", "--endpoint", "us-west-2"
        });

        try {
            assertEquals(cmd.region, Region.US_WEST_2);
        } catch (InvalidRegionException e) {
            fail();
            e.printStackTrace();
        }

        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId", "--endpoint", "eu-west-1"
        });

        assertEquals(cmd.region, Region.EU_WEST_1);

        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId", "--endpoint", "ap-northeast-1"
        });

        assertEquals(cmd.region, Region.AP_NORTHEAST_1);
        assertTrue(cmd.validateParam());

    }

    @Test()
    public void test_Region_Invalid() throws InvalidRegionException {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId", "--endpoint", "aa"
        });
        assertNull(cmd.region);
        assertEquals(cmd.cmdKind, ArchiveLowLevelKind.Bad);
    }

}

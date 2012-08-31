
package jp.mironal.java.aws.app.glacier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import jp.mironal.java.aws.app.glacier.ArchiveLowLevelControlCmd.InvalidRegionException;
import jp.mironal.java.aws.app.glacier.ArchiveLowLevelControlCmd.Kind;
import jp.mironal.java.aws.app.glacier.ArchiveLowLevelControlCmd.Sync;

import org.junit.Test;

public class ArchiveLowLevelControlCmdTest {

    @Test
    public void test_inventoryRetrieval() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "inventory-retrieval", "--vault", "hogehoge"
        });

        assertEquals(cmd.cmdKind, Kind.Inventory);
        assertEquals(cmd.vaultname, "hogehoge");
    }

    @Test
    public void test_inventoryRetrievalWithEndpoint() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "inventory-retrieval", "--vault", "hogehoge", "--endpoint", "foo"
        });

        assertEquals(cmd.cmdKind, Kind.Inventory);
        assertEquals(cmd.vaultname, "hogehoge");
        assertEquals(cmd.endpointStr, "foo");
    }

    @Test
    public void test_inventoryRetrievalWithSync() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "inventory-retrieval", "--vault", "hogehoge",
        });

        assertEquals(cmd.cmdKind, Kind.Inventory);
        assertEquals(cmd.vaultname, "hogehoge");
        assertEquals(cmd.syncType, Sync.Sync);

        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "inventory-retrieval", "async", "--vault", "hogehoge",
        });
        assertEquals(cmd.cmdKind, Kind.Inventory);
        assertEquals(cmd.vaultname, "hogehoge");
        assertEquals(cmd.syncType, Sync.Async);

    }

    @Test
    public void test_archiveRetrieval() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "archive-retrieval", "--vault", "homuhomu", "--archive", "archiveId"
        });

        assertEquals(cmd.cmdKind, Kind.Archive);
        assertEquals(cmd.vaultname, "homuhomu");
        assertEquals(cmd.archiveId, "archiveId");
    }

    @Test
    public void test_list() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "list", "--vault", "aaaaaa"
        });
        assertEquals(cmd.cmdKind, Kind.List);
        assertEquals(cmd.vaultname, "aaaaaa");
    }

    @Test
    public void test_desc() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId"
        });
        assertEquals(cmd.cmdKind, Kind.Describe);
        assertEquals(cmd.vaultname, "bbbbb");
        assertEquals(cmd.jobId, "jobId");
    }

    @Test
    public void test_Region() {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId", "--endpoint", "us-east-1"
        });

        try {
            assertEquals(cmd.getRegion(), jp.mironal.java.aws.app.glacier.AwsTools.Region.US_EAST_1);
        } catch (InvalidRegionException e) {
            fail();
            e.printStackTrace();
        }

        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId", "--endpoint", "us-west-1"
        });

        try {
            assertEquals(cmd.getRegion(), jp.mironal.java.aws.app.glacier.AwsTools.Region.US_WEST_1);
        } catch (InvalidRegionException e) {
            fail();
            e.printStackTrace();
        }

        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId", "--endpoint", "us-west-2"
        });

        try {
            assertEquals(cmd.getRegion(), jp.mironal.java.aws.app.glacier.AwsTools.Region.US_WEST_2);
        } catch (InvalidRegionException e) {
            fail();
            e.printStackTrace();
        }

        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId", "--endpoint", "eu-west-1"
        });

        try {
            assertEquals(cmd.getRegion(), jp.mironal.java.aws.app.glacier.AwsTools.Region.EU_WEST_1);
        } catch (InvalidRegionException e) {
            fail();
            e.printStackTrace();
        }

        cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId", "--endpoint", "ap-northeast-1"
        });

        try {
            assertEquals(cmd.getRegion(),
                    jp.mironal.java.aws.app.glacier.AwsTools.Region.AP_NORTHEAST_1);
        } catch (InvalidRegionException e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test(expected = InvalidRegionException.class)
    public void test_Region_Invalid() throws InvalidRegionException {
        ArchiveLowLevelControlCmd cmd = new ArchiveLowLevelControlCmd(new String[] {
                "desc", "--vault", "bbbbb", "--job", "jobId", "--endpoint", "aa"
        });
        cmd.getRegion();
    }
}

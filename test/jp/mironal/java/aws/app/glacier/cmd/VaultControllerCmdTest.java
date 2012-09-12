
package jp.mironal.java.aws.app.glacier.cmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import jp.mironal.java.aws.app.glacier.AwsTools.Region;
import jp.mironal.java.aws.app.glacier.GlacierTools;
import jp.mironal.java.aws.app.glacier.cmd.VaultControllerCmd.VaultCmdKind;

import org.junit.Test;

public class VaultControllerCmdTest {

    @Test
    public void test_Create() {
        VaultControllerCmd cmd = new VaultControllerCmd(new String[] {
                "create", "--vault", "hogehoge", "--debug"
        });

        assertEquals(cmd.cmdKind, VaultCmdKind.Create);
        assertEquals(cmd.region, GlacierTools.getDefaultEndpoint());
        assertEquals(cmd.vaultName, "hogehoge");
        assertTrue(cmd.validateParam());
        assertTrue(cmd.isDebug());

        // with region
        cmd = new VaultControllerCmd(new String[] {
                "create", "--vault", "hogehoge", "--region", "us-west-1",
        });
        assertEquals(cmd.cmdKind, VaultCmdKind.Create);
        assertEquals(cmd.region, Region.US_WEST_1);
        assertEquals(cmd.vaultName, "hogehoge");
        assertTrue(cmd.validateParam());
        assertFalse(cmd.isDebug());
    }

    @Test
    public void test_Create_InvalidParam() {
        // vault無し.
        VaultControllerCmd cmd = new VaultControllerCmd(new String[] {
            "create"
        });

        assertEquals(cmd.cmdKind, VaultCmdKind.Create);
        assertNull(cmd.vaultName);
        assertFalse(cmd.validateParam());
    }

    @Test
    public void test_Desc() {
        VaultControllerCmd cmd = new VaultControllerCmd(new String[] {
                "desc", "--vault", "homuhomu"
        });

        assertEquals(cmd.cmdKind, VaultCmdKind.Describe);
        assertEquals(cmd.region, GlacierTools.getDefaultEndpoint());
        assertEquals(cmd.vaultName, "homuhomu");
        assertTrue(cmd.validateParam());

        // with region
        cmd = new VaultControllerCmd(new String[] {
                "desc", "--vault", "homuhomu", "--region", "us-west-2"
        });
        assertEquals(cmd.cmdKind, VaultCmdKind.Describe);
        assertEquals(cmd.region, Region.US_WEST_2);
        assertEquals(cmd.vaultName, "homuhomu");
        assertTrue(cmd.validateParam());
    }

    @Test
    public void test_Desc_InvalidParam() {
        // vault無し.
        VaultControllerCmd cmd = new VaultControllerCmd(new String[] {
            "desc"
        });

        assertEquals(cmd.cmdKind, VaultCmdKind.Describe);
        assertNull(cmd.vaultName);
        assertFalse(cmd.validateParam());
    }

    @Test
    public void test_List() {
        VaultControllerCmd cmd = new VaultControllerCmd(new String[] {
            "list",
        });

        assertEquals(cmd.cmdKind, VaultCmdKind.List);
        assertEquals(cmd.region, GlacierTools.getDefaultEndpoint());
        assertNull(cmd.vaultName);
        assertTrue(cmd.validateParam());

        // with region
        cmd = new VaultControllerCmd(new String[] {
                "list", "--region", "eu-west-1"
        });

        assertEquals(cmd.cmdKind, VaultCmdKind.List);
        assertEquals(cmd.region, Region.EU_WEST_1);
        assertNull(cmd.vaultName);
        assertTrue(cmd.validateParam());
    }

    @Test
    public void test_List_InvalidParam() {
        // listはオプションを持たないのでテストしない.
        // オプションをつけたとしても使用しないので問題なし.
    }

    @Test
    public void test_Delete() {
        VaultControllerCmd cmd = new VaultControllerCmd(new String[] {
                "delete", "--vault", "homuhomu"
        });

        assertEquals(cmd.cmdKind, VaultCmdKind.Delete);
        assertEquals(cmd.region, GlacierTools.getDefaultEndpoint());
        assertEquals(cmd.vaultName, "homuhomu");
        assertTrue(cmd.validateParam());

        // with region
        cmd = new VaultControllerCmd(new String[] {
                "delete", "--vault", "homuhomu", "--region", "ap-northeast-1",
        });

        assertEquals(cmd.cmdKind, VaultCmdKind.Delete);
        assertEquals(cmd.region, Region.AP_NORTHEAST_1);
        assertEquals(cmd.vaultName, "homuhomu");
        assertTrue(cmd.validateParam());
    }

    @Test
    public void test_Delete_InvalidParam() {
        // vault無し.
        VaultControllerCmd cmd = new VaultControllerCmd(new String[] {
            "delete",
        });

        assertEquals(cmd.cmdKind, VaultCmdKind.Delete);
        assertEquals(cmd.region, GlacierTools.getDefaultEndpoint());
        assertNull(cmd.vaultName);
        assertFalse(cmd.validateParam());
    }

    @Test
    public void test_Region_Invalid() throws InvalidRegionException {
        VaultControllerCmd cmd = new VaultControllerCmd(new String[] {
                "create", "--vault", "hogehoge", "--region", "aa"
        });
        assertNull(cmd.region);
        assertEquals(cmd.cmdKind, VaultCmdKind.Bad);
        assertEquals(cmd.vaultName, "hogehoge");
        assertFalse(cmd.validateParam());
    }

    @Test
    public void test_Help() {
        VaultControllerCmd cmd = new VaultControllerCmd(new String[] {
            "-h"
        });
        assertEquals(cmd.cmdKind, VaultCmdKind.Help);
        assertNull(cmd.vaultName);
        assertTrue(cmd.validateParam());

        cmd = new VaultControllerCmd(new String[] {
            "--help"
        });
        assertEquals(cmd.cmdKind, VaultCmdKind.Help);
        assertNull(cmd.vaultName);
        assertTrue(cmd.validateParam());

        cmd = new VaultControllerCmd(new String[] {
            "help"
        });
        assertEquals(cmd.cmdKind, VaultCmdKind.Help);
        assertNull(cmd.vaultName);
        assertTrue(cmd.validateParam());
    }

}

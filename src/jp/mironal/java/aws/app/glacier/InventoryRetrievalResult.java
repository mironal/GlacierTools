
package jp.mironal.java.aws.app.glacier;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

public class InventoryRetrievalResult {

    public class ArchiveInfo {
        private String archiveId;
        private String archiveDescription;
        private String creationDate;
        private long size;
        private String SHA256TreeHash;

        private ArchiveInfo(JsonNode node) {
            archiveId = node.get("ArchiveId").getTextValue();
            archiveDescription = node.get("ArchiveDescription").getTextValue();
            creationDate = node.get("CreationDate").getTextValue();
            size = node.get("Size").getLongValue();
            SHA256TreeHash = node.get("SHA256TreeHash").getTextValue();
        }

        public String getArchiveId() {
            return this.archiveId;
        }

        public String getArchiveDescription() {
            return this.archiveDescription;
        }

        public String getCreationDate() {
            return this.creationDate;
        }

        public long getSize() {
            return this.size;
        }

        public String getSHA256TreeHash() {
            return this.SHA256TreeHash;
        }
    }

    private final String vaultArn;
    private final String inventoryDate;
    private final List<ArchiveInfo> archiveList = new ArrayList<ArchiveInfo>();

    public InventoryRetrievalResult(InputStream in) throws JsonParseException, IOException,
            ParseException {

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();

        JsonParser parser = factory.createJsonParser(in);

        JsonNode node = mapper.readTree(parser);
        vaultArn = node.get("VaultARN").getTextValue();
        inventoryDate = node.get("InventoryDate").getTextValue();

        for (JsonNode archive : node.findValue("ArchiveList")) {
            archiveList.add(new ArchiveInfo(archive));
        }
    }

    /**
     * Get the VaultARN.
     * 
     * @return String VaultARN
     */
    public String getVaultArn() {
        return this.vaultArn;
    }

    /**
     * Get the InventoryDate
     * 
     * @return String InventoryDate.
     */
    public String getInventoryDate() {
        return this.inventoryDate;
    }

    /**
     * Get the Archive list.
     * 
     * @return List Archives
     */
    public List<ArchiveInfo> getArchiveList() {
        return archiveList;
    }

    @Override
    public String toString() {
        final String NL = System.lineSeparator();
        final String SP = "    ";
        StringBuilder builder = new StringBuilder();
        builder.append("VaultArn      : ").append(getVaultArn()).append(NL);
        builder.append("InventoryDate : ").append(getInventoryDate()).append(NL);
        builder.append("ArchiveList   :").append(NL);
        for (ArchiveInfo info : getArchiveList()) {
            String separator = "";
            builder.append(separator);
            builder.append(SP).append("ArchiveId          : ").append(info.getArchiveId())
                    .append(NL);
            builder.append(SP).append("ArchiveDescription : ").append(info.getArchiveDescription())
                    .append(NL);
            builder.append(SP).append("CreationDate       : ").append(info.getCreationDate())
                    .append(NL);
            builder.append(SP).append("Size               : ").append(info.getSize()).append(NL);
            builder.append(SP).append("SHA256TreeHash     : ").append(info.getSHA256TreeHash())
                    .append(NL);

            separator = NL;
        }
        return builder.toString();
    }
}

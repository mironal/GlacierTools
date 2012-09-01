
package jp.mironal.java.aws.app.glacier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import org.codehaus.jackson.JsonParseException;
import org.junit.Test;

public class InventoryRetrievalOutputParserTest {

    @Test
    public void test() throws JsonParseException, UnsupportedEncodingException, IOException,
            ParseException {

        new InventoryRetrievalOutputParser(new FileInputStream("TestString.json"));
    }

}

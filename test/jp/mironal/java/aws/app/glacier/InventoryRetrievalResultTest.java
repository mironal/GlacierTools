
package jp.mironal.java.aws.app.glacier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import org.codehaus.jackson.JsonParseException;
import org.junit.Test;

public class InventoryRetrievalResultTest {

    @Test
    public void test() throws JsonParseException, UnsupportedEncodingException, IOException,
            ParseException {

        new InventoryRetrievalResult(new FileInputStream("TestString.json"));
    }

}

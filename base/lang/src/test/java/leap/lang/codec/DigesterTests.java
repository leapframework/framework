package leap.lang.codec;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

//from spring-security-crypto
public class DigesterTests {

    @Test
    public void digestIsCorrectFor3Iterations() {
        Digester digester = new Digester("SHA-1", 3);
        byte[] result = digester.digest(Utf8.encode("text"));
        // echo -n text | openssl sha1 -binary | openssl sha1 -binary | openssl sha1
        assertEquals("3cfa28da425eca5b894f0af2b158adf7001e000f", new String(Hex.encode(result)));
    }

}

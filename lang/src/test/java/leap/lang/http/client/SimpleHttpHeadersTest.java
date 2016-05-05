package leap.lang.http.client;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by fengzh on 5/5/16.
 */
public class SimpleHttpHeadersTest {
    @Test
    public void exists() throws Exception {
        SimpleHttpHeaders headers = new SimpleHttpHeaders();

        headers.add("Host", "localhost");

        assertTrue(headers.exists("Host"));
        assertTrue(headers.exists("host"));
        assertFalse(headers.exists("host123"));
    }

}
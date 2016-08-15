package leap.lang.util;

import leap.junit.concurrent.ConcurrentTestCase;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ShortIDTest extends ConcurrentTestCase {

    @Test
    public void testUnique() {
        int num = 20000;

        Set<String> set = new HashSet<>(num);

        int maxlen = 0;
        for(int i=0;i<num;i++) {
            String id = ShortID.generate();
            maxlen = Math.max(maxlen, id.length());
            set.add(id);
        }

        assertTrue(maxlen < 17);
        assertEquals(num, set.size());
    }

}

package leap.orm;

import leap.core.annotation.Inject;
import leap.core.junit.AppTestBase;
import org.junit.Test;

public class OrmConfigTest extends AppTestBase {

    private @Inject OrmConfig config;

    @Test
    public void testSerializeConfig() {
        assertEquals("json", config.getDefaultSerializer());

        OrmConfig.SerializeConfig defaultSerializeConfig = config.getDefaultSerializeConfig();
        assertNotNull(defaultSerializeConfig);

        assertEquals(1, config.getSerializeConfigs().size());
        assertSame(defaultSerializeConfig, config.getSerializeConfigs().get(config.getDefaultSerializer()));
    }

}

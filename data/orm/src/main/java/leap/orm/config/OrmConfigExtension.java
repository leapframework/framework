package leap.orm.config;

import java.util.HashMap;
import java.util.Map;

public class OrmConfigExtension {

    private Map<String,SerializeConfigImpl> serializeConfigs = new HashMap<>();

    public Map<String, SerializeConfigImpl> getSerializeConfigs() {
        return serializeConfigs;
    }

    public void addSerializeConfig(String name, SerializeConfigImpl sc) {
        serializeConfigs.put(name.toLowerCase(), sc);
    }

}
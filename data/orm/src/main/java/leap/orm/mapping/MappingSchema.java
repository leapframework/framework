package leap.orm.mapping;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class MappingSchema {

    private final Map<String, EntityMapping> ems;

    public MappingSchema(Map<String, EntityMapping> ems) {
        this.ems = Collections.unmodifiableMap(ems);
    }

    public Collection<EntityMapping> getEntityMappings() {
        return ems.values();
    }

    public EntityMapping getEntityMapping(String name) {
        return ems.get(name.toLowerCase());
    }

}

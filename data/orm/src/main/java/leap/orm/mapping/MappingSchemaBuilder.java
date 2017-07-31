package leap.orm.mapping;

import leap.lang.Buildable;
import leap.lang.exception.ObjectExistsException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class MappingSchemaBuilder implements Buildable<MappingSchema> {

    private final Map<String, EntityMappingBuilder> ems = new LinkedHashMap<>();

    public Collection<EntityMappingBuilder> getEntityMappings() {
        return ems.values();
    }

    public EntityMappingBuilder getEntityMapping(String name) {
        return ems.get(name.toLowerCase());
    }

    public void addEntity(EntityMappingBuilder em) throws ObjectExistsException {
        String key = em.getEntityName().toLowerCase();

        if(ems.containsKey(key)) {
            throw new ObjectExistsException("The entity mapping '" + em.getEntityName() + "' already exists in schema");
        }

        ems.put(key, em);
    }

    @Override
    public MappingSchema build() {
        Map<String, EntityMapping> map = new LinkedHashMap<>();

        ems.forEach((k, e) -> map.put(k, e.build()));

        return new MappingSchema(map);
    }
}
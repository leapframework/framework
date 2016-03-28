/*
 * Copyright 2016 Bingosoft Inc. All rights reserved.
 */
package leap.orm.mapping.config;

import leap.lang.Collections2;
import leap.orm.mapping.FieldMappingBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

public class GlobalFieldMappingConfig extends FieldMappingConfig {

    private final Set<String> includedEntities = new LinkedHashSet<>();
    private final Set<String> excludedEntities = new LinkedHashSet<>();

    public GlobalFieldMappingConfig(FieldMappingBuilder field) {
        super(field);
    }

    public GlobalFieldMappingConfig(FieldMappingBuilder field, FieldMappingStrategy strategy) {
        super(field, strategy);
    }

    public Set<String> getIncludedEntities() {
        return includedEntities;
    }

    public void addIncludedEntities(String... names) {
        Collections2.addAll(includedEntities, names);
    }

    public Set<String> getExcludedEntities() {
        return excludedEntities;
    }

    public void addExcludedEntities(String... names) {
        Collections2.addAll(this.excludedEntities, names);
    }

}

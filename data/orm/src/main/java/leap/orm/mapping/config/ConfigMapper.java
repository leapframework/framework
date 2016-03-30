/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.orm.mapping.config;

import leap.core.annotation.Inject;
import leap.orm.mapping.*;
import leap.orm.metadata.MetadataException;

public class ConfigMapper implements Mapper {

    protected @Inject MappingConfigSource source;

    @Override
    public void completeMappings(MappingConfigContext context) throws MetadataException {
        //load mapping configs.
        MappingConfig config = source.load(context.getOrmContext());

        //process the loaded mapping configs.
        processLoadedMappingConfigs(context, config);
    }

    protected void processLoadedMappingConfigs(MappingConfigContext context, MappingConfig config) {

        processGlobalFields(context, config);

    }

    protected void processGlobalFields(MappingConfigContext context, MappingConfig config) {

        for(GlobalFieldMappingConfig gf : config.getGlobalFields()) {
            processGlobalField(context, gf);
        }

    }

    protected void processGlobalField(MappingConfigContext context, GlobalFieldMappingConfig gf) {
        for(EntityMappingBuilder em : context.getEntityMappings()) {

            if(isIncluded(context, em, gf) && shoudMapping(context, em, gf)) {

                applyGlobalField(context, em, gf.getField());

            }
        }
    }

    protected void applyGlobalField(MappingConfigContext context, EntityMappingBuilder em, FieldMappingBuilder fm) {
        if(em.findFieldMappingByName(fm.getFieldName()) != null) {
            throw new MappingConfigException("Cannot apply global field '" + fm.getFieldName() +
                                             "' to entity '" + em.getEntityName() +
                                             "', the field already exists!");
        }

        FieldMappingBuilder real =
                context.getMappingStrategy().createFieldMappingByTemplate(context, em, fm);

        em.addFieldMapping(real);
    }

    protected boolean isIncluded(MappingConfigContext context, EntityMappingBuilder em, GlobalFieldMappingConfig gf) {

        if(!gf.getIncludedEntities().isEmpty()) {

            for(String name : gf.getIncludedEntities()) {

                if(name.equalsIgnoreCase(em.getEntityName())) {
                    return true;
                }

                if(null != em.getEntityClass() && em.getEntityClass().getName().equals(name)) {
                    return true;
                }

            }

            return false;
        }

        if(!gf.getExcludedEntities().isEmpty()) {

            for(String name : gf.getExcludedEntities()) {

                if(name.equalsIgnoreCase(em.getEntityName())) {
                    return false;
                }

                if(null != em.getEntityClass() && em.getEntityClass().getName().equals(name)) {
                    return false;
                }

            }

            return true;
        }

        return true;
    }

    protected boolean shoudMapping(MappingConfigContext context, EntityMappingBuilder em, FieldMappingConfig fc) {
        FieldMappingStrategy strategy = fc.getStrategy();

        if(strategy == FieldMappingStrategy.MANDATORY) {
            return true;
        }

        if(strategy == FieldMappingStrategy.IF_FIELD_NOT_EXISTS) {

            for(FieldMappingBuilder fm : em.getFieldMappings()) {
                if(fm.getFieldName().equalsIgnoreCase(fc.getField().getFieldName())) {
                    return false;
                }
            }

            return true;
        }

        if(strategy == FieldMappingStrategy.IF_COLUMN_EXISTS) {

            if(!fc.getField().isColumnNameDeclared()) {
                return false;
            }

            if(null != em.getPhysicalTable()) {
                return em.getPhysicalTable().findColumn(fc.getField().getColumn().getName()) != null;
            }

            return false;
        }

        return false;
    }
}
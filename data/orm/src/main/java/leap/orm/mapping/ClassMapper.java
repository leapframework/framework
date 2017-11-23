/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.orm.mapping;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.lang.resource.ResourceSet;
import leap.orm.annotation.Entity;
import leap.orm.annotation.ExtendedEntity;
import leap.orm.annotation.RestEntity;
import leap.orm.metadata.MetadataException;

import java.util.HashSet;
import java.util.Set;

public class ClassMapper implements Mapper {

    protected @Inject @M AppConfig       config;
    protected @Inject @M MappingStrategy mappingStrategy;

	@Override
    public void loadMappings(final MappingConfigContext context) throws MetadataException {
		ResourceSet resources = config.getResources();

        final Set<Class<?>> mapped = new HashSet<>();

		resources.processClasses((cls) -> {
            loadMapping(context, mapped, cls, true);
		});

        mapped.clear();

    }

    public void loadMappings(final MappingConfigContext context, Class... classes) {
        final Set<Class<?>> mapped = new HashSet<>();

        for(Class<?> cls : classes) {
            loadMapping(context, mapped, cls, false);
        }

        mapped.clear();
    }

    protected void loadMapping(final MappingConfigContext context, final Set<Class<?>> mapped, Class<?> cls, boolean checkContext) {
        if(mappingStrategy.isExplicitEntity(context,cls)){

            if(checkContext && !mappingStrategy.isContextModel(context.getOrmContext(), cls)) {
                return;
            }

            if(mapped.contains(cls)) {
                return;
            }

            RestEntity re= cls.getAnnotation(RestEntity.class);
            EntityMappingBuilder emb=null;
            if(re!=null){
            	emb=mappingStrategy.createRemoteEntityMappingByClass(context, cls);
            }else{
            	emb = mappingStrategy.createEntityMappingByClass(context, cls);
            }

            ExtendedEntity a = cls.getAnnotation(ExtendedEntity.class);
            if(null != a) {
                mapped.add(processExtendedEntityMapping(context, emb, a));
            }else{
                context.addEntityMapping(emb);
                mapped.add(cls);
            }
        }
    }

    protected Class<?> processExtendedEntityMapping(final MappingConfigContext context, EntityMappingBuilder ext, ExtendedEntity a) {
        Class<?> extClass  = ext.getEntityClass();
        Class<?> baseClass = Void.class.equals(a.of()) ? extClass.getSuperclass() : a.of();

        EntityMappingBuilder base = context.tryGetEntityMapping(baseClass);
        if(null == base) {
            base = mappingStrategy.createEntityMappingByClass(context, baseClass);
        }
        base.setExtendedEntityClass(extClass);

        //merge
        for(FieldMappingBuilder fmb : ext.getFieldMappings()) {
            if(null == base.findFieldMappingByName(fmb.getFieldName())) {
                base.addFieldMapping(fmb);
            }
        }

        //update or add entity mapping.
        context.removeEntityMapping(base.getEntityName());
        context.addEntityMapping(base);

        return baseClass;
    }

}
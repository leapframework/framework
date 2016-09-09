/*
 * Copyright 2015 the original author or authors.
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
package leap.orm.metadata;

import leap.core.annotation.Inject;
import leap.lang.Args;
import leap.lang.Types;
import leap.lang.beans.BeanProperty;
import leap.lang.meta.*;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.mapping.RelationProperty;

import java.lang.reflect.Type;

public class OrmMTypeFactory extends AbstractMTypeFactory implements MTypeFactory {
	
	protected @Inject OrmContext[] ormContexts;
	
	@Override
	public MType getMType(Class<?> type, Type genericType, MTypeContext context) {
        Args.notNull(context.root(), "Root factory must be exists!");

        if(Types.isSimpleType(type,genericType) || Types.isCollectionType(type,genericType)) {
            return null;
        }

        for(OrmContext c : ormContexts) {
			EntityMapping em = c.getMetadata().tryGetEntityMapping(type);
			
			if(null != em) {
				return getMType(type, genericType, context, c, em);
			}
			
		}

		return null;
	}

	protected MType getMType(Class<?> type, Type genericType, MTypeContext context, OrmContext c,  EntityMapping em) {
		MComplexTypeBuilder ct = new MComplexTypeBuilder(type);

		ct.setName(em.getEntityName());

        context.onComplexTypeCreating(type, ct.getName());

        MTypeFactory root = context.root();

		for(FieldMapping fm : em.getFieldMappings()) {
			MPropertyBuilder p = new MPropertyBuilder();
			p.setName(fm.getFieldName());

            BeanProperty bp = fm.getBeanProperty();
            if(null != bp) {
                p.setType(root.getMType(bp.getType(), bp.getGenericType()));
            }else{
                p.setType(root.getMType(fm.getJavaType()));
            }

			p.setLength(fm.getMaxLength());
			p.setRequired(!fm.isNullable());
			p.setPrecision(fm.getPrecision());
			p.setScale(fm.getScale());

            if(null != bp) {
                configureProperty(bp, p);
            }

            if(null == p.getCreatable()) {
                if(fm.isPrimaryKey()) {
                    p.setCreatable(false);
                }else{
                    p.setCreatable(fm.isInsert());
                }
            }

            if(null == p.getUpdatable()) {
                p.setUpdatable(fm.isUpdate());
            }

            if(null == p.getSortable()) {
                p.setSortable(false);
            }

            if(null == p.getFilterable()) {
                p.setFilterable(false);
            }

            if(null != bp) {
                configureProperty(bp, p);
            }

			ct.addProperty(p.build());
		}

        for(RelationProperty rp : em.getRelationProperties()) {

            EntityMapping targetEntity = c.getMetadata().getEntityMapping(rp.getTargetEntityName());

            MPropertyBuilder p = new MPropertyBuilder();
            p.setName(rp.getName());
            p.setReference(true);

            if(rp.isMany()) {
                p.setType(new MCollectionType(new MComplexTypeRef(targetEntity.getEntityName())));
            }else{
                p.setType(new MComplexTypeRef(targetEntity.getEntityName()));
            }

            if(null != rp.getBeanProperty()) {
                configureProperty(rp.getBeanProperty(), p);
            }
        }

        context.onComplexTypeCreated(type);
		
		return ct.build();
	}

}
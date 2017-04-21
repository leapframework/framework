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
package leap.orm.value;

import java.util.Map;

import leap.lang.Args;
import leap.lang.Named;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.beans.DynaBean;
import leap.orm.mapping.EntityMapping;

public abstract class EntityWrapper implements EntityBase {
	
	/**
	 * Wraps the given entity object to a {@link EntityWrapper} object.
	 * 
	 * <p>
	 * The supported entity type must be a {@link Map}, a {@link DynaBean} , a {@link Entity} or a pojo bean.
	 */
	@SuppressWarnings("rawtypes")
    public static EntityWrapper wrap(EntityMapping em,Object entity) {
		Args.notNull(em,"entity mapping");
		Args.notNull(entity,"entity");
		
		if(entity instanceof Map){
			return new MapEntityWrapper(em, entity,(Map)entity);
		}
		
		if(entity instanceof DynaBean){
			return new MapEntityWrapper(em, entity, ((DynaBean) entity).getProperties());
		}
		
		return new BeanEntityWrapper(em, entity);
	}

	protected final EntityMapping mapping;
	protected final Object		  wrapped;
	
	protected EntityWrapper(EntityMapping mapping,Object wrapped) {
		this.mapping = mapping;
		this.wrapped = wrapped;
	}

	/**
	 * Returns the {@link EntityMapping} of the wrapped entity object.
	 */
	public final EntityMapping getMapping() {
		return mapping;
	}
	
	/**
	 * Returns the wrapped entity object.
	 */
	public final Object getWrapped() {
		return wrapped;
	}
	
	@Override
    public final String getEntityName() {
	    return mapping.getEntityName();
    }
	
	@Override
    public final Object get(Named field) {
        return get(field.getName());
    }

	@Override
    public final <T extends EntityBase> T set(Named field, Object value) {
	    return set(field.getName(),value);
    }

	@SuppressWarnings("rawtypes")
	protected static final class MapEntityWrapper extends EntityWrapper {
		
        protected final Map fields;

		public MapEntityWrapper(EntityMapping mapping, Object entity, Map fields) {
	        super(mapping, entity);
	        Args.notNull(fields,"fields");
	        this.fields = fields;
        }
		
		@Override
        public boolean hasField(String field) {
	        return fields.containsKey(field);
        }

		@Override
        public Object get(String field) {
	        return fields.get(field);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends EntityBase> T set(String field, Object value) {
        	fields.put(field, value);
	        return (T)this;
        }
	}
	
	protected static final class BeanEntityWrapper extends EntityWrapper {

		protected final BeanType beanType;
		
		protected BeanEntityWrapper(EntityMapping mapping, Object entity) {
	        super(mapping, entity);
	        this.beanType = BeanType.of(entity.getClass());
        }

		@Override
        public boolean hasField(String field) {
	        return beanType.tryGetProperty(field, true) != null;
        }

		@Override
        public Object get(String field) {
	        BeanProperty bp = beanType.tryGetProperty(field,true);
	        return null == bp ? null : bp.getValue(wrapped);
        }
		
        @Override
        @SuppressWarnings("unchecked")
        public <T extends EntityBase> T set(String field, Object value) {
        	beanType.setProperty(wrapped, field, value, true);
	        return (T)this;
        }
	}
}
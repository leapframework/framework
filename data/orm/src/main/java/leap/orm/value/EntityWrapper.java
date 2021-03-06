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

import leap.lang.Args;
import leap.lang.Beans;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.beans.DynaBean;
import leap.lang.params.Params;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;
import leap.orm.model.Model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wraps an entity object (may be a {@link Map}, an {@link Entity} or a bean).
 */
public abstract class EntityWrapper implements EntityBase, Params {

    /**
     * Wraps the given entity object to a {@link EntityWrapper} object.
     *
     * <p>
     * The supported type must be a {@link Map}, a {@link DynaBean} , a {@link leap.lang.params.Params} or a pojo bean.
     */
    @SuppressWarnings("rawtypes")
    public static EntityWrapper wrap(EntityMapping em, Object entity) {
        return wrap(null, em, null, entity);
    }

    /**
     * Wraps the given entity object to a {@link EntityWrapper} object.
     *
     * <p>
     * The supported type must be a {@link Map}, a {@link DynaBean} , a {@link leap.lang.params.Params} or a pojo bean.
     */
    @SuppressWarnings("rawtypes")
    public static EntityWrapper wrap(OrmContext context, EntityMapping em, Object entity) {
        return wrap(context, em, null, entity);
    }

    /**
     * Wraps the given entity object to a {@link EntityWrapper} object.
     *
     * <p>
     * The supported type must be a {@link Map}, a {@link DynaBean} , a {@link leap.lang.params.Params} or a pojo bean.
     */
    @SuppressWarnings("rawtypes")
    public static EntityWrapper wrap(OrmContext context, EntityMapping em, Object id, Object entity) {
        Args.notNull(em, "entity mapping");
        Args.notNull(entity, "entity");

        if (entity instanceof Map) {
            return new MapWrapper(em, id, (Map) entity);
        }

        if (entity instanceof Model) {
            if (null == context) {
                throw new IllegalStateException("Orm context must be specified for Model class");
            }
            return new ModelWrapper(context, em, id, ((Model) entity));
        }

        if (entity instanceof DynaBean) {
            return new DynaWrapper(em, id, ((DynaBean) entity));
        }

        if (entity instanceof Params) {
            return new ParamsWrapper(em, id, (Params) entity);
        }

        return new BeanWrapper(em, id, entity);
    }

    protected final EntityMapping em;
    protected final Object        id;
    protected final Object        raw;

    protected EntityWrapper(EntityMapping em, Object id, Object raw) {
        this.em = em;
        this.id = id;
        this.raw = raw;
    }

    @Override
    public Map<String, Object> map() {
        return toMap();
    }

    @Override
    public Params setAll(Map<String, ?> m) {
        if (null != m) {
            m.forEach(this::set);
        }
        return this;
    }

    public final Object id() {
        return id;
    }

    public final Object tryGetIdByName(String name) {
        if (id instanceof Map) {
            return ((Map) id).get(name);
        }
        return id;
    }

    /**
     * Returns the wrapped entity.
     */
    public final <T> T raw() {
        return (T) raw;
    }

    @Override
    public boolean isEmpty() {
        return getFieldNames().isEmpty();
    }

    /**
     * Returns <code>true</code> if the wrapped entity is a POJO bean.
     */
    public boolean isBean() {
        return false;
    }

    /**
     * Returns <code>true</code> if the wrapped entity is a {@link Map}.
     */
    public boolean isMap() {
        return false;
    }

    /**
     * Returns <code>true</code> if the wrapped entity is a {@link DynaBean}.
     */
    public boolean isDyna() {
        return false;
    }

    /**
     * Returns <code>true</code> if the wrapped entity is a {@link Params}.
     */
    public boolean isParams() {
        return false;
    }

    @Override
    public final String getEntityName() {
        return em.getEntityName();
    }

    /**
     * Returns the {@link EntityMapping}.
     */
    public final EntityMapping getEntityMapping() {
        return em;
    }

    @Override
    public abstract EntityWrapper set(String field, Object value);

    public abstract Map<String, Object> toMap();

    @SuppressWarnings("rawtypes")
    protected static final class MapWrapper extends EntityWrapper {

        private final Map map;

        public MapWrapper(EntityMapping mapping, Object id, Map map) {
            super(mapping, id, map);
            Args.notNull(map, "fields");
            this.map = map;
        }

        @Override
        public boolean isMap() {
            return true;
        }

        @Override
        public Set<String> getFieldNames() {
            return map.keySet();
        }

        @Override
        public boolean contains(String name) {
            return map.containsKey(name);
        }

        @Override
        public Object get(String field) {
            return map.get(field);
        }

        @Override
        @SuppressWarnings("unchecked")
        public EntityWrapper set(String field, Object value) {
            map.put(field, value);
            return this;
        }

        @Override
        public Map<String, Object> toMap() {
            return map;
        }
    }

    protected static final class ParamsWrapper extends EntityWrapper {

        private final Params params;

        public ParamsWrapper(EntityMapping em, Object id, Params params) {
            super(em, id, params);
            this.params = params;
        }

        @Override
        public boolean isEmpty() {
            return params.isEmpty();
        }

        @Override
        public boolean isParams() {
            return true;
        }

        @Override
        public Set<String> getFieldNames() {
            return params.map().keySet();
        }

        @Override
        public boolean contains(String field) {
            return params.contains(field);
        }

        @Override
        public Object get(String field) {
            return params.get(field);
        }

        @Override
        public EntityWrapper set(String field, Object value) {
            params.set(field, value);
            return this;
        }

        @Override
        public Map<String, Object> toMap() {
            return params.map();
        }
    }

    protected static final class DynaWrapper extends EntityWrapper {

        private final DynaBean bean;

        public DynaWrapper(EntityMapping em, Object id, DynaBean bean) {
            super(em, id, bean);
            this.bean = bean;
        }

        @Override
        public boolean isDyna() {
            return true;
        }

        @Override
        public Set<String> getFieldNames() {
            return bean.getProperties().keySet();
        }

        @Override
        public boolean contains(String field) {
            return bean.getProperties().containsKey(field);
        }

        @Override
        public Object get(String field) {
            return bean.getProperty(field);
        }

        @Override
        public EntityWrapper set(String field, Object value) {
            bean.setProperty(field, value);
            return this;
        }

        @Override
        public Map<String, Object> toMap() {
            return bean.getProperties();
        }
    }

    protected static final class ModelWrapper extends EntityWrapper {

        private final Model model;

        public ModelWrapper(OrmContext context, EntityMapping em, Object id, Model model) {
            super(em, id, model);
            this.model = model;
            this.model.init(context, em);
        }

        @Override
        public boolean isDyna() {
            return true;
        }

        @Override
        public Set<String> getFieldNames() {
            return model.fields().keySet();
        }

        @Override
        public boolean contains(String field) {
            return model.contains(field);
        }

        @Override
        public Object get(String field) {
            return model.get(field);
        }

        @Override
        public EntityWrapper set(String field, Object value) {
            model.set(field, value);
            return this;
        }

        @Override
        public Map<String, Object> toMap() {
            return model.fields();
        }
    }

    protected static final class BeanWrapper extends EntityWrapper {

        protected final BeanType beanType;

        private Set<String> fieldNames;
        private Map         map;

        protected BeanWrapper(EntityMapping mapping, Object id, Object bean) {
            super(mapping, id, bean);
            this.beanType = BeanType.of(bean.getClass());
        }

        @Override
        public boolean isBean() {
            return true;
        }

        @Override
        public Set<String> getFieldNames() {
            if (null != fieldNames) {
                return fieldNames;
            }

            fieldNames = new LinkedHashSet<>();
            for (BeanProperty bp : beanType.getProperties()) {
                fieldNames.add(bp.getName());
            }
            return fieldNames;
        }

        @Override
        public boolean contains(String field) {
            return beanType.tryGetProperty(field, true) != null;
        }

        @Override
        public Object get(String field) {
            BeanProperty bp = beanType.tryGetProperty(field, true);
            return null == bp ? null : bp.getValue(raw);
        }

        @Override
        @SuppressWarnings("unchecked")
        public EntityWrapper set(String field, Object value) {
            if (!beanType.trySetProperty(raw, field, value, true)) {
                if (null == map) {
                    map = new HashMap();
                }
                map.put(field, value);
            }
            return this;
        }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> props = Beans.toMap(raw);
            if (null != map) {
                props.putAll(map);
            }
            return props;
        }
    }
}
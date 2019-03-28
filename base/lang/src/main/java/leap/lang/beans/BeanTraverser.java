/*
 *
 *  * Copyright 2019 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.lang.beans;

import leap.lang.Classes;
import leap.lang.Collections2;
import leap.lang.Enumerable;
import leap.lang.Enumerables;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class BeanTraverser {

    private final Object        bean;
    private final Set<Object>   traversed     = new HashSet<>();
    private final Set<Class<?>> skipClasses   = new HashSet<>();
    private final Set<String>   skipPackages  = new HashSet<>();
    private final Set<Class<?>> acceptClasses = new HashSet<>();

    public BeanTraverser(Object bean) {
        this.bean = bean;
        skipPackages.add("java.");
    }

    public BeanTraverser acceptClassesOnly(Class<?>... cs) {
        Collections2.addAll(acceptClasses, cs);
        return this;
    }

    public BeanTraverser skipClasses(Class<?>... cs) {
        Collections2.addAll(skipClasses, cs);
        return this;
    }

    public BeanTraverser skipPackages(String... pkgs) {
        Collections2.addAll(skipPackages, pkgs);
        return this;
    }

    public void traverse(BiConsumer<ValMeta, Object> func) {
        traverse(ValMeta.ROOT, bean, func);
    }

    protected void traverse(ValMeta meta, Object val, BiConsumer<ValMeta, Object> func) {
        if (null != val && traversed.contains(val)) {
            return;
        }

        func.accept(meta, val);

        if (null == val) {
            return;
        }

        traversed.add(val);

        if (val instanceof Map) {
            traverseMap((Map) val, func);
            return;
        }

        Enumerable e = Enumerables.tryOf(val);
        if (null != e) {
            traverseEnumerable(val, e, func);
            return;
        }

        if(!acceptClasses.isEmpty()) {
            boolean accept = false;
            for(Class<?> c : acceptClasses) {
                if(c.isAssignableFrom(val.getClass())) {
                    accept = true;
                    break;
                }
            }
            if(!accept) {
                return;
            }
        }

        for (String pkg : skipPackages) {
            if (Classes.getPackageName(val.getClass()).startsWith(pkg)) {
                return;
            }
        }

        for (Class<?> c : skipClasses) {
            if (c.isAssignableFrom(val.getClass())) {
                return;
            }
        }

        if (!Classes.isSimpleValueType(val.getClass())) {
            traverseBean(val, func);
        }
    }

    protected void traverseMap(Map map, BiConsumer<ValMeta, Object> func) {
        map.forEach((k, v) -> {
            traverse(new ValMeta(map, k.toString()), v, func);
        });
    }

    protected void traverseEnumerable(Object raw, Enumerable e, BiConsumer<ValMeta, Object> func) {
        for (int i = 0; i < e.size(); i++) {
            Object v = e.get(i);
            traverse(new ValMeta(raw, i), v, func);
        }
    }

    protected void traverseBean(Object bean, BiConsumer<ValMeta, Object> func) {
        for (BeanProperty bp : BeanType.of(bean.getClass()).getProperties()) {
            if (!bp.isReadable()) {
                continue;
            }
            Object p = bp.getValue(bean);
            traverse(new ValMeta(bean, bp), p, func);
        }
    }

    /**
     * The meta info of value.
     */
    public static class ValMeta {
        private static final ValMeta ROOT = new ValMeta(null, null, null, null, true);

        private final Object       parent;
        private final String       name;
        private final Integer      index;
        private final boolean      root;
        private final BeanProperty property;

        protected ValMeta(Object parent, String name) {
            this(parent, name, null, null, false);
        }

        protected ValMeta(Object parent, BeanProperty property) {
            this(parent, property.getName(), null, property, false);
        }

        protected ValMeta(Object parent, Integer index) {
            this(parent, null, index, null, false);
        }

        private ValMeta(Object parent, String name, Integer index, BeanProperty property, boolean root) {
            this.parent = parent;
            this.name = name;
            this.index = index;
            this.property = property;
            this.root = root;
        }

        public Object getParent() {
            return parent;
        }

        public String getName() {
            return name;
        }

        public Integer getIndex() {
            return index;
        }

        public BeanProperty getProperty() {
            return property;
        }

        public boolean isRoot() {
            return root;
        }
    }
}

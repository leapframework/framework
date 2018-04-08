/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests.core.validation;

import leap.core.annotation.Inject;
import leap.core.junit.AppTestBase;
import leap.core.validation.BeanValidator;
import leap.core.validation.Valid;
import leap.core.validation.ValidationException;
import leap.core.validation.annotations.Required;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanValidatorTest extends AppTestBase {

    protected @Inject BeanValidator beanValidator;

    @Test
    public void testNestedMap() {
        TParent parent = new TParent();
        parent.setName("Parent");

        TChild child = new TChild();
        child.setName("child");

        parent.childs.put(child.getName(), child);

        try {
            beanValidator.validate(parent);

            fail("Should validation failed");
        }catch(ValidationException e) {
            assertContains(e.getMessage(), "childs['child'].property");
        }
    }

    @Test
    public void testNestedCollection() {
        TParent parent = new TParent();
        parent.setName("Parent");

        TChild child = new TChild();
        child.setName("child");

        parent.children.add(child);

        try {
            beanValidator.validate(parent);

            fail("Should validation failed");
        }catch(ValidationException e) {
            assertContains(e.getMessage(), "children[0].property");
        }
    }

    public static class TParent {
        protected @Required String name;
        protected @Valid(required = false) Map<String, TChild> childs   = new HashMap<>();
        protected @Valid(required = false) List<TChild>        children = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, TChild> getChilds() {
            return childs;
        }

        public List<TChild> getChildren() {
            return children;
        }
    }

    public static class TChild {

        protected @Required String name;
        protected @Required String property;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }
    }
}

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

package tested.base.beans;

import leap.core.annotation.Bean;
import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.config.dyna.*;
import org.omg.CORBA.PUBLIC_MEMBER;

@Bean
@Configurable(prefix="pbean")
public class TConfigBean {

    @ConfigProperty("stringProperty1")
    public String rawStringProperty1;

    @ConfigProperty("stringProperty1")
    protected String rawStringProperty2;

    protected String rawStringProperty3;

    public Property<String>  stringProperty1;
    public Property<Integer> integerProperty1;
    public Property<Boolean> booleanProperty1;
    public Property<Long>    longProperty1;
    public Property<Double>  doubleProperty1;
    public StringProperty    stringProperty2;
    public IntegerProperty   integerProperty2;
    public LongProperty      longProperty2;
    public BooleanProperty   booleanProperty2;
    public DoubleProperty    doubleProperty2;
    public Property<String>  property1;
    public Property<CProp>   complexProperty1;

    public @ConfigProperty CProp    complexProperty2;
    public @ConfigProperty String[] arrayProperty1;

    protected int intPropertyWithDefaultValue;
    protected String strPropertyWithDefaultValue;
    protected boolean boolPropertyWithDefaultValue;
    protected String[] arrayPropertyWithDefaultValue;

    public String testNotFieldProperty1;
    public String testNotFieldProperty2;
    public String testNotFieldProperty3;

    public @Configurable.Nested NestedConfig nested1 = new NestedConfig();
    public @Configurable.Nested NestedConfig                      nested2;
    public @Configurable.Nested(prefix = "nested3") NestedConfig nested;
    public @Configurable.Nested NestedConfig                      nestedConfig;

    @ConfigProperty(key = "not_exists_property", defaultValue = "10")
    public int getIntPropertyWithDefaultValue() {
        return intPropertyWithDefaultValue;
    }

    @ConfigProperty(key = "not_exists_property", defaultValue = "ok")
    public String getStrPropertyWithDefaultValue() {
        return strPropertyWithDefaultValue;
    }

    @ConfigProperty(defaultValue = "true")
    public boolean isBoolPropertyWithDefaultValue() {
        return boolPropertyWithDefaultValue;
    }

    @ConfigProperty(value = "not_exists_property", defaultValue = "a,b")
    public String[] getArrayPropertyWithDefaultValue() {
        return arrayPropertyWithDefaultValue;
    }

    public String getPublicRawStringProperty2() {
        return rawStringProperty2;
    }

    @ConfigProperty("stringProperty1")
    public String getRawStringProperty3() {
        return rawStringProperty3;
    }

    @ConfigProperty("noFieldProperty1")
    public void setNoFieldProperty(String v) {
        this.testNotFieldProperty1 = v;
    }

    @ConfigProperty("noFieldProperty2")
    public void doConfigNoFieldProperty(String v) {
        this.testNotFieldProperty2 = v;
    }

    @ConfigProperty("noFieldProperty3")
    public TConfigBean setConfigNoFieldProperty(String v) {
        this.testNotFieldProperty3 = v;
        return this;
    }

    public static final class CProp {

        public String name;
        public int    value;

    }

    public static final class NestedConfig {

        @ConfigProperty
        public String prop1;

    }
}

/*
 *  Copyright 2020 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package leap.orm.tested;

import leap.lang.beans.DynaProps;
import leap.orm.annotation.Column;
import leap.orm.annotation.EnableDynamic;
import leap.orm.annotation.Entity;
import leap.orm.annotation.Id;

import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@EnableDynamic
public class EmdEntity implements DynaProps {

    @Id
    protected String id;

    @Column
    protected String name;

    @Column(embedded = true)
    protected String c1;

    @Column(embedded = true)
    protected Integer c2;

    protected Map<String, Object> dynaProperties = new LinkedHashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getC1() {
        return c1;
    }

    public void setC1(String c1) {
        this.c1 = c1;
    }

    public Integer getC2() {
        return c2;
    }

    public void setC2(Integer c2) {
        this.c2 = c2;
    }

    public <T> T get(String name) {
        return (T)dynaProperties.get(name);
    }

    @Override
    public Map<String, Object> getDynaProperties() {
        return dynaProperties;
    }

    @Override
    public void setDynaProperties(Map<String, Object> dynaProperties) {
        this.dynaProperties = dynaProperties;
    }
}

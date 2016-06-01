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

package leap.core.config;

public class SimpleAppProperty implements AppProperty {

    private final Object  source;
    private final String  name;
    private final boolean system;

    private String value;
    private String unprocessedValue;

    public SimpleAppProperty(Object source, String name, String value) {
        this(source, name, value, false);
    }

    public SimpleAppProperty(Object source, String name, String value, boolean system) {
        this.source = source;
        this.name   = name;
        this.value  = value;
        this.unprocessedValue = value;
        this.system = system;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void updateResolvedValue(String value) {
        this.value = value;
        this.unprocessedValue = value;
    }

    @Override
    public String getUnprocessedValue() {
        return unprocessedValue;
    }

    @Override
    public void updateProcessedValue(String value) {
        this.value = value;
    }

    @Override
    public boolean isSystem() {
        return system;
    }
}

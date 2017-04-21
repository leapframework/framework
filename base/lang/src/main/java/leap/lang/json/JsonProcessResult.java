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
package leap.lang.json;

public class JsonProcessResult {
    private String key;
    private Object value;

    public JsonProcessResult() {
    }

    public JsonProcessResult(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public JsonProcessResult setKey(String key) {
        this.key = key;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public JsonProcessResult setValue(Object value) {
        this.value = value;
        return this;
    }
}

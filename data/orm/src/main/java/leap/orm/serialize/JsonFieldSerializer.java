/*
 * Copyright 2016 the original author or authors.
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
package leap.orm.serialize;

import leap.lang.Initializable;
import leap.lang.json.JSON;
import leap.lang.json.JsonSettings;
import leap.orm.mapping.FieldMapping;

import java.lang.reflect.Type;

public class JsonFieldSerializer implements FieldSerializer,Initializable {

    //todo : config json serialize settings.
    protected JsonSettings settings;

    @Override
    public void init() {
        settings =
                new JsonSettings.Builder()
                        .ignoreEmpty()
                        .ignoreNull()
                        .build();
    }

    @Override
    public Object trySerialize(FieldMapping fm, Object plain) {
        return JSON.encode(plain,settings);
    }

    @Override
    public Object deserialize(FieldMapping fm, Object encoded, Class<?> type, Type genericType) {
        return JSON.decode((String)encoded, type);
    }

}
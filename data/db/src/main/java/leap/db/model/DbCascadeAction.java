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
package leap.db.model;

import leap.lang.Valued;

public enum DbCascadeAction implements Valued<String> {

	NONE("none"),

	CASCADE("cascade"),
	
	SET_NULL("set_null"),
	
	SET_DEFAULT("set_default"),
	
	RESTRICT("restrict");

    private final String value;

    DbCascadeAction(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
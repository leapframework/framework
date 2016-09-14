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
package leap.orm.command;

import java.util.Map;

/**
 * A command for creating a new record of entity.
 */
public interface InsertCommand {

    /**
     * Returns the passed in or generated id.
     */
    Object id();

    /**
     * Returns the generated id after execution.
     *
     * <p/>
     * Returns null if the entity has no generator of id.
     */
	Object getGeneratedId();

    /**
     * Sets the id of record.
     */
    InsertCommand id(Object id);

    /**
     * Sets the insert field.
     */
	InsertCommand set(String name,Object value);

    /**
     * Sets all the fields in the bean.
     */
	InsertCommand setAll(Object bean);

    /**
     * Sets all the fields in the map.
     */
	InsertCommand setAll(Map<String, Object> map);

    /**
     * Executes insert and returns the affected records.
     */
	int execute();

}
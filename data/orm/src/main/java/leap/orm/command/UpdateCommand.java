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

public interface UpdateCommand extends Executable {

    /**
     * Sets the id of record.
     */
	UpdateCommand withId(Object id);

    /**
     * Sets the fields from the given record.
     *
     * <p/>
     * The record must be a pojo bean, or a {@link Map},
     *
     * or a {@link leap.lang.params.Params}, or a {@link leap.lang.beans.DynaBean}.
     */
    UpdateCommand from(Object record);

    /**
     * Sets the field's value for update.
     */
	UpdateCommand set(String name,Object value);

    /**
     * Sets all the fields for update.
     */
    UpdateCommand setAll(Map<String,Object> fields);

    /**
     * Select valid fields for update.
     */
    UpdateCommand selective();

    /**
     * Executes update and returns the affected rows.
     */
	int execute();

}
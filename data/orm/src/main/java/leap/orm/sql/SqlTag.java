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

package leap.orm.sql;

import java.util.Map;

public interface SqlTag {

    /**
     * Returns the name of tag.
     */
    String getName();

    /**
     * Returns the content of name.
     */
    String getContent();

    /**
     * Returns the vars for eval expression.
     */
    Map<String, Object> getVars();

    /**
     * Sets the vars for eval expression.
     */
    void setVars(Map<String, Object> vars);

    /**
     * Returns the execution object.
     */
    Object getExecutionObject();

    /**
     * Sets the execution object.
     */
    void setExecutionObject(Object o);

}
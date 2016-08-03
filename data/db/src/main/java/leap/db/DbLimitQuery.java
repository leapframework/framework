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
package leap.db;

import java.util.List;

import leap.lang.value.Limit;

public interface DbLimitQuery {

    /**
     *  Returns the original sql text.
     */
	String getSql(Db db);

    /**
     * Returns the original sql text without order by expression.
     */
	String getSqlWithoutOrderBy(Db db);

    /**
     * Returns the limitation info.
     */
	Limit getLimit();

    /**
     * Returns the order by expression.
     */
	String getOrderBy();

    /**
     * Returns the sql args.
     */
	List<Object> getArgs();

}
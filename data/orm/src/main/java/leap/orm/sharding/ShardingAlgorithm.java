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

package leap.orm.sharding;

import leap.orm.mapping.EntityMapping;

public interface ShardingAlgorithm {

    /**
     * Returns true if the given table name is the sharding table of the entity.
     */
    boolean isShardingTable(EntityMapping em, String tableName);

    /**
     * Returns the sharding table name with the given value of sharding column.
     */
    String evalShardingTableName(EntityMapping em, Object value);

}

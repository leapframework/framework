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

package leap.orm.mapping.config;

public class ShardingConfig {

    private final String  entity;
    private final String  shardingField;
    private final boolean autoCreateTable;

    public ShardingConfig(String entity, String shardingField, boolean autoCreateTable) {
        this.entity = entity;
        this.shardingField = shardingField;
        this.autoCreateTable = autoCreateTable;
    }

    /**
     * Returns the name of entity or entity class.
     */
    public String getEntity() {
        return entity;
    }

    public String getShardingField() {
        return shardingField;
    }

    public boolean isAutoCreateTable() {
        return autoCreateTable;
    }
}

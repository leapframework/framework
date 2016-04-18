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

import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.EntityMappingBuilder;
import leap.orm.mapping.config.ShardingConfig;

public class DefaultShardingFactory implements ShardingFactory {

    private static final SimpleShardingAlgorithm DEFAULT_ALGORITHM = new SimpleShardingAlgorithm();

    @Override
    public ShardingAlgorithm getShardingAlgorithm(EntityMappingBuilder em, ShardingConfig config) {
        return DEFAULT_ALGORITHM;
    }

    protected static class SimpleShardingAlgorithm implements ShardingAlgorithm {

        @Override
        public boolean isShardingTable(EntityMapping em, String tableName) {

            String prefix = Strings.endsWith(em.getTableName(), "_") ? em.getTableName() : em.getTableName() + "_";

            return tableName.toLowerCase().startsWith(prefix.toLowerCase());
        }

        @Override
        public String evalShardingTableName(EntityMapping em, Object value) {
            if(null == value){
                return em.getTableName();
            }

            String prefix = Strings.endsWith(em.getTableName(), "_") ? em.getTableName() : em.getTableName() + "_";
            String suffix = Converts.toString(value);

            return prefix + suffix;
        }

    }
}
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

import leap.lang.Classes;
import leap.orm.mapping.*;
import leap.orm.sharding.ShardingFactory;

class ShardingMapper {

    void processShardings(ShardingFactory factory, MappingConfigContext context, MappingConfig config) {

        for(ShardingConfig s : config.getShardings()) {

            processSharding(factory, context, config, s);

        }

    }

    void processSharding(ShardingFactory factory, MappingConfigContext context, MappingConfig config, ShardingConfig s) {

        EntityMappingBuilder em = context.tryGetEntityMapping(s.getEntity());
        if(null == em) {
            Class<?> c = Classes.tryForName(s.getEntity());
            if(null != c) {
                em = context.tryGetEntityMapping(c);
            }
        }

        if(null == em) {
            return;
            //todo : multi-models
            //throw new MappingConfigException("No entity '" + s.getEntity() + "' exists in context '" + context.getName() + "', check sharding config");
        }

        FieldMappingBuilder fm = em.findFieldMappingByName(s.getShardingField());
        if(null == fm) {
            throw new MappingConfigException("No field '" + s.getShardingField() + "' exists in entity '" + s.getEntity() + "', check sharding config");
        }

        em.setSharding(true);
        em.setAutoCreateShardingTable(s.isAutoCreateTable());
        em.setShardingAlgorithm(factory.getShardingAlgorithm(em, s));
        fm.setSharding(true);
    }

}

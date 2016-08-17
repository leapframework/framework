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
package leap.orm.generator;

import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.lang.expression.AbstractExpression;
import leap.lang.expression.Expression;
import leap.lang.util.ShortUUID;
import leap.orm.mapping.EntityMappingBuilder;
import leap.orm.mapping.FieldMappingBuilder;
import leap.orm.metadata.MetadataContext;

import java.util.Map;
import java.util.UUID;

@Configurable(prefix = "orm.short_uuid")
public class ShortUUIDGenerator extends AbstractExpression implements IdGenerator, ValueGenerator,Expression {

    private int defaultColumnLength = 38;

    @Override
    public Integer getDefaultColumnLength() {
        return defaultColumnLength;
    }

    @ConfigProperty
    public void setDefaultColumnLength(int defaultColumnLength) {
        this.defaultColumnLength = defaultColumnLength;
    }

	@Override
    public void mapping(MetadataContext context, EntityMappingBuilder emb, FieldMappingBuilder fmb) {
		fmb.setValueGenerator(this);
    }
	
	@Override
    protected Object eval(Object context, Map<String, Object> vars) {
		return ShortUUID.randomUUID();
    }

}
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
import leap.lang.Initializable;
import leap.lang.expression.AbstractExpression;
import leap.lang.expression.Expression;
import leap.lang.util.ShortID;
import leap.lang.util.ShortUUID;
import leap.orm.mapping.EntityMappingBuilder;
import leap.orm.mapping.FieldMappingBuilder;
import leap.orm.metadata.MetadataContext;

import java.util.Map;
import java.util.Random;

@Configurable(prefix="shortid")
public class ShortIDGenerator extends AbstractExpression implements IdGenerator, ValueGenerator,Expression,Initializable {

    private Random  random;
    private String  alphabet;
    private Long    reduceTime;
    private Integer version;
    private Integer clusterWorkerId;

    private ShortID shortID;

    public void setRandom(Random random) {
        this.random = random;
    }

    @ConfigProperty
    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }

    @ConfigProperty
    public void setReduceTime(Long reduceTime) {
        this.reduceTime = reduceTime;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @ConfigProperty
    public void setClusterWorkerId(Integer clusterWorkerId) {
        this.clusterWorkerId = clusterWorkerId;
    }

    @Override
    public void init() {
        ShortID.Builder builder = new ShortID.Builder();
        builder.setRandom(random);
        builder.setAlphabet(alphabet);
        builder.setReduceTime(reduceTime);
        builder.setVersion(version);
        builder.setClusterWorkerId(clusterWorkerId);
        shortID = builder.build();
    }

    @Override
    public void mapping(MetadataContext context, EntityMappingBuilder emb, FieldMappingBuilder fmb) {
		fmb.setValueGenerator(this);
    }
	
	@Override
    protected Object eval(Object context, Map<String, Object> vars) {
		return shortID.generate();
    }

}
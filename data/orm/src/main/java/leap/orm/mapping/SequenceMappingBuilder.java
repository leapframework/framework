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
package leap.orm.mapping;

import leap.db.model.DbSequenceBuilder;
import leap.lang.Buildable;

public class SequenceMappingBuilder implements Buildable<SequenceMapping>{
	
	protected String            name;
	protected DbSequenceBuilder sequence = new DbSequenceBuilder();
	
	public SequenceMappingBuilder(){
		
	}
	
	public SequenceMappingBuilder(String name){
		this.name = name;
		this.sequence.setName(name);
	}
	
	public String getName() {
		return name;
	}

	public SequenceMappingBuilder setName(String name) {
		this.name = name;
		this.sequence.setName(name);
		return this;
	}
	
	public DbSequenceBuilder getSequence() {
		return sequence;
	}

	public SequenceMappingBuilder setSchema(String schema) {
		this.sequence.setSchema(schema);
		return this;
	}

	public SequenceMappingBuilder setStart(Long start) {
		this.sequence.setStart(start);
		return this;
	}

	public SequenceMappingBuilder setIncrement(Integer increment) {
		this.sequence.setIncrement(increment);
		return this;
	}

	public SequenceMappingBuilder setCache(Integer cache) {
		this.sequence.setCache(cache);
		return this;
	}

	@Override
    public SequenceMapping build() {
	    return new SequenceMapping(name, sequence.build());
    }
}

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
package leap.db.model;

import leap.lang.Buildable;
import leap.lang.json.JsonObject;
import leap.lang.json.JsonParsable;
import leap.lang.json.JsonValue;

public class DbSequenceBuilder implements Buildable<DbSequence>,JsonParsable {
	
	protected String  catalog;
	protected String  schema;
	protected String  name;
	protected Long    minValue;
	protected Long    maxValue;
	protected Integer increment;
	protected Long    start;
	protected Integer cache;
	protected Boolean cycle;
	
	public DbSequenceBuilder(){
		
	}
	
	public DbSequenceBuilder(String name){
		this.name = name;
	}
	
	public String getCatalog() {
		return catalog;
	}

	public DbSequenceBuilder setCatalog(String catalog) {
		this.catalog = catalog;
		return this;
	}

	public String getSchema() {
		return schema;
	}

	public DbSequenceBuilder setSchema(String schema) {
		this.schema = schema;
		return this;
	}

	public String getName() {
		return name;
	}

	public DbSequenceBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public Long getMinValue() {
		return minValue;
	}

	public DbSequenceBuilder setMinValue(Long minValue) {
		this.minValue = minValue;
		return this;
	}

	public Long getMaxValue() {
		return maxValue;
	}

	public DbSequenceBuilder setMaxValue(Long maxValue) {
		this.maxValue = maxValue;
		return this;
	}

	public Integer getIncrement() {
		return increment;
	}

	public DbSequenceBuilder setIncrement(Integer increment) {
		this.increment = increment;
		return this;
	}

	public Long getStart() {
		return start;
	}

	public DbSequenceBuilder setStart(Long start) {
		this.start = start;
		return this;
	}

	public Integer getCache() {
		return cache;
	}

	public DbSequenceBuilder setCache(Integer cache) {
		this.cache = cache;
		return this;
	}

	public Boolean getCycle() {
		return cycle;
	}

	public DbSequenceBuilder setCycle(Boolean cycle) {
		this.cycle = cycle;
		return this;
	}

	@Override
	public DbSequence build() {
		return new DbSequence(catalog, schema, name, minValue, maxValue, increment, start, cache, cycle);
	}

	@Override
    public void parseJson(JsonValue value) {
		JsonObject o = value.asJsonObject();
		this.catalog   = o.getString("catalog");
		this.schema    = o.getString("schema");
		this.name      = o.getString("name");
		this.minValue  = o.getLong("minValue");
		this.maxValue  = o.getLong("maxValue");
		this.increment = o.getInteger("increment");
		this.start     = o.getLong("start");
		this.cache     = o.getInteger("cache");
		this.cycle     = o.getBoolean("cycle");
    }
}

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

import leap.lang.json.JsonStringable;
import leap.lang.json.JsonWriter;

public class DbSequence extends DbSchemaObjectName implements JsonStringable {
	
	protected final Long    minValue;
	protected final Long    maxValue;
	protected final Integer increment;
	protected final Long    start;
	protected final Integer cache;
	protected final Boolean cycle;

	public DbSequence(String catalog, String schema, String name, boolean quoted,
					  Long minValue,Long maxValue, Integer increment, Long start, Integer cache, Boolean cycle) {
	    super(catalog, schema, name, quoted);
	    
	    this.minValue  = minValue;
	    this.maxValue  = maxValue;
	    this.increment = increment;
	    this.start     = start;
	    this.cache     = cache;
	    this.cycle     = cycle;
    }

	/**
	 * Returns the minimum value a sequence can generate.
	 * 
	 * <p>
	 * Returns <code>null</code> means to use the default minValue of underlying db.
	 */
	public Long getMinValue() {
		return minValue;
	}

	/**
	 * Returns the maximum value for the sequence.
	 * 
	 * <p>
	 * Returns <code>null</code> means to use the default maxValue of underlying db.
	 */
	public Long getMaxValue() {
		return maxValue;
	}

	/**
	 * The increment specifies which value is added to the current sequence value to create a new value.
	 * 
	 * <p>
	 * Returns <code>null</code> means to use the default increment of underlying db.
	 */
	public Integer getIncrement() {
		return increment;
	}

	/**
	 * The start allows the sequence to begin anywhere.
	 * 
	 * <p>
	 * Returns <code>null</code> means to use the default start of underlying db.
	 */
	public Long getStart() {
		return start;
	}

	/**
	 * The cache specifies how many sequence numbers are to be preallocated and stored in memory for faster access.
	 * 
	 * <p>
	 * Returns <code>null</code> means to use the default cache of underlying db.
	 */
	public Integer getCache() {
		return cache;
	}

	/**
	 * The cycle allows the sequence to wrap around when the maxvalue or minvalue 
	 * has been reached by an ascending or descending sequence respectively. 
	 * If the limit is reached, the next number generated will be the minvalue or maxvalue, respectively.
	 * 
	 * <p>
	 * Returns <code>null</code> means to use the default cycle of underlying db.
	 */
	public Boolean getCycle() {
		return cycle;
	}

	@Override
    public String toString() {
	    return "Sequence {name=" + name + "}";
    }

	@Override
    public void toJson(JsonWriter writer) {
	    writer.startObject();

	    writeName(writer);

	    writer.propertyOptional("minValue", minValue)
	    	  .propertyOptional("maxValue", maxValue)
	    	  .propertyOptional("increment",increment)
	    	  .propertyOptional("start",start)
	    	  .propertyOptional("cache",cache)
	    	  .propertyOptional("cycle",cycle);
	    
	    writer.endObject();
    }
}

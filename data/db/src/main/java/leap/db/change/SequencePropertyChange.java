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
package leap.db.change;

import leap.db.model.DbSequence;

public class SequencePropertyChange extends PropertyChange<DbSequence> {
	
	public static final String MIN_VALUE = "minValue";
	public static final String MAX_VALUE = "maxValue";
	public static final String INCREMENT = "increment";
	public static final String START     = "start";
	public static final String CACHE     = "cache";
	public static final String CYCLE     = "cycle";

	public SequencePropertyChange(DbSequence object, String property, Object oldValue, Object newValue) {
		super(object, property, oldValue, newValue);
	}

	@Override
	public SchemaObjectType getObjectType() {
		return SchemaObjectType.SEQUENCE;
	}
	
	public boolean isMinValueChanged(){
		return getProperty().equals(MIN_VALUE);
	}
	
	public boolean isMaxValueChanged(){
		return getProperty().equals(MAX_VALUE);
	}
	
	public boolean isIncrementChanged(){
		return getProperty().equals(INCREMENT);
	}
	
	public boolean isStartChanged(){
		return getProperty().equals(START);
	}
	
	public boolean isCacheChanged(){
		return getProperty().equals(CACHE);
	}
	
	public boolean isCycleChanged(){
		return getProperty().equals(CYCLE);
	}
}

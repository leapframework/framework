/*
 * Copyright 2015 the original author or authors.
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
package leap.lang.json;

import leap.lang.naming.NamingStyle;
import leap.lang.naming.NamingStyles;

class JsonWriterCreatorImpl implements JsonWriterCreator {

	private final Appendable out;
	
	protected boolean     keyQuoted = true;
	protected boolean     ignoreNull;
	protected boolean     ignoreFalse;
	protected boolean     ignoreEmptyString;
	protected boolean     ignoreEmptyArray;
	protected boolean	  detectCyclicReferences = true;
	protected boolean	  ignoreCyclicReferences = true;
	protected int		  maxDepth = JsonWriter.MAX_DEPTH;
	protected NamingStyle namingStyle;
	
	public JsonWriterCreatorImpl(Appendable out) {
		this.out = out;
	}
	
	@Override
    public JsonWriterCreator setDetectCyclicReferences(boolean detectCyclicReferences) {
		this.detectCyclicReferences = detectCyclicReferences;
	    return this;
    }
	
	@Override
    public JsonWriterCreator setIgnoreCyclicREferences(boolean ignoreCyclicReferences) {
		this.ignoreCyclicReferences = ignoreCyclicReferences;
	    return this;
    }

	@Override
    public JsonWriterCreator setKeyQuoted(boolean keyQuoted) {
		this.keyQuoted = keyQuoted;
	    return this;
    }

	@Override
	public JsonWriterCreator setIgnoreNull(boolean ignoreNull) {
		this.ignoreNull = ignoreNull;
		return this;
	}

	@Override
	public JsonWriterCreator setIgnoreFalse(boolean ignoreFalse) {
		this.ignoreFalse = ignoreFalse;
		return this;
	}

	@Override
	public JsonWriterCreator setIgnoreEmptyString(boolean ignoreEmptyString) {
		this.ignoreEmptyString = ignoreEmptyString;
		return this;
	}

	@Override
	public JsonWriterCreator setIgnoreEmptyArray(boolean ignoreEmptyArray) {
		this.ignoreEmptyArray = ignoreEmptyArray;
		return this;
	}
	
	@Override
    public JsonWriterCreator setMaxDepth(int depth) {
		this.maxDepth = depth;
	    return this;
    }

	@Override
	public JsonWriterCreator setNamingStyle(NamingStyle namingStyle) {
		this.namingStyle = namingStyle;
		return this;
	}

	@Override
	public JsonWriter create() {
		if(null == namingStyle){
			namingStyle = NamingStyles.RAW;
		}
		
		return new JsonWriterImpl(out, 
								  keyQuoted, 
								  ignoreNull, ignoreFalse, ignoreEmptyString, ignoreEmptyArray, 
								  detectCyclicReferences, ignoreCyclicReferences,
								  maxDepth,
								  namingStyle);
	}
}

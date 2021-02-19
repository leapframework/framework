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

import leap.lang.Creatable;
import leap.lang.beans.BeanProperty;
import leap.lang.naming.NamingStyle;

import java.util.function.Predicate;

public interface JsonWriterCreator extends Creatable<JsonWriter>{

    default JsonWriterCreator maxIgnore() {
        setIgnoreNull(true);
        setIgnoreEmptyString(true);
        setIgnoreEmptyArray(true);
        return setIgnoreFalse(true);
    }
	
	JsonWriterCreator setDetectCyclicReferences(boolean detectCyclicReferences);
	
	JsonWriterCreator setIgnoreCyclicREferences(boolean ignoreCyclicReferences);
	
	JsonWriterCreator setKeyQuoted(boolean keyQuoted);
	
	JsonWriterCreator setIgnoreNull(boolean ignoreNull);
	
	JsonWriterCreator setIgnoreFalse(boolean ignoreFalse);
	
	JsonWriterCreator setIgnoreEmptyString(boolean ignoreEmptyString);

	JsonWriterCreator setHtmlEscape(boolean htmlEscape);
	
	JsonWriterCreator setIgnoreEmptyArray(boolean ignoreEmptyArray);
	
	JsonWriterCreator setMaxDepth(int depth);
	
	JsonWriterCreator setNamingStyle(NamingStyle namingStyle);

    JsonWriterCreator setSettings(JsonSettings settings);

    JsonWriterCreator setBeanFilter(Predicate<Object> filter);

    JsonWriterCreator setPropertyFilter(Predicate<BeanProperty> filter);
}

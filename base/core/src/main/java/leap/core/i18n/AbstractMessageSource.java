/*
 * Copyright 2014 the original author or authors.
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
package leap.core.i18n;

import java.util.Locale;

import leap.lang.exception.ObjectNotFoundException;

public abstract class AbstractMessageSource implements MessageSource {

	@Override
    public String getMessage(String key, Object... args) throws ObjectNotFoundException {
	    return getMessage(null, key, args);
    }

	@Override
    public String getMessage(Locale locale, String key, Object... args) throws ObjectNotFoundException {
    	String m = tryGetMessage(locale, key, args);
    	
    	if(null == m){
    		throw new ObjectNotFoundException("The message key '" + key + "' not found");
    	}
    	
	    return m;
    }
	
	@Override
    public String tryGetMessage(String key, Object... args) {
	    return tryGetMessage(null, key, args);
    }

}
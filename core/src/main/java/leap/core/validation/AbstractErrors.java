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
package leap.core.validation;

import java.util.Locale;

import leap.lang.NamedError;
import leap.lang.Strings;

public abstract class AbstractErrors implements Errors {
	
	protected Locale locale;

	public AbstractErrors() {
	    super();
    }
	
	public AbstractErrors(Locale locale){
		this.locale = locale;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	@Override
    public void addAll(Errors errors, int maxErrors) {
		if(maxErrors > 0){
			for(NamedError e : errors){
				if(errors.maxErrorsReached(maxErrors)){
					return;
				}
				add(e);
			}
		}else{
			addAll(errors);
		}
    }

	@Override
    public void addAll(String objectName, Errors errors, int maxErrors) {
		if(maxErrors > 0){
			if(Strings.isEmpty(objectName)){
				addAll(errors,maxErrors);
			}else{
				for(NamedError e : errors){
					if(errors.maxErrorsReached(maxErrors)){
						return;
					}
					add(new NamedError(objectName + "." + e.getName(), e.getCode(), e.getMessage()));
				}
			}
		}else{
			addAll(objectName,errors);
		}
    }

	@Override
    public boolean maxErrorsReached(int maxErrors) {
		return maxErrors > 0 && size() >= maxErrors;
    }
}
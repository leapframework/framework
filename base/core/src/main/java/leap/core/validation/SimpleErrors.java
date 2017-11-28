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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import leap.lang.NamedError;
import leap.lang.Strings;
import leap.lang.exception.EmptyElementsException;

public class SimpleErrors extends AbstractErrors implements Errors {
	
	protected final List<NamedError> list;
	
	public SimpleErrors(){
		super();
		this.list = new ArrayList<>();
	}

	public SimpleErrors(Locale locale) {
	    super(locale);
	    this.list = new ArrayList<>();
    }
	
	@Override
    public boolean isEmpty() {
	    return list.isEmpty();
    }

	@Override
	public int size() {
		return list.size();
	}

	@Override
    public void clear() {
		list.clear();
    }
	
	@Override
    public String getMessage() {
		NamedError e = firstOrNull();
        if(null == e) {
            return "";
        }else{
            if(Strings.isEmpty(e.getName())) {
                return e.getMessage();
            }else{
                return "Invalid '" + e.getName() + "'. " + e.getMessage();
            }
        }
    }

	@Override
    public NamedError first() throws EmptyElementsException {
		if(list.isEmpty()){
			throw new EmptyElementsException("Cannot get first error from empty errors");
		}
	    return list.get(0);
    }
	
	public NamedError firstOrNull() {
		if(list.isEmpty()){
			return null;
		}
		return list.get(0);
	}

	@Override
	public void add(String objectName, String message) {
		list.add(createError(objectName, message));
	}

	@Override
    public void add(NamedError e) {
	    list.add(e);
    }

	@Override
    public void addAll(Errors errors) {
		if(null != errors){
			for(NamedError error : errors){
				this.list.add(error);
			}
		}
    }

	@Override
    public void addAll(String objectName, Errors errors) {
		if(Strings.isEmpty(objectName)){
			addAll(errors);
		}else if(null != errors){
			for(NamedError error : errors){
				list.add(createError(objectName + "." + error.getName(), error.getCode(), error.getMessage()));
			}
		}
	}

	@Override
    public List<String> getMessages(String objectName) {
		List<String> messages = new ArrayList<String>();
		
		for(NamedError error : list){
			if(Strings.equals(error.getName(), objectName)){
				messages.add(error.getMessage());
			}
		}
		
		return messages;
    }
	
	@Override
    public boolean contains(String objectName) {
		for(NamedError error : list){
			if(Strings.equals(error.getName(), objectName)){
				return true;
			}
		}
		return false;
    }

	@Override
    public Iterator<NamedError> iterator() {
	    return list.iterator();
    }
	
	protected NamedError createError(String name, String code, String message) {
		return new NamedError(name, code, message);
	}
	
	protected NamedError createError(String name, String message) {
		return new NamedError(name, message);
	}

	@Override
    public String toString() {
		return list.toString();
    }
}
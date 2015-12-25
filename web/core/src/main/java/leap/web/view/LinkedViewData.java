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
package leap.web.view;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import leap.core.validation.Validation;
import leap.web.Request;

public class LinkedViewData extends LinkedHashMap<String, Object> implements ViewData {
	
	private static final long serialVersionUID = 1357451409435828227L;

	public static LinkedViewData of(String name,Object value){
		LinkedViewData m = new LinkedViewData(new HashMap<String, Object>(1));
		m.put(name, value);
		return m;
	}
	
	private Object     returnValue;
	private Validation validation;
	
	public LinkedViewData() {
		super();
	}
	
	public LinkedViewData(int initialCapacity) {
		super(initialCapacity);
	}
	
	public LinkedViewData(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public LinkedViewData(Map<? extends String, ? extends Object> m) {
		super(m);
	}
	
	public LinkedViewData(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor, accessOrder);
	}

	@Override
    public Validation validation() {
		if(null == validation){
			validation = Request.current().getValidation();
		}
		return validation;
	}

	@Override
	public Object getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}
}

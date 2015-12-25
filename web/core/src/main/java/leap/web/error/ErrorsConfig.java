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
package leap.web.error;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ErrorsConfig {
	
	private Map<Integer, String> codeViewMappings      = new ConcurrentHashMap<>();
	private Map<Class<?>,String> exceptionViewMappings = new ConcurrentHashMap<>();
	private Map<Class<?>,String> exceptionCodeMappings = new ConcurrentHashMap<>();
	
	public ErrorsConfig addErrorView(Class<?> exceptionClass,String view) {
		exceptionViewMappings.put(exceptionClass, view);
		return this;
	}
	
	public ErrorsConfig addErrorView(int code,String view){
		codeViewMappings.put(code, view);
		return this;
	}
	
	public ErrorsConfig addErrorViews(ErrorsConfig ec) {
		if(null != ec){
			codeViewMappings.putAll(ec.getCodeViewMappings());
			exceptionViewMappings.putAll(ec.getExceptionViewMappings());
			exceptionCodeMappings.putAll(ec.getExceptionCodeMappings());
		}
		return this;
	}
	
	public ErrorsConfig addErrorCode(Class<?> exceptionClass,String code) {
		exceptionCodeMappings.put(exceptionClass, code);
		return this;
	}
	
	public Map<Integer, String> getCodeViewMappings() {
		return codeViewMappings;
	}

	public Map<Class<?>, String> getExceptionViewMappings() {
		return exceptionViewMappings;
	}
	
	public Map<Class<?>, String> getExceptionCodeMappings() {
		return exceptionCodeMappings;
	}
}
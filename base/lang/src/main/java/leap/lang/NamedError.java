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
package leap.lang;


public class NamedError extends leap.lang.Error implements Named{

	private static final long serialVersionUID = -5015086273697326467L;
	
	protected final String name;

	public NamedError(String name, String message) {
		this(name,null,message);
	}

	public NamedError(String name, String code, String message) {
	    super(code, message, null);
	    Args.notEmpty(name,"object name");
	    this.name = name;
    }
	
	public NamedError(String name, String message, Throwable cause) {
	    super(null, message, cause);
	    this.name = name;
    }
	
	public NamedError(String name, String code, String message, Throwable cause) {
	    super(code, message, cause);
	    this.name = name;
    }

	/**
	 * Returns the object name.
	 */
	public String getName() {
		return name;
	}

	@Override
    public String toString() {
		return "Error[name=" + name + ",code=" + code + ",message=" + message + "]";
    }
}
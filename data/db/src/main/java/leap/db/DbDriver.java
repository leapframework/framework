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
package leap.db;

import java.util.Map;

import leap.core.meta.MSimpleParameter;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.meta.MNamedWithDesc;

public interface DbDriver extends MNamedWithDesc {
	
	//Standard parameters

	String FILE_PARAMETER 	  = "file";
	String HOST_PARAMETER 	  = "host";
	String PORT_PARAMETER 	  = "port";
	String USERNAME_PARAMETER = "username";
	String PASSWROD_PARAMETER = "password";
	String DATABASE_PARAMETER = "database";
	
	public enum Type {
		FILE,
		MEM,
		SERVER
	}
	
	/**
	 * Returns the drvier type.
	 */
	Type getType();
	
	/**
	 * Returns the file extension if this driver is file type and the file must use an extension.
	 * 
	 * <p>
	 * Returns <code>null</code> if this driver is not file type or don't need an file extension.
	 */
	String getFileExtension();
	
	/**
	 * Returns the jdbc driver class name.
	 */
	String getDriverClassName();
	
	/**
	 * Returns an array contains all the meta parameters for creating jdbc url.
	 */
	MSimpleParameter[] getUrlParameters();
	
	/**
	 * Return the url parameter match the given name.
	 */
	MSimpleParameter getUrlParameter(String name) throws ObjectNotFoundException;
	
	/**
	 * Returns the jdbc url.
	 */
	String getUrl(Map<String, Object> params);
	
	/**
	 * Returns the username from the url parameters.
	 */
	String getUsername(Map<String, Object> params);
	
	/**
	 * Returns the password from the url parameters;
	 */
	String getPassword(Map<String, Object> params);
	
	/**
	 * Returns <code>true</code> if this driver is available.
	 */
	boolean isAvailable();
	
	/**
	 * Returns <code>true</code> if the type of this driver is {@link Type#FILE}.
	 */
	boolean isFile();
	
	/**
	 * Returns <code>true</code> if the type of this driver is {@link Type#MEM}.
	 */
	boolean isMemory();
}
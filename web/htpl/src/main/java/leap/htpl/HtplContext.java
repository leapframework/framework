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
package leap.htpl;

import java.util.Locale;
import java.util.Map;

import leap.core.i18n.MessageSource;
import leap.core.validation.Errors;
import leap.core.web.RequestBase;
import leap.web.assets.AssetSource;
import leap.lang.expression.Expression;

public interface HtplContext {
	
	HtplEngine getEngine();
	
	RequestBase getRequest();
	
	String getContextPath();
	
	Locale getLocale();
	
	Errors getErrors();
	
	MessageSource getMessageSource();
	
	AssetSource getAssetSource();
	
	Map<String, Object> getVariables();
	
	void setVariable(String name,Object value);
	
	void putVariables(Map<String, ?> variables);
	
	Object removeVariable(String name);
	
	Map<String, Object> getLocalVariables();
	
	void setLocalVariable(String name,Object value);
	
	void putLocalVariables(Map<String, ?> variables);
	
	/**
	 * Push a empty variables map to the stack.
	 */
	Map<String, Object> pushLocalVariables();
	
	/**
	 * Adds local variables map to the stack.
	 */
	Map<String,Object> pushLocalVariables(Map<String, Object> variables);

	/**
	 * Pops the last local variables map from stack.
	 * 
	 * @throws IllegalStateException if the local variables stack is empty.
	 */
	void popLocalVariables() throws IllegalStateException;
	
	Object eval(Expression expr);
	
	String evalString(Expression expr);
	
	boolean evalBoolean(Expression expr);
	
	boolean isDebug();
	
	boolean isRenderLayout();
}
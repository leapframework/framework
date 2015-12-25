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

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import leap.core.el.EL;
import leap.core.validation.Errors;
import leap.core.validation.SimpleErrors;
import leap.lang.convert.Converts;
import leap.lang.expression.Expression;

public abstract class AbstractHtplContext implements HtplContext {

	protected HtplEngine	            engine;
	protected Errors	                errors;
	protected boolean					renderLayout        = true;
	protected Map<String, Object>	    variables	        = new HashMap<String, Object>();
	private Stack<Map<String, Object>>	localVariablesStack	= new Stack<Map<String, Object>>();

	public AbstractHtplContext(HtplEngine engine) {
		this.engine = engine;
	}

	@Override
	public HtplEngine getEngine() {
		return engine;
	}

	public Errors getErrors() {
		if (null == errors) {
			errors = new SimpleErrors(getLocale());
		}
		return errors;
	}

	public void setErrors(Errors errors) {
		this.errors = errors;
	}

	@Override
	public Map<String, Object> getVariables() {
		return variables;
	}

	@Override
	public void setVariable(String name, Object value) {
		variables.put(name, value);

		if (!localVariablesStack.isEmpty()) {
			for (Map<String, Object> vars : localVariablesStack) {
				vars.put(name, value);
			}
		}
	}

	@Override
	public void putVariables(Map<String, ?> variables) {
		if (null != variables) {
			this.variables.putAll(variables);

			if (!localVariablesStack.isEmpty()) {
				for (Map<String, Object> vars : localVariablesStack) {
					vars.putAll(variables);
				}
			}
		}
	}

	@Override
	public void setLocalVariable(String name, Object value) {
		if (!localVariablesStack.isEmpty()) {
			localVariablesStack.peek().put(name, value);
		} else {
			variables.put(name, value);
		}
	}

	@Override
	public void putLocalVariables(Map<String, ?> variables) {
		if (null != variables) {
			if (!localVariablesStack.isEmpty()) {
				localVariablesStack.peek().putAll(variables);
			} else {
				this.variables.putAll(variables);
			}
		}
	}

	@Override
	public Object removeVariable(String name) {
		Object o = variables.remove(name);

		if (!localVariablesStack.isEmpty()) {
			for (Map<String, Object> vars : localVariablesStack) {
				vars.remove(name);
			}
		}

		return o;
	}

	@Override
	public Map<String, Object> getLocalVariables() {
		if (!localVariablesStack.isEmpty()) {
			return localVariablesStack.peek();
		} else {
			return variables;
		}
	}

	@Override
	public Map<String, Object> pushLocalVariables() {
		Map<String, Object> localVaraibles = createLocalVariables();
		localVariablesStack.add(localVaraibles);
		return localVaraibles;
	}

	@Override
	public Map<String, Object> pushLocalVariables(Map<String, Object> variables) {
		Map<String, Object> localVariables = createLocalVariables(variables);
		localVariablesStack.add(localVariables);
		return localVariables;
	}

	@Override
	public void popLocalVariables() throws IllegalStateException {
		if (localVariablesStack.isEmpty()) {
			throw new IllegalStateException("Empty local variables stack, cannot pop the last local variables map");
		}
		localVariablesStack.pop();
	}

	@Override
	public Object eval(Expression expr) {
		return expr.getValue(this, getLocalVariables());
	}

	@Override
	public String evalString(Expression expr) {
		return Converts.toString(eval(expr));
	}

	@Override
	public boolean evalBoolean(Expression expr) {
		return EL.test(eval(expr));
	}
	
	public boolean isRenderLayout() {
		return renderLayout;
	}

	public void setRenderLayout(boolean renderLayout) {
		this.renderLayout = renderLayout;
	}

	private Map<String, Object> createLocalVariables(Map<String, Object> localVariables) {
		Map<String, Object> map = createLocalVariables();

		if (null != localVariables) {
			map.putAll(localVariables);
		}

		return map;
	}

	private Map<String, Object> createLocalVariables() {
		Map<String, Object> map = new HashMap<String, Object>();

		if (!localVariablesStack.isEmpty()) {
			map.putAll(localVariablesStack.peek());
		} else {
			map.putAll(variables);
		}

		return map;
	}

}
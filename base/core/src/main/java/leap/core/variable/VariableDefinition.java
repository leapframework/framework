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
package leap.core.variable;

import leap.core.variable.Variable.Scope;
import leap.lang.Args;

public class VariableDefinition {

    private final Object   source;
    private final String   name;
    private final Scope    scope;
    private final Variable variable;

    private Object       singletonValue;
    private ScopeSupport scopeSupport;

    public VariableDefinition(String name, Variable variable) {
        this("unspecified", name, Scope.PROTOTYPE, variable);
    }

    public VariableDefinition(String name, Scope scope, Variable variable) {
        this("unspecified", name, scope, variable);
    }

    public VariableDefinition(Object source, String name, Scope scope, Variable variable) {
        Args.notNull(name, "name");
        Args.notNull(scope, "scope");
        Args.notNull(variable, "variable");
        this.source = source;
        this.name = name;
        this.scope = scope;
        this.variable = variable;
    }

    public String getName() {
        return name;
    }

    public Scope getScope() {
        return scope;
    }

    public Object getSource() {
        return source;
    }

    public Variable getVariable() {
        return variable;
    }

    public Object getSingletonValue() {
        return singletonValue;
    }

    public void setSingletonValue(Object singletonValue) {
        this.singletonValue = singletonValue;
    }

	public ScopeSupport getScopeSupport() {
		return scopeSupport;
	}

	public void setScopeSupport(ScopeSupport scopeSupport) {
		this.scopeSupport = scopeSupport;
	}
}

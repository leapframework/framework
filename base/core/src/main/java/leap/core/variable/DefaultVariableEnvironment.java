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

import leap.core.BeanFactory;
import leap.core.RequestContext;
import leap.core.annotation.Inject;
import leap.core.ioc.BeanDefinition;
import leap.core.ioc.PostCreateBean;
import leap.core.security.SecurityContext;
import leap.core.variable.Variable.Scope;
import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Beans;
import leap.lang.Strings;
import leap.lang.accessor.AttributeAccessor;
import leap.lang.accessor.PropertyGetter;
import leap.lang.convert.Converts;
import leap.lang.el.ElEvalContext;
import leap.lang.value.Null;

import javax.print.attribute.standard.OrientationRequested;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class DefaultVariableEnvironment implements VariableEnvironment, PostCreateBean {

    private static final String VARIABLE_SCOPE_ATTRIBUTE = DefaultVariableEnvironment.class.getName() + "$VariableScope";

    @Inject
    protected VariableLookup[] lookups;

    protected Map<String, VariableDefinition> variables     = new HashMap<>();
    protected Map<String, ScopeSupport>       scopeSupports = Collections.emptyMap();
    protected Scope                           defaultScope  = Scope.PROTOTYPE;

    public void setDefaultScope(Scope defaultScope) {
        Args.notNull(defaultScope, "default scope");
        this.defaultScope = defaultScope;
    }

    @Override
    public boolean checkVariableExists(String variable) {
        return variables.containsKey(Strings.lowerCase(variable));
    }

    @Override
    public Object resolveVariable(String variable, ElEvalContext context) {
        String key = Strings.lowerCase(variable);

        VariableDefinition vd = null;
        if (lookups.length > 0) {
            for (VariableLookup lookup : lookups) {
                vd = lookup.lookupVariable(key);
                if (null != vd) {
                    break;
                }
            }
        }
        if (null == vd) {
            vd = variables.get(key);
        }
        if (null == vd) {
            return resolveVariableProperty(key, variable);
        } else {
            return resolveVariable(vd, context);
        }
    }

    @Override
    public Object resolveVariable(String variable) {
        return resolveVariable(variable, null);
    }

    @Override
    public String resolveVariableAsString(String variable) {
        return Converts.toString(resolveVariable(variable));
    }

    protected Object resolveVariableProperty(String key, String variable) {
        int dotIndex = key.indexOf('.');

        if (dotIndex == Arrays2.INDEX_NOT_FOUND) {
            return null;
        }

        String             prefix = key.substring(0, dotIndex);
        VariableDefinition vd     = variables.get(prefix);

        if (null == vd) {
            return null;
        } else {
            Object value = resolveVariable(vd, null);
            if (null == value) {
                return null;
            } else if (value instanceof PropertyGetter) {
                return ((PropertyGetter) value).getProperty(variable.substring(dotIndex + 1));
            } else {
                return Beans.getNestableProperty(value, variable.substring(dotIndex + 1));
            }
        }
    }

    protected Object resolveVariable(VariableDefinition vd, ElEvalContext context) {
        return getScopeSupport(vd).getValue(vd.getName(), () -> getValue(vd.getVariable(), context));
    }

    protected ScopeSupport getScopeSupport(VariableDefinition vd) {
        ScopeSupport ss = vd.getScopeSupport();
        if (null != ss) {
            return ss;
        }

        final Scope scope = vd.getScope();
        if (null == scope || scope.isPrototype()) {
            ss = (name, var) -> var.get();
        } else if (scope.isSingleton()) {
            ss = (name, var) -> {
                Object singletonValue = vd.getSingletonValue();

                if (null != singletonValue) {
                    return Null.is(singletonValue) ? null : singletonValue;
                }

                singletonValue = var.get();

                if (null == singletonValue) {
                    vd.setSingletonValue(Null.VALUE);
                } else {
                    vd.setSingletonValue(singletonValue);
                }

                return singletonValue;
            };
        } else {
            ss = scopeSupports.get(scope.getValue());
            if (null == ss) {
                if (scope.isRequest()) {
                    ss = (name, var) -> {
                        return resolveScopedVariable(name, var, getVariablesScope(RequestContext.tryGetCurrent()));
                    };
                } else if (scope.isSession()) {
                    ss = (name, var) -> {
                        return resolveScopedVariable(name, var, getVariablesScope(getSessionScopeAccessor()));
                    };
                } else if (scope.isAuthentication()) {
                    ss = (name, var) -> {
                        return resolveScopedVariable(name, var, getVariablesScope(getAuthenticationScopeAccessor()));
                    };
                } else {
                    throw new IllegalStateException("Variable scope '" + scope + "' not supported");
                }
            }
        }
        return ss;
    }

    protected Object resolveScopedVariable(String name, Supplier<Object> var, Map<String, Object> scope) {
        if(null == scope){
            return var.get();
        }

        Object value = scope.get(name);

        if (null != value) {
            return Null.is(value) ? null : value;
        }

        value = var.get();

        if (null == value) {
            scope.put(name, Null.VALUE);
        } else {
            scope.put(name, value);
        }

        return value;
    }

    protected Map<String, Object> getVariablesScope(AttributeAccessor accessor) {
        if(null == accessor) {
            return null;
        }
        Map<String, Object> scope = (Map<String, Object>) accessor.getAttribute(VARIABLE_SCOPE_ATTRIBUTE);
        if (null == scope) {
            scope = new ConcurrentHashMap<>();
            accessor.setAttribute(VARIABLE_SCOPE_ATTRIBUTE, scope);
        }
        return scope;
    }

    protected AttributeAccessor getAuthenticationScopeAccessor() {
        RequestContext requestContext = RequestContext.tryGetCurrent();
        return null == requestContext ? null : requestContext.getAuthentication();
    }

    protected AttributeAccessor getSessionScopeAccessor() {
        RequestContext requestContext = RequestContext.tryGetCurrent();
        return null == requestContext ? null : requestContext.getSession();
    }

    @Override
    public void postCreate(BeanFactory beanFactory) throws Exception {
        scopeSupports = beanFactory.getNamedBeans(ScopeSupport.class);
        loadVariableFromBeans(beanFactory);
        loadVariableFromProviders(beanFactory);
    }

    protected void loadVariableFromBeans(BeanFactory beanFactory) {
        Map<Variable, BeanDefinition> beans = beanFactory.getBeansWithDefinition(Variable.class);

        beans.putAll(beanFactory.getBeansWithDefinition(VariableWithContext.class));

        for (Entry<Variable, BeanDefinition> entry : beans.entrySet()) {
            BeanDefinition bd = entry.getValue();

            String name = bd.getName();
            if (!Strings.isEmpty(name)) {
                Variable variable = entry.getKey();
                Scope    scope    = null;

                if (variable instanceof ScopedVariable) {
                    scope = ((ScopedVariable) variable).getScope();
                }

                if (null == scope) {
                    scope = defaultScope;
                }

                variables.put(name.toLowerCase(), new VariableDefinition(bd.getSource(), name, scope, variable));
            }
        }
    }

    protected void loadVariableFromProviders(BeanFactory beanFactory) {
        for (Entry<VariableProvider, BeanDefinition> providerEntry : beanFactory.getBeansWithDefinition(VariableProvider.class).entrySet()) {

            VariableProvider provider = providerEntry.getKey();
            Object           source   = providerEntry.getValue().getSource();

            List<VariableDefinition> providedVariables = provider.getVariables();

            for (VariableDefinition providerVd : providedVariables) {
                String             key = providerVd.getName().toLowerCase();
                VariableDefinition vd  = variables.get(key);

                if (null != vd) {
                    throw new VariableConfigException("Found duplicated variable '" + vd.getName() + "', check source [" + vd.getScope() + "," + source + "]");
                }

                variables.put(key, providerVd);
            }
        }
    }

    private Object getValue(Variable v, ElEvalContext context) {
        if (v instanceof VariableWithContext) {
            return ((VariableWithContext) v).getValue(context);
        } else {
            return v.getValue();
        }
    }

}
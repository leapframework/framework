/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.spring.boot.spel;

import leap.core.annotation.Inject;
import leap.core.spring.ExpressionFactory;
import leap.core.variable.VariableEnvironment;
import leap.lang.annotation.Init;
import leap.lang.expression.Expression;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.*;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

public class SpringExpressionFactory implements ExpressionFactory<org.springframework.expression.Expression> {

    private static final Log log = LogFactory.get(SpringExpressionFactory.class);

    @Inject
    protected VariableEnvironment env;

    @Inject
    protected BeanFactory beanFactory;

    protected EnvPropertyAccessor       envProperty;
    protected SpelExpressionParser      spelParser;
    protected StandardEvaluationContext evalContext;

    @Init
    public void init() {
        if (null == beanFactory) {
            log.error("Spring context not found, can't use spring expression factory");
            return;
        }

        SpelParserConfiguration c = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, this.getClass().getClassLoader());
        spelParser = new SpelExpressionParser(c);

        evalContext = new StandardEvaluationContext();
        envProperty = new EnvPropertyAccessor(env);

        evalContext.getPropertyAccessors().add(0, envProperty);
        evalContext.getPropertyAccessors().add(0, new MapPropertyAccessor());
        evalContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
    }

    public Expression createExpression(String expr) {
        return new SpringExpression(createSpringExpression(expr), evalContext, envProperty);
    }

    public org.springframework.expression.Expression createSpringExpression(String expr) {
        boolean template;

        int index = expr.indexOf("#{");
        if (index < 0) {
            template = false;
        } else if (index > 0) {
            template = true;
        } else if (index == 0 && expr.endsWith("}") && expr.indexOf("#{", 2) < 0) {
            template = false;
            expr = expr.substring(2, expr.length() - 1);
        } else {
            template = true;
        }

        return template ?
                spelParser.parseExpression(expr, ParserContext.TEMPLATE_EXPRESSION) :
                spelParser.parseExpression(expr);
    }

    protected static class EnvPropertyAccessor implements PropertyAccessor {
        private static final Class<?>[] TARGET_CLASSES = new Class<?>[]{EnvPropertyAccessor.class};

        private final VariableEnvironment env;

        public EnvPropertyAccessor(VariableEnvironment env) {
            this.env = env;
        }

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return TARGET_CLASSES;
        }

        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
            return this == target;
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
            Object value = env.resolveVariable(name);
            if (null == value && !env.checkVariableExists(name)) {
                throw new AccessException("Environment variable 'env." + name + "' not exists!");
            }
            return null == value ? TypedValue.NULL : new TypedValue(value);
        }

        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
            return false;
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
            throw new AccessException("Can't write env variable");
        }
    }

    protected static class MapPropertyAccessor implements PropertyAccessor {

        private static final Class<?>[] TARGET_CLASSES = new Class<?>[]{Map.class};

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return TARGET_CLASSES;
        }

        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
            return target instanceof Map;
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
            Map map = (Map) target;

            Object value;
            if (map.containsKey(name)) {
                value = map.get(name);
            } else {
                value = getIgnoreCase(map, name);
            }
            return null == value ? TypedValue.NULL : new TypedValue(value);
        }

        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
            return target instanceof Map;
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
            ((Map) target).put(name, newValue);
        }

        protected Object getIgnoreCase(Map<?, ?> map, String name) throws AccessException {
            for (Map.Entry entry : map.entrySet()) {
                Object key = entry.getKey();
                if (key instanceof String && name.equalsIgnoreCase((String) key)) {
                    return entry.getValue();
                }
            }
            throw new AccessException("The variable '" + name + "' not found");
        }
    }
}

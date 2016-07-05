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
package leap.core.el;

import java.util.Map.Entry;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.PostCreateBean;
import leap.lang.el.DefaultElParseContext;
import leap.lang.el.ElFunction;
import leap.lang.el.ElParseContext;
import leap.lang.el.ParentChildElParseContext;
import leap.lang.el.spel.SPEL;
import leap.lang.expression.Expression;

public class DefaultExpressionLanguage implements ExpressionLanguage, PostCreateBean {
	
    protected @Inject @M ElConfig              config;
    protected @Inject @M DefaultElParseContext parseContext;
	
	@Override
	public Expression createExpression(String expression) {
		return SPEL.createExpression(parseContext, expression);
	}
	
	@Override
    public Expression createExpression(ElConfig config, String expression) {
		ElParseContext c = createParseContext(config);
	    return SPEL.createExpression(new ParentChildElParseContext(this.parseContext, c), expression);
    }

	@Override
    public Expression createExpression(Object parseContext, String expression) {
		if(null == parseContext){
			return createExpression(expression);
		}
		
		if(!(parseContext instanceof ElParseContext)) {
			throw new IllegalStateException("Invalid parse context: " + parseContext.getClass());
		}
		
		return SPEL.createExpression(new ParentChildElParseContext(this.parseContext,this.parseContext),expression);
	}

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		this.parseContext = createParseContext(config);
	}
	
	protected DefaultElParseContext createParseContext(ElConfig config) {
		DefaultElParseContext parseContext = new DefaultElParseContext();
		
		parseContext.importPackages(config.getImportedPackages());
		
		for(Entry<String, Object> v : config.getRegisteredVariables().entrySet()){
			parseContext.setVariable(v.getKey(), v.getValue());
		}
		
		for(Entry<String, ElFunction> f : config.getRegisteredFunctions().entrySet()){
			parseContext.setFunction(f.getKey(), f.getValue());
		}
		
		return parseContext;
	}
}
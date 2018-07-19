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

import leap.core.AppContext;
import leap.lang.Objects2;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.el.ElException;
import leap.lang.el.ElParseContext;
import leap.lang.expression.CompositeExpression;
import leap.lang.expression.Expression;
import leap.lang.expression.ExpressionException;
import leap.lang.expression.ValuedExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EL {

	public static final String PREFIX = "${";
	public static final String SUFFIX = "}";
	
	/**
	 * Returns the default {@link ExpressionLanguage} bean in current app.
	 */
	public static ExpressionLanguage getAppDefaultExpressionLanguage() {
		return AppContext.factory().getBean(ExpressionLanguage.class);
	}
	
	/**
	 * Removes the prefix '${' and the suffix '}" in the given string.
	 */
	public static String removePrefixAndSuffix(String s){
		if(null != s && s.startsWith(PREFIX)){
			if(!s.endsWith(SUFFIX)) {
				throw new IllegalArgumentException("Unclosed expression '" + s + "', must be ends with '}'");
			}
			return s.substring(2,s.length() - 1);
		}
		return s;
	}
	
	/**
	 * Returns <code>true</code> if the given string prefix with {@link #PREFIX} and suffix with {@link #SUFFIX}.
	 */
	public static boolean hasPrefixAndSuffix(String s) {
		if(null == s){
			return false;
		}
		s = s.trim();
		return s.startsWith(PREFIX) && s.endsWith(SUFFIX);
	}
	
	/**
	 * Creates an {@link Expression} use default {@link ExpressionLanguage}.
	 * 
	 * <p>
	 * The prefix and suffix in the given expression text will be removed if exists. 
	 * 
	 * @see #getAppDefaultExpressionLanguage().
	 */
	public static Expression createExpression(String expression){
		return createExpression(getAppDefaultExpressionLanguage(), expression);
	}
	
	/**
	 * Creates an {@link Expression} use the given {@link ExpressionLanguage}.
	 * 
	 * <p>
	 * The prefix and suffix in the given expression text will be removed if exists. 
	 */
	public static Expression createExpression(ExpressionLanguage el,String expression){
		return el.createExpression(removePrefixAndSuffix(expression));
	}
	
	/**
	 * Creates an {@link ValuedExpression} if the given string without prefix '${' and suffix '}',
	 * Creates an {@link Expression} use the default {@link ExpressionLanguage} otherwise.
	 * 
	 * @see #getAppDefaultExpressionLanguage()
	 */
	public static Expression createValueExpression(String s){
		return createValueExpression(getAppDefaultExpressionLanguage(), s);
	}
	
	/**
	 * Creates an {@link ValuedExpression} if the given string without prefix '${' and suffix '}',
	 * Creates an {@link Expression} use the default {@link ExpressionLanguage} otherwise.
	 * 
	 * @see #getAppDefaultExpressionLanguage()
	 */
	public static Expression createValueExpression(String s,Class<?> valueType){
		return createValueExpression(getAppDefaultExpressionLanguage(), s, valueType);
	}
	
	/**
	 * Creates an {@link ValuedExpression} if the given string without prefix '${' and suffix '}',
	 * Creates an {@link Expression} use the given {@link ExpressionLanguage} otherwise.
	 */
	public static Expression createValueExpression(ExpressionLanguage el, String s) {
		if(hasPrefixAndSuffix(s)){
			return el.createExpression(removePrefixAndSuffix(s));
		}else{
			return new ValuedExpression<String>(s);
		}
	}
	
	/**
	 * Creates an {@link ValuedExpression} if the given string without prefix '${' and suffix '}',
	 * Creates an {@link Expression} use the given {@link ExpressionLanguage} otherwise.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static Expression createValueExpression(ExpressionLanguage el, String s, Class<?> valueType) {
		if(hasPrefixAndSuffix(s)){
			return el.createExpression(removePrefixAndSuffix(s));
		}else{
			return new ValuedExpression(Converts.convert(s, valueType));
		}
	}
	
	/**
	 * Creates an {@link ValuedExpression} if the given string without prefix '${' and suffix '}',
	 * Creates an {@link Expression} use the default {@link ExpressionLanguage} otherwise.
	 * 
	 * <p>
	 * Returns <code>null</code> if the given string is <code>null</code> or empty.
	 * 
	 * @see #getAppDefaultExpressionLanguage()
	 */
	public static Expression tryCreateValueExpression(String s){
		return tryCreateValueExpression(getAppDefaultExpressionLanguage(), s);
	}
	
	/**
	 * Creates an {@link ValuedExpression} if the given string without prefix '${' and suffix '}',
	 * Creates an {@link Expression} use the default {@link ExpressionLanguage} otherwise.
	 * 
	 * <p>
	 * Returns <code>null</code> if the given string is <code>null</code> or empty.
	 * 
	 * @see #getAppDefaultExpressionLanguage()
	 */
	public static Expression tryCreateValueExpression(String s,Class<?> valueType){
		return tryCreateValueExpression(getAppDefaultExpressionLanguage(), s, valueType);
	}
	
	/**
	 * Creates an {@link ValuedExpression} if the given string without prefix '${' and suffix '}',
	 * Creates an {@link Expression} use the given {@link ExpressionLanguage} otherwise.
	 * 
	 * <p>
	 * Returns <code>null</code> if the given string is <code>null</code> or empty.
	 */
	public static Expression tryCreateValueExpression(ExpressionLanguage el, String s) {
		if(Strings.isEmpty(Strings.trim(s))){
			return null;
		}
		
		if(hasPrefixAndSuffix(s)){
			return el.createExpression(removePrefixAndSuffix(s));
		}else{
			return new ValuedExpression<String>(s);
		}
	}
	
	/**
	 * Creates an {@link ValuedExpression} if the given string without prefix '${' and suffix '}',
	 * Creates an {@link Expression} use the given {@link ExpressionLanguage} otherwise.
	 * 
	 * <p>
	 * Returns <code>null</code> if the given string is <code>null</code> or empty.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static Expression tryCreateValueExpression(ExpressionLanguage el, String s, Class<?> valueType) {
		if(Strings.isEmpty(Strings.trim(s))){
			return null;
		}
		
		if(hasPrefixAndSuffix(s)){
			return el.createExpression(removePrefixAndSuffix(s));
		}else{
			return new ValuedExpression(Converts.convert(s, valueType));
		}
	}
	
	/**
	 * Evaluates a expression and returns the value.
	 */
	public static Object eval(String expression) throws ElException {
		return createExpression(expression).getValue();
	}
	
	/**
	 * Evaluates a expression and returns the value.
	 */
	public static Object eval(String expression,Map<String, Object> vars) throws ElException {
		return createExpression(expression).getValue(null,vars);
	}
	
	/**
	 * Evaluates a expression and returns the value.
	 */
	public static Object eval(String expression,Object root) throws ElException {
		return createExpression(expression).getValue(root);
	}
	
	/**
	 * Evaluates a expression and returns the value.
	 */
	public static Object eval(String expression,Object root,Map<String, Object> vars) throws ElException {
		return createExpression(expression).getValue(root,vars);
	}
	
	/**
	 * Tests the value of the given expression is <code>true</code> or <code>false</code>
	 */
	public static boolean test(Expression expression) {
		return test(expression.getValue());
	}
	
	/**
	 * Tests the value of the given expression is <code>true</code> or <code>false</code>
	 */
	public static boolean test(Expression expression,Object root) {
		return test(expression.getValue(root));
	}
	
	/**
	 * Tests the value of the given expression is <code>true</code> or <code>false</code>
	 */
	public static boolean test(Expression expression,Object root,Map<String, Object> vars) {
		return test(expression.getValue(root,vars));
	}
	
	/**
	 * Tests the given value is <code>true</code> or <code>false</code>
	 */
	public static boolean test(Object v){
        return test(v, false);
	}

    /**
     * Tests the given value is <code>true</code> or <code>false</code>
     */
    public static boolean test(Object v, boolean convertStringToBoolean){
        if(null == v){
            return false;
        }

        if(v instanceof Boolean){
            return (Boolean)v;
        }

        if(v instanceof Number){
            Number n = (Number)v;
            if(n.floatValue() == 0.0f || n.intValue() == 0){
                return false;
            }
            return true;
        }

        if(convertStringToBoolean && v instanceof String) {
            return Converts.toBoolean((String)v, false);
        }

        return !Objects2.isEmpty(v);
    }

	/**
	 * Parses a composite expression to an {@link Expression} object using the default {@link ElParseContext}.
	 *
	 * <p>
	 *
	 * For example, the composite expression “${firstName} ${lastName}”
	 * is composed of three EL expressions: eval-expression “${firstName}”,
	 * literal- expression “ “, and eval-expression “${lastName}”.
	 *
	 * <p>
	 * Once evaluated, the resulting String is then coerced to the expected type.
	 */
	public static CompositeExpression createCompositeExpression(String s) {
		return createCompositeExpression(getAppDefaultExpressionLanguage(), s);
	}

    /**
     * Parses a composite expression to an {@link Expression} object using the default {@link ElParseContext}.
     */
	public static CompositeExpression createCompositeExpression(ExpressionLanguage language, String str){
        return createCompositeExpression(language, str, PREFIX, SUFFIX);
	}

    /**
     * Parses a composite expression to an {@link Expression} object using the default {@link ElParseContext}.
     *
     * <p>
     *
     * For example, the composite expression “${firstName} ${lastName}”
     * is composed of three EL expressions: eval-expression “${firstName}”,
     * literal- expression “ “, and eval-expression “${lastName}”.
     *
     * <p>
     * Once evaluated, the resulting String is then coerced to the expected type.
     */
	public static CompositeExpression createCompositeExpression(ExpressionLanguage language, String str, String prefix, String suffix){
		if(null == str){
			return CompositeExpression.NULL;
		}
		if(Strings.EMPTY.equals(str)){
			return CompositeExpression.EMPTY;
		}
		if(str.indexOf(prefix) >= 0){
			return new CompositeExpression(str,parseNodes(language,str, prefix, suffix));
		}else{
			return new CompositeExpression(str);
		}
	}

	private static Object[] parseNodes(ExpressionLanguage language,String s, String prefix, String suffix){
		StringBuilder buf = new StringBuilder(s);

		List<Object> nodes = new ArrayList<Object>();

		int startIndex = nextEvalPrefix(buf,0, prefix);

		int mark=0;
		while(startIndex >= 0){
			int endIndex = buf.indexOf(suffix, startIndex + 2);
			if(endIndex > 0){
				if(mark < startIndex){
					nodes.add(buf.substring(mark,startIndex));
				}
				nodes.add(language.createExpression(buf.substring(startIndex+2,endIndex)));

				if(endIndex == buf.length() - 1){
					mark = buf.length();
					break;
				}else{
					mark = endIndex + 1;
				}

				startIndex = nextEvalPrefix(buf,endIndex + 1, prefix);
			}else{
				throw new ExpressionException("Unclosed expression starts from " + startIndex + ", expected suffix '}'");
			}
		}

		if(mark < buf.length()){
			nodes.add(buf.substring(mark));
		}

		return nodes.toArray(new Object[nodes.size()]);
	}

	private static int nextEvalPrefix(StringBuilder str, int from, String prefix){
		int i = str.indexOf(prefix,from);

		//escape
		if( i > 0 && str.charAt(i-1) == '\\'){
			str.deleteCharAt(i-1);
			return nextEvalPrefix(str, i+1, prefix);
		}

		return i;
	}
	
	protected EL() {
		
	}
	
}

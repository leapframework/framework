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

import java.util.Map;

import leap.core.AppContext;
import leap.lang.Objects2;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.el.ElException;
import leap.lang.expression.Expression;
import leap.lang.expression.ValuedExpression;

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
		
		return !Objects2.isEmpty(v);
	}
	
	protected EL() {
		
	}
	
}

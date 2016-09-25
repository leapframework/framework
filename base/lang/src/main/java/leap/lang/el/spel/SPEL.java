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
package leap.lang.el.spel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leap.lang.Objects2;
import leap.lang.Strings;
import leap.lang.el.DefaultElParseContext;
import leap.lang.el.ElException;
import leap.lang.el.ElFunction;
import leap.lang.el.ElParseContext;
import leap.lang.el.ElParseException;
import leap.lang.el.spel.parser.Parser;
import leap.lang.expression.CompositeExpression;
import leap.lang.expression.Expression;
import leap.lang.expression.ExpressionException;

/**
 * SPEL means simple el
 */
public class SPEL {

	private static final Map<String, ElFunction> functions    = new ConcurrentHashMap<>();
	private static final DefaultElParseContext   parseContext = new DefaultElParseContext(functions);
	
	public static final String PREFIX = "${";
	public static final String SUFFIX = "}";
	
	/**
	 * Returns the global default parse context object.
	 */
	public static ElParseContext getDefaultParseContext() {
		return parseContext;
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
	 * Parses the string expression to an {@link Expression} object using the default {@link ElParseContext}.
	 * 
	 * <p>
	 * The expression must not prefix with '${' and suffix with '}'.
	 */
	public static SpelExpression createExpression(String expression) throws ElParseException {
		return createExpression(parseContext, expression);
	}
	
	/**
	 * Parses the string expression to an {@link Expression} object using the given {@link ElParseContext}.
	 * 
	 * <p>
	 * The expression must not prefix with '${' and suffix with '}'.
	 */
	public static SpelExpression createExpression(ElParseContext context,String expression) throws ElParseException {
		return new SpelExpression(Parser.parse(context, expression));
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
		return parseCompositeExpression(parseContext, s);
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
	public static CompositeExpression createCompositeExpression(ElParseContext context, String s){
		return parseCompositeExpression(context, s);
	}
	
	/**
	 * Evaluates a expression and returns the value.
	 */
	public static Object eval(String expression) throws ElException {
		return createExpression(parseContext, expression).getValue();
	}
	
	/**
	 * Evaluates a expression and returns the value.
	 */
	public static Object eval(String expression,Object context) throws ElException {
		return createExpression(parseContext, expression).getValue(context);
	}
	
	/**
	 * Evaluates a expression and returns the value.
	 */
	public static Object eval(String expression,Object context,Map<String, Object> vars) throws ElException {
		return createExpression(parseContext, expression).getValue(context,vars);
	}
	
	protected static boolean testValue(Object v){
		if(v instanceof Boolean){
			return (Boolean)v;
		}
		return !Objects2.isEmpty(v);
	}
	
	private static CompositeExpression parseCompositeExpression(ElParseContext context, String str){
		if(null == str){
			return CompositeExpression.NULL;
		}
		if(Strings.EMPTY.equals(str)){
			return CompositeExpression.EMPTY;
		}
		if(str.indexOf(SPEL.PREFIX) >= 0){
			return new CompositeExpression(str,parseNodes(context,str));
		}else{
			return new CompositeExpression(str);
		}
	}
	
	private static Object[] parseNodes(ElParseContext context,String s){
		StringBuilder buf = new StringBuilder(s);
		
		List<Object> nodes = new ArrayList<Object>();
		
		int startIndex = nextEvalPrefix(buf,0);
		
		int mark=0;
		while(startIndex >= 0){
			int endIndex = buf.indexOf(SPEL.SUFFIX, startIndex + 2);
			if(endIndex > 0){
				if(mark < startIndex){
					nodes.add(buf.substring(mark,startIndex));
				}
				nodes.add(SPEL.createExpression(context,buf.substring(startIndex+2,endIndex)));
				
				if(endIndex == buf.length() - 1){
					mark = buf.length();
					break;
				}else{
					mark = endIndex + 1;
				}
				
				startIndex = nextEvalPrefix(buf,endIndex + 1);
			}else{
				throw new ExpressionException("Unclosed expression starts from " + startIndex + ", expected suffix '}'");
			}
		}
		
		if(mark < buf.length()){
			nodes.add(buf.substring(mark));
		}
		
		return nodes.toArray(new Object[nodes.size()]);
	}

	private static int nextEvalPrefix(StringBuilder str,int from){
		int i = str.indexOf(SPEL.PREFIX,from);
		
		//escape
		if( i > 0 && str.charAt(i-1) == '\\'){
			str.deleteCharAt(i-1);
			return nextEvalPrefix(str, i+1);
		}
		
		return i;
	}
	
	protected SPEL() {
		
	}
}
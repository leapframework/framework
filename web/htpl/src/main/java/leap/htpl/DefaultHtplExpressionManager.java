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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.annotation.N;
import leap.core.el.EL;
import leap.core.el.ExpressionLanguage;
import leap.web.assets.AssetConfig;
import leap.htpl.escaping.EscapableExpression;
import leap.htpl.expression.HtplExpression;
import leap.htpl.expression.HtplExpression.Parameters;
import leap.htpl.expression.MsgExpression;
import leap.htpl.expression.UrlExpression;
import leap.lang.Strings;
import leap.lang.expression.CompositeExpression;
import leap.lang.expression.Expression;
import leap.lang.expression.ExpressionException;
import leap.lang.expression.ValuedExpression;
import leap.lang.html.HTML;
import leap.lang.text.KeyValueParser;

public class DefaultHtplExpressionManager implements HtplExpressionManager {
	
	public static final char CHAR_VARIABLE = ':';
	public static final char CHAR_MESSAGE  = '#';
	public static final char CHAR_CTXPATH  = '~';
	
	protected @Inject @M BeanFactory beanFactory;
	protected @Inject @N AssetConfig assetConfig;
	
	@Inject(name="htpl")
	protected @M ExpressionLanguage el;
	
	@Override
	public ExpressionLanguage getExpressionLanguage() {
		return el;
	}

    public Expression parseExpression(HtplEngine engine, String text) throws ExpressionException {
	    try {
	        return createExpression(engine,text,(t) ->  EL.createExpression(el,t), true);
        } catch (Exception e) {
        	throw new ExpressionException("Error parsing expression '" + text + "' : " + e.getMessage() , e);
        }
    }

	public Expression tryParseCompositeExpression(HtplEngine engine, String text) {
        final List<Object> nodes = new ArrayList<>();

        AtomicBoolean hasExpression = new AtomicBoolean(false);

        doParseCompositeExpression(engine, text, new ParseHandler() {
            @Override
            public void textParsed(String text) {
                nodes.add(text);
            }

            @Override
            public void exprParsed(Expression expr) {
                nodes.add(expr);
                hasExpression.set(true);
            }
        });

        if(!hasExpression.get()) {
            return null;
        }else if(nodes.size() == 1 && nodes.get(0) instanceof Expression){
            return (Expression)nodes.get(0);
        }else{
            return new CompositeExpression(text, nodes.toArray());
        }
    }
    
	@Override
    public Expression parseCompositeExpression(HtplEngine engine, String text) {
        Expression expr = tryParseCompositeExpression(engine, text);
        if(null == expr) {
            expr = new ValuedExpression<>(text);
        }
        return expr;
    }
	
	@Override
    public void parseCompositeExpression(HtplEngine engine, String text, ParseHandler callback) {
		if(Strings.isEmpty(text)){
			return;
		}
	    doParseCompositeExpression(engine, text, callback);
    }
	
	@Override
    public Expression parseAttributeExpression(HtplEngine engine, String value) {
		Expression expression = tryParseAttributeExpression(engine, value);
		if(null == expression){
			return new ValuedExpression<String>(value);
		}else{
			return expression;
		}
    }
	
	@Override
    public Expression tryParseAttributeExpression(HtplEngine engine, String value) {
		final List<Object> nodes = new ArrayList<Object>();
		
		final AtomicBoolean hasExpressions = new AtomicBoolean(false);
		
		parseAttributeExpression(engine, value, new ParseHandler() {
			
			@Override
			public void textParsed(String text) {
				nodes.add(text);
			}
			
			@Override
			public void exprParsed(Expression expr) {
				nodes.add(expr);
				hasExpressions.set(true);
			}
		});
		
		if(!hasExpressions.get()) {
			return null;
		}
		
		if(nodes.size() == 1 && nodes.get(0) instanceof Expression){
			return (Expression)nodes.get(0);
		}else{
			return new CompositeExpression(value, nodes.toArray());	
		}
    }

	@Override
    public void parseAttributeExpression(HtplEngine engine, String value, ParseHandler callback) {
		if(Strings.isEmpty(value)){
			return;
		}
		doParseCompositeExpression(engine, value, callback);
    }
	
	protected void doParseCompositeExpression(HtplEngine engine, String s, ParseHandler callback){
		StringBuilder str = new StringBuilder(s);
		
		Prefix prefix = nextPrefix(str,0);
		
		int mark=0;
		while(prefix != null){
			int exprStart = prefix.startIndex + prefix.len;
			int endIndex  = str.indexOf(prefix.suffix, exprStart);
			if(endIndex > 0){
				if(mark < prefix.startIndex){
					callback.textParsed(str.substring(mark,prefix.startIndex));
				}
				
				String text = HTML.unescape(str.substring(exprStart,endIndex));
				
				if(prefix.isEl()){
					callback.exprParsed(createExpression(engine, text, (t) -> el.createExpression(t), prefix.escaped));	
				}else if(prefix.isMsg()){
					callback.exprParsed(createExpression(engine, text, (t) -> new MsgExpression(t), prefix.escaped));
				}else if(prefix.isUrl()){
					callback.exprParsed(createExpression(engine, text, (t) -> createUrlExpression(engine, t), prefix.escaped));
				}
				
				if(endIndex == str.length() - 1){
					mark = str.length();
					break;
				}else{
					mark = endIndex + 1;
				}
				
				prefix = nextPrefix(str, endIndex + 1);
			}else{
				throw new ExpressionException("Unclosed expression [ " + s + " ], starts from " + prefix.startIndex + ", expected suffix '" + prefix.suffix + "'");
			}
		}
		
		if(mark < str.length()){
			callback.textParsed(str.substring(mark));
		}
	}
	
    protected UrlExpression createUrlExpression(HtplEngine engine, String url) {
		if(null == assetConfig){
			return new UrlExpression(engine, url);	
		}
		return new leap.htpl.processor.assets.AssetsUrlExpression(assetConfig, engine, url);
	}
	
	protected Expression createExpression(HtplEngine engine, String text, Function<String, Expression> func, boolean escaped) {

		String expression = text;
		String params = null;
		
		if(text.length() > 2) {
			int seperatorIndex = text.lastIndexOf(';');

			if(seperatorIndex > 0) {
				int maxIndex = text.length() - 1;
				
				if(seperatorIndex == maxIndex) {
					expression = text.substring(0,text.length() - 2);
				}else{
					boolean seperatorInExpression = false;
					
					for(int i=seperatorIndex+1;i<text.length();i++) {
						char c = text.charAt(i);
						
						if(c == '\'' || c == '"'){
							seperatorInExpression = true;
							break;
						}
					}
					
					if(!seperatorInExpression) {
						expression = text.substring(0,seperatorIndex);
						params = text.substring(seperatorIndex + 1);
					}
				}
			}
		}
		
		Expression e;
		
		if(Strings.isEmpty(params)) {
			e = func.apply(expression);	
		}else{
			Map<String, String> map = new HashMap<String, String>();
			
			//parse params -> key:value, key:value
			KeyValueParser.parseKeyValuePairs(map, params);
			
			e = createWrappedHtplExpression(text, func.apply(expression), map);
		}
		
		if(!escaped && !(e instanceof EscapableExpression)) {
			return new HtplExpression(e, Parameters.UN_ESCAPED);
		}
		return e;
	}
	
	protected HtplExpression createWrappedHtplExpression(String text, Expression real, Map<String, String> map) {
		HtplExpression.Parameters params = new HtplExpression.Parameters();
		
		for(Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			
			if(!params.set(key, val)) {
				throw new ExpressionException("Invalid parameter [" + key + ":" + val + "] in expression : " + text);
			}
		}
		
		return new HtplExpression(real, params);
	}
	
	protected static final String EL_PREFIX_ESCAPED    = "${";
	protected static final String EL_PREFIX_UNESCAPED  = "$!{";
	protected static final String MSG_PREFIX_ESCAPED   = "#{";
	protected static final String MSG_PREFIX_UNESCAPED = "#!{";
	protected static final String URL_PREFIX_ESCAPED   = "@{";
	protected static final String URL_PREFIX_UNESCAPED = "@!{";
	protected static final String COMMON_EXPR_SUFFIX   = "}";

	protected Prefix nextPrefix(StringBuilder str,int from){
		int max = str.length() - 3;
		int pos = from;
		
		Prefix prefix = null;
		
		while(pos < max){
			String s = str.substring(pos,pos+2);
			
			if(Strings.equals(s, EL_PREFIX_ESCAPED)){
				prefix = Prefix.el(pos);
				break;
			}

			if(Strings.equals(s, MSG_PREFIX_ESCAPED)){
				prefix = Prefix.msg(pos);
				break;
			}
			
			if(Strings.equals(s, URL_PREFIX_ESCAPED)){
				prefix = Prefix.url(pos);
				break;
			}
			
			s = str.substring(pos,pos+3);
			
			if(Strings.equals(s, EL_PREFIX_UNESCAPED)){
				prefix = Prefix.elUnescaped(pos);
				break;
			}
			
			if(Strings.equals(s, MSG_PREFIX_UNESCAPED)){
				prefix = Prefix.msgUnescaped(pos);
				break;
			}
			
			if(Strings.equals(s, URL_PREFIX_UNESCAPED)){
				prefix = Prefix.urlUnescaped(pos);
				break;
			}
			
			pos++;
		}
		
		//escape
		if(prefix != null && pos > 0 && str.charAt(pos - 1) == '\\'){
			str.deleteCharAt(pos-1);
			return nextPrefix(str, pos+1);
		}

		return prefix;
	}
	
	protected static final class Prefix {
		
		protected static final int TYPE_EL  = 0;
		protected static final int TYPE_MSG = 1;
		protected static final int TYPE_URL = 2;
		
		protected static Prefix el(int startIndex){
			return new Prefix(startIndex, TYPE_EL,EL_PREFIX_ESCAPED.length(),COMMON_EXPR_SUFFIX, true);
		}
		
		protected static Prefix elUnescaped(int startIndex){
			return new Prefix(startIndex, TYPE_EL,EL_PREFIX_UNESCAPED.length(),COMMON_EXPR_SUFFIX, false);
		}
		
		protected static Prefix msg(int startIndex){
			return new Prefix(startIndex, TYPE_MSG,MSG_PREFIX_ESCAPED.length(),COMMON_EXPR_SUFFIX, true);
		}
		
		protected static Prefix msgUnescaped(int startIndex){
			return new Prefix(startIndex, TYPE_MSG,MSG_PREFIX_UNESCAPED.length(),COMMON_EXPR_SUFFIX, false);
		}
		
		protected static Prefix url(int startIndex) {
			return new Prefix(startIndex, TYPE_URL,URL_PREFIX_ESCAPED.length(),COMMON_EXPR_SUFFIX, true);
		}
		
		protected static Prefix urlUnescaped(int startIndex) {
			return new Prefix(startIndex, TYPE_URL,URL_PREFIX_UNESCAPED.length(),COMMON_EXPR_SUFFIX, false);
		}
		
		protected final int     startIndex;
		protected final int     type;
		protected final int     len;
		protected final String  suffix;
		protected final boolean escaped;
		
		protected Prefix(int startIndex, int type,  int len, String suffix, boolean escaped){
			this.startIndex = startIndex;
			this.type       = type;
			this.len        = len;
			this.suffix     = suffix;
			this.escaped    = escaped;
		}
		
		protected boolean isEscaped() {
			return escaped;
		}
		
		protected boolean isEl(){
			return type == TYPE_EL;
		}
		
		protected boolean isMsg(){
			return type == TYPE_MSG;
		}
		
		protected boolean isUrl() {
			return type == TYPE_URL;
		}
	}
}
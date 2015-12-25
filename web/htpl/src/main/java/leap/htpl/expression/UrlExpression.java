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
package leap.htpl.expression;

import java.util.Map;
import java.util.function.BiFunction;

import leap.htpl.HtplContext;
import leap.htpl.HtplEngine;
import leap.lang.Strings;
import leap.lang.expression.AbstractExpression;
import leap.lang.expression.Expression;
import leap.lang.path.Paths;

public class UrlExpression extends AbstractExpression {
	
	public static final String PREFIX = "@{";
	public static final String SUFFIX = "}";

	protected String 	   			       						  url;
	protected BiFunction<HtplContext,Map<String, Object>, String> func;
	
	protected UrlExpression(){
		
	}
	
	public UrlExpression(HtplEngine engine,String url) {
		this.url  = url;
		this.func = createFunc(engine, url);
	}
	
	public UrlExpression(HtplEngine engine,String url,Expression expression) {
		this.url  = url;
		this.func = createFunc(engine, expression);
	}
	
    @Override
    protected Object eval(Object context, Map<String, Object> vars) {
		if(null == func){
			return url;
		}else{
			return func.apply((HtplContext)context,vars);
		}
    }
	
	protected BiFunction<HtplContext,Map<String, Object>, String> createFunc(HtplEngine engine,final String url){
		//Protocol relative url
		if(Strings.startsWith(url, "//")){
			return null;
		}
		
		//Context relative url
		if(Strings.startsWith(url, "/") || Strings.startsWith(url, "~")){
			String pathWithoutPrefix = url.substring(1);
			final String path = Strings.isEmpty(pathWithoutPrefix) ? "" : Paths.prefixWithSlash(pathWithoutPrefix);
			
			return (c,vars) -> { return c.getContextPath() + path; };
		}
		
		//Server relative url
		if(Strings.startsWith(url, "^")){
			final String pathWithPrefix = Paths.prefixWithSlash(url.substring(1));
			return (c,vars) -> { return pathWithPrefix; };
		}
		
		return null;
	}
	
	protected BiFunction<HtplContext,Map<String, Object>, String> createFunc(HtplEngine engine,final Expression expression){
		return (c, vars) -> {
			String url = expression.getValue(String.class,c,vars);
			
			if(null == url){
				return "";
			}
			
			if(url.startsWith("//")){
				return url;
			}

			if(url.startsWith("/") || url.startsWith("~")) {
				String pathWithoutPrefix = url.substring(1);
				String path 			 = Strings.isEmpty(pathWithoutPrefix) ? "" : Paths.prefixWithSlash(pathWithoutPrefix);
				
				return c.getContextPath() + path;
			}
			
			if(url.startsWith("^")) {
				return Paths.prefixWithSlash(url.substring(1));
			}
			
			return url;
		};
	}

	@Override
    public String toString() {
		return url;
	}
}
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
package leap.orm.sql.parser;

import java.util.HashMap;
import java.util.Map;

import leap.lang.Strings;
import leap.lang.collection.SimpleCaseInsensitiveMap;

class Keywords {
	
	public static Keywords DEFAULT;
	public static Keywords ATWORDS;
	
	static {
		Map<String, Token> keywords = new HashMap<String, Token>();
		Map<String, Token> atwords  = new HashMap<String, Token>();
		
		for(Token token : Token.values()){
			if(token.isKeyword()){
				keywords.put(token.literal(),token);
			}else{
				String v = token.literal();
				if(null != v && v.startsWith("@") && v.length() > 1){
					String remaining = v.substring(1);
					if(Strings.isLetters(remaining)){
						atwords.put(remaining, token);
					}
				}
			}
		}

		DEFAULT = new Keywords(keywords);
		ATWORDS = new Keywords(atwords);
	}

	private final Map<String,Token> map;
	
	public Keywords(){
		this.map = new SimpleCaseInsensitiveMap<Token>();
	}
	
	public Keywords(Map<String, Token> keywords){
		this.map = new SimpleCaseInsensitiveMap<Token>(keywords);
	}
	
	public Token getToken(String keyword){
		return map.get(keyword);
	}
	
	public Map<String, Token> map(){
		return map;
	}
}
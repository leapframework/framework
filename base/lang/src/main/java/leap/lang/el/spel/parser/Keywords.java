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
package leap.lang.el.spel.parser;

import java.util.HashMap;
import java.util.Map;

class Keywords {
    public static Keywords DEFAULT;

    static {
        Map<String, Token> map = new HashMap<String, Token>();
        
        map.put("if", Token.IF);
        map.put("else", Token.ELSE);
        map.put("return", Token.RETURN);
        map.put("for", Token.FOR);
        map.put("while", Token.WHILE);
        map.put("do", Token.DO);
        map.put("new", Token.NEW);
        map.put("instanceof", Token.INSTNACEOF);
        map.put("contains", Token.CONTAINS);
        map.put("startsWith", Token.STARTS_WITH);
        map.put("endsWith", Token.ENDS_WITH);
        
        map.put("byte", Token.BYTE);
        map.put("short", Token.SHORT);
        map.put("int", Token.INT);
        map.put("long", Token.LONG);
        map.put("float", Token.FLOAT);
        map.put("double", Token.DOUBLE);
        
        map.put("null", Token.NULL);
        map.put("true", Token.TRUE);
        map.put("false", Token.FALSE);
        
        DEFAULT = new Keywords(map);
    }
    
    private final Map<String, Token> map;
    
    public Keywords(Map<String, Token> map) {
        this.map = map;
    }

    public Token getToken(String key) {
        return map.get(key);
    }
}

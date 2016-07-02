/*
 * Copyright 2015 the original author or authors.
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
package leap.lang.text;

import java.util.Map;

public class KeyValueParser {
    
    public static final class InvalidKeyValueStringException extends RuntimeException {

        private static final long serialVersionUID = -734586009003744561L;

        public InvalidKeyValueStringException() {
            super();
        }

        public InvalidKeyValueStringException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidKeyValueStringException(String message) {
            super(message);
        }

        public InvalidKeyValueStringException(Throwable cause) {
            super(cause);
        }
    }

    // key:value, key:value ...
    public static void parseKeyValuePairs(Map<String,String> pairs, String string){
        parseKeyValuePairs(pairs, string, ':');
    }
    
    public static void parseKeyValuePairs(Map<String,String> pairs, String string, char seperator){
        string = string.trim();
        int  mark;
        char ch;
        for(int i=0;i<string.length();i++){
            //skip white spaces
            for(;i<string.length();i++){
                ch = string.charAt(i);
                if(!Character.isWhitespace(ch)){
                    break;
                }
            }
            //scan key
            mark = i;
            String key  = null;
            String value = null;
            for(;i<string.length();i++){
                ch = string.charAt(i);
                if(ch == seperator){
                    key = string.substring(mark,i).trim();
                    i++;
                    break;
                }
            }
            //skip white spaces
            for(;i<string.length();i++){
                ch = string.charAt(i);
                if(!Character.isWhitespace(ch)){
                    break;
                }
            }
            //scan value
            mark = i;
            ch = string.charAt(i);

            for(;i<string.length();i++){
                ch = string.charAt(i);
                if(ch == ','){
                    value = string.substring(mark,i).trim();
                    break;
                }
            }
            if(null == value && mark < i){
                value = string.substring(mark,i).trim();
            }
            if(null == key || null == value){
                throw new InvalidKeyValueStringException("Invalid key-value string '" + string + "', must be 'key" + seperator + "value' format");
            }
            
            pairs.put(key,value);
        }
    }   

}

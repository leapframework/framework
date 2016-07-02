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
package leap.lang.st.stpl;

import java.util.ArrayList;
import java.util.List;

class StplParser {

	private static final int TOKEN_REPLACE_START = 1;
	private static final int TOKEN_DYNAMIC_START = 2;
	
	public static StplTemplate parse(String text) {
		char[] chars = text.toCharArray();
		int    len   = chars.length;
		
		char ch;
		
		int  pos;
		int  token  = -1;
		int  cursor = 0;
		int  start0 = 0;
		int  start1 = 0;
		
		List<StplNode> nodes = new ArrayList<>();
		
		for(pos=0;pos<len;pos++){
			ch = chars[pos];
			
			if(Character.isWhitespace(ch)){
				
				if(token == TOKEN_REPLACE_START){
					token = -1;
				}
				
				continue;
			}			
			
			switch (ch) {
				
	            case '$':
		            //$replacement$
	            	
	            	if(token == -1){
	            		token  = TOKEN_REPLACE_START;
	            		start0 = pos;
	            	}else if(token == TOKEN_REPLACE_START){
	            		
	            		if(start0 == pos -1){
	            			token = TOKEN_REPLACE_START;
	            			start0 = pos;
	            			continue;
	            		}
	            		
	            		if(cursor < start0){
	            			nodes.add(new StplNode.TextNode(text.substring(cursor,start0)));
	            		}
	            		
	            		nodes.add(new StplNode.ReplaceNode(text.substring(start0 + 1, pos)));
	            		
	            		token  = -1;
	            		cursor = pos + 1;
	            	}
	            	
		            break;
		            
	            case '{' :
	            	
	            	start0 = pos;
	            	
	            	if(pos < len - 1 && chars[pos+1] == '?'){
	            		pos++;
	            		
	            		start1 = pos;
	            		
	            		token = TOKEN_DYNAMIC_START;
	            		
	            		for(;pos<len;pos++){
	            			
	            			ch = chars[pos];
	            			
	            			if(ch == '}'){
	    	            		if(cursor < start0){
	    	            			nodes.add(new StplNode.TextNode(text.substring(cursor,start0)));
	    	            		}	            				
	            				
	            				nodes.add(new StplNode.ClauseNode(text.substring(start1 + 1,pos)));
	            				
	            				token  = -1; 
	            				cursor = pos+1;
	            				break;
	            			}
	            			
		            		if(token == -1){
		            			break;
		            		}	            			
	            		}
	            	}
		            
	            	break;
	            default:
	            	continue;
            }
		}
		
		if(cursor < len){
			nodes.add(new StplNode.TextNode(text.substring(cursor,len)));
		}		
		
		return new StplTemplate(text, nodes.toArray(new StplNode[nodes.size()]));
	}
}

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

import leap.lang.Objects2;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.params.Params;

abstract class StplNode {

	abstract boolean render(StringBuilder out,Params params);
	
	static class TextNode extends StplNode {
		private String text;
		
		TextNode(String text){
			this.text = text;
		}
		
		@Override
        public boolean render(StringBuilder out, Params params) {
			out.append(text);
	        return true;
        }

		@Override
        public String toString() {
	        return text;
        }
	}
	
	static class ReplaceNode extends StplNode {
		
		private String param;
		
        ReplaceNode(String param) {
        	this.param = param;
        }
        
		@Override
        public boolean render(StringBuilder out, Params params) {
			
			Object value = params.get(param);

			if(null != value){
				
				String string = Strings.trimToNull(Converts.toString(value));
				
				if(null != string){
					out.append(string);
					
					return true;
				}
			}
			
			return false;
        }

		@Override
        public String toString() {
	        return "$" + param + "$";
        }
	}
	
	static class ClauseNode extends StplNode {
		
		private StplTemplate tpl;
		
		ClauseNode(String text){
			this.tpl = StplParser.parse(text); 
		}
		
		@Override
        public boolean render(StringBuilder out, Params params) {
			
			for(StplNode expr : tpl.getNodes()) {
				if(expr instanceof ReplaceNode) {
					String name = ((ReplaceNode)expr).param;
					Object value = params.get(name);
					
					if(Objects2.isEmpty(value)){
						return false;
					}
				}
			}
			
			tpl.render(out, params);
			
	        return true;
        }

		@Override
        public String toString() {
	        return "{?" + tpl.getText() + "}";
        }
	}
}

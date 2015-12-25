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
package leap.htpl.escaping;

import java.io.IOException;

import leap.lang.Exceptions;

public class DefaultAttributeEscaper implements HtplEscaper {

	@Override
    public String escape(CharSequence cs) {
		if(null == cs){
			return null;
		}
		
		StringBuilder out = new StringBuilder(cs.length());
		
		try {
	        escape(cs,out);
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
		
	    return out.toString();
    }

	@Override
    public void escapeAndAppend(CharSequence cs, Appendable out) throws IOException {
		if(null == cs){
			return;
		}
		escape(cs,out);
    }

	protected void escape(CharSequence cs,Appendable out) throws IOException {
		for(int i=0;i<cs.length();i++) {
			char c = cs.charAt(i);
			
			if(c == '"'){
				out.append("&quot;");
				continue;
			}
			
			if(c == '&') {
				out.append("&amp;");
				continue;
			}
			
			out.append(c);
		}
	}
	
}

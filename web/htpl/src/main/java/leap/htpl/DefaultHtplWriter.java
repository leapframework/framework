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

import java.io.IOException;
import java.io.Writer;

import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.exception.NestedIOException;
import leap.lang.html.HTML;

public class DefaultHtplWriter implements HtplWriter {
	
	private static final String ATTR_EQQUOTE = "=\"";
	private static final char   ATTR_QUOTE   = '"';
	
	protected final Writer out;

	public DefaultHtplWriter(HtplEngine engine, Writer out) {
		this.out = out;
	}
	
	@Override
    public Appendable append(CharSequence csq) throws IOException {
		out.append(csq);
	    return this;
    }

	@Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
		out.append(csq,start,end);
	    return this;
    }

	@Override
    public Appendable append(char c) throws IOException {
		out.append(c);
	    return this;
    }

	@Override
    public HtplWriter attribute(String localName, String value) {
		write(localName).write(ATTR_EQQUOTE);
		
		try {
	        HTML.escapeAndAppend(value, out);
        } catch (IOException e) {
        	Exceptions.wrap(e);
        }
		
		write(ATTR_QUOTE);
		return this;
    }
	
	public HtplWriter write(CharSequence s) throws NestedIOException {
		if(null != s){
			try {
	            out.append(s);
            } catch (IOException e) {
            	throw new NestedIOException("Error writing string '" + s + "' : " + e.getMessage(),e);
            }
		}
		return this;
	}
	
	@Override
    public HtplWriter write(char c) throws NestedIOException {
		try {
            out.append(c);
        } catch (IOException e) {
        	throw new NestedIOException("Error writing char '" + c + "' : " + e.getMessage(),e);
        }
	    return this;
    }
	
	@Override
    public HtplWriter startElement(String prefix, String localName) {
		write('<');
		if(!Strings.isEmpty(prefix)){
			write(prefix).write(':');
		}
		write(localName);
	    return this;
    }

	@Override
    public HtplWriter closeElement() {
		write("/>");
	    return this;
    }

	@Override
    public HtplWriter closeElement(String prefix, String localName) {
		write("</");
		if(!Strings.isEmpty(prefix)){
			write(prefix).write(':');
		}
		write(localName).write('>');
	    return this;
    }
}
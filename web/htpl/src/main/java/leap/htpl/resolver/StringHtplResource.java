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
package leap.htpl.resolver;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Locale;

import leap.htpl.HtplResource;
import leap.lang.Out;

public class StringHtplResource implements HtplResource {
	
	public static final String UNSPECIFIED_SOURCE = "unspecified";

	private final Object source;
	private final String string;
	private final Locale locale;
	
	public StringHtplResource(String string) {
		this(UNSPECIFIED_SOURCE,string);
	}
	
	public StringHtplResource(String string,Locale locale) {
		this(UNSPECIFIED_SOURCE,string,locale);
	}
	
	public StringHtplResource(Object source,String string) {
		this(source,string,null);
	}
	
	public StringHtplResource(Object source,String string, Locale locale) {
		this.source = source;
		this.string = string;
		this.locale = locale;
	}
	
	@Override
    public Locale getLocale() {
	    return locale;
    }

	@Override
    public Object getSource() {
	    return source;
    }

	@Override
    public Reader getReader() throws IOException {
	    return new StringReader(string);
    }

	@Override
    public boolean reloadable() {
	    return false;
    }

	@Override
    public boolean reload(Out<Reader> out) throws IOException {
	    return false;
    }

	@Override
    public String toString() {
		return this.getClass().getSimpleName() + "[source=" + source + "]";
    }
}

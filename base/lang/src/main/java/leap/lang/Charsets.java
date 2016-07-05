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
package leap.lang;

import java.nio.charset.Charset;

/**
 * Charsets required of every implementation of the Java platform.
 * 
 * <p/>
 * 
 * From the Java documentation <a href="http://docs.oracle.com/javase/6/docs/api/java/nio/charset/Charset.html">
 */
public class Charsets {
	
	public static final String UTF_8_NAME  = "UTF-8";

	public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
	public static final Charset US_ASCII   = Charset.forName("US-ASCII");
	public static final Charset UTF_16     = Charset.forName("UTF-16");
	public static final Charset UTF_16BE   = Charset.forName("UTF-16BE");
	public static final Charset UTF_16LE   = Charset.forName("UTF-16LE");
	public static final Charset UTF_8      = Charset.forName("UTF-8");
	
	public static Charset defaultCharset(){
		//TODO : file.encoding ?
		return UTF_8;
	}
	
	public static Charset get(Charset charset){
		return null == charset ? defaultCharset() : charset;
	}
	
	public static Charset get(String charset){
		return null == charset ? defaultCharset() : Charset.forName(charset);
	}
	
	public static Charset forName(String charset){
		return Charset.forName(charset);
	}
	
	protected Charsets(){
		
	}
}
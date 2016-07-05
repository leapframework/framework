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
package leap.lang.yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;

import leap.lang.exception.NestedIOException;

public class YamlProperties extends LinkedHashMap<String, String> {

	private static final long serialVersionUID = -7395941062458585097L;
	
	public static YamlProperties read(String content) throws YamlException {
		try {
	        return new YamlProperties(new StringReader(content));
        } catch (IOException e) {
        	throw new NestedIOException(e);
        }
	}

	public static YamlProperties read(Reader reader) throws IOException,YamlException {
		return new YamlProperties(reader);
	}
	
	private Parser parser;
	private Event  event;
	
	private YamlProperties(Reader reader) throws IOException {
		this.parser = new Parser(reader);
		this.parse();
	}
	
	private void parse() throws IOException {
		while(next()){
			if(event.type == EventType.STREAM_START){
				continue;
			}
			
			if(event.type == EventType.DOCUMENT_START){
				continue;
			}
			
			if(event.type == EventType.DOCUMENT_END){
				continue;
			}
			
			if(event.type == EventType.STREAM_END){
				break;
			}
			
			if(event.type == EventType.MAPPING_START){
				readProperties("");
				continue;
			}
			
			throw new IllegalStateException("Not a valid yaml properties content, event : " + event);
		}
	}
	
	private void readProperties(String prefix) {
		while(next()){
			if(event.type == EventType.MAPPING_END){
				break;
			}
			
			if(event.type == EventType.SCALAR){
				String key = prefix + ((ScalarEvent)event).value;
				next();
				readProperty(key);
			}
		}
	}
	
	private void readProperty(String key) {
		if(event.type == EventType.SCALAR){
			put(key,((ScalarEvent)event).value);
			return;
		}
		
		if(event.type == EventType.MAPPING_START){
			readProperties(key + ".");
			return;
		}
		
		throw new IllegalStateException("Not a valid yaml properties content, must be scalar value or nested mappings, event : " + event);
	}
	
	private boolean next(){
		event = parser.getNextEvent();
		return null != event;
	}
}

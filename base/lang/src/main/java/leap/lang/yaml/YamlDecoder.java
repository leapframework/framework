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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class YamlDecoder {
	
	private Parser 	     parser;
	private Event  		 event;
	private EventType	 type;
	private List<Object> values = new ArrayList<Object>();
	
	public YamlDecoder(Reader reader) throws IOException {
		this.parser = new Parser(reader);
		this.next();
	}
	
	public YamlValue read() {
		if(event == null){
			throw new IllegalStateException("null event");
		}
		
		do{
			if(type == EventType.STREAM_START){
				continue;
			}
			
			if(type == EventType.STREAM_END){
				break;
			}
			
			if(type == EventType.DOCUMENT_START) {
				continue;
			}
			
			if(type == EventType.DOCUMENT_END){
				continue;
			}
			
			values.add(readValue());

		}while(next());
		
		if(values.size() == 0){
			return YamlValue.NULL;
		}
		
		if(values.size() == 1){
			return YamlValue.of(values.get(0));
		}
		
		return new YamlCollection(values);
	}
	
	private Object readValue() {
		EventType type = event.type;
		
		if(type == EventType.MAPPING_START){
			return readMap();
		}
		
		if(type == EventType.SEQUENCE_START) {
			return readList();
		}
		
		if(type == EventType.SCALAR) {
			return readScalarValue();
		}
		
		throw new IllegalStateException("Illegal yaml event : " + type);
	}
	
	private Map<String, Object> readMap() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		
		while(expectNext()){
			if(type == EventType.MAPPING_END){
				break;
			}
			
			if(type == EventType.SCALAR) {
				String key = ((ScalarEvent)event).value;
				
				expectNext();
				
				map.put(key, readValue());
				
				continue;
			}
			
			throw new IllegalStateException("Illegal yaml mapping event : " + type);
		}
		
		return map;
	}
	
	private List<Object> readList() {
		List<Object> list = new ArrayList<Object>();

		while(expectNext()){
			if(type == EventType.SEQUENCE_END){
				break;
			}
			
			list.add(readValue());
		}
		
		return list;
	}
	
	private Object readScalarValue() {
		ScalarEvent scalar = (ScalarEvent)event;
		return scalar.value;
	}
	
	private boolean next() {
		this.event = parser.getNextEvent();
		this.type  = null == event ? null : event.type;
		return null != event;
	}
	
	private boolean expectNext() {
		if(!next()){
			throw new IllegalStateException("Illegal yaml event, expect next event");
		}
		return true;
	}
}
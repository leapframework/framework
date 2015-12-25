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
package leap.core.utils;

import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

import leap.lang.csv.CSV;
import leap.lang.csv.CsvProcessor;
import leap.lang.enums.CaseType;
import leap.lang.io.IO;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;

public class CsvUtils extends CSV{
	
	public static Map<String, String> readMapFromResources(String location,CaseType toCase){
		return readMapFromResources(location, toCase, true);
	}
	
	public static void readMapFromResources(Map<String, String> map, String location,CaseType toCase) {
		readMapFromResources(map, new String[]{location}, toCase, true);
	}

	public static Map<String, String> readMapFromResources(String location,CaseType toCase,final boolean skipHeader) {
		return readMapFromResources(new String[]{location}, toCase, skipHeader);
	}
	
	public static void readMapFromResources(Map<String, String> map, String location,CaseType toCase,final boolean skipHeader) {
		readMapFromResources(map, new String[]{location}, toCase, skipHeader);
	}
	
	public static Map<String, String> readMapFromResources(String[] locations,CaseType toCase,final boolean skipHeader) {
		final Map<String, String> map = new LinkedHashMap<String, String>();
		
		readMapFromResources(map, locations, toCase, skipHeader);
		
		return map;
	}
	
	public static void readMapFromResources(Map<String, String> map, String[] locations,CaseType toCase) {
		readMapFromResources(map, locations, toCase, true);
	}
	
	public static void readMapFromResources(final Map<String, String> map, String[] locations,CaseType toCase,final boolean skipHeader) {
		if(null == toCase){
			toCase = CaseType.ORIGINAL;
		}
		
		final CaseType finalCase = toCase;
		
		for(Resource resource : Resources.scan(locations)){
			if(!resource.exists()){
				continue;
			}
			
			Reader reader = null;
			try{
				reader = resource.getInputStreamReader();
				CSV.read(reader,new CsvProcessor() {
					@Override
					public void process(int rownum, String[] values) throws Exception {
						//skip header
						if(rownum == 1 && skipHeader){
							return;
						}
						
						//columns : singular,plural
						String singular = finalCase.get(values[0]);
						String plural   = finalCase.get(values[1]);
						
						map.put(singular, plural);
						map.put(plural, singular);
					}
				});
			}finally{
				IO.close(reader);
			}
		}
	}

}
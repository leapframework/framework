/*
 * Copyright 2002-2011 the original author or authors.
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
package leap.lang.serialize;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leap.lang.Assert;

public class Serializes {
	
	public static final String JSON= "json";
	
	private static final Map<String,Serializer> serializers = new ConcurrentHashMap<String, Serializer>();
	
	static {
		register(JSON,new JsonSerializer());
	}
	
	public static void register(String name,Serializer serializer){
		serializers.put(name, serializer);
	}
	
	public static Serializer getSerializer(String name){
		return serializers.get(name);
	}
	
	public static Serializer ensureGetSerializer(String name){
		Serializer serializer = serializers.get(name);
		Assert.notNull(serializer,"serializer '" + name + "' not registered");
		return serializer;
	}
	
	public static Serializer getSerializer(Serialize annotation){
		if(null == annotation){
			return null;
		}
		return ensureGetSerializer(annotation.value());
	}
	
	protected Serializes(){
		
	}
}

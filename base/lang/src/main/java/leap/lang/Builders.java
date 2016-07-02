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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * builder utils.
 */
public class Builders {

	public static <T> T[] buildArray(List<? extends Buildable<T>> builders,T[] array){
		Args.notNull(array);
		Assert.isTrue(builders.size() == array.length,"the array's size must equals to the size of given builders");
		
		for(int i=0;i<array.length;i++){
			array[i] = builders.get(i).build();
		}
		
		return array;
	}
	
	public static <T> T[] buildArray(Buildable<T>[] builders, T[] array) {
		Args.notNull(array);
		Assert.isTrue(builders.length == array.length,"the array's size must equals to the size of given builders");
		
		for(int i=0;i<array.length;i++){
			array[i] = builders[i].build();
		}
		
		return array;
	}
	
	public static <T> List<T> buildList(Iterable<? extends Buildable<T>> builders){
		List<T> list = New.arrayList();
		
		if(null != builders) {
			for(Buildable<T> b : builders){
				list.add(b.build());
			}
		}
		
		return list;
	}
	
	public static <K,V> Map<K,V> buildMap(Map<K,? extends Buildable<V>> builders) {
		Map<K,V> m = New.linkedHashMap();
		
		for(Entry<K, ? extends Buildable<V>> e : builders.entrySet()) {
			m.put(e.getKey(), e.getValue().build());
		}
		
		return m;
	}
	
	protected Builders(){
		
	}
}

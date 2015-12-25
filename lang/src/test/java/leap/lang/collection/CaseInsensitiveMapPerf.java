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
package leap.lang.collection;

import java.util.HashMap;
import java.util.Map;

import leap.lang.reflect.Reflection;
import leap.lang.time.StopWatch;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CaseInsensitiveMapPerf {
	
	public static void main(String[] args) {
		CaseInsensitiveMapPerf perf = new CaseInsensitiveMapPerf();
		
		perf.run(HashMap.class,SimpleCaseInsensitiveMap.class);
		perf.run(HashMap.class,WrappedCaseInsensitiveMap.class);
		perf.run(SimpleCaseInsensitiveMap.class,WrappedCaseInsensitiveMap.class);
    }
	
	private void run(Class<? extends Map> cls1,Class<? extends Map> cls2){
        int num = 100000;
		
		System.out.println("--" + cls1.getSimpleName() + " vs " + cls2.getSimpleName() + " Performance Test(" + num + ")--");
		System.out.println();
		
        for (int i = 1; i <= 5; i++ ) {
        	runRound(cls1,cls2, num,i);
        }
	}
	
	protected void runRound(Class<? extends Map> cls1,Class<? extends Map> cls2, int num,int round){
        Map map1  = Reflection.newInstance(cls1);
        Map map2 = Reflection.newInstance(cls2);
        
        System.out.println("Round " + round + ":");
        
        StopWatch sw = StopWatch.startNew();

        for (int i = 0; i < num; i++) {
            map1.put("a" + String.valueOf(i), null);
        }
        System.out.println("  " + cls1.getSimpleName() + ".put:" + sw.getElapsedMilliseconds());

        sw.restart();
        for (int i = 0; i < num; i++) {
            map2.put("a" + String.valueOf(i), null);
        }
        System.out.println("  " + cls2.getSimpleName() +".put:" + sw.getElapsedMilliseconds());
        System.out.println();

        sw.restart();
        for (int i = 0; i < num; i++) {
            map1.get("a" + String.valueOf(i));
        }
        System.out.println("  " + cls1.getSimpleName() + ".get:" + sw.getElapsedMilliseconds());

        sw.restart();
        for (int i = 0; i < num; i++) {
            map2.get("a" + String.valueOf(i));
        }
        System.out.println("  " + cls2.getSimpleName() +".get:" + sw.getElapsedMilliseconds());
        System.out.println("");
	}
}

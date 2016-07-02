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
package leap.lang.el;

import java.util.HashMap;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import leap.lang.el.spel.SPEL;
import leap.lang.el.spel.SpelExpression;

import org.mvel2.MVEL;

public class ElPerformanceMain {
	private static final Map<String, Object>  vars = new HashMap<>();
	private static final String[]             exps = new String[20];
	
	static {
		vars.put("i", 100);
		vars.put("pi", 3.14d);
		vars.put("d", -3.9);
		vars.put("b", (byte) 4);
		vars.put("bool", false);
		vars.put("o", new O());
		
		final Map<String, Integer> m  = new HashMap<String, Integer>();
		m.put("d", 5);
		vars.put("m", m);
		vars.put("s", "hello world");

		int index = 0;
		exps[index++] = "100";
		exps[index++] = "i";
		exps[index++] = "100 + 1";
		exps[index++] = "100 + i * pi - d";
		exps[index++] = "o.prop";
		exps[index++] = "1000+100.0*99-(600-3*15)%(((68-9)-3)*2-100)+10000%7*71";
		exps[index++] = "s.substring(m.d)";
		exps[index++] = "s.substring(1).substring(2).indexOf('world')";
	}
	
	public static void main(String[] args) {
		run();
	}

	private static void run() {
		//wram up
		test(exps,1000,false);

		int times = 10 * 10000;
		System.out.println("");
		System.out.println("Do it (" + times + " times) : ");
		test(exps,times,true);
	}
	
	private static void test(String[] exps, int times, boolean print){
		for (String exp : exps) {
			if (exp == null) {
				break;
			}
			
			if(print){
				System.out.println("-- " + exp + " --");
			}
			
			long c1 = spel(exp, times, print);
			long c2 = mvel(exp, times, print);
			//long c3 = jsel(exp, times, print);
			
			if(print){
				System.out.println("winner : " + (c1 > c2 ? "mvel" : "spel"));
				System.out.println();
			}
		}
	}

	private static long spel(String exp, int times, boolean print) {
		SpelExpression expression = SPEL.createExpression(exp);
		//ElEvalContext  context    = new DefaultElEvalContext(new HashMap<String,Object>(vars));
		long start = System.currentTimeMillis();
		Object result = null;
		int i = 0;
		while (i++ < times) {
			result = expression.getValue(new HashMap<String,Object>(vars));
		}
		long end = System.currentTimeMillis();
		long cost = end - start;
		if(print){
			System.out.println("spel --------cost[" + cost + " ]---value[" + result + "]");
		}
		return cost;
	}
	
	private static long jsel(String exp, int times, boolean print) {
		try {
	        Compilable compilable = (Compilable)new ScriptEngineManager().getEngineByName("nashorn");

	        CompiledScript script = compilable.compile(exp);

	        long start = System.currentTimeMillis();
	        Object result = null;
	        int i = 0;
	        while (i++ < times) {
	        	result = script.eval(new SimpleBindings(new HashMap<String, Object>(vars)));
	        }
	        long end = System.currentTimeMillis();
	        long cost = end - start;
	        if(print){
	        	System.out.println("jsel --------cost[" + cost + " ]---value[" + result + "]");
	        }
	        return cost;
        } catch (ScriptException e) {
        	System.out.println("jsel -------- failed : " + e.getMessage());
        	return -1;
        }
	}
	
	private static long mvel(String exp, int times, boolean print) {
		try {
	        Object compiled = MVEL.compileExpression(exp);
	        //VariableResolverFactory variables = new CachingMapVariableResolverFactory(new HashMap<String,Object>(vars));
	        long start = System.currentTimeMillis();
	        Object result = null;
	        int i = 0;
	        while (i++ < times) {
	        	result = MVEL.executeExpression(compiled,new HashMap<String,Object>(vars));
	        }
	        long end = System.currentTimeMillis();
	        long cost = end - start;
	        if(print){
	        	System.out.println("mvel --------cost[" + cost + " ]---value[" + result + "]");
	        }
	        return cost;
        } catch (Exception e) {
        	System.out.println("mvel --------error[" + e.getMessage() + " ]");
        	return -1;
        }
	}
	
	public static final class O {
		private String prop = "o.p";

		public String getProp() {
			return prop;
		}

		public void setProp(String prop) {
			this.prop = prop;
		}
	}
}

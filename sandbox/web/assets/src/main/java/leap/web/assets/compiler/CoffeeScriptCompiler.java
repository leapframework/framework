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
package leap.web.assets.compiler;

import java.io.File;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import leap.lang.Classes;
import leap.web.assets.AssetCompileException;
import leap.web.assets.AssetCompiler;
import leap.web.assets.AssetManager;

public class CoffeeScriptCompiler extends AbstractAssetCompiler implements AssetCompiler {
	
	private static final String COMPILE_METHOD = "compile";
	
	private Invocable invocable;
	private Object	  coffeeScript;
	private Object	  options;
	
	private void init() {
		if(null == invocable){
			//create js engine
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
			
			//eval coffee script
			try{
				engine.eval(Classes.getResourceAsString(CoffeeScriptCompiler.class, "coffeescript/coffee-script.js"));
				
				options = engine.eval("var o = {bare: true}; o");
				coffeeScript = engine.get("CoffeeScript");
				
				invocable = (Invocable)engine;
			}catch(ScriptException e){
				throw new RuntimeException("Error eval coffee script by js engine, " + e.getMessage(), e);
			}
		}
	}

	public String compileScript(String source){
		init();
		
		try {
			return (String)invocable.invokeMethod(coffeeScript, COMPILE_METHOD, source, options);
        } catch (Exception e) {
        	throw new AssetCompileException("Error compile script, " + e.getMessage(), e);
        }
	}

	@Override
    protected String doCompile(AssetManager manager, String source, File file) {
	    return compileScript(source);
    }
}
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
import java.nio.charset.Charset;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import leap.core.script.Scripts;
import leap.lang.Charsets;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.reflect.ReflectClass;
import leap.web.assets.AssetCompileException;
import leap.web.assets.AssetCompiler;
import leap.web.assets.AssetManager;

public class LessCssCompiler extends AbstractAssetCompiler implements AssetCompiler {
	
	private static final Log log = LogFactory.get(LessCssCompiler.class);
	
	private static final String COMPILE_FUNCTION = "lessc";
	private static final String INIT_JS          = "less/init.js";
	private static final String LESS_JS          = "less/less.js";
	private static final String LESSC_JS         = "less/lessc.js";
	
	private static final String INIT_SCRIPTS;
	private static final String LESS_SCRIPTS;
	private static final String LESSC_SCRIPTS;
	
	static {
		INIT_SCRIPTS  = Classes.getResourceAsString(LessCssCompiler.class,  INIT_JS);
		LESS_SCRIPTS  = Classes.getResourceAsString(LessCssCompiler.class,  LESS_JS);
		LESSC_SCRIPTS = Classes.getResourceAsString(LessCssCompiler.class,  LESSC_JS);
	}
	
	private Invocable invocable; 
	
	public String compileCss(String source){
		return compileCss(source,null,"<inline>");
	}
	
	public String compileCss(File file){
		return compileCss(IO.readString(file,Charset.defaultCharset()), Paths.getDirPath(file.getAbsolutePath()), file.getName());
	}
	
	public String compileCss(String source,String currentDirectory, String filename){
		try {
			Invocable invocable = createInvocable();
			
			Object result = invocable.invokeFunction(COMPILE_FUNCTION, source, currentDirectory, filename);
			
			if(result instanceof String){
				return (String)result;
			}else {
				LessError e = LessError.convertFromScriptError(invocable, result);
				throw new AssetCompileException(e.messageWithLocation).setErrorInfo(e.filename, source, e.line, e.column);
			}
		} catch (AssetCompileException e){
			throw e;
        } catch (Exception e) {
        	throw new AssetCompileException("Error compile less css, " + e.getMessage(), e).setErrorInfo(filename, source);
        }
	}
	
	@Override
    protected String doCompile(AssetManager manager, String source, File file) {
		if(null == file){
			return compileCss(source);
		}else{
			return compileCss(source,Paths.getDirPath(file.getAbsolutePath()),file.getName());	
		}
    }

	protected Invocable createInvocable(){
		if(null == invocable){
			//create js engine
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
			
			//eval less script
			try{
				//set variables
				engine.put("log", log);
				engine.put("importer", new Importer());
				
				Scripts.eval(engine,INIT_JS,INIT_SCRIPTS);
				Scripts.eval(engine,LESS_JS,LESS_SCRIPTS);
				Scripts.eval(engine,LESSC_JS,LESSC_SCRIPTS);
				

				return (Invocable)engine;
			}catch(ScriptException e){
				throw new RuntimeException("Error eval less script by js engine, " + e.getMessage(), e);
			}
		}

		return invocable;
	}
	
	public static final class Importer {
		
		public String read(String path) {
			return IO.readString(new File(path), Charsets.UTF_8);
		}
		
	}
	
	protected static final class LessError {
		public String type;
		public String message;
		public String messageWithLocation;
		public String filename;
		public int	  line;
		public int    column;
		
		@SuppressWarnings({ "unchecked"})
        protected static LessError convertFromScriptError(Invocable invocable, Object o) throws Exception{
			
			if(o.getClass().getName().equals("jdk.nashorn.api.scripting.ScriptObjectMirror")){
				return convertFromNashorn(invocable, o);
			}else if(o instanceof Map){
				return convertFromScriptError(invocable,  (Map<String, Object>)o);
			}
			throw new IllegalStateException("The error object returned from script result must be a Map, now is '" +
											(o == null ? "null" : o.getClass().getName()) + "'");
		}
		
		protected static LessError convertFromNashorn(Invocable invocable, Object som)  throws Exception {
			//som means ScriptObjectMirror
			ReflectClass oc = ReflectClass.of(som.getClass());
			
			Object nativeError = oc.getField("sobj").getValue(som);
			ReflectClass ec = ReflectClass.of(nativeError.getClass());
			
			LessError e = new LessError();
			
			//instMessage
			e.message =  ec.getField("instMessage").getValue(nativeError).toString();
					
			//nashornException
			Object nashornException = ec.getField("nashornException").getValue(nativeError);
			ReflectClass nec = ReflectClass.of(nashornException.getClass());
			
			e.line = Converts.toInt(nec.getField("line").getValue(nashornException));
			e.column = Converts.toInt(nec.getField("column").getValue(nashornException));
			e.filename = Converts.toString(nec.getField("fileName").getValue(nashornException));
			
			e.messageWithLocation = e.message;
			if(!Strings.isEmpty(e.filename)){
				e.messageWithLocation  = e.messageWithLocation + " ( source file '" + e.filename + "' at line " + e.line + ", column " + e.column + " ) ";
			}
			
			return e;
		}
		
		protected static LessError converFromErrorMap(Invocable invocable, Map<String, Object> map) {
			LessError e = new LessError();
			
			e.type     = Converts.toString(map.get("type"));
			e.message  = Converts.toString(map.get("message"));
			e.filename = Converts.toString(map.get("filename"));
			e.line     = Converts.toInt(map.get("line"));
			e.column   = Converts.toInt(map.get("column"));
			
			e.messageWithLocation = e.message;
			
			if(!Strings.isEmpty(e.filename)){
				e.messageWithLocation  = e.messageWithLocation + " ( source file '" + e.filename + "' ) ";
			}
			
			if(e.line > 0){
				e.messageWithLocation = e.messageWithLocation + " at line " + e.line;
				
				if(e.column > 0){
					e.messageWithLocation = e.messageWithLocation + ", column " + e.column;	
				}
			}
			
			return e;
		}
	}
}
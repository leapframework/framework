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
package leap.core.script;

import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import leap.lang.el.ElException;
import leap.lang.expression.Expression;
import leap.lang.expression.AbstractExpression;
import leap.lang.expression.ExpressionException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public class ScriptExpression extends AbstractExpression implements Expression {
	
	private static final Log log = LogFactory.get(ScriptExpression.class);
	
	protected final String         engineName;
	protected final String         scriptText;
	protected final ScriptEngine   scriptEngine;
	protected final CompiledScript compiledScript;
	
	public ScriptExpression(String engineName,String scriptText){
		this.engineName     = engineName;
		this.scriptText     = scriptText;
		this.scriptEngine   = createScriptEngine();
		this.compiledScript = compileScript();
	}
	
	@Override
    protected Object eval(Object context, Map<String, Object> vars) {
		populateBindings(context, vars);
		try {
	        return runScript();
        } catch (Exception e) {
        	throw new ElException("Error eval script expression '" + scriptText + "', " + e.getMessage(), e);
        }
    }

	protected void populateBindings(Object context,Map<String,Object> vars) throws ExpressionException {
		ScriptContext scriptContext = scriptEngine.getContext();
		int scope = ScriptContext.ENGINE_SCOPE;
		
		if(null != context){
			scriptContext.setAttribute("context",context, scope);	
		}
		
		scriptContext.setBindings(new SimpleBindings(vars), scope);	
		
		/*
		if(null != vars){
			for(Entry<String,Object> entry : vars.entrySet()){
				scriptContext.setAttribute(entry.getKey(), entry.getValue(), scope);
			}
		}
		*/
	}
	
	protected Object runScript() throws Exception {
        if(null != compiledScript){
        	return compiledScript.eval();
        }else{
        	return scriptEngine.eval(scriptText);
        }
	}
	
    protected ScriptEngine createScriptEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine        engine  = null;
        try {
            engine = manager.getEngineByName(engineName);
        } catch (NoClassDefFoundError e) {
            log.error("Cannot load the script engine for " + engineName + ", please ensure correct JARs is provided on classpath.",e);
        }
        
        if (engine == null) {
            throw new IllegalArgumentException("No script engine could be created for: " + engineName);
        }
        
        /*
			The Jython JSR-223 engine defaults to compiling code in "exec" mode. 
			Changing this to "eval" mode allows Py.runCode() to return results to engine.eval() 
         */
        if (isPython()) {
            ScriptContext context = engine.getContext();
            context.setAttribute("com.sun.script.jython.comp.mode", "eval", ScriptContext.ENGINE_SCOPE);
        }
        
        return engine;
    }
    
    protected CompiledScript compileScript(){
    	if(scriptEngine instanceof Compilable){
    		try {
	            return ((Compilable)scriptEngine).compile(scriptText);
            } catch (ScriptException e) {
            	throw new ExpressionException("Script compile failed: " + e.getMessage(),e);
            }
    	}
    	return null;
    }
    
    protected boolean isPython() {
        return "python".equals(engineName) || "jython".equals(engineName);
    }
}
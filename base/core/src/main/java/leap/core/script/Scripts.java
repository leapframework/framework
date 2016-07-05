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
package leap.core.script;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Script utils
 */
public class Scripts {

	public static Object eval(ScriptEngine engine,String filename,String script) throws ScriptException{
		try{
			engine.put(ScriptEngine.FILENAME, filename);
			return engine.eval(script);	
		}finally{
			engine.put(ScriptEngine.FILENAME, null);	
		}
	}
	
	protected Scripts() {
		
	}

}

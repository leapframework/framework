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
package leap.htpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractHtplRenderable implements HtplRenderable {
	
	protected Map<String, Object> variables;
	
	public AbstractHtplRenderable() {

	}

	public AbstractHtplRenderable(Map<String, Object> variables) {
		this.variables = variables;
	}

	public void mergeVariables(Map<String, Object> variables){
		if(null != variables){
			getOrCreateVariables().putAll(variables);
		}
	}
	
	protected Map<String, Object> getOrCreateVariables(){
		if(null == variables){
			variables = new HashMap<String, Object>();
		}
		return variables;
	}
	
	@Override
	public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException {
		if(null == variables || variables.isEmpty()){
			doRender(tpl, context, writer);
		}else{
			try{
				context.pushLocalVariables(variables);
				doRender(tpl, context, writer);
			}finally{
				context.popLocalVariables();
			}
		}
	}
	
	protected abstract void doRender(HtplTemplate tpl,HtplContext context,HtplWriter writer) throws IOException;

}

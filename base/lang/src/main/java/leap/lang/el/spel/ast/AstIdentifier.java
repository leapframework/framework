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
package leap.lang.el.spel.ast;

import leap.lang.el.ElEvalContext;
import leap.lang.el.ElParseContext;

public class AstIdentifier extends AstExpr {

    private String name;
    private Object value;

    public AstIdentifier(){
    	
    }

    public AstIdentifier(ElParseContext context, String name){
        this.name  = name;
        this.value = context.resolveVariable(name); 
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void toString(StringBuilder buf) {
        buf.append(this.name);
    }

    @Override
    public Object eval(ElEvalContext context) {
    	Object v = context.resolveVariable(name);
    	
    	if(null != v){
    		return v;
    	}

    	if(context.isVariableResolved(name)){
    		return null;
    	}
    	
    	return value;
    }

	protected void doAccept(AstVisitor visitor) {
        visitor.startVisit(this);
        visitor.endVisit(this);
    }
}

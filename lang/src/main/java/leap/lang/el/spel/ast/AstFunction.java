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

import leap.lang.Strings;
import leap.lang.el.ElEvalContext;
import leap.lang.el.ElException;
import leap.lang.el.ElFunction;
import leap.lang.el.ElMethod;
import leap.lang.el.ElParseContext;

public class AstFunction extends AstInvocable {
	
	private String	   prefix;
	private String	   fullName;
	private ElFunction func;

	public AstFunction() {
	}
	
	public AstFunction(ElParseContext context, String name,AstExpr[] parameters) {
		this(context,null,name,parameters);
	}

	public AstFunction(ElParseContext context, String prefix, String name, AstExpr[] parameters) {
		super(name,parameters);
		this.prefix	  = prefix;
		this.fullName = Strings.isEmpty(prefix) ? name : (prefix + ":" + name);
		this.func     = context.resolveFunction(fullName);
	}
	
	public String getFullName(){
		return fullName;
	}

	public String getPrefix(){
		return prefix;
	}
	
	@Override
    public Object eval(ElEvalContext context) {
	    try {
	    	Object[] args = evalParameters(context);
	    	
	    	if(null == func){
    			if(null == prefix){
	    			Object root = context.getExternalContext();
	    			if(null != root){
	    				ElMethod m = context.resolveMethod(root, root.getClass(), name, args);
	    				if(null != m){
	    					return m.invoke(context, root, evalParameters(context));
	    				}
	    			}
    			}
	    		
	    		ElFunction func = context.resolveFunction(fullName);
	    		if(null == func){
	    			throw new ElException(context.getMessage("el.errors.noSuchFunction", fullName));
	    		}
	    		return func.invoke(context,args);
	    	}else{
	    		 return func.invoke(context,args);	
	    	}
	    } catch (ElException e){
	    	throw e;
        } catch (Throwable e) {
        	throw new ElException(context.getMessage("el.errors.functionInvokeError",fullName,e.getMessage()),e);
        }
    }

	@Override
    protected void doAccept(AstVisitor visitor) {
        if (visitor.startVisit(this)) {
            acceptChilds(visitor, this.parameters);
        }
        visitor.endVisit(this);
	}

	@Override
    public void toString(StringBuilder buf) {
		if(!Strings.isEmpty(prefix)){
			buf.append(prefix).append(':');
		}
	    super.toString(buf);
    }
}
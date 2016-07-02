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

import leap.lang.Beans;
import leap.lang.beans.BeanType;
import leap.lang.beans.DynaBean;
import leap.lang.el.ElEvalContext;
import leap.lang.el.ElException;
import leap.lang.el.ElPropertyResolver;
import leap.lang.el.ElTypes;

import java.util.Map;

public class AstItem extends AstExpr {
	private AstExpr array;
	private AstExpr index;

	public AstItem() {

	}

	public AstItem(AstExpr array, AstExpr index) {
		this.array = array;
		this.index = index;
	}

	public AstExpr getArray() {
		return array;
	}

	public void setArray(AstExpr array) {
		this.array = array;
	}

	public AstExpr getIndex() {
		return index;
	}

	public void setIndex(AstExpr index) {
		this.index = index;
	}
	
    @Override
    public Object eval(ElEvalContext context) {
		Object i = index.eval(context);
		if(null == i){
			throw new ElException(context.getMessage("el.errors.nullArrayIndex", this.toString()));
		}
		
		int type = ElTypes.resolveTypeByVal(i);
		if(type == ElTypes.STRING){
			Object a = null == array ? context.getRoot() : array.eval(context);
			if(null == a){
				return null;
			}
			return getProperty(context, a, (String)i);
		}else {
			Object a = null == array ? context.getRoot() : array.eval(context);
			if(null == a){
				throw new ElException(context.getMessage("el.errors.nullArrayValue", this.toString()));
			}
			return getArray(context, a, type, i);
		}
    }
    
    @SuppressWarnings("rawtypes")
    protected Object getProperty(ElEvalContext context, Object o, String name){
    	if(o instanceof Map){
    		return ((Map)o).get(name);
    	}
    	
    	if(o instanceof ElPropertyResolver){
    		return ((ElPropertyResolver) o).resovleProperty(name);
    	}
    	
    	if(o instanceof DynaBean){
    		return ((DynaBean)o).getProperty(name);
    	}
    	
    	return Beans.getProperty(BeanType.of(o.getClass()), o, name);
    }
	
	protected Object getArray(ElEvalContext context, Object a, int itype, Object ival){
		int index;
		
    	if(itype > 49){
    		index = ((Number)ival).intValue();
    	}else{
    		throw new ElException(context.getMessage("el.errors.invalidArrayIndex", this.toString(), "'" + ival + "' is not a vaid integer value"));
    	}
		
		return context.getArrayItem(a, index);
	}

	@Override
	protected void doAccept(AstVisitor visitor) {
		if (visitor.startVisit(this)) {
			acceptChild(visitor, array);
			acceptChild(visitor, index);
		}
		visitor.endVisit(this);
	}

	@Override
	public void toString(StringBuilder buf) {
		array.toString(buf);
		buf.append("[");
		index.toString(buf);
		buf.append("]");
	}
}

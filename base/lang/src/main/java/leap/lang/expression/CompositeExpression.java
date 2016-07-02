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
package leap.lang.expression;

import java.util.Map;

import leap.lang.Strings;


public class CompositeExpression extends AbstractExpression {
	
	public static final CompositeExpression NULL  = new CompositeExpression(null);
	public static final CompositeExpression EMPTY = new CompositeExpression("");
	
	private final String   string;
	private final Object[] nodes;
	
	public CompositeExpression(String string) {
		this.string = string;
		this.nodes  = null;
	}

	public CompositeExpression(String string,Object[] nodes) {
		this.string = string;
		this.nodes  = nodes;
	}
	
	public boolean isTextOnly() {
		return null == nodes;
	}
	
	@Override
    protected Object eval(Object context, Map<String, Object> vars) {
		if(null == nodes){
			return string;
		}
		
		if(nodes.length == 1){
			return evalNode(context, vars, nodes[0]);
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=0;i<nodes.length;i++){
			sb.append(evalNode(context, vars, nodes[i]));
		}
		
		return sb.toString();
	}
	
	protected String evalNode(Object context, Map<String, Object> vars, Object node){
		if(node instanceof String){
			return (String)node;
		}else{
			String string = ((Expression)node).getValue(String.class, context, vars);
			return string == null ? Strings.EMPTY : string;
		}
	}

	@Override
    public String toString() {
	    return string;
    }
}

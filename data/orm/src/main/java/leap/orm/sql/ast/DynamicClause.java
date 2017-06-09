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
package leap.orm.sql.ast;

import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.params.Params;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamicClause extends DynamicNode implements AstNodeContainer {

    private AstNode[]        bodyNodes;
    private NamedParamNode[] paramNodes;
    private boolean          tagOnly;
    private boolean          nullable;

    public DynamicClause(AstNode[] bodyNodes){
	    this(bodyNodes,null);
	}
	
    public DynamicClause(AstNode[] bodyNodes, Map<String,String> params) {
        this.bodyNodes = bodyNodes;

        resolveParamNodes();
        
        if(null != params) {
            String nullableValue = params.remove("nullable");
            if(!Strings.isEmpty(nullableValue)) {
               this.nullable = Converts.toBoolean(nullableValue, false); 
            }
        }

        //{? and @tagname{..} }
        if(paramNodes.length == 0 && bodyNodes.length >= 2) {
            if(bodyNodes[1] instanceof Tag) {
                tagOnly = true;
            }
        }
    }
	
	public AstNode[] getBodyNodes() {
		return bodyNodes;
	}

    @Override
    public void resolveDynamic(Appendable buf, Params params) {
        if(test(params)) {
            super.resolveDynamic(buf, params);
        }
    }

    @Override
    protected void toString_(Appendable buf) throws IOException {
		buf.append("{?");
		
		for(int i=0;i<bodyNodes.length;i++){
			bodyNodes[i].toString(buf);
		}
		
		if(nullable) {
		    buf.append(";nullable:true");
		}
		
		buf.append("}");
    }

	protected boolean test(Params params) {
		for(NamedParamNode n : paramNodes) {
		    if(nullable) {
		        if(!params.contains(n.getName())) {
		            return false;
		        }
		    }else {
                Object v = params.get(n.getName());
                if(null == v) {
                    return false;
                }
		    }
			/*
			if(v instanceof CharSequence && ((CharSequence) v).length() == 0) {
				return false;
			}
			*/
		}
		return true;
	}

	@Override
    protected void buildStatement_(SqlContext context, SqlStatementBuilder stm, Params params) throws IOException {
        if(!tagOnly) {
            if(!test(params)) {
                return;
            }

            for(AstNode n : bodyNodes) {
                n.buildStatement(context, stm, params);
            }
        }else{
            SqlStatementBuilder.SavePoint dynaPoint = stm.createSavePoint();

            for(AstNode n : bodyNodes) {
                if(n instanceof Tag) {
                    SqlStatementBuilder.SavePoint tagPoint = stm.createSavePoint();

                    n.buildStatement(context, stm, params);

                    if(!tagPoint.hasChanges()) {
                        dynaPoint.restore();
                        return;
                    }
                }else{
                    n.buildStatement(context, stm, params);
                }
            }
        }
    }

	@Override
	public AstNode[] getNodes() {
		return bodyNodes;
	}

	@Override
	public <T extends AstNode> T findLastNode(Class<T> type) {
		return AstUtils.findLastNode(bodyNodes, type);
	}

	protected void resolveParamNodes() {
		List<NamedParamNode> nodes = new ArrayList<NamedParamNode>();
		
		for(AstNode node : bodyNodes) {
			if(node instanceof NamedParamNode) {
				nodes.add((NamedParamNode)node);
			}
		}
		
		paramNodes = nodes.toArray(new NamedParamNode[nodes.size()]);
	}

}

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
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.params.Params;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamicClause extends DynamicNode implements AstNodeContainer {

    private static final Log log = LogFactory.get(DynamicClause.class);

    private AstNode[]        bodyNodes;
    private NamedParamNode[] paramNodes;
    private Tag              tagNode;
    private boolean          nullable;

    public DynamicClause(AstNode[] bodyNodes){
	    this(bodyNodes,null);
	}
	
    public DynamicClause(AstNode[] bodyNodes, Map<String,String> params) {
        this.bodyNodes  = bodyNodes;
        this.paramNodes = resolveParamNodes();
        
        if(null != params) {
            String nullableValue = params.remove("nullable");
            if(!Strings.isEmpty(nullableValue)) {
               this.nullable = Converts.toBoolean(nullableValue, false); 
            }
        }

        //{? and @tagname{..} }
        if(paramNodes.length == 0) {
            for(AstNode n : bodyNodes) {
                if(n instanceof Tag) {
                    tagNode = (Tag)n;
                    break;
                }
            }
        }
    }

    @Override
    public AstNode[] getNodes() {
        return bodyNodes;
    }

    public NamedParamNode[] getNamedParamNodes() {
        return paramNodes;
    }

    @Override
    public <T extends AstNode> T findLastNode(Class<T> type) {
        return AstUtils.findLastNode(bodyNodes, type);
    }

    @Override
    public void prepare(MetadataContext context, Sql sql) {
        for(AstNode node : bodyNodes) {
            node.prepare(context, sql);
        }
    }

    @Override
    public boolean resolveDynamic(SqlContext context, Sql sql, Appendable buf, Params params) throws IOException {
        if(test(params)) {
            StringBuilder s = new StringBuilder();

            for(AstNode n : bodyNodes) {
                if(!n.resolveDynamic(context, sql, s, params)) {
                    return false;
                }
            }

            buf.append(s);
            return true;
        }
        return false;
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
    protected void buildStatement_(SqlContext context, Sql sql, SqlStatementBuilder stm, Params params) throws IOException {

        if(null != tagNode){
            String s = tagNode.process(context, sql, params);
            if(Strings.isEmpty(s)) {
                if(log.isDebugEnabled()) {
                    log.debug("Tag {} -> (empty)", tagNode.toString());
                }
                return;
            }

            for(AstNode n : bodyNodes) {
                if(n == tagNode) {
                    tagNode.buildStatement(context, sql, stm, params, s);
                }else{
                    n.buildStatement(context, sql, stm, params);
                }
            }
        }else{
            if(!test(params)) {
                return;
            }

            for(AstNode n : bodyNodes) {
                n.buildStatement(context, sql, stm, params);
            }
        }

    }

	protected NamedParamNode[] resolveParamNodes() {
		List<NamedParamNode> nodes = new ArrayList<NamedParamNode>();
		
		for(AstNode node : bodyNodes) {
			if(node instanceof NamedParamNode) {
				nodes.add((NamedParamNode)node);
			}
		}
		
		return nodes.toArray(new NamedParamNode[nodes.size()]);
	}

}

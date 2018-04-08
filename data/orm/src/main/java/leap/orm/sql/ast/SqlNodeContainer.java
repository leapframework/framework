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

import leap.lang.params.Params;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.PreparedBatchSqlStatementBuilder;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;

import java.io.IOException;
import java.util.function.Function;

public class SqlNodeContainer extends SqlNode implements AstNodeContainer {

    private static final AstNode[] EMPTY_NODES = new AstNode[0];

	protected AstNode[] nodes = EMPTY_NODES;
	
	public SqlNodeContainer(){

	}
	
	public SqlNodeContainer(AstNode[] nodes){
		this.nodes = nodes;
	}
	
	public AstNode[] getNodes() {
		return nodes;
	}
	
	public void setNodes(AstNode[] nodes) {
		this.nodes = nodes;
	}

    @Override
    public void prepare(MetadataContext context, Sql sql) {
        if(null != nodes) {
            for(AstNode node : nodes) {
                node.prepare(context, sql);
            }
        }
    }

    @Override
    protected void toSql_(Appendable buf) throws IOException {
		for(int i=0;i<nodes.length;i++){
			nodes[i].toSql(buf);
		}
    }

	@Override
    protected void toString_(Appendable buf) throws IOException {
        if(null != nodes) {
            for(int i=0;i<nodes.length;i++){
                nodes[i].toString(buf);
            }
        }
    }

	@Override
	protected void buildStatement_(SqlContext context, Sql sql, SqlStatementBuilder stm, Params params) throws IOException {
	    for(int i=0;i<nodes.length;i++){
            AstNode node = nodes[i];
	    	node.buildStatement(context, sql, stm, params);
	    }
    }
	
	@Override
    protected void prepareBatchStatement_(SqlContext context, PreparedBatchSqlStatementBuilder stm,Object[] params) throws IOException {
		for(int i=0;i<nodes.length;i++) {
			nodes[i].prepareBatchStatement(context, stm,params);
		}
    }

	@Override
    public <T extends AstNode> T findLastNode(Class<T> type) {
	    return AstUtils.findLastNode(nodes, type);
    }

    @Override
    public boolean traverse(Function<AstNode, Boolean> visitor) {
        if(!visitor.apply(this)) {
            return false;
        }

        for(AstNode node : nodes) {
            if(!node.traverse(visitor)){
                return false;
            }
        }

        return true;
    }
}

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
package leap.orm.sql;

import leap.lang.Strings;
import leap.lang.annotation.Internal;
import leap.lang.params.Params;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.ast.AstNode;
import leap.orm.sql.ast.AstUtils;
import leap.orm.sql.ast.DynamicNode;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Internal
@SuppressWarnings("unchecked")
public class Sql {
	
	public enum ParseLevel {
        DYNA,
		BASE,
		MORE
	}
	
	public enum Type {
		INSERT,
		
		UPDATE,
		
		DELETE,
		
		SELECT,
		
		UNRESOLVED;
	}
	
	public enum Scope {
        UNKNOWN,
		
		SELECT_LIST,
		
		WHERE,
		
		ORDER_BY,
		
		STRING
	}

	private final Type      type;
	private final AstNode[] nodes;
	
	public Sql(Type type, AstNode[] nodes){
		this.type  = type;
		this.nodes = nodes;
	}
	
	public Type type(){
		return type;
	}
	
	public AstNode[] nodes(){
		return nodes;
	}
	
	public boolean isEmpty(){
		return null == nodes || nodes.length == 0;
	}
	
    public <T extends AstNode> T firstNode(){
		return (T)nodes[0];
	}
	
	public <T extends AstNode> T lastNode(){
		return (T)nodes[nodes.length-1];
	}

	public boolean isSelect(){
		return type == Type.SELECT;
	}
	
	public boolean isInsert(){
		return type == Type.INSERT;
	}
	
	public boolean isUpdate(){
		return type == Type.UPDATE;
	}
	
	public boolean isDelete(){
		return type == Type.DELETE;
	}
	
	public boolean isUnresolved(){
		return type == Type.UNRESOLVED;
	}

    public boolean isDynamic() {
        AtomicBoolean b = new AtomicBoolean();

        traverse(n -> {
            if(n instanceof DynamicNode) {
                b.set(true);
                return false;
            }
            return true;
        });

        return b.get();
    }
	
	public void buildStatement(SqlContext context, SqlStatementBuilder stm,Params params){
		for(int i=0;i<nodes.length;i++){
			AstNode node = nodes[i];
			node.buildStatement(context, this, stm, params);
		}
	}

    public void prepare(MetadataContext context) {
        for(AstNode node : nodes) {
            node.prepare(context, this);
        }
    }
	
	public void prepareBatchSqlStatement(SqlContext context, PreparedBatchSqlStatementBuilder stm,Object[] params) {
		for(int i=0;i<nodes.length;i++) {
			nodes[i].prepareBatchStatement(context, stm,params);
		}
	}
	
	/*
	public void resolveBatchParameters(Out<String> sql,Out<BatchSqlParameter> params) {
		List<BatchSqlParameter> params = new ArrayList<BatchSqlParameter>();
		
		for(int i=0;i<nodes.length;i++) {
			nodes[i].resolveBatchParameters(params);
		}
		
		return params.toArray(new BatchSqlParameter[params.size()]);
	}
	*/
	
	public <T extends AstNode> T findFirstNode(Class<T> type){
		return AstUtils.findFirstNode(nodes, type);
	}
	
    public <T extends AstNode> T findLastNode(Class<T> type){
    	return AstUtils.findLastNode(nodes, type);
	}
    
    public String toSql() {
		StringBuilder sb = new StringBuilder();

		for(int i=0;i<nodes.length;i++){
			nodes[i].toSql(sb);
		}
		
		return Strings.trim(sb.toString());
    }

    public String resolveDynamicSql(Params params) {
        StringBuilder sb = new StringBuilder();

        for(int i=0;i<nodes.length;i++) {
            nodes[i].resolveDynamic(sb, params);
        }

        return Strings.trim(sb.toString());
    }
	
	@Override
    public String toString() {
		StringBuilder sb = new StringBuilder();

		for(int i=0;i<nodes.length;i++){
			nodes[i].toString(sb);
		}
		
		return Strings.trim(sb.toString());
	}

    public void traverse(Function<AstNode, Boolean> visitor) {
        for(AstNode node : nodes) {
            if(!node.traverse(visitor)){
                return;
            }
        }
    }
}

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

import leap.lang.Exceptions;
import leap.lang.annotation.Internal;
import leap.lang.params.Params;
import leap.orm.sql.PreparedBatchSqlStatementBuilder;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;

import java.io.IOException;
import java.util.function.Function;

@Internal
public abstract class AstNode {

	public final void toString(Appendable buf) {
		try {
	        toString_(buf);
        } catch (IOException e) {
        	Exceptions.wrapAndThrow(e);
        }
	}
	
	public final void toSql(Appendable buf) {
		try {
	        toSql_(buf);
        } catch (IOException e) {
        	Exceptions.wrapAndThrow(e);
        }
	}
	
	public final void buildStatement(SqlStatementBuilder stm,Params params) {
		try {
			buildStatement_(stm, params);
        } catch (IOException e) {
        	Exceptions.wrapAndThrow(e);
        }
	}
	
	public final void prepareBatchStatement(SqlContext context, PreparedBatchSqlStatementBuilder stm) {
		try {
			prepareBatchStatement_(context, stm);
        } catch (IOException e) {
        	Exceptions.wrapAndThrow(e);
        }
    }
	
	@SuppressWarnings("unchecked")
    public <T extends AstNode> T as(){
		return (T)this;
	}
	
	@SuppressWarnings("unchecked")
    public <T extends AstNode> T as(Class<T> type){
		return (T)this;
	}

	@Override
    public String toString() {
		StringBuilder sb = new StringBuilder();

		toString(sb);

		return sb.toString();
    }
	
    protected void toSql_(Appendable buf) throws IOException {
		toString_(buf);
    }

    /**
     * Returns true if continue.
     */
    public boolean traverse(Function<AstNode, Boolean> visitor) {
        return visitor.apply(this);
    }
	
	protected abstract void toString_(Appendable buf) throws IOException;
	
	protected abstract void buildStatement_(SqlStatementBuilder stm,Params params) throws IOException;
	
	protected abstract void prepareBatchStatement_(SqlContext context, PreparedBatchSqlStatementBuilder stm) throws IOException;

}
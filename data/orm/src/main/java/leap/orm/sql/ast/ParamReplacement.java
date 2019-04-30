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
import leap.orm.sql.*;
import leap.orm.sql.Sql.Scope;

import java.io.IOException;

public class ParamReplacement extends NamedParamNode {
	
	public ParamReplacement(Scope scope, String name) {
		super(scope, name);
    }
	
	@Override
    public boolean isReplace() {
	    return true;
    }

    @Override
    public boolean resolveDynamic(SqlContext context, Sql sql, Appendable buf, Params params) throws IOException {
        Object v = params.get(name);
        if(null != v) {
            String s = v.toString();
            if(s.length() > 0) {
                buf.append(s);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void prepareBatchStatement_(SqlContext context, PreparedBatchSqlStatementBuilder stm,Object[] params) throws IOException {
		throw new SqlClauseException("Batch executing sql cannot use Replacement Parameter [" + this + "]");	    
    }

	@Override
    protected Object getParameterValue(SqlStatementBuilder stm, Params params) {
	    return params.get(name);
    }

	@Override
	protected void toString_(Appendable buf) throws IOException {
		buf.append('$').append(name).append('$');
    }
}
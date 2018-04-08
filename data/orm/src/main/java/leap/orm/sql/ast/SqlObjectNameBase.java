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

import java.io.IOException;

import leap.db.DbDialect;
import leap.lang.params.Params;
import leap.orm.sql.PreparedBatchSqlStatementBuilder;
import leap.orm.sql.Sql;
import leap.orm.sql.Sql.Scope;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;

public abstract class SqlObjectNameBase extends SqlNode {

    protected boolean quoted;
	protected Scope   scope;
	protected String  firstName;
	protected String  secondaryName;
	protected String  lastName;
	
	public boolean isQuoted() {
        return quoted;
    }

    public void setQuoted(boolean quoted) {
        this.quoted = quoted;
    }

    public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getSecondaryName() {
		return secondaryName;
	}

	public void setSecondaryName(String secondaryName) {
		this.secondaryName = secondaryName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getSecondaryOrFirstName(){
		return null != secondaryName ? secondaryName : firstName;
	}
	
	public boolean isLastNameOnly() {
		return null == firstName && null == lastName;
	}

	@Override
    protected void buildStatement_(SqlContext context, Sql sql, SqlStatementBuilder stm, Params params) throws IOException {
	    if(quoted) {
	        super.buildStatement_(context, sql, stm, params);
	    } else{
	        toString(stm, stm.dialect());    
	    }
	}
	
    @Override
    protected void prepareBatchStatement_(SqlContext context, PreparedBatchSqlStatementBuilder stm,Object[] params) throws IOException {
        if(quoted) {
            super.prepareBatchStatement_(context, stm,params);
        }else{
            toString(stm, context.dialect());    
        }
    }

    @Override
    protected void toString_(Appendable buf) throws IOException {
		if(null != firstName){
			buf.append(firstName).append('.');
		}
		
		if(null != secondaryName){
			buf.append(secondaryName).append('.');
		}
		
		buf.append(lastName);
    }
    
    protected void toString(Appendable buf, DbDialect dialect) throws IOException {
        if(null != firstName){
            buf.append(firstName).append('.');
        }
        
        if(null != secondaryName){
            buf.append(secondaryName).append('.');
        }
        
        appendLastName(buf, dialect);
    }
    
    protected void appendLastName(Appendable buf, DbDialect dialect) throws IOException {
        buf.append(dialect.quoteIdentifier(lastName, true));
    }
}
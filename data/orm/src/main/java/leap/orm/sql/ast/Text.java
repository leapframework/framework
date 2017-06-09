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
import leap.lang.params.Params;
import leap.orm.sql.PreparedBatchSqlStatementBuilder;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;

import java.io.IOException;

/**
 * Indicates a text segment in a sql text.
 */
public class Text extends AstNode {
	
	private final StringBuilder buf = new StringBuilder(16);
	
	public Text(String text){
		this.buf.append(text);
	}
	
    public Text append(String s) {
        buf.append(s);
        return this;
    }

    public boolean isBlank() {
        return Strings.isBlank(buf);
    }

	@Override
    protected void toString_(Appendable buf) throws IOException {
		buf.append(this.buf);
    }

	@Override
	protected void buildStatement_(SqlContext context, SqlStatementBuilder stm, Params params) throws IOException {
		stm.append(buf);
    }
	
	@Override
    protected void prepareBatchStatement_(SqlContext context, PreparedBatchSqlStatementBuilder stm,Object[] params) throws IOException {
		stm.append(buf);
    }

	@Override
    public String toString() {
		return buf.toString();
    }
}
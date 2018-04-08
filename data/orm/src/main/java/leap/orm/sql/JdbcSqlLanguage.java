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

import java.util.ArrayList;
import java.util.List;

import leap.orm.metadata.MetadataContext;
import leap.orm.sql.parser.SqlParser;

public class JdbcSqlLanguage implements SqlLanguage {

	@Override
    public List<SqlClause> parseClauses(MetadataContext context, String text, Options options) throws SqlClauseException {
		List<String> sqls = SqlParser.split(text);
		
		List<SqlClause> clauses = new ArrayList<SqlClause>();
		
		for(int i=0;i<sqls.size();i++){
			clauses.add(new JdbcSqlClause(sqls.get(i)));
		}
		
	    return clauses;
    }

	@Override
    public SqlClause parseClause(MetadataContext context, String sql, Options options) throws SqlClauseException {
	    return new JdbcSqlClause(sql);
    }


}
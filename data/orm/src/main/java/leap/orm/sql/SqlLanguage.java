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

import java.util.List;

import leap.orm.metadata.MetadataContext;

public interface SqlLanguage {

    interface Options {

        Options EMPTY = new Options() {};

        default Boolean getWhereFieldsEnabled() {
            return null;
        }

        default Boolean getQueryFilterEnabled() {
            return null;
        }
    }

    default List<SqlClause> parseClauses(MetadataContext context, String text) throws SqlClauseException {
        return parseClauses(context, text, Options.EMPTY);
    }
	
	List<SqlClause> parseClauses(MetadataContext context, String text, Options options) throws SqlClauseException;

    default SqlClause parseClause(MetadataContext context,String sql) throws SqlClauseException {
        return parseClause(context, sql, Options.EMPTY);
    }

	SqlClause parseClause(MetadataContext context,String sql, Options options) throws SqlClauseException;

}
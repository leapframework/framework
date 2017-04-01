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

import leap.orm.mapping.EntityMapping;
import leap.orm.metadata.MetadataContext;

public interface SqlFactory {
	
	SqlCommand createSqlCommand(MetadataContext context, String sql);
	
	SqlCommand createSqlCommand(MetadataContext context,String source,String sql);

	SqlCommand createInsertCommand(MetadataContext context,EntityMapping em);
	
	SqlCommand createInsertCommand(MetadataContext context,EntityMapping em,String[] fields);
	
	SqlCommand createUpdateCommand(MetadataContext context,EntityMapping em);
	
	SqlCommand createUpdateCommand(MetadataContext context,EntityMapping em,String[] fields);
	
	SqlCommand createDeleteCommand(MetadataContext context,EntityMapping em);
	
	SqlCommand createDeleteAllCommand(MetadataContext context,EntityMapping em);
	
	SqlCommand createExistsCommand(MetadataContext context,EntityMapping em);
	
	SqlCommand createCountCommand(MetadataContext context,EntityMapping em);
	
	SqlCommand createFindCommand(MetadataContext context,EntityMapping em);
	
	SqlCommand createFindListCommand(MetadataContext context,EntityMapping em);
	
	SqlCommand createFindAllCommand(MetadataContext context,EntityMapping em);

    /**
     * returns [alias.]col1,[alias].col2,...
     */
    String createSelectColumns(MetadataContext context, EntityMapping em, String tableAlias);
}
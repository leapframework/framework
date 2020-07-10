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

import java.util.Set;

public interface SqlFactory {

    /**
     * Creates a normal {@link SqlCommand} by the given sql.
     */
	SqlCommand createSqlCommand(MetadataContext context, String sql);

    /**
     * Creates a native jdbc {@link SqlCommand} by the given sql.
     */
    SqlCommand createNativeCommand(MetadataContext context, String sql);

    /**
     * Creates an insert {@link SqlCommand} with all the fields in primary table of the given entity.
     */
	default SqlCommand createInsertCommand(MetadataContext context,EntityMapping em) {
        return createInsertCommand(context, em, false);
    }

    /**
     * Creates an insert {@link SqlCommand} with all the fields in primary or secondary table of the given entity.
     */
    SqlCommand createInsertCommand(MetadataContext context,EntityMapping em, boolean secondary);

    /**
     * Creates an insert {@link SqlCommand} with the fields in primary table of the given entity.
     */
	default SqlCommand createInsertCommand(MetadataContext context,EntityMapping em,String[] fields) {
        return createInsertCommand(context, em, fields, false);
    }

    /**
     * Creates an insert {@link SqlCommand} with the fields in primary or secondary table of the given entity.
     */
    SqlCommand createInsertCommand(MetadataContext context,EntityMapping em,String[] fields, boolean secondary);

    /**
     * Creates an update {@link SqlCommand} with all the fields in primary table of the given entity.
     */
	default SqlCommand createUpdateCommand(MetadataContext context,EntityMapping em) {
        return createUpdateCommand(context, em, false);
    }

    /**
     * Creates an update {@link SqlCommand} with all the fields in primary or secondary table of the given entity.
     */
    SqlCommand createUpdateCommand(MetadataContext context,EntityMapping em, boolean secondary);

    /**
     * Creates an update {@link SqlCommand} with the fields in primary table of the given entity.
     *
     * <p/>
     * Returns <code>null</code> if no updated fields.
     */
	default SqlCommand createUpdateCommand(MetadataContext context,EntityMapping em,String[] fields) {
        return createUpdateCommand(context, em, fields, false);
    }

    /**
     * Creates an update {@link SqlCommand} with the fields in primary or secondary table of the given entity.
     *
     * <p/>
     * Returns <code>null</code> if no updated fields.
     */
    SqlCommand createUpdateCommand(MetadataContext context,EntityMapping em,String[] fields, boolean secondary);

    /**
     * Creates a delete {@link SqlCommand} with the id in primary table of the given entity.
     */
	default SqlCommand createDeleteCommand(MetadataContext context,EntityMapping em) {
        return createDeleteCommand(context, em, false);
    }

    /**
     * Creates a delete {@link SqlCommand} with th eid in primary or secondary table of the given entity.
     */
    SqlCommand createDeleteCommand(MetadataContext context, EntityMapping em, boolean secondary);

    /**
     * Creates a delete {@link SqlCommand} for all records in primary table of the given entity.
     */
	default SqlCommand createDeleteAllCommand(MetadataContext context,EntityMapping em) {
        return createDeleteAllCommand(context, em, false);
    }

    /**
     * Creates a delete {@link SqlCommand} for all records in primary or secondary table of the given entity.
     */
    SqlCommand createDeleteAllCommand(MetadataContext context,EntityMapping em, boolean secondary);

    /**
     * Creates an exists {@link SqlCommand} with id in primary table of the given entity.
     */
	SqlCommand createExistsCommand(MetadataContext context,EntityMapping em);

    /**
     * Creates an exists {@link SqlCommand} for all records in primary table of the given entity.
     */
    SqlCommand createCountCommand(MetadataContext context,EntityMapping em);

    /**
     * Creates a select one {@link SqlCommand} in primary table and secondary table (if exists) of the given entity.
     */
    SqlCommand createFindCommand(MetadataContext context,EntityMapping em);

    /**
     * todo: doc
     */
	SqlCommand createFindListCommand(MetadataContext context,EntityMapping em);

    /**
     * todo: doc
     */
	SqlCommand createFindAllCommand(MetadataContext context, EntityMapping em);

    /**
     * returns [alias.]col1,[alias].col2,...
     */
    default String createSelectColumns(MetadataContext context, EntityMapping em, String alias) {
        return createSelectColumns(context, em, alias, null);
    }

    /**
     * returns [alias.]col1,[alias].col2,...
     */
    String createSelectColumns(MetadataContext context, EntityMapping em, String alias, Set<String> excludes);
}
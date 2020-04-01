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
package leap.orm;

import leap.lang.exception.ObjectNotFoundException;
import leap.lang.expression.Expression;
import leap.lang.jdbc.JdbcType;
import leap.lang.naming.NamingStyles;

import java.util.Map;
import java.util.Set;

public interface OrmConfig {

    String KEY_PREFIX = "orm";

    /**
     * Returns true if auto create tables at startup.
     *
     * <p/>
     * Default is false.
     */
    boolean isAutoCreateTables();

    /**
     * Returns true if auto mapping db tables to entities.
     */
    boolean isAutoMappingTables();

    /**
     * Returns true if read db schema at startup.
     *
     * <p/>
     * Default is true.
     */
    boolean isReadDbSchema();

	/**
	 * Returns true if auto generate fields for {@link leap.orm.model.Model}.
     *
     * <p/>
     * Default is true.
	 *
	 * <p/>
	 * See {@link #getAutoGeneratedFieldNames()}.
	 */
	boolean isAutoGenerateColumns();

	/**
	 * Returns the names of auto generated fields for {@link leap.orm.model.Model}.
	 */
	Set<String> getAutoGeneratedFieldNames();

	/**
	 * Returns true if auto generate optimistic lock for {@link leap.orm.model.Model}.
     *
     * <p/>
     * Default is true.
	 */
	boolean isAutoGenerateOptimisticLock();

	/**
	 * Returns true if allow model cross orm context, else false.
	 * <p/>
	 * Default is false
	 */
	boolean isModelCrossContext();

    /**
     * Returns true if allow mapping class's simple name as entity name
     *
     * <p/>
     * Default is false.
     */
    boolean isMappingClassSimpleName();

    /**
     * Returns true if the field at class must be declared explicitly.
     *
     * <p/>
     * Default is false.
     */
    boolean isMappingFieldExplicitly();

    /**
     * Returns true if auto mapping table name with acronym.
     *
     * <p/>
     * Example:
     *
     * 'User' entity will auto mapping table 'sys_user'.
     */
    boolean isAutoMappingTableWithAcronym();

    /**
     * Returns true if the dao events is enabled by default.
     */
    boolean isEventsDefaultEnabled();

    /**
     * Returns true if enables filter column(s).
     */
    default boolean isFilterColumnEnabled() {
        return getFilterColumnConfig().isEnabled();
    }

    /**
     * Returns the {@link FilterColumnConfig}.
     */
    FilterColumnConfig getFilterColumnConfig();

    /**
     * Returns true if enables query filter.
     */
    default boolean isQueryFilterEnabled() {
        return getQueryFilterConfig().isEnabled();
    }

    /**
     * Returns the {@link QueryFilterConfig}.
     */
    QueryFilterConfig getQueryFilterConfig();

    /**
     * Returns the {@link EmbeddedColumnConfig}.
     */
    EmbeddedColumnConfig getEmbeddedColumnConfig();

    /**
     * zero means no limitation.
     */
    long getDefaultMaxResults();

    /**
     * Returns the field name of optimistic lock column.
     */
    String getOptimisticLockFieldName();

	/**
	 * The naming style of table.
	 * 
	 * @see NamingStyles
	 */
	String getTableNamingStyle();
	
	/**
	 * The naming style of column.
	 * 
	 * @see NamingStyles
	 */
	String getColumnNamingStyle();

    /**
     * Returns true if should convert to bean property's type at read map.
     */
	boolean isConvertPropertyForReadMap();

    /**
     * Returns the format name of default serializer.
     */
    String getDefaultSerializer();

    /**
     * Required. Returns the default serialize configuration.
     */
    SerializeConfig getDefaultSerializeConfig();

    /**
     * Returns the {@link SerializeConfig} of the name.
     *
     * @throws ObjectNotFoundException if the name not exists.
     */
    SerializeConfig getSerializeConfig(String name) throws ObjectNotFoundException;

    /**
     * Returns an immutable map contains all the serialize configurations.
     *
     * <p/>
     * The returned key is is the serialize format.
     */
    Map<String, SerializeConfig> getSerializeConfigs();

    /**
     * The serialize config interface.
     */
    interface SerializeConfig {

        /**
         * Required. Returns the default jdbc type of column which use this serialize format.
         */
        JdbcType getDefaultColumnType();

        /**
         * Required. Returns the default length of column which use this serialize format.
         */
        Integer getDefaultColumnLength();

    }

    /**
     * todo : doc
     */
    interface QueryFilterConfig {

        boolean isEnabled();

        String getTagName();

        String getAlias();
    }

    /**
     * todo : doc
     */
    interface FilterColumnConfig {

        /**
         * Default is <code>true</code>
         */
        boolean isEnabled();

        /**
         * Optional.
         */
        Expression getFilteredIf();

    }

    interface EmbeddedColumnConfig {

        /**
         * Returns the default column name.
         */
        String getName();

        /**
         * Returns the default column type name.
         */
        String getType();

        /**
         * Returns the default column length.
         */
        Integer getLength();
    }
}
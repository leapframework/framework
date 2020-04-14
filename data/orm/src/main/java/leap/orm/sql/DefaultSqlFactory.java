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

import leap.core.annotation.Inject;
import leap.db.DbDialect;
import leap.db.model.DbColumn;
import leap.db.support.JsonColumnSupport;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.annotation.Nullable;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.metadata.MetadataContext;

import java.util.ArrayList;
import java.util.List;

public class DefaultSqlFactory implements SqlFactory {

    private static final Log log = LogFactory.get(DefaultSqlFactory.class);

    @Inject
    protected SqlLanguage defaultSqlLanguage;

    @Inject("jdbc")
    protected SqlLanguage jdbcSqlLanguage;

    @Override
    public SqlCommand createSqlCommand(MetadataContext context, String sql) {
        return createCommand(context, null, null, sql);
    }

    @Override
    public SqlCommand createNativeCommand(MetadataContext context, String sql) {
        return createCommand(context, null, null, sql, jdbcSqlLanguage);
    }

    @Override
    public SqlCommand createInsertCommand(MetadataContext context, EntityMapping em, boolean secondary) {
        String sql = getInsertSql(context, em, secondary);
        return null == sql ? null : createCommand(context, em, null, sql);
    }

    @Override
    public SqlCommand createInsertCommand(MetadataContext context, EntityMapping em, String[] fields, boolean secondary) {
        if (null == fields || fields.length == 0) {
            return createInsertCommand(context, em, secondary);
        } else {
            return createCommand(context, em, null, getInsertSql(context, em, fields, secondary));
        }
    }

    public SqlCommand createUpdateCommand(MetadataContext context, EntityMapping em, boolean secondary) {
        String sql = getUpdateSql(context, em, secondary);
        return null == sql ? null : createCommand(context, em, null, sql);
    }

    @Override
    public SqlCommand createUpdateCommand(MetadataContext context, EntityMapping em, String[] fields, boolean secondary) {
        if (null == fields || fields.length == 0) {
            return createUpdateCommand(context, em, secondary);
        }

        String sql = getUpdateSql(context, em, fields, secondary);
        return null == sql ? null : createCommand(context, em, null, sql);
    }

    @Override
    public SqlCommand createDeleteCommand(MetadataContext context, EntityMapping em, boolean secondary) {
        return createCommand(context, em, null, getDeleteSql(context, em, secondary));
    }

    @Override
    public SqlCommand createDeleteAllCommand(MetadataContext context, EntityMapping em, boolean secondary) {
        return createCommand(context, em, null, getDeleteAllSql(context, em, secondary));
    }

    @Override
    public SqlCommand createExistsCommand(MetadataContext context, EntityMapping em) {
        return createCommand(context, em, null, getExistsSql(context, em));
    }

    @Override
    public SqlCommand createCountCommand(MetadataContext context, EntityMapping em) {
        return createCommand(context, em, null, getCountSql(context, em));
    }

    @Override
    public SqlCommand createFindCommand(MetadataContext context, EntityMapping em) {
        return createCommand(context, em, null, getFindSql(context, em));
    }

    @Override
    public SqlCommand createFindListCommand(MetadataContext context, EntityMapping em) {
        return createCommand(context, em, null, getFindListSql(context, em));
    }

    @Override
    public SqlCommand createFindAllCommand(MetadataContext context, EntityMapping em) {
        return createCommand(context, em, null, getFindAllSql(context, em));
    }

    protected SqlCommand createCommand(MetadataContext context, @Nullable EntityMapping em, String source, String sql) {
        return createCommand(context, em, source, sql, defaultSqlLanguage);
    }

    protected SqlCommand createCommand(MetadataContext context, @Nullable EntityMapping em, String source, String sql, SqlLanguage lang) {
        Args.notEmpty(sql, "sql");
        return new DefaultSqlCommand(new SqlInfo.Builder(source, source, null, lang, sql, null).build());
    }

    protected String getInsertSql(MetadataContext context, EntityMapping em, boolean secondary) {
        checkSecondary(em, secondary);

        StringBuilder sql    = new StringBuilder();
        StringBuilder values = new StringBuilder();

        sql.append("insert into ").append(secondary ? em.getSecondaryTableName() : em.getEntityName()).append("(");

        final List<FieldMapping> embedded = new ArrayList<>();

        int index = 0;
        for (FieldMapping fm : em.getFieldMappings()) {
            if (fm.isInsert()) {

                if (!secondary && fm.isSecondary()) {
                    continue;
                }

                if (secondary && !(fm.isPrimaryKey() || fm.isSecondary())) {
                    continue;
                }

                if (fm.isEmbedded()) {
                    embedded.add(fm);
                    continue;
                }

                if (index > 0) {
                    sql.append(",");
                    values.append(",");
                }
                parseFieldInsertSql(sql, values, fm, context);
                index++;
            }
        }

        addInsertEmbeddedColumn(context, em, embedded, index, sql, values);

        if (index == 0) {
            log.warn("Cannot create insert sql for entity '{}' : no insert columns", em.getEntityName());
            return null;
        }

        sql.append(") values (").append(values).append(")");

        return sql.toString();
    }

    protected void checkSecondary(EntityMapping em, boolean secondary) {
        if (secondary && !em.hasSecondaryTable()) {
            throw new IllegalStateException("Entity '" + em + "' has no secondary table");
        }
    }

    protected String getInsertSql(MetadataContext context, EntityMapping em, String[] fields, boolean secondary) {
        checkSecondary(em, secondary);

        StringBuilder sql    = new StringBuilder();
        StringBuilder values = new StringBuilder();

        String tableName = secondary ? em.getSecondaryTableName() : em.getEntityName();

        sql.append("insert into ").append(tableName).append("(");

        int index = 0;

        final List<FieldMapping> embedded = new ArrayList<>();

        for (FieldMapping fm : em.getFieldMappings()) {
            if (!fm.isInsert()) {
                continue;
            }

            if (!fm.isPrimaryKey()) {
                if (!secondary && fm.isSecondary()) {
                    continue;
                }

                if (secondary && !fm.isSecondary()) {
                    continue;
                }
            }

            if (fm.isEmbedded()) {
                embedded.add(fm);
                continue;
            }

            boolean contains = false;
            for (String field : fields) {
                if (Strings.equalsIgnoreCase(field, fm.getFieldName())) {
                    contains = true;
                    break;
                }
            }

            if (contains || fm.isAutoGenerateValue()) {
                if (index > 0) {
                    sql.append(",");
                    values.append(",");
                }
                parseFieldInsertSql(sql, values, fm, context);
                index++;
            }
        }

        addInsertEmbeddedColumn(context, em, embedded, index, sql, values);

        sql.append(") values (").append(values).append(")");

        return sql.toString();
    }

    protected void parseFieldInsertSql(StringBuilder sql, StringBuilder values, FieldMapping fm, MetadataContext context) {
        DbDialect dialect = context.getDb().getDialect();

        DbColumn column = fm.getColumn();

        sql.append(dialect.quoteIdentifier(column.getName()));

        if (!Strings.isEmpty(fm.getSequenceName())) {
            values.append(dialect.getNextSequenceValueSqlString(fm.getSequenceName()));
        } else {
            values.append("#").append(fm.getFieldName()).append("#");
        }
    }

    protected String getUpdateSql(MetadataContext context, EntityMapping em, boolean secondary) {
        checkSecondary(em, secondary);

        DbDialect dialect = context.getDb().getDialect();

        StringBuilder sql = new StringBuilder();

        sql.append("update ").append(secondary ? em.getSecondaryTableName() : em.getEntityName()).append(" set ");

        int index = 0;

        final List<FieldMapping> embedded = new ArrayList<>();

        for (FieldMapping fm : em.getFieldMappings()) {
            if (fm.isUpdate() && !fm.isPrimaryKey()) {

                if (!secondary && fm.isSecondary()) {
                    continue;
                }

                if (secondary && !fm.isSecondary()) {
                    continue;
                }

                if (fm.isEmbedded()) {
                    embedded.add(fm);
                    continue;
                }

                if (index > 0) {
                    sql.append(",");
                }

                parseFieldUpdateSql(sql, context, fm);

                index++;
            }
        }

        if (embedded.size() > 0) {
            addUpdateEmbeddedColumn(context, em, embedded, index, sql);
            index++;
        }

        if (index == 0) {
            log.warn("Cannot create update sql for entity '{}': no update columns", em.getEntityName());
            return null;
        }

        sql.append(" where ");

        appendPrimaryKey(context, em, sql);

        index = 0;
        FieldMapping lp = em.getOptimisticLockField();
        if (null != lp) {
            if (index > 0) {
                sql.append(" and");
            }
            sql.append(" ").append(dialect.quoteIdentifier(lp.getColumnName())).append("=#").append(lp.getFieldName()).append("#");
        }

        return sql.toString();
    }

    protected String getUpdateSql(MetadataContext context, EntityMapping em, String[] fields, boolean secondary) {
        checkSecondary(em, secondary);

        DbDialect dialect = context.getDb().getDialect();

        StringBuilder sql = new StringBuilder();

        sql.append("update ").append(secondary ? em.getSecondaryTableName() : em.getEntityName()).append(" set ");

        int index = 0;

        final List<FieldMapping> embedded = new ArrayList<>();

        for (FieldMapping fm : em.getFieldMappings()) {

            if (fm.isPrimaryKey() || !fm.isUpdate()) {
                continue;
            }

            if (!secondary && fm.isSecondary()) {
                continue;
            }

            if (secondary && !fm.isSecondary()) {
                continue;
            }

            boolean contains = false;
            for (String field : fields) {
                if (Strings.equalsIgnoreCase(field, fm.getFieldName())) {
                    contains = true;
                    break;
                }
            }

            if (contains && fm.isEmbedded()) {
                embedded.add((fm));
                continue;
            }

            if (contains || fm.isOptimisticLock()) {
                if (index > 0) {
                    sql.append(",");
                }

                parseFieldUpdateSql(sql, context, fm);

                index++;
            }
        }

        if (embedded.size() > 0) {
            addUpdateEmbeddedColumn(context, em, embedded, index, sql);
            index++;
        }

        if (index == 0) {
            return null;
        }

        sql.append(" where ");

        appendPrimaryKey(context, em, sql);

        index = 0;
        FieldMapping lp = em.getOptimisticLockField();
        if (null != lp) {
            if (index > 0) {
                sql.append(" and");
            }
            sql.append(" ").append(dialect.quoteIdentifier(lp.getColumnName())).append("=#").append(lp.getFieldName()).append("#");
            index++;
        }

        return sql.toString();
    }

    protected void parseFieldUpdateSql(StringBuilder sql, MetadataContext context, FieldMapping fm) {
        DbDialect dialect = context.getDb().getDialect();

        sql.append(dialect.quoteIdentifier(fm.getColumnName())).append("=#");

        if (fm.isOptimisticLock()) {
            sql.append(fm.getNewOptimisticLockFieldName());
        } else {
            sql.append(fm.getFieldName());
        }

        sql.append("#");
    }

    protected String getDeleteSql(MetadataContext context, EntityMapping em, boolean secondary) {
        checkSecondary(em, secondary);

        DbDialect dialect = context.getDb().getDialect();

        StringBuilder sql = new StringBuilder();

        sql.append("delete from ").append(secondary ? em.getSecondaryTableName() : em.getEntityName()).append(" where ");

        appendPrimaryKey(context, em, sql);

        return sql.toString();
    }

    protected String getDeleteAllSql(MetadataContext context, EntityMapping em, boolean secondary) {
        checkSecondary(em, secondary);

        StringBuilder sql = new StringBuilder();

        sql.append("delete from ").append(secondary ? em.getSecondaryTableName() : em.getEntityName());

        return sql.toString();
    }

    protected String getExistsSql(MetadataContext context, EntityMapping em) {
        StringBuilder sql = new StringBuilder();

        sql.append("select 1 from ").append(em.getEntityName()).append(" where ");

        appendPrimaryKey(context, em, sql);

        return sql.toString();
    }

    protected String getCountSql(MetadataContext context, EntityMapping em) {
        StringBuilder sql = new StringBuilder();

        sql.append("select count(*) from ").append(em.getEntityName());

        return sql.toString();
    }

    protected String getFindSql(MetadataContext context, EntityMapping em) {
        StringBuilder sql = new StringBuilder();

        sql.append("select ");
        sql.append(createSelectColumns(context, em, null))
                .append(" from ")
                .append(em.getEntityName())
                .append(" t ");
        sql.append(" where ");

        appendPrimaryKey(context, em, sql);

        return sql.toString();
    }

    protected void appendPrimaryKey(MetadataContext context, EntityMapping em, StringBuilder sql) {
        appendPrimaryKey(context, em, sql, null);
    }

    protected void appendPrimaryKey(MetadataContext context, EntityMapping em, StringBuilder sql, String alias) {
        DbDialect dialect = context.getDb().getDialect();

        int index = 0;
        for (FieldMapping key : em.getKeyFieldMappings()) {
            if (index > 0) {
                sql.append(" and ");
            }

            if (null != alias) {
                sql.append(alias).append('.');
            }
            sql.append(dialect.quoteIdentifier(key.getColumnName())).append("=#").append(key.getFieldName()).append("#");
            index++;
        }
    }

    protected String getFindListSql(MetadataContext context, EntityMapping em) {
        if (em.isCompositeKey()) {
            throw new IllegalStateException("Cannot create 'findList' sql for composite key entity '" + em.getEntityName() + "'");
        }

        DbDialect dialect = context.getDb().getDialect();

        StringBuilder sql = new StringBuilder();

        sql.append("select ")
                .append(createSelectColumns(context, em, null))
                .append(" from ")
                .append(em.getEntityName())
                .append(" t ").append(" where ");

        int index = 0;
        for (FieldMapping key : em.getKeyFieldMappings()) {
            if (index > 0) {
                sql.append(" and ");
            }

            sql.append(dialect.quoteIdentifier(key.getColumnName())).append(" in #").append(key.getFieldName()).append("#");
            index++;
        }

        return sql.toString();
    }

    protected String getFindAllSql(MetadataContext context, EntityMapping em) {
        StringBuilder sql = new StringBuilder();

        sql.append("select ")
                .append(createSelectColumns(context, em, null))
                .append(" from ")
                .append(em.getEntityName())
                .append(" t ");

        return sql.toString();
    }

    @Override
    public String createSelectColumns(MetadataContext context, EntityMapping em, String alias) {
        StringBuilder s = new StringBuilder();

        DbDialect dialect = context.getDb().getDialect();

        int index = 0;

        for (FieldMapping fm : em.getFieldMappings()) {
            if (fm.isEmbedded()) {
                continue;
            }

            if (index > 0) {
                s.append(',');
            }

            if (!Strings.isEmpty(alias)) {
                s.append(alias).append('.');
            }

            s.append(dialect.quoteIdentifier(fm.getColumnName()));

            index++;
        }

        if (em.hasEmbeddedFieldMappings()) {
            s.append(',');
            if (!Strings.isEmpty(alias)) {
                s.append(alias).append('.');
            }
            s.append(em.getEmbeddedColumn().getName());
        }

        return s.toString();
    }

	protected void addInsertEmbeddedColumn(MetadataContext context, EntityMapping em, List<FieldMapping> embedded,
										   int index, StringBuilder sql, StringBuilder values) {
		if (embedded.isEmpty()) {
			return;
		}

		if (index > 0) {
			sql.append(",");
			values.append(",");
		}

		final String   column = em.getEmbeddedColumn().getName();
		final String[] fields = embedded.stream().map(f -> f.getFieldName()).toArray(String[]::new);

		final JsonColumnSupport jcs = context.getDb().getDialect().getJsonColumnSupport();
		if(null != jcs) {
			jcs.applyInsertExpr(column, fields, (n) -> "#" + n + "#", (item, val) -> {
				sql.append(item);
				values.append(val);
			});
		}else {
			sql.append(column);
			values.append('#').append(column).append('#');
		}
	}

	protected void addUpdateEmbeddedColumn(MetadataContext context, EntityMapping em, List<FieldMapping> embedded, int index, StringBuilder sql) {
		if (embedded.isEmpty()) {
			return;
		}

		if (index > 0) {
			sql.append(",");
		}

		final String   column = em.getEmbeddedColumn().getName();
		final String[] fields = embedded.stream().map(f -> f.getFieldName()).toArray(String[]::new);

		final JsonColumnSupport jcs = context.getDb().getDialect().getJsonColumnSupport();
		if(null != jcs) {
			sql.append(jcs.getUpdateExpr(column, fields, (n) -> "#" + n + "#"));
		}else {
			sql.append(column).append('=')
					.append('#').append(column).append('#');
		}
	}
}
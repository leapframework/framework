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
package leap.orm.command;

import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.lang.expression.Expression;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.sql.SqlCommand;
import leap.orm.value.EntityWrapper;

import java.util.ArrayList;
import java.util.List;

public class DefaultBatchInsertCommand extends AbstractEntityDaoCommand implements BatchInsertCommand {

    protected final List<EntityWrapper> entities = new ArrayList<>();

	public DefaultBatchInsertCommand(Dao dao,EntityMapping em, Object[] records) {
	    super(dao,em);

        for(Object record : records) {
            entities.add(EntityWrapper.wrap(em, record));
        }
    }

	@Override
	public int[] execute() {
        if(entities.isEmpty()) {
            return new int[0];
        }

        prepare();

        String[] fields = entities.get(0).getFieldNames().toArray(Arrays2.EMPTY_STRING_ARRAY);
        if(fields.length == 0) {
            throw new IllegalStateException("No insert fields");
        }

        final SqlCommand primaryCommand   = context.getSqlFactory().createInsertCommand(context, em, fields);
        final SqlCommand secondaryCommand =
                em.hasSecondaryTable() ? context.getSqlFactory().createInsertCommand(context, em, fields, true) : null;

        Object[] records = entities.toArray(new Object[entities.size()]);

        if(null == secondaryCommand) {
            return primaryCommand.executeBatchUpdate(this, records);
        }

        if(null == primaryCommand) {
            return secondaryCommand.executeBatchUpdate(this, records);
        }

        return dao.doTransaction((s) -> {
            int[] result = primaryCommand.executeBatchUpdate(this, records);
            secondaryCommand.executeBatchUpdate(this, records);
            return result;
        });
	}

    protected void prepare(){
        for(EntityWrapper entity : entities) {
            for(FieldMapping fm : em.getFieldMappings()){
                Object value = entity.get(fm.getFieldName());

                if(null == value) {
                    if (!Strings.isEmpty(fm.getSequenceName())) {
                        value = db.getDialect().getNextSequenceValueSqlString(fm.getSequenceName());
                    } else {
                        Expression expression = fm.getInsertValue();
                        if (null != expression) {
                            value = expression.getValue(entity);
                        } else {
                            expression = fm.getDefaultValue();
                            if (null != expression) {
                                value = expression.getValue(entity);
                            }
                        }
                    }
                    if(null != value) {
                        entity.set(fm.getFieldName(), value);
                    }
                }
            }
        }

    }
}
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

    protected final SqlCommand          primaryCommand;
    protected final List<EntityWrapper> entities = new ArrayList<>();

	public DefaultBatchInsertCommand(Dao dao,EntityMapping em, Object[] records) {
	    super(dao,em);
	    this.primaryCommand = metadata.getSqlCommand(em.getEntityName(), SqlCommand.INSERT_COMMAND_NAME);

        for(Object record : records) {
            entities.add(EntityWrapper.wrap(em, record));
        }
    }

	@Override
	public int[] execute() {
        prepare();

        final SqlCommand secondaryCommand =
                em.hasSecondaryTable() ? context.getSqlFactory().createInsertCommand(context, em, true) : null;

        Object[] records = entities.toArray(new Object[entities.size()]);

        if(null != secondaryCommand) {
            return dao.doTransaction((s) -> {
                secondaryCommand.executeBatchUpdate(this, records);
                return primaryCommand.executeBatchUpdate(this, records);
            });
        }else {
            return primaryCommand.executeBatchUpdate(this, records);
        }
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
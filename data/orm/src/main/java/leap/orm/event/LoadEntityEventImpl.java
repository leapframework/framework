/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.orm.event;

import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.Mappings;
import leap.orm.query.QueryContext;
import leap.orm.value.EntityWrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoadEntityEventImpl extends EntityEventBase implements LoadEntityEvent {

    protected final QueryContext queryContext;
    protected final int          type;
    protected final List<Row>    rows;

    public LoadEntityEventImpl(QueryContext context, EntityMapping em, List list, boolean find) {
        super(context.getOrmContext(), em);
        this.queryContext = context;
        this.rows = rows(list);
        this.type = find ? 1 : 2;
    }

    @Override
    public QueryContext getQueryContext() {
        return queryContext;
    }

    @Override
    public boolean isFind() {
        return type == 1;
    }

    @Override
    public boolean isQuery() {
        return type == 2;
    }

    @Override
    public int size() {
        return rows.size();
    }

    @Override
    public Iterator<Row> iterator() {
        return rows.iterator();
    }

    protected List<Row> rows(List list) {
        List<Row> rows = new ArrayList<>(list.size());

        for(Object item : list) {
            rows.add(new RowImpl(EntityWrapper.wrap(mapping, item)));
        }

        return rows;
    }

    public static final class RowImpl implements Row {

        private static final Object NULL_ID = new Object();

        private final EntityWrapper ew;

        private Object id;

        public RowImpl(EntityWrapper ew) {
            this.ew = ew;
        }

        @Override
        public Object getId() {
            if(id == NULL_ID) {
                return null;
            }
            if(id == null) {
                id = Mappings.getId(ew.getEntityMapping(), ew);
                if(null == id) {
                    id = NULL_ID;
                }
            }
            return id;
        }

        @Override
        public EntityWrapper getEntity() {
            return ew;
        }
    }
}

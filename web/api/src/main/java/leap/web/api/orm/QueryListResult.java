/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.web.api.orm;

import leap.core.value.Record;

import java.util.ArrayList;
import java.util.List;

public final class QueryListResult {

    public static QueryListResult EMPTY = new QueryListResult(new ArrayList<>(), 0);

    public List<Record> list;
    public long         count;
    public Object       entity;

    public QueryListResult() {

    }

    public QueryListResult(List<Record> list, long count) {
        this(list, count, null);
    }

    public QueryListResult(List<Record> list, long count, Object entity) {
        this.list = list;
        this.count = count;
        this.entity = entity;
    }

    public List<Record> getList() {
        return list;
    }

    public void setList(List<Record> list) {
        this.list = list;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }
}

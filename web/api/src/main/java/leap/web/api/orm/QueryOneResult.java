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

public class QueryOneResult {

    public Record            record;
    public Object            entity;
    public List<ExpandError> expandErrors;

    public QueryOneResult() {

    }

    public QueryOneResult(Record record) {
        this(record, null);
    }

    public QueryOneResult(Record record, List<ExpandError> expandErrors) {
        this(record, null, expandErrors);
    }

    public QueryOneResult(Record record, Object entity) {
        this.record = record;
        this.entity = entity;
    }

    public QueryOneResult(Record record, Object entity, List<ExpandError> expandErrors) {
        this.record = record;
        this.entity = entity;
        this.expandErrors = expandErrors;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public List<ExpandError> getExpandErrors() {
        return expandErrors;
    }

    public void setExpandErrors(List<ExpandError> expandErrors) {
        this.expandErrors = expandErrors;
    }

    public void addExpandError(ExpandError e) {
        if(null == expandErrors) {
            expandErrors = new ArrayList<>();
        }
        expandErrors.add(e);
    }
}

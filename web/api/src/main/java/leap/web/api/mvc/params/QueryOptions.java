/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.web.api.mvc.params;

import leap.lang.value.Page;
import leap.web.Params;
import leap.web.annotation.ParamsWrapper;
import leap.web.annotation.QueryParam;

@ParamsWrapper
public class QueryOptions extends QueryOptionsBase {

    protected @QueryParam("page_size")   Integer pageSize;
    protected @QueryParam("page")        Integer pageIndex;
    protected @QueryParam("limit")       Integer limit;
    protected @QueryParam("offset")      Integer offset;  //0-based
    protected @QueryParam("total")       boolean total;
    protected @QueryParam("orderby")     String  orderBy;
    protected @QueryParam("filters")     String  filters;
    protected @QueryParam("joins")       String  joins;

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public boolean isTotal() {
        return total;
    }

    public void setTotal(boolean total) {
        this.total = total;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getJoins() {
        return joins;
    }

    public void setJoins(String joins) {
        this.joins = joins;
    }

    public Page getPage(int defaultPageSize) {
        if(null != limit || null != offset) {
            if(null == limit) {
                return Page.limit(defaultPageSize, offset);
            }else{
                return Page.limit(limit, null == offset ? 0 : offset);
            }
        }

        if(null != pageIndex || null != pageSize) {
            if(null == pageIndex) {
                return Page.indexOf(1, pageSize);
            }else{
                return Page.indexOf(pageIndex, null == pageSize ? defaultPageSize : pageSize);
            }
        }

        return Page.indexOf(1, defaultPageSize);
    }

}
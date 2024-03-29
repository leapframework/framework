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

import leap.lang.Strings;
import leap.lang.json.JsonIgnore;
import leap.lang.json.JsonName;
import leap.lang.text.scel.ScelExpr;
import leap.lang.value.Page;
import leap.web.annotation.NonParam;
import leap.web.annotation.ParamsWrapper;
import leap.web.annotation.QueryParam;
import leap.web.api.query.*;
import leap.web.exception.BadRequestException;

import java.util.HashMap;
import java.util.Map;

@ParamsWrapper
public class QueryOptions extends QueryOptionsBase {

    @QueryParam("page_size")
    @JsonName("page_size")
    protected Integer pageSize;

    @QueryParam("page")
    @JsonName("page")
    protected Integer pageIndex;

    @QueryParam("limit")
    protected Integer limit;

    @QueryParam("offset")
    protected Integer offset;  //0-based

    @QueryParam("total")
    protected boolean total;

    @QueryParam("orderby")
    @JsonName("orderby")
    protected String orderBy;

    @QueryParam("viewId")
    protected String viewId;

    @QueryParam("filters")
    protected String filters;

    @QueryParam("aggregates")
    protected String aggregates;

    @QueryParam("groupby")
    @JsonName("groupby")
    protected String groupBy;

    @QueryParam("joins")
    protected String joins;

    @QueryParam("distinct")
    protected Boolean distinct;

    @NonParam
    @JsonIgnore
    protected ScelExpr resolvedFilters;

    @NonParam
    @JsonIgnore
    protected OrderBy resolvedOrderBy;

    @NonParam
    @JsonIgnore
    protected Aggregate resolvedAggregate;

    @NonParam
    @JsonIgnore
    protected GroupBy resolvedGroupBy;

    @NonParam
    @JsonIgnore
    protected Select resolvedSelect;

    @NonParam
    @JsonIgnore
    protected Join[] resolvedJoins;

    @NonParam
    @JsonIgnore
    protected Map<String, Object> queryParams;

    @NonParam
    @JsonIgnore
    protected Map<String, Object> extraFilters;

    @NonParam
    @JsonIgnore
    protected Boolean allowSingleExpr;

    public boolean getAllowSingleExpr() {
        return (null == allowSingleExpr)?false:allowSingleExpr;
    }

    public void setAllowSingleExpr(boolean allowSingleExpr) {
        this.allowSingleExpr = allowSingleExpr;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setPage_size(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public void setPage(Integer page) {
        this.pageIndex = page;
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

    public void setOrderby(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getAggregates() {
        return aggregates;
    }

    public void setAggregates(String aggregates) {
        this.aggregates = aggregates;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public void setGropuby(String gropuby) {
        this.groupBy = gropuby;
    }

    public String getJoins() {
        return joins;
    }

    public void setJoins(String joins) {
        this.joins = joins;
    }

    public boolean isDistinct() {
        return null != distinct && distinct;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
    }

    public void setQueryParam(String name, Object value) {
        if(null == queryParams) {
            queryParams = new HashMap<>();
        }
        queryParams.put(name, value);
    }

    public Map<String, Object> getExtraFilters() {
        return extraFilters;
    }

    public void setExtraFilters(Map<String, Object> extraFilters) {
        this.extraFilters = extraFilters;
    }

    public Page getPage(int defaultPageSize) {
        return getPage(defaultPageSize, null);
    }

    public Page getPage(int defaultPageSize, Integer maxPageSize) {
        if (null != limit || null != offset) {
            return Page.limit(choicePageSize(limit, defaultPageSize, maxPageSize), null == offset ? 0 : offset);
        }

        return Page.indexOf(null == pageIndex ? 1 : pageIndex, choicePageSize(pageSize, defaultPageSize, maxPageSize));
    }

    public int choicePageSize(Integer value, int defaultPageSize, Integer maxPageSize) {
        int size = null == value ? defaultPageSize : value;
        if (null != maxPageSize && size > maxPageSize) {
            size = maxPageSize;
        }
        return size;
    }

    public ScelExpr getResolvedFilters() {
        if (null == resolvedFilters && !Strings.isEmpty(filters)) {
            try {
                resolvedFilters = FiltersParser.parse(filters, getAllowSingleExpr());
            } catch (Exception e) {
                throw new BadRequestException("Invalid filter expr '" + filters + "', " + e.getMessage(), e);
            }
        }
        return resolvedFilters;
    }

    public void setResolvedFilters(ScelExpr resolvedFilters) {
        this.resolvedFilters = resolvedFilters;
    }

    public OrderBy getResolvedOrderBy() {
        if (null == resolvedOrderBy && !Strings.isEmpty(orderBy)) {
            resolvedOrderBy = OrderByParser.parse(orderBy);
        }
        return resolvedOrderBy;
    }

    public void setResolvedOrderBy(OrderBy resolvedOrderBy) {
        this.resolvedOrderBy = resolvedOrderBy;
    }

    public GroupBy getResolvedGroupBy() {
        if (null == resolvedGroupBy && Strings.isNotEmpty(groupBy)) {
            resolvedGroupBy = GroupByParser.parse(groupBy);
        }
        return resolvedGroupBy;
    }

    public void setResolvedGroupBy(GroupBy resolvedGroupBy) {
        this.resolvedGroupBy = resolvedGroupBy;
    }

    public Aggregate getResolvedAggregate() {
        if (null == resolvedAggregate && Strings.isNotEmpty(aggregates)) {
            resolvedAggregate = AggregateParser.parse(aggregates, getAllowSingleExpr());
        }
        return resolvedAggregate;
    }

    public void setResolvedAggregate(Aggregate resolvedAggregate) {
        this.resolvedAggregate = resolvedAggregate;
    }

    public Select getResolvedSelect() {
        if (null == resolvedSelect && Strings.isNotEmpty(select)) {
            resolvedSelect = SelectParser.parse(select);
        }
        return resolvedSelect;
    }

    public void setResolvedSelect(Select resolvedSelect) {
        this.resolvedSelect = resolvedSelect;
    }

    public Join[] getResolvedJoins() {
        if (null == resolvedJoins && !Strings.isEmpty(joins)) {
            resolvedJoins = JoinParser.parse(joins);
        }
        return resolvedJoins;
    }

    public void setResolvedJoins(Join[] resolvedJoins) {
        this.resolvedJoins = resolvedJoins;
    }

    public void clearResolved() {
        this.resolvedJoins = null;
        this.resolvedOrderBy = null;
        this.resolvedFilters = null;
        this.resolvedExpands = null;
    }
}
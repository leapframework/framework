/*
 * Copyright 2017 the original author or authors.
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

package leap.web.api.config.model;

import leap.lang.exception.ObjectExistsException;
import leap.web.route.Routes;

import java.util.*;

/**
 * Configuration for restd api.
 */
public class RestdConfig {

    protected String  dataSourceName;
    protected boolean readonly;

    protected Set<String>               includedModels = new LinkedHashSet<>();
    protected Set<String>               excludedModels = new LinkedHashSet<>();
    protected Set<String>               readonlyModels = new HashSet<>();
    protected Map<String, Model>        models         = new HashMap<>();
    protected Map<String, SqlOperation> sqlOperations  = new LinkedHashMap<>();

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public Set<String> getIncludedModels() {
        return includedModels;
    }

    public void addIncludedModel(String name) {
        includedModels.add(name);
    }

    public Set<String> getExcludedModels() {
        return excludedModels;
    }

    public void addExcludedModel(String name) {
        excludedModels.add(name);
    }

    public Set<String> getReadonlyModels() {
        return readonlyModels;
    }

    public void addReadonlyModel(String name) {
        readonlyModels.add(name.toLowerCase());
    }

    public boolean isReadonlyModel(String name) {
        return readonlyModels.contains(name.toLowerCase());
    }

    public Map<String, Model> getModels() {
        return models;
    }

    public Model getModel(String name) {
        return models.get(name.toLowerCase());
    }

    public void addModel(Model model) {
        String key = model.getName().toLowerCase();
        if(models.containsKey(key)) {
            throw new ObjectExistsException("The configuration of model '" + model.getName() + "' already exists!");
        }
        models.put(key, model);
    }

    public Map<String, SqlOperation> getSqlOperations() {
        return sqlOperations;
    }

    public SqlOperation getSqlOperation(String name) {
        return sqlOperations.get(name.toLowerCase());
    }

    public void addSqlOperation(SqlOperation op) {
        String key = op.getName().toLowerCase();
        if(sqlOperations.containsKey(key)) {
            throw new ObjectExistsException("The sql operation '" + op.getName() + "' already exists!");
        }
        sqlOperations.put(key, op);
    }

    public boolean isModelAnonymous(String name) {
        Model model = getModel(name);
        if(null != model && null != model.getAnonymous()) {
            return model.getAnonymous();
        }

        return false;
    }

    public boolean allowCreateModel(String name) {
        Model model = getModel(name);
        if(null != model && null != model.getCreateOperationEnabled()) {
            return model.getCreateOperationEnabled();
        }

        return !readonly;
    }

    public boolean allowUpdateModel(String name) {
        Model model = getModel(name);
        if(null != model && null != model.getUpdateOperationEnabled()) {
            return model.getUpdateOperationEnabled();
        }

        return !readonly;
    }

    public boolean allowDeleteModel(String name) {
        Model model = getModel(name);
        if(null != model && null != model.getDeleteOperationEnabled()) {
            return model.getDeleteOperationEnabled();
        }

        return !readonly;
    }

    public boolean allowFindModel(String name) {
        Model model = getModel(name);
        if(null != model && null != model.getFindOperationEnabled()) {
            return model.getFindOperationEnabled();
        }

        return true;
    }

    public boolean allowQueryModel(String name) {
        Model model = getModel(name);
        if(null != model && null != model.getQueryOperationEnabled()) {
            return model.getQueryOperationEnabled();
        }

        return true;
    }

    /**
     * The configuration of restd model.
     */
    public static class Model {

        protected final String name;

        protected Boolean anonymous;
        protected Boolean createOperationEnabled;
        protected Boolean updateOperationEnabled;
        protected Boolean deleteOperationEnabled;
        protected Boolean findOperationEnabled;
        protected Boolean queryOperationEnabled;

        protected Map<String, SqlOperation> sqlOperations = new LinkedHashMap<>();

        public Model(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Boolean getAnonymous() {
            return anonymous;
        }

        public void setAnonymous(Boolean anonymous) {
            this.anonymous = anonymous;
        }

        public Boolean getCreateOperationEnabled() {
            return createOperationEnabled;
        }

        public void setCreateOperationEnabled(Boolean createOperationEnabled) {
            this.createOperationEnabled = createOperationEnabled;
        }

        public Boolean getUpdateOperationEnabled() {
            return updateOperationEnabled;
        }

        public void setUpdateOperationEnabled(Boolean updateOperationEnabled) {
            this.updateOperationEnabled = updateOperationEnabled;
        }

        public Boolean getDeleteOperationEnabled() {
            return deleteOperationEnabled;
        }

        public void setDeleteOperationEnabled(Boolean deleteOperationEnabled) {
            this.deleteOperationEnabled = deleteOperationEnabled;
        }

        public Boolean getFindOperationEnabled() {
            return findOperationEnabled;
        }

        public void setFindOperationEnabled(Boolean findOperationEnabled) {
            this.findOperationEnabled = findOperationEnabled;
        }

        public Boolean getQueryOperationEnabled() {
            return queryOperationEnabled;
        }

        public void setQueryOperationEnabled(Boolean queryOperationEnabled) {
            this.queryOperationEnabled = queryOperationEnabled;
        }

        public Map<String, SqlOperation> getSqlOperations() {
            return sqlOperations;
        }

        public SqlOperation getSqlOperation(String name) {
            return sqlOperations.get(name.toLowerCase());
        }

        public void addSqlOperation(SqlOperation op) {
            String key = op.getName().toLowerCase();
            if(sqlOperations.containsKey(key)) {
                throw new ObjectExistsException("The sql operation '" + op.getName() + "' of model '" + name + "' already exists!");
            }
            sqlOperations.put(key, op);
        }
    }

    /**
     * The configuration of a restd sql operation.
     */
    public static class SqlOperation {

        protected String name;
        protected String sqlKey;

        /**
         * The name of operation.
         */
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        /**
         * The key of sql command.
         */
        public String getSqlKey() {
            return sqlKey;
        }

        public void setSqlKey(String sqlKey) {
            this.sqlKey = sqlKey;
        }
    }
}
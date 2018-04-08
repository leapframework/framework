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
package leap.db;

import leap.core.AppContext;
import leap.core.transaction.TransactionManager;
import leap.core.transaction.TransactionProvider;
import leap.lang.Args;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.Try;
import leap.lang.jdbc.JDBC;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class DbBase implements Db {

    protected final String              server;
    protected final Log                 log;
    protected final String              name;
    protected final String              description;
    protected final DbPlatform          platform;
    protected final DataSource          dataSource;
    protected final DbDialect           dialect;
    protected final DbMetadata          metadata;
    protected final DbComparator        comparator;
    protected final TransactionProvider tp;

    protected DbBase(String name, DataSource ds, DatabaseMetaData md,
                     DbPlatform platform, DbMetadata metadata, DbDialect dialect, DbComparator comparator){

		Args.notEmpty(name);
        Args.notNull(ds);
        Args.notNull(md);
		Args.notNull(platform);
		Args.notNull(metadata);
		Args.notNull(dialect);
		Args.notNull(comparator);
		
		this.name        = name;
        this.server      = extractServerFromJdbcUrl(md);
		this.description = metadata.getProductName() + " " + metadata.getProductVersion();
		this.platform	 = platform;
		this.dataSource  = ds;
		this.metadata    = metadata;
		this.dialect     = dialect;
		this.comparator  = comparator;

        AppContext context = AppContext.tryGetCurrent();
        if(null != context) {
            tp = context.getBeanFactory().getBean(TransactionManager.class).getProvider(ds);
        }else{
            tp = null;
        }

		this.awareObjects();
		this.log = getLog(this.getClass());

        this.init();
	}
	
	@Override
    public String getName() {
	    return name;
    }
	
	@Override
    public String getType() {
	    return platform.getName();
    }

	@Override
    public String getDescription() {
	    return description;
    }
	
	@Override
    public DbPlatform getPlatform() {
	    return platform;
    }

	@Override
    public DbDialect getDialect() {
	    return dialect;
    }
	
	@Override
    public DbMetadata getMetadata() {
	    return metadata;
    }
	
	@Override
    public DbComparator getComparator() {
	    return comparator;
    }

	@Override
    public DataSource getDataSource() {
	    return dataSource;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + name + ")";
    }

    public Log getLog(Class<?> cls){
        //leap.db.cls(type:host:port)
        String name = Classes.getPackageName(DbBase.class) + "." + cls.getSimpleName() +
                      "(" + platform.getName().toLowerCase() + (null == server ? ")" : (":" + server + ")"));
		return LogFactory.get(name);
	}

    protected void awareObjects(){
        if(null != dataSource && dataSource instanceof DbAware){
            ((DbAware)dataSource).setDb(this);
        }

        if(dialect instanceof DbAware){
            ((DbAware)dialect).setDb(this);
        }

        if(metadata instanceof DbAware){
            ((DbAware)metadata).setDb(this);
        }

        if(comparator instanceof DbAware){
            ((DbAware) comparator).setDb(this);
        }
    }

    protected void init() {
        DbExecution execution = createExecution();

        for(Resource r : findMetaInfClasspathSql("init")) {
            loadSqlStatements(execution, r);
        }

        loadSqlStatements(execution, findClasspathSql("init"));

        if(!execution.sqls().isEmpty()) {
            log.info("Init db '{}' with {} sql statements", name, execution.sqls().size());
            execution.execute();
        }
    }

    protected boolean loadSqlStatements(DbExecution execution, Resource file) {
        if(null == file || !file.exists()) {
            return false;
        }
        String sqls = file.getContent();
        if(!Strings.isEmpty(sqls)) {
            log.debug("Load init sql file '{}'", file.getClasspath());
            execution.addAll(dialect.splitSqlStatements(sqls));
        }
        return true;
    }

    protected Resource[] findMetaInfClasspathSql(String filename) {
        final String prefix = "classpath*:META-INF/conf/db/" + name + "/" + filename;

        Set<Resource> found = new LinkedHashSet<>();

        ResourceSet rs = Resources.scan(prefix + "_" + getType().toLowerCase() + ".sql");
        rs.forEach(found::add);

        rs = Resources.scan(prefix + ".sql");
        for(Resource untyped : rs) {

            boolean exists = false;
            for(Resource typed : found) {
                //exists -> META-INF/conf/db/{name}/filename_{type}.sql
                //r      -> META-INF/conf/db/{name}/filename.sql

                String cp = Strings.removeEnd(untyped.getClasspath(),".sql");
                if((cp + "_" + getType().toLowerCase() + ".sql").equals(typed.getClasspath())) {
                    exists = true;
                    break;
                }
            }

            if(!exists) {
                found.add(untyped);
            }
        }

        return found.toArray(new Resource[0]);
    }

    protected Resource findClasspathSql(String filename) {
        Resource resource = findClasspathSql("classpath:/conf/db/" + name, filename);
        if(null == resource) {
            resource = findClasspathSql("classpath:/conf/db", filename);
        }
        return resource;
    }

    protected Resource findClasspathSql(String classpath, String filename) {
        final String prefix = classpath + "/" + filename;

        Resource resource = Resources.getResource(prefix + "_" + getType().toLowerCase() + ".sql");
        if(null != resource && resource.exists()) {
            return resource;
        }

        resource = Resources.getResource(prefix + ".sql");
        if(null != resource && resource.exists()) {
            return resource;
        }

        return null;
    }

    protected static String extractServerFromJdbcUrl(DatabaseMetaData md) {
        return Try.throwUncheckedWithResult(() -> {

            return JDBC.tryExtractServerString(md.getURL());

        });
    }
}

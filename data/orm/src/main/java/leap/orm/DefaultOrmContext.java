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
package leap.orm;

import leap.core.AppContext;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.BeanPrimaryAware;
import leap.core.ioc.PostCreateBean;
import leap.core.ioc.PostInjectBean;
import leap.core.ioc.PreInjectBean;
import leap.db.Db;
import leap.db.DbFactory;
import leap.lang.Assert;
import leap.lang.Readonly;
import leap.orm.command.CommandFactory;
import leap.orm.dao.Dao;
import leap.orm.dao.DefaultDao;
import leap.orm.dao.WrappedDao;
import leap.orm.dmo.DefaultDmo;
import leap.orm.dmo.Dmo;
import leap.orm.event.EntityEventHandler;
import leap.orm.mapping.MappingStrategy;
import leap.orm.metadata.MetadataContext;
import leap.orm.metadata.OrmMetadataManager;
import leap.orm.naming.NamingStrategy;
import leap.orm.parameter.ParameterStrategy;
import leap.orm.query.QueryFactory;
import leap.orm.reader.EntityReader;
import leap.orm.reader.RowReader;
import leap.orm.sql.SqlFactory;
import leap.orm.validation.EntityValidator;

import javax.sql.DataSource;

public class DefaultOrmContext implements OrmContext,BeanPrimaryAware,PostCreateBean,PreInjectBean,PostInjectBean,MetadataContext {
	
	private final Readonly _readonly = new Readonly(this);
	
    protected @Inject @M AppContext         appContext;
    protected @Inject @M Db                 db;
    protected @Inject @M DataSource         dataSource;
    protected @Inject @M OrmMetadata        metadata;
    protected @Inject @M OrmMetadataManager metadataManager;
    protected @Inject @M MappingStrategy    mappingStrategy;
    protected @Inject @M NamingStrategy     namingStrategy;
    protected @Inject @M ParameterStrategy  parameterStrategy;
    protected @Inject @M CommandFactory     commandFactory;
    protected @Inject @M SqlFactory         sqlFactory;
    protected @Inject @M QueryFactory       queryFactory;
    protected @Inject @M EntityReader       entityReader;
    protected @Inject @M RowReader          rowReader;
    protected @Inject @M OrmConfig          config;
    protected @Inject @M EntityValidator    entityValidator;
    protected @Inject @M EntityEventHandler entityEventHandler;

    protected String name;
    protected Dao    dao;
    protected Dmo    dmo;
    protected boolean primary;

    public DefaultOrmContext(){
		
	}
	
	public DefaultOrmContext(String name,DataSource dataSource){
		this.setName(name);
		this.setDataSource(dataSource);
	}

	@Override
	public boolean isPrimary() {
		return primary;
	}

	@Override
    public void setBeanPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public OrmConfig getConfig() {
	    return config;
    }
	
	public void setConfig(OrmConfig config) {
		_readonly.check();
		this.config = config;
	}

	@Override
    public AppContext getAppContext() {
		return appContext;
	}

	@Override
    public String getName() {
	    return name;
    }
	
	@Override
    public DataSource getDataSource() {
	    return dataSource;
    }

	@Override
    public Db getDb() {
	    return db;
    }

    @Override
    public Dao getDao() {
        return dao;
    }

    @Override
    public Dmo getDmo() {
        return dmo;
    }

    @Override
    public OrmMetadata getMetadata() {
	    return metadata;
    }
	
	public void setName(String name) {
		_readonly.check();
		this.name = name;
	}

	public void setDataSource(DataSource dataSource) {
		_readonly.check();
		this.dataSource = dataSource;
		this.db			= DbFactory.getInstance(this.name, dataSource);
	}

	public void setMetadata(OrmMetadata metadata) {
		_readonly.check();
		this.metadata = metadata;
	}
	
	public OrmMetadataManager getMetadataManager() {
        return metadataManager;
    }

    @Override
	public MappingStrategy getMappingStrategy() {
		return mappingStrategy;
	}

	@Override
	public CommandFactory getCommandFactory() {
		return commandFactory;
	}

	@Override
	public SqlFactory getSqlFactory() {
		return sqlFactory;
	}

	@Override
	public QueryFactory getQueryFactory() {
		return queryFactory;
	}

	@Override
	public NamingStrategy getNamingStrategy() {
		return namingStrategy;
	}

	@Override
	public ParameterStrategy getParameterStrategy() {
		return parameterStrategy;
	}

	@Override
	public EntityReader getEntityReader() {
		return entityReader;
	}

	@Override
    public RowReader getRowReader() {
	    return rowReader;
    }

    @Override
    public EntityValidator getEntityValidator() {
        return entityValidator;
    }

    @Override
    public EntityEventHandler getEntityEventHandler() {
        return entityEventHandler;
    }

    @Override
    public void preInject(BeanFactory factory) {
		Assert.notNull(db,"The 'db' field must not be null");
		
		_readonly.check();
		
		this.appContext = factory.getAppContext();
		
	    if(null == metadata){
	    	this.metadata = factory.tryGetBean(OrmMetadata.class,name);
	    }

        if(null == dao) {
            this.dao = factory.tryGetBean(Dao.class, name);
        }

        if(null == dmo) {
            this.dmo = factory.tryGetBean(Dmo.class, name);
        }
    }

	@Override
    public void postInject(BeanFactory factory) {
		_readonly.check();
    }
	
	@Override
    public void postCreate(BeanFactory beanFactory) throws Exception {
		_readonly.check().enable();
		
    	if(null == metadata){
    		OrmMetadataManager mm = beanFactory.getBean(OrmMetadataManager.class);
    		metadata = mm.createMetadata();
    		mm.loadMetadata(this);
    	}

        if(null == dao) {
            dao = new WrappedDao(beanFactory.inject(new DefaultDao(this)));
            beanFactory.addBean(Dao.class, dao, name, primary);
        }

        if(null == dmo) {
            dmo = beanFactory.inject(new DefaultDmo(this));
            beanFactory.addBean(Dmo.class, dmo, name, primary);
        }
    	
    	for(OrmContextInitializable init : beanFactory.getBeans(OrmContextInitializable.class)){
    		init.postInitialize(this);
    	}

    }

	@Override
    public String toString() {
	    return this.getClass().getSimpleName() + "(" + getName() + ")";
    }

}
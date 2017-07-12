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
package leap.orm.dmo;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.PostCreateBean;
import leap.core.ioc.PostInjectBean;
import leap.core.ioc.PreInjectBean;
import leap.core.validation.annotations.NotEmpty;
import leap.lang.Readonly;
import leap.lang.Strings;
import leap.orm.Orm;
import leap.orm.OrmContext;
import leap.orm.OrmFactory;
import leap.orm.OrmMetadata;
import leap.orm.command.CommandFactory;
import leap.orm.df.DataFactory;

public abstract class DmoBase extends Dmo implements PreInjectBean, PostInjectBean, PostCreateBean {
	
	private final Readonly _readonly = new Readonly(this);
	
    protected @Inject @M OrmFactory  ormFactory;
    protected @Inject @M DataFactory dataFactory;

    protected String     name;
    protected OrmContext ormContext;

	protected DmoBase(){
		
	}
	
	protected DmoBase(String name){
		this.name = name;
	}
	
	@Override
    public OrmContext getOrmContext() {
	    return ormContext;
    }

	public void setOrmContext(OrmContext context) {
		checkReadonly();
		this.name       = context.getName();
		this.ormContext = context;
	}
	
	public void setOrmFactory(OrmFactory ormFactory) {
		checkReadonly();
		this.ormFactory = ormFactory;
	}

	@Override
	public DataFactory getDataFactory() {
		return dataFactory;
	}

	@Override
    public void preInject(BeanFactory factory) {
		checkReadonly();
		if(null == ormContext){
		    if(Strings.equals(name, Orm.DEFAULT_NAME)){
		    	ormContext = factory.tryGetBean(OrmContext.class);
		    }else{
		    	ormContext = factory.tryGetBean(OrmContext.class,name);
		    }
		}
    }

	@Override
    public void postInject(BeanFactory factory) {
	    this.dataFactory = ormFactory.createDataFactory(this);
    }
	
	protected OrmMetadata metadata(){
		return ormContext.getMetadata();
	}
	
	protected CommandFactory commandFactory(){
		return ormContext.getCommandFactory();
	}
	
	@Override
    public void postCreate(BeanFactory beanFactory) throws Exception {
		_readonly.check().enable();
		this.doInit();
    }

	protected final void checkReadonly(){
		_readonly.check();
	}
	
	protected void doInit() throws Exception {
		
	}
}
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
package leap.orm.df;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.lang.Args;
import leap.lang.Strings;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.dmo.Dmo;
import leap.orm.domain.FieldDomain;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;

public class DefaultDataFactory implements DataFactory,DataGeneratorContext {
	
	private final Map<String, DataGenerator> generatorCache = new ConcurrentHashMap<String, DataGenerator>();

	protected final OrmContext  context;
	protected final OrmMetadata metadata;
	
    protected @Inject @M BeanFactory         beanFactory;
    protected @Inject @M DataGenerator       defaultGenerator;
    protected @Inject @M DomainDatas         domainDatas;

    @Inject(name = "domain")
    protected @M DataGenerator               domainGenerator;
	
	public DefaultDataFactory(Dmo dmo){
		this.context  = dmo.getOrmContext();
		this.metadata = context.getMetadata();
	}

	@Override
    public DomainDatas getDomainDatas() {
	    return domainDatas;
    }

	@Override
    public <T> T generate(Class<T> entityClass) {
		Args.notNull(entityClass,"entity class");
		
		EntityMapping em = metadata.getEntityMapping(entityClass);
		
		T bean = em.getBeanType().newInstance();

		for(FieldMapping fm : em.getFieldMappings()){
			if(fm.isAutoGenerateValue()){
				continue;
			}
			
			Object value = generateFieldValue(em, fm);
			
			if(null != value){
				fm.getBeanProperty().setValue(bean, value);
			}
		}
		
	    return bean;
    }
	
	protected Object generateFieldValue(EntityMapping em,FieldMapping fm) {
		String cacheKey = Strings.lowerCase(em.getEntityName() + "." + fm.getFieldName());
		
		DataGenerator generator = generatorCache.get(cacheKey);
		
		if(null == generator){
			FieldDomain domain = fm.getDomain();
			if(null != domain){
				generator = beanFactory.tryGetBean(DataGenerator.class,domain.getQualifiedName());
				
				if(null == generator){
					DomainData data = domainDatas.tryGetDomainData(domain);
					
					if(null != data){
						generator = domainGenerator;
					}
				}
			}
			
			if(null == generator){
				generator = defaultGenerator;
			}
			
			generatorCache.put(cacheKey, generator);
		}
		
		return generator.generateValue(this, em, fm);
	}
}
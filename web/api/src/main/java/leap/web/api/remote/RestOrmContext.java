package leap.web.api.remote;

import leap.orm.mapping.EntityMapping;
import leap.web.api.remote.ds.RestDataSource;

public class RestOrmContext {

	private RestDataSource dataSource;

	private EntityMapping entityMapping;

	public RestOrmContext(){

	}

	public RestOrmContext(RestDataSource dataSource,EntityMapping em){
		this.dataSource=dataSource;
		this.entityMapping=em;
	}

	public RestDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(RestDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public EntityMapping getEntityMapping() {
		return entityMapping;
	}

	public void setEntityMapping(EntityMapping entityMapping) {
		this.entityMapping = entityMapping;
	}

}

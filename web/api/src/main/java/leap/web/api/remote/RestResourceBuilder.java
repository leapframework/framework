package leap.web.api.remote;

import leap.core.AppContext;
import leap.lang.Strings;
import leap.orm.mapping.EntityMapping;

public class RestResourceBuilder {
	private String endpoint;
	private EntityMapping entityMapping;

	public static RestResourceBuilder newBuilder(){
		return new RestResourceBuilder();
	}

	public RestResource build(){
		RestResource res=AppContext.factory().inject(new DefaultRestResource());
		if(Strings.isNotBlank(endpoint)){
			res.setEndpoint(endpoint);
		}
		if(Strings.isEmpty(res.getEndpoint()) &&  entityMapping!=null){
			res.setEndpoint(entityMapping.getRemoteDataSource());
		}
		if(Strings.isEmpty(res.getEndpoint())){
			throw new RuntimeException("can't build rest resource,when endpoint or entityMapping is empty!");
		}
		return res;
	}


	public RestResourceBuilder setEndpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}


	public RestResourceBuilder setEntityMapping(EntityMapping entityMapping) {
		this.entityMapping = entityMapping;
		return this;
	}
}

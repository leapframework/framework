package leap.web.api.remote;

import leap.core.AppContext;
import leap.lang.Assert;
import leap.lang.Strings;
import leap.lang.path.Paths;
import leap.orm.Orm;
import leap.orm.OrmContext;
import leap.orm.enums.RemoteType;
import leap.orm.mapping.EntityMapping;
import leap.web.api.remote.ds.RestDataSource;
import leap.web.api.remote.ds.RestDatasourceManager;

public class RestResourceBuilder {
	private String endpoint;
	private EntityMapping entityMapping;

	public static RestResourceBuilder newBuilder(){
		return new RestResourceBuilder();
	}

	public RestResource build(){
		DefaultRestResource res=AppContext.factory().inject(new DefaultRestResource());
		if(entityMapping!=null){
			RestDatasourceManager manager=getDataSourceManager();
			RestDataSource ds=manager.tryGetDataSource(entityMapping.getRemoteSettings().getDataSource());
			RestOrmContext context=new RestOrmContext(ds,entityMapping);
			res.setOrmContext(context);
			if(ds!=null && Strings.isNotEmpty(ds.getEndpoint())){
				String url=Paths.suffixWithSlash(ds.getEndpoint())+entityMapping.getRemoteSettings().getPathPrefix();
				res.setEndpoint(url);
			}
		}

		if(Strings.isNotBlank(endpoint)){
			res.setEndpoint(endpoint);
		}

		if(Strings.isEmpty(res.getEndpoint())){
			throw new RuntimeException("can't build rest resource,when endpoint or entityMapping is empty!");
		}
		return res;
	}

	private RestDatasourceManager getDataSourceManager(){
		return AppContext.getBean(RestDatasourceManager.class);
	}


	public RestResourceBuilder setEndpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}


	public RestResourceBuilder setEntityMapping(EntityMapping entityMapping) {
		Assert.notNull(entityMapping, "entity mapping can't be null.");
		Assert.isTrue(entityMapping.isRemote() && RemoteType.rest.equals(entityMapping.getRemoteSettings().getRemoteType())
				, "entity must be remote rest model.");
		this.entityMapping = entityMapping;
		return this;
	}

	public RestResourceBuilder setEntityClass(Class<?> cls){
		OrmContext orm= Orm.context(cls);
		if(orm==null){
			throw new RuntimeException("can't find entity mapping from cls:"+cls.getName());
		}
		EntityMapping em =orm.getMetadata().getEntityMapping(cls);
		setEntityMapping(em);
		return this;
	}

}

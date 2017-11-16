package leap.web.api.remote.ds;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leap.core.BeanFactory;
import leap.core.ioc.PostCreateBean;
import leap.lang.exception.ObjectExistsException;

public class DefaultRestDataSourceManager implements RestDatasourceManager,PostCreateBean {

	protected Map<String, RestDataSource>        allDataSources;
	protected Map<String, RestDataSource>        allDataSourcesImmutableView;

	@Override
	public boolean hasDataSources() {
		if(allDataSources==null || allDataSources.size()==0){
			return false;
		}
		return true;
	}

	@Override
	public RestDataSource tryGetDataSource(String name) {
		if(!hasDataSources()){
			return null;
		}
		return allDataSources.get(name);
	}

	@Override
	public Map<String, RestDataSource> getAllDataSources() {
		return this.allDataSourcesImmutableView;
	}

	@Override
	public void registerDataSource(String name, RestDataSource ds) throws ObjectExistsException {
		synchronized (this) {
            if(allDataSources.containsKey(name)) {
                throw new ObjectExistsException("DataSource '" + name + "' already exists!");
            }

            allDataSources.put(name, ds);
        }
	}

	@Override
	public boolean removeDataSource(String name) {
		return null != allDataSources.remove(name);
	}

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		this.allDataSources    = new ConcurrentHashMap<>(factory.getNamedBeans(RestDataSource.class));

		this.allDataSourcesImmutableView = Collections.unmodifiableMap(allDataSources);
	}
}

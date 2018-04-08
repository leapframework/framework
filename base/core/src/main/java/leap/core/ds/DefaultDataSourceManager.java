/*
 * Copyright 2014 the original author or authors.
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
package leap.core.ds;

import leap.core.AppConfig;
import leap.core.AppContext;
import leap.core.BeanFactory;
import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ds.management.MDataSource;
import leap.core.ds.management.MDataSourceConfig;
import leap.core.ds.management.MDataSourceProxy;
import leap.core.ioc.BeanList;
import leap.core.ioc.PostCreateBean;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.jmx.MBeanExporter;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Configurable(prefix = "dsm")
public class DefaultDataSourceManager implements DataSourceManager,PostCreateBean,MDataSourceConfig {

    protected @Inject @M AppContext                   context;
    protected @Inject @M AppConfig                    config;
    protected @Inject @M DataSourceFactory[]          dataSourceFactories;
    protected @Inject @M BeanList<DataSourceListener> listeners;
    protected @Inject @M MBeanExporter                mbeanExporter;

	protected String 						 defaultDataSourceBeanName;
    protected DataSource                     defaultDataSource;
    protected Map<String, DataSource>        allDataSources;
    protected Map<String, DataSource>        allDataSourcesImmutableView;
    protected UnPooledDataSourceFactory      unpooledDataSourceFactory = new UnPooledDataSourceFactory();

    protected boolean                        monitoring;
    protected long                           slowSqlThreshold;
    protected long                           verySlowSqlThreshold;
    protected boolean                        logSlowSql;
    protected boolean                        logVerySlowSql;
    protected boolean                        exportMBean;

    @ConfigProperty
    public void setMonitoring(boolean monitoring) {
        this.monitoring = monitoring;
    }

    @Override
    public long getSlowSqlThreshold() {
        return slowSqlThreshold;
    }

    @ConfigProperty
    public void setSlowSqlThreshold(long slowSqlThreshold) {
        this.slowSqlThreshold = slowSqlThreshold;
    }

    @Override
    public long getVerySlowSqlThreshold() {
        return verySlowSqlThreshold;
    }

    @ConfigProperty
    public void setVerySlowSqlThreshold(long verySlowSqlThreshold) {
        this.verySlowSqlThreshold = verySlowSqlThreshold;
    }

    public boolean isLogSlowSql() {
        return logSlowSql;
    }

    @ConfigProperty
    public void setLogSlowSql(boolean logSlowSql) {
        this.logSlowSql = logSlowSql;
    }

    public boolean isLogVerySlowSql() {
        return logVerySlowSql;
    }

    @ConfigProperty
    public void setLogVerySlowSql(boolean logVerySlowSql) {
        this.logVerySlowSql = logVerySlowSql;
    }

    public boolean isExportMBean() {
        return exportMBean;
    }

    @ConfigProperty
    public void setExportMBean(boolean exportMBean) {
        this.exportMBean = exportMBean;
    }

    @Override
    public void addListener(DataSourceListener listener) {
		if(listeners.contains(listener)){
			throw new ObjectExistsException("The listener already exists");
		}
		listeners.add(listener);
	}

	@Override
    public boolean removeListener(DataSourceListener listener) {
	    return listeners.remove(listener);
    }

	@Override
    public boolean hasDataSources() {
        return !allDataSources.isEmpty();
    }

    @Override
    public DataSource getDefaultDataSource() {
		if(null == defaultDataSource){
			throw new ObjectNotFoundException("No default dataSource defined");
		}
	    return defaultDataSource;
    }

	@Override
	public String getDefaultDataSourceBeanName() throws ObjectNotFoundException {
		return defaultDataSourceBeanName;
	}

	@Override
    public DataSource tryGetDefaultDataSource() {
	    return defaultDataSource;
    }
	
	@Override
    public DataSource getDataSource(String name) throws ObjectNotFoundException {
		DataSource ds = tryGetDataSource(name);
		
		if(null == ds){
			throw new ObjectNotFoundException("DataSource '" + name + "' not found");
		}
		
	    return ds;
    }

	@Override
    public DataSource tryGetDataSource(String name) {
	    DataSource ds = allDataSources.get(name);
	    
        if (null == ds && DEFAULT_DATASOURCE_NAME.equals(name)) {
            ds = defaultDataSource;
        }	       
	      
        return ds;
    }

	@Override
    public Map<String, DataSource> getAllDataSources() {
	    return allDataSourcesImmutableView;
    }

    @Override
    public void registerDataSource(String name, DataSource ds) throws ObjectExistsException {
        synchronized (this) {
            if(allDataSources.containsKey(name)) {
                throw new ObjectExistsException("DataSource '" + name + "' already exists!");
            }

            allDataSources.put(name, ds);

            notifyDataSourceCreated(name, ds, exportMBean);
        }
    }

    @Override
    public boolean removeDataSource(String name) {
        return null != allDataSources.remove(name);
    }

    @Override
    public DataSource createDefaultDataSource(DataSourceConfig conf) throws ObjectExistsException,SQLException {
		synchronized (this) {
			if(null != defaultDataSource){
				throw new ObjectExistsException("Default dataSource already exists");
			}

			DataSource ds = createDataSource(conf);
			
			this.defaultDataSource = ds;
			this.allDataSources.put(DEFAULT_DATASOURCE_NAME, ds);
			
			notifyDataSourceCreated(DEFAULT_DATASOURCE_NAME, ds, conf.isExportMBean());
			
			return ds;
        }
    }

	@Override
    public DataSource createDataSource(String name, DataSourceConfig props) throws ObjectExistsException,SQLException {
		synchronized (this) {
			if(allDataSources.containsKey(name)){
				throw new ObjectExistsException("DataSource '" + name + "' already exists");
			}

            if(props.isDefault() && null != defaultDataSource) {
                throw new ObjectExistsException("Default DataSource already exists!");
            }
			
			DataSource ds = createDataSource(props);
			
			allDataSources.put(name,ds);

            if(props.isDefault()) {
                defaultDataSource = ds;
            }
			
			notifyDataSourceCreated(name, ds, props.isExportMBean());
			
			return ds;
        }
    }
	
	public DataSource createDataSource(DataSourceConfig conf) throws UnsupportedOperationException, SQLException {
		DataSource ds = tryCreateDataSource(conf);
		if(null == ds){
			throw new UnsupportedOperationException("The given datasource properties does not supported");
		}
		return ds;
	}
	
	public DataSource tryCreateDataSource(DataSourceConfig conf) throws SQLException {
		DataSource ds = null;
		for(DataSourceFactory f : dataSourceFactories){
			if((ds = f.tryCreateDataSource(conf)) != null){
				break;
			}
		}
		
		if(null == ds && config.isDebug()){
			ds = unpooledDataSourceFactory.tryCreateDataSource(conf);
		}
		
		if(null != ds) {
			validateDataSource(ds);
		}

        if(monitoring && null != ds && ! (ds instanceof MDataSourceProxy)) {
            ds = new MDataSourceProxy(ds, this);
        }
		
		return ds;
	}
	
	@Override
    public void destroyDataSource(DataSource ds) throws UnsupportedOperationException {
		if(!tryDestroyDataSource(ds)){
			throw new UnsupportedOperationException("The given dataSource does not supported");
		}
    }
	
	@Override
    public boolean tryDestroyDataSource(DataSource ds) {

        try {
            DataSource real = ds;
            if(ds instanceof MDataSourceProxy) {
                real = ((MDataSourceProxy) ds).wrapped();
                ((MDataSourceProxy) ds).destroy();
            }

            for (DataSourceFactory f : dataSourceFactories) {
                if (f.tryDestroyDataSource(real)) {
                    return true;
                }
            }

            return unpooledDataSourceFactory.tryDestroyDataSource(real);
        }finally{
            String name = null;
            for(Entry<String, DataSource> entry : allDataSources.entrySet()) {
                if(entry.getValue() == ds) {
                    name = entry.getKey();
                    break;
                }
            }

            if(null != name) {
                allDataSources.remove(name);
                notifyDataSourceDestroyed(name, ds);
            }

            if(ds == defaultDataSource) {
                defaultDataSource = null;
            }
        }
    }
	
	@Override
    public void validateDataSource(DataSource ds) throws SQLException {
		try(Connection conn = ds.getConnection()){}
	}
	
	@Override
    public boolean tryValidateDataSource(DataSource ds) {
		try {
	        validateDataSource(ds);
	        return true;
        } catch (SQLException e) {
        	return false;
        }
    }

    @Override
    public MDataSource getManagedDataSource(DataSource ds) {
        return null == ds ? null : (MDataSource)ds;
    }

    protected void notifyDataSourceCreated(String name, DataSource ds, boolean exportMBean) {

        if(ds instanceof MDataSourceProxy) {
            ((MDataSourceProxy) ds).setName(name);
        }

        if(exportMBean) {
            exportDataSourceMBean(name, ds);
        }

		for(DataSourceListener l : listeners){
			l.onDataSourceCreated(name, ds);
		}
	}
	
	protected void notifyDataSourceDestroyed(String name,DataSource ds) {
        unexportDataSourceMBean(name, ds);

		for(DataSourceListener l : listeners){
			l.onDataSourceDestroyed(name, ds);
		}
	}

    protected void exportDataSourceMBean(String name, DataSource ds) {
        if(!(ds instanceof MDataSource)) {
            return;
        }
        mbeanExporter.export(objectName(name), ((MDataSource)ds).getMBean());
    }

    protected void unexportDataSourceMBean(String name, DataSource ds) {
        if(!(ds instanceof MDataSource)) {
            return;
        }
        mbeanExporter.unexport(objectName(name));
    }

    protected ObjectName objectName(String name) {
        String fullName = "DataSources:name=" + context.getName() + "_" + name;
        try {
            return new ObjectName(fullName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException(e);
        }
    }

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
        this.defaultDataSource = factory.tryGetBean(DataSource.class);
		this.allDataSources    = new ConcurrentHashMap<>(factory.getNamedBeans(DataSource.class));
		
		//Create dataSource(s) from config.
		//createDataSourcesFromConfig(factory, allDataSources);
		
		if(null == this.defaultDataSource) {
		    this.defaultDataSource = factory.tryGetBean(DataSource.class);    
		}
	    
		if(null == defaultDataSource){
			this.defaultDataSource = allDataSources.get(DEFAULT_DATASOURCE_NAME);
			
			if(null == defaultDataSource && allDataSources.size() == 1) {
			    defaultDataSource = allDataSources.values().iterator().next();
			}
		}

		if(null != defaultDataSource && !allDataSources.containsValue(this.defaultDataSource)){
		    allDataSources.put(DEFAULT_DATASOURCE_NAME, defaultDataSource);
			defaultDataSourceBeanName = DEFAULT_DATASOURCE_NAME;
		}

		if(null != defaultDataSource && allDataSources.containsValue(this.defaultDataSource)){
			for(Entry<String, DataSource> entry : allDataSources.entrySet()){
				if(entry.getValue() == this.defaultDataSource){
					defaultDataSourceBeanName = entry.getKey();
					if(null == defaultDataSourceBeanName){
                        defaultDataSourceBeanName = DEFAULT_DATASOURCE_NAME;
                    }
					break;
				}
			}
		}
		
		for(Entry<String, DataSource> entry : allDataSources.entrySet()){
			notifyDataSourceCreated(entry.getKey(), entry.getValue(), exportMBean);
		}
		
		this.allDataSourcesImmutableView = Collections.unmodifiableMap(allDataSources);
    }

    /*
	protected void createDataSourcesFromConfig(BeanFactory factory,  Map<String, DataSource> dataSourcesMap) {
		for(Entry<String, DataSourceConfig> entry : config.getDataSourceConfigs().entrySet()) {
			String           name = entry.getKey();
            DataSourceConfig conf = entry.getValue();

			if(dataSourcesMap.containsKey(name) || (conf.isDefault() && null != this.defaultDataSource)) {
				continue;
			}

			try {
			    DataSource ds = createDataSource(conf);
			    
	            dataSourcesMap.put(entry.getKey(), ds);
	            
	            if(conf.isDefault()) {
	                this.defaultDataSource = ds;
	                
	                if(null == factory.tryGetBeanExplicitly(DataSource.class)){
	                    factory.setPrimaryBean(DataSource.class, this.defaultDataSource);
	                }
	            }
            } catch (SQLException e) {
            	throw new AppConfigException("Error creating dataSource '" + entry.getKey() + ", " + e.getMessage(), e);
            }
		}
	}
	*/
}

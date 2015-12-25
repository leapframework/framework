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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import leap.lang.Args;
import leap.lang.Classes;
import leap.lang.Enums;
import leap.lang.Maps;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.extension.ExProperties;
import leap.lang.jdbc.TransactionIsolation;

public class DataSourceConfig {
	//DataSource class name
	public static final String DATASOURCE_CLASS_NAME         = "dataSourceClassName";
	
	//Common Jndi properties
	public static final String DATASOURCE_JNDI_NAME			 = "dataSourceJndiName";
	public static final String DATASOURCE_JNDI_RESOURCE_REF  = "dataSourceJndiResourceRef";
	public static final String DATASOURCE_JNID_WRAPPED		 = "dataSourceJndiWrapped";
	
	//Common Driver properties
	public static final String DRIVER_CLASS_NAME             = "driverClassName";
	public static final String JDBC_URL				         = "jdbcUrl";
	public static final String URL                           = "url"; //The alias name of jdbcUrl;
	public static final String USERNAME                      = "username";
	public static final String PASSWORD			             = "password";
	
	//Common Connection state
	public static final String DEFAULT_AUTO_COMMIT           = "defaultAutoCommit";
	public static final String DEFAULT_TRANSACTION_ISOLATION = "defaultTransactionIsolation";
	public static final String DEFAULT_READ_ONLY			 = "defaultReadOnly";
	public static final String DEFAULT_CATALOG				 = "defaultCatalog";
	
	//Common Pool properties
	public static final String MAX_ACTIVE					 = "maxActive";
	public static final String MAX_IDLE						 = "maxIdle";
	public static final String MIN_IDLE					     = "minIdle";
	public static final String MAX_WAIT						 = "maxWait";
	
	private static final Set<String> keys = new HashSet<String>();
	static {
		keys.add(DATASOURCE_CLASS_NAME);
		keys.add(DATASOURCE_JNDI_NAME);
		keys.add(DATASOURCE_JNDI_RESOURCE_REF);
		keys.add(DRIVER_CLASS_NAME);
		keys.add(JDBC_URL);
		keys.add(USERNAME);
		keys.add(PASSWORD);
		keys.add(DEFAULT_AUTO_COMMIT);
		keys.add(DEFAULT_TRANSACTION_ISOLATION);
		keys.add(DEFAULT_READ_ONLY);
		keys.add(DEFAULT_CATALOG);
		keys.add(MAX_ACTIVE);
		keys.add(MAX_IDLE);
		keys.add(MIN_IDLE);
		keys.add(MAX_WAIT);
	}
	
	public static Builder createBuilder() {
		return new Builder();
	}
	
	protected final String				 dataSourceType;
	protected final String				 dataSourceClassName;
	protected final String				 dataSourceJndiName;
	protected final Boolean				 dataSourceJndiResourceRef;
	protected final boolean				 dataSourceJndiWrapped;
	protected final String 				 driverClassName;
	protected final String   			 jdbcUrl;
	protected final String   			 username;
	protected final String   		     password;
	protected final Boolean				 defaultAutoCommit;
	protected final TransactionIsolation defaultTransactionIsolation;
	protected final Boolean				 defaultReadOnly;
	protected final String				 defaultCatalog;
	protected final Integer				 maxActive;
	protected final Integer				 maxIdle;
	protected final Integer				 minIdle;
	protected final Integer				 maxWait;
	
	protected final Map<String, String> allMap;
	protected final Map<String, String> extMap;
	
	private ExProperties allProps;
	private ExProperties extProps;
	
	private boolean _default;
	
	@SuppressWarnings("unchecked")
    public DataSourceConfig(String dataSourceType, boolean isDefault,  Map<String,String> properties) {
		Args.notEmpty(properties,"properties");
		
		this.dataSourceType				 = dataSourceType;
		this._default                    = isDefault;
		this.dataSourceClassName		 = Strings.trimToNull(properties.get(DATASOURCE_CLASS_NAME));
		
		this.dataSourceJndiName			 = Strings.trimToNull(properties.get(DATASOURCE_JNDI_NAME));
		this.dataSourceJndiResourceRef	 = Maps.getBoolean(properties, DATASOURCE_JNDI_RESOURCE_REF); 
		this.dataSourceJndiWrapped		 = Maps.getBoolean(properties, DATASOURCE_JNID_WRAPPED, false);
		
		this.driverClassName             = Strings.trimToNull(properties.get(DRIVER_CLASS_NAME));
	    this.jdbcUrl                     = Strings.firstNotEmpty(Strings.trimToNull(properties.get(JDBC_URL)),Strings.trimToNull(properties.get(URL)));
	    this.username                    = properties.get(USERNAME);
	    this.password                    = properties.get(PASSWORD);
	    
	    this.defaultAutoCommit           = Maps.getBoolean(properties, DEFAULT_AUTO_COMMIT);
	    this.defaultTransactionIsolation = getDefaultTransactionIsolation(properties);
	    this.defaultReadOnly			 = Maps.getBoolean(properties, DEFAULT_READ_ONLY);
	    this.defaultCatalog				 = properties.get(DEFAULT_CATALOG);
	    
	    this.maxActive					 = Maps.getInteger(properties, MAX_ACTIVE);
	    this.maxIdle					 = Maps.getInteger(properties, MAX_IDLE);
	    this.minIdle					 = Maps.getInteger(properties, MIN_IDLE);
	    this.maxWait					 = Maps.getInteger(properties, MAX_WAIT);
	    
	    this.allMap              		 = null == properties ? Collections.EMPTY_MAP : Collections.unmodifiableMap(properties);
	    this.extMap	   			 	     = extraceExtProperties(properties);
    }
	
	public boolean isDefault() {
	    return _default;
	}
	
	public String getDataSourceType() {
		return dataSourceType;
	}
	
	public boolean hasDataSourceClassName() {
		return null != dataSourceClassName;
	}

	public String getDataSourceClassName() {
		return dataSourceClassName;
	}
	
	public boolean hasDataSourceJndiName() {
		return null != dataSourceJndiName;
	}

	public String getDataSourceJndiName() {
		return dataSourceJndiName;
	}
	
	public boolean hasDataSourceJndiResourceRef() {
		return null != dataSourceJndiResourceRef;
	}

	public Boolean getDataSourceJndiResourceRef() {
		return dataSourceJndiResourceRef;
	}
	
	public boolean isDataSourceJndiWrapped() {
		return dataSourceJndiWrapped;
	}

	public boolean hasDriverClassName() {
		return null != driverClassName;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public boolean hasDefaultAutoCommit() {
		return null != defaultAutoCommit;
	}
	
	public Boolean getDefaultAutoCommit() {
		return defaultAutoCommit;
	}
	
	public boolean hasDefaultTransactionIsolation() {
		return null != defaultTransactionIsolation;
	}
	
	public TransactionIsolation getDefaultTransactionIsolation() {
		return defaultTransactionIsolation;
	}
	
	public boolean hasDefaultReadOnly() {
		return null != defaultReadOnly;
	}
	
	public Boolean getDefaultReadOnly() {
		return defaultReadOnly;
	}
	
	public boolean hasDefaultCatalog() {
		return null != defaultCatalog;
	}

	public String getDefaultCatalog() {
		return defaultCatalog;
	}
	
	public boolean hasMaxActive() {
		return null != maxActive;
	}
	
	public Integer getMaxActive() {
		return maxActive;
	}

	public boolean hasMaxIdle() {
		return null != maxIdle;
	}
	
	public Integer getMaxIdle() {
		return maxIdle;
	}
	
	public boolean hasMinIdle() {
		return null != minIdle;
	}

	public Integer getMinIdle() {
		return minIdle;
	}
	
	public boolean hasMaxWait() {
		return null != maxWait;
	}

	public Integer getMaxWait() {
		return maxWait;
	}
	
	public boolean hasProperty(String name) {
		return allMap.containsKey(name);
	}
	
	public String getProperty(String name) {
		return allMap.get(name);
	}
	
	public ExProperties props() {
		if(null == allProps) {
			allProps = ExProperties.create(allMap);
		}
		return allProps;
	}

	/**
	 * Returns an immutable {@link Map} contains all the properties.
	 */
	public Map<String, String> map() {
		return allMap;
	}
	
	public ExProperties getExtProperties() {
		if(null == extProps) {
			extProps = ExProperties.create(extMap);
		}
		return extProps;
	}
	
	/**
	 * Returns an immutable {@link Map} contains all the ext properties.
	 * 
	 * <p>
	 * A ext property is a propety not in the built-in property keys.
	 */
	public Map<String, String> getExtPropertiesMap() {
		return extMap;
	}
	
	/**
	 * Injects the properties into the given bean.
	 */
	public void inject(Object bean) {
		Args.notNull(bean,"bean");
	
		BeanType bt = BeanType.of(bean.getClass());
		
		for(Entry<String, String> p : allMap.entrySet()){
			String key = p.getKey();
			String val = p.getValue();
			
			if(!Strings.isEmpty(val)){
				BeanProperty bp = bt.tryGetProperty(key);
				if(null != bp){
					Object pv = bp.getValue(bean);
					if(null == pv || pv == Classes.getDefaultValue(bp.getType())){
						bp.setValue(bean, val);
					}
				}
			}
		}
	}
	
	private TransactionIsolation getDefaultTransactionIsolation(Map<String, String> all) {
		String v = all.get(DEFAULT_TRANSACTION_ISOLATION);
		if(Strings.isEmpty(v)){
			return null;
		}
		
		if(Strings.isDigits(v)){
			return Enums.valueOf(TransactionIsolation.class, Integer.parseInt(v));
		}else{
			return Enums.nameOf(TransactionIsolation.class, v);
		}
	}
	
	private Map<String, String> extraceExtProperties(Map<String, String> all){
		Map<String, String> dbps = new LinkedHashMap<>();
		for(Entry<String, String> entry : all.entrySet()){
			if(!keys.contains(entry.getKey())){
				dbps.put(entry.getKey(), entry.getValue());
			}
		}
		return Collections.unmodifiableMap(dbps);
	}
	
	public static final class Builder {
		
		private String dataSourceType;
		private boolean _default;
		private final Map<String, String> map = new LinkedHashMap<>();
		
		public Builder(){
			
		}
		
		public Builder(String driverClassName,String url,String username,String password) {
			this.setDriverClassName(driverClassName)
				.setUrl(url)
				.setUsername(username)
				.setPassword(password);
		}
		
		public Builder(Map<String, String> properties){
			if(null != properties){
				this.map.putAll(properties);
			}
		}
		
		public Builder(Properties properties){
			if(null != properties){
				this.map.putAll(ExProperties.create(properties).toMap());
			}
		}
		
		public String getDataSourceType() {
			return dataSourceType;
		}

		public Builder setDataSourceType(String dataSourceType) {
			this.dataSourceType = dataSourceType;
			return this;
		}

		public Builder setDriverClassName(String driverClassName){
			return setProperty(DRIVER_CLASS_NAME, driverClassName);
		}
		
		public Builder setUrl(String url){
			return setProperty(JDBC_URL, url);
		}
		
		public Builder setJdbcUrl(String url) {
		    return setProperty(JDBC_URL, url);
		}
		
		public Builder setUsername(String username){
			return setProperty(USERNAME, username);
		}
		
		public Builder setPassword(String password) {
			return setProperty(PASSWORD, password);
		}
		
		public Builder setProperty(String name, String value){
			map.put(name, value);
			return this;
		}
		
		public Builder setDefault(boolean isDefault) {
		    this._default = isDefault;
		    return this;
		}
		
		public DataSourceConfig build() {
			return new DataSourceConfig(dataSourceType,_default,map);
		}
	}
}
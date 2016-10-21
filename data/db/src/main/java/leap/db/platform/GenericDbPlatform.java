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
package leap.db.platform;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.LocalizeKey;
import leap.core.annotation.M;
import leap.core.i18n.I18N;
import leap.core.i18n.MessageSource;
import leap.core.ioc.PostCreateBean;
import leap.db.DbBase;
import leap.db.DbDriver;
import leap.db.DbPlatformBase;
import leap.lang.exception.NestedIOException;
import leap.lang.json.JSON;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@LocalizeKey("db.platform")
public abstract class GenericDbPlatform extends DbPlatformBase implements PostCreateBean {
	
	protected static final GenericDbDriver[] EMPTY_DRIVES = new GenericDbDriver[]{};

    protected @Inject @M MessageSource       messageSource;
    protected @Inject @M GenericDbDriver[]   drivers;
	
	protected GenericDbPlatform(String type) {
	    super(type);
    }
	
	protected GenericDbPlatform(String type, Function<DatabaseMetaData,Boolean> matcher) {
	    super(type, matcher);
    }
	
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
    public DbDriver[] getDrivers() {
	    return drivers;
    }

	@Override
    public List<DbDriver> getDrivers(boolean available) {
		List<DbDriver> list = new ArrayList<>();
		
		for(int i=0;i<drivers.length;i++){
			DbDriver driver = drivers[i];
			
			if(!available || driver.isAvailable()){
				list.add(driver);
			}
		}
		
	    return list;
    }

	@Override
    protected DbBase doTryCreateDbInstance(String name,DataSource ds, Connection connection, DatabaseMetaData jdbcMetadata) throws SQLException {
		GenericDbDialect        dialect        = createDialect(jdbcMetadata);
		GenericDbMetadataReader metadataReader = createMetadataReader(jdbcMetadata);
		GenericDbMetadata       metadata       = createMetadata(connection, jdbcMetadata, dialect.getDefaultSchemaName(connection, jdbcMetadata),metadataReader);
		GenericDbComparator     comparator     = createComparator(jdbcMetadata);
		
		metadataReader.init(dialect);
		
	    return createDb(name, ds, jdbcMetadata, metadata, dialect,comparator);
    }
	
	protected abstract GenericDbDialect createDialect(DatabaseMetaData jdbcMetadata) throws SQLException;
	
	protected GenericDbMetadataReader createMetadataReader(DatabaseMetaData jdbcMetadata) throws SQLException {
		return new GenericDbMetadataReader();
	}
	
	protected GenericDbMetadata createMetadata(Connection connection,
											   DatabaseMetaData jdbcMetadata,
											   String defaultSchemaName,
											   GenericDbMetadataReader metadataReader) throws SQLException{
		
		return new GenericDbMetadata(jdbcMetadata,defaultSchemaName,metadataReader);
		
	}
	
	protected GenericDbComparator createComparator(DatabaseMetaData jdbcMetadata) throws SQLException {
		return new GenericDbComparator();
	}
	
	protected GenericDb createDb(String name,DataSource ds, DatabaseMetaData dmd, GenericDbMetadata metadata,GenericDbDialect dialect, GenericDbComparator comparator) {
		return new GenericDb(name, ds, dmd, this, metadata, dialect, comparator);
	}
	
	@Override
    public void postCreate(BeanFactory beanFactory) throws Throwable {
		//load drivers
		this.drivers = loadDrivers();
		
		//read localized properties
		I18N.localize(messageSource, this);
	}

	protected GenericDbDriver[] loadDrivers() {
		String configFile = this.getClass().getSimpleName() + ".driver.json";
				
		Resource r = Resources.getResource(this.getClass(),configFile);
		
		if(!r.exists()){
			return EMPTY_DRIVES;
		}
		
		try {
			String keyPrefix = I18N.getLocalizedKeyPrefix(this) + ".drivers";
			
	        try(Reader reader = r.getInputStreamReader()){
	        	GenericDbDriver[] drivers = JSON.decodeArray(reader, GenericDbDriver.class);
	        	
	        	for(GenericDbDriver driver : drivers){
	        		I18N.localize(messageSource, driver, keyPrefix + "." + driver.getName());
	        	}
	        	
	        	return drivers;
	        }
        } catch (IOException e) {
        	throw new NestedIOException("Error reading driver config file '" + r.getDescription() + "', " + e.getMessage(), e);
        }
	}
}
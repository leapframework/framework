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

import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import leap.core.BeanFactory;
import leap.core.ioc.PostCreateBean;
import leap.lang.Arrays2;
import leap.lang.Locales;
import leap.lang.Strings;
import leap.lang.csv.CSV;
import leap.lang.csv.CsvProcessor;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.orm.domain.FieldDomain;

public class DefaultDomainDatas implements DomainDatas,PostCreateBean {
	
	private static final Log log = LogFactory.get(DefaultDomainDatas.class);

	private static final String   SYS_RESOURCE_PREFIX    = "META-INF/leap/data/domain/";
	private static final String[] SYS_RESORUCE_LOCATIONS = new String[]{
																		  "classpath*:" + SYS_RESOURCE_PREFIX + "*.csv",
																		  "classpath*:" + SYS_RESOURCE_PREFIX + "*/*.csv"
																		 };
	
	private static final String   USR_RESOURCE_PREFIX    = "data/domain/";
	private static final String[] USR_RESORUCE_LOCATIONS = new String[]{
																		  "classpath*:" + USR_RESOURCE_PREFIX + "*.csv",
																		  "classpath*:" + USR_RESOURCE_PREFIX + "*/*.csv"
																		 };	
	
	protected final Map<String, DomainData> datas = new HashMap<String, DomainData>();
	
	@Override
    public void postCreate(BeanFactory factory) throws Exception {
	    loadDatas(factory.getAppConfig().getDefaultLocale(),SYS_RESOURCE_PREFIX,SYS_RESORUCE_LOCATIONS);
	    loadDatas(factory.getAppConfig().getDefaultLocale(),USR_RESOURCE_PREFIX,USR_RESORUCE_LOCATIONS);
    }

	@Override
    public DomainData tryGetDomainData(FieldDomain domain) {
	    return datas.get(Strings.lowerCase(domain.getQualifiedName()));
    }

	@Override
    public DomainData tryGetDomainData(String qualifedDomainName) {
	    return datas.get(Strings.lowerCase(qualifedDomainName));
    }

	private static String dataKey(String entityName,String fieldName){
		return Strings.lowerCase(Strings.isEmpty(entityName) ? fieldName : (entityName + "." + fieldName));
	}
	
	protected void loadDatas(Locale defaultLocale, String locationPrefix,String[] locations){
		ResourceSet resources = Resources.scan(locations);
		
		if(!resources.isEmpty()){
			
			log.debug("Loading domains datas from {} resources",resources.size());
			
            for(final Resource resource : resources) {
            	String relativePath = resource.getClasspath().substring(locationPrefix.length());
            	String filename     = Paths.getFileNameWithoutExtension(Paths.getFileName(relativePath));
            	String entityName     = Strings.substringBefore(relativePath, "/");
            	String localeName   = Locales.extractFromFilename(filename);
            	String domainName   = Strings.isEmpty(localeName) ? filename : filename.substring(0,filename.length() - localeName.length() - 1);
            	String dataKey      = dataKey(entityName, domainName);
            	
            	final Locale locale = !Strings.isEmpty(localeName) ? Locales.forName(localeName) : defaultLocale;
            	
            	DomainData existsData = datas.get(dataKey);
            	if(null == existsData){
            		existsData = new DomainData();
            		datas.put(dataKey, existsData);
            	}
            	
            	final DomainData data = existsData;
            	
            	Reader reader = null;
            	try{
            		reader = resource.getInputStreamReader();
	            	CSV.read(reader, new CsvProcessor() {
	            		@Override
	            		public void process(int rownum, String[] values) throws Exception {
	            			if(rownum == 1){
	            				String[] cols = data.cols();
	            				if(null == cols){
	            					data.setCols(values);
	            				}else if(!Arrays2.equals(cols, values, true)){
	            					throw new IllegalStateException("Headers in resource '" + resource.getClasspath() + "' should be '" + Strings.join(cols,","));
	            				}
	            				return;
	            			}
	            			
	            			String[] cols = data.cols();
	            			
            				if(values.length > cols.length){
            					throw new IllegalStateException("Column size '" + values.length +
            													"' exceed the header size '" + cols.length + 
            												    " in row " + rownum + " : " + Strings.join(values,",") + 
            												    ", check the source : " + resource.getClasspath());
            				}
            				
            				if(values.length < cols.length){
            					values = Arrays.copyOf(values, cols.length);
            				}

            				data.addRow(locale,values);
	            		}
	            	});
            	}finally{
            		IO.close(reader);
            	}
            }
		}
	}
}
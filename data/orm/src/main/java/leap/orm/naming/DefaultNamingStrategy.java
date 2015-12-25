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
package leap.orm.naming;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import leap.core.AppConfigException;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.core.validation.annotations.NotNull;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.csv.CSV;
import leap.lang.csv.CsvProcessor;
import leap.lang.io.IO;
import leap.lang.naming.NamingStyle;
import leap.lang.naming.NamingStyles;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.orm.OrmConfig;

public class DefaultNamingStrategy implements NamingStrategy,PostCreateBean {

	private static final String[] PLURALS_LOCATIONS = new String[]{
																	"classpath*:/META-INF/leap/data/plurals.csv",
																	"classpath:/data/plurals.csv"
																   };
	
	private static final String[] ACRONYMS_LOCATIONS = new String[]{
																	"classpath*:/META-INF/leap/data/acronyms.csv",
																	"classpath:/data/acronyms.csv",
																	};
	
	private static final Map<String, String>      singuralToPlurals  = new HashMap<String, String>();
	private static final Map<String, String>      pluralToSingurals  = new HashMap<String, String>();
	private static final Map<String, Set<String>> singuralToAcronyms = new HashMap<String, Set<String>>();
	private static final Set<String> 			  allAcronyms        = new HashSet<String>();
	
	static {
		loadPlurals();
		loadAcronyms();
	}
	
	@Inject
	@NotNull
	protected OrmConfig   ormConfig;
	
	protected NamingStyle tableNamingStyle;
	protected NamingStyle columnNamingStyle;
	
	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		tableNamingStyle  = NamingStyles.get(ormConfig.getTableNamingStyle(), factory);
		columnNamingStyle = NamingStyles.get(ormConfig.getColumnNamingStyle(), factory);
		
		if(null == tableNamingStyle) {
			throw new AppConfigException("Table naming style '" + ormConfig.getTableNamingStyle() + "' not found");
		}
		
		if(null == columnNamingStyle) {
			throw new AppConfigException("Column naming style '" + ormConfig.getColumnNamingStyle() + "' not found");
		}
    }

	@Override
    public boolean isPluralOf(String plural, String singural) {
		plural   = plural.toLowerCase();
		singural = singural.toLowerCase();
		
		String pluralToSingural = pluralToSingurals.get(plural);
		
		if(null != pluralToSingural && pluralToSingural.equals(singural)){
			return true;
		}
		
		//reference : http://en.wikipedia.org/wiki/English_plurals
		
		//Regular plurals
		if(plural.equals(singural + "s") || plural.equals(singural + "es")){
			return true;
		}
		
		//Plurals of nouns in -y
		if(singural.endsWith("y") && plural.endsWith("ies") && 
		   Strings.removeEnd(singural, "y").equals(Strings.equals(plural, "ies"))){
			return true;
		}
		
		//Plurals of nouns in -f
		if(singural.endsWith("f") && plural.endsWith("ves") && 
				   Strings.removeEnd(singural, "f").equals(Strings.equals(plural, "ves"))){
					return true;
				}
		
		//Plurals of nouns in -fe
		if(singural.endsWith("fe") && plural.endsWith("ves") && 
				   Strings.removeEnd(singural, "fe").equals(Strings.equals(plural, "ves"))){
					return true;
				}		
		
	    return false;
    }
	
	@Override
    public boolean isTableOfEntity(String tableName, String entityName) {
		String tableToEntityName = tableToEntityName(tableName);
		
		//equals
		if(Strings.equalsIgnoreCase(entityName, tableToEntityName)){
			return true;
		}
		
		//singular <-> plural
		if(isPluralOf(entityName, tableToEntityName) || isPluralOf(tableToEntityName, entityName)){
			return true;
		}
		
		return false;
    }
	
	@Override
    public boolean isTableOfWithAcronym(String tableName, String entityName) {
		
		//TODO : Optimize
		if(tableName.indexOf('_') < 0){
			return false;
		}
		
		//table starts with acronym
		tableName  = tableName.toLowerCase();
		entityName = entityName.toLowerCase();
		
		String singular = singuralToPlurals.get(entityName);
		if(null == singular){
			singular = entityName;
		}
		
		Set<String> acronymSet = singuralToAcronyms.get(singular);
		if(null != acronymSet){
			for(String acronym : acronymSet){
				if(isTableOfWithAcronym(acronym,tableName,entityName)){
					return true;
				}
			}
		}

		for(String acronym : allAcronyms){
			if(isTableOfWithAcronym(acronym, tableName, entityName)){
				return true;
			}
		}
		
		return false;
    }

	protected boolean isTableOfWithAcronym(String acronym,String tableName,String entityName){
		if(tableName.startsWith(acronym + "_")) {
			
			String tableToEntityName = tableToEntityName(tableName.substring(acronym.length() + 1));
			
			//equals
			if(Strings.equalsIgnoreCase(entityName, tableToEntityName)){
				return true;
			}
			
			//singular <-> plural
			if(isPluralOf(entityName, tableToEntityName) || isPluralOf(tableToEntityName, entityName)){
				return true;
			}
		}
		
		return false;
	}
	
	@Override
    public boolean isColumnOfField(String columnName, String fieldName) {
		String columnToFieldName = columnToFieldName(columnName);
		
		//equals
		if(Strings.equalsIgnoreCase(fieldName, columnToFieldName)){
			return true;
		}
		
		//singular <-> plural
		if(isPluralOf(fieldName, columnToFieldName) || isPluralOf(columnToFieldName, fieldName)){
			return true;
		}
		
	    return false;
    }

	@Override
	public String entityName(String entityName) {
		return Strings.upperFirst(entityName);
	}

	@Override
	public String tableName(String tableName) {
		return Strings.lowerCase(tableName);
	}

	@Override
	public String entityToTableName(String entityName) {
		return tableName(tableNamingStyle.of(entityName));
	}

	@Override
    public String tableToEntityName(String tableName) {
		return Strings.upperCamel(tableName, '_');
    }

	@Override
    public String fieldName(String fieldName) {
	    return Strings.lowerFirst(fieldName);
    }

	@Override
    public String fieldToColumnName(String fieldName) {
	    return columnName(columnNamingStyle.of(fieldName));
    }

	@Override
    public String columnName(String columnName) {
	    return Strings.lowerCase(columnName);
    }
	
	@Override
    public String columnToFieldName(String columnName) {
		if(columnName.indexOf('_') >= 0){
			return Strings.lowerCamel(columnName,'_');
		}else{
			return columnName;
		}
    }
	
	@Override
    public String generateSequenceName(String tableName, String identityColumnName) {
		//${tableName}_SEQ
	    if(tableName.endsWith("_")){
	    	return Strings.upperCase(tableName) + "SEQ";
	    }else{
	    	return Strings.upperCase(tableName) + "_SEQ";
	    }
    }
	
	@Override
    public String getFieldNameForNewValue(String fieldName) {
	    return "new" + Strings.upperFirst(fieldName);
    }

	@Override
    public String getLocalFieldName(String referencedEntityName, String referencedFieldName) {
		return Strings.lowerFirst(referencedEntityName) + Strings.upperFirst(referencedFieldName); 
    }
	
	@Override
    public String getForeignKeyName(String localEntityName, String referencedEntityName, String relationName) {
	    return "fk_" + Strings.lowerUnderscore(localEntityName) + "_" + Strings.lowerUnderscore(relationName);
    }
	
	@Override
    public String getJoinEntityName(String entityName, String targetEntityName) {
	    return Strings.upperCamel(entityName,targetEntityName);
    }

	protected String unerscores(String name){
		name = Strings.lowerUnderscore(name);
		return name.indexOf('_') > 0 ? name : name + "_";
	}
	
	private static void loadPlurals() {
		for(Resource resource : Resources.scan(PLURALS_LOCATIONS)){
			if(!resource.exists()){
				continue;
			}
			
			Reader reader = null;
			try{
				reader = resource.getInputStreamReader();
				
				CSV.read(reader,new CsvProcessor() {
					@Override
					public void process(int rownum, String[] values) throws Exception {
						//skip header
						if(rownum == 1){
							return;
						}
						
						//columns : singular,plural
						String singular = values[0].toLowerCase();
						String plural   = values[1].toLowerCase();
						
						singuralToPlurals.put(singular, plural);
						pluralToSingurals.put(plural, singular);
					}
				});
				
			}finally{
				IO.close(reader);
			}
		}
	}
	
	private static void loadAcronyms(){
		for(Resource resource : Resources.scan(ACRONYMS_LOCATIONS)){
			if(!resource.exists()){
				continue;
			}
			
			Reader reader = null;
			try{
				reader = resource.getInputStreamReader();
				CSV.read(reader,new CsvProcessor() {
					@Override
					public void process(int rownum, String[] values) throws Exception {
						//skip header
						if(rownum == 1){
							return;
						}
						
						//columns : singular,acronyms
						String singular = values[0].toLowerCase();
						String acronyms = values[1].toLowerCase();

						Set<String> acronymSet = New.linkedHashSet(Strings.split(acronyms,'|'));
						
						singuralToAcronyms.put(singular, acronymSet);
						allAcronyms.addAll(acronymSet);
					}
				});
			}finally{
				IO.close(reader);
			}
		}
	}
}
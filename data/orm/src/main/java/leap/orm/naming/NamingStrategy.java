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


public interface NamingStrategy {
	
	boolean isPluralOf(String plural,String singural);
	
	boolean isTableOfEntity(String tableName,String entityName);
	
	boolean isTableOfWithAcronym(String tableName,String entityName);
	
	boolean isColumnOfField(String columnName,String fieldName);
	
	String entityName(String entityName);
	
	String tableName(String tableName);
	
	String entityToTableName(String entityName);
	
	String tableToEntityName(String tableName);
	
	String fieldName(String fieldName);
	
	String fieldToColumnName(String fieldName);
	
	String columnName(String columnName);
	
	String columnToFieldName(String columnName);
	
	String generateSequenceName(String tableName,String identityColumnName);
	
	String getFieldNameForNewValue(String fieldName);
	
	String getLocalFieldName(String referencedEntityName,String referencedFieldName);
	
	String getForeignKeyName(String localEntityName,String referencedEntityName,String relationName);
	
	String getJoinEntityName(String entityName,String targetEntityName);
}
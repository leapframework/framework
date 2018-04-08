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
package leap.orm.reader;

import leap.core.exception.TooManyRecordsException;
import leap.core.value.Record;
import leap.core.value.SimpleRecord;
import leap.db.DbDialect;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.convert.Converts;
import leap.lang.jdbc.JdbcTypes;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.reflect.Reflection;
import leap.lang.value.Null;
import leap.orm.OrmContext;
import leap.orm.mapping.*;
import leap.orm.model.Model;
import leap.orm.naming.NamingStrategy;
import leap.orm.sql.SqlContext;
import leap.orm.value.Entity;
import leap.orm.value.EntityBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultEntityReader implements EntityReader {
	
	private static final Log log = LogFactory.get(DefaultEntityReader.class);
	
	protected final Map<Class<?>, Map<String,Object>> beanColumnMappings = new ConcurrentHashMap<Class<?>, Map<String,Object>>();

	@Override
    public <T> T readFirst(OrmContext context, SqlContext sqlContext, ResultSet rs, EntityMapping em, Class<T> resultClass) throws SQLException {
		if(rs.next()){
			ResultSetMapping rsm = createResultSetMapping(context, sqlContext,rs, em);
			return readCurrentRow(context, rs, rsm, resultClass);
		}
		return null;
    }

	@Override
	public <T> T readSingle(OrmContext context, SqlContext sqlContext, ResultSet rs, EntityMapping em, Class<T> resultClass) throws SQLException, TooManyRecordsException {
		if(rs.next()){
			ResultSetMapping rsm = createResultSetMapping(context, sqlContext,rs, em);
			
			T result = readCurrentRow(context, rs, rsm, resultClass);
			
			if(rs.next()){
				throw new TooManyRecordsException("Found two or more results in the returned result set");
			}
			
			return result;
		}
		
		return null;
	}
	
	@Override
    public <T> List<T> readList(OrmContext context, SqlContext sqlContext, ResultSet rs, EntityMapping em, Class<T> elementType, Class<? extends T> resultClass) throws SQLException {
		List<T> list = new ArrayList<T>();
		
		if(rs.next()){
			ResultSetMapping rsm = createResultSetMapping(context, sqlContext, rs, em);
			do{
				list.add(readCurrentRow(context, rs, rsm, resultClass));
			}while(rs.next());
		}
		
		if(log.isDebugEnabled()){
			log.debug("Read {} rows of '{}' from result set",list.size(),em.getEntityName());	
		}
		
		return list;
    }
	
	@SuppressWarnings("unchecked")
    protected <T> T readCurrentRow(OrmContext context, ResultSet rs,ResultSetMapping rsm,Class<T> resultClass) throws SQLException {
        if(Record.class.equals(resultClass)) {
            return (T)readRecord(context, rs, rsm);
        }
		
		if(Model.class.isAssignableFrom(resultClass)){
			return (T)readModel(context, rs, rsm,(Class<? extends Model>)resultClass);
		}
		
		if(Map.class.equals(resultClass)){
			return (T)readMap(context, rs,rsm);
		}

        if(Entity.class.equals(resultClass) || EntityBase.class.equals(resultClass)){
            return (T)readEntity(context, rs, rsm);
        }

        return readBean(context, rs,rsm,resultClass);
	}
	
	protected ResultSetMapping createResultSetMapping(OrmContext context,SqlContext sqlContext, ResultSet rs,EntityMapping em) throws SQLException {
		return new DefaultResultSetMapping(context, sqlContext, rs, em);
	}

    protected Record readRecord(OrmContext context, ResultSet rs,ResultSetMapping rsm) throws SQLException {
        Record entity = new SimpleRecord();

        readMap(context, rs, rsm, entity);

        return entity;
    }
	
	protected EntityBase readEntity(OrmContext context, ResultSet rs,ResultSetMapping rsm) throws SQLException {
		Entity entity = new Entity(rsm.getPrimaryEntityMapping().getEntityName());
		
		readMap(context, rs, rsm, entity);

		return entity;
	}
	
	protected Object readModel(OrmContext context,ResultSet rs,ResultSetMapping rsm,Class<? extends Model> modelClass) throws SQLException {
		Model model = Reflection.newInstance(modelClass);

        DbDialect dialect = context.getDb().getDialect();

        for(int i=0;i<rsm.getColumnCount();i++){
            ResultColumnMapping cm = rsm.getColumnMapping(i);
            FieldMapping  fm = cm.getFieldMapping();

            String name  = cm.getColumnLabel();
            Object value = readColumnValue(dialect, rs, cm, fm, i+1);

            if(null != fm) {
                name = fm.getFieldName();
            } else {
				name = getKey(context, cm, name);
			}
			
            model.set(name, value);
			if(cm.isAlias() && !name.equals(cm.getAliasName())){
				model.set(cm.getAliasName(), value);
			}
        }

		return model;
	}
	
	protected Map<String, Object> readMap(OrmContext context, ResultSet rs,ResultSetMapping rsm) throws SQLException {
		Map<String,Object> map = new LinkedHashMap<String, Object>(rsm.getColumnCount());
		
		readMap(context, rs, rsm, map);
		
		return map;
	}
	
	protected <T> T readBean(OrmContext context, ResultSet rs,ResultSetMapping rsm,Class<T> beanClass) throws SQLException {
		BeanType beanType = BeanType.of(beanClass);
		
		T bean = beanType.newInstance();
		
		DbDialect dialect = context.getDb().getDialect();
		
		for(int i=0;i<rsm.getColumnCount();i++){
			ResultColumnMapping cm = rsm.getColumnMapping(i);
			FieldMapping  fm = cm.getFieldMapping();
			BeanProperty  bp;
			
			if(null != fm && beanClass.equals(cm.getEntityMapping().getEntityClass())){
				bp = fm.getBeanProperty();
			}else{
				if(null != fm){
					bp = beanType.tryGetProperty(fm.getFieldName());
				}else{
					bp = getBeanPropertyByColumn(context,beanType, cm);
				}
			}
			
			if(null != bp){
                Object value = readColumnValue(dialect, rs, cm, fm, i+1);
				bp.setValue(bean, value);
			}
		}
		
		return bean;
	}
	
	protected void readMap(OrmContext context,ResultSet rs,ResultSetMapping rsm,Map<String,Object> map) throws SQLException {
		DbDialect dialect = context.getDb().getDialect();
		
		for(int i=0;i<rsm.getColumnCount();i++){
			ResultColumnMapping cm = rsm.getColumnMapping(i);
			FieldMapping  fm = cm.getFieldMapping();
			
			Object value = readColumnValue(dialect, rs, cm, fm, i+1);
			
			if(null == fm) {
				String key = cm.getColumnLabel();
				key = getKey(context, cm, key);
				map.put(key, value);
			}else{
                map.put(fm.getFieldName(), value);
            }
		}
	}

	private String getKey(OrmContext context, ResultColumnMapping cm, String key) {
		if(cm.isAlias()){
			return cm.getAliasName();
		}
		if(Strings.isBlank(cm.getColumnLabel()) || Strings.equals(cm.getColumnLabel(), cm.getColumnName())) {
            key = Strings.lowerCamel(cm.getColumnName(), '_');
        }
		return key;
	}

	protected Object readColumnValue(DbDialect dialect,ResultSet rs,ResultColumnMapping cm,FieldMapping fm, int index) throws SQLException{
		Object value = dialect.getColumnValue(rs, index, cm.getColumnType());
		
		if(null != value){
            if(null == fm) {
                Class<?> targetType = JdbcTypes.forTypeCode(cm.getColumnType()).getDefaultReadType();
                value = Converts.convert(value, targetType);
            }else{
                BeanProperty bp = fm.getBeanProperty();

                if(null != bp) {

                    if(null != fm.getSerializer()) {
                        value = fm.getSerializer().deserialize(fm, value, bp.getType(), bp.getGenericType());
                    }else{
                        value = Converts.convert(value, bp.getType(), bp.getGenericType());
                    }

                }else {

                    if(null != fm.getSerializer()){
                        value = fm.getSerializer().deserialize(fm, value);
                    }

                }
            }
		}
		
		return value;
	}

	protected BeanProperty getBeanPropertyByColumn(OrmContext context, BeanType beanType,ResultColumnMapping cm){
		Map<String, Object> mappings = beanColumnMappings.get(beanType.getBeanClass());
		
		String columnName = cm.getColumnLabel();
		
		if(null == mappings){
			mappings = new ConcurrentHashMap<>();
			beanColumnMappings.put(beanType.getBeanClass(), mappings);
		}else{
			Object bp = mappings.get(columnName);
			if(null != bp){
				if(Null.is(bp)){
					return null;
				}
				return (BeanProperty)bp;
			}
		}
		
		NamingStrategy namingStrategy = context.getNamingStrategy();
		
		for(BeanProperty bp : beanType.getProperties()){
			if(bp.getName().equalsIgnoreCase(namingStrategy.columnToFieldName(columnName))){
				mappings.put(columnName, bp);
				return bp;
			}
		}
		
		mappings.put(columnName, Null.VALUE);
		return null;
	}
}
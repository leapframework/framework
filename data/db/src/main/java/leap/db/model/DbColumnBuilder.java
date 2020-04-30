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
package leap.db.model;

import leap.lang.Args;
import leap.lang.Assert;
import leap.lang.Buildable;
import leap.lang.Strings;
import leap.lang.jdbc.JdbcTypes;
import leap.lang.json.JsonObject;
import leap.lang.json.JsonParsable;
import leap.lang.json.JsonValue;

import java.sql.Types;

public class DbColumnBuilder implements Buildable<DbColumn>,JsonParsable {
	
	public static DbColumnBuilder guid(String name){
		return new DbColumnBuilder(name, Types.VARCHAR, 38);
	}
	
	public static DbColumnBuilder varchar(String name,int length){
		return new DbColumnBuilder(name, Types.VARCHAR, length);
	}
	
	public static DbColumnBuilder bool(String name){
		return new DbColumnBuilder(name, Types.BOOLEAN);
	}
	
	public static DbColumnBuilder clob(String name){
		return new DbColumnBuilder(name, Types.CLOB);
	}
	
	public static DbColumnBuilder blob(String name){
		return new DbColumnBuilder(name, Types.BLOB);
	}
	
	public static DbColumnBuilder smallint(String name){
		return new DbColumnBuilder(name, Types.SMALLINT);
	}
	
	public static DbColumnBuilder integer(String name){
		return new DbColumnBuilder(name, Types.INTEGER);
	}
	
	public static DbColumnBuilder bigint(String name){
		return new DbColumnBuilder(name, Types.BIGINT);
	}
	
	public static DbColumnBuilder decimal(String name){
		return new DbColumnBuilder(name, Types.DECIMAL).setPrecision(19).setScale(4);
	}
	
	public static DbColumnBuilder timestamp(String name){
		return new DbColumnBuilder(name, Types.TIMESTAMP);
	}

	protected String  name;
	protected Integer typeCode;
	protected String  typeName;
	protected String  specialType;
	protected String  nativeType;
	protected Integer length;
	protected Integer precision;
	protected Integer scale;
	protected Boolean nullable;
	protected Boolean primaryKey;
	protected Boolean unique;
	protected Boolean autoIncrement;
	protected String  defaultValue;
	protected String  comment;

	public DbColumnBuilder(){
		
	}

    public DbColumnBuilder(DbColumnBuilder template) {
        this.name = template.name;
        this.typeCode = template.typeCode;
        this.typeName = template.typeName;
        this.specialType = template.specialType;
        this.nativeType = template.nativeType;
        this.length = template.length;
        this.precision = template.precision;
        this.scale = template.scale;
        this.nullable = template.nullable;
        this.primaryKey = template.primaryKey;
        this.unique = template.unique;
        this.autoIncrement = template.autoIncrement;
        this.defaultValue = template.defaultValue;
        this.comment = template.comment;
    }
	
	public DbColumnBuilder(String name){
		this.name = name;
	}
	
	public DbColumnBuilder(String name,int typeCode){
		this(name,typeCode,0);
	}
	
	public DbColumnBuilder(String name,int typeCode,int length) {
		this(name,typeCode,length,true);
	}
	
	public DbColumnBuilder(String name,int typeCode,int length,boolean nullable) {
		this.name     = name;
		this.typeCode = typeCode;
		this.typeName = JdbcTypes.forTypeCode(typeCode).getName();
		this.length   = length;
		this.nullable = nullable;
	}
	
	public DbColumnBuilder(DbColumn cloneFrom){
		Args.notNull(cloneFrom,"the clone from");
		
		this.name          = cloneFrom.getName();
		this.typeCode      = cloneFrom.getTypeCode();
		this.typeName      = cloneFrom.getTypeName();
		this.specialType   = cloneFrom.getSpecialType();
		this.nativeType    = cloneFrom.getNativeType();
		this.length        = cloneFrom.getLength();
		this.precision     = cloneFrom.getPrecision();
		this.scale         = cloneFrom.getScale();
		this.nullable      = cloneFrom.isNullable();
		this.primaryKey    = cloneFrom.isPrimaryKey();
		this.unique        = cloneFrom.isUnique();
		this.autoIncrement = cloneFrom.isAutoIncrement();
		this.defaultValue  = cloneFrom.getDefaultValue();
		this.comment	   = cloneFrom.getComment();
	}
	
	public DbColumnBuilder notNull(){
		return setNullable(false);
	}
	
	public DbColumnBuilder unique(){
		return setUnique(true);
	}
	
	public DbColumnBuilder primaryKey(){
		return setPrimaryKey(true);
	}
	
	public String getName() {
		return name;
	}

	public DbColumnBuilder setName(String name) {
		this.name = name;
		return this;
	}
	
	public DbColumnBuilder trySetName(String name){
		if(Strings.isEmpty(this.name)){
			this.name = name;
		}
		return this;
	}
	
	public Integer getTypeCode() {
		return typeCode;
	}

	public DbColumnBuilder setTypeCode(Integer typeCode) {
		this.typeCode = typeCode;
		if(null != typeCode){
			this.typeName = JdbcTypes.forTypeCode(typeCode).getName();	
		}else{
			this.typeName = null;
		}
		
		return this;
	}
	
	public DbColumnBuilder trySetTypeCode(Integer typeCode){
		if(null == this.typeCode){
			this.setTypeCode(typeCode);
		}
		return this;
	}
	
	public String getTypeName() {
		return typeName;
	}

	public DbColumnBuilder setTypeName(String typeName) {
		Args.notNull(typeName);
		this.typeName = typeName;
		this.typeCode = JdbcTypes.forTypeName(typeName).getCode();
		return this;
	}

	public DbColumnBuilder trySetTypeName(String typeName){
		if(Strings.isEmpty(this.typeName)){
			this.setTypeName(typeName);
		}
		return this;
	}

	public String getSpecialType() {
		return specialType;
	}

	public void setSpecialType(String specialType) {
		this.specialType = specialType;
	}

	public String getNativeType() {
		return nativeType;
	}

	public void setNativeType(String nativeType) {
		this.nativeType = nativeType;
	}

	public Integer getLength() {
		return length;
	}

	public DbColumnBuilder setLength(Integer length) {
		this.length = length;
		return this;
	}

	public DbColumnBuilder trySetLength(Integer length){
		if(null == this.length){
			this.length = length;
		}
		return this;
	}
	
	public Integer getPrecision() {
		return precision;
	}

	public DbColumnBuilder setPrecision(Integer precision) {
		this.precision = precision;
		return this;
	}
	
	public DbColumnBuilder trySetPrecision(Integer precision){
		if(null == this.precision){
			this.precision = precision;
		}
		return this;
	}

	public Integer getScale() {
		return scale;
	}

	public DbColumnBuilder setScale(Integer scale) {
		this.scale = scale;
		return this;
	}
	
	public DbColumnBuilder trySetScale(Integer scale){
		if(null == this.scale){
			this.scale = scale;
		}
		return this;
	}
	
	public boolean isNullable(){
		return null != nullable && nullable;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public DbColumnBuilder setNullable(Boolean nullable) {
		this.nullable = nullable;
		return this;
	}
	
	public DbColumnBuilder trySetNullable(Boolean nullable){
		if(null == this.nullable){
			this.nullable = nullable;
		}
		return this;
	}
	
	public boolean isPrimaryKey(){
		return null != primaryKey && primaryKey;
	}

	public Boolean getPrimaryKey() {
		return primaryKey;
	}

	public DbColumnBuilder setPrimaryKey(Boolean primaryKey) {
		this.primaryKey = primaryKey;
		
		if(null != primaryKey && primaryKey){
			this.nullable   = false;
		}
		
		return this;
	}
	
	public DbColumnBuilder trySetPrimaryKey(Boolean primaryKey){
		if(null == this.primaryKey){
			this.setPrimaryKey(primaryKey);
		}
		return this;
	}
	
	public boolean isUnique(){
		return null != unique && unique;
	}

	public Boolean getUnique() {
		return unique;
	}
	
	public DbColumnBuilder setUnique(Boolean unique) {
		this.unique = unique;
		return this;
	}
	
	public DbColumnBuilder trySetUnique(Boolean unique){
		if(null == this.unique){
			this.unique = unique;
		}
		return this;
	}
	
	public boolean isAutoIncrement(){
		return null != autoIncrement && autoIncrement;
	}

	public Boolean getAutoIncrement() {
		return autoIncrement;
	}

	public DbColumnBuilder setAutoIncrement(Boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
		return this;
	}
	
	public DbColumnBuilder trySetAutoIncrement(Boolean autoIncrement){
		if(null == this.autoIncrement){
			this.autoIncrement = autoIncrement;
		}
		return this;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public DbColumnBuilder setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	
	public DbColumnBuilder trySetDefaultValue(String defaultValue){
		if(Strings.isEmpty(this.defaultValue)){
			this.defaultValue = defaultValue;
		}
		return this;
	}

	public String getComment() {
		return comment;
	}

	public DbColumnBuilder setComment(String comment) {
		this.comment = comment;
		return this;
	}
	
	public DbColumnBuilder trySetComment(String comment){
		if(Strings.isEmpty(this.comment)){
			this.comment = comment;
		}
		return this;
	}

	@Override
    public DbColumn build() {
		Assert.notNull(typeCode,"type code");
		
		if(length == null){
			length = 0;
		}
		
		if(precision == null){
			precision = 0;
		}
		
		if(scale == null){
			scale = 0;
		}
		
		if(primaryKey == null){
			primaryKey = false;
		}
		
		if(nullable == null){
			nullable = true;
		}
		
		if(unique == null){
			unique = false;
		}
		
		if(autoIncrement == null){
			autoIncrement = false;
		}
		
	    return new DbColumn(name, typeCode,typeName, specialType, nativeType,
				length, precision, scale, nullable, primaryKey, unique, autoIncrement, defaultValue, comment);
    }

	@Override
    public void parseJson(JsonValue value) {
		JsonObject o = value.asJsonObject();
		this.name          = o.getString("name");
		this.typeCode      = o.getInteger("typeCode");
		this.typeName      = o.getString("typeName");
		this.specialType   = o.getString("specialType");
		this.nativeType    = o.getString("nativeType");
		this.length        = o.getInteger("length");
		this.precision     = o.getInteger("precision");
		this.scale         = o.getInteger("scale");
		this.nullable      = o.getBoolean("nullable");
		this.primaryKey    = o.getBoolean("primaryKey");
		this.unique	       = o.getBoolean("unique");
		this.autoIncrement = o.getBoolean("autoIncrement");
		this.comment       = o.getString("comment");
		this.defaultValue  = o.getString("defaultValue");
    }
}

/*
 * Copyright 2015 the original author or authors.
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
package leap.orm.domain;

import leap.core.el.EL;
import leap.lang.Strings;
import leap.lang.expression.Expression;
import leap.lang.jdbc.JdbcType;
import leap.lang.jdbc.JdbcTypes;
import leap.orm.annotation.ADomain;

public class DefaultDomainCreator implements DomainCreator {
    
    @Override
    public FieldDomainBuilder tryCreateFieldDomainByAnnotation(DomainConfigContext context, Class<?> at) {
        ADomain fd = at.getAnnotation(ADomain.class);
        if(null != fd) {
            return createFieldDomainByAnnotation(context, at, fd);
        }
        return null;
    }
    
    @Override
    public FieldDomainBuilder createFieldDomainByAnnotation(DomainConfigContext context, Class<?> at, ADomain fd) {
        String  entityName        = null;
        String  name              = Strings.firstNotEmpty(fd.name(),fd.annotationType().getSimpleName());
        String  defaultColumnName = fd.column();
        String  typeName          = fd.type().getTypeName();
        Boolean nullable          = fd.nullable().getValue();
        Integer length            = fd.length() <= 0 ? null : fd.length();
        Integer precision         = fd.length() <= 0 ? null : fd.precision();
        Integer scale             = fd.scale() < 0 ? null : fd.scale();
        String  defaultValue      = fd.defaultValue();
        Boolean insert            = fd.insert().getValue();
        Boolean update            = fd.update().getValue();
        String  insertValue       = fd.insertValue();
        String  updateValue       = fd.updateValue();
        Integer sortOrder         = fd.order() == Integer.MIN_VALUE ? null : fd.order();
        boolean override          = fd.override();
        
        EntityDomain entityDomain = null;
        
        if(!Strings.isEmpty(entityName)){
            entityDomain = context.tryGetEntityDomain(entityName);
            if(null == entityDomain){
                throw new DomainConfigException("Entity domain '" + entityName + "' not found, check the annotation : " + at.getName());
            }
        }
        
        //check name
        if(Strings.isEmpty(name)){
            throw new DomainConfigException("The 'name' attribute must be defined in domain, check the annotation : " + at.getName());
        }
        
        JdbcType type = null;
        if(!Strings.isEmpty(typeName)) {
            type = JdbcTypes.tryForTypeName(typeName);
            if(null == type){
                throw new DomainConfigException("Jdbc type '" + typeName + "' not supported, check the annotation : " + at.getName());
            }
        }
        
        //check is domain exists
        if(!override){
            String qname = context.qualifyName(null == entityDomain ? null : entityDomain.getName(), name);
            FieldDomain fieldDomain = context.tryGetFieldDomain(qname);
            if(null != fieldDomain){
                throw new DomainConfigException(Strings.format(
                        "Found duplicated field domain '" + name + "' in : {0},{1}",fieldDomain.getSource(), at.getName()));
            }
        }
        
        Expression insertValueExpression = null;
        Expression updateValueExpression = null;
        
        if(!Strings.isEmpty(insertValue)){
            insertValueExpression = EL.tryCreateValueExpression(insertValue);   
        }
        
        if(!Strings.isEmpty(updateValue)){
            updateValueExpression = EL.tryCreateValueExpression(updateValue);
        }
        
        FieldDomainBuilder domain = new FieldDomainBuilder(at)
                                        .setEntityDomain(entityDomain)
                                        .setName(name)
                                        .setDefaultColumnName(defaultColumnName)
                                        .setType(type)
                                        .setNullable(nullable)
                                        .setLength(length)
                                        .setPrecision(precision)
                                        .setScale(scale)
                                        .setDefaultValue(defaultValue)
                                        .setInsert(insert)
                                        .setUpdate(update)
                                        .setInsertValue(insertValueExpression)
                                        .setUpdateValue(updateValueExpression)
                                        .setSortOrder(sortOrder);
        
        if(Strings.isEmpty(fd.name())) {
            domain.setUnnamed(true);
        }
        
        return domain;
    }


}

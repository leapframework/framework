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
import leap.lang.Ordered;
import leap.lang.Strings;
import leap.lang.expression.Expression;
import leap.lang.jdbc.JdbcType;
import leap.lang.jdbc.JdbcTypes;
import leap.orm.annotation.ADomain;

public class DefaultDomainCreator implements DomainCreator {
    
    @Override
    public DomainBuilder tryCreateFieldDomainByAnnotation(Domains context, Class<?> at) {
        ADomain fd = at.getAnnotation(ADomain.class);
        if(null != fd) {
            return createFieldDomainByAnnotation(context, at, fd);
        }
        return null;
    }
    
    @Override
    public DomainBuilder createFieldDomainByAnnotation(Domains context, Class<?> at, ADomain ad) {
        String  name              = Strings.firstNotEmpty(ad.name(), ad.annotationType().getSimpleName());
        String  defaultColumnName = ad.column();
        String  typeName          = ad.type().getTypeName();
        Boolean nullable          = ad.nullable().getValue();
        Integer length            = ad.length() <= 0 ? null : ad.length();
        Integer precision         = ad.length() <= 0 ? null : ad.precision();
        Integer scale             = ad.scale() < 0 ? null : ad.scale();
        String  defaultValue      = ad.defaultValue();
        Boolean insert            = ad.insert().getValue();
        Boolean update            = ad.update().getValue();
        String  insertValue       = ad.insertValue();
        String  updateValue       = ad.updateValue();
        Float sortOrder           = ad.order() == Ordered.MINIMUM_SORT_ORDER ? null : ad.order();
        boolean override          = ad.override();
        
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
            Domain domain = context.tryGetDomain(name);
            if(null != domain){
                throw new DomainConfigException(Strings.format(
                        "Found duplicated field domain '" + name + "' in : {0},{1}", domain.getSource(), at.getName()));
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
        
        DomainBuilder domain = new DomainBuilder(at)
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
        
        if(Strings.isEmpty(ad.name())) {
            domain.setUnnamed(true);
        }
        
        return domain;
    }


}

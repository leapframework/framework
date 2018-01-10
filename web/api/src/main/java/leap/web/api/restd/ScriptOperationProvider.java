/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.restd;

import leap.lang.meta.MType;
import leap.web.action.Argument;
import leap.web.action.ArgumentBuilder;
import leap.web.action.FuncActionBuilder;
import leap.web.api.meta.model.MApiOperationBuilder;
import leap.web.api.meta.model.MApiParameter;
import leap.web.api.meta.model.MApiParameterBuilder;
import leap.web.api.restd.crud.CrudOperation;
import leap.web.route.RouteBuilder;

import java.lang.reflect.Array;
import java.util.Map;

public abstract class ScriptOperationProvider extends CrudOperation {

    protected void createArguments(RouteBuilder route, FuncActionBuilder action, MApiOperationBuilder mo) {
        for(MApiParameterBuilder p : mo.getParameters()) {
            ArgumentBuilder arg = new ArgumentBuilder();
            arg.setName(p.getName());
            arg.setRequired(p.getRequired());

            //validators
            if(null != p.getValidators() && !p.getValidators().isEmpty())  {
                p.getValidators().forEach(arg::addValidator);
            }

            //type
            resolveArgumentType(route, p, arg);

            //location.
            resolveArgumentLocation(route, p, arg);

            action.addArgument(arg);
        }
    }

    protected void resolveArgumentType(RouteBuilder route, MApiParameterBuilder p, ArgumentBuilder arg) {
        MType type = p.getType();
        if (type.isComplexType() || type.isTypeRef()) {
            arg.setType(Map.class);
            return;
        }

        if(type.isSimpleType()) {
            arg.setType(type.asSimpleType().getJavaType());
            return;
        }

        if(type.isObjectType()) {
            arg.setType(Object.class);
            return;
        }

        if(type.isDictionaryType()) {
            arg.setType(Map.class);
            return;
        }

        if(type.isCollectionType()) {
            MType elementType = type.asCollectionType().getElementType();
            if(elementType.isSimpleType()) {
                arg.setType(Array.newInstance(elementType.asSimpleType().getJavaType(),0).getClass());
            }else if(elementType.isObjectType()){
                arg.setType(Object[].class);
            }else {
                arg.setType(Map[].class);
            }
            return;
        }

        throw new IllegalStateException("Unsupported type '" + type + "' at parameter '" + p.getName() + "'");
    }

    protected void resolveArgumentLocation(RouteBuilder route, MApiParameterBuilder p, ArgumentBuilder arg) {
        setDefaultLocation(route, p);

        if (p.getLocation() == MApiParameter.Location.BODY) {
            arg.setLocation(Argument.Location.REQUEST_BODY);
            return;
        }

        if (p.getLocation() == MApiParameter.Location.QUERY) {
            arg.setLocation(Argument.Location.QUERY_PARAM);
            return;
        }

        if (p.getLocation() == MApiParameter.Location.FORM) {
            arg.setLocation(Argument.Location.REQUEST_PARAM);
            return;
        }

        if (p.getLocation() == MApiParameter.Location.PATH) {
            arg.setLocation(Argument.Location.PATH_PARAM);
            return;
        }

        if (p.getLocation() == MApiParameter.Location.HEADER) {
            arg.setLocation(Argument.Location.HEADER_PARAM);
            return;
        }

        throw new IllegalStateException("Location '" + p.getLocation() + "' not implemented!");
    }

    protected boolean setDefaultLocation(RouteBuilder route, MApiParameterBuilder p) {
        if(null == p.getLocation()) {
            if (route.getPathTemplate().getTemplateVariables().contains(p.getName())) {
                p.setLocation(MApiParameter.Location.PATH);
                return true;
            }

            if(p.getType().isComplexType() || p.getType().isTypeRef() || p.getType().isDictionaryType()) {
                p.setLocation(MApiParameter.Location.BODY);
                return true;
            }

            p.setLocation(MApiParameter.Location.QUERY);
            return true;
        }
        return false;
    }

}

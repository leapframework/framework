/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.web.api.spec.swagger;

import leap.lang.Arrays2;
import leap.lang.Collections2;
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.lang.io.IO;
import leap.lang.json.JSON;
import leap.lang.json.JsonObject;
import leap.lang.json.JsonValue;
import leap.lang.meta.*;
import leap.lang.yaml.YAML;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.model.*;
import leap.web.api.spec.ApiSpecReader;
import leap.web.api.spec.InvalidSpecException;
import leap.web.api.spec.UnsupportedSpecException;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static leap.web.api.spec.swagger.SwaggerConstants.*;

public class SwaggerSpecReader implements ApiSpecReader {

    private final List<String> HTTP_METHODS_LOWER_CASE = Arrays.asList("get", "put", "post", "delete", "options", "head", "patch");

    protected boolean validate = true;

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    @Override
    public ApiMetadataBuilder read(Reader reader) throws IOException {
        String content = IO.readString(reader).trim();

        Map<String,Object> swagger;

        if(content.startsWith("{")) {
            swagger = JSON.decode(content);
        }else{
            swagger = YAML.decode(content);
        }

        ApiMetadataBuilder m = new ApiMetadataBuilder();

        readSwagger(swagger, m);

        return m;
    }

    public void readSwagger(Map<String,Object> map, ApiMetadataBuilder m) {
        JsonObject swagger = JsonObject.of(map);

        String swaggerVersion = swagger.getString(SWAGGER);
        if(!"2.0".equals(swaggerVersion)) {
            throw new UnsupportedSpecException("Unsupported swagger version : " + swaggerVersion);
        }

        readBase(map, m);
        readPaths(swagger.getMap(PATHS), m);
        readSecurityDefinitions(swagger.getMap(SECURITY_DEFINITIONS), m);
        readDefinitions(swagger.getMap(DEFINITIONS), m);
        readResponses(swagger.getMap(RESPONSES), m);
        readTags(swagger.getList(TAGS), m);

        //todo : security & securityDefinitions.
    }

    public void readBase(Map<String,Object> map, ApiMetadataBuilder m) {
        JsonObject swagger = JsonObject.of(map);

        m.setHost(swagger.getString(HOST));
        m.setBasePath(swagger.getString(BASE_PATH));

        List<String> schemes = swagger.getList(SCHEMES);
        if(null != schemes) {
            schemes.forEach( s -> m.addProtocol(s));
        }

        List<String> produces = swagger.getList(PRODUCES);
        if(null != produces) {
            produces.forEach(m::addProduce);
        }

        List<String> consumes = swagger.getList(CONSUMES);
        if(null != consumes) {
            consumes.forEach(m::addConsume);
        }

        JsonObject info = swagger.getObject(INFO);
        if(null != info) {
            m.setVersion(info.getString(VERSION));
            m.setTitle(info.getString(TITLE));
            m.setName(m.getTitle());
            m.setSummary(info.getString(SUMMARY));
            m.setDescription(info.getString(DESCRIPTION));
            m.setTermsOfService(info.getString(TERMS_OF_SERVICE));

            JsonObject contact = info.getObject(CONTACT);
            if(null != contact) {

                MApiContactBuilder c = new MApiContactBuilder();
                c.setName(contact.getString(NAME));
                c.setEmail(contact.getString(EMAIL));
                c.setUrl(contact.getString(URL));

                m.setContact(c);
            }
        }

        //todo : external docs
    }

    public void readPaths(Map<String,Object> paths, ApiMetadataBuilder m) {
        if(null == paths) {
            return ;
        }

        paths.forEach((pathTemplate, path) -> {
            m.addPath(readPath(pathTemplate, (Map<String,Object>)path));
        });

    }

    public MApiPathBuilder readPath(String pathTemplate, Map<String,Object> map) {
        return readPath(null, pathTemplate, map);
    }

    public MApiOperationBuilder readOperation(String method, Map<String,Object> map) {
        return readOperation(null, null, method, map);
    }

    protected MApiPathBuilder readPath(ApiMetadataBuilder m, String pathTemplate, Map<String,Object> map) {
        MApiPathBuilder mp = new MApiPathBuilder();

        mp.setPathTemplate(pathTemplate);

        map.forEach((method, operation) -> {
            if(HTTP_METHODS_LOWER_CASE.contains(method) && null != operation) {
                mp.addOperation(readOperation(method, (Map<String,Object>)operation));
            }
        });

        return mp;
    }

    protected MApiOperationBuilder readOperation(ApiMetadataBuilder m, MApiPathBuilder p, String method, Map<String,Object> map) {
        MApiOperationBuilder mo = new MApiOperationBuilder();

        JsonObject o = JsonObject.of(map);

        mo.setMethod(HTTP.Method.valueOf(method.toUpperCase()));

        mo.setCorsEnabled(o.get(X_CORS, Boolean.class));

        //tags
        List<String> tags = o.getList(TAGS);
        if(null != tags){
            tags.forEach(mo::addTag);
        }

        mo.setSummary(o.getString(SUMMARY));
        mo.setDescription(o.getString(DESCRIPTION));
        mo.setDeprecated(o.get(DEPRECATED, Boolean.class, false));

        //todo : protocols.

        List<String> produces = o.getList(PRODUCES);
        if(null != produces) {
            produces.forEach(mo::addProduce);
        }

        List<String> consumes = o.getList(CONSUMES);
        if(null != consumes) {
            consumes.forEach(mo::addConsume);
        }

        //parameters
        List<MApiParameterBuilder> params = readParameters(o.getList(PARAMETERS));
        params.forEach(mo::addParameter);

        //responses
        List<MApiResponseBuilder> responses = readResponses(o.getMap(RESPONSES));
        responses.forEach(mo::addResponse);

        //security
        List<Map<String,Object>> security = o.getList(SECURITY);
        if(null != security) {
            security.forEach(sc->{
                for(Map.Entry<String, Object> entry:sc.entrySet()){
                    MApiSecurityReq sec = new MApiSecurityReq(entry.getKey());
                    sec.addScopes(Collections2.toStringArray((Collection<String>)entry.getValue()));
                    mo.getSecurity().put(sec.getName(),sec);
                    break;
                }
            });
        }else{
            mo.setAllowAnonymous(true);
        }

        String id = o.getString(OPERATION_ID);
        if(!Strings.isEmpty(id)) {
            mo.setId(id);
            mo.setName(id);
        }else {
            mo.setName(mo.getMethod().name().toLowerCase());
            //todo : create operation id ?
        }

        return mo;
    }

    public List<MApiParameterBuilder> readParameters(List<Map<String,Object>> list) {
        List<MApiParameterBuilder> params = new ArrayList<>();

        if(null != list) {
            list.forEach( p -> {
                params.add(readParameter(p));
            });
        }

        return params;
    }

    public MApiParameterBuilder readParameter(Map<String,Object> map) {
        MApiParameterBuilder mp = new MApiParameterBuilder();

        JsonObject p = JsonObject.of(map);

        readParameterBase(null, p, mp);
        mp.setLocation(readParameterIn(mp.getName(), p.getString(IN)));

        return mp;
    }

    public List<MApiResponseBuilder> readResponses(Map<String,Object> map) {
        List<MApiResponseBuilder> responses = new ArrayList<>();

        if(null != map) {

            map.forEach((name, resp) ->  responses.add(readResponse(name, (Map<String,Object>)resp)));

        }

        return responses;
    }

    public MApiResponseBuilder readResponse(String name, Map<String,Object> map) {
        MApiResponseBuilder mr = new MApiResponseBuilder();

        JsonObject resp = JsonObject.of(map);

        mr.setName(name);
        mr.setSummary(resp.getString(SUMMARY));
        mr.setDescription(resp.getString(DESCRIPTION));

        try{
            mr.setStatus(Integer.parseInt(name));
        }catch(NumberFormatException e) {
            //not a http status.
        }

        JsonObject schema = resp.getObject(SCHEMA);
        if(null != schema) {
            mr.setType(readType(schema));
        }

        return mr;
    }

    public void readSecurityDefinitions(Map<String,Object> definitions, ApiMetadataBuilder m) {
        if(null == definitions) {
            return ;
        }

        definitions.forEach((name, def) -> m.addSecurityDef(readSecurityDef(name,(Map<String,Object>)def)));
    }

    public void readDefinitions(Map<String,Object> definitions, ApiMetadataBuilder m) {
        if(null == definitions) {
            return ;
        }

        definitions.forEach((name, model) -> {
            m.addModel(readModel(name, (Map<String,Object>)model));
        });
    }

    public void readResponses(Map<String, Object> responses, ApiMetadataBuilder m) {
        if(null == responses) {
            return;
        }

        responses.forEach((name, resp) -> {
            m.putResponse(name, readResponse(name, (Map<String,Object>)resp));
        });

    }

    public void readTags(List<Map<String,Object>> tags, ApiMetadataBuilder m) {
        if(null == tags) {
            return;
        }

        for(Map<String,Object> map : tags) {

            JsonObject tag = JsonObject.of(map);

            String name = tag.getString(NAME);
            String desc = tag.getString(DESCRIPTION);

            m.addTag(new MApiTag(name, name, null ,desc, null));
        }
    }

    public MApiSecurityDef readSecurityDef(String name, Map<String, Object> map){
        if(map == null){
            return null;
        }
        Object type = map.get(TYPE);
        if(Objects.equals(type,OAUTH2)){
            String authzUrl = Objects.toString(map.get(AUTHZ_URL));
            String tokenUrl = Objects.toString(map.get(TOKEN_URL));
            String flow     = Objects.toString(map.get(FLOW));
            MOAuth2ApiSecurityDef def = new MOAuth2ApiSecurityDef(name, name, authzUrl,tokenUrl,flow,map);
            return def;
        }
        return null;
    }

    public MApiModelBuilder readModel(String name, Map<String,Object> map) {
        return readModel(name, map, SwaggerExtension.NOP);
    }

    public MApiModelBuilder readModel(String name, Map<String,Object> map, SwaggerExtension ex) {
        MApiModelBuilder mm = new MApiModelBuilder();

        JsonObject model = JsonObject.of(map);

        List<String> requiredProperties = model.getList(REQUIRED);

        mm.setName(name);
        mm.setTitle(model.getString(TITLE));
        mm.setSummary(model.getString(SUMMARY));
        mm.setDescription(model.getString(DESCRIPTION));
        mm.setEntity(model.getBoolean(X_ENTITY, false));

        Map<String,Object> properties = model.getMap(PROPERTIES);
        if(null != properties) {
            List<MApiPropertyBuilder> list = readProperties(properties, requiredProperties, ex);
            list.forEach(mm::addProperty);
        }

        return mm;
    }

    public List<MApiPropertyBuilder> readProperties(Map<String,Object> properties) {
        return readProperties(properties, null);
    }

    public List<MApiPropertyBuilder> readProperties(Map<String,Object> properties, List<String> requiredProperties) {
        return readProperties(properties, requiredProperties, SwaggerExtension.NOP);
    }

    public List<MApiPropertyBuilder> readProperties(Map<String,Object> properties, List<String> requiredProperties, SwaggerExtension ex) {
        List<MApiPropertyBuilder> list = new ArrayList<>();
        properties.forEach((propName, propMap) -> {
            MApiPropertyBuilder p = readProperty(propName, (Map<String,Object>)propMap, ex);

            if(null != requiredProperties && requiredProperties.contains(p.getName())) {
                p.setRequired(true);
            }

            list.add(p);

        });
        return list;
    }

    protected void readParameterBase(String name, JsonObject p, MApiParameterBaseBuilder mp) {
        readParameterBase(name, p, mp, SwaggerExtension.NOP);
    }

    protected void readParameterBase(String name, JsonObject p, MApiParameterBaseBuilder mp, SwaggerExtension ex) {
        mp.setName(p.getString(NAME));
        mp.setTitle(p.getString(TITLE));
        mp.setSummary(p.getString(SUMMARY));
        mp.setDescription(p.getString(DESCRIPTION));
        mp.setRequired(p.get(REQUIRED, Boolean.class));
        mp.setType(readParameterType(name, p, ex));
        mp.setDefaultValue(p.get(DEFAULT));

        MApiValidationBuilder v = new MApiValidationBuilder();
        v.setPattern(p.getString(PATTERN));
        v.setMaxLength(p.getInteger(MAX_LENGTH));
        v.setMinLength(p.getInteger(MIN_LENGTH));
        v.setMaximum(p.get(MAXIMUM));
        v.setExclusiveMaximum(p.getBoolean(EXCLUSIVE_MAXIMUM, v.isExclusiveMaximum()));
        v.setMinimum(p.get(MINIMUM));
        v.setExclusiveMinimum(p.getBoolean(EXCLUSIVE_MINIMUM, v.isExclusiveMinimum()));

        List<String> enumValues = p.getList(ENUM);
        if(null != enumValues) {
            mp.setEnumValues(enumValues.toArray(Arrays2.EMPTY_STRING_ARRAY));
        }

        mp.setValidation(v);

        readFormat(p, mp);
    }

    public MApiPropertyBuilder readProperty(String name, Map<String,Object> map) {
        return readProperty(name, map, SwaggerExtension.NOP);
    }

    public MApiPropertyBuilder readProperty(String name, Map<String,Object> map, SwaggerExtension ex) {
        MApiPropertyBuilder mp = new MApiPropertyBuilder();

        JsonObject p = JsonObject.of(map);

        readParameterBase(name, p, mp, ex);
        mp.setName(name);

        //yaml read all values to string.
        mp.setIdentity(p.getBoolean(X_IDENTITY, false));
        mp.setUnique(p.getBoolean(X_UNIQUE, false));
        mp.setSortable(p.get(X_SORTABLE, Boolean.class));
        mp.setFilterable(p.get(X_FILTERABLE, Boolean.class));
        mp.setCreatable(p.get(X_CREATABLE, Boolean.class));
        mp.setUpdatable(p.get(X_UPDATABLE, Boolean.class));
        mp.setExpandable(p.get(X_EXPANDABLE, Boolean.class));

        return mp;
    }

    protected void readFormat(JsonObject json, MApiParameterBaseBuilder p) {
        String format = json.getString(FORMAT);
        if(!Strings.isEmpty(format)) {
            p.setFormat(format);

            if("password".equals(format)) {
                p.setPassword(true);
            }
        }

        String type = json.getString(TYPE);
        if("file".equals(type)) {
            p.setFile(true);
        }
    }

    protected MApiParameter.Location readParameterIn(String param, String in) {
        if(Strings.isEmpty(in)){
            throw new InvalidSpecException("invalid specification of parameter "+param + ": property named in can not be empty!");
        }
        switch (in) {
            case "body" :
                return MApiParameter.Location.BODY;
            case "path" :
                return MApiParameter.Location.PATH;
            case "query" :
                return MApiParameter.Location.QUERY;
            case "formData" :
                return MApiParameter.Location.FORM;
            case "header" :
                return MApiParameter.Location.HEADER;
            default :
                throw new UnsupportedSpecException("Unsupported parameter in '" + in + "' of '" + param + "'");
        }
    }

    protected MType readParameterType(String name, JsonObject p) {
        return readParameterType(name, p, SwaggerExtension.NOP);
    }

    protected MType readParameterType(String name, JsonObject p, SwaggerExtension ex) {
        JsonObject schema = p.getObject(SCHEMA);
        if(null != schema) {
            return readType(schema, ex);
        }else{
            return readType(name, p, ex);
        }
    }

    protected MComplexTypeRef readRefType(String ref) {
        ref = Strings.removeStart(ref, "#/definitions/");
        return new MComplexTypeRef(ref);
    }

    protected MCollectionType readCollectionType(JsonObject items, SwaggerExtension ex) {
        return new MCollectionType(readType(items, ex));
    }

    protected MType readType(JsonObject property) {
        return readType(property, SwaggerExtension.NOP);
    }

    protected MType readType(JsonObject property, SwaggerExtension ex) {
        return readType(null, property, ex);
    }

    protected MType readType(String name, JsonObject property, SwaggerExtension ex) {
        String ref = property.getString(REF);
        if(!Strings.isEmpty(ref)) {
            return readRefType(ref);
        }

        String type = property.getString(TYPE);
        if(Strings.isEmpty(type)) {
            if(!validate) {
                return null;
            }
            throw new InvalidSpecException("Invalid type in property : " + JSON.stringify(property.raw()));
        }

        MType mtype = null != ex ? ex.readType(type) : null;
        if(null != mtype) {
            return mtype;
        }

        if(type.equals(OBJECT)) {
            return readObjectType(name, property);
        }

        if(type.equals(ARRAY)) {
            return readCollectionType(property.getObject(ITEMS), ex);
        }

        String format = property.getString(FORMAT);
        return readSimpleType(type, format);
    }

    protected MType readObjectType(String name, JsonObject o) {
        JsonObject additionalProperties = o.getObject(ADDITIONAL_PROPERTIES);

        if(null == additionalProperties) {
            //check is nested model
            Object property = o.get(PROPERTIES);
            if(null != property && property instanceof Map) {
                if(null == name) {
                    name = "Embedded";
                }
                MApiModelBuilder model = readModel(name, o.asMap());
                return model.toMComplexType().build();
            }else {
                return MObjectType.TYPE;
            }
        }else{
            if(additionalProperties.asMap().isEmpty()) {
                return MObjectType.TYPE;
            }else {
                MType valueType = readType(additionalProperties);
                return new MDictionaryType(MSimpleTypes.STRING, valueType);
            }
        }
    }

//    protected MComplexType readComplexType(String name, JsonObject model) {
//        MComplexTypeBuilder ct = new MComplexTypeBuilder();
//        ct.setName(name);
//
//        Map<String,Object> properties = model.getMap(PROPERTIES);
//        if(null != properties) {
//            properties.forEach((propName, propMap) -> {
//                ct.addProperty(readComplexTypeProperty(propName, (Map<String,Object>)propMap));
//            });
//        }
//
//        return ct.build();
//    }

    protected MProperty readComplexTypeProperty(String name, Map<String,Object> map) {
        MPropertyBuilder mp = new MPropertyBuilder();

        JsonObject p = JsonObject.of(map);

        mp.setName(name);
        mp.setRequired(p.get(REQUIRED, Boolean.class));
        mp.setType(readType(p));

        return mp.build();
    }

    public MSimpleType readSimpleType(String type) {
        return readSimpleType(type, "");
    }

    public MSimpleType readSimpleType(String type, String format) {
        switch (type) {

            case "integer" :

                if("int32".equals(format) || Strings.isEmpty(format)) {
                    return MSimpleTypes.INTEGER;
                }

                if("int64".equals(format)) {
                    return MSimpleTypes.BIGINT;
                }

                throw new InvalidSpecException("Invalid format '" + format + "' of type '" + type + "'");

            case "long":
                return MSimpleTypes.BIGINT;

            case "double":
                return MSimpleTypes.DOUBLE;

            case "float" :
                return MSimpleTypes.SINGLE;

            case "byte" :
                return MSimpleTypes.BYTE;

            case "binary":
                return MSimpleTypes.BINARY;

            case "date" :
                return MSimpleTypes.DATE;

            case "dateTime" :
                return MSimpleTypes.DATETIME;

            case "number" :

                if("float".equals(format) || Strings.isEmpty(format)) {
                    return MSimpleTypes.SINGLE;
                }

                if("double".equals(format)) {
                    return MSimpleTypes.DOUBLE;
                }

                throw new InvalidSpecException("Invalid format '" + format + "' of type '" + type + "'");

            case "boolean" :
                return MSimpleTypes.BOOLEAN;

            case "string" :

                if("byte".equals(format)) {
                    return MSimpleTypes.BYTE;
                }

                if("binary".equals(format)) {
                    return MSimpleTypes.BINARY;
                }

                if("date-time".equals(format)) {
                    return MSimpleTypes.DATETIME;
                }

                if("date".equals(format)) {
                    //todo : "date" format
                    return MSimpleTypes.DATETIME;
                }

                if("password".equals(format)) {
                    return MSimpleTypes.STRING;
                }

                if(!Strings.isEmpty(format)) {
                    /*
                        the format property is an open string-valued property,
                        and can have any value to support documentation needs.
                        Formats such as "email", "uuid", etc.
                     */
                }

                return MSimpleTypes.STRING;

            case "file" :
                return MSimpleTypes.STRING;

            default :
                throw new InvalidSpecException("Invalid type '" + type + "'");
        }
    }
}

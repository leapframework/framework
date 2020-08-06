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
package leap.web.api.spec.swagger;

import leap.core.doc.annotation.Doc;
import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.json.JsonWriter;
import leap.lang.meta.*;
import leap.web.Request;
import leap.web.api.config.ApiConfigException;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.*;
import leap.web.api.meta.model.MApiParameter.Location;
import leap.web.api.spec.ApiSpecContext;
import leap.web.api.spec.JsonSpecWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static leap.lang.Strings.nullToEmpty;
import static leap.web.api.spec.swagger.SwaggerConstants.*;

public class SwaggerJsonWriter extends JsonSpecWriter {

    protected static final String OAUTH2             = "oauth2";
    protected static final String OAUTH2_ACCESS_CODE = "oauth2_access_code";
    
    protected static final class WriteContext {
        String defaultSecurity;
    }

    protected boolean isIncluded(ApiSpecContext sc, String part) {
        if(null == sc.getParts() || sc.getParts().isEmpty()) {
            return true;
        }
        return sc.getParts().contains(part);
    }

	@Override
	protected void write(ApiSpecContext sc, ApiMetadata m, JsonWriter w) {
		Args.notNull(m, "metadata");
		Args.notNull(w, "writer");
		
		WriteContext context = new WriteContext();
		
		w.startObject();
		w.property(SWAGGER, "2.0");

            w.property(INFO, () -> {
                w.startObject()
                    .property(TITLE, m.getTitle());

                if(isIncluded(sc, INFO)) {
                    w.propertyOptional(SUMMARY, m.getSummary())
                            .property(DESCRIPTION, nullToEmpty(m.getDescription()))
                            .propertyOptional(TERMS_OF_SERVICE, m.getTermsOfService())
                            .propertyOptional(CONTACT, m.getConcat());
                }

                w.propertyOptional(VERSION, m.getVersion());
                w.endObject();
            });

        w.propertyOptional(HOST, getHost(sc, m))
        .propertyOptional(BASE_PATH, sc.getContextPath() + Strings.nullToEmpty(m.getBasePath()))
        .propertyOptional(SCHEMES, m.getProtocols())
        .propertyOptional(CONSUMES, m.getConsumes())
        .propertyOptional(PRODUCES, m.getProduces());

        if(isIncluded(sc, PATHS)) {
            w.property(PATHS, () -> writePaths(context, m, w));
        }

        if(isIncluded(sc, DEFINITIONS)) {
            w.property(DEFINITIONS, () -> writeDefinitions(context, m, w));
        }

        if(!m.getResponses().isEmpty()) {
            if(isIncluded(sc, RESPONSES)) {
                MApiResponse[] responses = m.getResponses().values().toArray(new MApiResponse[0]);
                w.property(RESPONSES, () -> writeResponses(context, m, w, responses));
            }
        }

        if(!Arrays2.isEmpty(m.getSecurityDefs())) {
            if(isIncluded(sc, SECURITY_DEFINITIONS)) {
                w.property(SECURITY_DEFINITIONS, () -> writeSecurityDefs(context, m, w));
                writeDefaultSecurity(context, m, w);
            }
        }

        if(!Arrays2.isEmpty(m.getTags())) {
            if(isIncluded(sc, TAGS)) {
                w.property(TAGS, () -> writeTags(context, m, w));
            }
        }

        writeExtension(w, m.getExtension());

		w.endObject();
	}

    @Override
    protected void writeModels(ApiSpecContext sc, ApiMetadata m, JsonWriter w) {
        WriteContext context = new WriteContext();
        writeDefinitions(context, m, w);
    }

    protected String getHost(ApiSpecContext sc, ApiMetadata m) {
        String host = m.getHost();
        if(!Strings.isEmpty(host)) {
            return host;
        }else if(!Strings.isEmpty(sc.getHost())) {
            int port = sc.getPort();
            if(port > 0) {
                return sc.getHost() + ":" + port;
            }else{
                return sc.getHost();
            }
        }else{
            return null;
        }
    }

	protected void writeDefaultSecurity(WriteContext context, ApiMetadata m, JsonWriter w) {
	    if(null == context.defaultSecurity){
	        return;
	    }

	    w.property(SECURITY, () -> {
	       w.startArray();
	       
	       w.startObject();
	       w.property(context.defaultSecurity, () -> {
               w.array(m.getPermissions(), (p) -> w.value(p.getValue()));
	       });
	       w.endObject();
	       
	       w.endArray();
	    });
	}
	
	protected void writePaths(WriteContext context, ApiMetadata m, JsonWriter w) {
		w.startObject();
	
		for(Entry<String, MApiPath> entry : m.getPaths().entrySet()) {
			w.property(entry.getKey(), () -> writePath(context, m, w, entry.getValue()) );
		}
		
		w.endObject();
	}

	protected void writePath(WriteContext context, ApiMetadata m, JsonWriter w, MApiPath p) {
		w.startObject();

		for(MApiOperation o : p.getOperations()) {

            if (checkProfile(o)) continue;

            w.property(o.getMethod().name().toLowerCase(), () -> {
				writeOperation(context, m, w, p, o);
			});
		}
		
		w.endObject();
	}

    private boolean checkProfile(MApiOperation o) {
        if(null == o.getRoute()) {
            return false;
        }

        String[] profiles = tryGetProfiles(o);
        if(null != profiles) {
            Request request = Request.tryGetCurrent();

            if (null != request) {
                String requestProfile = request.getParameter("profile");

                if (Strings.isNotBlank(requestProfile)) {

                    if(!Arrays2.containsAny(profiles, requestProfile)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String[] tryGetProfiles(MApiOperation o) {
        if(null == o || null == o.getRoute()
                || null == o.getRoute().getAction()
                || null == o.getRoute().getAction().getMethod()
                || null == o.getRoute().getAction().getMethod().getAnnotation(Doc.class)) {
            return null;
        }

        String[] profiles = o.getRoute().getAction().getMethod().getAnnotation(Doc.class).profile();

        return Arrays2.isNotEmpty(profiles) ? profiles : null;
    }

    private void writeExtension(JsonWriter w, MApiExtension extension) {
        if(null != extension) {
            extension.getAttributes().forEach((name, value) -> {
                if(null != value) {
                    w.property("x-" + name, value);
                }
            });
        }
    }

    private void writeModelExtension(WriteContext context, ApiMetadata m, JsonWriter w, MApiExtension extension) {
        if(null != extension) {
            extension.getAttributes().forEach((name, value) -> {
                if(null != value) {
                    if (name.equals("relations")) {
                        Map relations = Converts.convert(value, Map.class);
                        if (!relations.isEmpty()) {
                            w.property("x-relations", () -> {
                                w.startObject();
                                MApiProperty p;
                                for (Object relation : relations.values()) {
                                    p = Converts.convert(relation, MApiPropertyBuilder.class).build();
                                    writeModelProperty(context, m, w, p);
                                }
                                w.endObject();
                            });
                        }
                    } else {
                        w.property("x-" + name, value);
                    }
                }
            });
        }
    }

    protected void writeOperation(WriteContext context, ApiMetadata m, JsonWriter w, MApiPath p, MApiOperation o) {
		w.startObject();

        if(null != o.getCorsEnabled()) {
            w.property(X_CORS, o.getCorsEnabled());
        }

        writeExtension(w, o.getExtension());

        w.propertyOptional(TAGS, o.getTags());
        w.propertyOptional(SUMMARY, o.getSummary());
		w.property(DESCRIPTION, nullToEmpty(o.getDescription()));

        w.propertyOptional(OPERATION_ID, o.getId());

        if(!o.isAllowAnonymous()) {
            if(o.getSecurity().length > 0 && m.getSecurityDefs().length > 0) {
                w.property(SECURITY, () -> {
                    w.array(o.getSecurity(), (sc) -> writeSecurity(context, m, w, sc));
                });

                if(o.isAllowClientOnly()) {
                    w.property(X_SECURITY,writer -> {
                        writer.startObject();
                        writer.property(ALLOW_CLIENT_ONLY, true);
                        writer.endObject();
                    });
                }else {
                    w.property(X_SECURITY,writer -> {
                        writer.startObject();
                        writer.property(USER_REQUIRED, true);
                        writer.endObject();
                    });
                }
            }
        }

		if(o.getConsumes().length > 0) {
			w.property(CONSUMES, o.getConsumes());
		}
		
		if(o.getProduces().length > 0) {
			w.property(PRODUCES, o.getProduces());
		}
		
		//writeDefaultSecurity(context, m, w);

		//write parameters
		if(o.getParameters().length > 0) {
			w.property(PARAMETERS, () -> writeParameters(context, m, w, o.getParameters()));
		}
		
		//write responses
		if(o.getResponses().length > 0) {
			w.property(RESPONSES, () -> writeResponses(context, m, w, o.getResponses()) );
		}
		
		w.endObject();
	}

	protected void writeSecurities(WriteContext context, ApiMetadata m, JsonWriter w, MApiSecurityReq[] sc){
    	if(sc == null){
			return;
		}

		for(MApiSecurityReq s : sc){
			writeSecurity(context,m,w,s);
		}
	}

	protected void writeSecurity(WriteContext context, ApiMetadata m, JsonWriter w, MApiSecurityReq sc){
		w.startObject();
		w.property(sc.getName(),sc.getScopes());
		w.endObject();
	}

	protected void writeParameters(WriteContext context, ApiMetadata m, JsonWriter w, MApiParameter[] ps) {
        w.array(ps, p -> writeParameter(context, m, w, p));
	}
	
	protected void writeParameter(WriteContext context, ApiMetadata m, JsonWriter w, MApiParameter p) {
		w.startObject();
		
		w.property(NAME, p.getName())
         .propertyOptional(SUMMARY, p.getSummary())
         .property(DESCRIPTION, nullToEmpty(Strings.firstNotEmpty(p.getDescription(),p.getSummary(), p.getTitle())))
         .property(IN, SwaggerMappings.in(p.getLocation()));

        w.propertyOptional(REQUIRED, p.getRequired());

        writeExtension(w, p.getExtension());

        try{
            if(Location.BODY == p.getLocation()) {
                w.property(SCHEMA, () -> {
                    w.startObject();
                    writeParameterType(context, m, w, p);
                    w.endObject();
                });
            }else{
                writeParameterType(context, m, w, p);
            }
        }catch(RuntimeException e) {
            throw e;
        }

		w.endObject();
	}
	
	protected void writeResponses(WriteContext context, ApiMetadata m, JsonWriter w, MApiResponse[] rs) {
		w.startObject();

		for(MApiResponse r : rs) {
			w.property(r.getName(), () -> writeResponse(context, m, w, r));
		}
		
		w.endObject();
	}
	
	protected void writeResponse(WriteContext context, ApiMetadata m, JsonWriter w, MApiResponse r) {
		w.startObject();

        //w.propertyOptional(SUMMARY, r.getSummary()); not standard
		w.property(DESCRIPTION, nullToEmpty(Strings.firstNotEmpty(r.getDescription(), r.getSummary())));
		
		MType type = r.getType();
		if(null != type && !type.isVoidType()) {
			w.property(SCHEMA, () -> {
				w.startObject();
				writeType(context, m, w, type);
				w.endObject();
			});
		}

        if(r.getHeaders().length > 0) {
            w.property("headers",() -> {

                w.startObject();

                for(MApiHeader header : r.getHeaders()) {

                    w.property(header.getName(),() -> {

                        w.startObject();

                        w.property(DESCRIPTION, nullToEmpty(header.getDescription()));
                        writeType(context, m, w, header.getType());
                        w.endObject();

                    });

                }

                w.endObject();

            });
        }
		
		w.endObject();
	}
	
	protected void writeDefinitions(WriteContext context, ApiMetadata m, JsonWriter w) {
		w.startObject();
		
		for(Entry<String, MApiModel> entry : m.getModels().entrySet()) {
			String   name  = entry.getKey();
			MApiModel model = entry.getValue();
			
			w.property(name, () -> writeModel(context, m, w, model));
		}
		
		w.endObject();
	}
	
    protected void writeSecurityDefs(WriteContext context, ApiMetadata m, JsonWriter w) {
        w.startObject();

        for(MApiSecurityDef def : m.getSecurityDefs()) {
            
            if(def instanceof MOAuth2ApiSecurityDef) {
                writeOAuth2SecurityDef(context, m, w, (MOAuth2ApiSecurityDef)def);
                continue;
            }
            
        }

        w.endObject();
    }
    
    protected void writeOAuth2SecurityDef(WriteContext context, ApiMetadata m, JsonWriter w, MOAuth2ApiSecurityDef d) {
        switch (d.getFlow()){
			case SwaggerConstants.IMPLICIT:
				writeOAuth2Implicit(context, m, w, d);
				break;
			case SwaggerConstants.ACCESS_CODE:
				writeOAuth2AccessCode(context, m, w, d);
				break;
            case SwaggerConstants.APPLICATION:
                writeOAuth2Application(context, m, w, d);
                break;
            case SwaggerConstants.PASSWORD:
                writeOAuth2Password(context, m, w, d);
                break;
			default:
				throw new ApiConfigException("not support flow:"+d.getFlow());
		}
    }
    
    protected void writeOAuth2Implicit(WriteContext context, ApiMetadata m, JsonWriter w, MOAuth2ApiSecurityDef d) {
        w.property(d.getName(), () -> {
            w.startObject();
            
            w.property(TYPE, SwaggerConstants.OAUTH2)
            .property(FLOW, IMPLICIT)
            .property(AUTHZ_URL, d.getAuthzEndpointUrl());
            
            writeOAuth2Scopes(context, m, w, d, m.getPermissions());
            
            w.endObject();
        });
    }
    
    protected void writeOAuth2AccessCode(WriteContext context, ApiMetadata m, JsonWriter w, MOAuth2ApiSecurityDef d) {
        w.property(d.getName(), () -> {
            w.startObject();
            
            w.property(TYPE, SwaggerConstants.OAUTH2)
            .property(FLOW, ACCESS_CODE)
            .property(AUTHZ_URL, d.getAuthzEndpointUrl())
            .property(TOKEN_URL, d.getTokenEndpointUrl());
            
            writeOAuth2Scopes(context, m, w, d, m.getPermissions());
            
            w.endObject();
        });
    }

    protected void writeOAuth2Application(WriteContext context, ApiMetadata m, JsonWriter w, MOAuth2ApiSecurityDef d) {
        w.property(d.getName(), () -> {
            w.startObject();

            w.property(TYPE, SwaggerConstants.OAUTH2)
                    .property(FLOW, APPLICATION)
                    .property(TOKEN_URL, d.getTokenEndpointUrl());

            writeOAuth2Scopes(context, m, w, d, m.getPermissions());

            w.endObject();
        });
    }

    protected void writeOAuth2Password(WriteContext context, ApiMetadata m, JsonWriter w, MOAuth2ApiSecurityDef d) {
        w.property(d.getName(), () -> {
            w.startObject();

            w.property(TYPE, SwaggerConstants.OAUTH2)
                    .property(FLOW, PASSWORD)
                    .property(TOKEN_URL, d.getTokenEndpointUrl());

            writeOAuth2Scopes(context, m, w, d, m.getPermissions());

            w.endObject();
        });
    }
    
    protected void writeOAuth2Scopes(WriteContext context, ApiMetadata m, JsonWriter w, MOAuth2ApiSecurityDef d, MApiPermission[] scopes) {
        w.property(SCOPES, () -> {
            w.startObject();
            if(null != scopes) {
                for (MApiPermission scope : scopes) {
                    w.property(scope.getValue(), Strings.trim(scope.getDescription()));
                }
            }
            w.endObject();
        });
    }

    protected void writeTags(WriteContext context, ApiMetadata m, JsonWriter w) {

        w.array(m.getTags(), (tag) -> {

            w.startObject();

            w.property(NAME, tag.getName());
            w.propertyOptional(DESCRIPTION, tag.descOrSummaryOrTitle());

            w.endObject();

        });

    }

	protected void writeModel(WriteContext context, ApiMetadata m, JsonWriter w, MApiModel model) {
		w.startObject();
        writeModelWithinObject(context, m, w, model);
		w.endObject();
	}

    protected void writeModelWithinObject(WriteContext context, ApiMetadata m, JsonWriter w, MApiModel model) {
        if(!model.hasBaseModel()) {
            w.property(TYPE, OBJECT);
        }

        if(model.isEntity()) {
            w.property(X_ENTITY, true);
        }

        writeModelExtension(context, m, w, model.getExtension());

        w.propertyOptional(TITLE, model.getTitle());
        w.propertyOptional(SUMMARY, model.getSummary());
        w.propertyOptional(DESCRIPTION, model.getDescription());

        if(!model.hasBaseModel()) {
            writeModelProperties(context, m, w, model);
        }else{
            w.property(ALL_OF, () -> {

                w.startArray();

                //item1 : the base model.
                w.startObject()
                        .property(REF, ref(model.getBaseName()))
                        .endObject();

                w.separator();

                //item2 : self
                w.startObject()
                        .property(TYPE, OBJECT);
                writeModelProperties(context, m, w, model);
                w.endObject();

                w.endArray();

            });
        }
    }

    protected void writeModelProperties(WriteContext context, ApiMetadata m, JsonWriter w, MApiModel model) {
        for(MApiProperty p : model.getProperties()) {
            if(p.isDiscriminator()) {
                w.property(DISCRIMINATOR, p.getName());
                break;
            }
        }

        List<String> requiredProperties = New.arrayList();
        for(MApiProperty p : model.getProperties()) {
            if(isRequired(p)) {
                requiredProperties.add(p.getName());
            }
        }

        if(!requiredProperties.isEmpty()) {
            w.property(REQUIRED, requiredProperties);
        }

        w.property(PROPERTIES, () -> {
            w.startObject();

            for(MApiProperty p : model.getProperties()) {
                if (p.isHidden()) {
                    continue;
                }
                writeModelProperty(context, m, w, p);
            }

            w.endObject();
        });
    }

    protected void writeModelProperty(WriteContext context, ApiMetadata m, JsonWriter w, MApiProperty p) {
        w.property(propertyName(p.getName()), () -> {
            w.startObject();

            w.propertyOptional(TITLE, p.getTitle());
            w.property(DESCRIPTION, Strings.nullToEmpty(Strings.firstNotEmpty(p.getDescription(),p.getSummary())));
            writeParameterType(context, m, w, p);

            if(p.isReadonly()) {
                w.property(READONLY, true);
            }

            writeExtension(w, p.getExtension());

            if(p.isIdentity()) {
                w.property(X_IDENTITY, true);
            }

            if(p.isUnique()) {
                w.property(X_UNIQUE, true);
            }

            w.propertyOptional(X_SELECTABLE, p.getSelectable());
            w.propertyOptional(X_AGGREGATABLE, p.getAggregatable());
            w.propertyOptional(X_GROUPABLE,  p.getGroupable());
            w.propertyOptional(X_CREATABLE,  p.getCreatable());
            w.propertyOptional(X_UPDATABLE,  p.getUpdatable());
            w.propertyOptional(X_SORTABLE,   p.getSortable());
            w.propertyOptional(X_FILTERABLE, p.getFilterable());

            if(p.isReference()) {
                w.propertyOptional(X_EXPANDABLE, p.getExpandable());
            }

            w.endObject();
        });
    }

    protected boolean isRequired(MApiProperty p) {
        return !p.isReadonly() && (null != p.getRequired() && p.getRequired());
    }

	protected void writeParameterType(WriteContext context, ApiMetadata m, JsonWriter w, MApiParameterBase p) {
		MType type = p.getType();

		if(type.isSimpleType()) {
			writeSimpleParameterType(context, m, w, p, type.asSimpleType());
			return;
		}

        if(type.isObjectType()) {
            writeObjectType(context, m, w);
            return;
        }
		
		if(type.isCollectionType()) {
			writeArrayParameterType(context, m, w, p, type.asCollectionType());
			return;
		}
		
		if(type.isTypeRef()) {
			writeRefParameterType(context, m, w, p, type.asTypeRef());
			return;
		}

        if(type.isDictionaryType()) {
            writeDictionaryType(context, m, w, type.asDictionaryType());
            return;
        }

        if(type.isComplexType()){
            MComplexType ct = type.asComplexType();
            MApiModel model = null;
            if(null != ct.getJavaType()) {
                model = m.tryGetModel(ct.getJavaType());
            }else {
                model = m.tryGetModel(ct.getName());
            }
            if(null != model) {
                writeRefType(context, m, w, ct.createTypeRef());
            }else {
                model = new MApiModelBuilder(ct).build();
                w.property(TYPE, OBJECT);
                writeModelProperties(context, m, w, model);
            }
            return;
        }
		
		throw new IllegalStateException("Unsupported type kind '" + type.getTypeKind() + "' of parameter '" + p.getName() + "'");
	}

	protected void writeSimpleParameterType(WriteContext context, ApiMetadata m, JsonWriter w, MApiParameterBase p, MSimpleType st) {
        if(p.isFile()) {
            w.property(TYPE, FILE);
        }else{
            SwaggerType type = writeSimpleType(context, m, w, st);

            //format
            if(null != type && null == type.format()) {
                w.propertyOptional(FORMAT, p.getFormat());
            }
        }

        w.propertyOptional(DEFAULT, p.getDefaultValue());

        MApiValidation v = p.getValidation();
        if(null != v) {
            w.propertyOptional(PATTERN,    v.getPattern());
            w.propertyOptional(MAX_LENGTH, v.getMaxLength());
            w.propertyOptional(MIN_LENGTH, v.getMinLength());
            w.propertyOptional(ENUM,       v.getEnumValues());

            if(null != v.getMaximum()) {
                w.property(MAXIMUM, v.getMaximum());
                w.property(EXCLUSIVE_MAXIMUM, v.isExclusiveMaximum());
            }

            if(null != v.getMinimum()) {
                w.property(MINIMUM, v.getMinimum());
                w.property(EXCLUSIVE_MINIMUM, v.isExclusiveMinimum());
            }
        }

        if(null == v || null == v.getEnumValues()) {
            w.propertyOptional(ENUM, p.getEnumValues());
        }
    }


    protected void writeObjectType(WriteContext context, ApiMetadata m, JsonWriter w) {
        w.property(TYPE, OBJECT);
    }

    protected void writeDictionaryType(WriteContext context, ApiMetadata m, JsonWriter w, MDictionaryType type) {
        w.property(TYPE, OBJECT);
        w.property(ADDITIONAL_PROPERTIES, () -> {
            w.startObject();
            writeType(context, m, w, type.getValueType());
            w.endObject();
        });
    }
	
	protected void writeArrayParameterType(WriteContext context, ApiMetadata m, JsonWriter w, MApiParameterBase p, MCollectionType ct) {
		writeArrayType(context, m, w, ct);
	}
	
	protected void writeRefParameterType(WriteContext context, ApiMetadata m, JsonWriter w, MApiParameterBase p, MTypeRef tr) {
		writeRefType(context, m, w, tr);
	}
	
	protected void writeRefType(WriteContext context, ApiMetadata m, JsonWriter w, MTypeRef tr) {
        if(!m.getModels().containsKey(tr.getRefTypeName())) {
            throw new IllegalStateException("The referenced type '" + tr.getRefTypeName() + "' not exists!");
        }
		w.property(REF, ref(tr.getRefTypeName()));
	}

    protected String ref(String name) {
        return "#/definitions/" + name;
    }

	protected void writeArrayType(WriteContext context, ApiMetadata m, JsonWriter w, MCollectionType ct) {
		w.property(TYPE, ARRAY)
		 .property(ITEMS,() -> {
			 w.startObject();
			 writeType(context, m, w, ct.getElementType());
			 w.endObject();
		 });
	}
	
	protected void writeType(WriteContext context, ApiMetadata m, JsonWriter w, MType type) {
        if(type.isVoidType()) {
            return;
        }

		if(type.isSimpleType()) {
			writeSimpleType(context, m, w, type.asSimpleType());
			return;
		}

        if(type.isObjectType()) {
            writeObjectType(context, m, w);
            return;
        }

		if(type.isCollectionType()) {
			writeArrayType(context, m, w, type.asCollectionType());
			return;
		}
		
		if(type.isTypeRef()) {
			writeRefType(context, m, w, type.asTypeRef());
			return;
		}

        if(type.isDictionaryType()) {
            writeDictionaryType(context, m, w, type.asDictionaryType());
            return;
        }

        if(type.isComplexType()) {
            MComplexType ct = type.asComplexType();
            if(m.getModels().containsKey(ct.getName())) {
                writeRefType(context, m, w, ((MComplexType)type).createTypeRef());
            }else {
                MApiModel model = new MApiModelBuilder(ct).build();
                writeModelWithinObject(context, m, w, model);
            }
            return;
        }
        
		throw new IllegalStateException("Unsupported type kind '" + type.getTypeKind() + "'");
	}

    public SwaggerType convertSimpleType(MSimpleType st) {
        SwaggerType type = null;

        MSimpleTypeKind k = st.getSimpleTypeKind();

        if(k == MSimpleTypeKind.BIGINT) {
            type = SwaggerType.LONG;
        }else if(k == MSimpleTypeKind.BINARY) {
            type = SwaggerType.BINARY;
        }else if(k == MSimpleTypeKind.BOOLEAN) {
            type = SwaggerType.BOOLEAN;
        }else if(k == MSimpleTypeKind.BYTE) {
            type = SwaggerType.BYTE;
        }else if(k == MSimpleTypeKind.DATETIME) {
            type = SwaggerType.DATETIME;
        }else if(k == MSimpleTypeKind.DATE) {
            type = SwaggerType.DATE;
        }else if(k == MSimpleTypeKind.TIME) {
            type = SwaggerType.TIME;
        }else if(k == MSimpleTypeKind.DECIMAL) {
            type = SwaggerType.DOUBLE;
        }else if(k == MSimpleTypeKind.DOUBLE) {
            type = SwaggerType.DOUBLE;
        }else if(k == MSimpleTypeKind.INTEGER) {
            type = SwaggerType.INTEGER;
        }else if(k == MSimpleTypeKind.SINGLE) {
            type = SwaggerType.FLOAT;
        }else if(k == MSimpleTypeKind.SMALLINT) {
            type = SwaggerType.INTEGER;
        }else if(k == MSimpleTypeKind.STRING) {
            type = SwaggerType.STRING;
        }

        return type;
    }
	
	protected SwaggerType writeSimpleType(WriteContext context, ApiMetadata m, JsonWriter w, MSimpleType st) {
		SwaggerType type = convertSimpleType(st);
		if(null == type) {
			throw new IllegalStateException("Unsupported type '" + st + "' in swagger");
		}
		
		w.property(TYPE, type.type());

        if(null != type.format()) {
            w.property(FORMAT, type.format());
        }

        return type;
	}
}
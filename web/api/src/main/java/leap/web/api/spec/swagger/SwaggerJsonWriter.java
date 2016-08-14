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

import static leap.lang.Strings.nullToEmpty;
import static leap.web.api.spec.swagger.SwaggerConstants.*;

import java.util.Map.Entry;

import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.lang.json.JsonWriter;
import leap.lang.meta.*;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiOperation;
import leap.web.api.meta.model.MApiParameter;
import leap.web.api.meta.model.MApiParameter.Location;
import leap.web.api.meta.model.MApiParameterBase;
import leap.web.api.meta.model.MApiPath;
import leap.web.api.meta.model.MApiProperty;
import leap.web.api.meta.model.MApiResponse;
import leap.web.api.meta.model.MApiSecurityDef;
import leap.web.api.meta.model.MOAuth2ApiSecurityDef;
import leap.web.api.meta.model.MOAuth2Scope;
import leap.web.api.spec.ApiSpecContext;
import leap.web.api.spec.JsonSpecWriter;

public class SwaggerJsonWriter extends JsonSpecWriter {
    
    protected static final String OAUTH             = "oauth";
    protected static final String OAUTH_ACCESS_CODE = "oauth_access_code";
    
    protected static final class WriteContext {
        String   defaultSecurity;
        String[] defaultScopes;
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
			 .property(TITLE, m.getTitle())
			 .property(DESCRIPTION, nullToEmpty(m.descOrSummary()))
			 .propertyOptional(TERMS_OF_SERVICE, m.getTermsOfService())
			 .propertyOptional(CONCAT, m.getConcat())
			 .propertyOptional(VERSION, m.getVersion())
			 .endObject();
		});
		
		w.propertyOptional(HOST, getHost(sc, m))
		 .propertyOptional(BASE_PATH, m.getBasePath())
		 .propertyOptional(SCHEMES, m.getProtocols())
		 .propertyOptional(CONSUMES, m.getConsumes())
		 .propertyOptional(PRODUCES, m.getProduces());
		
        if(!Arrays2.isEmpty(m.getSecurityDefs())) {
            w.property(SECURITY_DEFINITIONS, () -> writeSecurityDefs(context, m, w));
        }
        
        writeDefaultSecurity(context, m, w);
		
		w.property(PATHS, () -> writePaths(context, m, w) )
		 .property(DEFINITIONS, () -> writeDefinitions(context, m, w) );		
		
		w.endObject();
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
	           w.array(context.defaultScopes);
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
			w.property(o.getMethod().name().toLowerCase(), () -> {
				writeOperation(context, m, w, p, o);
			});
		}
		
		w.endObject();
	}
	
	protected void writeOperation(WriteContext context, ApiMetadata m, JsonWriter w, MApiPath p, MApiOperation o) {
		w.startObject();
		
		w.property(DESCRIPTION, nullToEmpty(o.descOrSummary()));

        w.property(OPERATION_ID, o.getName()); //todo : unique id ?
		
		if(o.getConsumes().length > 0) {
			w.property(CONSUMES, o.getConsumes());
		}
		
		if(o.getProduces().length > 0) {
			w.property(PRODUCES, o.getProduces());
		}
		
		writeDefaultSecurity(context, m, w);

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
	
	protected void writeParameters(WriteContext context, ApiMetadata m, JsonWriter w, MApiParameter[] ps) {
        w.array(ps, p -> writeParameter(context, m, w, p));
	}
	
	protected void writeParameter(WriteContext context, ApiMetadata m, JsonWriter w, MApiParameter p) {
		w.startObject();
		
		w.property(NAME, p.getName())
         .property(DESCRIPTION, nullToEmpty(p.descOrSummary()))
         .property(IN, SwaggerMappings.in(p.getLocation()))
         .propertyOptional(REQUIRED, p.getRequired());
		
		if(Location.BODY == p.getLocation()) {
			w.property(SCHEMA, () -> {
				w.startObject();
				writeParameterType(context, m, w, p);
				w.endObject();
			});
		}else{
			writeParameterType(context, m, w, p);
		}
		
		w.endObject();
	}
	
	protected void writeResponses(WriteContext context, ApiMetadata m, JsonWriter w, MApiResponse[] rs) {
		w.startObject();

		for(MApiResponse r : rs) {
			w.property(String.valueOf(r.getStatus()), () -> writeResponse(context, m, w, r));
		}
		
		w.endObject();
	}
	
	protected void writeResponse(WriteContext context, ApiMetadata m, JsonWriter w, MApiResponse r) {
		w.startObject();

		w.property(DESCRIPTION, nullToEmpty(r.descOrSummary()));
		
		MType type = r.getType();
		if(null != type && !type.isVoidType()) {
			w.property(SCHEMA, () -> {
				w.startObject();
				writeType(context, m, w, type);
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
            
            throw new IllegalStateException("Unsupported api security def : " + def);
        }

        w.endObject();
    }
    
    protected void writeOAuth2SecurityDef(WriteContext context, ApiMetadata m, JsonWriter w, MOAuth2ApiSecurityDef d) {
        context.defaultSecurity = OAUTH;
        if(null != d.getScopes() && d.getScopes().length > 0) {
            String[] values = new String[d.getScopes().length];
            for(int i=0;i<values.length;i++) {
                values[i] = d.getScopes()[i].getValue();
            }
            context.defaultScopes = values;
        }
        
        writeOAuth2Implicit(context, m, w, d);
        //writeOAuth2AccesCode(context, m, w, d);
    }
    
    protected void writeOAuth2Implicit(WriteContext context, ApiMetadata m, JsonWriter w, MOAuth2ApiSecurityDef d) {
        w.property(OAUTH, () -> {
            w.startObject();
            
            w.property(TYPE, OAUTH2)
            .property(FLOW, IMPLICIT)
            .property(AUTHZ_URL, d.getAuthzEndpointUrl());
            
            writeOAuth2Scopes(context, m, w, d, d.getScopes());
            
            w.endObject();
        });
    }
    
    protected void writeOAuth2AccesCode(WriteContext context, ApiMetadata m, JsonWriter w, MOAuth2ApiSecurityDef d) {
        w.property(OAUTH_ACCESS_CODE, () -> {
            w.startObject();
            
            w.property(TYPE, OAUTH2)
            .property(FLOW, ACCESS_CODE)
            .property(AUTHZ_URL, d.getAuthzEndpointUrl())
            .property(TOKEN_URL, d.getTokenEndpointUrl());
            
            writeOAuth2Scopes(context, m, w, d, d.getScopes());
            
            w.endObject();
        });
    }
    
    protected void writeOAuth2Scopes(WriteContext context, ApiMetadata m, JsonWriter w, MOAuth2ApiSecurityDef d, MOAuth2Scope[] scopes) {
        w.property(SCOPES, () -> {
            w.startObject();
            if(null != scopes) {
                for (MOAuth2Scope scope : d.getScopes()) {
                    w.property(scope.getValue(), scope.getDescription());
                }
            }
            w.endObject();
        });
    }
	
	protected void writeModel(WriteContext context, ApiMetadata m, JsonWriter w, MApiModel model) {
		w.startObject();

        w.property(TYPE, OBJECT);

		w.property(PROPETIES, () -> {
			w.startObject();
			
			for(MApiProperty p : model.getProperties()) {
				w.property(propertyName(p.getName()), () -> {
					w.startObject();

					writeParameterType(context, m, w, p);

                    w.propertyOptional(REQUIRED,     p.getRequired());
                    w.propertyOptional(X_CREATABLE,  p.getCreatable());
                    w.propertyOptional(X_UPDATABLE,  p.getUpdatable());
                    w.propertyOptional(X_SORTABLE,   p.getSortable());
                    w.propertyOptional(X_FILTERABLE, p.getFilterable());

                    w.endObject();
				});
			}
			
			w.endObject();
		});
		
		w.endObject();
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
		
		throw new IllegalStateException("Unsupported type kind '" + type.getTypeKind() + "' of parameter '" + p.getName() + "'");
	}

	protected void writeSimpleParameterType(WriteContext context, ApiMetadata m, JsonWriter w, MApiParameterBase p, MSimpleType st) {
        if(p.isFile()) {
            w.property(TYPE, FILE);
        }else{
            writeSimpleType(context, m, w, st);
        }
        w.propertyOptional(ENUM, p.getEnumValues());
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
		String ref = "#/definitions/" + tr.getRefTypeName();
		w.property(REF, ref);
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
		
		throw new IllegalStateException("Unsupported type kind '" + type.getTypeKind() + "'");
	}
	
	protected void writeSimpleType(WriteContext context, ApiMetadata m, JsonWriter w, MSimpleType st) {
		SwaggerType type;
		
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
		}else{
			throw new IllegalStateException("Unsupported type '" + k + "' in swagger");
		}
		
		w.property(TYPE, type.type());

        if(null != type.fomrat()) {
            w.property(FORMAT, type.fomrat());
        }
	}
}
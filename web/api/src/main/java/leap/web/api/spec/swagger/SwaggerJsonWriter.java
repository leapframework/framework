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
import static leap.web.api.spec.swagger.SwaggerConstants.ACCESS_CODE;
import static leap.web.api.spec.swagger.SwaggerConstants.ARRAY;
import static leap.web.api.spec.swagger.SwaggerConstants.AUTHZ_URL;
import static leap.web.api.spec.swagger.SwaggerConstants.BASE_PATH;
import static leap.web.api.spec.swagger.SwaggerConstants.CONCAT;
import static leap.web.api.spec.swagger.SwaggerConstants.CONSUMES;
import static leap.web.api.spec.swagger.SwaggerConstants.DEFINITIONS;
import static leap.web.api.spec.swagger.SwaggerConstants.DESCRIPTION;
import static leap.web.api.spec.swagger.SwaggerConstants.FLOW;
import static leap.web.api.spec.swagger.SwaggerConstants.FORMAT;
import static leap.web.api.spec.swagger.SwaggerConstants.HOST;
import static leap.web.api.spec.swagger.SwaggerConstants.IMPLICIT;
import static leap.web.api.spec.swagger.SwaggerConstants.IN;
import static leap.web.api.spec.swagger.SwaggerConstants.INFO;
import static leap.web.api.spec.swagger.SwaggerConstants.ITEMS;
import static leap.web.api.spec.swagger.SwaggerConstants.NAME;
import static leap.web.api.spec.swagger.SwaggerConstants.OAUTH2;
import static leap.web.api.spec.swagger.SwaggerConstants.PARAMETERS;
import static leap.web.api.spec.swagger.SwaggerConstants.PATHS;
import static leap.web.api.spec.swagger.SwaggerConstants.PRODUCES;
import static leap.web.api.spec.swagger.SwaggerConstants.PROPETIES;
import static leap.web.api.spec.swagger.SwaggerConstants.REF;
import static leap.web.api.spec.swagger.SwaggerConstants.REQUIRED;
import static leap.web.api.spec.swagger.SwaggerConstants.RESPONSES;
import static leap.web.api.spec.swagger.SwaggerConstants.SCHEMA;
import static leap.web.api.spec.swagger.SwaggerConstants.SCHEMES;
import static leap.web.api.spec.swagger.SwaggerConstants.SCOPES;
import static leap.web.api.spec.swagger.SwaggerConstants.SECURITY;
import static leap.web.api.spec.swagger.SwaggerConstants.SECURITY_DEFINITIONS;
import static leap.web.api.spec.swagger.SwaggerConstants.SWAGGER;
import static leap.web.api.spec.swagger.SwaggerConstants.TERMS_OF_SERVICE;
import static leap.web.api.spec.swagger.SwaggerConstants.TITLE;
import static leap.web.api.spec.swagger.SwaggerConstants.TOKEN_URL;
import static leap.web.api.spec.swagger.SwaggerConstants.TYPE;
import static leap.web.api.spec.swagger.SwaggerConstants.VERSION;

import java.util.Map.Entry;

import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.json.JsonWriter;
import leap.lang.meta.MCollectionType;
import leap.lang.meta.MSimpleType;
import leap.lang.meta.MSimpleTypeKind;
import leap.lang.meta.MType;
import leap.lang.meta.MTypeRef;
import leap.web.api.meta.model.ApiMetadata;
import leap.web.api.meta.model.ApiModel;
import leap.web.api.meta.model.ApiOperation;
import leap.web.api.meta.model.ApiParameter;
import leap.web.api.meta.model.ApiParameter.Location;
import leap.web.api.meta.model.ApiParameterBase;
import leap.web.api.meta.model.ApiPath;
import leap.web.api.meta.model.ApiProperty;
import leap.web.api.meta.model.ApiResponse;
import leap.web.api.meta.model.ApiSecurityDef;
import leap.web.api.meta.model.OAuth2ApiSecurityDef;
import leap.web.api.meta.model.OAuth2Scope;
import leap.web.api.spec.JsonSpecWriter;

public class SwaggerJsonWriter extends JsonSpecWriter {
    
    protected static final String OAUTH             = "oauth";
    protected static final String OAUTH_ACCESS_CODE = "oauth_access_code";
    
    protected static final class WriteContext {
        String   defaultSecurity;
        String[] defaultScopes;
    }

	@Override
	protected void write(ApiMetadata m, JsonWriter w) {
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
		
		w.propertyOptional(HOST, m.getHost())
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
	
		for(Entry<String, ApiPath> entry : m.getPaths().entrySet()) {
			w.property(entry.getKey(), () -> writePath(context, m, w, entry.getValue()) );
		}
		
		w.endObject();
	}
	
	protected void writePath(WriteContext context, ApiMetadata m, JsonWriter w, ApiPath p) {
		w.startObject();

		for(ApiOperation o : p.getOperations()) {
			w.property(o.getMethod().name().toLowerCase(), () -> {
				writeOperation(context, m, w, p, o);
			});
		}
		
		w.endObject();
	}
	
	protected void writeOperation(WriteContext context, ApiMetadata m, JsonWriter w, ApiPath p, ApiOperation o) {
		w.startObject();
		
		w.property(DESCRIPTION, nullToEmpty(o.descOrSummary()));
		
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
	
	protected void writeParameters(WriteContext context, ApiMetadata m, JsonWriter w, ApiParameter[] ps) {
		w.startArray();
		
		for(ApiParameter p : ps) {
			writeParameter(context, m, w, p);
		}
		
		w.endArray();
	}
	
	protected void writeParameter(WriteContext context, ApiMetadata m, JsonWriter w, ApiParameter p) {
		w.startObject();
		
		w.property(NAME, p.getName())
         .property(DESCRIPTION, nullToEmpty(p.descOrSummary()))
         .property(IN, SwaggerMappings.in(p.getLocation()))
         .property(REQUIRED, p.isRequired());
		
		if(Location.BODY == p.getLocation()) {
			w.property(SCHEMA, () -> {
				w.startObject();
				writeParameterProperties(context, m, w, p);
				w.endObject();
			});
		}else{
			writeParameterProperties(context, m, w, p);
		}
		
		w.endObject();
	}
	
	protected void writeResponses(WriteContext context, ApiMetadata m, JsonWriter w, ApiResponse[] rs) {
		w.startObject();

		for(ApiResponse r : rs) {
			w.property(String.valueOf(r.getStatus()), () -> writeResponse(context, m, w, r));
		}
		
		w.endObject();
	}
	
	protected void writeResponse(WriteContext context, ApiMetadata m, JsonWriter w, ApiResponse r) {
		w.startObject();

		w.property(DESCRIPTION, nullToEmpty(r.descOrSummary()));
		
		MType type = r.getType();
		if(null != type) {
			w.property(SCHEMA, () -> {
				w.startObject();
				writeTypeProperties(context, m, w, type);
				w.endObject();
			});
		}
		
		w.endObject();
	}
	
	protected void writeDefinitions(WriteContext context, ApiMetadata m, JsonWriter w) {
		w.startObject();
		
		for(Entry<String, ApiModel> entry : m.getModels().entrySet()) {
			String   name  = entry.getKey();
			ApiModel model = entry.getValue();
			
			w.property(name, () -> writeModel(context, m, w, model));
		}
		
		w.endObject();
	}
	
    protected void writeSecurityDefs(WriteContext context, ApiMetadata m, JsonWriter w) {
        w.startObject();

        for(ApiSecurityDef def : m.getSecurityDefs()) {
            
            if(def instanceof OAuth2ApiSecurityDef) {
                writeOAuth2SecurityDef(context, m, w, (OAuth2ApiSecurityDef)def);
                continue;
            }
            
            throw new IllegalStateException("Unsupported api security def : " + def);
        }

        w.endObject();
    }
    
    protected void writeOAuth2SecurityDef(WriteContext context, ApiMetadata m, JsonWriter w, OAuth2ApiSecurityDef d) {
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
    
    protected void writeOAuth2Implicit(WriteContext context, ApiMetadata m, JsonWriter w, OAuth2ApiSecurityDef d) {
        w.property(OAUTH, () -> {
            w.startObject();
            
            w.property(TYPE, OAUTH2)
            .property(FLOW, IMPLICIT)
            .property(AUTHZ_URL, d.getAuthzEndpointUrl());
            
            writeOAuth2Scopes(context, m, w, d, d.getScopes());
            
            w.endObject();
        });
    }
    
    protected void writeOAuth2AccesCode(WriteContext context, ApiMetadata m, JsonWriter w, OAuth2ApiSecurityDef d) {
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
    
    protected void writeOAuth2Scopes(WriteContext context, ApiMetadata m, JsonWriter w, OAuth2ApiSecurityDef d, OAuth2Scope[] scopes) {
        w.property(SCOPES, () -> {
            w.startObject();
            if(null != scopes) {
                for (OAuth2Scope scope : d.getScopes()) {
                    w.property(scope.getValue(), scope.getDescription());
                }
            }
            w.endObject();
        });
    }
	
	protected void writeModel(WriteContext context, ApiMetadata m, JsonWriter w, ApiModel model) {
		w.startObject();
		
		w.property(PROPETIES, () -> {
			w.startObject();
			
			for(ApiProperty p : model.getProperties()) {
				w.property(propertyName(p.getName()), () -> {
					w.startObject();
					writeParameterProperties(context, m, w, p);
					w.endObject();
				});
			}
			
			w.endObject();
		});
		
		w.endObject();
	}
	
	protected void writeParameterProperties(WriteContext context, ApiMetadata m, JsonWriter w, ApiParameterBase p) {
		MType type = p.getType();
		
		if(type.isSimpleType()) {
			writeSimpleParameterProperties(context, m, w, p, type.asSimpleType());
			return;
		}
		
		if(type.isCollectionType()) {
			writeArrayParameterProperties(context, m, w, p, type.asCollectionType());
			return;
		}
		
		if(type.isTypeRef()) {
			writeRefParameterProperties(context, m, w, p, type.asTypeRef());
			return;
		}
		
		throw new IllegalStateException("Unsupproted type kind '" + type.getTypeKind() + "'");
	}
	
	protected void writeSimpleParameterProperties(WriteContext context, ApiMetadata m, JsonWriter w, ApiParameterBase p, MSimpleType st) {
		writeSimpleTypePropeties(context, m, w, st);
		w.propertyOptional(FORMAT, p.getFormat());
	}
	
	protected void writeArrayParameterProperties(WriteContext context, ApiMetadata m, JsonWriter w, ApiParameterBase p, MCollectionType ct) {
		writeArrayTypeProperties(context, m, w, ct);
	}
	
	protected void writeRefParameterProperties(WriteContext context, ApiMetadata m, JsonWriter w, ApiParameterBase p, MTypeRef tr) {
		writeRefTypeProperties(context, m, w, tr);
	}
	
	protected void writeRefTypeProperties(WriteContext context, ApiMetadata m, JsonWriter w, MTypeRef tr) {
		String ref = "#/definitions/" + tr.getRefTypeName();
		w.property(REF, ref);
	}
	
	protected void writeArrayTypeProperties(WriteContext context, ApiMetadata m, JsonWriter w, MCollectionType ct) {
		w.property(TYPE, ARRAY)
		 .property(ITEMS,() -> {
			 w.startObject();
			 writeTypeProperties(context, m, w, ct.getElementType());
			 w.endObject();
		 });
	}
	
	protected void writeTypeProperties(WriteContext context, ApiMetadata m, JsonWriter w, MType type) {
		if(type.isSimpleType()) {
			writeSimpleTypePropeties(context, m, w, type.asSimpleType());
			return;
		}
		
		if(type.isCollectionType()) {
			writeArrayTypeProperties(context, m, w, type.asCollectionType());
			return;
		}
		
		if(type.isTypeRef()) {
			writeRefTypeProperties(context, m, w, type.asTypeRef());
			return;
		}
		
		throw new IllegalStateException("Unsupproted type kind '" + type.getTypeKind() + "'");
	}
	
	protected void writeSimpleTypePropeties(WriteContext context, ApiMetadata m, JsonWriter w, MSimpleType st) {
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
	}
}
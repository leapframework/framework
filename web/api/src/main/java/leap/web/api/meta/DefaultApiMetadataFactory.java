/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.api.meta;

import leap.core.annotation.Inject;
import leap.core.meta.MTypeManager;
import leap.core.web.path.PathTemplate;
import leap.lang.Enums;
import leap.lang.Strings;
import leap.lang.TypeInfo;
import leap.lang.Types;
import leap.lang.http.HTTP;
import leap.lang.http.MimeTypes;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.meta.*;
import leap.web.App;
import leap.web.action.Action;
import leap.web.action.Argument;
import leap.web.action.Argument.Location;
import leap.web.api.annotation.MetaApiResponse;
import leap.web.api.config.ApiConfig;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.meta.model.*;
import leap.web.multipart.MultipartFile;
import leap.web.route.Route;

import javax.servlet.http.Part;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class DefaultApiMetadataFactory implements ApiMetadataFactory {

	private static final Log log = LogFactory.get(DefaultApiMetadataFactory.class);
	
	protected @Inject App                    app;
	protected @Inject ApiMetadataProcessor[] processors;
	protected @Inject MTypeManager           mtypeManager;
	
	@Override
    public ApiMetadata createMetadata(ApiConfig c) {
		ApiMetadataBuilder md = new ApiMetadataBuilder();
		
		ApiMetadataContext context = createContext(c, md);
		
		setBaseInfo(context, md);
		
		createSecurityDefs(context, md);
		
		createPaths(context, md);
		
		return processMetadata(context, md);
    }
	
    protected ApiMetadataContext createContext(ApiConfig c, ApiMetadataBuilder md) {
		final MTypeFactory tf = createMTypeFactory(c, md);
		
		return new ApiMetadataContext() {
			
			@Override
			public MTypeFactory getMTypeFactory() {
				return tf;
			}
			
			@Override
			public ApiConfig getConfig() {
				return c;
			}
		};
	}
	
	protected MTypeFactory createMTypeFactory(ApiConfig c, ApiMetadataBuilder md) {
		return mtypeManager.factory()
                    .setListener(new MTypeListener() {
                        @Override
                        public void onComplexTypeCreated(MComplexType ct) {
                            md.addModel(new MApiModelBuilder(ct));
                        }
                    })

                    .setStrategy(new MTypeStrategy() {
                        @Override
                        public String getComplexTypeName(String name) {
                            for(String prefix : c.getRemovalModelNamePrefixes()) {
                                if(Strings.startsWithIgnoreCase(name, prefix)) {
                                    name = Strings.removeStartIgnoreCase(name, prefix);
                                    break;
                                }
                            }
                            return name;
                        }
                    })

                    .create();
	}

	protected void setBaseInfo(ApiMetadataContext context, ApiMetadataBuilder md) {
		ApiConfig c = context.getConfig();
		
		md.setBasePath(c.getBasePath());
		md.setName(c.getName());
		md.setTitle(c.getTitle());
		md.setVersion(c.getVersion());
		md.setSummary(c.getSummary());
		md.setDescription(c.getDescription());
		md.addProtocols(c.getProtocols());
		md.addProduces(c.getProduces());
		md.addConsumes(c.getConsumes());
	}
	
    protected void createSecurityDefs(ApiMetadataContext context, ApiMetadataBuilder md) {
        ApiConfig c = context.getConfig();
        if(c.isOAuthEnabled()) {
            MOAuth2ApiSecurityDef def =
                    new MOAuth2ApiSecurityDef(c.getOAuthAuthorizationUrl(),
                                             c.getOAuthTokenUrl(),
                                             c.getOAuthScopes());
            
            md.addSecurityDef(def);
        }
    }
	
	protected ApiMetadata processMetadata(ApiMetadataContext context, ApiMetadataBuilder md) {
		for(ApiMetadataProcessor p : processors) {
			p.preProcess(context, md);
		}

        processDefault(context, md);

		ApiMetadata m = md.build();
		
		for(ApiMetadataProcessor p : processors) {
			p.postProcess(context, m);
		}
		
		return m;
	}

    protected void processDefault(ApiMetadataContext context, ApiMetadataBuilder m) {
        String defaultMimeType = MimeTypes.APPLICATION_JSON;

        if(m.getConsumes().isEmpty()) {
            m.addConsume(defaultMimeType);
        }

        if(m.getProduces().isEmpty()) {
            m.addProduce(defaultMimeType);
        }
    }
	
	protected void createPaths(ApiMetadataContext context, ApiMetadataBuilder md) {
		for(Route route : context.getConfig().getRoutes()) {
			createApiPath(context, md, route);	
		}
	}
	
	protected void createApiPath(ApiMetadataContext context, ApiMetadataBuilder md, Route route) {
		PathTemplate pt = route.getPathTemplate();

		MApiPathBuilder path = md.getPath(pt.getTemplate());
		if(null == path) {
			path = new MApiPathBuilder();
			path.setPathTemplate(pt);
			md.addPath(path);
		}

        log.debug("Path {} -> {} :", pt, route.getAction());
		createApiOperation(context, md, route, path);
	}
	
	protected void createApiOperation(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiPathBuilder path) {
		MApiOperationBuilder op = new MApiOperationBuilder();
		
		op.setName(route.getAction().getName());

		//Set http method
		setApiMethod(context, m, route, path, op);

        log.debug(" {}", op.getMethod());
	
		//Create parameters.
		createApiParameters(context, m, route, path, op);
		
		//Create responses.
		createApiResponses(context, m, route, path, op);

		path.addOperation(op);
	}
	
	protected void setApiMethod(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiPathBuilder path, MApiOperationBuilder op) {
		String method = route.getMethod();
		
		if("*".equals(method)) {
			boolean hasBodyParameter = false;
			for(Argument a : route.getAction().getArguments()) {
				if(a.getLocation() == Location.REQUEST_BODY || a.getLocation() == Location.PART_PARAM) {
					hasBodyParameter = true;
				}
			}
			
			if(hasBodyParameter) {
				op.setMethod(HTTP.Method.POST);
			}else{
				op.setMethod(HTTP.Method.GET);
			}
		}else{
			op.setMethod(HTTP.Method.valueOf(method));
		}
	}
	
	protected void createApiParameters(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiPathBuilder path, MApiOperationBuilder op) {
		Action action = route.getAction();

        log.trace("  Parameters({})", action.getArguments().length);
		
		for(Argument a : action.getArguments()) {
            MApiParameterBuilder p = new MApiParameterBuilder();

            p.setName(a.getName());

            log.trace("   {}", a.getName(), p.getLocation());

            if(isParameterFileType(a.getType())) {
                p.setType(MSimpleTypes.BINARY);
                p.setFile(true);
                op.addConsume(MimeTypes.MULTIPART_FORM_DATA);
            }else{
                p.setType(createMType(context, m, a.getTypeInfo()));
            }

            p.setLocation(getParameterLocation(context, action, a, op, p));

            if (null != a.getRequired()) {
                p.setRequired(a.getRequired());
            } else if (p.getLocation() == MApiParameter.Location.PATH) {
                p.setRequired(true);
            }

            if(a.getType().isEnum()){
                p.setEnumValues(Enums.getValues(a.getType()));
            }

            op.addParameter(p);
        }
	}
	
	protected void createApiResponses(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiPathBuilder path, MApiOperationBuilder op) {
        MetaApiResponse[] annotations =
                route.getAction().getAnnotationsByType(MetaApiResponse.class);

        if(annotations.length > 0) {
            for(MetaApiResponse a : annotations) {
                op.addResponse(createApiResponse(context, m, route, a));
            }
        }else{
            if(route.getAction().hasReturnValue()) {
                Class<?> returnType        = route.getAction().getReturnType();
                Type     genericReturnType = route.getAction().getGenericReturnType();

                MApiResponseBuilder resp = MApiResponseBuilder.ok();

                resolveApiResponseType(context, m, returnType, genericReturnType, resp);

                op.addResponse(resp);
            }else{
                op.addResponse(MApiResponseBuilder.ok());
            }
        }
	}

    protected MApiResponseBuilder createApiResponse(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MetaApiResponse a) {
        MApiResponseBuilder resp = new MApiResponseBuilder();
        resp.setStatus(a.status());
        if(!route.getAction().hasReturnValue()) {
            return resp;
        }

        Class<?> returnType  = a.type();
        Type     genericType = a.genericType();

        if(Void.class.equals(returnType)) {
            returnType  = route.getAction().getReturnType();
            genericType = route.getAction().getGenericReturnType();
        }

        resolveApiResponseType(context, m, returnType, genericType, resp);

        return resp;
    }

    protected void resolveApiResponseType(ApiMetadataContext context, ApiMetadataBuilder m, Class<?> type, Type genericType, MApiResponseBuilder resp) {
        if(type.equals(ApiResponse.class) ) {
            if(null == genericType || genericType.equals(ApiResponse.class)) {
                return;
            }else{
                Type typeArgument = Types.getTypeArgument(genericType);

                type = Types.getActualType(typeArgument);
                genericType = typeArgument;
            }
        }

        if(isResponseFileType(type)) {
            resp.setType(MSimpleTypes.BINARY);
            resp.setFile(true);
        }else{
            resp.setType(createMType(context, m, Types.getTypeInfo(type, genericType)));
        }
    }
	
	protected MType createMType(ApiMetadataContext context, ApiMetadataBuilder m, TypeInfo ti) {
		if (ti.isSimpleType()) {
			return context.getMTypeFactory().getMType(ti.getType(), ti.getGenericType());
		} else if (ti.isCollectionType()) {
			TypeInfo eti = ti.getElementTypeInfo();
			MType elementType = createMType(context, m, eti);
			return new MCollectionType(elementType);
		} else {
			//Complex Type
			MType mtype = context.getMTypeFactory().getMType(ti.getType(), ti.getGenericType());

            if(mtype.isDictionaryType()) {
                return mtype;
            }else if(mtype.isTypeRef()) {
				MComplexTypeRef ref = (MComplexTypeRef)mtype;
				return new MComplexTypeRef(ref.getRefTypeName(), ref.getRefTypeQName());
			}else{
				MComplexType ct = mtype.asComplexType();
				return new MComplexTypeRef(ct.getName());
			}
		}
	}
	
	protected MApiParameter.Location getParameterLocation(ApiMetadataContext context, Action action, Argument arg, MApiOperationBuilder o, MApiParameterBuilder p) {
		Location from = arg.getLocation();
		if(null == from || from == Location.UNDEFINED) {
			
			if(p.getType().isTypeRef() || p.getType().isCollectionType()) {
				return MApiParameter.Location.BODY;
			}else{
				if(o.getMethod() == HTTP.Method.GET) {
					return MApiParameter.Location.QUERY;
				}else{
					return MApiParameter.Location.FORM;
				}
			}
		}
		
		if(from == Location.QUERY_PARAM) {
		    return MApiParameter.Location.QUERY;
		}
		
		if(from == Location.PATH_PARAM) {
			return MApiParameter.Location.PATH;
		}

        if(from == Location.HEADER_PARAM) {
            return MApiParameter.Location.HEADER;
        }

        if(from == Location.PART_PARAM) {
            return MApiParameter.Location.FORM;
        }
		
		if(from == Location.REQUEST_BODY) {
			return MApiParameter.Location.BODY;
		}
		
		if(from == Location.REQUEST_PARAM) {
			if(o.getMethod() == HTTP.Method.GET) {
				return MApiParameter.Location.QUERY;
			}else{
				return MApiParameter.Location.FORM;
			}
		}
		
		throw new IllegalStateException("Unsupported location '" + from + "' by swagger in parameter '" + arg + "'");
	}

    private static final Set<Class<?>> PARAM_FILE_TYPES = new HashSet<>();
    static {
        PARAM_FILE_TYPES.add(Part.class);
        PARAM_FILE_TYPES.add(MultipartFile.class);
    }

    protected boolean isParameterFileType(Class<?> c) {
        return PARAM_FILE_TYPES.contains(c);
    }

    protected boolean isResponseFileType(Class<?> c) {
        return File.class.equals(c);
    }
}
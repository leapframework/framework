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
import leap.core.meta.MTypeContainer;
import leap.core.meta.MTypeManager;
import leap.core.web.path.PathTemplate;
import leap.lang.Classes;
import leap.lang.Enums;
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.lang.http.MimeTypes;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.meta.MComplexType;
import leap.lang.meta.MSimpleTypes;
import leap.lang.meta.MType;
import leap.lang.meta.MTypeStrategy;
import leap.web.App;
import leap.web.action.Action;
import leap.web.action.Argument;
import leap.web.action.Argument.Location;
import leap.web.api.Api;
import leap.web.api.annotation.ApiModel;
import leap.web.api.annotation.Response;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.model.ModelConfig;
import leap.web.api.config.model.OAuthConfig;
import leap.web.api.meta.model.*;
import leap.web.api.route.ApiRoute;
import leap.web.api.spec.swagger.SwaggerConstants;
import leap.web.multipart.MultipartFile;
import leap.web.route.Route;
import javax.servlet.http.Part;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultApiMetadataFactory implements ApiMetadataFactory {

	private static final Log log = LogFactory.get(DefaultApiMetadataFactory.class);
	
	protected @Inject App                    app;
	protected @Inject ApiMetadataProcessor[] processors;
	protected @Inject MTypeManager           mtypeManager;
    protected @Inject ApiMetadataStrategy    strategy;

	@Override
    public ApiMetadata createMetadata(Api api) {
		ApiMetadataBuilder md = api.getConfigurator().getMetadata();
        if(null == md) {
            md = new ApiMetadataBuilder();
        }
		
		ApiMetadataContext context = createContext(api, md);

        prepareMetadata(context, md);
		
		setBaseInfo(context, md);

        createResponses(context, md);

        createPermissions(context, md);
		
		createSecurityDefs(context, md);
		
		createPaths(context, md);

        createModels(context, md);

        createTags(context, md);
		
		return processMetadata(context, md);
    }

    private void prepareMetadata(ApiMetadataContext context, ApiMetadataBuilder md) {

        final ApiConfig c = context.getConfig();

        c.getApiRoutes().forEach(ar -> {

            Route route = ar.getRoute();

            if(!c.isCorsDisabled() && !route.isCorsDisabled()) {
                route.setCorsEnabled(true);
            }

        });
    }

    @Override
    public MApiOperationBuilder createOperation(ApiMetadataContext context, ApiMetadataBuilder m, Route route) {
        MApiOperationBuilder op = route.getAction().getExtension(MApiOperationBuilder.class);
        if(null != op){
            op.setRoute(route);
            return op;
        }

        op = new MApiOperationBuilder(route);
        op.setName(route.getAction().getName());
        op.setCorsEnabled(route.isCorsEnabled());

        //Set http method
        setApiMethod(context, m, route, op);

        log.debug(" {}", op.getMethod());

        //Create security
        createApiSecurity(context, m, route, op);

        //Create parameters.
        createApiParameters(context, m, route, op);

        //Create responses.
        createApiResponses(context, m, route, op);

        return op;
    }

    protected ApiMetadataContext createContext(Api api, ApiMetadataBuilder md) {
		final MTypeContainer tf = createMTypeFactory(api.getConfig(), md);
		
		return new ApiMetadataContext() {

            @Override
            public MTypeContainer getMTypeContainer() {
                return tf;
            }

            @Override
            public Api getApi() {
                return api;
            }
        };
	}
	
	protected MTypeContainer createMTypeFactory(ApiConfig c, ApiMetadataBuilder md) {
		return mtypeManager.factory()
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

                    .setAlwaysReturnComplexTypeRef(true)

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

    protected void createResponses(ApiMetadataContext context, ApiMetadataBuilder m) {
        ApiConfig c = context.getConfig();

        c.getCommonResponses().forEach((name, r) -> {
            MType type = r.getType();
            if(type.isComplexType()) {
                MComplexType ct = type.asComplexType();

                if(!m.containsModel(ct.getName())) {
                    tryAddModel(context, m, ct);
                }

                type = ct.createTypeRef();
            }

            MApiResponseBuilder rb = new MApiResponseBuilder(r);
            rb.setType(type);
            m.putResponse(name, rb);
        });

    }

    protected void createPermissions(ApiMetadataContext context, ApiMetadataBuilder md) {
        context.getConfig().getPermissions().values().forEach(p -> md.addPermission(p));
    }

    protected void createSecurityDefs(ApiMetadataContext context, ApiMetadataBuilder md) {
        OAuthConfig oc = context.getConfig().getOAuthConfig();

        if(oc != null && oc.isEnabled()) {
            for(MApiSecurityDef sd : md.getSecurityDefs()) {
                if(sd.isOAuth2()) {
                    return;
                }
            }

            MOAuth2ApiSecurityDef def =
                    new MOAuth2ApiSecurityDef(
                            SwaggerConstants.OAUTH2,
                            SwaggerConstants.OAUTH2,
                            oc.getAuthorizationUrl(),
                            oc.getTokenUrl(),
                            oc.getFlow(),
                            null);
            
            md.addSecurityDef(def);
        }
    }
	
	protected ApiMetadata processMetadata(ApiMetadataContext context, ApiMetadataBuilder md) {
        preProcessDefault(context, md);
		for(ApiMetadataProcessor p : processors) {
			p.preProcess(context, md);
		}

        postProcessDefault(context, md);
        for(ApiMetadataProcessor p : processors) {
            p.postProcess(context, md);
        }

		ApiMetadata m = md.build();

		completeProcessDefault(context, m);
		for(ApiMetadataProcessor p : processors) {
			p.completeProcess(context, m);
		}
		
		return m;
	}

    protected void preProcessDefault(ApiMetadataContext context, ApiMetadataBuilder m) {
        //process paths.
        m.getPaths().forEach((k,p) -> preProcessPath(context, m, p));
    }

    protected void preProcessPath(ApiMetadataContext context, ApiMetadataBuilder m, MApiPathBuilder p) {
        ApiConfig c = context.getConfig();

        p.getOperations().forEach(op -> {
            //operation tag
            createOperationTags(context, m, op.getRoute(), p, op);

            //operation id
            String opId = op.getId();
            if (c.isUniqueOperationId()) {
                boolean isCreated = true;
                if (Strings.isEmpty(opId)) {
                    isCreated = strategy.tryCreateOperationId(c, m, p, op);
                } else if (m.getOperationIds().contains(opId.toLowerCase())) {
                    op.setId(null);
                    isCreated = strategy.tryCreateOperationId(c, m, p, op);
                } else {
                    m.getOperationIds().add(opId.toLowerCase());
                }
                if (!isCreated) {
                    log.warn("Invalid operation id in path: " + p.getPathTemplate() + ", please specify or check case");
                }
            } else if (Strings.isEmpty(opId)) {
                strategy.tryCreateOperationId(c, m, p, op);
            }
        });

    }

    protected void postProcessDefault(ApiMetadataContext context, ApiMetadataBuilder m) {
        String defaultMimeType = MimeTypes.APPLICATION_JSON;

        if(m.getConsumes().isEmpty()) {
            m.addConsume(defaultMimeType);
        }

        if(m.getProduces().isEmpty()) {
            m.addProduce(defaultMimeType);
        }

        //process models.
        m.getModels().forEach((name,model) -> postProcessModel(context, m, name, model));
    }

    protected void completeProcessDefault(ApiMetadataContext context, ApiMetadata m) {
        final ApiConfig c = context.getConfig();

        m.getPaths().forEach((k,path) -> {

            for(MApiOperation op : path.getOperations()) {
                Route route = op.getRoute();
                if(null != route) {
                    route.setExtension(MApiPath.class,      path);
                    route.setExtension(MApiOperation.class, op);
                }
            }

        });
    }

    protected void postProcessModel(ApiMetadataContext context, ApiMetadataBuilder m, String name, MApiModelBuilder model) {
        postProcessModelInheritance(context, m, name, model);
    }

    protected void postProcessModelInheritance(ApiMetadataContext context, ApiMetadataBuilder m, String name, MApiModelBuilder model) {
        if(null != model.getJavaTypes()) {
            for(Class<?> javaType : model.getJavaTypes()) {
                //already processed.
                if (null != model.getBaseName()) {
                    return;
                }

                Class<?> c = javaType.getSuperclass();
                if (Object.class.equals(c) || model.getJavaTypes().contains(c)) {
                    return;
                }

                MApiModelBuilder parent = m.tryGetModel(c);
                if (null != parent) {
                    //the parent's inheritance must be processed firstly.
                    postProcessModelInheritance(context, m, name, parent);

                    //process child.
                    model.setBaseName(parent.getName());

                    //removes the properties inherited from base model.
                    parent.getProperties().keySet().forEach((p) -> {
                        model.removeProperty(p);
                    });
                }
            }
        }
    }
	
	protected void createPaths(ApiMetadataContext context, ApiMetadataBuilder md) {
		for(ApiRoute ar : context.getConfig().getApiRoutes()) {
            if(!ar.isOperation()) {
                continue;
            }
			createApiPath(context, md, ar.getRoute());
		}
	}

    protected void createModels(ApiMetadataContext context, ApiMetadataBuilder m) {
        ApiConfig config = context.getConfig();

        config.getResourceTypes().values().forEach((t) -> {
            if(null == m.tryGetModel(t)) {
                //create model for resource type.
                context.getMTypeContainer().getMType(t);
            }
        });

        config.getModelConfigs().forEach((c)-> {
            if(!Strings.isEmpty(c.getClassName()) && null == m.tryGetModelByClassName(c.getClassName())) {
                context.getMTypeContainer().getMType(Classes.forName(c.getClassName()));
            }
        });

        config.getModels().forEach(model -> {
            m.addModel(model);
        });

        config.getComplexTypes().forEach(ct -> {
            tryAddModel(context, m, ct);
        });

        context.getMTypeContainer().getComplexTypes().forEach((type, ct) -> {
            tryAddModel(context, m, ct);
        });
    }

    @Override
    public MApiModelBuilder tryAddModel(ApiMetadataContext context, ApiMetadataBuilder md, MComplexType ct) {
        String name = modelName(context, ct);
        MApiModelBuilder model = md.tryGetModel(name);
        if(null != model) {
            if(null != ct.getJavaType() && !model.getJavaTypes().contains(ct.getJavaType())) {
                model.addJavaType(ct.getJavaType());
            }
            return null;
        }

        model = new MApiModelBuilder(ct, name);
        model.getProperties().values().forEach(p -> {
            if(p.getType().isComplexType()) {
                MComplexType complexType = (MComplexType)p.getType();
                if(null == md.tryGetModel(complexType.getName())) {
                    tryAddModel(context, md, ct);
                }
            }
        });

        md.addModel(model);
        return model;
    }

    protected String modelName(ApiMetadataContext context, MComplexType ct) {
        if(null != ct.getJavaType()) {
            ApiModel a = ct.getJavaType().getAnnotation(ApiModel.class);
            if(null != a) {
                String name = Strings.firstNotEmpty(a.name(),a.value());
                if(!Strings.isEmpty(name)) {
                    return name;
                }
            }

            ModelConfig mc = context.getConfig().getModelConfig(ct.getJavaType());
            if(null != mc && !Strings.isEmpty(mc.getName())) {
               return mc.getName();
            }
        }
        return ct.getName();
    }

    protected void createTags(ApiMetadataContext context, ApiMetadataBuilder md) {
	    context.getConfig().getTags().forEach(md::addTag);
    }

	protected void createApiPath(ApiMetadataContext context, ApiMetadataBuilder md, Route route) {
		PathTemplate pt = route.getPathTemplate();
		
        String pathTemplate = Strings.removeStart(pt.getTemplate(), md.getBasePath());
        if(!Strings.startsWith(pathTemplate,"/")){
            pathTemplate = "/"+pathTemplate;
        }
        
		MApiPathBuilder path = md.getPath(pathTemplate);
		if(null == path) {
			path = new MApiPathBuilder();
			path.setPathTemplate(pathTemplate);
			md.addPath(path);
		}

        log.debug("Path {} -> {} :", pt, route.getAction());
		path.addOperation(createOperation(context, md, route));
	}
	
	protected void setApiMethod(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiOperationBuilder op) {
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

    protected void createApiSecurity(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiOperationBuilder op){
        // todo create operation security

    }

    protected void createApiParameters(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiOperationBuilder op) {
		Action action = route.getAction();

        log.trace("  Parameters({})", action.getArguments().length);
		
		for(Argument a : action.getArguments()) {
            MApiParameterBuilder p = a.getExtension(MApiParameterBuilder.class);
            if(null != p) {
                op.addParameter(p);
                continue;
            }

            if(a.isWrapper()) {
                createApiWrapperParameter(context, m, route, op, a);
                if(!a.isRequestBody()) {
                    continue;
                }
            }

            if(!a.isContextual()) {
                createApiParameter(context, m, route, op, a);
            }
        }
	}

	protected void createApiWrapperParameter(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiOperationBuilder op, Argument a){
        for(Argument wa : a.getWrappedArguments()) {
            if(!wa.isContextual()) {
                createApiParameter(context, m, route, op, wa).setWrapperArgument(a);
            }
        }
    }

    protected MApiParameterBuilder createApiParameter(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiOperationBuilder op, Argument a) {
        MApiParameterBuilder p = new MApiParameterBuilder();

        p.setName(a.getName());
        p.setArgument(a);

        log.trace("{}", a.getName(), p.getLocation());

        if(isParameterFileType(a.getType())) {
            p.setType(MSimpleTypes.BINARY);
            p.setFile(true);
            op.addConsume(MimeTypes.MULTIPART_FORM_DATA);
        }else{
            p.setType(createMType(context, m, route.getAction().getControllerClass(), a.getType(), a.getGenericType()));
        }

        p.setLocation(getParameterLocation(context, route.getAction(), a, op, p));

        if (null != a.getRequired()) {
            p.setRequired(a.getRequired());
        } else if (p.getLocation() == MApiParameter.Location.PATH) {
            p.setRequired(true);
        }

        if(a.getType().isEnum()){
            p.setEnumValues(Enums.getValues(a.getType()));
        }

        op.addParameter(p);

        return p;
    }
	
	protected void createApiResponses(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiOperationBuilder op) {
        MApiResponseBuilder[] resps = route.getAction().getExtension(MApiResponseBuilder[].class);
        if(null != resps && resps.length > 0) {
            for(MApiResponseBuilder resp : resps) {
                op.addResponse(resp);
            }
            return;
        }


        Response[] annotations =
                route.getAction().getAnnotationsByType(Response.class);

        List<MApiResponseBuilder> responses = new ArrayList<>();
        if(annotations.length > 0) {
            for(Response a : annotations) {
                responses.add(createApiResponse(context, m, route, a));
            }
        }

        boolean hasSuccess = false;
        for(MApiResponseBuilder r : responses) {
            if(r.getStatus() >= 200 && r.getStatus() < 300) {
                hasSuccess = true;
            }
        }

        if(responses.isEmpty() || !hasSuccess) {
            Integer status = route.getSuccessStatus();
            if(null == status) {
                status = 200;
            }

            if(route.getAction().hasReturnValue()) {
                Class<?> returnType        = route.getAction().getReturnType();
                Type     genericReturnType = route.getAction().getGenericReturnType();

                MApiResponseBuilder resp = MApiResponseBuilder.success(status);

                resolveApiResponseType(context, m, route.getAction().getControllerClass(), returnType, genericReturnType, resp);

                op.addResponse(resp);
            }else{
                op.addResponse(MApiResponseBuilder.success(status));
            }
        }

        responses.forEach(op::addResponse);

        //todo : common responses ?
	}

    protected void createOperationTags(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiPathBuilder path, MApiOperationBuilder op) {
        if(null == route) {
            return;
        }

        MApiTag[] tags = route.getAction().removeExtension(MApiTag[].class);
        if(null != tags) {
            for(MApiTag tag : tags) {
                op.addTag(tag.getName());
                if(m.getTags().get(tag.getName()) == null) {
                    m.addTag(tag);
                }
            }
        }

        Class<?> resourceType = context.getConfig().getResourceTypes().get(route);
        if(null != resourceType) {
            MApiModelBuilder model = m.tryGetModel(resourceType);
            if(null != model) {
                op.addTag(model.getName());

                MApiTag tag = m.getTags().get(model.getName());
                if(null == tag) {
                    m.addTag(new MApiTag(model.getName(),model.getTitle(),model.getSummary(),model.getDescription(),null));
                }
            }else{
                op.addTag(resourceType.getSimpleName());
                m.tryAddTag(resourceType.getSimpleName());
            }
        }
    }

    protected MApiResponseBuilder createApiResponse(ApiMetadataContext context, ApiMetadataBuilder m, Route route, Response a) {
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

        resolveApiResponseType(context, m, route.getAction().getControllerClass(), returnType, genericType, resp);

        return resp;
    }

    protected void resolveApiResponseType(ApiMetadataContext context, ApiMetadataBuilder m, Class<?> declaringClass, Class<?> type, Type genericType, MApiResponseBuilder resp) {
        if(isResponseFileType(type)) {
            resp.setType(MSimpleTypes.BINARY);
            resp.setFile(true);
        }else{
            resp.setType(createMType(context, m, declaringClass, type, genericType));
        }
    }
	
	protected MType createMType(ApiMetadataContext context, ApiMetadataBuilder m, Class<?> declaringClass, Class<?> type, Type genericType) {
        return context.getMTypeContainer().getMType(declaringClass, type, genericType);
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
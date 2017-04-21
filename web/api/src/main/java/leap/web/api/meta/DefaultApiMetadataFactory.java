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
import leap.web.api.annotation.ApiModel;
import leap.web.api.annotation.Response;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.model.ModelConfig;
import leap.web.api.config.model.OAuthConfig;
import leap.web.api.meta.desc.ApiDescContainer;
import leap.web.api.meta.desc.CommonDescContainer;
import leap.web.api.meta.desc.ModelDesc;
import leap.web.api.meta.desc.OperationDescSet;
import leap.web.api.meta.model.*;
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
    protected @Inject ApiDescContainer       apiDescContainer;
	
	@Override
    public ApiMetadata createMetadata(ApiConfig c) {
		ApiMetadataBuilder md = new ApiMetadataBuilder();
		
		ApiMetadataContext context = createContext(c, md);
		
		setBaseInfo(context, md);

        createResponses(context, c, md);

        createPermissions(context, md);
		
		createSecurityDefs(context, md);
		
		createPaths(context, md);

        createModels(context, md);
		
		return processMetadata(context, md);
    }

    @Override
    public MApiOperationBuilder createOperation(ApiMetadataContext context, ApiMetadataBuilder m, Route route) {
        MApiOperationBuilder op = new MApiOperationBuilder(route);

        op.setName(route.getAction().getName());

        //Set http method
        setApiMethod(context, m, route, op);

        log.debug(" {}", op.getMethod());

        //Set description and summary
        setOperationDesc(context, m, route, op);

        //Create security
        createApiSecurity(context, m, route, op);

        //Create parameters.
        createApiParameters(context, m, route, op);

        //Create responses.
        createApiResponses(context, m, route, op);

        return op;
    }

    protected ApiMetadataContext createContext(ApiConfig c, ApiMetadataBuilder md) {
		final MTypeContainer tf = createMTypeFactory(c, md);
		
		return new ApiMetadataContext() {

            @Override
            public MTypeContainer getMTypeContainer() {
                return tf;
            }

            @Override
			public ApiConfig getConfig() {
				return c;
			}

            @Override
            public ApiDescContainer getDescContainer() {
                return apiDescContainer;
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

    protected void createResponses(ApiMetadataContext context, ApiConfig c, ApiMetadataBuilder m) {
        c.getCommonResponses().forEach((name, r) -> {
            MType type = r.getType();
            if(type.isComplexType()) {
                MComplexType ct = type.asComplexType();

                if(!m.containsModel(ct.getName())) {
                    m.addModel(createModel(context, m, ct));
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
        ApiConfig c = context.getConfig();
        OAuthConfig oauthConfig = c.getOAuthConfig();
        if(oauthConfig != null && oauthConfig.isOauthEnabled()) {
            MOAuth2ApiSecurityDef def =
                    new MOAuth2ApiSecurityDef(
                            SwaggerConstants.OAUTH2,
                            SwaggerConstants.OAUTH2,
                            oauthConfig.getOauthAuthzEndpointUrl(),
                            oauthConfig.getOauthTokenEndpointUrl(),
                            oauthConfig.getFlow(),
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

		ApiMetadata m = md.build();
		
		for(ApiMetadataProcessor p : processors) {
			p.postProcess(context, m);
		}
		
		return m;
	}

    protected void preProcessDefault(ApiMetadataContext context, ApiMetadataBuilder m) {
        //process paths.
        m.getPaths().forEach((k,p) -> preProcessPath(context, m, p));
    }

    protected void preProcessPath(ApiMetadataContext context, ApiMetadataBuilder m, MApiPathBuilder p) {

        p.getOperations().forEach(op -> {

            //operation tag
            createOperationTags(context, m, op.getRoute(), p, op);

            //operation id
            if(Strings.isEmpty(op.getId())) {
                strategy.tryCreateOperationId(context.getConfig(), m, p, op);
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

    protected void postProcessModel(ApiMetadataContext context, ApiMetadataBuilder m, String name, MApiModelBuilder model) {
        postProcessModelInheritance(context, m, name, model);
    }

    protected void postProcessModelInheritance(ApiMetadataContext context, ApiMetadataBuilder m, String name, MApiModelBuilder model) {
        if(null != model.getJavaType()) {

            //already processed.
            if(null != model.getBaseName()) {
                return;
            }

            Class<?> c = model.getJavaType().getSuperclass();
            if(null == c) {
                return;
            }

            MApiModelBuilder parent = m.tryGetModel(c);
            if(null != parent) {
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
	
	protected void createPaths(ApiMetadataContext context, ApiMetadataBuilder md) {
		for(Route route : context.getConfig().getRoutes()) {
			createApiPath(context, md, route);	
		}
	}

    protected void createModels(ApiMetadataContext context, ApiMetadataBuilder m) {

        context.getConfig().getResourceTypes().values().forEach((t) -> {
            if(null == m.tryGetModel(t)) {
                //create model for resource type.
                context.getMTypeContainer().getMType(t);
            }
        });

        context.getConfig().getModelTypes().forEach((t, c)-> {
            if(null == m.tryGetModel(t)) {
                context.getMTypeContainer().getMType(t);
            }
        });

        context.getMTypeContainer().getComplexTypes().forEach((type, ct) -> {
            m.addModel(createModel(context, m, ct));
        });

    }

    protected MApiModelBuilder createModel(ApiMetadataContext context, ApiMetadataBuilder m, MComplexType ct) {

        MApiModelBuilder model = new MApiModelBuilder(ct);

        if(null != model.getJavaType()) {
            ApiModel a = model.getJavaType().getAnnotation(ApiModel.class);
            if(null != a) {
                //name
                String name = Strings.firstNotEmpty(a.name(),a.value());
                if(!Strings.isEmpty(name)) {
                    model.setName(name);
                }
            }

            ModelConfig c = context.getConfig().getModelTypes().get(model.getJavaType());
            if(null != c) {
                if(!Strings.isEmpty(c.getName())) {
                    model.setName(c.getName());
                }
            }
        }


        if(null != apiDescContainer) {
            ModelDesc mdesc = apiDescContainer.getModelDesc(ct.getJavaType());
            if(null != mdesc) {
                model.getProperties().forEach((n, p) -> {
                    ModelDesc.PropertyDesc pdesc = mdesc.getPropertyDesc(p.getName());
                    if(null != pdesc && !Strings.isEmpty(pdesc.getDesc())) {
                        p.setDescription(pdesc.getDesc());
                    }
                });
            }
        }

        return model;
    }

	protected void createApiPath(ApiMetadataContext context, ApiMetadataBuilder md, Route route) {
		PathTemplate pt = route.getPathTemplate();

		MApiPathBuilder path = md.getPath(pt.getTemplate());
		if(null == path) {
			path = new MApiPathBuilder();
			path.setPathTemplate(pt.getTemplate());
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

	protected void setOperationDesc(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiOperationBuilder op){
        if(op.getRoute().getAction() != null && op.getRoute().getAction().hasController()){
            OperationDescSet set = apiDescContainer.getOperationDescSet(route.getAction().getController());
            if(set == null){
                return;
            }
            OperationDescSet.OperationDesc desc = set.getOperationDesc(op.getRoute().getAction());
            if(desc != null){
                op.setDesc(desc);
            }
        }
    }

    protected void createApiSecurity(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiOperationBuilder op){
        // todo create operation security

    }

    protected void createApiParameters(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiOperationBuilder op) {
		Action action = route.getAction();

        log.trace("  Parameters({})", action.getArguments().length);
		
		for(Argument a : action.getArguments()) {
            if(a.isWrapper()) {
                createApiWrapperParameter(context, m, route, op, a);
                if(!a.isRequestBody()) {
                    continue;
                }
            }

            if(!a.isContextual()) {
                String description = null;
                if(op.getDesc() != null){
                    OperationDescSet.ParameterDesc desc = op.getDesc().getParameter(a);
                    if(desc != null){
                        description = desc.getDescription();
                    }
                }
                createApiParameter(context, m, route, op, a,description);
            }
        }
	}

	protected void createApiWrapperParameter(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiOperationBuilder op, Argument a){
        OperationDescSet.ParameterDesc desc = null;
        if(op.getDesc() != null){
            desc = op.getDesc().getParameter(a);
        }

        CommonDescContainer.Parameter parameter = apiDescContainer.getCommonParameter(a.getType());

        for(Argument wa : a.getWrappedArguments()) {
            if(!wa.isContextual()) {
                String description = null;
                if(desc != null){
                    OperationDescSet.PropertyDesc propertyDesc = desc.getProperty(wa.getDeclaredName());
                    if(propertyDesc != null){
                        description = propertyDesc.getDesc();
                    }
                }else if(parameter != null){
                    CommonDescContainer.Property property = parameter.getProperty(wa.getDeclaredName());
                    if(property != null){
                        description = property.getDesc();
                    }
                }
                createApiParameter(context, m, route, op, wa,description);
            }
        }
    }

    protected void createApiParameter(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiOperationBuilder op, Argument a, String desc) {
        MApiParameterBuilder p = new MApiParameterBuilder();

        p.setName(a.getName());

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

        p.setDescription(desc);

        op.addParameter(p);
    }
	
	protected void createApiResponses(ApiMetadataContext context, ApiMetadataBuilder m, Route route, MApiOperationBuilder op) {
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
                MApiTag tag = m.getTags().get(resourceType.getSimpleName());
                if(null == tag) {
                    m.addTag(new MApiTag(resourceType.getSimpleName()));
                }
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
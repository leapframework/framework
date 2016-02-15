/*
 * Copyright 2014 the original author or authors.
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
package leap.web.action;

import leap.core.AppConfigException;
import leap.core.annotation.Inject;
import leap.core.validation.Validation;
import leap.core.validation.ValidationException;
import leap.core.validation.ValidationManager;
import leap.lang.*;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.intercepting.Execution;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.*;
import leap.web.Result;
import leap.web.action.Argument.Location;
import leap.web.annotation.RequestBean;
import leap.web.annotation.Consumes;
import leap.web.annotation.RequestBody;
import leap.web.config.WebInterceptors;
import leap.web.exception.ResponseException;
import leap.web.exception.ValidateFailureException;
import leap.web.format.FormatManager;
import leap.web.format.FormatNotAcceptableException;
import leap.web.format.FormatNotFoundException;
import leap.web.format.RequestFormat;
import leap.web.multipart.MultipartFile;
import leap.web.route.Route;
import leap.web.route.RouteBuilder;
import leap.web.view.ViewData;

import javax.servlet.http.Part;
import java.util.ArrayList;
import java.util.List;

public class DefaultActionManager implements ActionManager,AppListener {
	
	private static final Log log = LogFactory.get(DefaultActionManager.class);
	
	protected static final ResultProcessor NOP_RESULT_PROCESSOR = new AbstractResultProcessor() {
		@Override
		public void processReturnValue(ActionContext context, Object returnValue, Result result) throws Throwable {
		}
	};
	
	private List<ExecutionAttributes> easList = new ArrayList<>();

	protected @Inject App                        app;
    protected @Inject ValidationManager          validationManager;
	protected @Inject RequestFormat[]            requestFormats;
	protected @Inject FormatManager              formatManager;
	protected @Inject ResultProcessorProvider[]  resultProcessorProviders;
	protected @Inject ArgumentResolverProvider[] argumentResolverProviders;
	protected @Inject ActionInitializable[]      actionInitializables;
	protected @Inject WebInterceptors            interceptors;

	@Override
    public void postAppStart(App app) throws Throwable {
		ActionInterceptor[] actionInterceptors = interceptors.getActionInterceptors().toArray(new ActionInterceptor[]{});

		for(ExecutionAttributes eas : easList) {
			eas.interceptors = new ActionInterceptors(Arrays2.concat(actionInterceptors, eas.action.getInterceptors()));
		}
		
		easList.clear();
		easList = null;
    }

	@Override
    public void prepareAction(RouteBuilder route) {
		if(null == easList) {
			throw new IllegalStateException("Cannot create action when app started");
		}
		
		for(ActionInitializable init : actionInitializables){
			init.postActionInit(route,route.getAction());
		}
		
		//set execution attributes
		ExecutionAttributes eas = new ExecutionAttributes();
    	route.setExecutionAttributes(eas);

		//resolve request body argument
    	RequestBodyArgumentInfo rbaf = resolveRequestBodyArgument(route, eas);
		
		//prepare argument resolvers
		eas.executionArguments = new ExecutionArgument[route.getAction().getArguments().length];
    	for(int i=0;i<eas.executionArguments.length;i++){
    		ExecutionArgument ea = new ExecutionArgument();
    		eas.executionArguments[i] = ea;
    		
    		prepareArgument(route, route.getAction().getArguments()[i], ea, rbaf);
    	}
		
		//prepare result processor
    	eas.resultProcessor = getResultProcessor(route);
    	
    	//prepare formats
    	eas.annotatedFormats = getAnnotatedFormats(route);
    	eas.supportedFormats = getSupportedFormats(route);
    	eas.action           = route.getAction();
    	
    	easList.add(eas);
    }
	
	@Override
    public Object executeAction(ActionContext context, Validation validation) throws Throwable {
		ExecutionAttributes eas = (ExecutionAttributes)context.getRoute().getExecutionAttributes();
		DefaultActionExecution execution = new DefaultActionExecution(validation);
		
		try {
            //resolve request format
            RequestFormat requestFormat = resolveRequestFormat(context, eas);

            //pre-execute action interceptors
            if (State.isIntercepted(eas.interceptors.preExecuteAction(context, validation))) {
                execution.setStatus(Execution.Status.INTERCEPTED);
                return null;
            }

            //resolve argument values
            Object[] args = resolveArgumentValues(context, validation, requestFormat, eas);
            execution.setArgs(args);

            //expose arguments as view data
            exposeArgumentsAsViewData(context.getResult().getViewData(), context, eas, execution);

            //if validate errors, do not continue to execute the action.
            if (validation.hasErrors() && !context.isAcceptValidationError()) {
                execution.setStatus(Execution.Status.FAILURE);
                if (State.isContinue(eas.interceptors.onActionFailure(context, validation, execution))) {
                    throw new ValidationException(validation.errors());
                } else {
                    log.info("Action validation error handled by interceptors");
                    return null;
                }
            }

            //execute action
            Action action = context.getAction();
            Object value = action.execute(context, args);

            execution.setReturnValue(value);
            execution.setStatus(Execution.Status.SUCCESS);

            //post-execute action interceptors
            eas.interceptors.postExecuteAction(context, validation, execution);
            return execution.getReturnValue();

        }catch(ResponseException e) {
            log.debug("Caught a ResponseException while executing action, just throw it!");
            throw e;
		}catch(RequestIntercepted e) {
            log.debug("Caught a RequestIntercepted while executing action, just throw it!");
			throw e;
		}catch(Throwable e){
            log.warn("Error executing action {}, {}", context.getAction(), e.getMessage(), e);
			execution.setStatus(Execution.Status.FAILURE);
			execution.setException(e);
			
			if(!State.isIntercepted(eas.interceptors.onActionFailure(context, validation, execution))) {
				FailureHandler[] failureHandlers = context.getRoute().getFailureHandlers();
				if(failureHandlers.length > 0) {
					for(FailureHandler h : failureHandlers) {
						if(h.handleFailure(context, execution, context.getResult())) {
							log.info("Action handled by failure handler");
							return null;
						}
					}
				}

                //TODO: special validation exception.
                if(e instanceof ValidationException) {
                    throw new ValidateFailureException(validation);
                }

				throw e;
			}else{
				log.info("Action error handled by interceptors",e);
				return null;
			}
		}finally {
			eas.interceptors.completeExecuteAction(context, validation, execution);
		}
    }
	
	@Override
    public void processResult(ActionContext context, Validation validation, Object returnValue, Result result) throws Throwable {
		ExecutionAttributes eas = (ExecutionAttributes)context.getRoute().getExecutionAttributes();
		
		if(validation.hasErrors() && !context.isAcceptValidationError()){
			eas.resultProcessor.processValidationErrors(context, validation, result);			
		}else{
			eas.resultProcessor.processReturnValue(context, returnValue, result);	
		}
	}
	
	protected void exposeArgumentsAsViewData(ViewData vm, ActionContext context,ExecutionAttributes eas, DefaultActionExecution execution) {
		Argument[] arguments = context.getAction().getArguments();
		
		if(arguments.length > 0){
			
			ExecutionArgument[] executionArguments = eas.executionArguments;
			Object[] values = execution.getArgs();
			
			for(int i=0;i<arguments.length;i++){
				if(executionArguments[i].isContextual){
					continue;
				}
				String name = arguments[i].getViewAttributeName();
				if(!vm.containsKey(name)){
					vm.put(name, values[i]);
				}
			}
		}
	}
	
	protected RequestBodyArgumentInfo resolveRequestBodyArgument(RouteBuilder route, ExecutionAttributes eas){
        return resolveRequestBodyArgument(route, route.getAction().getArguments());
	}

    protected RequestBodyArgumentInfo resolveRequestBodyArgument(RouteBuilder route, Argument[] arguments){
        RequestBodyArgumentInfo rbaf = new RequestBodyArgumentInfo();

        //found declared request body argument
        int count=0;
        for(Argument a : arguments){
            if(a.getLocation() == Location.REQUEST_BODY){
                rbaf.argument   = a;
                rbaf.annotation = Classes.getAnnotation(a.getAnnotations(), RequestBody.class);
                rbaf.declared   = true;
                count++;
                continue;
            }

            if(a.isAnnotationPresent(RequestBody.class)) {
                RequestBody rb = Classes.getAnnotation(a.getAnnotations(), RequestBody.class);
                if(!rb.value()) {
                    continue;
                }

                rbaf.argument   = a;
                rbaf.annotation = rb;
                rbaf.declared   = true;
                count++;
                continue;
            }

            if(a.getType().isAnnotationPresent(RequestBody.class)) {
                RequestBody rb = a.getType().getAnnotation(RequestBody.class);
                if(!rb.value()) {
                    continue;
                }

                rbaf.argument   = a;
                rbaf.annotation = rb;
                rbaf.declared   = true;
                count++;
                continue;
            }
        }
        if(count > 1){
            throw new AppConfigException("Only one request body argument allowed, check the action : " + route.getAction());
        }

        if(null != rbaf.argument){
            return rbaf;
        }

        //Finds the candidate request body arguments.
        List<Argument> candidates = new ArrayList<>();
        for(Argument a : arguments){
            if(ContextArgumentResolver.isContext(a.getType())){
                continue;
            }

            TypeInfo ti = a.getTypeInfo();
            if(ti.isComplexType() || ti.isComplexElementType()){
                candidates.add(a);
            }
        }

        if(candidates.size() == 1) {
            rbaf.argument = candidates.get(0);
        }

        return rbaf;
    }
	
    protected Object[] resolveArgumentValues(ActionContext context, Validation validation, RequestFormat format, ExecutionAttributes eas) throws Throwable {
		Action action = context.getAction();
		
		Object[] args;
		
		Argument[] 		    arguments 		   = action.getArguments();
		ExecutionArgument[] executionArguments = eas.executionArguments;
		
		if(arguments.length > 0){
			args = new Object[arguments.length];
			
			for(int i=0;i<args.length;i++){
				if(log.isTraceEnabled()){
					log.trace("Resolving value of argument '{}'",arguments[i].getName());
				}
				Argument         argument = arguments[i];
				ArgumentResolver resolver = executionArguments[i].resolver;
				
				Object value = resolver.resolveValue(context, argument);
				
				ArgumentValidator[] validators = argument.getValidators();
				for(int j=0;j<validators.length;j++){
					ArgumentValidator v = validators[j];
                    if(!v.validate(validation, argument, value)) {
                        break;
                    }
				}
				
				args[i] = value;
			}
		}else{
			args = Arrays2.EMPTY_OBJECT_ARRAY;
		}
		
		return args;
    }
	
    protected ResultProcessor getResultProcessor(RouteBuilder route) {
		Args.notNull(route,"route");
		Args.notNull(route.getAction(),"action");
		
		Action action = route.getAction();
		
		//Get external processor.
		ResultProcessor processor;
		for(ResultProcessorProvider provider : resultProcessorProviders){
			if((processor = provider.tryGetResultProcessor(action)) != null){
				return processor;
			}
		}
		
		//Get internal processor.
		if(action.hasReturnValue()){
			Class<?> returnType = action.getReturnType();
			
			//The return type is Result
			if(Result.class.isAssignableFrom(returnType)){
				return ResultResultProcessor.INSTANCE;
			}
			
			//The return type is Renderable.
			if(Renderable.class.isAssignableFrom(returnType)){
				return RenderableResultProcessor.INSTANCE;
			}
			
			//The return type is String
			if(String.class.equals(returnType)){
				return new StringResultProcessor(app, route);
			}
			
			//Render the return value by response formats
			return new FormattingResultProcessor(app,route);
		}
		
		//No return value, render view directly
		if(!Strings.isEmpty(route.getDefaultViewName())){
			return new ViewResultProcessor(route.getDefaultViewName(), route.getDefaultView());
		}
		
		//No result processor
		return NOP_RESULT_PROCESSOR;
    }

    protected void prepareArgument(RouteBuilder route, Argument argument, ExecutionArgument ea, RequestBodyArgumentInfo rbaf) {
        ArgumentResolver resolver = null;

        boolean args = Classes.isAnnotatioinPresent(argument.getAnnotations(), RequestBean.class) ||
                       argument.getType().isAnnotationPresent(RequestBean.class);

        if(args && !argument.getTypeInfo().isComplexType()) {
            throw new IllegalStateException("Only Complex Type can be 'RequestBean', check arg : " + argument);
        }

        resolver = args ? createRequestBeanArgumentResolver(route, argument, rbaf) : resolveArgumentResolver(route, argument, rbaf);

        ea.resolver     = resolver;
        ea.isContextual = resolver instanceof ContextArgumentResolver;
    }

    protected ArgumentResolver resolveArgumentResolver(RouteBuilder route, Argument argument, RequestBodyArgumentInfo rbaf) {
        //Get context resolver
        if(ContextArgumentResolver.isContext(argument.getType())){
            return ContextArgumentResolver.of(argument.getType());
        }

        ArgumentResolver resolver = null;

        //Get external resolver
        for(ArgumentResolverProvider provider : argumentResolverProviders){
            if((resolver = provider.tryGetArgumentResolver(route, route.getAction(), argument)) != null){
                break;
            }
        }

        if(null == resolver){
            TypeInfo typeInfo = argument.getTypeInfo();
            if(typeInfo.isCollectionType()){
                //Collection type resolver
                resolver = new CollectionArgumentResolver(app, route, argument);
            }else if(typeInfo.isSimpleType()){
                //Simple type resolver
                resolver = new SimpleArgumentResolver(app, route, argument);
            }else{
                if(argument.getType().equals(Part.class) || argument.getType().equals(MultipartFile.class)) {
                    //Part resolver
                    resolver = new MultipartArgumentResolver(app, route, argument);
                }else{
                    //Complex type resolver
                    resolver = new ComplexArgumentResolver(app, route, argument);
                }
            }
        }

        //Get request body resolver
        if(argument == rbaf.argument){
            resolver = new RequestBodyArgumentResolver(app, route.getAction(), argument, rbaf.annotation, rbaf.declared, resolver);
        }

        return resolver;
    }

    protected RequestBeanArgumentResolver createRequestBeanArgumentResolver(RouteBuilder route, Argument argument, RequestBodyArgumentInfo rbaf) {
        BeanType bt = BeanType.of(argument.getType());

        boolean requestBody = rbaf.isDeclaredRequestBody(argument);

        List<RequestBeanArgumentResolver.BeanArgument> bas = new ArrayList<>();

        for(BeanProperty bp : bt.getProperties()) {
            RequestBeanArgumentResolver.BeanArgument ba = new RequestBeanArgumentResolver.BeanArgument();
            ba.property = bp;
            ba.argument = new ArgumentBuilder(validationManager, bp).build();

            bas.add(ba);
        }

        Argument[] nestedArguments = bas.stream().map(ba -> ba.argument).toArray(Argument[]::new);
        RequestBodyArgumentInfo nestedRbaf = resolveRequestBodyArgument(route, nestedArguments);
        if(!nestedRbaf.declared && requestBody) {
            nestedRbaf = new RequestBodyArgumentInfo();
        }

        for(RequestBeanArgumentResolver.BeanArgument ba : bas) {
            ba.resolver = resolveArgumentResolver(route, ba.argument, nestedRbaf);
        }

        return new RequestBeanArgumentResolver(app,
                                         argument,
                                         requestBody,
                                         bt,
                                         bas.toArray(new RequestBeanArgumentResolver.BeanArgument[]{}));
    }
    
	protected RequestFormat resolveRequestFormat(ActionContext context,ExecutionAttributes eas) throws Throwable {
		Action  action  = context.getAction();
		Request request = context.getRequest();
		
		RequestFormat fmt = context.getRequestFormat();
		
		if(null == fmt){ 
			fmt = formatManager.resolveRequestFormat(request);
		}
		
		if(null != fmt && null != eas.annotatedFormats){
			if(!Arrays2.contains(eas.annotatedFormats, fmt)){
				throw new FormatNotAcceptableException("The request format '" + fmt.getName() + "' not acceptable by action '" + action + "'");
			}
		}
		
		if(null == fmt){
			fmt = selectRequstFormat(context,eas);
		}
		
		if(null != fmt){
			context.setRequestFormat(fmt);
		}
		
		return fmt;
	}
	
	protected RequestFormat selectRequstFormat(ActionContext context,ExecutionAttributes eas) throws Throwable {
		if(null != eas.annotatedFormats){
			RequestFormat fmt = formatManager.selectRequestFormat(context.getRequest(), eas.annotatedFormats);
			if(null == fmt){
				fmt = eas.annotatedFormats[0];
			}
			return fmt;
		}else{
			return formatManager.selectRequestFormat(context.getRequest(), eas.supportedFormats);
		}
	}
	
	protected RequestFormat[] getAnnotatedFormats(RouteBuilder route) {
		Action action = route.getAction();
		Consumes consumes = action.searchAnnotation(Consumes.class);
		if(null == consumes){
			return null;
		}
		RequestFormat[] formats = new RequestFormat[consumes.value().length];
		for(int i=0;i<formats.length;i++){
			String name = consumes.value()[i];
			formats[i] = formatManager.tryGetRequestFormat(name);
			if(null == formats[i]){
				throw new FormatNotFoundException("The format '" + name + "' not found, check the action '" + action + "'");
			}
		}
		return formats;
	}
	
	protected RequestFormat[] getSupportedFormats(RouteBuilder route) {
		Action action = route.getAction(); 
				
		List<RequestFormat> supportedFormats = new ArrayList<>();
		
		for(RequestFormat fmt : requestFormats){
			if(fmt.supports(action)){
				supportedFormats.add(fmt);
			}
		}
		
		return supportedFormats.toArray(new RequestFormat[]{});
	}
	
	public static final class ExecutionAttributes {
		static ExecutionAttributes EMPTY = new ExecutionAttributes();
		
		static {
			EMPTY.interceptors       = new ActionInterceptors(Action.EMPTY_INTERCEPTORS);
			EMPTY.executionArguments = new ExecutionArgument[]{};
			EMPTY.annotatedFormats   = new RequestFormat[]{};
			EMPTY.supportedFormats   = EMPTY.annotatedFormats;
		}
		
		static ExecutionAttributes get(Route route) {
			ExecutionAttributes o = (ExecutionAttributes)route.getExecutionAttributes();
			return null == o ? EMPTY : o;
		}
		
		public Action			   action;
    	public ActionInterceptors  interceptors;
    	public ResultProcessor     resultProcessor;
    	public ExecutionArgument[] executionArguments;
		public RequestFormat[] 	   annotatedFormats;
		public RequestFormat[] 	   supportedFormats;
    }
	
	public static final class ExecutionArgument {
		public ArgumentResolver resolver;
		public boolean          isContextual;
		//public boolean          isRequestBody;
	}
    
    protected final class RequestBodyArgumentInfo {
    	public Argument    argument;
    	public RequestBody annotation;
    	public boolean	   declared;

        boolean isDeclaredRequestBody(Argument a) {
            return declared && argument == a;
        }

        boolean isCandidateRequestBody(Argument a) {
            return !declared && argument == a;
        }
    }
}
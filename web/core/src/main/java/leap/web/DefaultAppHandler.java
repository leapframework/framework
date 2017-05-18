/*
 * Copyright 2013 the original author or authors.
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
package leap.web;

import leap.core.AppException;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.validation.SimpleErrors;
import leap.core.validation.Validation;
import leap.core.validation.ValidationManager;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.intercepting.State;
import leap.lang.time.StopWatch;
import leap.web.action.ActionContext;
import leap.web.action.ActionManager;
import leap.web.action.DefaultActionContext;
import leap.web.assets.AssetSource;
import leap.web.config.WebConfig;
import leap.web.cors.CorsHandler;
import leap.web.debug.DebugDetector;
import leap.web.error.ErrorInfo;
import leap.web.exception.BadRequestException;
import leap.web.exception.ResponseException;
import leap.web.format.FormatManager;
import leap.web.format.ResponseFormat;
import leap.web.locale.LocaleResolver;
import leap.web.multipart.MultipartContext;
import leap.web.route.Route;
import leap.web.theme.Theme;
import leap.web.theme.ThemeManager;
import leap.web.view.LinkedViewData;
import leap.web.view.View;
import leap.web.view.ViewSource;
import leap.web.view.WrappedViewData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static leap.web.cors.CorsHandler.REQUEST_HEADER_ACCESS_CONTROL_REQUEST_METHOD;

public class DefaultAppHandler extends AppHandlerBase implements AppHandler {

    protected @Inject @M RequestHandlerMapping[] handlerMappings;
    protected @Inject @M RequestInterceptors interceptors;
    protected @Inject @M ActionManager actionManager;
    protected @Inject @M ValidationManager validationManager;
    protected @Inject @M DebugDetector debugDetector;
    protected @Inject @M ThemeManager themeManager;
    protected @Inject @M FormatManager formatManager;
    protected @Inject @M ViewSource viewSource;
    protected @Inject @M AssetSource assetSource;
    protected @Inject @M WebConfig webConfig;

    protected LocaleResolver localeResolver;
    protected int maxExecutionCount = 10;

    @Override
    public void setApp(App app) {
        super.setApp(app);
        this.interceptors = new RequestInterceptors(app.interceptors().getRequestInterceptors());
    }

    @Override
    public void prepareRequest(final Request request, final Response response) throws Throwable {
        //debug
        debugDetector.detectDebugStatus(request);

        //set locale
        if (null != localeResolver) {
            request.setLocale(localeResolver.resolveLocale(request));
        }

        if (null == request.getLocale()) {
            request.setLocale(app.getDefaultLocale());
        }

        //validation
        request.setValidation(validationManager.createValidation(new SimpleErrors() {
            @Override
            public Locale getLocale() {
                if (null == locale) {
                    return request.getLocale();
                } else {
                    return locale;
                }
            }
        }));

        //format manager
        request.setFormatManager(formatManager);

        //theme
        Theme theme = themeManager.resolveTheme(request);
        if (null != theme) {
            request.setThemeName(theme.getName());

            if (null != theme.getMessageSource()) {
                request.setMessageSource(theme.getMessageSource());
            }

            if (null != theme.getAssetSource()) {
                request.setAssetSource(theme.getAssetSource());
            }

            if (null != theme.getViewSource()) {
                request.setViewSource(theme.getViewSource());
            }
        }

        //message source
        if (null == request.getMessageSource()) {
            request.setMessageSource(app.getMessageSource());
        }

        //view source
        if (null == request.getViewSource()) {
            request.setViewSource(viewSource);
        }

        //asset source
        if (null == request.getAssetSource()) {
            request.setAssetSource(assetSource);
        }
    }

    @Override
    public boolean handleRequest(Request request, Response response) throws ServletException, IOException {
        if (_trace) {
            log.trace("Received request : {}", request.getPath());
        }

        StopWatch sw = StopWatch.startNew();
        DefaultRequestExecution execution = new DefaultRequestExecution();
        try {
            boolean handled = false;
            try {
                //handle by interceptors
                handled = State.isIntercepted(interceptors.preHandleRequest(request, response));

                //handle by handlers
                if (!handled) {
                    handled = handleByHandlers(request, response);
                }

                //routing to action
                if (!handled) {
                    Router router = request.getExternalRouter();
                    if (null == router) {
                        router = new SimpleRouter(app.routes(), null);
                    }

                    DefaultActionContext ac = newActionContext(request, response);

                    //resolve action path
                    String path = resolveActionPath(request, response, router, ac);

                    if (_debug) {
                        log.debug("Routing path '{}'", ac.getPath());
                    }

                    if (handleCorePreflightRequest(request, response, router, ac)) {
                        handled = true;
                    } else {
                        int routeState = routeAndExecuteAction(request, response, router, ac);

                        if (routeState == ROUTE_STATE_HANLDED) {
                            handled = true;
                        } else if (routeState == ROUTE_STATE_NOT_HANDLED) {

                            if (webConfig.isCorsEnabled() &&
                                    webConfig.getCorsHandler().preHandle(request, response).isIntercepted()) {

                                log.debug("request (no route) handled by cors handler");

                                handled = true;

                            } else if (State.isIntercepted(interceptors.handleNoRoute(request, response))) {

                                log.debug("request (no route) handled by interceptor");

                                handled = true;
                            } else {
                                handled = handleNoAction(request, response, router, path);
                            }
                        } else {
                            return false;
                        }
                    }
                }

                if (!handled && _debug) {
                    log.debug("Request '{}' not handled", request.getPath());
                }
            } catch (RequestIntercepted e) {
                log.debug("Caught a RequestIntercepted Exception, finish handling request.", e);
                handled = true;
            } catch (ResponseException e) {
                handled = true;
                renderResponseException(request, response, e);
            }

            if (handled) {
                execution.success();
                interceptors.postHandleRequest(request, response, execution);
            } else {
                execution.failure();
                interceptors.onRequestFailure(request, response, execution);
            }
            return handled;
        } catch (Throwable e) {
            if (_debug) {
                log.error("Error handling request '{}', {}", request.getPath(), e.getMessage());
            }

            try {
                execution.failure(e);
                if (State.isIntercepted(interceptors.onRequestFailure(request, response, execution))) {
                    return true;
                }

                if (handleError(request, response, e)) {
                    return true;
                }
            } catch (Throwable e1) {
                if (_debug) {
                    log.error("Error executing 'handleError' in interceptors, {}", e1.getMessage(), e);
                }
                throwException(e1);
            }

            throwException(e);
            return true;
        } finally {
            if (_debug) {
                log.debug("Request '" + request.getPath() + "' executed {}ms", sw.getElapsedMilliseconds());
            }
            try {
                interceptors.completeHandleRequest(request, response, execution);
            } catch (Throwable e) {
                if (_debug) {
                    log.error("Error executing 'completeHandle' in interceptors, {}", e.getMessage(), e);
                }
                throwException(e);
            }
        }
    }

    protected void throwException(Throwable e) throws ServletException, IOException {
        if (e instanceof ServletException) {
            throw (ServletException) e;
        }
        if (e instanceof IOException) {
            throw (IOException) e;
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        throw new AppException(e.getMessage(), e);
    }

    protected boolean handleByHandlers(Request request, Response response) throws Throwable {
        for (int i = 0; i < handlerMappings.length; i++) {
            RequestHandlerMapping mapping = handlerMappings[i];
            if (mapping.matches(request)) {
                if (_debug) {
                    log.debug("Handling request '{}' by handler '{}'...", request.getPath(),
                            mapping.getRequestHandler().getClass().getName());
                }
                if (mapping.getRequestHandler().handle(request, response)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected DefaultActionContext newActionContext(Request request, Response response) {
        return new DefaultActionContext(request, response);
    }

    protected String resolveActionPath(Request request, Response response, Router routeInfo,
                                       DefaultActionContext ac) throws Exception {
        String path = null;

        if (null == path) {
            if (request.hasPathExtension()) {
                if (webConfig.isActionExtensionEnabled() && webConfig.getActionExtensions().contains(
                        request.getPathExtension())) {
                    path = request.getServicePathWithoutExtension();
                } else if (webConfig.isFormatExtensionEnabled()) {
                    ResponseFormat fmt = request.getFormatManager().tryGetResponseFormat(request.getPathExtension());
                    if (null != fmt) {
                        ac.setResponseFormat(fmt);
                    }
                }
            }

            if (null == path) {
                path = request.getServicePath();
            }
        }

        path = "".equals(path) ? "/" : path;

        ac.setPath(path);

        return path;
    }

    //0 : not handled , 1: handled 2 : end
    private static final int ROUTE_STATE_NOT_HANDLED = 0;
    private static final int ROUTE_STATE_HANLDED = 1;
    private static final int ROUTE_STATE_END = 2;

    protected boolean handleCorePreflightRequest(Request request,
                                                 Response response,
                                                 Router router,
                                                 DefaultActionContext ac) throws Throwable {

        CorsHandler handler = webConfig.getCorsHandler();
        if (!handler.isPreflightRequest(request)) {
            return false;
        }

        if (!Strings.isEmpty(ac.getPath())) {
            Enumeration<String> methods = request.getServletRequest().getHeaders(REQUEST_HEADER_ACCESS_CONTROL_REQUEST_METHOD);
            Route route = null;
            while (methods.hasMoreElements()){
                String method = methods.nextElement();
                route = router.match(method, ac.getPath(), request.getParameters(), New.hashMap());
                if(null != route){
                    break;
                }
            }
            if(null == route){
                route = router.match(null, ac.getPath(), request.getParameters(), New.hashMap());
            }
            if (null == route) {
                return false;
            }

            handler.preHandle(request, response);
            return true;
        }

        return false;
    }

    protected int routeAndExecuteAction(Request request,
                                        Response response,
                                        Router router,
                                        DefaultActionContext ac) throws Throwable {

        if (!Strings.isEmpty(ac.getPath())) {
            Map<String, String> pathVariables = new LinkedHashMap<>();

            Route route = router.match(request.getMethod(),
                    ac.getPath(),
                    request.getParameters(),
                    pathVariables);

            if (null == route && request.hasPathExtension() && null != ac.getResponseFormat()) {
                route = router.match(request.getMethod(),
                        request.getServicePathWithoutExtension(),
                        request.getParameters(),
                        pathVariables);

                if (null != route) {
                    ac.setPath(request.getServicePathWithoutExtension());
                }
            }

            if (null != route) {

                if (_debug) {
                    log.debug("Handling request '{}' by action '{}'...", request.getPath(), route.getAction());
                }

                //Check https only.
                if (route.isHttpsOnly() && !request.isSecure()) {
                    //TODO : https only exception.
                    throw new BadRequestException("The request must be https");
                }

                ac.setRoute(route);
                ac.setPathParameters(pathVariables);

                if (route.supportsMultipart() && request.isMultipart()) {
                    log.debug("Found multipart request and action");
                    MultipartContext.setMultipartAction(request.getServletRequest(), ac);
                    return ROUTE_STATE_END;
                }

                //handle cors request.
                if (route.isCorsEnabled() || (webConfig.isCorsEnabled() && !route.isCorsDisabled())) {
                    if (webConfig.getCorsHandler().preHandle(request, response).isIntercepted()) {
                        log.debug("Request was intercepted by cors handler");
                        return ROUTE_STATE_HANLDED;
                    }
                }

                if (State.isIntercepted(interceptors.handleRoute(request, response, ac.getRoute(), ac))) {
                    return ROUTE_STATE_HANLDED;
                }

                Result result = new Result();
                request.setResult(result);

                executeAndRenderAction(request, response, ac, result);

                return ROUTE_STATE_HANLDED;
            }
        }

        return ROUTE_STATE_NOT_HANDLED;
    }

    @Override
    public void executeAction(Request request, Response response, ActionContext ac) throws Throwable {
        Result result = new Result();
        request.setResult(result);
        executeAndRenderAction(request, response, ac, result);
    }

    @Override
    public boolean handleAction(Request request, Response response, String actionPath) throws Throwable {
        Router router = request.getExternalRouter();
        if (null == router) {
            router = new SimpleRouter(app.routes(), null);
        }

        DefaultActionContext ac = newActionContext(request, response);
        ac.setPath(actionPath);

        if (ROUTE_STATE_HANLDED == routeAndExecuteAction(request, response, router, ac)) {
            return true;
        }

        return handleNoAction(request, response, router, actionPath);
    }

    protected boolean handleNoAction(Request request, Response response, Router router, String path) throws Throwable {


        if (response.isHandled()) {
            return true;
        }

        if (path.equals("/")) {
            path = homePath;
        }

        if (router.handleNotFound(request, response, path)) {
            return true;
        }

        View view = request.getViewSource().getView(path, request.getLocale());
        if (null == view) {
            if (path.endsWith("/")) {
                path = path + "index";
            } else {
                path = path + "/index";
            }
            view = request.getViewSource().getView(path, request.getLocale());
        }
        if (null != view) {
            if (_debug) {
                log.debug("Handling request '{}' by view '{}'...", request.getPath(), view.toString());
            }
            view.render(request, response, WrappedViewData.EMPTY);
            return true;
        }

        return false;
    }

    @Override
    public boolean handleError(Request request, Response response, int status) throws Throwable {
        return handleError(request, response, status, null, null);
    }

    @Override
    public boolean handleError(Request request, Response response, int status, String message) throws Throwable {
        return handleError(request, response, status, message, null);
    }

    protected boolean handleError(Request request, Response response, int status, String message,
                                  Throwable exception) throws Throwable {
        if (response.isHandled() || response.isCommitted()) {
            return false;
        }

        if (request.isAjax()) {
            webConfig.getAjaxHandler().handleError(request, response, status, message, exception);
            return true;
        }

        View view = app.errorViews().resolveView(request, status);
        if (null != view) {

            if (_debug) {
                log.debug("Rendering error view '{}' for error {} : {}", view, status, message, exception);
            }

            response.setStatus(status);
            LinkedViewData m = LinkedViewData.of("error", new ErrorInfo(status, message, exception));
            view.render(request, response, m);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleError(Request request, Response response, Throwable exception) throws Throwable {
        if (response.isHandled() || response.isCommitted()) {
            return false;
        }

        int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

        if (request.isAjax()) {
            webConfig.getAjaxHandler().handleError(request, response, status,
                    null == exception ? null : exception.getMessage(), exception);
            return true;
        }

        View view = app.errorViews().resolveView(request, exception.getClass());
        if (null != view) {

            if (_debug) {
                log.debug("Rendering error view '{}'", view, exception);
            }

            response.setStatus(status);
            LinkedViewData m = LinkedViewData.of("error", new ErrorInfo(status, exception));
            view.render(request, response, m);
            return true;

        }
        return handleError(request, response, status, exception.getMessage(), exception);
    }

    protected void executeAndRenderAction(Request request, Response response, ActionContext ac,
                                          Result result) throws Throwable {
        request.setActionContext(ac);

        if (result.increaseAndGetExecutionCount() > maxExecutionCount) {
            throw new ResultException("Max execution count " + maxExecutionCount + " reached, may be cyclic executing");
        }

        Validation validation = request.getValidation();

        //execute action
        Object returnValue = executeAction(ac, validation);

        //raw response
        if (response.isHandled() || response.isCommitted()) {
            log.debug("Response was rendered or committed, do not render the result of action");
            return;
        }

        if (null == result.getRenderable()) {
            //process the return value
            processResult(ac, validation, returnValue, result);
        }

        //render result
        renderResult(request, response, ac, result);
    }

    protected Object executeAction(ActionContext ac, Validation validation) throws Throwable {
        return actionManager.executeAction(ac, validation);
    }

    protected void processResult(ActionContext ac, Validation validation, Object returnValue,
                                 Result result) throws Throwable {
        actionManager.processResult(ac, validation, returnValue, result);
    }

    public void renderResponseException(Request request, Response response, ResponseException e) throws Throwable {
        Content content = e.getContent();
        if (null == content) {
            if (!handleError(request, response, e.getStatus(), e.getMessage(), e.getCause())) {
                response.sendError(e.getStatus(), e.getMessage());
            }
        } else {
            response.setStatus(e.getStatus());
            content.render(request, response);
        }
    }

    protected void renderResult(Request request, Response response, ActionContext actionContext,
                                Result result) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Rendering result : {} , locale : {}", result, request.getLocale());
        }

        //todo : hard code cors exposed headers processing before writing data.
        webConfig.getCorsHandler().postHandle(request, response);

        Renderable renderable = result.getRenderable();

        //no renderable specified explicitly.
        if (null == renderable) {
            if (!result.isCommitted()) {
                throw new ResultNotFoundException(actionContext, result,
                        "Result not found for action '" + actionContext.getAction().toString() + "'");
            }

            response.setStatus(result.getStatus());
            return;
        }

        //set response status if the result status is valid.
        if (result.getStatus() != Result.STATUS_UNDEFINED) {
            response.setStatus(result.getStatus());
        }

        //render
        renderable.render(request, response);
    }
}
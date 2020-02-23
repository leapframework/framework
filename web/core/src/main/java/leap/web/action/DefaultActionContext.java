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
package leap.web.action;

import leap.lang.accessor.MapAttributeAccessor;
import leap.web.Request;
import leap.web.Response;
import leap.web.Result;
import leap.web.format.RequestFormat;
import leap.web.format.ResponseFormat;
import leap.web.route.Route;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultActionContext extends MapAttributeAccessor implements ActionContext {

    protected final Request  request;
    protected final Response response;

    protected String              method;
    protected String              path;
    protected Route               route;
    protected ActionExecution     execution;
    protected Map<String, String> pathParameters;
    protected RequestFormat       requestFormat;
    protected ResponseFormat      responseFormat;
    protected Boolean             acceptValidationError;

    private ActionParams        resolvedParams;
    private Map<String, Object> mergedParameters;
    private Map<String, Object> mergedParametersWithArgs;

    public DefaultActionContext(Request request, Response response) {
        this.request = request;
        this.response = response;
        this.method = request.getMethod();
    }

    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    public Response getResponse() {
        return response;
    }

    @Override
    public Result getResult() {
        return request.getResult();
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    @Override
    public Action getAction() {
        return null == route ? null : route.getAction();
    }

    @Override
    public ActionExecution getActionExecution() {
        return execution;
    }

    @Override
    public void setActionExecution(ActionExecution execution) {
        this.execution = execution;
    }

	@Override
	public ActionParams getResolvedParams() {
		return resolvedParams;
	}

	@Override
	public void setResolvedParams(ActionParams resolvedParams) {
		this.resolvedParams = resolvedParams;
	}

	@Override
    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(Map<String, String> params) {
        this.pathParameters = Collections.unmodifiableMap(params);
    }

    public RequestFormat getRequestFormat() {
        return requestFormat;
    }

    public void setRequestFormat(RequestFormat requestFormat) {
        this.requestFormat = requestFormat;
    }

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(ResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
    }

    @Override
    public boolean isAcceptValidationError() {
        if (null != acceptValidationError) {
            return acceptValidationError;
        } else if (null != request.getAcceptValidationError()) {
            return request.getAcceptValidationError();
        } else if (null != route) {
            return route.isAcceptValidationError();
        } else {
            return false;
        }
    }

    public void setAcceptValidationError(boolean accept) {
        this.acceptValidationError = accept;
    }

    @Override
    public Map<String, Object> getMergedParameters() {
        if (null == mergedParameters) {
            mergedParameters = new LinkedHashMap<>(request.getParameters());
            mergedParameters.putAll(pathParameters);
            mergedParameters = Collections.unmodifiableMap(mergedParameters);
        }
        return mergedParameters;
    }

    @Override
    public Map<String, Object> getMergedParametersWithArgs() {
        if (null == mergedParametersWithArgs) {
            Action action = getAction();

            if (action.getArguments().length == 0) {
                return getMergedParameters();
            }

            if (null == execution) {
                throw new IllegalStateException("Action args has not been resolved");
            }

            mergedParametersWithArgs = new LinkedHashMap<>(getMergedParameters());
            for (int i = 0; i < action.getArguments().length; i++) {
                mergedParametersWithArgs.put(action.getArguments()[i].getName(), execution.getArgs()[i]);
            }
        }
        return mergedParametersWithArgs;
    }
}
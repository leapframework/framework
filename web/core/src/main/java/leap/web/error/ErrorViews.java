package leap.web.error;

import java.util.Map;

import leap.web.Request;
import leap.web.view.View;

public interface ErrorViews {

	ErrorViews addErrorView(Class<?> exceptionClass, View view);

	ErrorViews addErrorView(Class<?> exceptionClass, ErrorView view);

	ErrorViews addErrorView(Class<?> exceptionClass, String viewName);

	ErrorViews addErrorView(int status, ErrorView view);

	ErrorViews addErrorView(int status, View view);

	ErrorViews addErrorView(int status, String viewName);

	ErrorViews addErrorViews(ErrorsConfig config);

	View resolveView(Request request, int status) throws Throwable;

	View resolveView(Request request, Class<?> exceptionClass) throws Throwable;

	ErrorView getErrorView(int status);

	ErrorView getErrorView(Class<?> exceptionClass);

	Map<Integer, ErrorView> getStatusViewMappings();

	Map<Class<?>, ErrorView> getExceptionViewMappings();

}
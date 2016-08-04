package leap.web.route;

import leap.lang.Sourced;
import leap.web.action.FailureHandler;
import leap.web.format.RequestFormat;
import leap.web.format.ResponseFormat;
import leap.web.view.View;

import java.util.Comparator;
import java.util.Map;

/**
 * Indicates a routing rule use to mapping a request to a handler.
 */
public interface Route extends RouteBase, Sourced {

	Comparator<Route> COMPARATOR = 
			new Comparator<Route>() {
               @Override
               public int compare(Route o1, Route o2) {
                   if (o1 == null && o2 == null) {
                       return 1;
                   }

                   if (o1 == null) {
                       return 1;
                   }

                   if (o2 == null) {
                       return -1;
                   }

                   int result = o1.getPathTemplate().compareTo(o2.getPathTemplate());

                   if (result == 0) {
                       result = Integer.compare(o2.getRequiredParameters().size(), o1
                               .getRequiredParameters().size());
                   }

                   if (result == 0) {
                       if (o1.getMethod().equals("*")) {
                           result = 1;
                       } else if (o2.getMethod().equals("*")) {
                           result = -1;
                       } else {
                           result = o1.getMethod().compareToIgnoreCase(o2.getMethod());
                       }
                   }

                   return result == 0 ? 1 : result;
               }

           };

	/**
	 * Returns a object indicates the source location defined this route.
	 */
	Object getSource();

	/**
	 * Returns the http method defined in this routing rule.
	 * 
	 * <p>
	 * The returned http method muse be a valid http method name in upper case.
	 * 
	 * <p>
	 * Returns <code>*</code> means to match all http methods.
	 */
	String getMethod();

	/**
	 * Returns an array contains all the {@link FailureHandler}.
	 */
	FailureHandler[] getFailureHandlers();

    /**
     * Returns the http response status if success or null if use default.
     */
	Integer getSuccessStatus();

    /**
     * Sets the http response status if success.
     *
     * <p/>
     * The status must be null or 2xx.
     *
     * @throws IllegalStateException if the status is not null or not a 2xx value.
     */
    void setSuccessStatus(Integer status) throws IllegalStateException;

	/**
	 * Returns <code>true</code> if this action supports multipart request.
	 */
	boolean supportsMultipart();

	/**
	 * Returns <code>true</code> if this enables cors support explicitly.
	 */
	boolean isCorsEnabled();

	/**
	 * Returns <code>true</code> if this disables cors support explicitly.
	 */
	boolean isCorsDisabled();
	
	/**
	 * Returns the {@link RequestFormat} or <code>null</code>.
	 */
	RequestFormat getRequestFormat();

	/**
	 * Returns the {@link ResponseFormat} or <code>null</code>.
	 */
	ResponseFormat getResponseFormat();

	/**
	 * Returns a {@link View} object for rendering the result of action.
	 * 
	 * <p>
	 * Returns <code>null</code> if no view exists for the action.
	 */
	View getDefaultView();

	/**
	 * Returns a view name for rendering the result of action.
	 *
	 * <p>
	 * Returns <code>null</code> if no view exists for the action.
	 */
	String getDefaultViewName();

	/**
	 * Returns the controller's path or <code>null</code>
	 */
	String getControllerPath();

	/**
	 * Returns the execution attributes or <code>null</coce>.
	 */
	Object getExecutionAttributes();

	/**
	 * Returns a {@link Map} contains all the requried parameters or empty.
	 */
	Map<String, String> getRequiredParameters();

	/**
	 * Enables cors support if <code>true</code>, 
	 * 
	 * or
	 * 
	 * Disables cors support if <code>null</code>,
	 * 
	 * or 
	 * 
	 * Donothing if <code>null</code>.
	 */
	void setCorsEnabled(Boolean enabled);
	
	/**
	 * Sets the <code>supportMultipart</code> property.
	 */
	void setSupportsMultipart(boolean supports);

    /**
     * Returns true if allow anonymous access.
     */
    boolean isAllowAnonymous();

    /**
     * Sets is allow anonymous access.
     */
    void setAllowAnonymous(Boolean allowAnonymous);

    /**
     * Returns true if allow client only access.
     */
    boolean isAllowClientOnly();

    /**
     * Sets is allow client only access.
     */
    void setAllowClientOnly(Boolean allowClientOnly);
	
	/**
	 * Returns <code>true</code> if the route enables csrf support explicitly.
	 */
	boolean isCsrfEnabled();
	
	/**
	 * Returns <code>true</code> if the route disables csrf support explicitly.
	 */
	boolean isCsrfDisabled();
	
	/**
	 * Eanbles or Disables csrf.
	 */
	void setCsrfEnabled(Boolean enabled);
	
	/**
	 * Returns <code>true</code> if the action accepts validation error.
	 * 
	 * That means the action will be executed in spite of validation error.
	 */
	boolean isAcceptValidationError();
	
	/**
	 * Sets the property.
	 */
	void setAcceptValidationError(boolean accept);
	
	/**
	 * Returns <code>true</code> if the route only accepts https request.
	 */
	boolean isHttpsOnly();
	
	/**
	 * Sets https only.
	 */
	void setHttpsOnly(boolean httpsOnly);

}
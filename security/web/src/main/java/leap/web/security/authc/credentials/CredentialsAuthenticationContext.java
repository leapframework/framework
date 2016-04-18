package leap.web.security.authc.credentials;

import leap.core.validation.ValidationContext;

public interface CredentialsAuthenticationContext extends ValidationContext {
	/**
	 * Return the error status of this context
	 * @return
	 */
	boolean isError();
	
	/**
	 * Sets the error status of this context
	 * @param error
	 */
	void setError(boolean error);
	
	/**
	 * Get the error object of this context
	 * @return
	 */
	Object getErrorObj();
	
	/**
	 * Sets the error object of this context
	 * @param obj
	 */
	void setErrorObj(Object obj);
	
	/**
	 * Get current authenticating identity
	 * @return
	 */
	String getIdentity();
	
	/**
	 * Sets current authenticating identity
	 * @return
	 */
	void setIdentity(String identity);
}

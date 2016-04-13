package leap.web.security.authc.credentials;

import leap.core.validation.Validation;

public class DefaultAuthenticateCredentialsContext implements AuthenticateCredentialsContext {
	
	protected Validation 	validation;
	protected boolean 		error;
	protected Object		errorObj;
	protected String		identity;
	
	public DefaultAuthenticateCredentialsContext(Validation validation) {
		super();
		this.validation = validation;
	}

	@Override
	public Validation validation() {
		return validation;
	}

	@Override
	public boolean isError() {
		return error;
	}

	@Override
	public void setError(boolean error) {
		this.error = error;
	}

	@Override
	public Object getErrorObj() {
		return errorObj;
	}

	@Override
	public void setErrorObj(Object obj) {
		this.errorObj = obj;
	}

	@Override
	public String getIdentity() {
		return identity;
	}

	@Override
	public void setIdentity(String identity) {
		this.identity = identity;
	}

}

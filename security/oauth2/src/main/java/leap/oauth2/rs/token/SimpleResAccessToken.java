package leap.oauth2.rs.token;

import java.util.Map;

import leap.core.security.token.SimpleTokenCredentials;
import leap.lang.Strings;
import leap.oauth2.OAuth2Constants;

public class SimpleResAccessToken extends SimpleTokenCredentials implements ResAccessToken {

	protected final String type;
	protected final boolean bearer;
	protected final Map<String, Object> params;
	
	public SimpleResAccessToken(String type, String token, Map<String, Object> params) {
	    super(token);
		this.type  = type;
		this.bearer = Strings.isEmpty(type) || OAuth2Constants.BEARER_TYPE.equalsIgnoreCase(type);
		this.params = params;
	}

	/**
	 * Returns the token type.
	 */
	public String getType() {
		return type;
	}

    @Override
    public boolean isBearer() {
        return bearer;
    }

    @Override
    public Object getParameter(String name) {
        return params.get(name);
    }

    @Override
    public Map<String, Object> getParameters() {
        return params;
    }

	@Override
	public boolean isJwt() {
		return false;
	}
}
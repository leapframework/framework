package leap.oauth2.rs.token;

import leap.core.security.token.SimpleTokenCredentials;
import leap.lang.Strings;
import leap.oauth2.OAuth2Constants;

import java.util.Map;

/**
 * Created by KAEL on 2016/5/8.
 */
public class SimpleJwtAccessToken extends SimpleTokenCredentials implements ResAccessToken  {

    protected final String type;
    protected final boolean bearer;
    protected final Map<String, Object> params;

    public SimpleJwtAccessToken(String type, String token, Map<String, Object> params) {
        super(token);
        this.type = type;
        this.bearer = Strings.isEmpty(type) || OAuth2Constants.BEARER_TYPE.equalsIgnoreCase(type);;
        this.params = params;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public Object getParameter(String name) {
        return params.get(name);
    }

    @Override
    public boolean isBearer() {
        return this.bearer;
    }

    @Override
    public Map<String, Object> getParameters() {
        return params;
    }

    @Override
    public boolean isJwt() {
        return true;
    }
}

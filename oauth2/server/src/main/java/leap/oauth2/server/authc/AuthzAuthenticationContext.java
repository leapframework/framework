package leap.oauth2.server.authc;

import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.client.AuthzClient;

public class AuthzAuthenticationContext {

	private OAuth2Params oauthParam;

	private AuthzClient client;

	private Object data;

	public AuthzAuthenticationContext(){

	}

	public AuthzAuthenticationContext(OAuth2Params oauthParam,AuthzClient client,Object data){
		this.oauthParam=oauthParam;
		this.client=client;
		this.data=data;
	}

	public OAuth2Params getOauthParam() {
		return oauthParam;
	}

	public void setOauthParam(OAuth2Params oauthParam) {
		this.oauthParam = oauthParam;
	}

	public AuthzClient getClient() {
		return client;
	}

	public void setClient(AuthzClient client) {
		this.client = client;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}


}

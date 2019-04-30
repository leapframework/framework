package leap.web.api.remote;

import leap.oauth2.webapp.token.at.AccessToken;

import java.util.Map;

/**
 * 服务端使用客户端at，转换后的新access_token
 * @author fulsh
 *
 */
public class MappedAccessToken implements AccessToken {
	private AccessToken at;

	private String rawToken;

	public MappedAccessToken(){

	}

	public MappedAccessToken(String rawToken,AccessToken at){
		this.at=at;
		this.rawToken=rawToken;
	}


	@Override
	public String getClientId() {
		return this.at.getClientId();
	}

	@Override
	public String getUserId() {
		return this.at.getUserId();
	}

	@Override
	public String getScope() {
		return this.at.getScope();
	}

	@Override
	public boolean isExpired() {
		return this.at.isExpired();
	}

	@Override
	public String getToken() {
		return this.at.getToken();
	}

	@Override
	public String getRefreshToken() {
		return this.at.getRefreshToken();
	}

	public String getRawToken() {
		return rawToken;
	}

	public void setRawToken(String rawToken) {
		this.rawToken = rawToken;
	}

	@Override
	public Map<String, Object> getClaims() {
		return this.at.getClaims();
	}
}

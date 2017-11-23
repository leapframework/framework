package leap.web.api.remote;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import leap.core.annotation.Inject;
import leap.lang.http.HTTP;
import leap.lang.http.client.HttpRequest;
import leap.oauth2.webapp.code.DefaultCodeVerifier;
import leap.oauth2.webapp.token.Token;
import leap.oauth2.webapp.token.TokenContext;
import leap.oauth2.webapp.token.TokenExtractor;
import leap.oauth2.webapp.token.at.AccessToken;
import leap.web.Request;

public class TokenFetcher extends DefaultCodeVerifier {

	@Inject
	protected TokenExtractor tokenExtractor;

	//客户端At到当前应用自身的Token映射
	final static Cache<String, AccessToken> tokenMappings = CacheBuilder.newBuilder()
					.maximumSize(5000)
					.expireAfterWrite(10, TimeUnit.HOURS).build();

	public AccessToken getAccessToken(Request request){
		AccessToken at= TokenContext.getAccessToken();
		if(at!=null){
			return at;
		}
		Token token= tokenExtractor.extractTokenFromRequest(request);
		if(token==null){
			return null;
		}
		String clientAt=token.getToken();
		at=mapToSelfToken(clientAt);
		return at;
	}

	private AccessToken mapToSelfToken(String clientAt){
		AccessToken token=tokenMappings.getIfPresent(clientAt);
		if(token!=null){
			if(token.isExpired()){
				token=refreshAccessToken(token);
				tokenMappings.put(clientAt, token);
			}
			return token;
		}
		token=newAccessToken(clientAt);
		if(token!=null){
			tokenMappings.put(clientAt, token);
		}
		return token;
	}


	public AccessToken newAccessToken(String at) {
		if (null == config.getTokenUrl()) {
			throw new IllegalStateException("The tokenUrl must be configured");
		}

		HttpRequest request = httpClient.request(config.getTokenUrl())
				.addFormParam("grant_type", "token_client_credentials").addFormParam("access_token", at)
				.setMethod(HTTP.Method.POST);

		return fetchAccessToken(request);
	}

	public AccessToken refreshAccessToken(AccessToken old) {
		if (null == config.getTokenUrl()) {
			throw new IllegalStateException("The tokenUrl must be configured");
		}

		HttpRequest request = httpClient.request(config.getTokenUrl()).addFormParam("grant_type", "refresh_token")
				.addFormParam("refresh_token", old.getRefreshToken()).setMethod(HTTP.Method.POST);

		return fetchAccessToken(request);
	}

}

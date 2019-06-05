package leap.web.api.remote;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.lang.http.client.HttpRequest;
import leap.oauth2.webapp.code.DefaultCodeVerifier;
import leap.oauth2.webapp.token.Token;
import leap.oauth2.webapp.token.TokenContext;
import leap.oauth2.webapp.token.TokenExtractor;
import leap.oauth2.webapp.token.at.AccessToken;
import leap.oauth2.webapp.token.at.SimpleAccessToken;
import leap.web.Request;
import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.TimeUnit;

public class TokenFetcher extends DefaultCodeVerifier {

    @Inject
    protected TokenExtractor tokenExtractor;

    final static ExpiringMap<String, MappedAccessToken> tokenMappings =
            ExpiringMap.builder().maxSize(5000).expiration(10, TimeUnit.HOURS).build();

    public AccessToken getAccessToken(Request request) {
        AccessToken at = TokenContext.getAccessToken();
        if (at != null) {
            return at;
        }
        Token token = tokenExtractor.extractTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        return new SimpleAccessToken(token.getToken());

        /* todo: re-new access token use backend client credentials?
        String clientAt = token.getToken();
        at = mapToSelfToken(clientAt);
        return at;
        */
    }

    private MappedAccessToken mapToSelfToken(String clientAt) {
        MappedAccessToken token = tokenMappings.get(clientAt);
        if (token != null) {
            if (token.isExpired()) {
                AccessToken at = refreshAccessToken(token);
                token = new MappedAccessToken(clientAt, at);
                tokenMappings.put(clientAt, token);
            }
            return token;
        }
        token = newAccessToken(clientAt);
        if (token != null) {
            tokenMappings.put(clientAt, token);
        }
        return token;
    }

    private MappedAccessToken newAccessToken(String token) {
        if (null == config.getTokenUrl()) {
            throw new IllegalStateException("The tokenUrl must be configured");
        }

        //todo: client only token can't use 'token_client_credentials' grant_type, use client_credentials instead
        HttpRequest request = httpClient.request(config.getTokenUrl())
                .addFormParam("grant_type", "token_client_credentials").addFormParam("access_token", token)
                .setMethod(HTTP.Method.POST);

        AccessToken accessToken = fetchAccessToken(request);
        return new MappedAccessToken(token, accessToken);
    }

    public AccessToken refreshAccessToken(AccessToken old) {
        //No refresh token, returns null.
        if (Strings.isEmpty(old.getRefreshToken())) {
            return null;
        }

        if (old instanceof MappedAccessToken) {
            tokenMappings.remove(((MappedAccessToken) old).getRawToken());
        }

        if (null == config.getTokenUrl()) {
            throw new IllegalStateException("The tokenUrl must be configured");
        }

        HttpRequest request = httpClient.request(config.getTokenUrl()).addFormParam("grant_type", "refresh_token")
                .addFormParam("refresh_token", old.getRefreshToken()).setMethod(HTTP.Method.POST);

        AccessToken newAt = fetchAccessToken(request);
        if (old instanceof MappedAccessToken) {
            MappedAccessToken mapped = new MappedAccessToken(((MappedAccessToken) old).getRawToken(), newAt);
            tokenMappings.put(mapped.getRawToken(), mapped);
            newAt = mapped;
        }
        return newAt;
    }

}

package leap.oauth2.server;

import leap.core.i18n.MessageKey;
import leap.lang.http.HTTP;

import java.util.Locale;

import static leap.oauth2.server.OAuth2Errors.ERROR_INVALID_GRANT;

/**
 * @author kael.
 */
public class OAuth2ErrorBuilder {
    protected int        status;
    protected String     error;
    protected String     errorCode;
    protected String     referral;
    protected String     errorDescription;
    private   MessageKey key;

    public static OAuth2ErrorBuilder create() {
        return new OAuth2ErrorBuilder();
    }
    
    public static OAuth2ErrorBuilder createUnauthorized(){
        return create().withStatus(HTTP.SC_UNAUTHORIZED);
    }
    
    public static OAuth2ErrorBuilder createInvalidGrant(){
        return createUnauthorized().withError(ERROR_INVALID_GRANT);
    }
    
    public OAuth2ErrorBuilder withStatus(int status) {
        this.status = status;
        return this;
    }

    public OAuth2ErrorBuilder withError(String error) {
        this.error = error;
        return this;
    }

    public OAuth2ErrorBuilder withErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public OAuth2ErrorBuilder withErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
        return this;
    }

    public OAuth2ErrorBuilder withReferral(String referral) {
        this.referral = referral;
        return this;
    }

    public OAuth2ErrorBuilder withMessageKey(MessageKey key) {
        this.key = key;
        return this;
    }

    public OAuth2ErrorBuilder withMessageKey(Locale locale, String key, Object[] params) {
        this.key = OAuth2Errors.messageKey(locale, key, params);
        return this;
    }

    public OAuth2Error build() {
        return new SimpleOAuth2Error(status, error, errorCode, referral, errorDescription, key);
    }
}

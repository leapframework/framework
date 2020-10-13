package leap.oauth2.server;

import leap.core.i18n.MessageKey;
import leap.core.i18n.MessageSource;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.http.HTTP;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import static leap.oauth2.server.OAuth2Errors.*;

/**
 * @author kael.
 */
public class OAuth2ErrorBuilder {
    protected int           status;
    protected String        error;
    protected String        errorCode;
    protected String        referral;
    protected String        errorDescription;
    protected Map<String, Object> properties = New.hashMap();
    private   MessageKey    key;
    private   MessageSource messageSource;

    public static OAuth2ErrorBuilder create() {
        return new OAuth2ErrorBuilder();
    }

    public static OAuth2ErrorBuilder createUnauthorized() {
        return create().withStatus(HTTP.SC_UNAUTHORIZED);
    }

    public static OAuth2ErrorBuilder createInvalidGrant() {
        return createUnauthorized().withError(ERROR_INVALID_GRANT);
    }

    public static OAuth2ErrorBuilder createInvalidToken() {
        return createUnauthorized().withError(ERROR_INVALID_TOKEN);
    }

    public static OAuth2ErrorBuilder createInvalidUser() {
        return createUnauthorized().withError(ERROR_INVALID_USER);
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

    public OAuth2ErrorBuilder withMessageSource(MessageSource source){
        this.messageSource = source;
        return this;
    }

    public OAuth2ErrorBuilder withProperty(String key, Object value){
        properties.put(key, value);
        return this;
    }

    public OAuth2ErrorBuilder withProperties(Map<String, Object> properties){
        this.properties = properties;
        return this;
    }

    public OAuth2ErrorBuilder withProperties(Consumer<Map<String, Object>> consumer){
        consumer.accept(properties);
        return this;
    }

    public OAuth2Error build() {
        SimpleOAuth2Error soe = new SimpleOAuth2Error(status, error, Strings.isEmpty(errorCode) ? error : errorCode, referral, errorDescription, key);
        if (null != messageSource){
            soe.setMessageSource(messageSource);
        }
        soe.setProperties(properties);
        return soe;
    }
}

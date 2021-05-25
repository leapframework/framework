package leap.oauth2.webapp;

public interface OAuth2ConfigBase {

    /**
     * Returns <code>true</code> if oauth2.0 is enabled in current web app.
     */
    boolean isEnabled();

    /**
     * todo : doc
     */
    String getTokenUrl();

    /**
     * todo: doc
     */
    String getClientId();

    /**
     * todo: doc
     */
    String getClientSecret();

}
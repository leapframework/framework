package leap.web.api;

import leap.lang.Named;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.ApiMetadata;

public interface Api extends Named {

    /**
     * Returns the base path of api.
     */
    String getBasePath();

    /**
     * Returns the configuration of this api.
     */
    ApiConfig getConfig();

    /**
     * Returns the configurator of this api.
     *
     * @throws IllegalStateException if this api has been created.
     */
    ApiConfigurator getConfigurator() throws IllegalStateException;

    /**
     * Returns the {@link ApiMetadata} of this api.
     *
     * @throws IllegalStateException if this api has not been created.
     */
    ApiMetadata getMetadata() throws IllegalStateException;

    /**
     * Sets the {@link ApiMetadata} of this api.
     *
     * @throws IllegalStateException if this api has been created.
     */
    void setMetadata(ApiMetadata metadata) throws IllegalStateException;

    /**
     * Returns <code>true</code> if this api is created.
     */
    boolean isCreated();

    /**
     * Returns <code>true</code> if this api is dynamic (not configured)
     */
    boolean isDynamic();

    /**
     * Creates this api.
     *
     * @throws IllegalStateException if this api has been created.
     */
    void create() throws IllegalStateException;
}
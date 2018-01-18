package leap.web.api;

import leap.lang.ExtensibleBase;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.ApiMetadata;

import java.util.function.Consumer;

public class DefaultApi extends ExtensibleBase implements Api {

    protected final Consumer<Api>   creator;
    protected final ApiConfigurator configurator;
    protected final ApiConfig       config;
    protected final boolean         dynamic;

    protected volatile boolean     created;
    protected          ApiMetadata metadata;

    public DefaultApi(Consumer<Api> creator, ApiConfigurator configurator, boolean dynamic) {
        this.creator      = creator;
        this.configurator = configurator;
        this.config       = configurator.config();
        this.dynamic      = dynamic;
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public String getBasePath() {
        return config.getBasePath();
    }

    @Override
    public ApiConfig getConfig() {
        return config;
    }

    @Override
    public ApiConfigurator getConfigurator() throws IllegalStateException {
        if(created) {
            throw new IllegalStateException("Cannot get api configurator after created!");
        }
        return configurator;
    }

    @Override
    public ApiMetadata getMetadata() {
        mustCreated();
        return metadata;
    }

    @Override
    public void setMetadata(ApiMetadata metadata) {
        if(created) {
            throw new IllegalStateException("Cannot set metadata, api already created!");
        }
        this.metadata = metadata;
    }

    @Override
    public boolean isCreated() {
        return created;
    }

    @Override
    public void markCreated() throws IllegalStateException {
        if(created) {
            throw new IllegalStateException("Can't mark created, already created yet!");
        }
        this.created = true;
    }

    @Override
    public boolean isDynamic() {
        return dynamic;
    }

    @Override
    public void create() throws IllegalStateException {
        if(created) {
            throw new IllegalStateException("Cannot create api again, it already created yet!");
        }
        this.creator.accept(this);
    }

    protected void mustCreated() {
        if(!created) {
            throw new IllegalStateException("Api '" + config.getName() + "' not created!");
        }
    }

    @Override
    public String toString() {
        return "Api[" + getName() + "," + getBasePath() + "]";
    }
}

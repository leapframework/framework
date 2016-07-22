package leap.core.ioc;

import java.util.Set;

import leap.lang.Ordered;
import leap.lang.Sourced;

public interface BeanDefinition extends TypeDefinition, Sourced,Ordered {
	
	int DEFAULT_SORT_ORDER = 100;

    /**
     * Optional. Returns the bean id.
     */
	String getId();

    /**
     * Returns the defined class of bean.
     */
    Class<?> getBeanClass();

    /**
     * Returns <code>true</code> if the bean is singleton.
     */
	boolean isSingleton();

    /**
     * Returns <code>true</code> if the bean is lazy init (create the instance).
     */
    boolean isLazyInit();

    /**
     * Returns <code>true</code> if the bean already inited(the instance has be created).
     */
    boolean isInited();

    /**
     * Returns <code>true</code> if the bean is configurable.
     */
    boolean isConfigurable();

    /**
     * Returns the key prefix of configuration properties.
     */
    String getConfigurationPrefix();

    /**
     * Returns an immutable {@link Set} contains the qualifiers
     */
	Set<String> getQualifiers();

    /**
     * Returns the instance of bean if singleton.
     */
    Object getSingletonInstance();

}
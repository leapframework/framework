package leap.core.ioc;

import java.util.Set;

import leap.lang.Ordered;
import leap.lang.Sourced;

public interface BeanDefinition extends Sourced,Ordered {
	
	int DEFAULT_SORT_ORDER = 100;

    /**
     * Optional. Returns the bean id.
     */
	String getId();

    /**
     * Optional. Returns the bean name.
     */
	String getName();

    /**
     * Returns the defined class of bean.
     */
    Class<?> getBeanClass();

    /**
     * Returns <code>true</code> if the bean is a primary bean.
     */
	boolean isPrimary();

    /**
     * Returns <code>true</code> if the bean is singleton.
     */
	boolean isSingleton();

    /**
     * Returns <code>true</code> if the bean is lazy init (create the instance).
     */
    boolean isLazyInit();

    /**
     * Returns <code>true</code> if the bean already inited.
     */
    boolean isInited();

    /**
     * Returns an immutable {@link Set} contains the qualifiers
     */
	Set<String> getQualifiers();

    /**
     * Returns the instance of bean if singleton.
     */
    Object getSingletonInstance();
}
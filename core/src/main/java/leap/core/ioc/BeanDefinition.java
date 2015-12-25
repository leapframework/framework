package leap.core.ioc;

import java.util.Set;

import leap.lang.Ordered;
import leap.lang.Sourced;

public interface BeanDefinition extends Sourced,Ordered {
	
	int DEFAULT_SORT_ORDER = 100;

	String getId();

	String getName();

	boolean isPrimary();

	boolean isSingleton();

	Set<String> getQualifiers();
}
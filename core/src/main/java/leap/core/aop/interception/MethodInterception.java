package leap.core.aop.interception;

import java.util.function.Supplier;

public interface MethodInterception {

    String getClassName();

    String getMethodName();

    String getMethodDesc();

    Object getObject();

    Object[] getArguments();

    MethodInterceptor[] getInterceptors();

    Runnable getRunnable();

    <T> Supplier<T> getSupplier();
}

package leap.core.aop.interception;

public interface MethodInterception {

    String getClassName();

    String getMethodName();

    String getMethodDesc();

    Object getObject();

    Object[] getArguments();

    MethodInterceptor[] getInterceptors();

    Runnable getRunnable();
}

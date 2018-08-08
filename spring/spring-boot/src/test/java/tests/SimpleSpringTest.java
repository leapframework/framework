package tests;

import leap.core.el.ExpressionLanguage;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SimpleSpringTest extends AbstractTest {

    @Autowired
    protected ExpressionLanguage el;

    @Test
    public void testStartup() {
        //do nothing.
    }

    @Test
    public void testVariable() {
        Object hello = el.createExpression("env.hello").getValue();
        assertEquals("hello", hello);
    }
}

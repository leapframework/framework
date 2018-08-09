package tests;

import app.beans.LeapBean;
import app.beans.ListType;
import app.beans.SpringBean;
import leap.core.el.ExpressionLanguage;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SimpleSpringTest extends AbstractTest {

    @Autowired
    protected ExpressionLanguage el;

    @Autowired
    protected SpringBean springBean;

    @Autowired
    protected LeapBean leapBean;

    @Test
    public void testStartup() {
        //do nothing.
    }

    @Test
    public void testVariable() {
        Object hello = el.createExpression("env.hello").getValue();
        assertEquals("hello", hello);
    }

    @Test
    public void testCyclicAutowired() {
        assertNotNull(springBean);
        assertNotNull(springBean.getLeapBean());
        assertNotNull(leapBean);
        assertNotNull(leapBean.getSpringBean());
        assertSame(springBean.getLeapBean(), leapBean);
        assertSame(leapBean.getSpringBean(), springBean);
    }

    @Test
    public void testLeapInjectList() {
        List<ListType> beans = leapBean.getBeans();
        assertEquals(2, beans.size());
    }
}
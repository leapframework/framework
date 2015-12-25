package ${package}.sample;

import leap.lang.Strings;
import leap.core.junit.AppTestBase;

import org.junit.Test;

public class HelloBeanTest extends AppTestBase {
	
	@Test
	public void testGetHelloBean(){
		HelloBean bean = beanFactory.getBean(HelloBean.class);
		assertNotNull(bean);
	}

}
package app;

import app.beans.HelloBean;
import app.beans.HelloVariable;
import app.beans.SpringBean;
import leap.core.variable.Variable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoConfiguration {

    @Bean("hello")
    public HelloBean helloBean1() {
        return new HelloBean();
    }

    @Bean
    public Variable helloVariable() {
        return new HelloVariable();
    }

    @Bean("helloBean")
    public HelloBean helloBean2() {
        return new HelloBean();
    }

    @Bean
    public SpringBean springBean() {
        return new SpringBean();
    }
}
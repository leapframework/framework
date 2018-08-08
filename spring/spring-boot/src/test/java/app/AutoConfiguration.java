package app;

import app.beans.HelloBean;
import app.beans.HelloVariable;
import leap.core.variable.Variable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoConfiguration {

    @Bean("hello")
    public Variable helloVariable() {
        return new HelloVariable();
    }

    @Bean("hello")
    public HelloBean helloBean() {
        return new HelloBean();
    }

}
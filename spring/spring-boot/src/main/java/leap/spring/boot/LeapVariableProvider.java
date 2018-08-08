package leap.spring.boot;

import leap.core.annotation.Inject;
import leap.core.variable.Variable;
import leap.core.variable.VariableDefinition;
import leap.core.variable.VariableProvider;
import leap.lang.annotation.Init;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeapVariableProvider implements VariableProvider {

    @Inject
    protected ApplicationContext context;

    protected List<VariableDefinition> defs = new ArrayList<>();

    @Init
    private void init() {
        Map<String, Variable> vars = context.getBeansOfType(Variable.class);
        vars.forEach((name, var) -> {
            defs.add(new VariableDefinition(name, var));
        });
    }

    @Override
    public List<VariableDefinition> getVariables() {
        return defs;
    }

}
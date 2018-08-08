package app.beans;

import leap.core.variable.Variable;

public class HelloVariable implements Variable {

    @Override
    public Object getValue() {
        return "hello";
    }

}

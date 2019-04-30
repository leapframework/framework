package leap.web.api.route;

import leap.web.route.Route;

public class SimpleApiRoute implements ApiRoute {

    private final Route   route;
    private final boolean dynamic;
    private final boolean operation;

    public SimpleApiRoute(Route route, boolean dynamic, boolean operation) {
        this.route = route;
        this.dynamic = dynamic;
        this.operation = operation;
    }

    @Override
    public Route getRoute() {
        return route;
    }

    @Override
    public boolean isDynamic() {
        return dynamic;
    }

    @Override
    public boolean isOperation() {
        return operation;
    }

}
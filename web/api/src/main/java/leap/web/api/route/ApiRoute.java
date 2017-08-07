package leap.web.api.route;

import leap.web.route.Route;

public interface ApiRoute {

    /**
     * Returns the {@link Route}.
     */
    Route getRoute();

    /**
     * Returns <code>true</code> if the route is dynamic created.
     */
    boolean isDynamic();

    /**
     * Returns <code>true</code> if the route is api operation.
     */
    boolean isOperation();

}
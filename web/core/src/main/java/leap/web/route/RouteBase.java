/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.route;

import leap.core.web.path.PathTemplate;
import leap.web.action.Action;

/**
 * The base interface of {@link Route} and {@link RouteBuilder}
 */
public interface RouteBase {

    /**
     * Returns the path template defined in this routing rule use to match a request path.
     */
    PathTemplate getPathTemplate();

    /**
     * Returns a {@link Action} object to handle http request or <code>null</code> if no action.
     */
    Action getAction();

    /**
     * Replaces the action.
     */
    void setAction(Action action);

    /**
     * Return the controller of {@link Action} or <code>null</code> if no controller
     */
    default Object getController(){
        Action action = getAction();
        if(action == null){
            return null;
        }
        if(action.hasController()){
            return action.getController();
        }
        return null;
    }
}
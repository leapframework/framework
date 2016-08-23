/*
 * Copyright 2013 the original author or authors.
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
package leap.web.action;

import leap.core.web.path.PathTemplate;

import java.lang.reflect.Method;

/**
 * The strategy interface for configuring and processing action.
 */
public interface ActionStrategy {

    /**
     * Returns true if the class is a controller class.
     */
	boolean isControllerClass(Class<?> cls);

    /**
     * Required.
     *
     * Returns the controller name of the controller class.
     */
    String getControllerName(Class<?> cls);

    /**
     * Required.
     *
     * Returns the instance of the the controller class.
     */
    Object getControllerInstance(Class<?> cls);

    /**
     * Required.
     *
     * Returns the default or declared paths of the controller class.
     */
    String[] getControllerPaths(Class<?> cls);

    /**
     * Returns true if the method is an action method.
     */
    boolean isActionMethod(ControllerInfo ci, Method m);

    /**
     * Returns true if the action is an index action.
     */
	boolean isIndexAction(ActionBuilder action);

    /**
     * Returns the mappings of action.
     */
	ActionMapping[] getActionMappings(ActionBuilder action);

    /**
     * Returns the default view names for the action.
     */
	String[] getDefaultViewNames(ActionBuilder action, String controllerPath, String actionPath, PathTemplate pathTemplate);

}
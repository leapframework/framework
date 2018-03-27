/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.route;

import leap.web.action.*;
import leap.web.format.RequestFormat;
import leap.web.format.ResponseFormat;
import leap.web.view.View;

import java.util.Collections;
import java.util.Map;

/**
 * A {@link Route} contains nested routes.
 */
public interface NestedRoute extends Route {

    Action NOP_ACTION = new HandlerAction((req, resp) -> {});

    /**
     * Returns a matched {@link Route} or <code>null</code> if no route matched.
     */
    Route match(String method, String path, Map<String,Object> inParameters, Map<String,String> outVariables);

    /**
     * todo: doc
     */
    default boolean isCheckAmbiguity() {
        return true;
    }

    @Override
    default FailureHandler[] getFailureHandlers() {
        return new FailureHandler[0];
    }

    @Override
    default Integer getSuccessStatus() {
        return null;
    }

    @Override
    default void setSuccessStatus(Integer status) throws IllegalStateException {

    }

    @Override
    default boolean supportsMultipart() {
        return false;
    }

    @Override
    default boolean isCorsEnabled() {
        return false;
    }

    @Override
    default boolean isCorsDisabled() {
        return false;
    }

    @Override
    default RequestFormat getRequestFormat() {
        return null;
    }

    @Override
    default ResponseFormat getResponseFormat() {
        return null;
    }

    @Override
    default View getDefaultView() {
        return null;
    }

    @Override
    default String getDefaultViewName() {
        return null;
    }

    @Override
    default String getControllerPath() {
        return null;
    }

    @Override
    default Object getExecutionAttributes() {
        return null;
    }

    @Override
    default Map<String, String> getRequiredParameters() {
        return Collections.emptyMap();
    }

    @Override
    default void setCorsEnabled(Boolean enabled) {

    }

    @Override
    default void setSupportsMultipart(boolean supports) {

    }

    @Override
    default Boolean getAllowAnonymous() {
        return null;
    }

    @Override
    default void setAllowAnonymous(Boolean allowAnonymous) {

    }

    @Override
    default Boolean getAllowRememberMe() {
        return null;
    }

    @Override
    default void setAllowRememberMe(Boolean allowRememberMe) {

    }

    @Override
    default Boolean getAllowClientOnly() {
        return null;
    }

    @Override
    default void setAllowClientOnly(Boolean allowClientOnly) {

    }

    @Override
    default String[] getPermissions() {
        return new String[0];
    }

    @Override
    default void setPermissions(String[] permissions) {

    }

    @Override
    default String[] getClientOnlyPermissions() {
        return new String[0];
    }

    @Override
    default void setClientOnlyPermissions(String[] permissions) {

    }

    @Override
    default String[] getRoles() {
        return new String[0];
    }

    @Override
    default void setRoles(String[] roles) {

    }

    @Override
    default boolean isCsrfEnabled() {
        return false;
    }

    @Override
    default boolean isCsrfDisabled() {
        return false;
    }

    @Override
    default void setCsrfEnabled(Boolean enabled) {

    }

    @Override
    default boolean isAcceptValidationError() {
        return false;
    }

    @Override
    default void setAcceptValidationError(boolean accept) {

    }

    @Override
    default boolean isHttpsOnly() {
        return false;
    }

    @Override
    default void setHttpsOnly(boolean httpsOnly) {

    }

    @Override
    default Action getAction() {
        return NOP_ACTION;
    }

    @Override
    default void setAction(Action action) {
        //do nothing.
    }

    @Override
    default Object getController() {
        return null;
    }

}
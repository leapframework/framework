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

import leap.web.action.Action;
import leap.web.action.FailureHandler;
import leap.web.format.RequestFormat;
import leap.web.format.ResponseFormat;
import leap.web.view.View;

import java.util.Collections;
import java.util.Map;

public interface SubRoutes extends Route {

    /**
     * Returns a matched {@link Route} or <code>null</code> if no route matched.
     */
    Route match(String method, String path, Map<String,Object> inParameters, Map<String,String> outVariables);

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
    default String[] getPermissions() {
        return new String[0];
    }

    @Override
    default void setAllowClientOnly(Boolean allowClientOnly) {

    }

    @Override
    default void setPermissions(String[] permissions) {

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
        return null;
    }

    @Override
    default Object getController() {
        return null;
    }
}
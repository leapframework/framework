/*
 * Copyright 2020 the original author or authors.
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

package leap.web.api.remote;

public interface TokenStrategyProvider {

    /**
     * Returns the strategy of the given type.
     */
    default TokenStrategy getStrategy(TokenStrategy.Type type) {
        if (null == type) {
            type = TokenStrategy.Type.DEFAULT;
        }
        if (type == TokenStrategy.Type.DEFAULT) {
            return getDefaultStrategy();
        }
        if (type == TokenStrategy.Type.FORCE_WITH_APP) {
            return getForceWithAppStrategy();
        }
        if (type == TokenStrategy.Type.TRY_WITH_APP) {
            return getTryWithAppStrategy();
        }
        if (type == TokenStrategy.Type.APP_ONLY) {
            return getAppOnlyStrategy();
        }
        throw new IllegalStateException("No supported type '" + type + "'");
    }

    default TokenStrategy.Type getDefaultStrategyType() {
        return TokenStrategy.Type.TRY_WITH_APP;
    }

    /**
     * Returns the default strategy.
     */
    default TokenStrategy getDefaultStrategy() {
        return getTryWithAppStrategy();
    }

    /**
     * todo: doc
     */
    TokenStrategy getOriginalStrategy();

    /**
     * todo: doc
     */
    TokenStrategy getForceWithAppStrategy();

    /**
     * todo: doc
     */
    TokenStrategy getTryWithAppStrategy();

    /**
     * todo: doc
     */
    TokenStrategy getAppOnlyStrategy();

}
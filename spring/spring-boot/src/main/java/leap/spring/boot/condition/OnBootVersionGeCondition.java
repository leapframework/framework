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

package leap.spring.boot.condition;

import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnBootVersionGeCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final String min = (String) metadata.getAllAnnotationAttributes(ConditionalOnBootVersionGe.class.getName()).getFirst("value");
        final String ver = SpringBootVersion.getVersion();

        if (min.compareTo(ver) <= 0) {
            return ConditionOutcome.match();
        } else {
            return ConditionOutcome.noMatch("Expected min version '" + min + "', but '" + ver + "'");
        }
    }

}

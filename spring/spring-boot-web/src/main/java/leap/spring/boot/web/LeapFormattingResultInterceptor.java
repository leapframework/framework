/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.spring.boot.web;

import leap.lang.Out;
import leap.lang.intercepting.State;
import leap.web.Response;
import leap.web.Result;
import leap.web.action.ActionContext;
import leap.web.action.FormattingResultInterceptor;
import org.springframework.http.ResponseEntity;

public class LeapFormattingResultInterceptor implements FormattingResultInterceptor {

    @Override
    public State preProcessReturnValue(ActionContext context, Result result, Out<Object> returnValue) throws Throwable {
        Object v = returnValue.get();

        if(v instanceof ResponseEntity) {
            ResponseEntity re = (ResponseEntity)v;

            result.setStatus(re.getStatusCodeValue());

            Response response = context.getResponse();
            re.getHeaders().forEach((name,values) -> {
                for(String value : values) {
                    response.addHeader(name, value);
                }
            });

            Object body = re.getBody();
            if(null == body) {
                return State.INTERCEPTED;
            }else {
                returnValue.set(body);
            }
        }

        return State.CONTINUE;
    }
}

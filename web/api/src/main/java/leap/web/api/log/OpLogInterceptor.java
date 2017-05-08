/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.web.api.log;

import leap.core.annotation.Inject;
import leap.core.validation.Validation;
import leap.lang.Classes;
import leap.lang.intercepting.State;
import leap.web.action.Action;
import leap.web.action.ActionContext;
import leap.web.action.ActionExecution;
import leap.web.action.ActionInterceptor;
import leap.web.api.annotation.NonOpLog;
import leap.web.api.annotation.OpLog;

import java.lang.annotation.Annotation;

/**
 * Created by kael on 2016/10/10.
 */
public class OpLogInterceptor implements ActionInterceptor {
    @Inject
    private OpLogManager manager;

    @Override
    public void completeExecuteAction(ActionContext context, Validation validation,
                                   ActionExecution execution) throws Throwable {
        Action action = context.getAction();
        if(action != null){
            OpLog controllerAnnotation = action.getControllerAnnotation(OpLog.class);
            OpLog actionAnnotation = Classes.getAnnotation(action.getAnnotations(), OpLog.class);
            NonOpLog nonOpLog = Classes.getAnnotation(action.getAnnotations(), NonOpLog.class);
            OpLog annotation = actionAnnotation==null?controllerAnnotation:actionAnnotation;
            if(annotation != null && nonOpLog == null){
                manager.saveLog(annotation,context,execution.getArgs());
            }
        }
    }

}

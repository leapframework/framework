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
package app.controllers;

import leap.lang.Assert;
import leap.web.annotation.QueryParam;
import leap.web.annotation.ArgumentsWrapper;
import leap.web.annotation.RequestBody;

import java.util.Map;

public class ArgumentsWrapperController {

    public void test(ReqBean bean) {
        Assert.notEmpty(bean.id);
        Assert.notEmpty(bean.name);
    }

    public void test0(ReqBean0 bean) {
        Assert.notEmpty(bean.id);
        Assert.notNull(bean.nested);
        Assert.notEmpty(bean.nested.name);
    }

    public void test1(@RequestBody ReqBean1 bean) {
        Assert.notEmpty(bean.id);
        Assert.notEmpty(bean.name);
    }

    public void test2(ReqBean2 bean) {
        Assert.notEmpty(bean.id);
        Assert.notEmpty(bean.name);
        Assert.isNull(bean.vars);
    }

    public void test4(IdBean id,NameBean name) {
        Assert.notEmpty(id.id);
        Assert.notEmpty(name.name);
    }

    public void test5(IdBean id,BodyBean body) {
        Assert.notEmpty(id.id);
        Assert.notEmpty(body.name);
    }

    public void test6(ReqBean4 bean) {
        Assert.notEmpty(bean.id);
        Assert.notEmpty(bean.name);
        Assert.notEmpty(bean.body.name);
    }

    public void test7(ReqBean5 bean) {
        Assert.notEmpty(bean.id);
        Assert.notEmpty(bean.name);
        Assert.notEmpty(bean.body.name);
        Assert.notEmpty(bean.params.get("name"));
    }

    @ArgumentsWrapper
    public static final class ReqBean {
        public @QueryParam  String id;
        public @RequestBody String name;
    }

    @ArgumentsWrapper
    public static final class ReqBean0 {
        public @QueryParam  String     id;
        public @RequestBody NestedBean nested;
    }

    public static final class NestedBean {
        public String name;
    }

    @ArgumentsWrapper
    public static final class ReqBean1 {
        public @QueryParam String id;
        public             String name;
    }

    @ArgumentsWrapper
    @RequestBody
    public static final class ReqBean2 {
        public String             id;
        public String             name;
        public Map<String,Object> vars;
    }

    @ArgumentsWrapper
    public static final class IdBean {
        public @QueryParam String id;
    }

    @ArgumentsWrapper
    public static final class NameBean {
        public @QueryParam String name;
    }

    @RequestBody
    public static final class BodyBean {
        public String name;
    }

    @ArgumentsWrapper.BodyParams
    public static final class BodyBean1 {
        public String name;
    }

    public static final class BodyBean2 {
        public String name;
    }

    @ArgumentsWrapper
    @RequestBody
    public static final class ReqBean4 {
        public String    id;
        public String    name;
        public BodyBean1 body;
    }

    @ArgumentsWrapper
    @RequestBody
    public static final class ReqBean5 {
        public                              String              id;
        public                              String              name;
        public @ArgumentsWrapper.BodyParams BodyBean2           body;
        public @ArgumentsWrapper.BodyParams Map<String, Object> params;
    }
}
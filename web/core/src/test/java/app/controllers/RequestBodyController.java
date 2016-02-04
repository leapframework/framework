/*
 * Copyright 2014 the original author or authors.
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

import app.models.products.Product;
import leap.lang.Assert;
import leap.lang.Charsets;
import leap.lang.codec.Hex;
import leap.lang.io.IO;
import leap.lang.json.JSON;
import leap.web.Request;
import leap.web.action.ControllerBase;
import leap.web.annotation.Arguments;
import leap.web.annotation.QueryParam;
import leap.web.annotation.RequestBody;

import java.io.IOException;
import java.io.InputStream;

public class RequestBodyController extends ControllerBase {
	
	public void index() {
		ok();
	}
	
	public String stringBody(@RequestBody String body) {
		return body;
	}
	
	public String stringBody1(@RequestBody String body,String p1) {
		return body + ":" + p1;
	}
	
	public String bytesBody(@RequestBody byte[] body) {
		return Hex.encode(body);
	}
	
	public String entityBody(Product product) {
		return JSON.encode(product);
	}

	public String peekInputStream(Request request) throws IOException{
        StringBuilder s = new StringBuilder();
        try(InputStream in = request.peekInputStream()){
            s.append(IO.readString(in, Charsets.defaultCharset()));
        }
        try(InputStream in = request.getInputStream()){
            s.append(IO.readString(in, Charsets.defaultCharset()));
        }
        return s.toString();
    }

    public void peekInputStreamErr(Request request) throws IOException {
        StringBuilder s = new StringBuilder();
        try(InputStream in = request.getInputStream()){
            s.append(IO.readString(in, Charsets.defaultCharset()));
        }
        try(InputStream in = request.peekInputStream()){
            s.append(IO.readString(in, Charsets.defaultCharset()));
        }
    }

    public void argsBody(ArgsBean bean) {
        Assert.notEmpty(bean.id);
        Assert.notEmpty(bean.name);
    }

    public void argsBody1(@RequestBody ArgsBean1 bean) {
        Assert.notEmpty(bean.id);
        Assert.notEmpty(bean.name);
    }

    @Arguments
    public static final class ArgsBean {
        public @QueryParam  String id;
        public @RequestBody String name;
    }

    @Arguments
    public static final class ArgsBean1 {
        public @QueryParam String id;
        public             String name;
    }
}
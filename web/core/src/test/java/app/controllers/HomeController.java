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
package app.controllers;

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.lang.Assert;
import leap.lang.New;
import leap.lang.http.HTTP;
import leap.web.App;
import leap.web.Content;
import leap.web.Contents;
import leap.web.Request;
import leap.web.Response;
import leap.web.Results;
import leap.web.annotation.HttpsOnly;
import leap.web.annotation.NonAction;
import leap.web.annotation.Path;
import leap.web.annotation.Produces;
import leap.web.annotation.http.*;
import leap.web.format.ResponseFormat;
import leap.web.view.ViewData;
import app.Global;
import app.beans.TestBean;
import app.beans.TestPrimaryBean;
import app.controllers.products.ProductController;

public class HomeController {
	
    public @Inject @M App               app;
    public @Inject @M Global            global;
    public @Inject @M ProductController productController;
    public @Inject @M TestBean          testBean1;

    private @Inject TestBean            testBean2;
    private @Inject TestPrimaryBean     testPrimaryBean;
	
	public void test_inject() {
		Assert.isTrue(testBean1 == testBean2);
		Assert.isTrue(app == testBean1.app);
		Assert.notNull(testPrimaryBean);
		Assert.isTrue(testBean1 == testPrimaryBean.getTestBean());		
	}
	
	/**
	 * View /WEB-INF/views/index.jsp
	 */
	public String index(){
		return "Hello world!";
	}
	
	/**
	 * No view , String -> text/plain
	 */
	public String text(){
		return "Hello world!";
	}
	
	public Content html(){
		return Contents.html("<h1>Hello world!</h1>");
	}
	
	/**
	 * Explicit annotated with response format 'html' will set the ContentTpe as 'text/html'
	 */
	@Produces("html")
	public String html1() {
		return "<h1>Hello world!</h1>";
	}
	
	/**
	 * Render as format 'html"
	 */
	public void html3(){
		Results.render("<h1>Hello world!</h1>",ResponseFormat.HTML);
	}
	
	/**
	 * Response format : text/html
	 */
	public void html2(){
		Results.render(Contents.html("<h1>Hello world!</h1>"));
	}
	
	@Produces("json")
	public String jsonString() {
		return "Hello json";
	}
	
	public void noContent(){
		Results.noContent();
	}
	
	public void notImplemented(){
		Results.notImplemented();
	}
	
	public void nothing(){
		Results.ok();
	}
	
	@POST
	public void postAction(){
		Results.text("METHOD:" + Request.current().getMethod());
	}
	
	public void rawResponse(){
		Request  request  = Request.current();
		Response response = request.response();
		
		response.setStatus(HTTP.SC_OK); //status must be setted
		response.setContentType("text/plain;charset=UTF-8");
		response.getWriter().write("RawResponse");
	}
	
	public void renderView(){
		Results.renderView("/test_render_view").setReturnValue("Hello world!");
	}
	
	public void redirectTo(){
		Results.redirect("^/redirect_to_notfound_url");
	}
	
	public void redirectTo1(){
		Results.redirect("/redirect_to_notfound_url");
	}
	
	public void redirectTo2(){
		Results.redirect("/redirect_to_notfound_url",New.hashMap("p","1"));
	}
	
	public String redirectTo3(){
		return "redirect:/redirect_to_notfound_url";
	}
	
	public void forwardTo(){
		Results.forward("views:/test_forward.jsp");
	}
	
	public void forwardTo1(){
		Results.forward("/test_forward.jsp");
	}
	
	public void exception1() {
		throw new RuntimeException("Test Exception"); 
	}
	
	public String controllerPath() {
		return Request.current().getActionContext().getRoute().getControllerPath();
	}
	
	@HttpsOnly
	public String httpsOnly() {
	    return "OK";
	}
	
	@NonAction
	public void nonAction() {
		
	}
	
	@Path("/arbitrary_path/{subPath:.*}")
	public String arbitraryPath(String subPath) {
		return subPath;
	}

    @GET("method_with_path")
	public void methodWithPathGet() {

    }

    @POST("method_with_path")
    public void methodWithPathPost() {

    }

    @PUT("method_with_path")
    public void methodWithPathPut() {

    }

    @DELETE("method_with_path")
    public void methodWithPathDelete() {

    }

    @HEAD("method_with_path")
    public void methodWithPathHead() {

    }

    @OPTIONS("method_with_path")
    public void methodWithPathOptions() {

    }
	
	public void jsp(ViewData vd) {
	    
	}

	public static final class NestedController {
        public void index() {

        }
    }
}
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

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import leap.lang.Strings;
import leap.web.annotation.Multipart;
import leap.web.annotation.Path;
import leap.web.multipart.MultipartFile;
import leap.web.multipart.Multiparts;

public class MultipartController {

	@Multipart
	public void upload0(HttpServletRequest req,HttpServletResponse resp) throws Throwable{
	    Collection<Part> parts = req.getParts();

	    StringBuilder sb = new StringBuilder();
	    
	    int i=0;
	    for(Part part : parts) {
	        if(i > 0) {
	            sb.append(',');   
	        }
	        sb.append(part.getName());
	        i++;
	    }
	    
	    resp.getWriter().write(sb.toString());
	}
	
	public String upload1(String s1,Part p1) {
		return s1 + " " + Multiparts.readString(p1);
	}
	
	public String uploadFile(MultipartFile file1) throws IOException {
		return file1.getOriginalFilename() + "!" + file1.getString();
	}
	@Path("{id}/upload")
	public String uploadFileWithExpressionPath(String id, MultipartFile file){
		if(Strings.isNotEmpty(id) && file != null){
			return "ok";
		}
		return "fail";
	}

}
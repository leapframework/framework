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
package leap.samples.petclinic.controllers;

import java.util.List;

import leap.lang.Strings;
import leap.samples.petclinic.models.Owner;
import leap.web.action.ControllerBase;
import leap.web.annotation.Index;
import leap.web.annotation.Path;
import leap.web.annotation.http.GET;
import leap.web.annotation.http.POST;

public class OwnersController extends ControllerBase {
	
	@GET
	@Index
	public void list(){
		setViewData("owners", Owner.all());
	}
	
	@GET
	public void new_(){
		renderView("newOrEdit","owner",new Owner());
	}
	
	@POST
	public void create(Owner owner){
		if(!validation().validate("owner", owner).hasErrors()){
			owner.create();
			redirect(Strings.format("/owners/{0}/show",owner.id()));
		}else{
			renderView("newOrEdit","owner",owner);
		}
	}
	
	@GET
	@Path("{id}/edit")
	public void edit(Integer id){
		setViewData("owner",Owner.find(id));
		renderView("/owners/newOrEdit");
	}
	
	@POST
	@Path("{id}/update")
	public void update(Owner owner){
		if(!validation()
			.validate("owner", owner)
			.hasErrors()){
			
			owner.update();
			redirect("/owners/list");
			
		}else{
			setViewData("owner",owner);
			renderView("/owners/newOrEdit");
		}
	}

	@Path("{id}/delete")
	public void delete(Integer id){
		Owner.delete(id);
		redirect("/owners/list");
	}
	
	@GET
	@Path("{id}/show")
	public void show(Integer id){
		Owner owner = Owner.find(id);
		setViewData("owner", owner);
	}
	
	@GET
	public void find(){
		setViewData("owner",new Owner());
	}

	@POST
	public void find(Owner owner){
		if(null == owner){
			redirect("list");
			return;
		}
		
		//Validation
		if(validation()
		   .required("lastName", owner.getLastName())
		   .hasErrors()){
			return;
		}
		
		//Query
		List<Owner> results = Owner.findAllByLastNameLike( owner.getLastName() + "%");
		if(results.isEmpty()){
			validation().addError("lastName", "notFound", "not found");
			renderView("find","owner", owner);
		}else if(results.size() > 1){
			renderView("list","owners",results);
		}else{
			redirect("/owners/" + results.get(0).getId() + "/show");
		}
	}
}
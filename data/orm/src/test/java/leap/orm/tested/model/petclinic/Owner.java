/*
 * Copyright 2002-2013 the original author or authors.
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
package leap.orm.tested.model.petclinic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import leap.lang.tostring.ToStringBuilder;
import leap.orm.annotation.Entity;
import leap.orm.tested.model.base.PersonBase;

/**
 * Simple JavaBean domain object representing an owner.
 */
@Entity(table="owners")
public class Owner extends PersonBase {

	private String address;

    private String city;

    //@Digits(fraction = 0, integer = 10)
    private String telephone;

    //@OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private Set<Pet> pets;
    
    @Override
    public Owner setFullName(String firstName, String lastName) {
	    return (Owner)super.setFullName(firstName, lastName);
    }

	public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    protected void setPetsInternal(Set<Pet> pets) {
        this.pets = pets;
    }

    protected Set<Pet> getPetsInternal() {
        if (this.pets == null) {
            this.pets = new HashSet<Pet>();
        }
        return this.pets;
    }

    public List<Pet> getPets() {
    	return new ArrayList<Pet>(getPetsInternal());
        //List<Pet> sortedPets = new ArrayList<Pet>(getPetsInternal());
        ///PropertyComparator.sort(sortedPets, new MutableSortDefinition("name", true, true));
        //return Collections.unmodifiableList(sortedPets);
    }

    public void addPet(Pet pet) {
        getPetsInternal().add(pet);
        pet.setOwner(this);
    }

    /**
     * Return the Pet with the given name, or null if none found for this Owner.
     *
     * @param name to test
     * @return true if pet name is already in use
     */
    public Pet getPet(String name) {
        return getPet(name, false);
    }

    /**
     * Return the Pet with the given name, or null if none found for this Owner.
     *
     * @param name to test
     * @return true if pet name is already in use
     */
    public Pet getPet(String name, boolean ignoreNew) {
        name = name.toLowerCase();
        for (Pet pet : getPetsInternal()) {
            if (!ignoreNew || !pet.isNew()) {
                String compName = pet.getName();
                compName = compName.toLowerCase();
                if (compName.equals(name)) {
                    return pet;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
	                .append("id", this.getId())
	                .append("new", this.isNew())
	                .append("lastName", this.getLastName())
	                .append("firstName", this.getFirstName())
	                .append("address", this.address)
	                .append("city", this.city)
	                .append("telephone", this.telephone)
	                .toString();
    }
}

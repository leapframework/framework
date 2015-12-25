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

import leap.orm.annotation.Entity;
import leap.orm.tested.model.base.PersonBase;

/**
 * Simple JavaBean domain object representing a veterinarian.
 */
@Entity(table="vets")
public class Vet extends PersonBase {

    //@ManyToMany(fetch = FetchType.EAGER)
    //@JoinTable(name = "vet_specialties", joinColumns = @JoinColumn(name = "vet_id"),
    //        inverseJoinColumns = @JoinColumn(name = "specialty_id"))
    private Set<Specialty> specialties;

    protected void setSpecialtiesInternal(Set<Specialty> specialties) {
        this.specialties = specialties;
    }

    protected Set<Specialty> getSpecialtiesInternal() {
        if (this.specialties == null) {
            this.specialties = new HashSet<Specialty>();
        }
        return this.specialties;
    }

    public List<Specialty> getSpecialties() {
    	return new ArrayList<Specialty>(getSpecialtiesInternal());
//        List<Specialty> sortedSpecs = new ArrayList<Specialty>(getSpecialtiesInternal());
//        PropertyComparator.sort(sortedSpecs, new MutableSortDefinition("name", true, true));
//        return Collections.unmodifiableList(sortedSpecs);
    }

    public int getNrOfSpecialties() {
        return getSpecialtiesInternal().size();
    }

    public void addSpecialty(Specialty specialty) {
        getSpecialtiesInternal().add(specialty);
    }
}

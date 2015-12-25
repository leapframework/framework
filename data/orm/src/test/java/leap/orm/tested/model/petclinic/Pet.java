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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import leap.orm.annotation.Entity;
import leap.orm.annotation.ManyToOne;

/**
 * Simple business object representing a pet.
 */
@Entity(table="pets")
public class Pet extends NamedEntity {

    //@DateTimeFormat(pattern = "yyyy/MM/dd")
    private Date birthDate;

    //@ManyToOne
    //@JoinColumn(name = "type_id")
    private PetType type;

    @ManyToOne
    //@JoinColumn(name = "owner_id")
    private Owner owner;

    //@OneToMany(cascade = CascadeType.ALL, mappedBy = "pet", fetch = FetchType.EAGER)
    private Set<Visit> visits;

    public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public void setType(PetType type) {
        this.type = type;
    }

    public PetType getType() {
        return this.type;
    }

    protected void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Owner getOwner() {
        return this.owner;
    }

    protected void setVisitsInternal(Set<Visit> visits) {
        this.visits = visits;
    }

    protected Set<Visit> getVisitsInternal() {
        if (this.visits == null) {
            this.visits = new HashSet<Visit>();
        }
        return this.visits;
    }

    public List<Visit> getVisits() {
    	return new ArrayList<Visit>(getVisitsInternal());
        //List<Visit> sortedVisits = new ArrayList<Visit>(getVisitsInternal());
        //PropertyComparator.sort(sortedVisits, new MutableSortDefinition("date", false, false));
        //return Collections.unmodifiableList(sortedVisits);
    }

    public void addVisit(Visit visit) {
        getVisitsInternal().add(visit);
        visit.setPet(this);
    }

}

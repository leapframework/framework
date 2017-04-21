/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package app.models.testing;

import leap.orm.annotation.Column;
import leap.orm.annotation.Id;
import leap.orm.annotation.ManyToMany;
import leap.orm.annotation.Relational;
import leap.orm.model.Model;

import java.util.List;

@ManyToMany(target = ManyToManyModel2.class, joinEntityType = ManyToManyJoin1.class)
public class ManyToManyModel1 extends Model {

    @Id
    public String id;

    @Column
    public String col;

    @Relational
    public List<ManyToManyModel2> model2List;

}
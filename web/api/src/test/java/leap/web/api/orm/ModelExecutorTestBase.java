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

package leap.web.api.orm;

import app.models.Author;
import app.models.Book;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.core.junit.AppTestBase;
import leap.core.meta.MTypeManager;
import leap.lang.meta.MComplexType;
import leap.lang.meta.MComplexTypeRef;
import leap.lang.meta.MType;
import leap.orm.dao.Dao;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.DefaultApiConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiModelBuilder;

public abstract class ModelExecutorTestBase extends AppTestBase implements PostCreateBean {

    protected @Inject MTypeManager tm;
    protected @Inject Dao          dao;

    protected ApiConfig   ac;
    protected ApiMetadata amd;

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        ac = factory.inject(new DefaultApiConfig("api", "/", ""));

        ApiMetadataBuilder m = new ApiMetadataBuilder();
        m.setName("api");
        m.addModel(am(Book.class));
        m.addModel(am(Author.class));

        amd = m.build();
    }

    /**
     * Returns the {@link MApiModel} for orm model.
     */
    protected MApiModelBuilder am(Class<?> ormModel) {
        MType type = tm.getMType(ormModel);

        MComplexType ct;
        if(type instanceof MComplexTypeRef) {
            ct = tm.getComplexType(((MComplexTypeRef) type).getRefTypeName());
        }else{
            ct = (MComplexType)type;
        }

        return new MApiModelBuilder(ct);
    }
}

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
package leap.orm.command;

import leap.db.Db;
import leap.lang.Assert;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.dmo.Dmo;
import leap.orm.metadata.OrmMetadataManager;

public abstract class AbstractDmoCommand implements DmoCommand {
	
	protected final Dmo                dmo;
	protected final OrmContext         context;
	protected final Db			       db;
	protected final OrmMetadata        metadata;
	protected final OrmMetadataManager metadataManager;
	
	private Boolean success	= null; 	
	
	protected AbstractDmoCommand(Dmo dmo){
		this.dmo      = dmo;
		this.context  = dmo.getOrmContext();
		this.db       = context.getDb();
		this.metadata = context.getMetadata();
		this.metadataManager = context.getMetadataManager();
	}

	@Override
    public boolean executed() {
	    return success != null;
    }
	
	@Override
    public boolean success() throws IllegalStateException {
		Assert.isTrue(null != success,"command must be executed for getting the 'success' state");
	    return success;
    }

	@Override
    public final boolean execute() {
		success = doExecute();
	    return success;
    }
	
	protected abstract boolean doExecute();
}
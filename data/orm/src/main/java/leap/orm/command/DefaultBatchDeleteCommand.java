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

import leap.lang.params.Params;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.sql.SqlCommand;

public class DefaultBatchDeleteCommand extends AbstractEntityDaoCommand implements BatchDeleteCommand {
	
	protected final Object[]	  idObjectArray;
	protected final SqlCommand    sqlCommand;
	protected final Params[]  idParameters;
	
	public DefaultBatchDeleteCommand(Dao dao,EntityMapping em,Object[] ids) {
	    super(dao,em);
	    this.idObjectArray = ids;//getObjectArray(ids);
	    this.sqlCommand    = metadata.getSqlCommand(em.getEntityName(), SqlCommand.DELETE_COMMAND_NAME);
	    this.idParameters  = new Params[idObjectArray.length];
	    
	    for(int i=0;i<idObjectArray.length;i++){
	    	idParameters[i] = context.getParameterStrategy().createIdParameters(context, em, idObjectArray[i]);
	    }
    }

	@Override
	public int[] execute() {
		return sqlCommand.executeBatchUpdate(this, idParameters);
	}
	
	/*
	protected Object[] getObjectArray(Object[] ids) {
		if(ids.length == 1) {
			Object o1 = ids[0];
			if(o1.getClass().isArray()){
				return Objects2.toObjectArray(o1);
			}
		}
		return ids;
	}
	*/

}

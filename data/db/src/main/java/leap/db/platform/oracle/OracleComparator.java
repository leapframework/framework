/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package leap.db.platform.oracle;

import leap.db.model.DbColumn;
import leap.db.platform.GenericDbComparator;

/**
 * Created by kael on 2017/2/13.
 */
public class OracleComparator extends GenericDbComparator {
    
    public static final String INTEGER = "integer";
    public static final String BIG_INT = "bigint";
    public static final String INTEGER_TYPE = "number(10,0)";
    public static final String BIG_INT_TYPE = "number(19,0)";
    
    @Override
    protected boolean compareColumnTypeDefinition(DbColumn sourceColumn, String sourceTypeDef, DbColumn targetColumn,
                                                  String targetTypeDef) {
        if(INTEGER.equalsIgnoreCase(sourceTypeDef)){
            sourceTypeDef = INTEGER_TYPE;
        }
        if(INTEGER.equalsIgnoreCase(targetTypeDef)){
            targetTypeDef = INTEGER_TYPE;
        }
        
        return super.compareColumnTypeDefinition(sourceColumn, sourceTypeDef, targetColumn, targetTypeDef);
    }
}

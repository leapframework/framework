/*
 *
 *  * Copyright 2016 the original author or authors.
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

package leap.core.ds.management;

import leap.lang.Named;

/**
 * The management interface of {@link javax.sql.DataSource}.
 */
public interface MDataSource extends Named {

    /**
     * Returns the mbean instance of this DataSource.
     */
    Object getMBean();

    /**
     * Returns all the current opened connections.
     */
    MConnection[] getActiveConnections();

    /**
     * Returns the last reported slow sqls.
     */
    MSlowSql[] getSlowSqls();

    /**
     * Clear all slow sqls an return the number of cleared.
     */
    int clearSlowSqls();

    /**
     * Returns the last reported very slow sqls.
     */
    MSlowSql[] getVerySlowSqls();

    /**
     * Clear all very slow sqls an return the number of cleared.
     */
    int clearVerySlowSqls();
}
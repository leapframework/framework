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
package leap.orm.df;

import java.util.List;

import leap.lang.Randoms;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;

public class DomainDataGenerator implements DataGenerator {

	@Override
	public Object generateValue(DataGeneratorContext context, EntityMapping em, FieldMapping fm) {
		DomainData data = context.getDomainDatas().tryGetDomainData(fm.getDomain());

		if(data.isMultiColumns()){
			throw new UnsupportedOperationException("Multi-values domain data not supported currently");
		}
		
		List<Object[]> rows = data.rows();
		
		if(rows.isEmpty()){
			return null;
		}
		
		return rows.get(Randoms.nextInt(rows.size() - 1))[0];
	}

}
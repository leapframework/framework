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
package leap.orm.generator;

import java.sql.Types;

import leap.core.annotation.Inject;
import leap.core.validation.annotations.NotNull;
import leap.db.Db;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.annotation.Sequence;
import leap.orm.mapping.EntityMappingBuilder;
import leap.orm.mapping.FieldMappingBuilder;
import leap.orm.mapping.SequenceMappingBuilder;
import leap.orm.metadata.MetadataContext;

public class AutoIdGenerator implements IdGenerator {
	
	private static final Log log = LogFactory.get(AutoIdGenerator.class);
	
	protected @NotNull ValueGenerator uuidGenerator;
	
	protected int uuidLength = 38;
	protected int minUuidLength = 36;
	
	@Inject(name="uuid")
	public void setUuidGenerator(ValueGenerator uuidGenerator) {
		this.uuidGenerator = uuidGenerator;
	}
	
	public void setUuidLength(int uuidLength) {
		this.uuidLength = uuidLength;
	}

    @Override
    public Integer getDefaultColumnLength() {
        return uuidLength;
    }

    @Override
    public void mapping(MetadataContext context, EntityMappingBuilder emb, FieldMappingBuilder fmb) {
		Db db = context.getDb();
		if(null == db) {
			return;
		}
		
		//smallint, integer or big integer type for sequence , identity or table generator 
		if(isIntegerType(fmb)){
			
			if(db.getDialect().supportsAutoIncrement()){
				mappingAutoIncrement(context, emb, fmb);
			}else if(db.getDialect().supportsSequence()){
				mappingSequence(context, emb, fmb);
			}else{
				//TODO : table sequence generator
				throw new IllegalStateException("db '" + db.getDescription() + "' not supports identity and sequence, can not use autoid");
			}
			
			return ;
		}
		
		//varchar type for guid generator
		if(isGuidType(fmb)){
			mappingUUID(context, emb, fmb);
		}
    }
	
	public void mappingAutoIncrement(MetadataContext context, EntityMappingBuilder emb, FieldMappingBuilder fmb){
		fmb.getColumn().setAutoIncrement(true);

		emb.setInsertInterceptor(context1 -> {
            if(!context1.isReturnGeneratedId()){
                return null;
            }

            return context1.getOrmContext().getDb().getDialect().getAutoIncrementIdHandler(input -> context1.setGeneratedId(input));
        });
	}
	
	public void mappingSequence(MetadataContext context, EntityMappingBuilder emb,final FieldMappingBuilder fmb){
		SequenceMappingBuilder seq = new SequenceMappingBuilder();
		
		setSequenceProperties(context, emb, fmb, seq);
		
		fmb.setSequenceName(seq.getName());
		
		if(null == context.getMetadata().tryGetSequenceMapping(seq.getName())){
			context.getMetadata().addSequenceMapping(seq.build());
		}else{
			log.info("Sequence '{}' already exists, skip adding it into the metadata",seq.getName());
		}
		
		emb.setInsertInterceptor(context1 -> {
            if(!context1.isReturnGeneratedId()){
                return null;
            }
            return context1.getOrmContext().getDb().getDialect()
                          .getInsertedSequenceValueHandler(fmb.getSequenceName(),
                                  input -> context1.setGeneratedId(input));
        });
	}
	
	protected void mappingUUID(MetadataContext context, EntityMappingBuilder emb, FieldMappingBuilder fmb){
		fmb.setInsertValue(uuidGenerator);
		int length = fmb.getColumn().getLength() == null? uuidLength : fmb.getColumn().getLength();
		if(length < minUuidLength){
			String warnInfo = "the column of field `"+fmb.getFieldName()+"` in "+emb.getEntityName()+" is an uuid field, it's length must >= " + minUuidLength;
			log.warn(warnInfo);
		}
		fmb.getColumn().setLength(length);
	}
	
	protected void setSequenceProperties(MetadataContext context,
										 EntityMappingBuilder emb,FieldMappingBuilder fmb,SequenceMappingBuilder seq){
	
		Sequence a = fmb.getBeanProperty() != null ? fmb.getBeanProperty().getAnnotation(Sequence.class) : null;
		if(null != a){
			seq.setName(Strings.firstNotEmpty(a.name(),a.value()));
			
			if(a.start() != Long.MAX_VALUE){
				seq.setStart(a.start());
			}
			
			if(a.increment() != Integer.MIN_VALUE){
				seq.setIncrement(a.increment());
			}
			
			if(a.cache() != Integer.MIN_VALUE){
				seq.setCache(a.cache());
			}
		}
		
		seq.setSchema(emb.getTableSchema());
		
		if(Strings.isEmpty(seq.getName())){
			seq.setName(context.getNamingStrategy().generateSequenceName(emb.getTableName(), fmb.getColumn().getName()));
		}
	}
	
	protected boolean isIntegerType(FieldMappingBuilder fmb) {
        if(null != fmb.getJavaType()) {
            Class<?> type = fmb.getJavaType();

            if(Classes.isInteger(type) || Classes.isBigInteger(type)) {
                return true;
            }
        }

        if(null != fmb.getColumn().getTypeCode()) {
            int typeCode = fmb.getColumn().getTypeCode();

            if (Types.SMALLINT == typeCode || Types.INTEGER == typeCode || Types.BIGINT == typeCode) {
                return true;
            }
        }
		
		return false;
	}
	
	protected boolean isGuidType(FieldMappingBuilder fmb){
		if(null != fmb.getJavaType()){
			return fmb.getJavaType().equals(String.class);
		}

        if(null != fmb.getColumn().getTypeCode()) {
            if (fmb.getColumn().getTypeCode() == Types.VARCHAR) {
                return true;
            }
        }
		
		return false;
	}
}
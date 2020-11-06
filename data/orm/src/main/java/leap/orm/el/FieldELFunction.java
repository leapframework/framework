package leap.orm.el;

import leap.lang.el.ElEvalContext;
import leap.orm.mapping.FieldMapping;
import java.util.Map;

public class FieldELFunction {

    public static boolean isChanged(ElEvalContext context, String field) {
        Object value  = context.resolveVariable(field);
        Object record = context.resolveVariable("$record");
        Object origin = null;
        if (record instanceof Map) {
            origin = ((Map) record).get(field);
        }

        if (null == value && null == origin) {
            return false;
        }

        if (null != value) {
            if (null == origin) {
                return true;
            }

            FieldMapping[] fms = null;
            Object fmsObj = context.resolveVariable("$fms");
            if (fmsObj instanceof FieldMapping[]) {
                fms = (FieldMapping[]) fmsObj;
            }
            if (null != fms) {
                for (FieldMapping fm : fms) {
                    if (fm.getFieldName().equalsIgnoreCase(field)) {
                        if (null != fm.getSerializer()) {
                            return !fm.getSerializer().matches(fm, value, origin);
                        }
                        break;
                    }
                }
            }

            return !value.equals(origin);
        }

        return true;
    }

}
package leap.lang.meta;

import leap.lang.Enums;
import leap.lang.beans.BeanProperty;
import leap.lang.meta.annotation.*;

public abstract class AbstractMTypeFactory implements MTypeFactory {

    protected final void configureProperty(BeanProperty bp, MPropertyBuilder mp) {

        if(null == mp.getEnumValues() && bp.getType().isEnum()) {
            mp.setEnumValues(Enums.getValues(bp.getType()));
        }

        Property p = bp.getAnnotation(Property.class);
        if(null != p) {
            if(p.required().isPresent()) {
                mp.setRequired(p.required().value());
            }

            if(p.creatable().isPresent()) {
                mp.setCreatable(p.creatable().value());
            }

            if(p.updatable().isPresent()) {
                mp.setUpdatable(p.updatable().value());
            }

            if(p.filterable().isPresent()) {
                mp.setFilterable(p.filterable().value());
            }

            if(p.sortable().isPresent()) {
                mp.setSortable(p.sortable().value());
            }
        }

        Sortable sortable = bp.getAnnotation(Sortable.class);
        if(null != sortable) {
            mp.setSortable(sortable.value());
        }

        Filterable filterable = bp.getAnnotation(Filterable.class);
        if(null != filterable) {
            mp.setFilterable(filterable.value());
        }

        Creatable creatable = bp.getAnnotation(Creatable.class);
        if(null != creatable) {
            mp.setCreatable(creatable.value());
        }

        Updatable updatable = bp.getAnnotation(Updatable.class);
        if(null != updatable) {
            mp.setUpdatable(updatable.value());
        }


    }

}
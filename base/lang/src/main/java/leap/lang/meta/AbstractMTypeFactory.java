package leap.lang.meta;

import leap.lang.Classes;
import leap.lang.Enums;
import leap.lang.beans.BeanProperty;
import leap.lang.meta.annotation.*;

import java.lang.annotation.Annotation;

public abstract class AbstractMTypeFactory implements MTypeFactory {

    protected final void configureProperty(BeanProperty bp, MPropertyBuilder mp) {

        if(null == mp.getEnumValues() && bp.getType().isEnum()) {
            mp.setEnumValues(Enums.getValues(bp.getType()));
        }

        Annotation[] annotations = bp.getAnnotations();

        Property p = Classes.getAnnotation(annotations, Property.class, true);
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

        Sortable sortable = Classes.getAnnotation(annotations, Sortable.class, true);
        if(null != sortable) {
            mp.setSortable(sortable.value());
        }

        Filterable filterable = Classes.getAnnotation(annotations, Filterable.class, true);
        if(null != filterable) {
            mp.setFilterable(filterable.value());
        }

        Creatable creatable = Classes.getAnnotation(annotations, Creatable.class, true);
        if(null != creatable) {
            mp.setCreatable(creatable.value());
        }

        Updatable updatable = Classes.getAnnotation(annotations, Updatable.class, true);
        if(null != updatable) {
            mp.setUpdatable(updatable.value());
        }


    }

}
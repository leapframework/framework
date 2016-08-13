package leap.lang.meta;

import leap.lang.Enums;
import leap.lang.beans.BeanProperty;
import leap.lang.meta.annotation.UserCreatable;
import leap.lang.meta.annotation.UserFilterable;
import leap.lang.meta.annotation.UserSortable;
import leap.lang.meta.annotation.UserUpdatable;

public abstract class AbstractMTypeFactory implements MTypeFactory {

    protected final void configureProperty(BeanProperty bp, MPropertyBuilder mp) {

        if(null == mp.getEnumValues() && bp.getType().isEnum()) {
            mp.setEnumValues(Enums.getValues(bp.getType()));
        }

        UserSortable sortable = bp.getAnnotation(UserSortable.class);
        if(null != sortable) {
            mp.setUserSortable(sortable.value());
        }

        UserFilterable filterable = bp.getAnnotation(UserFilterable.class);
        if(null != filterable) {
            mp.setUserFilterable(filterable.value());
        }

        UserCreatable creatable = bp.getAnnotation(UserCreatable.class);
        if(null != creatable) {
            mp.setUserCreatable(creatable.value());
        }

        UserUpdatable updatable = bp.getAnnotation(UserUpdatable.class);
        if(null != updatable) {
            mp.setUserUpdatable(updatable.value());
        }

    }

}
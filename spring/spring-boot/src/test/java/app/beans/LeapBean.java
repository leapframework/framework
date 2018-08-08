package app.beans;

import leap.core.annotation.Inject;

import java.util.List;

public class LeapBean implements ListType {

    @Inject
    protected SpringBean springBean;

    @Inject
    protected List<ListType> beans;

    public SpringBean getSpringBean() {
        return springBean;
    }

    public void setSpringBean(SpringBean springBean) {
        this.springBean = springBean;
    }

    public List<ListType> getBeans() {
        return beans;
    }

    public void setBeans(List<ListType> beans) {
        this.beans = beans;
    }
}

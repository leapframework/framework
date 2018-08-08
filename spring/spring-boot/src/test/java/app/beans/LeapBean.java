package app.beans;

import leap.core.annotation.Inject;

public class LeapBean {

    @Inject
    protected SpringBean springBean;

    public SpringBean getSpringBean() {
        return springBean;
    }

    public void setSpringBean(SpringBean springBean) {
        this.springBean = springBean;
    }

}

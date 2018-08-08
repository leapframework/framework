package app.beans;

import org.springframework.beans.factory.annotation.Autowired;

public class SpringBean implements ListType{

    @Autowired
    protected LeapBean leapBean;

    public LeapBean getLeapBean() {
        return leapBean;
    }

    public void setLeapBean(LeapBean leapBean) {
        this.leapBean = leapBean;
    }
}

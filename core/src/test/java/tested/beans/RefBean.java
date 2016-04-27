package tested.beans;

/**
 * Created by KAEL on 2016/4/27.
 */
public class RefBean {
    private String name;
    private RefBean refBean;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RefBean getRefBean() {
        return refBean;
    }

    public void setRefBean(RefBean refBean) {
        this.refBean = refBean;
    }
}

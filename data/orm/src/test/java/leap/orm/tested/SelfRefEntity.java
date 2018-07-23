package leap.orm.tested;

import leap.orm.annotation.Column;
import leap.orm.annotation.Id;
import leap.orm.annotation.ManyToOne;

public class SelfRefEntity {

    @Id
    protected String id;

    @Column
    @ManyToOne(target = SelfRefEntity.class)
    protected String parentId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}

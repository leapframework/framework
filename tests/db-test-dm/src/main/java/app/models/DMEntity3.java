package app.models;

import leap.core.validation.annotations.Required;
import leap.lang.enums.Bool;
import leap.orm.annotation.*;
import leap.orm.model.Model;

@Entity
@DataSource("dm")
public class DMEntity3 extends Model {

    @Id
    private String id;

    @Column
    @Required
    @ManyToOne(DMEntity1.class)
    private String entity1Id;

    @Column
    @ManyToOne(value = DMEntity2.class, optional = Bool.TRUE)
    private String entity2Id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntity1Id() {
        return entity1Id;
    }

    public void setEntity1Id(String entity1Id) {
        this.entity1Id = entity1Id;
    }

    public String getEntity2Id() {
        return entity2Id;
    }

    public void setEntity2Id(String entity2Id) {
        this.entity2Id = entity2Id;
    }
}

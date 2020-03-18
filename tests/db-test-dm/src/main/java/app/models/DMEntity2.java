package app.models;

import leap.orm.annotation.Column;
import leap.orm.annotation.DataSource;
import leap.orm.annotation.Entity;
import leap.orm.annotation.Id;
import leap.orm.model.Model;

@Entity
@DataSource("dm")
public class DMEntity2 extends Model {

    @Id
    private String id;

    @Column
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

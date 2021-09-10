package app.models;

import leap.orm.annotation.DataSource;
import leap.orm.annotation.Entity;
import leap.orm.annotation.Id;
import leap.orm.model.Model;

@Entity(schema = "TEST")
@DataSource("dm")
public class DMSchemaEntity1 extends Model {

    @Id
    private String id;

    public DMSchemaEntity1() {
    }

    public DMSchemaEntity1(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
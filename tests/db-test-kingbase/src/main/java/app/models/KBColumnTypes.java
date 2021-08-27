package app.models;

import leap.orm.annotation.Column;
import leap.orm.annotation.DataSource;
import leap.orm.annotation.Id;
import leap.orm.annotation.Table;
import leap.orm.annotation.domain.CreatedAt;
import leap.orm.model.Model;
import java.util.Date;
import java.util.Map;

@Table
@DataSource("kingbase")
public class KBColumnTypes extends Model {

    @Id(generator = "shortid")
    private String id;

    @Column
    private Integer number;

    @Column
    @CreatedAt
    private Date createdAt;

    @Column
    private Boolean enabled;

    @Column
    private Map<String, Object> map;

    @Column
    private KBEntity1 kbEntity1;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public KBEntity1 getKbEntity1() {
        return kbEntity1;
    }

    public void setKbEntity1(KBEntity1 kbEntity1) {
        this.kbEntity1 = kbEntity1;
    }
}

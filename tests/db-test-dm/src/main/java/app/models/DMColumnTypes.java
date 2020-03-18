package app.models;

import leap.orm.annotation.*;
import leap.orm.annotation.domain.CreatedAt;
import leap.orm.model.Model;

import java.util.Date;
import java.util.Map;

@Table
@DataSource("dm")
public class DMColumnTypes extends Model {

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
    private DMEntity1 dmEntity1;

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

    public DMEntity1 getDmEntity1() {
        return dmEntity1;
    }

    public void setDmEntity1(DMEntity1 dmEntity1) {
        this.dmEntity1 = dmEntity1;
    }
}

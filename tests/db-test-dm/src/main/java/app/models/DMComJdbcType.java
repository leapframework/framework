package app.models;

import leap.orm.annotation.*;
import leap.orm.annotation.domain.CreatedAt;
import leap.orm.model.Model;

import java.util.Date;

@Table
@DataSource("dm")
public class DMComJdbcType extends Model {

    @Id(generator = "shortid")
    private String id;

    @Column
    private Integer number;

    @Column
    @CreatedAt
    private Date createdAt;

    @Column
    private Boolean enabled;

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
}

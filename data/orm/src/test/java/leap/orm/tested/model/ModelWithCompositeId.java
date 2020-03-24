package leap.orm.tested.model;

import leap.orm.annotation.Column;
import leap.orm.annotation.Id;
import leap.orm.model.Model;

public class ModelWithCompositeId extends Model {

    @Id
    @Column
    private String taskId;

    @Id
    @Column
    private String projectId;

    @Column
    private String id;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

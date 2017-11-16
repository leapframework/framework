package app.models;

import leap.orm.annotation.Column;
import leap.orm.annotation.DataSource;
import leap.orm.annotation.Entity;
import leap.orm.annotation.Id;

@Entity
@DataSource("db2")
public class Entity3 {

	@Id
	private String id;

    @Column
	private String field1;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}
}

package app.models;

import leap.lang.meta.annotation.Filterable;
import leap.orm.annotation.Column;
import leap.orm.annotation.Id;
import leap.orm.annotation.RestEntity;

/**
 * 通过restful方式对Entity4进行操作
 * @author 梁生
 *
 */
@RestEntity(path = "entity4", dataSource = "restapi1")
public class RemoteEntity {

	@Id
	protected String id;

	@Column
	@Filterable
	private String name;

	@Column
	@Filterable
	private String title;

	@Column
	protected String field1;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}

package app.models;

import leap.orm.annotation.AutoCreateTable;
import leap.orm.annotation.Id;
import leap.orm.model.Model;

@AutoCreateTable
public class Tag extends Model {

	@Id(generator = "shortid")
	private String id;

	private String title;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}

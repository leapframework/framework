package app.models;

import leap.orm.annotation.AutoCreateTable;
import leap.orm.annotation.Id;
import leap.orm.annotation.ManyToOne;
import leap.orm.annotation.Unique;
import leap.orm.model.Model;

@AutoCreateTable
public class BookTag extends Model {

    @Id(generator = "shortid")
    private String id;

    @ManyToOne(target = Book.class)
    @Unique("bookTag")
    private String bookId;

    @ManyToOne(target = Tag.class)
    @Unique("bookTag")
    private String tagId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

}
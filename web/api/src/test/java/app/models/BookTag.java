package app.models;

import leap.orm.annotation.AutoCreateTable;
import leap.orm.annotation.Id;
import leap.orm.annotation.ManyToOne;
import leap.orm.model.Model;

@AutoCreateTable
public class BookTag extends Model {

    @Id
    @ManyToOne(target = Book.class)
    private String bookId;

    @Id
    @ManyToOne(target = Tag.class)
    private String tagId;

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
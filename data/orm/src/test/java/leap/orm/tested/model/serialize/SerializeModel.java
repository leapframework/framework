package leap.orm.tested.model.serialize;

import leap.orm.annotation.Column;
import leap.orm.annotation.Id;
import leap.orm.model.Model;

import java.util.Map;

public class SerializeModel extends Model {

    @Id
    @Column
    protected String id;

    @Column
    protected String name;

    @Column
    protected String[] stringArray;

    @Column
    protected int[] intArray;

    @Column
    protected Integer[] integerArray;

    @Column
    protected Map<String,Object> nestMap;

    @Column
    protected NestedBean nestedBean;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getStringArray() {
        return stringArray;
    }

    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public void setIntArray(int[] intArray) {
        this.intArray = intArray;
    }

    public Integer[] getIntegerArray() {
        return integerArray;
    }

    public void setIntegerArray(Integer[] integerArray) {
        this.integerArray = integerArray;
    }

    public Map<String, Object> getNestMap() {
        return nestMap;
    }

    public void setNestMap(Map<String, Object> nestMap) {
        this.nestMap = nestMap;
    }

    public NestedBean getNestedBean() {
        return nestedBean;
    }

    public void setNestedBean(NestedBean nestedBean) {
        this.nestedBean = nestedBean;
    }

}
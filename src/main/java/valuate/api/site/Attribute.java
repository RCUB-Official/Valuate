package valuate.api.site;

public class Attribute {

    private final String id;
    private String value;

    Attribute(String id, String value) {
        this.id = id;
        this.value = value;
    }

    Attribute(String id) {
        this.id = id;
        this.value = null;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return id + ":" + value;
    }

}

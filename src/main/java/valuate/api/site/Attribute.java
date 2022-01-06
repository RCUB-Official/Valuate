package valuate.api.site;

import java.io.Serializable;

public class Attribute implements Serializable {

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

    public boolean isEmpty() {
        return ("".equals(value) || value == null);
    }

    @Override
    public String toString() {
        if (!isEmpty()) {
            return value;
        } else {
            return id;
        }
    }

}

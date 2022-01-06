package valuate.api.attribute;

import java.io.Serializable;

public class Attribute implements Serializable {

    private final String fieldId;
    private String value;

    public Attribute(String fieldId, String value) {
        this.fieldId = fieldId;
        this.value = value;
    }

    public Attribute(String id) {
        this.fieldId = id;
        this.value = null;
    }

    public String getFieldId() {
        return fieldId;
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
            return fieldId;
        }
    }

}

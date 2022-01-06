package valuate.api.feedback;

public final class AttributeField {

    private final String fieldId;
    private boolean inSnippetEditor;
    private boolean providedByFeedback;
    private boolean mandatory;
    private String defaultValue;
    private String adminNote;

    AttributeField(String fieldId, boolean inSnippetEditor, boolean providedByFeedback, boolean mandatory, String defaultValue, String adminNote) {
        this.fieldId = fieldId;
        this.inSnippetEditor = inSnippetEditor;
        this.providedByFeedback = providedByFeedback;
        this.mandatory = mandatory;
        this.defaultValue = defaultValue;
        this.adminNote = adminNote;
    }

    public String getFieldId() {
        return fieldId;
    }

    public boolean isInSnippetEditor() {
        return inSnippetEditor;
    }

    public void setInSnippetEditor(boolean inSnippetEditor) {
        this.inSnippetEditor = inSnippetEditor;
    }

    public boolean isProvidedByFeedback() {
        return providedByFeedback;
    }

    public void setProvidedByFeedback(boolean providedByFeedback) {
        this.providedByFeedback = providedByFeedback;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    @Override
    public String toString() {
        return fieldId;
    }
}

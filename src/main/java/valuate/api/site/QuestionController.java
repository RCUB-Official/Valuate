package valuate.api.site;

import framework.utilities.HashCalculator;
import framework.utilities.Utilities;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name = "questionController", eager = true)
@ViewScoped
public class QuestionController implements Serializable {

    private static final Logger LOG = Logger.getLogger(QuestionController.class.getName());

    private long siteId;    // Must be parsed from the GET parameter
    private String questionId;
    private String questionText;

    private Question question = null;

    public QuestionController() {
        questionId = "";
        questionText = "";
    }

    @PostConstruct
    public void init() {
        try {
            siteId = Long.parseLong((String) Utilities.getObject("#{param.site_id}"));
            questionId = (String) Utilities.getObject("#{param.question_id}");
            question = QuestionServer.getQuestion(siteId, questionId);
        } catch (NumberFormatException | NullPointerException ex) {
            LOG.log(Level.INFO, "Site not found ({0}, \"{1}\")", new Object[]{siteId, questionId});
        }
    }

    public Question getQuestion() {
        return question;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void addNewQuestion() {
        // TODO: md5 ako je prazno pitanje
        if ("".equals(questionId)) {
            questionId = HashCalculator.md5(questionText);
        }

        QuestionServer.addQuestion(siteId, questionId, questionText);
        questionId = "";
        questionText = "";
    }

    public String deleteCurrentQuestion() {
        QuestionServer.deleteQuestion(question.getSiteId(), question.getQuestionId());
        return "site?faces-redirect=true&site_id=" + question.getSiteId();
    }

}

package valuate;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name = "mockReportController")
@ViewScoped
public class MockReportController implements Serializable {

    public List<MockReport> getReports() throws ParseException {
        List<MockReport> reports = new LinkedList<>();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        // Amerikanac, novembar
        MockReport report = new MockReport(formatter.parse("2021-11-20"), 5);
        report.setUsername("Kyle");
        report.setComment("I'm delighted with all of the changes you have implemented. Keep up with the good work!");
        report.setCountry("US");
        report.setIpAddress("103.13.241.71");
        report.setUserAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:94.0) Gecko/20100101 Firefox/94.0");

        reports.add(report);

        // Francuz, oktobar
        report = new MockReport(formatter.parse("2021-11-11"), 4);
        report.setUsername("-");
        report.setComment("Je l'aime, mais il peut encore être amélioré.");
        report.setCountry("FR");
        report.setIpAddress("103.133.84.10");
        report.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 12_0_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Safari/605.1.15");

        reports.add(report);

        //Srbin, septembar
        report = new MockReport(formatter.parse("2021-11-04"), 4);
        report.setUsername("Zika");
        report.setComment("Radi na Operi. Sad valja, ali moze malo bolje sa bojama.");
        report.setCountry("RS");
        report.setIpAddress("147.91.1.42");
        report.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36 OPR/81.0.4196.37");
        reports.add(report);

        //Svaba, jun
        report = new MockReport(formatter.parse("2021-10-22"), 4);
        report.setUsername("-");
        report.setComment("Nicht ganz nutzlos, aber es ist schlecht.");
        report.setCountry("DE");
        report.setIpAddress("102.177.115.8");
        report.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36");
        reports.add(report);

        //Srbin, februar
        report = new MockReport(formatter.parse("2021-10-15"), 1);
        report.setUsername("Zika");
        report.setComment("Ništa ovo ne valja, ne radi mi na Operi.");
        report.setCountry("RS");
        report.setIpAddress("147.91.1.42");
        report.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36 OPR/81.0.4196.37");
        reports.add(report);

        return reports;
    }
}

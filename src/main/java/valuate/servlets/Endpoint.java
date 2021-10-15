package valuate.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Endpoint extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(Endpoint.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //request.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        try (InputStream inputStream = request.getInputStream();) {
            JSONParser parser = new JSONParser();
            JSONObject parsedObject = (JSONObject) parser.parse(new InputStreamReader(inputStream));

            LOG.info("Received feedback " + (String) parsedObject.get("comment") + " graded: " + (long) parsedObject.get("grade"));
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        
    }
}

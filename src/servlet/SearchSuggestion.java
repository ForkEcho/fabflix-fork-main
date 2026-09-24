package servlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet("/movie-suggestion")
public class SearchSuggestion extends HttpServlet {
    /*
     * populate the movie hash map.
     * Key is movie ID. Value is movie title.
     */
    private DataSource dataSourceMaster;
    private DataSource dataSourceSlave;

    public void init(ServletConfig config) {
        try {
            dataSourceMaster = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadWrite");
            dataSourceSlave = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private String createFullTextMatchQuery(String[] tokens){
        if(tokens == null || tokens.length == 0){
            return "";
        }

        if(tokens.length == 1){
            return tokens[0] + "*";
        }

        String modifiedInput = "";
        for(int i = 0; i < tokens.length; i++){
            String token = tokens[i];
            modifiedInput += "+" + token + "*";
            if(i != tokens.length - 1){
                modifiedInput += " ";
            }
        }

        return modifiedInput;
    }

    /*
     *
     * Match the query against movies and return a JSON response.
     *
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "heroID": 113 } }
     * ]
     *
     * The format is like this because it can be directly used by the
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     *
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        request.getServletContext().log("Getting movies");

        try (PrintWriter out = response.getWriter(); Connection conn = dataSourceSlave.getConnection()) {
            JsonArray jsonArray = new JsonArray();
            String query = request.getParameter("query");
            PreparedStatement statement;

            // return the empty json array if query is null or empty
            if (query == null || query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }

            String[] tokens = query.split("\\s+");
            String modifiedInput = createFullTextMatchQuery(tokens);

            String sql = "SELECT id, title FROM movies m WHERE MATCH (m.title) AGAINST (? IN BOOLEAN MODE) LIMIT 10";
            statement = conn.prepareStatement(sql);
            statement.setString(1, modifiedInput);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                jsonArray.add(generateJsonObject(movieId, movieTitle));
            }
            rs.close();
            statement.close();

            JsonObject result = new JsonObject();
            result.add("suggestions", jsonArray);

            out.write(result.toString());
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }
    }

    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "heroID": 11 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieID, String movieTitle) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieID", movieID);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }


}

package servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


// Declaring a WebServlet called servlet.StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "servlet.StarsServlet", urlPatterns = "/api/stars")
public class StarsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSourceMaster;
    private DataSource dataSourceSlave;

    public void init(ServletConfig config) {
        try {
            dataSourceMaster = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb-master");
            dataSourceSlave = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb-slave");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        String id = request.getParameter("id");
        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSourceSlave.getConnection()) {
            // Declare our statement
            String query = "select s.name, s.id, count(all_stars.movieId) as movie_count from stars s " +
                    "join stars_in_movies sim on sim.starId = s.id " +
                    "join movies m on sim.movieId = m.id and m.id = ? " +
                    "left join stars_in_movies all_stars on s.id = all_stars.starId " +
                    "group by s.name, s.id order by movie_count desc";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);
            System.out.println(statement);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String star_id = rs.getString("id");
                String star_name = rs.getString("name");
                String movie_count = rs.getString("movie_count");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("star_id", star_id);
                jsonObject.addProperty("star_name", star_name);
                jsonObject.addProperty("movie_count", movie_count);
                jsonArray.add(jsonObject);
            }

            rs.close();
            statement.close();
            // Log to localhost log

            request.getServletContext().log("getting " + jsonArray.size() + " results");
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
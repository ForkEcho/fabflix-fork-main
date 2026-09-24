package servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called servlet.SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "servlet.GenresServlet", urlPatterns = "/api/genres")
public class GenresServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
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

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSourceSlave.getConnection()) {
            String query;
            PreparedStatement statement;

            if (id == null || id.isEmpty()) {
                query = "select * from genres";
                statement = conn.prepareStatement(query);
            }
            else {
                query = "SELECT * from genres_in_movies as gm, genres as g, movies as m " +
                        "where gm.genreId = g.id and m.id = gm.movieId and m.id = ? order by g.name asc";
                statement = conn.prepareStatement(query);
                statement.setString(1, id);
            }

            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {

                // Create a JsonObject based on the data we retrieve from rs
                String genreId = rs.getString("id");
                String genre = rs.getString("name");

                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("genre", genre);
                jsonObject.addProperty("id", genreId);
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}

package servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import resources.Item;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

import static java.lang.System.out;

@WebServlet(name = "servlet.DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String action = request.getParameter("table");
        JsonObject responseJsonObject = new JsonObject();

        try (Connection conn = dataSourceMaster.getConnection()) {
            switch (action) {
                case "stars":
                    String starName = request.getParameter("starName");
                    String birthYear = request.getParameter("birthYear");

                    String query = "SELECT CONCAT('nm', COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)),0)+1) AS newId FROM stars";
                    PreparedStatement statement = conn.prepareStatement(query);
                    ResultSet rs = statement.executeQuery();
                    rs.next();
                    String starId = rs.getString("newId");

                    String insertQuery = "INSERT INTO stars(id, name, birthYear) VALUES (?, ?, ?)";

                    PreparedStatement insertStatement = conn.prepareStatement(insertQuery);

                    insertStatement.setString(1, starId);
                    insertStatement.setString(2, starName);
                    if (birthYear == null || birthYear.isEmpty()) {
                        insertStatement.setNull(3, Types.INTEGER);
                    }
                    else {
                        insertStatement.setInt(3, Integer.parseInt(birthYear));
                    }

                    insertStatement.executeUpdate();
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Star added! ID: " + starId);
                    responseJsonObject.addProperty("id", starId);

                    break;
                case "movies":
                    String movieTitle = request.getParameter("movieTitle");
                    String movieYear = request.getParameter("movieYear");
                    String movieDirector = request.getParameter("movieDirector");
                    String movieStar = request.getParameter("movieStar");
                    String movieGenre = request.getParameter("movieGenre");

                    String movieQuery = "{CALL add_movie(?,?,?,?,?,?)}";
                    CallableStatement movieStatement = conn.prepareCall(movieQuery);
                    movieStatement.setString(1, movieTitle);
                    movieStatement.setInt(2, Integer.parseInt(movieYear));
                    movieStatement.setString(3, movieDirector);
                    movieStatement.setString(4, movieStar);
                    movieStatement.setString(5, movieGenre);
                    movieStatement.registerOutParameter(6, java.sql.Types.VARCHAR);

                    movieStatement.execute();

                    String message = movieStatement.getString(6);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", message);
                    break;
            }

        } catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", e.toString());
        }
        response.getWriter().write(responseJsonObject.toString());
    }
}

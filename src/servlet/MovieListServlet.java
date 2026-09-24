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
import java.sql.SQLException;
import java.util.HashMap;

// Declaring a WebServlet called servlet.MovieListServlet, which maps to url "/api/movie-list"
@WebServlet(name = "servlet.MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet {
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

    private String[] getFirst3Items(Connection conn, String movieId, String item, String joinTable) {
        StringBuilder ids = new StringBuilder();
        StringBuilder names = new StringBuilder();
        //The user does not input item and joinTable so SQL injection is not possible here.
        String query = String.format(
                "select x.id, x.name from %s x join %s xim on x.id = xim.%s where xim.movieId = ? limit 3",
                item + "s", joinTable, item + "Id");

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, movieId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ids.append(rs.getString("id")).append(",");
                    names.append(rs.getString("name")).append(",");
                }
                if (ids.length() > 0) ids.setLength(ids.length() - 1);
                if (names.length() > 0) names.setLength(names.length() - 1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new String[]{ids.toString(), names.toString()};
    }


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */

    private boolean stringNotNullNotEmpty(String val) {
        return val != null && !val.isEmpty();
    }

    /* primary-desc, secondary-asc, primary-asc, secondary-desc

     */
    private String createSortingStatement(String primaryString, String secondaryString) {
        String statement = "";
        String primaryField = primaryString.split("-")[0];
        String primaryOrder = primaryString.split("-")[1];
        String secondaryField = secondaryString.split("-")[0];
        String secondaryOrder = secondaryString.split("-")[1];

        if (primaryField.equals("title")) {
            if (primaryOrder.equals("asc") || primaryOrder.equals("desc")) {
                statement += "m.title " + primaryOrder + ", ";
            }
        }

        else if (primaryField.equals("rating")) {
            if (primaryOrder.equals("asc") || primaryOrder.equals("desc")) {
                statement += "r.rating " + primaryOrder + ", ";
            }
        }

        if (secondaryField.equals("title")) {
            if (secondaryOrder.equals("asc") || secondaryOrder.equals("desc")) {
                statement += "m.title " + secondaryOrder + " ";
            }
        }
        else if (secondaryField.equals("rating")) {
            if (secondaryOrder.equals("asc") || secondaryOrder.equals("desc")) {
                statement += "r.rating " + secondaryOrder + " ";
            }
        }


        return statement;
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type
        request.getServletContext().log("Getting movies");
        PrintWriter out = response.getWriter();
        // Get a connection from dataSource and let resource manager close the connection after usage.
        long startTimeTS = System.nanoTime();
        try (Connection conn = dataSourceSlave.getConnection()) {

            //String query = "select * from movies m join ratings r on m.id = r.movieId order by r.rating desc limit 20";
            String query;
            PreparedStatement statement;
            String searchStar = request.getParameter("star");
            String searchYearAsString = request.getParameter("year");

            Integer searchYear = null;

            if (stringNotNullNotEmpty(searchYearAsString)) {
                searchYear = Integer.parseInt(searchYearAsString);
            }

            String searchDirector = request.getParameter("director");
            String searchTitle = request.getParameter("title");
            String browseTitle = request.getParameter("browse-title");
            String searchGenreID = request.getParameter("genre");

            String postings = request.getParameter("N");
            String offset = request.getParameter("offset");

            String primarySort = request.getParameter("sort-primary");
            String secondarySort = request.getParameter("sort-secondary");
            String sortingStatement = "r.rating desc";
            String fullTextSearchTitle = request.getParameter("fulltext-title");

            if(fullTextSearchTitle != null) {
                System.out.println(fullTextSearchTitle);
            }

            if (stringNotNullNotEmpty(primarySort) && stringNotNullNotEmpty(secondarySort)) {
                sortingStatement = createSortingStatement(primarySort, secondarySort);
            }

            System.out.println(sortingStatement);

            //Default number of postings
            int numPostings = 25;
            int offsetValue = 0;

            if (stringNotNullNotEmpty(postings)) {
                numPostings = Integer.parseInt(postings);
                if (numPostings > 100 || numPostings < 1) {
                    numPostings = 25;
                }
            }

            if (stringNotNullNotEmpty(offset)) {
                offsetValue = Integer.parseInt(offset);
            }

            /* Both can't have values at the same time */
            if(stringNotNullNotEmpty(searchTitle) && stringNotNullNotEmpty(fullTextSearchTitle)) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage","Cannot use both fulltext title search and substring title search at the same time!");
                out.write(jsonObject.toString());
                response.setStatus(500);
                out.close();
                return;
            }

            /* Both can't have values at the same time  */
            if (stringNotNullNotEmpty(searchTitle) && stringNotNullNotEmpty(browseTitle)) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage",
                        "Cannot browse by starting title character and search by substring title at the same time!");

                out.write(jsonObject.toString());
                response.setStatus(500);
                out.close();
                return;
            }

            if (!stringNotNullNotEmpty(searchStar) && searchYear == null && !stringNotNullNotEmpty(searchDirector) && !stringNotNullNotEmpty(searchTitle) && !stringNotNullNotEmpty(fullTextSearchTitle)) {
                if (stringNotNullNotEmpty(searchGenreID)) {
                    query = "select * from movies m join ratings r on m.id = r.movieId join genres_in_movies gm on gm.movieId = m.id join genres g on g.id = gm.genreId where g.id = ? order by " + sortingStatement + " limit " + numPostings + " offset " + offsetValue;
                    statement = conn.prepareStatement(query);
                    statement.setString(1, searchGenreID);

                }
                else if (stringNotNullNotEmpty(browseTitle)) {
                    query = "select * from movies m join ratings r on m.id = r.movieId where ";
                    String orderByString = "order by " + sortingStatement + " limit " + numPostings + " offset " + offsetValue;

                    if (browseTitle.equalsIgnoreCase("*")) {
                        query += "m.title REGEXP '^[^A-Za-z0-9]' " + orderByString;
                        statement = conn.prepareStatement(query);

                    }
                    else {
                        query += "m.title LIKE ? " + orderByString;
                        statement = conn.prepareStatement(query);
                        statement.setString(1, browseTitle + "%");
                    }

                }
                else {
                    query = "select * from movies m join ratings r on m.id = r.movieId order by " + sortingStatement + " limit " + numPostings + " offset " + offsetValue;
                    statement = conn.prepareStatement(query);
                }
                System.out.println(statement);
            }
            else {
                String conditionals = "";

                query = "select * from movies m " +
                        "join ratings r on m.id = r.movieId " +
                        /*"join stars_in_movies sm on m.id = sm.movieId " +
                        "join stars s on sm.starId = s.id " +*/
                        "where ";

                int index = 1;
                HashMap<Integer, String> indexValues = new HashMap<>();

                if (stringNotNullNotEmpty(searchStar)) {
                    conditionals += "EXISTS (SELECT 1 FROM stars_in_movies sm JOIN stars s on sm.starId = s.id WHERE sm.movieId = m.id AND s.name LIKE ?)";
                    indexValues.put(index, "%" + searchStar + "%");
                    index++;

                }

                if (stringNotNullNotEmpty(searchYearAsString)) {
                    if (index > 1) {
                        conditionals += " AND ";
                    }

                    conditionals += "m.year = ?";
                    indexValues.put(index, searchYear.toString());
                    index++;
                }


                if (stringNotNullNotEmpty(searchDirector)) {
                    if (index > 1) {
                        conditionals += " AND ";
                    }

                    conditionals += "m.director LIKE ?";
                    indexValues.put(index, "%" + searchDirector + "%");
                    index++;
                }

                if (stringNotNullNotEmpty(searchTitle)) {
                    if (index > 1) {
                        conditionals += " AND ";
                    }

                    //Wildcard uses regex with LIKE here
                    /*if (searchTitle.equalsIgnoreCase("*")) {
                        conditionals += "m.title REGEXP ?";
                        indexValues.put(index, "^[^A-Za-z0-9]");
                    }*/
                    conditionals += "m.title LIKE ?";
                    indexValues.put(index, "%" + searchTitle + "%");

                } else if (stringNotNullNotEmpty(fullTextSearchTitle)){
                    if (index > 1) {
                        conditionals += " AND ";
                    }

                    String[] tokens = fullTextSearchTitle.split("\\s+");
                    conditionals += "MATCH (m.title) AGAINST (? IN BOOLEAN MODE)";
                    String modifiedInput = createFullTextMatchQuery(tokens);
                    indexValues.put(index, modifiedInput);
                }

                query += conditionals + " order by " + sortingStatement + " limit " + numPostings + " offset " + offsetValue;
                statement = conn.prepareStatement(query);

                for (int i = 0; i < indexValues.size(); i++) {
                    statement.setString(i + 1, indexValues.get(i + 1));
                }
                System.out.println(statement);

            }

            // Perform the query
            long startTimeTJ = System.nanoTime();
            ResultSet rs = statement.executeQuery();
            long endTimeTJ = System.nanoTime();
            long elapsedTimeTJ = endTimeTJ - startTimeTJ;
            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieRating = rs.getString("rating") != null ? rs.getString("rating") : "N/A" ;
                String[] stars = getFirst3Items(conn, movieId, "star", "stars_in_movies");
                String[] genres = getFirst3Items(conn, movieId, "genre", "genres_in_movies");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_rating", movieRating);
                jsonObject.addProperty("star_id", stars[0]);
                jsonObject.addProperty("star_name", stars[1]);
                jsonObject.addProperty("genre_id", genres[0]);
                jsonObject.addProperty("genre_name", genres[1]);
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Time logging
            long endTimeTS = System.nanoTime();
            long elapsedTimeTS = endTimeTS - startTimeTS;
            response.setHeader("X-TS-Time", String.valueOf(elapsedTimeTS));
            response.setHeader("X-TJ-Time", String.valueOf(elapsedTimeTJ));

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
    }
}

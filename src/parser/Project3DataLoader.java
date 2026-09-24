package parser;

import parser.classes.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class Project3DataLoader {
    Connection connection;

    public void init(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false", "mytestuser", "My6$Password"
            );

            if(connection != null){
                System.out.println("Connected to database");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadGenresInMovies(HashSet<GenreInMovie> genresInMoviesEntries){
        try {
            int batchSize = 5000;
            int currentCount = 0;

            connection.setAutoCommit(false);
            String query = "INSERT IGNORE INTO genres_in_movies VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            for (GenreInMovie genreInMovie : genresInMoviesEntries) {
                try {
                    preparedStatement.setInt(1, genreInMovie.getGenreId());
                    preparedStatement.setString(2, genreInMovie.getMovieId());
                    preparedStatement.addBatch();
                    if (++currentCount % batchSize == 0) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                        connection.commit();
                    }
                } catch (Exception e) {
                    System.err.println("Error inserting genres_in_movies entry into batch: " + e.getMessage());
                }
            }

            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
            connection.commit();
            System.out.println("GenresInMovies inserted successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void populateRatings(HashMap<String, Movie> movies){
        for(Movie movie: movies.values()){
            try {
                String query = "INSERT INTO ratings VALUES (?, ?, ?)";
                Random random = new Random();
                BigDecimal rating = BigDecimal.valueOf(random.nextInt(100) / 10.0).setScale(2, RoundingMode.HALF_UP);

                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, movie.getId());
                preparedStatement.setFloat(2, rating.floatValue());
                preparedStatement.setInt(3,  random.nextInt(10000));
                preparedStatement.executeUpdate();
            } catch (Exception e){
                System.out.println(e.getMessage());
            }

        }
    }*/

    public void loadRatings(HashMap<String, Movie> movies){
        try {
            int batchSize = 500;
            int currentCount = 0;
            connection.setAutoCommit(false);
            String query = "INSERT IGNORE INTO ratings VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (Movie movie : movies.values()) {
                try {
                    Rating rating = movie.getRatingInfo();
                    preparedStatement.setString(1, rating.getMovieId());
                    preparedStatement.setFloat(2, rating.getRating());
                    preparedStatement.setInt(3, rating.getNumVotes());
                    preparedStatement.addBatch();
                    if (++currentCount % batchSize == 0) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                        connection.commit();
                    }
                } catch (Exception e) {
                    System.err.println("Error inserting rating into batch: " + e.getMessage());
                }
            }

            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
            connection.commit();
            System.out.println("Ratings inserted successfully");
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void loadStarsInMovies(Set<StarInMovie> starsInMoviesEntries){
        try {
            int batchSize = 20000;
            int currentCount = 0;
            connection.setAutoCommit(false);
            String query = "INSERT IGNORE INTO stars_in_movies VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (StarInMovie starInMovie : starsInMoviesEntries) {
                try {
                    preparedStatement.setString(1, starInMovie.getStarId());
                    preparedStatement.setString(2, starInMovie.getMovieId());
                    preparedStatement.addBatch();
                    if (++currentCount % batchSize == 0) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                        connection.commit();
                    }
                } catch (Exception e) {
                    System.err.println("Error inserting stars_in_movies entry into batch: " + e.getMessage());
                }
            }

            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
            connection.commit();
            connection.setAutoCommit(true);
            System.out.println("Stars in movies inserted successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadMovies(HashMap<String, Movie> movies){
        try {
            int batchSize = 1000;
            int currentCount = 0;
            connection.setAutoCommit(false);
            String query = "INSERT IGNORE INTO movies VALUES (?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (Movie movie : movies.values()) {
                try {
                    preparedStatement.setString(1, movie.getId());
                    preparedStatement.setString(2, movie.getTitle());
                    preparedStatement.setInt(3, movie.getYear());
                    preparedStatement.setString(4, movie.getDirector());
                    preparedStatement.addBatch();

                    if (++currentCount % batchSize == 0) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                        connection.commit();
                    }
                } catch (Exception e) {
                    System.err.println("Error inserting movie into batch: " + e.getMessage());
                }
            }

            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
            connection.commit();
            System.out.println("Movies inserted successfully");
            connection.setAutoCommit(true);
        } catch (Exception err){
            err.printStackTrace();
        }
    }

    public void loadGenres(HashMap<Integer, Genre> genres){
        try {
            int batchSize = 100;
            int currentCount = 0;
            connection.setAutoCommit(false);
            String query = "INSERT IGNORE INTO genres VALUES (?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (Genre genre : genres.values()) {
                try {
                    preparedStatement.setInt(1, genre.getId());
                    preparedStatement.setString(2, genre.getName());
                    preparedStatement.addBatch();
                    if (++currentCount % batchSize == 0) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                        connection.commit();
                    }
                } catch (Exception e) {
                    System.err.println("Error inserting genre into batch: " + e.getMessage());
                }
            }

            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
            connection.commit();
            System.out.println("Genres inserted successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadStars(HashMap<String, Star> stars){
        try {
            int batchSize = 1000;
            int currentCount = 0;
            connection.setAutoCommit(false);
            String query = "INSERT IGNORE INTO stars VALUES (?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (Star star : stars.values()) {
                try {
                    preparedStatement.setString(1, star.getId());
                    preparedStatement.setString(2, star.getName());
                    if (star.getBirthYear() != null) {
                        preparedStatement.setInt(3, star.getBirthYear());
                    } else {
                        preparedStatement.setNull(3, java.sql.Types.INTEGER);
                    }
                    preparedStatement.addBatch();
                    if (++currentCount % batchSize == 0) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                        connection.commit();
                    }
                } catch (Exception e) {
                    System.err.println("Error inserting star into batch: " + e.getMessage());
                }
            }

            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
            connection.commit();
            System.out.println("Stars inserted successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
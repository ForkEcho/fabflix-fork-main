package parser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import parser.classes.*;


public class Project3CSVParser {
    HashMap<String, Movie> movies;
    HashMap<String, Star> stars;
    HashMap<Integer, Genre> genres;

    HashSet<StarInMovie> starsInMoviesEntries;
    HashSet<GenreInMovie> genresInMoviesEntries;
    HashSet<Rating> ratings;

    public Project3CSVParser() {
        this.movies = new HashMap<>();
        this.stars = new HashMap<>();
        this.genres = new HashMap<>();
        this.starsInMoviesEntries = new HashSet<>();
        this.genresInMoviesEntries = new HashSet<>();
        this.ratings = new HashSet<>();
    }

    public void parseGenreMovieEntries(String genreMovieFile){
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(genreMovieFile))){
            System.out.println("Reading genre_in_movies entries from csv file");
            String currentLine;
            bufferedReader.readLine();

            while((currentLine = bufferedReader.readLine()) != null){
                List<String> values = parseCSVLine(currentLine);
                String stringGenreId = values.get(0);
                String stringMovieId = values.get(1);

                int genreId = 0;

                try {
                    if(stringGenreId != null && !stringGenreId.isEmpty()){
                        genreId = Integer.parseInt(stringGenreId);
                        GenreInMovie genreInMovie = new GenreInMovie(genreId, stringMovieId);
                        if(genres.containsKey(genreId) && movies.containsKey(stringMovieId)){
                            genresInMoviesEntries.add(genreInMovie);
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse from following data: " + currentLine);
                    System.err.println("Reason: " + e.getMessage());
                }
            }
            System.out.println("Done reading genre_in_movies entries!");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parseStarMovieEntries(String genreMovieFile){
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(genreMovieFile))){
            System.out.println("Loading stars_in_movies entries from CSV file...");
            String currentLine;
            bufferedReader.readLine();
            while((currentLine = bufferedReader.readLine()) != null){
                List<String> values = parseCSVLine(currentLine);

                String starId = values.get(0);
                String movieId;
                if(values.size() < 2){
                    movieId = "";
                } else{
                    movieId = values.get(1);
                }

                StarInMovie starInMovie = new StarInMovie(starId, movieId);

                if(movies.containsKey(movieId) && stars.containsKey(starId)){
                    starsInMoviesEntries.add(starInMovie);
                } else {
                    if (!stars.containsKey(starId) && !movies.containsKey(movieId)) {
                        System.err.println("Error parsing current row! Both StarID " + starId + " and Movie ID " + movieId + " were not found from previously loading stars and movies csv files!");
                    } else if (!stars.containsKey(starId)) {
                        System.err.println("Error parsing current row! StarID " + starId + " was not found from previous stars csv file!");
                    } else if (!movies.containsKey(movieId)) {
                        System.err.println("Error parsing current row! MovieID " + movieId + " was not found from previous movies csv file!");
                    }
                }
            }
            System.out.println("Done loading stars_in_movies entries!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseRatings(String ratingsFile){
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(ratingsFile))){
            System.out.println("Loading ratings from CSV file...");
            String currentLine;
            bufferedReader.readLine();
            while((currentLine = bufferedReader.readLine()) != null){
                List<String> values = parseCSVLine(currentLine);
                String movieId = values.get(0);
                String ratingAsString = values.get(1);
                String numVotesAsString = values.get(2);

                if(movies.containsKey(movieId)){
                    try {
                        float rating = Float.parseFloat(ratingAsString);
                        int numVotes = Integer.parseInt(numVotesAsString);
                        Rating ratingEntry = new Rating(movieId, rating, numVotes);
                        Movie currMovie = movies.get(movieId);
                        /* Update randomized rating to rating info found in current row */
                        currMovie.setRating(ratingEntry);
                        movies.put(movieId, currMovie);
                    } catch (NumberFormatException e) {
                        System.err.println("Could not parse current row containing the following: " + currentLine);
                        System.err.println("Reason: " + e.getMessage());
                    }
                }
            }
            System.out.println("Done loading ratings!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseGenres(String genresFile){
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(genresFile))){
            System.out.println("Loading genres from CSV file...");
            String currentLine;
            bufferedReader.readLine();
            while((currentLine = bufferedReader.readLine()) != null) {
                List<String> values = parseCSVLine(currentLine);
                String genreId = values.get(0);
                String genreName = values.get(1);

                int id = 0;
                try {
                    if (genreId != null && !genreId.isEmpty()) {
                        id = Integer.parseInt(genreId);
                        Genre genre = new Genre(id, genreName);
                        if(!genres.containsKey(id)){
                            genres.put(id, genre);
                        }

                    } else {
                        System.err.println("Could not parse row containing the following data: " + currentLine);
                        System.err.println("Genre ID is NULL or empty");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse row containing the following data: " + currentLine);
                    System.err.println("Reason: " + e.getMessage());
                }
            }
            System.out.println("Done parsing genres from CSV file");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void parseStars(String starsFile) {
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(starsFile))){
            System.out.println("Parsing stars from CSV file...");
            String currentLine;
            bufferedReader.readLine();

            while((currentLine = bufferedReader.readLine()) != null){
                List<String> values = parseCSVLine(currentLine);
                String id = values.get(0);
                String name = values.get(1);
                String birthYear = null;

                if(values.size() > 2){
                   birthYear = values.get(2);
                }

                Integer intYear;
                try {
                    if(birthYear != null){
                        intYear = Integer.parseInt(birthYear);
                    } else {
                        intYear = null;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Current row contains invalid year from following row: " + currentLine);
                    System.err.println("Will use NULL values instead");
                    intYear = null;
                }

                Star star = new Star(id, name, intYear);
                if (!stars.containsKey(id)) {
                    stars.put(id, star);
                }
            }

            System.out.println("Done parsing stars from CSV file!");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private List<String> parseCSVLine(String line) {
        ArrayList<String> values = new ArrayList<>();
        StringBuilder currWord = new StringBuilder();
        boolean quoteEncountered = false;

        for(int i = 0; i < line.length(); i++){
            char currentChar = line.charAt(i);

            if(currentChar == '"'){
                if(i + 1 < line.length() && quoteEncountered && line.charAt(i+1) == '"') {
                    currWord.append('"');
                } else if (i == 0 || line.charAt(i - 1) != '\\') {
                    quoteEncountered = !quoteEncountered;
                }
            } else if (currentChar == ',' && !quoteEncountered){
               values.add(currWord.toString());
               currWord.setLength(0);
            } else {
                currWord.append(currentChar);
            }
        }

        values.add(currWord.toString());
        return values;
    }

    public void parseMovies(String moviesFile) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(moviesFile))){
            System.out.println("Parsing movies from CSV file...");
            String currentLine;
            while((currentLine = bufferedReader.readLine()) != null){
                List<String> values = parseCSVLine(currentLine);
                if(values.toArray().length != 4) {
                    //Inconsistency report using piping to file
                    System.err.println("Could not parse movie data containing the following: " + currentLine);
                    System.err.println("Reason: Insufficient/Extra values!");
                } else {
                    String movieId = values.get(0);
                    String movieName = values.get(1);
                    String movieYear = values.get(2);
                    String movieDirector = values.get(3);

                    int year = 0;
                    try {
                        if(movieYear != null && !movieYear.isEmpty()){
                            year = Integer.parseInt(movieYear);
                        }

                        if(year < 0){
                            throw new NumberFormatException("Could not parse movie data. Year cannot be negative!");
                        }

                        Movie movie = new Movie(movieId, movieName, year, movieDirector);
                        if (!movies.containsKey(movieId)) {
                            movies.put(movieId, movie);
                        }

                    } catch (NumberFormatException e) {
                        System.err.println("Could not parse movie data with the following data: " + movieYear);
                        System.err.println("Reason: " + e.getMessage());
                    }
                }
            }
            System.out.println("Done parsing movies!");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
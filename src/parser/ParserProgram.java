package parser;

public class ParserProgram {
    public static void main(String[] args){
        Project3CSVParser parser = new Project3CSVParser();
        Project3DataLoader loader = new Project3DataLoader();
        parser.parseMovies("src/resources/movies.csv");
        parser.parseStars("src/resources/stars.csv");
        parser.parseGenres("src/resources/genres.csv");
        parser.parseStarMovieEntries("src/resources/stars_in_movies.csv");
        parser.parseGenreMovieEntries("src/resources/genres_in_movies.csv");
        parser.parseRatings("src/resources/ratings.csv");
        loader.init();
        loader.loadMovies(parser.movies);
        loader.loadStars(parser.stars);
        loader.loadGenres(parser.genres);
        loader.loadGenresInMovies(parser.genresInMoviesEntries);
        loader.loadStarsInMovies(parser.starsInMoviesEntries);
        loader.loadRatings(parser.movies); //Ensures all movies have a rating
        loader.closeConnection();
    }
}
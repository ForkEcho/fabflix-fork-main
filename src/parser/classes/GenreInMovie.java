package parser.classes;

public class GenreInMovie {
    private int genreId;
    private String movieId;

    public GenreInMovie(int genreId, String movieId){
        this.genreId = genreId;
        this.movieId = movieId;
    }

    public int getGenreId() {
        return genreId;
    }

    public String getMovieId() {
        return movieId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GenreInMovie && genreId == ((GenreInMovie) obj).getGenreId() && movieId.equals(((GenreInMovie) obj).getMovieId());
    }
}
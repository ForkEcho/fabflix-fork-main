package parser.classes;

public class StarInMovie {
    private String starId;
    private String movieId;

    public StarInMovie(String starId, String movieId){
        this.starId = starId;
        this.movieId = movieId;
    }

    public String getStarId() {
        return starId;
    }

    public String getMovieId() {
        return movieId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StarInMovie && starId.equals(((StarInMovie) obj).starId) && movieId.equals(((StarInMovie) obj).movieId);
    }
}
package parser.classes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class Rating {
    private String movieId;
    private float rating;
    private int numVotes;

    public Rating(String movieId) {
        this.movieId = movieId;
        generateRandomRating();
        generateRandomNumVotes();
    }

    public Rating(String movieId, float rating, int numVotes) {
        this.movieId = movieId;
        this.rating = rating;
        this.numVotes = numVotes;
    }

    private void generateRandomRating(){
        Random random = new Random();
        BigDecimal decimalRating = BigDecimal.valueOf(random.nextInt(100) / 10.0).setScale(2, RoundingMode.HALF_UP);
        rating = decimalRating.floatValue();
    }

    private void generateRandomNumVotes(){
        Random random = new Random();
        numVotes = random.nextInt(10000);
    }

    public String getMovieId() {
        return movieId;
    }

    public float getRating() {
        return rating;
    }

    public int getNumVotes() {
        return numVotes;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Rating && movieId.equals(((Rating) o).movieId);
    }
}
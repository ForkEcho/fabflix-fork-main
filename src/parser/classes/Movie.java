package parser.classes;

public class Movie {
    private String id;
    private String title;
    private int year;
    private String director;
    private Rating rating;

    public Movie(String id, String title, int year, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.rating = new Rating(id);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public Rating getRatingInfo() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Movie && (((Movie) obj).getId().equals(id) || ((Movie) obj).getTitle().equalsIgnoreCase(title));
    }
}

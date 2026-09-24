package parser.classes;

public class Star {
    private String id;
    private String name;
    private Integer birthYear;

    public Star(String id, String name, Integer birthYear) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Star && (id.equals(((Star) other).id) || name.equalsIgnoreCase(((Star) other).getName()));
    }
}
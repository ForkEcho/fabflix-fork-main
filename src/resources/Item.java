package resources;

public class Item {
    private final String id;
    private String name;
    private int quantity;
    private double price;

    public Item(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.quantity = 1;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    public void decrementQuantity() {
        this.quantity--;
    }

    public double getPrice() {
        return price;
    }

    public double getTotalPrice() {
        return price * quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}



package ua.lpnu.deposits.model;

/**
 * Represents a bank that offers deposit products.
 */
public class Bank {

    private int id;
    private String name;
    private double rating;

    /**
     * Constructs a Bank with all fields.
     *
     * @param id     unique identifier
     * @param name   bank name
     * @param rating bank reliability rating
     */
    public Bank(int id, String name, double rating) {
        this.id = id;
        this.name = name;
        this.rating = rating;
    }

    /**
     * Constructs a Bank without an id (for insert operations).
     *
     * @param name   bank name
     * @param rating bank reliability rating
     */
    public Bank(String name, double rating) {
        this(0, name, rating);
    }

    /** @return the bank's database id */
    public int getId() {
        return id;
    }

    /** @param id the database id to set */
    public void setId(int id) {
        this.id = id;
    }

    /** @return the bank's name */
    public String getName() {
        return name;
    }

    /** @param name the name to set */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the bank's reliability rating */
    public double getRating() {
        return rating;
    }

    /** @param rating the rating to set */
    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Bank{id=" + id + ", name='" + name + "', rating=" + rating + '}';
    }
}

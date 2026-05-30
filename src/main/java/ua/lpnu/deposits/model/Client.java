package ua.lpnu.deposits.model;

/**
 * Represents a bank client who can open deposits.
 */
public class Client {

    private int id;
    private String firstName;
    private String lastName;
    private String email;

    /**
     * Constructs a Client with all fields.
     *
     * @param id        unique identifier
     * @param firstName client's first name
     * @param lastName  client's last name
     * @param email     client's email address
     */
    public Client(int id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    /**
     * Constructs a Client without an id (for insert operations).
     *
     * @param firstName client's first name
     * @param lastName  client's last name
     * @param email     client's email address
     */
    public Client(String firstName, String lastName, String email) {
        this(0, firstName, lastName, email);
    }

    /** @return the client's database id */
    public int getId() {
        return id;
    }

    /** @param id the database id to set */
    public void setId(int id) {
        this.id = id;
    }

    /** @return the client's first name */
    public String getFirstName() {
        return firstName;
    }

    /** @param firstName the first name to set */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /** @return the client's last name */
    public String getLastName() {
        return lastName;
    }

    /** @param lastName the last name to set */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /** @return the client's full name */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** @return the client's email address */
    public String getEmail() {
        return email;
    }

    /** @param email the email to set */
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Client{id=" + id + ", name='" + getFullName() + "', email='" + email + "'}";
    }
}

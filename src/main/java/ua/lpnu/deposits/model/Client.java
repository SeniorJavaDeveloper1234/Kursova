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

    /**
     * Returns the client's database id.
     *
     * @return the database id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the client's database id.
     *
     * @param id the database id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the client's first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the client's first name.
     *
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the client's last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the client's last name.
     *
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the client's full name (first + last).
     *
     * @return the full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the client's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the client's email address.
     *
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Client{id=" + id + ", name='" + getFullName() + "', email='" + email + "'}";
    }
}

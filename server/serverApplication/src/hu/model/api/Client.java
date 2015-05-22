package hu.model.api;


/**
 * This class represents a {@link Client}.
 */
public class Client {
	private Integer id;
	private String name;
	private String secret;
	
	/**
     * Create a {@link Client} with an id.
     * 
     * @param id
     *            to set.
     */
    public Client(Integer id) {
        this.id = id;
    }
    
    /**
     * 
     * @return the id of the {@link Client}.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * 
     * @return the name of the {@link Client}.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name of the {@link Client}.
     * 
     * @param name
     *            to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 
     * @return the secret of the {@link Client}.
     */
    public String getSecret() {
        return this.secret;
    }

    /**
     * Set the secret of the {@link Client}.
     * 
     * @param secret
     *            to set.
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public String toString() {
        return String.format("Client (%s:%s)", this.name, this.secret);
    }

    @Override
    public int hashCode() {
        return (this.id == null) ? 0 : this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Client other = (Client) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
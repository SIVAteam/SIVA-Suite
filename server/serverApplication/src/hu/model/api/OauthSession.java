package hu.model.api;

import java.util.Date;


/**
 * This class represents a {@link OauthSession}.
 */
public class OauthSession {
	private String token;
	private Integer clientId;
	private Integer userId;
	private String scope;
	private Date expireDate;
	
	/**
     * Create a {@link OauthSession} with an id.
     * 
     * @param token
     *            to set.
     */
    public OauthSession(String token) {
        this.token = token;
    }

    /**
     * 
     * @return the token of the {@link OauthSession}.
     */
    public String getToken() {
        return this.token;
    }
    
    /**
     * 
     * @return the client's id.
     */
    public Integer getClientId() {
        return this.clientId;
    }

    /**
     * Set the client's id.
     * 
     * @param clientId
     *            to set.
     */
    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }
    
    /**
     * 
     * @return the user's id.
     */
    public Integer getUserId() {
        return this.userId;
    }

    /**
     * Set the user's id.
     * 
     * @param userId
     *            to set.
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    /**
     * 
     * @return the scope of the {@link OauthSession}.
     */
    public String getScope() {
        return this.scope;
    }

    /**
     * Set the scope of the {@link OauthSession}.
     * 
     * @param scope
     *            to set.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    /**
     * 
     * @return the expire date of the {@link OauthSession}.
     */
    public Date getExpireDate() {
        return this.expireDate;
    }

    /**
     * Set expire date of the {@link OauthSession}.
     * 
     * @param clientId
     *            to set.
     */
    public void setExpireDate(Date expiresAt) {
        this.expireDate = expiresAt;
    }

    @Override
    public String toString() {
        return String.format("OauthSession (%s)", this.token);
    }

    @Override
    public int hashCode() {
        return (this.token == null) ? 0 : this.token.hashCode();
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
        OauthSession other = (OauthSession) obj;
        if (this.token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!this.token.equals(other.token)) {
            return false;
        }
        return true;
    }
}
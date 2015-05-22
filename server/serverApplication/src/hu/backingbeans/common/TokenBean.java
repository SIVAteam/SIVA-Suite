package hu.backingbeans.common;

import hu.model.Video;
import hu.model.Token;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * This is the backing bean for the token input form. It holds the entered
 * {@link Token} which is used to gain access to a {@link Video}.
 */
@ManagedBean
@RequestScoped
public class TokenBean {
    private Token token;

    /**
     * 
     * @return the token which was entered by a user.
     */
    public Token getToken() {
        return this.token;
    }

    /**
     * Set the token entered by a user.
     * 
     * @param token
     *            to set.
     */
    public void setToken(Token token) {
        this.token = token;
    }
}
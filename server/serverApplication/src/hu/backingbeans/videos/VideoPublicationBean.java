package hu.backingbeans.videos;

import hu.model.Video;
import hu.model.Token;

import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.html.HtmlInputHidden;

/**
 * This is the backing bean for editing the publication settings of a
 * {@link Video}.
 */
@ManagedBean
@RequestScoped
public class VideoPublicationBean {
    private Integer id;
    private String title;
    private Date start;
    private Date stop;
    private boolean passwordAvailable = true;
    private String password;
    private boolean tokenAvailable = true;
    private int numTokens;
    private int tokenLength = 15;
    private List<Token> tokens;
    private List<TokenListEntryBean> tokenOutputList;
    private HtmlInputHidden hiddenId = new HtmlInputHidden();
    private String emailList;
    
    /**
     * @return the emailList.
     */
    public String getEmailList() {
        return emailList;
    }

    /**
     * Set a list of email addresses.
     * @param emailList to set.
     */
    public void setEmailList(String emailList) {
        this.emailList = emailList;
    }

    /**
     * @return the field containing the id of the {@link Video}.
     */
    public HtmlInputHidden getHiddenId() {
        return this.hiddenId;
    }
 
    /**
     * Set the field containing the id of the {@link Video}.
     * @param hiddenId
     *            to set.
     */
    public void setHiddenId(HtmlInputHidden hiddenId) {
        this.hiddenId = hiddenId;
    }
 
    /**
     * Set the id of the {@link Video} kept in a hidden field.
     * 
     * @param hiddenValue
     *            to set.
     */
    public void setHiddenIdValue(Object hiddenValue) {
        this.hiddenId.setValue(hiddenValue);
    }
 
    /**
     * @return the id of the {@link Video} kept in a hidden field.
     */
    public Object getHiddenIdValue() {
        return this.hiddenId.getValue();
    }

    /**
     * 
     * @return the id of the {@link Video}.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Set the id of the {@link Video}.
     * 
     * @param id
     *            to set.
     */
    public void setId(Integer id) {
        this.id = id;
    }
    
    /**
     * 
     * @return the title of the {@link Video}.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the title of the {@link Video}.
     * 
     * @param title
     *            to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     * @return the starting time of the {@link Video}.
     */
    public Date getStart() {
        return this.start;
    }

    /**
     * Set the starting time of the {@link Video}.
     * 
     * @param start
     *            to set.
     */
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * 
     * @return the end time of the {@link Video}.
     */
    public Date getStop() {
        return this.stop;
    }

    /**
     * Set the end time of the {@link Video}.
     * 
     * @param stop
     *            to set.
     */
    public void setStop(Date stop) {
        this.stop = stop;
    }

    /**
     * 
     * @return the password of the {@link Video}.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set the password of the {@link Video}.
     * 
     * @param password
     *            to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return true if the {@link Video} is protected with a password.
     */
    public boolean isPasswordAvailable() {
        return passwordAvailable;
    }

    /**
     * Set if the {@link Video} is protected with a password.
     * 
     * @param passwordAvailable
     *            to set.
     */
    public void setPasswordAvailable(boolean passwordAvailable) {
        this.passwordAvailable = passwordAvailable;
    }

    /**
     * @return true if the {@link Video} is accessed by using
     *         {@link Token}s.
     */
    public boolean isTokenAvailable() {
        return tokenAvailable;
    }

    /**
     * Set if the {@link Video} is accessed by using {@link Token}s.
     * 
     * @param tokenAvailable
     *            to set.
     */
    public void setTokenAvailable(boolean tokenAvailable) {
        this.tokenAvailable = tokenAvailable;
    }
    
    /**
     * 
     * @return the number of {@link Token}s that will be generated for a
     *         {@link Video}.
     */
    public int getNumTokens() {
        return this.numTokens;
    }

    /**
     * Set the number of {@link Token}s that will be generated for a
     * {@link Video}.
     * 
     * @param numTokens
     *            to set.
     */
    public void setNumTokens(int numTokens) {
        this.numTokens = numTokens;
    }
    
    /**
     * 
     * @return the length of {@link Token}s that will be generated for a
     *         {@link Video}.
     */
    public int getTokenLength() {
        return this.tokenLength;
    }

    /**
     * Set the length of {@link Token}s that will be generated for a
     * {@link Video}.
     * 
     * @param tokenLength
     *            to set.
     */
    public void setTokenLength(int tokenLength) {
        this.tokenLength = tokenLength;
    }

    /**
     * 
     * @return the {@link List} of all generated {@link Token}s.
     */
    public List<Token> getTokens() {
        return this.tokens;
    }

    /**
     * Set the {@link List} of all generated {@link Token}s.
     * 
     * @param tokens
     *            to set.
     */
    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    /**
     * 
     * @return the {@link List} of all generated {@link Token}s for output.
     */
    public List<TokenListEntryBean> getTokenOutputList() {
        return this.tokenOutputList;
    }

    /**
     * Set the {@link List} of all generated {@link Token}s for output.
     * 
     * @param tokens
     *            to set.
     */
    public void setTokenOutputList(List<TokenListEntryBean> tokenOutputList) {
        this.tokenOutputList = tokenOutputList;
    }
    
    /**
     * This backing bean holds the information about a single entry in a
     * {@link List} of {@link Token}s separate columns.
     */
    @ManagedBean
    @RequestScoped
    public static class TokenListEntryBean {
        private Token[] columns;

        /**
         * 
         * @return the {@link Token} columns of the entry.
         */
        public Token[] getColumns() {
            return this.columns;
        }

        /**
         * Set the {@link Token} columns of the entry.
         * 
         * @param user
         *            to set.
         */
        public void setColumns(Token[] columns) {
            this.columns = columns;
        }
    }
}
package hu.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * This abstract class contains common functionality for the controller classes.
 * It also provides methods to achieve reliable unit testing.
 */
public abstract class AController {

    private FacesContext cxtMock = null;
    private boolean mockSet = false;
    private String lastFacesMessage = null;
    private String lastRedirect = null;
    private List<String> outputStreamList = null;

    /**
     * Get the current instance of the {@link FacesContext}.
     * @return the current {@link FacesContext} when running in the application,
     *         when under test return the mock of the {@link FacesContext}.
     */
    protected FacesContext getCurrentFcInstance() {
        if (FacesContext.getCurrentInstance() == null) {
            this.mockSet = true;
            return this.cxtMock;
        } else {
            return FacesContext.getCurrentInstance();
        }
    }
    
    /**
     * Returns whether a JUnit-Test is currently performed.
     * @return true if it's a JUnit-Test.
     */
    public boolean isJUnitTest(){
	return (FacesContext.getCurrentInstance() == null);
    }

    /**
     * Set a manually created and configured mock for unit testing.
     * 
     * @param cxtMock
     *            to set.
     */
    public void setMock(FacesContext cxtMock) {
        this.mockSet = true;
        outputStreamList = new LinkedList<String>();
        this.cxtMock = cxtMock;
    }

    /**
     * Get a message from the language file by using the key of the pair of
     * values.
     * 
     * @param key
     *            to which the corresponding message should be fetched.
     * @return the message that is defined in the language file for a certain
     *         key or return "! INVALID MESSAGE KEY !" if key is not present.
     */
    protected String getCommonMessage(String key) {
        FacesContext cxt;

        if (this.mockSet) {
            cxt = this.cxtMock;
        } else {
            cxt = FacesContext.getCurrentInstance();
        }

        ResourceBundle bundle = ResourceBundle.getBundle(
                "hu.configuration.CommonMessages", cxt.getViewRoot()
                        .getLocale());

        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        } else {
            return "! INVALID MESSAGE KEY !";
        }

    }

    /**
     * Send a global {@link FacesMessage}.
     * 
     * @param message
     *            that should be sent.
     */
    protected void sendGlobalMessageToFacelet(String message) {
        if (this.mockSet) {
            this.lastFacesMessage = message;
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(message));
            this.removeDuplicateGlobalMessages();
        }
    }
    
    /**
     * Remove duplicate messages from global message list.
     */
	private void removeDuplicateGlobalMessages() {
		FacesMessage lastMessage = null;
		for (Iterator<FacesMessage> it = FacesContext.getCurrentInstance()
				.getMessages(); it.hasNext();) {
			FacesMessage message = it.next();
			if (lastMessage == null
					|| !message.getSummary().equals(lastMessage.getSummary())) {
				lastMessage = message;
			} else {
				it.remove();
			}
		}
	}

    /**
     * Send a {@link FacesMessage} to the message field with the provided id.
     * 
     * @param targetId
     *            of the field that should receive the message.
     * @param message
     *            that should be sent.
     */
    protected void sendMessageTo(String targetId, String message) {
        if (this.mockSet) {
            this.lastFacesMessage = message;
        } else {
            FacesContext cxt = FacesContext.getCurrentInstance();
            cxt.addMessage(targetId, new FacesMessage(cxt.getApplication()
                    .evaluateExpressionGet(cxt, message, String.class)));
        }
    }

    /**
     * 
     * @return the last {@link FacesMessage} that got logged during a unit test.
     */
    public String getLastFacesMessage() {
        return this.lastFacesMessage;
    }

    /**
     * Redirect the user to another page. DO NOT FORGET to RETURN from the
     * invoking method after using this, to stop the flow of the currently
     * executing method!
     * 
     * @param viewId
     *            of the facelet you want redirect to.
     */
    protected void redirectTo(String viewId) {
        if (this.mockSet) {
            this.lastRedirect = viewId;
        } else {
            FacesContext
                    .getCurrentInstance()
                    .getApplication()
                    .getNavigationHandler()
                    .handleNavigation(FacesContext.getCurrentInstance(), null,
                            viewId);
        }
    }

    /**
     * 
     * @return the last redirect that was sent during a unit test.
     */
    public String getLastRedirect() {
        return this.lastRedirect;
    }

    /**
     * Write a byte stream to a specified output stream.
     * 
     * @param outputStream
     *            to write to.
     * @param output
     *            to write to output stream.
     * @throws IOException
     */
    protected void writeToOutputStream(OutputStream outputStream, byte[] output)
            throws IOException {
        if (this.mockSet || outputStream == null) {
            try {
                outputStreamList.add(new String(output, "UTF-8"));
            } catch (UnsupportedEncodingException ignored) {
            }
        } else {
            outputStream.write(output);
        }
    }

    /**
     * 
     * @return the output stream that was redirected to a list during a unit
     *         test.
     */
    public List<String> getOutputStream() {
        return this.outputStreamList;
    }

    /**
     * Clear last sent faces message, latest redirect and the output stream.
     */
    public void clearLogs() {
        this.outputStreamList.clear();
        this.lastFacesMessage = null;
        this.lastRedirect = null;
    }
}
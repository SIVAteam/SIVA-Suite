package hu.validators;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;

/**
 * This abstract class is for different function that will be used in all
 * validators. Every {@link Validator} in this package extend this class.
 */
public abstract class AValidator implements Validator {

    /**
     * This method get the value of the given key.
     * 
     * @param key
     *            of the required value.
     * @param comp
     *            is the {@link UIComponent} which is the base class for user
     *            interface components.
     * @param fctx
     *            is the {@link FacesContext} in which the desired value is
     *            located.
     * @return the value of the given key as string.
     */
    protected static String readParamString(String key, UIComponent comp,
            FacesContext fctx) {
        ValueExpression valueExpression = comp.getValueExpression(key);
        
        if (valueExpression != null) {
            return (String) valueExpression.getValue(fctx.getELContext());
        }

        if (comp.getAttributes().containsKey(key)) {
            return (String) comp.getAttributes().get(key);
        }

        return null;
    }

    /**
     * This method get the value of the given key.
     * 
     * @param key
     *            of the required value.
     * @param comp
     *            is the {@link UIComponent} which is the base class for user
     *            interface components.
     * @param fctx
     *            is the {@link FacesContext} in which the desired value is
     *            located.
     * @return the value of the given key as string.
     */
    protected static Integer readParamInt(String key, UIComponent comp,
            FacesContext fctx) {
        ValueExpression valueExpression = comp.getValueExpression(key);

        if (valueExpression != null) {
            return Integer.parseInt((String) valueExpression.getValue(fctx
                    .getELContext()));
        }

        if (comp.getAttributes().containsKey(key)) {
            return Integer.parseInt((String) comp.getAttributes().get(key));
        } else {
            return null;
        }
    }

    /**
     * Read a parameter as a boolean.
     * 
     * @param key
     *            of the parameter to read.
     * @param comp
     *            whose parameters to read.
     * @param fctx
     *            in which the component is located.
     * @return the parameter of the component with the given key.
     */
    protected static Boolean readParamBoolean(String key, UIComponent comp, FacesContext fctx) {
        ValueExpression valueExpression = comp.getValueExpression(key);

        if (valueExpression != null) {
            return Boolean.parseBoolean((String) valueExpression.getValue(fctx.getELContext()));
        }

        if (comp.getAttributes().containsKey(key)) {
            return Boolean.parseBoolean((String) comp.getAttributes().get(key));
        } else {
            return null;
        }
    }

    /**
     * Get the message for the given sting 
     * 
     * @param fctx of actual page
     * @param key of the message
     * @return message value to the given string
     */
    protected static String getDefaultMessage(FacesContext fctx, String key) {
        String returnValue = null;
        try {
            ResourceBundle rBundle = ResourceBundle.getBundle(
                    "hu.configuration.CommonMessages", fctx.getViewRoot()
                    .getLocale());
            returnValue = rBundle.getString(key);
        } catch (MissingResourceException e) {
            return "Error";
        }
        
        return returnValue;
    }
    
    protected static FacesMessage getValidatorMessage(String msgKey, String defaultMessage, FacesContext fctx, UIComponent comp) {
        FacesMessage returnMessage = new FacesMessage();
        returnMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
        
        String msg = readParamString(msgKey, comp, fctx);
        if (msg != null) {
            returnMessage.setSummary(msg);
        } else {
            returnMessage.setSummary(getDefaultMessage(fctx, defaultMessage));
        }
        return returnMessage;
    }
}
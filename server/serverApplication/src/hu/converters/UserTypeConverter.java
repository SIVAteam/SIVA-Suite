package hu.converters;

import hu.model.users.EUserType;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * This class implements a {@link Converter} to convert user input to a
 * representation using {@link EUserType} and vice versa.
 */
public class UserTypeConverter implements Converter {
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAsObject(FacesContext fctx, UIComponent comp, String data) {
        EUserType returnUserType = null;
        if (data != null && data.length() > 0) {
            if (data.equals(EUserType.Administrator.toString())) {
                returnUserType = EUserType.Administrator;
            }
            else if (data.equals(EUserType.Anonymous.toString())) {
                returnUserType = EUserType.Anonymous;
            }
            else if (data.equals(EUserType.Participant.toString())) {
                returnUserType = EUserType.Participant;
            }
            else if (data.equals(EUserType.Tutor.toString())) {
                returnUserType = EUserType.Tutor;
            }
        }
        return returnUserType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString(FacesContext fctx, UIComponent comp, Object data) {
        if (data != null) {
            return data.toString();
        }
        return null;
    }
}
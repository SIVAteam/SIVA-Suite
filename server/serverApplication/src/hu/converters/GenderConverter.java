package hu.converters;

import hu.model.users.EGender;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * This class implements a {@link Converter} to convert user input to a
 * representation using {@link EGender} and vice versa.
 */
public class GenderConverter implements Converter {
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAsObject(FacesContext fctx, UIComponent comp, String data) {
        EGender returnGender = null;
        if (data != null && data.length() > 0) {
            if (data.equals(EGender.Male.toString())) {
                returnGender = EGender.Male;
            }
            else if (data.equals(EGender.Female.toString())) {
                returnGender = EGender.Female;
            }
        }
        return returnGender;
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
package hu.converters;

import hu.model.EParticipationRestriction;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * This class implements a {@link Converter} to convert user input to a
 * representation using {@link EParticipationRestriction} and vice versa.
 */
public class ParticipationRestrictionConverter implements Converter {
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAsObject(FacesContext fctx, UIComponent comp, String data) {
        EParticipationRestriction returnParticipationRestriction = null;
        if (data != null && data.length() > 0) {
            if (data.equals(EParticipationRestriction.GroupAttendants.toString())) {
                returnParticipationRestriction = EParticipationRestriction.GroupAttendants;
            }
            else if (data.equals(EParticipationRestriction.Password.toString())) {
                returnParticipationRestriction = EParticipationRestriction.Password;
            }
            else if (data.equals(EParticipationRestriction.Private.toString())) {
                returnParticipationRestriction = EParticipationRestriction.Private;
            }
            else if (data.equals(EParticipationRestriction.Public.toString())) {
                returnParticipationRestriction = EParticipationRestriction.Public;
            }
            else if (data.equals(EParticipationRestriction.Registered.toString())) {
                returnParticipationRestriction = EParticipationRestriction.Registered;
            }
            else if (data.equals(EParticipationRestriction.Token.toString())) {
                returnParticipationRestriction = EParticipationRestriction.Token;
            }
        }
        return returnParticipationRestriction;
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
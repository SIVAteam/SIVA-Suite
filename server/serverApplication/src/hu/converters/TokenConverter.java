package hu.converters;

import hu.model.Token;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * This class implements a {@link Converter} to convert user input to
 * {@link Token} objects and vice versa.
 */
@FacesConverter("hu.converters.tokenConverter")
public class TokenConverter implements Converter {
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAsObject(FacesContext fctx, UIComponent comp, String data) {
        Token returnToken = null;
        if (data != null && data.length() > 0) {
            returnToken = new Token(data);
        }
        return returnToken;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString(FacesContext fctx, UIComponent comp, Object data) {
        Token token = (Token) data;
        if (token != null) {
            return token.getToken();
        }
        return null;
    }
}
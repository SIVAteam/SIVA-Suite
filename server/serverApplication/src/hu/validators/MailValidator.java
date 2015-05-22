package hu.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * This class implements a {@link Validator} to check if a provided email
 * address is in the correct format. It is in the correct format, if it includes
 * an "at" symbol (@) and given symbols before and after. 
 * 
 * The default error message is defined in the parameter
 * "mailValidator_message".
 */
@FacesValidator("MailValidator")
public class MailValidator extends AValidator {
    private static final String DEFALULT_MSG = "mailValidator_message";
    private static final String EMAIL_PATTERN = 
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-.]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(FacesContext fctx, UIComponent comp, Object data)
            throws ValidatorException {
        String email = (String) data;
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            throw new ValidatorException(getValidatorMessage(DEFALULT_MSG,
                    DEFALULT_MSG, fctx, comp));
        }
    }
}
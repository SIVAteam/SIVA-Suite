package hu.validators;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * This class implements a {@link Validator} to do generic length checking on
 * text input provided by the user. It tests if the length of the given input is
 * smaller or equal to the given maximum length.
 * 
 * The minimum length is defined by the parameter "minLengthValidator". The
 * maximum length is defined by the parameter "maxLengthValidator". The error
 * message for a too short input is defined in the parameter
 * "lengthValidator_tooShort_message". The error message for a too long input is
 * defined in the parameter "lengthValidator_tooLong_message". The default error
 * message is defined in the parameter "lengthValidator_message".
 */
@FacesValidator("LengthValidator")
public class LengthValidator extends AValidator {
    private static final String MIN_LENGTH = "minLengthValidator";
    private static final String MAX_LENGTH = "maxLengthValidator";
    private static final String TOO_SHORT_MSG = "lengthValidator_tooShort_message";
    private static final String TOO_LONG_MSG = "lengthValidator_tooLong_message";
    private static final String DEFAULT_MSG = "lengthValidator_message";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(FacesContext fctx, UIComponent comp, Object data)
            throws ValidatorException {
        Integer minLength = readParamInt(MIN_LENGTH, comp, fctx);
        Integer maxLength = readParamInt(MAX_LENGTH, comp, fctx);
        String inputText = (String) data;
        Integer inputLength = inputText.length();

        if (minLength != null) {
            if (inputLength < minLength) {
                throw new ValidatorException(getValidatorMessage(TOO_SHORT_MSG,
                        DEFAULT_MSG, fctx, comp));
            }
        }
        if (maxLength != null) {
            if (inputLength > maxLength) {
                throw new ValidatorException(getValidatorMessage(TOO_LONG_MSG,
                        DEFAULT_MSG, fctx, comp));
            }
        }
    }
}
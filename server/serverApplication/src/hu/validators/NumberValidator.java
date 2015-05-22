package hu.validators;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * This class implements a {@link Validator} to do generic checking on numeric
 * input provided by the user. E.g. if the input is between or equal the given
 * lowest and highest number.
 * 
 * The highest number is defined by the parameter "maxNumberValidator". The
 * lowest number is defined by the parameter "minNumberValidator". The error
 * message for a too small input is defined in the parameter
 * "numberValidator_tooSmall_message". The error message for a too big input is
 * defined in the parameter "numberValidator_tooBig_message". The default error
 * message is defined in the parameter "numberValidator_message". 
 */
@FacesValidator("NumberValidator")
public class NumberValidator extends AValidator {
    private static final String MAX_NUMBER = "maxNumberValidator";
    private static final String MIN_NUMBER = "minNumberValidator";
    private static final String TOO_SMALL_MSG = "numberValidator_tooSmall_message";
    private static final String TOO_BIG_MSG = "numberValidator_tooBig_message";
    private static final String DEFAULT_MSG = "numberValidator_message";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(FacesContext fctx, UIComponent comp, Object data)
            throws ValidatorException {
        Integer userInputNumber = Integer.parseInt(data.toString());
        Integer minNumber = readParamInt(MIN_NUMBER, comp, fctx);
        Integer maxNumber = readParamInt(MAX_NUMBER, comp, fctx);

        if (maxNumber != null) {
            if (userInputNumber > maxNumber) {
                throw new ValidatorException(getValidatorMessage(TOO_BIG_MSG,
                        DEFAULT_MSG, fctx, comp));
            }
        }
        if (minNumber != null) {
            if (userInputNumber < minNumber) {
                throw new ValidatorException(getValidatorMessage(TOO_SMALL_MSG,
                        DEFAULT_MSG, fctx, comp));
            }
        }
    }
}
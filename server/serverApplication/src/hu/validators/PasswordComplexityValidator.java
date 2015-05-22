package hu.validators;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * This class implements a {@link Validator} to test, if the provided password
 * is complex enough to be considered secure. The given password is complex
 * enough if it has reached a minimum length. It has to include three different
 * parts of charsets, chosen from numbers, lower case, upper case and special
 * characters. E.g the password is long enough and consists lower case, upper
 * case and numbers or upper case, numbers and special characters.
 * 
 * The minimum length is given by the parameter "lengthPasswordValidator" The
 * error message for a too short password is defined in the parameter
 * "passwordComplexityValidator_tooShort_message". The error message for an
 * input with not enough charsets is defined in the parameter
 * "passwordComplexityValidator_charset_message".
 */
@FacesValidator("PasswordComplexityValidator")
public class PasswordComplexityValidator extends AValidator {
    private static final String MIN_LENGTH = "lengthPasswordValidator";
    private static final String TOO_SHORT_MSG = "passwordComplexityValidator_tooShort_message";
    private static final String CHARSET_MSG = "passwordComplexityValidator_charset_message";
    private static final String DEFAULT_MSG = "passwordComplexityValidator_message";

    private static final String SPECIAL_CHARACTERS = "[/,:<>!~§@#$%^&()+=?()\"|!\\[#$-]";
    private static final String NUMERIC_PATTERN = "(?=.*\\d).*";
    private static final String LOWER_CASE_PATTERN = "(?=.*[a-z]).*";
    private static final String UPPER_CASE_PATTERN = "(?=.*[A-Z]).*";
    private static final String SPECIAL_PATTERN = "(?=.*" + SPECIAL_CHARACTERS
            + ").*";
    private static final int PATTERNS_TO_MATCH = 3;

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(FacesContext fctx, UIComponent comp, Object data)
            throws ValidatorException {
        String inputPassword = (String) data;
        Integer minimumLength = readParamInt(MIN_LENGTH, comp, fctx);

        if (minimumLength != null) {
            if (inputPassword.length() < minimumLength) {
                throw new ValidatorException(getValidatorMessage(TOO_SHORT_MSG,
                        DEFAULT_MSG, fctx, comp));
            }
        }

        // check charsets complexity
        int matchCounter = 0;
        if (inputPassword.matches(NUMERIC_PATTERN)) {
            matchCounter++;
        }
        if (inputPassword.matches(LOWER_CASE_PATTERN)) {
            matchCounter++;
        }
        if (inputPassword.matches(UPPER_CASE_PATTERN)) {
            matchCounter++;
        }
        if (inputPassword.matches(SPECIAL_PATTERN)) {
            matchCounter++;
        }

        if (matchCounter < PATTERNS_TO_MATCH) {
            throw new ValidatorException(getValidatorMessage(CHARSET_MSG,
                    CHARSET_MSG, fctx, comp));
        }
    }
}
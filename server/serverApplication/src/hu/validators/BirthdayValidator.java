package hu.validators;

import java.util.Calendar;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * This class implements a {@link Validator} to test if a given birthday is in
 * the correct format, e.g. a valid date in the past.
 * 
 * The minimum age is set by the parameter "minBirthdayValidator". The maximum
 * age is set by the parameter "maxBirthdayValidator". The error message for a
 * date in the future is defined in the parameter
 * "birthdayValidator_inFuture_message". The error message for a too young user is
 * defined in the parameter "tbirthdayValidator_tooYoung_message". The error
 * message for a too old user is defined in the parameter
 * "birthdayValidator_tooOld_message". The default error message is defined in the
 * parameter "birthdayValidator_message".
 */
@FacesValidator("BirthdayValidator")
public class BirthdayValidator extends AValidator {
    private static final String MIN_BIRTHDAY = "minBirthdayValidator";
    private static final String MAX_BIRTHDAY = "maxBirthdayValidator";
    private static final String IN_FUTURE_MSG = "birthdayValidator_inFuture_message";
    private static final String TOO_YOUNG_MSG = "birthdayValidator_tooYoung_message";
    private static final String TOO_OLD_MSG = "birthdayValidator_tooOld_message";
    private static final String DEFAULT_MSG = "birthdayValidator_message";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(FacesContext fctx, UIComponent comp, Object data)
            throws ValidatorException {
        Integer minAge = readParamInt(MIN_BIRTHDAY, comp, fctx);
        Integer maxAge = readParamInt(MAX_BIRTHDAY, comp, fctx);
        Calendar now = Calendar.getInstance();
        Calendar birthday = Calendar.getInstance();
        birthday.setTime((Date) data);

        Integer age = now.get(Calendar.YEAR)
                - birthday.get(Calendar.YEAR)
                - (now.get(Calendar.MONTH) > birthday.get(Calendar.MONTH) ? 1
                        : 0);

        if (birthday.after(now)) {
            throw new ValidatorException(getValidatorMessage(IN_FUTURE_MSG,
                    DEFAULT_MSG, fctx, comp));
        } else if (minAge != null) {
            if (age < minAge) {
                throw new ValidatorException(getValidatorMessage(TOO_YOUNG_MSG,
                        DEFAULT_MSG, fctx, comp));
            }
        } else if (maxAge != null) {
            if (age > maxAge) {
                throw new ValidatorException(getValidatorMessage(TOO_OLD_MSG,
                        DEFAULT_MSG, fctx, comp));
            }
        }
    }
}
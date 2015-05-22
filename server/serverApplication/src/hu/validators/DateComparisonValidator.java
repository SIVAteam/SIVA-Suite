package hu.validators;

import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * This class implements a {@link Validator} to compare two {@link Date}s.
 * 
 * The other field to compare to is specified in
 * "dateComparisonValidatorCompareToId". The options
 * "dateComparisonValidatorOtherIsSmaller" and
 * "dateComparisonValidatorOtherIsGreater" allow to configure how the comparison
 * is applied. "dateComparisonValidatorThisTooSmallMessage" and
 * "dateComparisonValidatorThisTooLargeMessage" define error messages in case
 * the comparison fails. "dateComparisonValidatorOtherMissingMessage" and
 * "dateComparisonValidatorThisMissingMessage" define the error messages in case
 * one date is missing.
 */
@FacesValidator("DateComparisonValidator")
public class DateComparisonValidator extends AValidator {
    private static final String COMPARE_TO_ID = "dateComparisonValidatorCompareToId";
    private static final String OTHER_IS_SMALLER = "dateComparisonValidatorOtherIsSmaller";
    private static final String OTHER_IS_GREATER = "dateComparisonValidatorOtherIsGreater";
    private static final String OTHER_MISSING = "dateComparisonValidatorOtherMissingMessage";
    private static final String THIS_TOO_SMALL_MSG = "dateComparisonValidatorThisTooSmallMessage";
    private static final String THIS_TOO_LARGE_MSG = "dateComparisonValidatorThisTooLargeMessage";
    private static final String THIS_MISSING = "dateComparisonValidatorThisMissingMessage";
    private static final String DEFAULT_MSG = "dateComparisonValidator_message";

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(FacesContext fctx, UIComponent comp, Object data)
            throws ValidatorException {
        Boolean otherGreater = DateComparisonValidator.readParamBoolean(OTHER_IS_GREATER, comp, fctx);
        Boolean otherSmaller = DateComparisonValidator.readParamBoolean(OTHER_IS_SMALLER, comp, fctx);
        otherGreater = (otherGreater == null) ? false : otherGreater;
        otherSmaller = (otherSmaller == null) ? false : otherSmaller;
        String otherId = DateComparisonValidator.readParamString(COMPARE_TO_ID, comp, fctx);
        
        if (otherId == null || otherGreater == otherSmaller) {
            throw new IllegalArgumentException("Illegal configuration for DateComparisonValidator");
        }
        
        Date ourDate = (Date) data;
        UIInput input = (UIInput) fctx.getViewRoot().findComponent(otherId);
        Date otherDate = (Date) input.getValue();
        
        if (ourDate == null) {
            throw new ValidatorException(DateComparisonValidator.getValidatorMessage(THIS_MISSING,
                    DEFAULT_MSG, fctx, comp));
        }
        
        if (otherDate == null) {
            throw new ValidatorException(DateComparisonValidator.getValidatorMessage(OTHER_MISSING,
                    DEFAULT_MSG, fctx, comp));
        }
        
        if (otherGreater && otherDate.compareTo(ourDate) < 0) {
            throw new ValidatorException(DateComparisonValidator.getValidatorMessage(THIS_TOO_LARGE_MSG,
                    DEFAULT_MSG, fctx, comp));
        }
        
        if (otherSmaller && otherDate.compareTo(ourDate) > 0) {
            throw new ValidatorException(DateComparisonValidator.getValidatorMessage(THIS_TOO_SMALL_MSG,
                    DEFAULT_MSG, fctx, comp));
        }
        
    }
}

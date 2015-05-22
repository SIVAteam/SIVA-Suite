package hu.validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;

/**
 * This class implements a {@link Validator} to test if an uploaded file is
 * using the correct file type and is not too big.
 */
@FacesValidator("UploadValidator")
public class UploadValidator extends AValidator {
	private static final String MAX_SIZE = "maxSize";
    private static final String FILE_TYPES = "fileTypes";
    private static final String TOO_BIG_MSG = "uploadValidator_tooBig_message";
    private static final String WRONG_FILETYPE_MSG = "uploadValidator_wrongFileType_message";
    private static final String DEFAULT_MSG = "uploadValidator_message";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate(FacesContext fctx, UIComponent comp, Object data)
			throws ValidatorException {
		Part video = (Part) data;
		Integer maxSize = readParamInt(MAX_SIZE, comp, fctx);
        String fileTypes = readParamString(FILE_TYPES, comp, fctx);
        
		if (maxSize != null && video.getSize() > 1024 * 1024 * maxSize) {
			throw new ValidatorException(new FacesMessage(String.format(getValidatorMessage(TOO_BIG_MSG,
					DEFAULT_MSG, fctx, comp).getSummary(), maxSize)));
		}
		
		if (fileTypes != null){
			String types[] = fileTypes.split(",");
			boolean found = false;
			for(String type: types){
				if(type.equals(video.getContentType())){
					found = true;
					break;
				}
			}
			
			if(!found){
				throw new ValidatorException(getValidatorMessage(WRONG_FILETYPE_MSG,
						DEFAULT_MSG, fctx, comp));
			}			
		}
	}
}
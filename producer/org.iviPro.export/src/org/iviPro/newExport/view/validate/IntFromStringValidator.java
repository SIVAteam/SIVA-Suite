package org.iviPro.newExport.view.validate;

import org.eclipse.jface.fieldassist.ControlDecoration;

public class IntFromStringValidator extends DecoratingStringValidator {

	public IntFromStringValidator(ControlDecoration controlDecoration, String message) {
		super(controlDecoration, message);
	}

	@Override
	protected boolean check(String value) {
		try {
			Integer.valueOf(value);
		} catch (NumberFormatException ignore) {
			return true;
		}
		return false;
	}

}

package org.iviPro.newExport.view.validate;

import org.eclipse.jface.fieldassist.ControlDecoration;

public class StringNotEmptyValidator extends DecoratingStringValidator {

	public StringNotEmptyValidator(ControlDecoration controlDecoration,
			String message) {
		super(controlDecoration, message);
	}

	@Override
	protected boolean check(String value) {
		return value.trim().length() <= 0;
	}

}

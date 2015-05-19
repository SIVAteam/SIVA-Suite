package org.iviPro.newExport.view.validate;

import org.eclipse.jface.fieldassist.ControlDecoration;

public class StringLengthValidator extends DecoratingStringValidator {

	private final int maxLength;

	public StringLengthValidator(ControlDecoration controlDecoration,
			String message, int maxLength) {
		super(controlDecoration, message);
		if (maxLength < 0) {
			throw new IllegalArgumentException(
					"The maximum length has to be positive!");
		}
		this.maxLength = maxLength;
	}

	@Override
	protected boolean check(String value) {
		return value.trim().length() > maxLength;
	}

}

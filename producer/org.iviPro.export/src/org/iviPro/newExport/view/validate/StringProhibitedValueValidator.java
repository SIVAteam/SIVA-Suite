package org.iviPro.newExport.view.validate;

import java.util.List;

import org.eclipse.jface.fieldassist.ControlDecoration;

public class StringProhibitedValueValidator extends DecoratingStringValidator {

	private final List<String> prohibitedValues;
	private final boolean ignoreCase;

	public StringProhibitedValueValidator(ControlDecoration controlDecoration,
			String message, List<String> prohibitedValues, boolean ignoreCase) {
		super(controlDecoration, message);
		this.prohibitedValues = prohibitedValues;
		this.ignoreCase = ignoreCase;
	}

	@Override
	protected boolean check(String value) {
		if (ignoreCase) {
			for (String prohibitedValue : prohibitedValues) {
				if (value.equalsIgnoreCase(prohibitedValue)) {
					return true;
				}
			}
		} else {
			for (String prohibitedValue : prohibitedValues) {
				if (value.trim().equals(prohibitedValue)) {
					return true;
				}
			}
		}
		return false;
	}
}

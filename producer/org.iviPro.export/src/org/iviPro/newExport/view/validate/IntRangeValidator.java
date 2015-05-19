package org.iviPro.newExport.view.validate;

import org.eclipse.jface.fieldassist.ControlDecoration;

public class IntRangeValidator extends DecoratingIntValidator {

	private final int lowerBoundary;
	private final int upperBoundary;

	public IntRangeValidator(ControlDecoration controlDecoration,
			String message, int lowerBoundary, int upperBoundary) {
		super(controlDecoration, message);
		if (upperBoundary < lowerBoundary) {
			throw new IllegalArgumentException(
					"The upper boundary has to be greather than the lower boundary!");
		}
		this.lowerBoundary = lowerBoundary;
		this.upperBoundary = upperBoundary;
	}

	@Override
	protected boolean check(int value) {
		return value < lowerBoundary || value > upperBoundary;
	}

}

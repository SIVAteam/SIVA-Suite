package org.iviPro.newExport.view.validate;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.ControlDecoration;

public abstract class DecoratingIntValidator implements IValidator {

	private final ControlDecoration controlDecoration;
	private final String message;

	protected DecoratingIntValidator(ControlDecoration controlDecoration,
			String message) {
		super();
		this.controlDecoration = controlDecoration;
		this.message = message;
	}

	@Override
	public final IStatus validate(Object value) {
		if (check(((Integer) value).intValue())) {
			controlDecoration.show();
			return ValidationStatus.error(message);
		}
		controlDecoration.hide();
		return Status.OK_STATUS;
	}

	protected abstract boolean check(int value);

}

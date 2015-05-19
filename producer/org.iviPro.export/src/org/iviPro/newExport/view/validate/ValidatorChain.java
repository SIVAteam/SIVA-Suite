package org.iviPro.newExport.view.validate;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ValidatorChain implements IValidator {

	private final IValidator[] validators;

	public ValidatorChain(IValidator... validators) {
		super();
		this.validators = validators;
	}

	@Override
	public IStatus validate(Object value) {
		for (IValidator validator : validators) {
			IStatus status = validator.validate(value);
			if (status != Status.OK_STATUS) {
				return status;
			}
		}
		return Status.OK_STATUS;
	}

}

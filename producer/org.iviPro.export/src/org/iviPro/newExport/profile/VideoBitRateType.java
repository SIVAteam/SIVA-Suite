package org.iviPro.newExport.profile;

import org.iviPro.newExport.ExportException;

public enum VideoBitRateType {

	ORIGINAL("Original", "original"), FIXED("Fixed", "fixed");

	private final String label;
	private final String parameter;

	private VideoBitRateType(String label, String parameter) {
		this.label = label;
		this.parameter = parameter;
	}

	public String getLabel() {
		return label;
	}

	public String getParameter() {
		return parameter;
	}

	@Override
	public String toString() {
		return label;
	}

	public static VideoBitRateType fromParameter(String parameter)
			throws ExportException {
		if (parameter.equals(ORIGINAL.parameter)) {
			return ORIGINAL;
		} else if (parameter.equals(FIXED.parameter)) {
			return FIXED;
		} else {
			throw new ExportException(
					String.format(
							"Could not find a video bit rate type matching parameter '%s'.",
							parameter));
		}
	}
}

package org.iviPro.newExport.profile;

import org.iviPro.newExport.ExportException;

public enum VideoDimensionType {

	PROJECT("Project", "project"), ORIGINAL("Original", "original"), FIXED(
			"Fixed", "fixed");

	private final String label;
	private final String parameter;

	private VideoDimensionType(String label, String parameter) {
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

	public static VideoDimensionType fromParameter(String parameter)
			throws ExportException {
		if (parameter.equals(ORIGINAL.parameter)) {
			return ORIGINAL;
		} else if (parameter.equals(PROJECT.parameter)) {
			return PROJECT;
		} else if (parameter.equals(FIXED.parameter)) {
			return FIXED;
		} else {
			throw new ExportException(
					String.format(
							"Could not find a video dimension type matching parameter '%s'.",
							parameter));
		}
	}
}

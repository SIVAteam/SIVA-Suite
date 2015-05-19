package org.iviPro.newExport.profile;

import java.io.File;

import org.iviPro.newExport.util.FileUtils;

public class ExportDirectories {

	private final File outputFolder;
	private final File tmpOutputFolder;

	public ExportDirectories(File outputFolder, String exportTitle) {
		this.outputFolder = new File(outputFolder + File.separator
				+ exportTitle + File.separator);
		this.tmpOutputFolder = new File(FileUtils.TEMP_DIRECTORY
				+ File.separator + exportTitle + File.separator);
	}

	public File getOutputFolder() {
		return outputFolder;
	}

	public File getTmpOutputFolder() {
		return tmpOutputFolder;
	}

	@Override
	public String toString() {
		return "ExtendedExportProfile [outputFolder=" + outputFolder //$NON-NLS-1$
				+ ", tmpOutputFolder=" + tmpOutputFolder + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}

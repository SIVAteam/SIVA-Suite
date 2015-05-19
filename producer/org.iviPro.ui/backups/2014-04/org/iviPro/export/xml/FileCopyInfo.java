package org.iviPro.export.xml;

import java.util.Locale;
import org.iviPro.model.IFileBasedObject;

public class FileCopyInfo implements Comparable<FileCopyInfo> {

	public final String sourcePath;
	public final String targetFilename;
	public final Locale locale;
	public final IFileBasedObject sourceObject;
	public final ExportParameters parameters;

	public FileCopyInfo(String sourcePath, String targetPath, Locale locale,
			IFileBasedObject sourceObject, ExportParameters parameters) {
		super();
		this.sourcePath = sourcePath;
		this.targetFilename = targetPath;
		this.locale = locale;
		this.sourceObject = sourceObject;
		this.parameters = parameters;
	}
	
	public ExportParameters getExportParameters() {
		return this.parameters;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof FileCopyInfo) {
			return targetFilename.equals(((FileCopyInfo) o).targetFilename);
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(FileCopyInfo o) {
		return targetFilename.compareTo(o.targetFilename);
	}

}

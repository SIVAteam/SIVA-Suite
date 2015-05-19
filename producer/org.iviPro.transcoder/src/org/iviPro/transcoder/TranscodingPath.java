package org.iviPro.transcoder;

import java.io.File;

public class TranscodingPath {

	private final File inputFile;
	private final String outputFileName;
	private final File outputFolder;

	public TranscodingPath(File inputFile, String outputFileName,
			File outputFolder) {
		this.inputFile = inputFile;
		this.outputFileName = outputFileName;
		this.outputFolder = outputFolder;
	}

	public TranscodingPath(File inputFile, File outputFile) {
		this.inputFile = inputFile;
		this.outputFileName = getNameWithoutExtension(outputFile);
		this.outputFolder = outputFile.getParentFile();
	}

	private String getNameWithoutExtension(File file) {
		return file.getName().substring(0, file.getName().lastIndexOf("."));
	}

	public File getInputFile() {
		return inputFile;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public File getOutputFolder() {
		return outputFolder;
	}

	@Override
	public String toString() {
		return "TranscodingPath [inputFile=" + inputFile + ", outputFileName="
				+ outputFileName + ", outputFolder=" + outputFolder + "]";
	}

}

package org.iviPro.transcoder;

import java.io.File;

import org.iviPro.transcoding.exception.TranscodingException;
import org.iviPro.transcoding.exception.TranscodingReason;
import org.iviPro.transcoding.exception.TranscodingReasonDescriptor;

public abstract class TranscodingDescriptor {

	private final TranscodingPath transcodingPath;
	private final boolean overwrite;
	private final MediaSection mediaSection;
	private final File output;

	public TranscodingDescriptor(TranscodingPath transcodingPath,
			boolean overwrite, MediaSection mediaSection, String fileExtension) {
		this.transcodingPath = transcodingPath;
		this.overwrite = overwrite;
		this.mediaSection = mediaSection;
		this.output = new File(transcodingPath.getOutputFolder(),
				transcodingPath.getOutputFileName() + "." + fileExtension);
	}

	public TranscodingPath getTranscodingPath() {
		return transcodingPath;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public MediaSection getMediaSection() {
		return mediaSection;
	}

	public File getInputFile() {
		return transcodingPath.getInputFile();
	}

	public File getOutputFile() {
		return output;
	}

	public void validateFiles() throws TranscodingException {
		if (!transcodingPath.getInputFile().canRead()) {
			throw new TranscodingException(new TranscodingReason(
					TranscodingReasonDescriptor.INPUT_FILE_NOT_READABLE,
					new String[] { transcodingPath.getInputFile().toString() }));
		}
		if (overwrite && output.exists() && !output.canWrite()) {
			throw new TranscodingException(new TranscodingReason(
					TranscodingReasonDescriptor.OUTPUT_FILE_NOT_WRITEABLE,
					new String[] { output.toString() }));
		}
		if (!overwrite && output.exists()) {
			throw new TranscodingException(new TranscodingReason(
					TranscodingReasonDescriptor.OUTPUT_FILE_ALREADY_EXISTS,
					new String[] { output.toString() }));
		}
	}

	@Override
	public String toString() {
		return "TranscodingDescriptor [transcodingPath=" + transcodingPath
				+ ", overwrite=" + overwrite + ", mediaSection=" + mediaSection
				+ ", output=" + output + "]";
	}

}

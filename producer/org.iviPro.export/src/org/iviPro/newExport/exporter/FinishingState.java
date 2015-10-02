package org.iviPro.newExport.exporter;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.ExportDirectories;
import org.iviPro.newExport.profile.ExportProfile;
import org.iviPro.newExport.util.FileUtils;
import org.iviPro.newExport.util.ZipUtils;

public class FinishingState extends ExporterState {

	private static Logger logger = Logger.getLogger(FinishingState.class);

	// @formatter:off
	private static final String LOG_COPYING_FILES = "%s Copying the exported files from the temporary output directory to the output directory."; //$NON-NLS-1$
	private static final String LOG_COPYING_OUTPUT = "%s Copying the contents of directory '%s' to directory '%s'."; //$NON-NLS-1$
	private static final String LOG_COMPRESS_OUTPUT = "%s Compressing the contents of directory '%s' into archive '%s'."; //$NON-NLS-1$
	// @formatter:on

	public FinishingState(ExportProfile exportProfile,
			ExportDirectories exportDirectories) {
		super(TaskSettings.CLEANUP, exportProfile, exportDirectories);
	}

	// TODO Give feedback about the finished export!

	public void run(Exporter exporter, IProgressMonitor monitor)
			throws ExportException {
		logger.debug(String.format(LOG_COPYING_FILES, loggerPrefix));
		try {
			// TODO Delete the files in the temporary directory!
			monitor.beginTask(taskSettings.getName(),
					taskSettings.getDuration());
			if (exportProfile.isCompress()) {
				File archive = new File(exportDirectories.getOutputFolder(),
						exportProfile.getProfileTitle()
								+ ExportProfile.ARCHIVE_EXTENSION);

				logger.debug(String.format(LOG_COMPRESS_OUTPUT, loggerPrefix,
						exportDirectories.getTmpOutputFolder(),
						archive.getAbsolutePath()));

				try {
					ZipUtils.zip(exportDirectories.getTmpOutputFolder(),
							archive, true, true);
					FileUtils.delete(exportDirectories.getTmpOutputFolder());
				} catch (IOException e) {
					throw new ExportException(
							Messages.Exception_CompressingOutputFailed);
				}
			} else {
				logger.debug(String.format(LOG_COPYING_OUTPUT, loggerPrefix,
						exportDirectories.getTmpOutputFolder(),
						exportDirectories.getOutputFolder()));

				try {
					FileUtils.copyDirectoryContent(
							exportDirectories.getTmpOutputFolder(),
							exportDirectories.getOutputFolder());
					FileUtils.delete(exportDirectories.getTmpOutputFolder());
				} catch (IOException cause) {
					throw new ExportException(
							Messages.Exception_CopyingOutputFailed, cause);
				}
			}
		} finally {
			monitor.done();
		}
	}
}

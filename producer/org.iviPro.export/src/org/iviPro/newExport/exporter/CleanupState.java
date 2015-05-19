package org.iviPro.newExport.exporter;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.profile.ExportDirectories;
import org.iviPro.newExport.profile.ExportProfile;
import org.iviPro.newExport.util.FileUtils;

public class CleanupState extends ExporterState {

	// @formatter:off
	private static final String LOG_DELETE_TEMP_DIRECTORY = "%s Deleting the temporary export directory '%s'."; //$NON-NLS-1$
	// @formatter:on

	private static final Logger logger = Logger.getLogger(CleanupState.class);

	public CleanupState(ExportProfile exportProfile,
			ExportDirectories exportDirectories) {
		super(TaskSettings.CLEANUP, exportProfile, exportDirectories);
	}

	// TODO Give feedback about the failed export!
	
	public void run(Exporter exporter, IProgressMonitor monitor)
			throws ExportException {
		logger.debug(String.format(LOG_DELETE_TEMP_DIRECTORY, loggerPrefix,
				exportDirectories.getTmpOutputFolder()));
		try {
			monitor.beginTask(taskSettings.getName(),
					taskSettings.getDuration());
			if (exportDirectories.getTmpOutputFolder().exists()) {
				FileUtils.delete(exportDirectories.getTmpOutputFolder());
			}
		} finally {
			monitor.done();
		}
	}
}

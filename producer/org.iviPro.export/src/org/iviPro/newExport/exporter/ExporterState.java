package org.iviPro.newExport.exporter;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.ExportDirectories;
import org.iviPro.newExport.profile.ExportProfile;

public abstract class ExporterState {

	// @formatter:off
	static final String LOG_EXCEPTION_FORWARD = "%s %s"; //$NON-NLS-1$
	private static final String LOG_TASK_CANCELED = "%s Task was canceled by user... Interrupting the export job."; //$NON-NLS-1$
	private static final String LOGGER_PREFIX = "[%s]"; //$NON-NLS-1$
	// @formatter:on

	private static final Logger logger = Logger.getLogger(ExporterState.class);

	final TaskSettings taskSettings;
	final ExportDirectories exportDirectories;
	final ExportProfile exportProfile;
	final String loggerPrefix;

	ExporterState(TaskSettings taskSettings, ExportProfile exportProfile,
			ExportDirectories exportDirectories) {
		this.taskSettings = taskSettings;
		this.exportProfile = exportProfile;
		this.exportDirectories = exportDirectories;
		this.loggerPrefix = String.format(LOGGER_PREFIX,
				exportProfile.getProfileTitle());
	}

	abstract void run(Exporter exporter, IProgressMonitor monitor)
			throws ExportException;

	void checkCanceled(IProgressMonitor monitor) throws InterruptedException {
		if (monitor.isCanceled()) {
			logger.info(String.format(LOG_TASK_CANCELED, loggerPrefix));
			monitor.setTaskName(Messages.Canceled);
			monitor.subTask(Messages.Aborting);
			throw new InterruptedException(
					Messages.Exception_TaskCanceledByUser);
		}
	}
}

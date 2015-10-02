package org.iviPro.newExport.job;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.iviPro.model.Project;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.exporter.Exporter;
import org.iviPro.newExport.exporter.TaskSettings;
import org.iviPro.newExport.profile.ExportDirectories;
import org.iviPro.newExport.profile.ExportProfile;

public class ExportJob extends UIProcess {

	// @formatter:off
	private static final String LOG_EXPORT_START = "Starting export job '%s' in background."; //$NON-NLS-1$
	private static final String LOG_EXPORT_COMPLETED = "Finished exporting the project '%s' with export profile '%s'."; //$NON-NLS-1$
	private static final String LOG_EXPORT_FAILED = "Exporting the project '%s' with export profile '%s' failed!"; //$NON-NLS-1$
	// @formatter:on

	private static final Logger logger = Logger.getLogger(ExportJob.class);
	private static final String NAME = "Export"; //$NON-NLS-1$

	private final ExportProfile exportProfile;
	private final File outputFolder;
	private final Project project;

	public ExportJob(Project project, Display display,
			ExportProfile exportProfile, File outputFolder, int currentExport,
			int exportCount) {
		super(display, String.format("%s (%d/%d): %s" + " (%s)", NAME, //$NON-NLS-1$ //$NON-NLS-2$
				currentExport, exportCount, project.getTitle(),
				exportProfile.getProfileTitle()));
		this.exportProfile = exportProfile;
		this.outputFolder = outputFolder;
		this.project = project;
	}

	@Override
	public void runInBackground(IProgressMonitor monitor) {
		logger.debug(String.format(LOG_EXPORT_START, getName()));
		File projectFile = project.getFile().getValue();
		try {
			monitor.beginTask(TaskSettings.ALL.getName(),
					TaskSettings.ALL.getDuration());
			monitor.subTask(TaskSettings.PROJECT.getName());
			monitor.worked(TaskSettings.PROJECT.getDuration());

			new Exporter(project, exportProfile, new ExportDirectories(
					outputFolder, exportProfile.getProfileTitle()), monitor)
					.export();
		} catch (ExportException cause) {
			logger.error(cause.getMessage(), cause);
			logger.error(String.format(LOG_EXPORT_FAILED,
					projectFile.getName(), exportProfile.getProfileTitle()));
		} finally {
			monitor.done();
		}

		logger.info(String.format(LOG_EXPORT_COMPLETED, projectFile.getName(),
				exportProfile.getProfileTitle()));
	}

	@Override
	protected void runInUIThread() {
		// TODO Give Feedback!
	}
}

package org.iviPro.newExport.exporter;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.iviPro.model.Project;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.ExportDirectories;
import org.iviPro.newExport.profile.ExportProfile;
import org.iviPro.newExport.util.FileUtils;

public class PreparingState extends ExporterState {

	// @formatter:off
	private static final String LOG_PREPARE_EXPORT_DIRECTORIES = "%s Preparing the export directories."; //$NON-NLS-1$
	private static final String LOG_PREPARE_OUTPUT_DIRECTORY = "%s Preparing the output direcoty '%s'."; //$NON-NLS-1$
	private static final String LOG_PREPARE_TEMP_OUTPUT_DIRECTORY = "%s Preparing the temporary output direcoty '%s'."; //$NON-NLS-1$
	// @formatter:on

	private static final Logger logger = Logger.getLogger(PreparingState.class);

	private final Project project;

	public PreparingState(ExportProfile exportProfile,
			ExportDirectories exportDirectories, Project project) {
		super(TaskSettings.PREPARING, exportProfile, exportDirectories);
		this.project = project;
	}

	public void run(Exporter exporter, IProgressMonitor monitor)
			throws ExportException {
		logger.debug(String
				.format(LOG_PREPARE_EXPORT_DIRECTORIES, loggerPrefix));
		try {
			monitor.beginTask(taskSettings.getName(),
					taskSettings.getDuration());
			try {
				prepareTmpOutputDirectory();
				prepareOutputDirectory();
			} catch (SecurityException cause) {
				throw new ExportException(
						Messages.Exception_PreparingExportDirectoriesFailed, cause);
			}
		} finally {
			monitor.done();
		}

		exporter.switchState(new DescriptorExportingState(exportProfile,
				exportDirectories, project));
	}

	private void prepareTmpOutputDirectory() {
		logger.debug(String.format(LOG_PREPARE_TEMP_OUTPUT_DIRECTORY,
				loggerPrefix, exportDirectories.getTmpOutputFolder()));
		if (exportDirectories.getTmpOutputFolder().exists()) {
			FileUtils.clear(exportDirectories.getTmpOutputFolder());
		} else {
			exportDirectories.getTmpOutputFolder().mkdirs();
		}
	}

	private void prepareOutputDirectory() {
		logger.debug(String.format(LOG_PREPARE_OUTPUT_DIRECTORY, loggerPrefix,
				exportDirectories.getOutputFolder()));
		if (exportDirectories.getOutputFolder().exists()) {
			FileUtils.clear(exportDirectories.getOutputFolder());
		} else {
			exportDirectories.getOutputFolder().mkdirs();
		}
	}
}

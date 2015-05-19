package org.iviPro.newExport.exporter;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.iviPro.model.Project;
import org.iviPro.newExport.ExportDefinition;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.profile.ExportDirectories;
import org.iviPro.newExport.profile.ExportProfile;

public class Exporter implements ExportDefinition {

	// @formatter:off
	private static final String LOG_EXPORT_PROJECT = "Exporting the current project with export profile '%s' to '%s'."; //$NON-NLS-1$
	// @formatter:on

	private static final Logger logger = Logger.getLogger(Exporter.class);

	private final Project project;
	private final ExportProfile exportProfile;
	private final ExportDirectories exportDirectories;
	private final IProgressMonitor monitor;

	private ExporterState state;

	public Exporter(Project project, ExportProfile exportProfile,
			ExportDirectories exportDirectories, IProgressMonitor monitor) {
		this.project = project;
		this.exportProfile = exportProfile;
		this.exportDirectories = exportDirectories;
		this.monitor = monitor;
	}
	
	// TODO Give feedback about the starting export!

	public void export() throws ExportException {
		logger.debug(String.format(LOG_EXPORT_PROJECT, exportProfile.getProfileTitle(),
				exportDirectories.getOutputFolder()));
		switchState(new PreparingState(exportProfile, exportDirectories,
				project));
	}

	void switchState(ExporterState state) throws ExportException {
		monitor.subTask(state.taskSettings.getName());
		this.state = state;
		this.state.run(
				this,
				new SubProgressMonitor(monitor, state.taskSettings
						.getDuration()));
	}
}

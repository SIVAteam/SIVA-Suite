package org.iviPro.newExport.exporter;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.iviPro.model.Project;
import org.iviPro.newExport.ExportDefinition;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.descriptor.xml.XmlDescriptorExporter;
import org.iviPro.newExport.profile.ExportDirectories;
import org.iviPro.newExport.profile.ExportProfile;
import org.iviPro.newExport.resources.ProjectResources;
import org.iviPro.newExport.util.FileUtils;

public class DescriptorExportingState extends ExporterState implements
		ExportDefinition {

	// @formatter:off
	private static final String LOG_EXPORT_DESCRIPTORS = "%s Exporting the descriptors."; //$NON-NLS-1$
	private static final String LOG_EXPORT_DESCRIPTOR_XML = "%s Exporting the XML descriptor."; //$NON-NLS-1$
	private static final String LOG_EXPORT_DESCRIPTOR_SMIL = "%s Exporting the SMIL descriptor."; //$NON-NLS-1$
	// @formatter:on

	private static Logger logger = Logger
			.getLogger(DescriptorExportingState.class);

	private final Project project;

	DescriptorExportingState(ExportProfile exportProfile,
			ExportDirectories exportDirectories, Project project) {
		super(TaskSettings.DESCRIPTOR, exportProfile, exportDirectories);
		this.project = project;
	}

	public void run(Exporter exporter, IProgressMonitor monitor)
			throws ExportException {
		logger.debug(String.format(LOG_EXPORT_DESCRIPTORS, loggerPrefix));
		ProjectResources projectResources = new ProjectResources();
		try {
			monitor.beginTask(taskSettings.getName(),
					taskSettings.getDuration());

			checkCanceled(monitor);
			monitor.subTask(TaskSettings.DESCRIPTOR_XML.getName());
			if (exportProfile.getProfile().getGeneral().isExportXml()) {
				exportXml(projectResources);
			}
			monitor.worked(TaskSettings.DESCRIPTOR_XML.getDuration());

			checkCanceled(monitor);
			monitor.subTask(TaskSettings.DESCRIPTOR_SMIL.getName());
			if (exportProfile.getProfile().getGeneral().isExportSmil()) {
				exportSmil(projectResources);
			}
			monitor.worked(TaskSettings.DESCRIPTOR_SMIL.getDuration());
		} catch (InterruptedException e) {
			exporter.switchState(new CleanupState(exportProfile,
					exportDirectories));
		} finally {
			monitor.done();
		}
		
		exporter.switchState(new ResourcesExportingState(exportProfile,
				exportDirectories, projectResources));
	}

	private void exportXml(ProjectResources projectResources)
			throws ExportException {
		logger.debug(String.format(LOG_EXPORT_DESCRIPTOR_XML, loggerPrefix));

		File xmlDirectory = null;
		try {
			xmlDirectory = FileUtils
					.createSubdirectory(exportDirectories.getTmpOutputFolder(),
							exportProfile.getProfile().getGeneral()
									.getDescriptorDirectory());
		} catch (IOException e) {
			logger.error(String.format(LOG_EXCEPTION_FORWARD, loggerPrefix,
					e.getMessage()));
			throw new ExportException(
					Messages.Exception_CreatingDescriptorDirectoryFailed, e);
		}

		new XmlDescriptorExporter(project, exportProfile.getProfile(),
				xmlDirectory, projectResources).export();
	}

	private void exportSmil(ProjectResources projectResources)
			throws ExportException {
		logger.debug(String.format(LOG_EXPORT_DESCRIPTOR_SMIL, loggerPrefix));
		throw new ExportException(Messages.Exception_SmileNotSupported);
	}
}

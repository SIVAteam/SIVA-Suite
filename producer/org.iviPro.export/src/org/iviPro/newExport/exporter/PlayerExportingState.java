package org.iviPro.newExport.exporter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.ExportDirectories;
import org.iviPro.newExport.profile.ExportProfile;
import org.iviPro.newExport.util.PathHelper;

public class PlayerExportingState extends ExporterState {

	private final static Set<String> FILE_COPY_BLACKLIST = new HashSet<String>();
	{
		FILE_COPY_BLACKLIST.add(".svn"); //$NON-NLS-1$
	}

	// @formatter:off
	private static final String LOG_COPYING_PLAYER_FILES = "%s Copying player files."; //$NON-NLS-1$
	private static final String LOG_COPYING_FLASH_PLAYER_FILES = "%s Copying Flash player files."; //$NON-NLS-1$
	private static final String LOG_COPYING_HTML_PLAYER_FILES = "%s Copying HTML player files."; //$NON-NLS-1$
	// @formatter:on

	private static final Logger logger = Logger
			.getLogger(PlayerExportingState.class);

	PlayerExportingState(ExportProfile exportProfile,
			ExportDirectories exportDirectories) {
		super(TaskSettings.PLAYER, exportProfile, exportDirectories);
	}

	void run(Exporter exporter, IProgressMonitor monitor)
			throws ExportException {
		logger.debug(String.format(LOG_COPYING_PLAYER_FILES, loggerPrefix));
		try {
			monitor.beginTask(TaskSettings.PLAYER.getName(),
					TaskSettings.PLAYER.getDuration());

			checkCanceled(monitor);
			monitor.subTask(TaskSettings.PLAYER_FLASH.getName());
			logger.debug(String.format(LOG_COPYING_FLASH_PLAYER_FILES,
					loggerPrefix));
			if (exportProfile.getProfile().getGeneral().isExportFlashPlayer()) {
				PathHelper.copyFiles(PathHelper.FLASH_PLAYER_DIRECTORY,
						exportDirectories.getTmpOutputFolder(), true,
						FILE_COPY_BLACKLIST);
			}
			monitor.worked(TaskSettings.PLAYER_FLASH.getDuration());

			checkCanceled(monitor);
			monitor.subTask(TaskSettings.PLAYER_HTML.getName());
			logger.debug(String.format(LOG_COPYING_HTML_PLAYER_FILES,
					loggerPrefix));
			if (exportProfile.getProfile().getGeneral().isExportHtmlPlayer()) {
				PathHelper.copyFiles(PathHelper.HTML_PLAYER_DIRECTORY,
						exportDirectories.getTmpOutputFolder(), true,
						FILE_COPY_BLACKLIST);
			}
			monitor.worked(TaskSettings.PLAYER_HTML.getDuration());
		} catch (IOException cause) {
			logger.error(cause.getMessage(), cause);
			throw new ExportException(
					Messages.Exception_CopyingPlayerFilesFailed, cause);
		} catch (InterruptedException e) {
			exporter.switchState(new CleanupState(exportProfile,
					exportDirectories));
			// End export chain.
			return;
		} finally {
			monitor.done();
		}
		exporter.switchState(new FinishingState(exportProfile,
				exportDirectories));
	}
}

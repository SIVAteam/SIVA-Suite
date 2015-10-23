package org.iviPro.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.iviPro.application.Application;

/**
 * Helper-Class, which provides some static methods helpful for getting path
 * names, or certain parts of filenames like extensi ons.
 * 
 * @author dellwo
 * 
 */
public class PathHelper {
	
	/**
	 * The name of the system property which holds the path to the directory 
	 * where to look for libraries and native programs.
	 * <b>Note:</b><br>
	 * Changes to this value have to be carried over to 
	 * <code>org.iviPro.newExport.PathHelper</code> as well.
	 */
	public static final String SYSPROP_LIBDIR = "libdir"; //$NON-NLS-1$
	/**
	 * The name of the system property which holds the path to the directory 
	 * where application specific data is stored.
	 * <b>Note:</b><br>
	 * Changes to this value have to be carried over to 
	 * <code>org.iviPro.newExport.PathHelper</code> as well.
	 * Additionally the logger uses this property to store the log file.
	 */
	public static final String SYSPROP_SIVADIR = "user.siva"; //$NON-NLS-1$

	/**
	 * Cuts the extension of a filename of, e.g. 'test.jpg' results in 'test'.
	 * 
	 * @param filename
	 *            The filename whose extensions should be cut off.
	 * @return The filename without extension. If the given filename is null,
	 *         the result will also be null.
	 */
	public static String getFilenameWithoutExtension(String filename) {
		if (filename != null) {
			int dotPos = filename.lastIndexOf('.');
			if (dotPos > 0) {
				return filename.substring(0, dotPos);
			} else {
				return filename;
			}
		}
		return null;
	}

	/**
	 * Gibt die Erweiterung der angegebenen Datei zurueck. Wenn der Dateiname
	 * keine Endung hat, dann wird null zurueck gegeben.
	 * 
	 * @param file
	 *            Die Datei.
	 * @return Endung der Datei, oder null, wenn Datei keine Endung hat.
	 */
	public static String getExtension(File file) {
		String filename = file.getName();
		int dotPos = filename.lastIndexOf('.') + 1;
		if (dotPos > 0 && dotPos < filename.length()) {
			String extension = filename.substring(dotPos);
			return extension;
		}
		return null;
	}

	/**
	 * Gibt eine URL zu einer internen Datei des org.iviPro.ui Plugins an.
	 * 
	 * @param relativePath
	 *            Der relative Pfad im Plugin, beginnend mit einem Slash, z.B.
	 *            /etc/loggerconfig.ini
	 * @return URL zu dieser Datei.
	 */
	public static URL getInternalURL(String relativePath) {
		try {
			URL fileURL = FileLocator.find(Platform
					.getBundle(Application.PLUGIN_ID), new Path(
							relativePath), null); //$NON-NLS-1$
			return fileURL;
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
	}

	/**
	 * Gibt den Pfad zur loggerconfig.ini zurueck, in der die LOG4J
	 * Konfiguration enthalten ist.
	 * 
	 * @return
	 */
	public static URL getPathToLoggerIni() {
		try {
			URL fileURL = FileLocator.find(Platform
					.getBundle(Application.PLUGIN_ID), new Path(
					"/configs/loggerconfig.ini"), null); //$NON-NLS-1$
			return fileURL;
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
	}

	/**
	 * Kopiert eine Datei in eine andere Datei.
	 * 
	 * @param in
	 *            Zu kopierende Datei
	 * @param out
	 *            Ziel-Datei
	 * @param overwrite
	 *            Wenn die Ziel-Datei existiert und overwrite auf true gesetzt
	 *            ist, wird die Zieldatei überschrieben. Ansonsten wird eine
	 *            IOException geworfen. Existiert die Ziel-Datei nicht, hat der
	 *            Parameter keine Bedeutung.
	 * @throws IOException
	 *             Falls beim Kopieren ein Fehler auftrat oder wenn die
	 *             Ziel-Datei existierte und overwrite auf false gesetzt ist.
	 */
	public static void copyFile(File in, File out, boolean overwrite)
			throws IOException {
		if (out.exists() && !overwrite) {
			throw new IOException("Target file does already exist."); //$NON-NLS-1$
		}
		// Ausgabe-Datei erstellen, falls sie noch nicht existiert.
		if (!out.exists()) {
			if (!out.createNewFile()) {
				throw new IOException("File '" + out.getAbsolutePath() //$NON-NLS-1$
						+ "' could not be created."); //$NON-NLS-1$
			}
		}
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	/**
	 * Kopiert alle Dateien aus einem Ordner in einen anderen Ordner. Eventuell
	 * vorhandene Dateien werden ueberschrieben, wenn overwrite auf true gesetzt
	 * ist.
	 * 
	 * @param srcDir
	 *            Das Quell-Verzeichnis
	 * @param targetDir
	 *            Das Ziel-Verzeichnis.
	 * @param overwrite
	 *            Gibt an ob eventuell schon vorhandene Dateien ueberschrieben
	 *            werden sollen.
	 * @param blacklist
	 *            Dateien mit diesen Namen werden nicht mitkopiert.
	 * 
	 * @throws IOException
	 *             Wenn beim Kopieren ein Fehler auftrat
	 */
	public static void copyFiles(File srcDir, File targetDir,
			boolean overwrite, Set<String> blacklist) throws IOException {
		// Falls Ziel-Verzeichnis nicht existiert, erstellen wir es
		if (!targetDir.exists()) {
			if (!targetDir.mkdirs()) {
				throw new IOException("Directory '" + targetDir //$NON-NLS-1$
						+ "' could not be created."); //$NON-NLS-1$
			}
		}
		// Teste das Quelle und Ziel les- bzw schreibbar sind.
		if (!(srcDir.exists() && srcDir.isDirectory() && srcDir.canRead())) {
			throw new IOException("Directory '" + srcDir + "' can't be read."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!(targetDir.exists() && targetDir.isDirectory() && targetDir
				.canWrite())) {
			throw new IOException("Directory '" + targetDir //$NON-NLS-1$
					+ "' can't be written."); //$NON-NLS-1$
		}
		// Kopiere alle Dateien
		File[] childs = srcDir.listFiles();
		for (File src : childs) {
			String srcName = src.getName();
			// Nur wenn Datei nicht in Blacklist ist kopieren
			if (!blacklist.contains(srcName)) {
				File target = new File(targetDir, srcName);
				if (src.isFile()) {
					// Wenn child eine Datei ist, kopiere sie falls Ziel nicht
					// existiert oder ueberschrieben werden darf.
					if (!target.exists() || overwrite) {
						copyFile(src, target, overwrite);
					}
				} else {
					// Wenn child ein Verzeichnis ist, kopiere alls Dateien in
					// das
					// entsprechende Zielverzeichnis. Existiert das
					// Zielverzeichnis
					// noch nicht, wird es erstellt.
					copyFiles(src, target, overwrite, blacklist);
				}
			}
		}
	}

	public static File getPathToVLC() {
		String libDir = System.getProperty(SYSPROP_LIBDIR);
		return new File(libDir + File.separatorChar
				+ "vlclibs" + File.separatorChar); //$NON-NLS-1$
	}

	/**
	 * Gibt den Pfad zur ffmpeg_export.exe zurueck
	 * 
	 * @return
	 */
	public static File getPathToFFMpegExe() {
		String libDir = System.getProperty(SYSPROP_LIBDIR);
		return new File(libDir + File.separatorChar + "ffmpeg.exe"); //$NON-NLS-1$
	}

	/**
	 * Gibt den Pfad zur ffprobe.exe zurueck
	 * 
	 * @return
	 */
	public static File getPathToFFProbeExe() {
		String libDir = System.getProperty(SYSPROP_LIBDIR);
		return new File(libDir + File.separatorChar + "ffprobe.exe"); //$NON-NLS-1$
	}
	
	public static File getPathToImageCache() {
		return new File(getSivaDir() + File.separatorChar + "ImageCache"); //$NON-NLS-1$
	}

	/* Speichert das App-Data Directory, wo wir unsere Arbeitsdaten ablegen */
	private static File appDataDirectory = null;

	/**
	 * Gibt den Pfad auf das App-Data Verzeichnis zurueck
	 * 
	 * @param applicationId
	 *            Ein eindeutiger Identifier der Anwendung dessen App-Data
	 *            Verzeichnis zurueck gegeben werden soll.
	 * @return
	 */
	public static File getAppDataDirectory(String applicationId) {
		if (appDataDirectory != null) {
			return appDataDirectory;
		}
		File directory = null;
		String userHome = null;
		try {
			userHome = System.getProperty("user.home"); //$NON-NLS-1$
		} catch (SecurityException ignore) {
		}
		if (userHome != null) {
			OSId osId = getOSId();
			if (osId == OSId.WINDOWS) {
				File appDataDir = null;
				try {
					String appDataEV = System.getenv("LOCALAPPDATA"); //$NON-NLS-1$
					if ((appDataEV != null) && (appDataEV.length() > 0)) {
						appDataDir = new File(appDataEV);
					}
				} catch (SecurityException ignore) {
				}
				if ((appDataDir != null) && appDataDir.isDirectory()) {
					// ${APPDATA}\${applicationId}
					String path = applicationId + "\\"; //$NON-NLS-1$
					directory = new File(appDataDir, path);
				} else {
					// ${userHome}\Application Data\${applicationId}
					String path = "Application Data\\" + applicationId + "\\"; //$NON-NLS-1$ //$NON-NLS-2$
					directory = new File(userHome, path);
				}
			} else if (osId == OSId.OSX) {
				// ${userHome}/Library/Application Support/${applicationId}
				String path = "Library/Application Support/" + applicationId //$NON-NLS-1$
						+ "/"; //$NON-NLS-1$
				directory = new File(userHome, path);
			} else {
				// ${userHome}/.${applicationId}/
				String path = "." + applicationId + "/"; //$NON-NLS-1$ //$NON-NLS-2$
				directory = new File(userHome, path);
			}
		}
		appDataDirectory = directory;
		return directory;
	}

	/*
	 * The following enum and method only exist to distinguish Windows and OSX
	 * for the sake of getDirectory().
	 */
	private enum OSId {
		WINDOWS, OSX, UNIX
	}

	private static OSId getOSId() {
		PrivilegedAction<String> doGetOSName = new PrivilegedAction<String>() {
			public String run() {
				return System.getProperty("os.name"); //$NON-NLS-1$
			}
		};
		OSId id = OSId.UNIX;
		String osName = AccessController.doPrivileged(doGetOSName);
		if (osName != null) {
			if (osName.toLowerCase().startsWith("mac os x")) { //$NON-NLS-1$
				id = OSId.OSX;
			} else if (osName.contains("Windows")) { //$NON-NLS-1$
				id = OSId.WINDOWS;
			}
		}
		return id;
	}
	
	/**
	 * Set the system property for the library and executable directory.
	 * @param libdir directory path of libraries and executables
	 */
	public static void setLibDir(String libdir) {
		System.setProperty(SYSPROP_LIBDIR, libdir);
	}
	
	/**
	 * Set the system property for the directory where application specific
	 * data is stored.
	 * @param sivaDir directory path of application workspace
	 */
	public static void setSivaDir(String sivaDir) {
		System.setProperty(SYSPROP_SIVADIR, sivaDir);
	}
	
	/**
	 * Set the system property for the directory where application specific
	 * data is stored.
	 * @param sivaDir directory path of application workspace
	 */
	public static String getSivaDir() {
		return System.getProperty(SYSPROP_SIVADIR);
	}
		
}

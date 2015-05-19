package org.iviPro.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CreateSampleLoggerConfig {

	private static final String DEFAULT_LOG_LEVEL = "WARN"; //$NON-NLS-1$

	/**
	 * @param args
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws IOException,
			URISyntaxException {

		URL url = CreateSampleLoggerConfig.class.getResource("."); //$NON-NLS-1$
		File workspace = null;
		workspace = new File(url.toURI());
		for (int i = 0; i < 5; i++) {
			workspace = workspace.getParentFile();
		}

		File mainSrcDir = new File(workspace.getAbsoluteFile() + File.separator
				+ "org.iviPro.ui/src"); //$NON-NLS-1$
		File modelSrcDir = new File(workspace.getAbsoluteFile()
				+ File.separator + "org.iviPro.model/src"); //$NON-NLS-1$
		File exportSrcDir = new File(workspace.getAbsoluteFile()
				+ File.separator + "org.iviPro.export/src"); //$NON-NLS-1$
		
		String loggerContent = getHeader();

		System.out.println("Determining source directories..."); //$NON-NLS-1$
		System.out
				.println("    Workspace path: " + workspace.getAbsolutePath()); //$NON-NLS-1$
		System.out.println("    Model sources : " //$NON-NLS-1$
				+ modelSrcDir.getAbsolutePath());
		System.out.println("    Main sources  : " //$NON-NLS-1$
				+ mainSrcDir.getAbsolutePath());
		System.out.println("    Export sources  : " //$NON-NLS-1$
				+ exportSrcDir.getAbsolutePath());
		System.out.println();

		List<File> allPackageDirs = new ArrayList<File>();
		findPackageDirectories(mainSrcDir, allPackageDirs);
		for (File file : allPackageDirs) {
			loggerContent += scanPackageDir(mainSrcDir, file);
		}

		allPackageDirs.clear();
		findPackageDirectories(modelSrcDir, allPackageDirs);
		for (File file : allPackageDirs) {
			loggerContent += scanPackageDir(modelSrcDir, file);
		}
		
		allPackageDirs.clear();
		findPackageDirectories(exportSrcDir, allPackageDirs);
		for (File file : allPackageDirs) {
			loggerContent += scanPackageDir(exportSrcDir, file);
		}

		loggerContent += getFooter();

		System.out.print("\n\nWriting logger-config..."); //$NON-NLS-1$
		File loggerConfig = new File(workspace.getAbsoluteFile()
				+ File.separator + "org.iviPro.ui/etc/loggerconfig.ini"); //$NON-NLS-1$
		FileWriter fileWriter = new FileWriter(loggerConfig);
		fileWriter.append(loggerContent);
		fileWriter.flush();
		fileWriter.close();
		System.out.println("DONE"); //$NON-NLS-1$
		System.out.println("New logger configuration written to:"); //$NON-NLS-1$
		System.out.println("    " + loggerConfig.getAbsolutePath()); //$NON-NLS-1$

	}

	private static String scanPackageDir(File baseDir, File dir) {

		String packageName = dir.getAbsolutePath().replace(
				baseDir.getAbsolutePath() + File.separator, ""); //$NON-NLS-1$
		packageName = packageName.replace(File.separatorChar, '.');
		System.out.println("Scanning package '" + packageName + "'..."); //$NON-NLS-1$ //$NON-NLS-2$
		String result = "" // //$NON-NLS-1$
				+ "#------------------------------------------------------------\n" //$NON-NLS-1$
				+ "# " //$NON-NLS-1$
				+ packageName
				+ "\n"//  //$NON-NLS-1$
				+ "#------------------------------------------------------------\n"; //$NON-NLS-1$
		File[] javaFiles = getJavaFiles(dir);
		for (int i = 0; i < javaFiles.length; i++) {
			result += "log4j.logger." + packageName + "." //$NON-NLS-1$ //$NON-NLS-2$
					+ javaFiles[i].getName().replace(".java", "") + " = " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ DEFAULT_LOG_LEVEL + "\n"; //$NON-NLS-1$

		}
		result += "\n"; //$NON-NLS-1$
		return result;
	}

	private static File[] getJavaFiles(File directory) {
		File[] javaFiles = directory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".java"); //$NON-NLS-1$
			}

		});
		return javaFiles;
	}

	private static void findPackageDirectories(File baseDir, List<File> result) {
		Queue<File> workQueue = new LinkedList<File>();
		workQueue.add(baseDir);

		while (!workQueue.isEmpty()) {
			File curDir = workQueue.poll();
			File[] files = curDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					workQueue.add(files[i]);
					File[] javaFiles = getJavaFiles(files[i]);
					if (javaFiles.length > 0) {
						result.add(files[i]);
					}

				}
			}
		}
	}

	private static String getHeader() {
		return "" // //$NON-NLS-1$
				+ "########################################################\n" //$NON-NLS-1$
				+ "#     AUTOMATICALLY CREATED LOGGER CONFIGURATION       #\n" //$NON-NLS-1$
				+ "########################################################\n" //$NON-NLS-1$
				+ "#                                                      #\n" //$NON-NLS-1$
				+ "#  |-----------------------------------------------|   #\n" //$NON-NLS-1$
				+ "#  |  Playground - change whatever you like here   |   #\n" //$NON-NLS-1$
				+ "#  |-----------------------------------------------|   #\n" //$NON-NLS-1$
				+ "#                                                      #\n" //$NON-NLS-1$
				+ "########################################################\n" //$NON-NLS-1$
				+ "\n" // //$NON-NLS-1$
				+ "\n" //$NON-NLS-1$
				+ "# Set default logging mode\n" //$NON-NLS-1$
				+ "log4j.rootLogger   = " //$NON-NLS-1$
				+ DEFAULT_LOG_LEVEL + ", Console\n\n\n"; //$NON-NLS-1$

	}

	private static String getFooter() {
		String footer = "\n\n\n" //$NON-NLS-1$
				+ "#  |-----------------------------------------------|\n" //$NON-NLS-1$
				+ "#  | Please do not change anything below this line |\n" //$NON-NLS-1$
				+ "#  |-----------------------------------------------|\n" //$NON-NLS-1$
				+ "\n" //$NON-NLS-1$
				+ "log4j.appender.Console                          = org.apache.log4j.ConsoleAppender\n" //$NON-NLS-1$
				+ "log4j.appender.Console.layout                   = org.apache.log4j.PatternLayout\n" //$NON-NLS-1$
				+ "log4j.appender.Console.target                   = System.out\n" //$NON-NLS-1$
				+ "log4j.appender.Console.layout.ConversionPattern = %-5p\\t%-30.30c\\t%m%n\n" //$NON-NLS-1$
				+ "# log4j.appender.Console.layout.ConversionPattern = %-16t %-20c{1} %m%n\n" //$NON-NLS-1$
				+ "\n" //$NON-NLS-1$
				+ "log4j.appender.Textfile                          = org.apache.log4j.RollingFileAppender\n" //$NON-NLS-1$
				+ "log4j.appender.Textfile.append                   = false\n" //$NON-NLS-1$
				+ "log4j.appender.Textfile.File                     = ./logfile.log\n" //$NON-NLS-1$
				+ "log4j.appender.Textfile.MaxFileSize              = 10MB\n" //$NON-NLS-1$
				+ "log4j.appender.Textfile.layout                   = org.apache.log4j.PatternLayout\n" //$NON-NLS-1$
				+ "log4j.appender.Textfile.layout.ConversionPattern = %d\\t%10.10r\\t%-5p\\t%-30.30c\\t%m%n\n" //$NON-NLS-1$
				+ "\n" //$NON-NLS-1$
				+ "log4j.appender.HTMLfile                          = org.apache.log4j.RollingFileAppender\n" //$NON-NLS-1$
				+ "log4j.appender.HTMLfile.append                   = false\n" //$NON-NLS-1$
				+ "log4j.appender.HTMLfile.File                     = ./logfile.html\n" //$NON-NLS-1$
				+ "log4j.appender.HTMLfile.MaxFileSize              = 10MB\n" //$NON-NLS-1$
				+ "log4j.appender.HTMLfile.layout                   = util.LogHTMLLayout\n" //$NON-NLS-1$
				+ "\n"; //$NON-NLS-1$
		return footer;
	}
}

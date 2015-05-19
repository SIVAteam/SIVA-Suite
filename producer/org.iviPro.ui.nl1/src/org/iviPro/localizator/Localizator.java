package org.iviPro.localizator;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class Localizator {

	private static final String PROJECT_LOCALIZED = "org.iviPro.ui.nl1"; //$NON-NLS-1$
	private static final String PROJECT_ORIGINAL = "org.iviPro.ui"; //$NON-NLS-1$
	private static final String LOCALE = "de"; //$NON-NLS-1$
	private static final String localizedSuffix = "_" + LOCALE + ".properties"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String nonlocalizedSuffix = ".properties"; //$NON-NLS-1$

	public static final Color COLOR_TRANSLATED = new Color(229, 255, 214);
	public static final Color COLOR_UNTRANSLATED = new Color(255, 223, 223);
	public static final Color COLOR_DEPRECATED = new Color(255, 255, 219);
	public static final Color COLOR_EQUAL = new Color(235, 235, 235);

	private File workspaceDir;
	private File projectDirLocalized;
	private File projectDirOriginal;
	private JFrame window;
	private JList fileList;
	private JTextArea log;
	private JTabbedPane tabPane;
	private HashMap<PropFile, Component> tabMap = new HashMap<PropFile, Component>();

	public Localizator() {

		// GUI initialisieren
		initGUI();

		// Workspace-Verzeichnis festellen
		workspaceDir = getWorkspaceDirectory();
		projectDirLocalized = getProjectDir(PROJECT_LOCALIZED, workspaceDir);
		projectDirOriginal = getProjectDir(PROJECT_ORIGINAL, workspaceDir);

		// Search all files
		List<String> filelistLocalized = findPropertyFiles(projectDirLocalized);
		List<String> filelistOriginal = findPropertyFiles(projectDirOriginal);
		List<String> mergedFiles = getMergedFilelist(filelistLocalized,
				filelistOriginal);

		// Create missing files
		List<String> missingFiles = getMissingFiles(filelistLocalized,
				filelistOriginal);
		createMissingFilesInLocalizedProject(missingFiles);
		log("Anzahl nun noch fehlender Dateien: " //$NON-NLS-1$
				+ getMissingFiles(findPropertyFiles(projectDirLocalized),
						filelistOriginal).size());

		// Create PropFile List
		List<PropFile> propFiles = new ArrayList<PropFile>(mergedFiles.size());
		for (String filename : mergedFiles) {
			try {
				log("Reading: " + filename); //$NON-NLS-1$
				PropFile file = new PropFile(filename, projectDirLocalized,
						projectDirOriginal);
				propFiles.add(file);
			} catch (Exception e) {
				logError(e);
			}
		}

		fileList.setListData(propFiles.toArray());

	}

	/**
	 * Wird aufgerufen wenn das Fenster geschlossen werden soll. Falls es noch
	 * zu speichernde Prop-Files gibt, wird der User benachrichtig. Canceled er
	 * die Operation, wird das Fenster auch nicht geschlossen.
	 */
	private void onWindowClose() {
		List<PropFile> changedFiles = getChangedPropFiles();
		if (closeFiles(changedFiles)) {
			System.exit(0);
		}
	}

	/**
	 * Gibt eine Liste der momentan noch offenen, ungespeicherten Property-Files
	 * zureuck.
	 * 
	 * @return
	 */
	private List<PropFile> getChangedPropFiles() {
		List<PropFile> changedFiles = new ArrayList<PropFile>();
		for (PropFile propFile : tabMap.keySet()) {
			if (propFile.hasChanged()) {
				changedFiles.add(propFile);
			}
		}
		return changedFiles;
	}

	/**
	 * Speichert die angegebenen Dateien.
	 * 
	 * @param filesToSave
	 */
	private void saveFiles(List<PropFile> filesToSave) {
		for (PropFile propFile : filesToSave) {
			if (propFile.hasLocalizedFileChanged()) {
				propFile.saveLocalizedFile();
			}
			if (propFile.hasOriginalFileChanged()) {
				propFile.saveOriginalFile();
			}
			propFile.setUnchanged();
		}
		tabPane.revalidate();
		tabPane.repaint();
	}

	/**
	 * Speichert die angegebenen Dateien.
	 * 
	 * @param filesToSave
	 */
	private void revertFiles(List<PropFile> filesToRevert) {
		for (PropFile propFile : filesToRevert) {
			propFile.revert();
		}
	}

	/**
	 * Schliesst die gegebenen Dateien. Der Benutzer wird gefragt, wenn noch
	 * Dateien zum Speichern sind. Bricht der Benutzer dabei ab, wird keine
	 * Datei geschlossen und die Methode gibt false zurueck. True wird zurueck
	 * gegeben, wenn das Schliessen der Dateien erfolgt ist.
	 * 
	 * @param filesToClose
	 * @return
	 */
	private boolean closeFiles(List<PropFile> filesToClose) {
		// Ungespeicherte Dateien feststellen
		ArrayList<PropFile> changedFiles = new ArrayList<PropFile>();
		for (PropFile tabFile : filesToClose) {
			if (tabFile.hasChanged()) {
				changedFiles.add(tabFile);
			}
		}
		if (changedFiles.size() > 0) {
			// Es gibt ungespeicherte Dateien:
			// Dialog-Meldung zum Speichern erstellen
			String dialogMsg = "There are " + changedFiles.size() //$NON-NLS-1$
					+ " unsaved Property-Files\n\n" //$NON-NLS-1$
					+ "Do you want to save them?"; //$NON-NLS-1$
			if (changedFiles.size() == 1) {
				PropFile changedSingleFile = changedFiles.get(0);
				dialogMsg = "Property-File has changed:\n" + changedSingleFile //$NON-NLS-1$
						+ "\n\n" + "Do you want to save these changes?"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			// Dialog anzeigen
			int answer = JOptionPane.showOptionDialog(window, dialogMsg,
					"Save changes?", JOptionPane.YES_NO_CANCEL_OPTION, //$NON-NLS-1$
					JOptionPane.QUESTION_MESSAGE, null, null, null);
			if (answer == JOptionPane.CANCEL_OPTION
					|| answer == JOptionPane.CLOSED_OPTION) {
				// Nicht speichern => Abbruch
				return false;
			} else if (answer == JOptionPane.YES_OPTION) {
				// Speichern => Alle geaenderten Dateien speichern
				saveFiles(changedFiles);
			} else if (answer == JOptionPane.NO_OPTION) {
				// Nicht speichern => Revert alle geaenderten Dateien
				revertFiles(changedFiles);
			}
		}
		for (PropFile tabFile : filesToClose) {
			Component tab = tabMap.get(tabFile);
			tabPane.remove(tab);
			tabMap.remove(tabFile);
		}
		return true;
	}

	/**
	 * Wird aufgerufen wenn die Action zum Speichern aller offenen Dateien
	 * aufrufen wird.
	 */
	private void onSaveAll() {
		ArrayList<PropFile> openFiles = new ArrayList<PropFile>(tabMap.keySet());
		saveFiles(openFiles);
	}

	/**
	 * Wird aufgerufen wenn die Action zum Oeffnen einer Datei aufgerufen wird.
	 */
	private void onOpenFile() {
		int selectedIndex = fileList.getSelectedIndex();
		if (selectedIndex >= 0) {
			final PropFile propFile = (PropFile) fileList.getModel()
					.getElementAt(selectedIndex);
			//System.out.println("Doubleclicked #" + selectedIndex + ": " //$NON-NLS-1$ //$NON-NLS-2$
			//		+ propFile);

			if (tabMap.containsKey(propFile)) {
				tabPane.setSelectedComponent(tabMap.get(propFile));
			} else {
				// final TableModel model = new TableModel(propFile);
				JTable table = new JTable(propFile);
				table.setDefaultRenderer(Object.class,
						new DefaultTableCellRenderer() {

							@Override
							public Component getTableCellRendererComponent(
									JTable table, Object value,
									boolean isSelected, boolean hasFocus,
									int row, int column) {
								Component component = super
										.getTableCellRendererComponent(table,
												value, isSelected, hasFocus,
												row, column);
								if (!isSelected) {
									Property prop = propFile.getProperty(row);
									int state = prop.getState();
									if (state == Property.DEPRECATED) {
										component
												.setBackground(COLOR_DEPRECATED);
									} else if (state == Property.TRANSLATED) {
										component
												.setBackground(COLOR_TRANSLATED);
									} else if (state == Property.EQUAL) {
										component.setBackground(COLOR_EQUAL);
									} else {
										component
												.setBackground(COLOR_UNTRANSLATED);
									}
								}
								return component;
							}

						});
				table.setShowGrid(true);
				JScrollPane tableScrollPane = new JScrollPane(table);
				tabPane.addTab(propFile.toString(), tableScrollPane);
				int tabPos = tabPane.getTabCount() - 1;
				ButtonTabComponent tabTitle = new ButtonTabComponent(tabPane,
						propFile);
				table.getModel().addTableModelListener(tabTitle);
				tabPane.setTabComponentAt(tabPos, tabTitle);
				tabMap.put(propFile, tableScrollPane);
				Component tabComponent = tabPane.getComponentAt(tabPos);
				tabPane.setSelectedComponent(tabComponent);
			}

		}

	}

	/**
	 * Wird aufgerufen wenn die Action zum Schliessen aller offenen Dateien
	 * aufrufen wird.
	 */
	private void onCloseAll() {
		ArrayList<PropFile> openFiles = new ArrayList<PropFile>(tabMap.keySet());
		closeFiles(openFiles);
	}

	/**
	 * Wird aufgerufen wenn die Action zum Entfernen der veralteten Eintraege
	 * aufgerufen wird.
	 */
	private void onRemoveDeprecatedEntries() {
		int tabIndex = tabPane.getSelectedIndex();
		if (tabIndex < 0) {
			JOptionPane.showMessageDialog(window,
					"No file is open. Nothing removed.", "Information", //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		PropFile propFile = getPropFileForTabIndex(tabIndex);
		int numRemoved = propFile.removeDeprecatedProperties();
		String msg;
		if (numRemoved == 1) {
			msg = "1 deprecated file was deleted."; //$NON-NLS-1$
		} else if (numRemoved > 1) {
			msg = numRemoved + " deprecated files were deleted."; //$NON-NLS-1$
		} else {
			msg = "No deprecated entries. Nothing removed."; //$NON-NLS-1$
		}
		JOptionPane.showMessageDialog(window, msg, "Information", //$NON-NLS-1$
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Wird aufgerufen wenn die Action zum Loeschen der veralteten Dateien
	 * aufrufen wird.
	 */
	private void onDeleteDeprecated() {
		List<PropFile> deprecatedFiles = new ArrayList<PropFile>();
		final List<PropFile> undeprecatedFiles = new ArrayList<PropFile>();
		for (int i = 0; i < fileList.getModel().getSize(); i++) {
			Object obj = fileList.getModel().getElementAt(i);
			if (obj instanceof PropFile) {
				PropFile propFile = (PropFile) obj;
				if (propFile.getState() == Property.DEPRECATED) {
					deprecatedFiles.add(propFile);
				} else {
					undeprecatedFiles.add(propFile);
				}
			}
		}
		revertFiles(deprecatedFiles);
		closeFiles(deprecatedFiles);
		for (PropFile deprecatedFile : deprecatedFiles) {
			File fileToDelete = deprecatedFile.getLocalizedFile();
			if (!fileToDelete.delete()) {
				logError("Could not delete file: " + fileToDelete); //$NON-NLS-1$
			} else {
				log("File deleted: " + fileToDelete); //$NON-NLS-1$
			}
		}
		String msg;
		if (deprecatedFiles.size() > 0) {

			if (deprecatedFiles.size() == 1) {
				msg = "1 deprecated file was deleted."; //$NON-NLS-1$
			} else {
				msg = deprecatedFiles.size()
						+ " deprecated files were deleted."; //$NON-NLS-1$
			}
			fileList.setModel(new AbstractListModel() {

				@Override
				public int getSize() {
					return undeprecatedFiles.size();
				}

				@Override
				public Object getElementAt(int index) {
					return undeprecatedFiles.get(index);
				}
			});
		} else {
			msg = "No deprecated files found. Nothing was deleted."; //$NON-NLS-1$
		}
		JOptionPane.showMessageDialog(window, msg, "Information", //$NON-NLS-1$
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Gibt das Property-File fuer ein bestimmtes Tab zurueck.
	 * 
	 * @param index
	 *            Tab-Index des Tabs.
	 * @return
	 */
	private PropFile getPropFileForTabIndex(int index) {
		Component componentToRemove = tabPane.getComponentAt(index);
		Iterator<PropFile> it = tabMap.keySet().iterator();
		PropFile propFile = null;
		while (it.hasNext()) {
			PropFile tabFile = it.next();
			Component tabComponent = tabMap.get(tabFile);
			if (tabComponent == componentToRemove) {
				propFile = tabFile;
			}
		}
		return propFile;
	}

	/**
	 * Erstellt das Menu
	 * 
	 * @param window
	 */
	private void initMenu(JFrame window) {
		// Create menu
		JMenuBar menu = new JMenuBar();
		JMenu menuFile = new JMenu("File"); //$NON-NLS-1$
		// Save all files
		JMenuItem menuSaveAllFiles = new JMenuItem(new AbstractAction(
				"Save all files") { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent arg0) {
				onSaveAll();
			}
		});
		menuFile.add(menuSaveAllFiles);
		// Close all Files
		JMenuItem closeAllFiles = new JMenuItem(new AbstractAction(
				"Close all files") { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent arg0) {
				onCloseAll();
			}
		});
		menuFile.add(closeAllFiles);
		menuFile.addSeparator();
		// Delete deprecated
		JMenuItem menuFileDeleteDeprecated = new JMenuItem(new AbstractAction(
				"Delete Deprecated Files") { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent arg0) {
				onDeleteDeprecated();
			}
		});
		menuFile.add(menuFileDeleteDeprecated);
		// Exit
		JMenuItem menuFileExit = new JMenuItem(new AbstractAction("Exit") { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent arg0) {
				onWindowClose();
			}
		});
		menuFile.addSeparator();
		menuFile.add(menuFileExit);
		// Edit
		JMenu menuEdit = new JMenu("Edit"); //$NON-NLS-1$
		// Edit -> Remove unused entries
		JMenuItem menuEditRemoveUnused = new JMenuItem(new AbstractAction(
				"Remove deprecated entries") { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent arg0) {
				onRemoveDeprecatedEntries();
			}
		});
		menuEdit.add(menuEditRemoveUnused);
		// Zusammenbauen
		menu.add(menuFile);
		menu.add(menuEdit);
		window.setJMenuBar(menu);
	}

	/**
	 * Initialisiert die GUI Komponenten
	 */
	private void initGUI() {
		// Create window
		window = new JFrame("SIVA Localizator"); //$NON-NLS-1$
		window.setSize(new Dimension(1280, 720));
		window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		window.setLayout(new BorderLayout());
		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent arg0) {
				onWindowClose();
			}

		});

		// Create menu
		initMenu(window);

		// Create log panel
		log = new JTextArea();
		JScrollPane logScrollpane = new JScrollPane(log);
		logScrollpane.setPreferredSize(new Dimension(100, 100));

		// Create main panel
		tabPane = new JTabbedPane() {

			@Override
			public void remove(int index) {
				PropFile propFileToClose = getPropFileForTabIndex(index);
				List<PropFile> helperList = new ArrayList<PropFile>();
				helperList.add(propFileToClose);
				closeFiles(helperList);
			}

		};
		tabPane.setFont(log.getFont());

		// Create file list
		fileList = new JList();
		fileList.setFont(log.getFont());
		fileList.setCellRenderer(new ColorRenderer());
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileList.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					onOpenFile();
				}
			}

		});
		JScrollPane fileListScrollpane = new JScrollPane(fileList);

		// Create vertical Split-Panel
		JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		verticalSplit.setDividerLocation(520);
		verticalSplit.setTopComponent(tabPane);
		verticalSplit.setBottomComponent(logScrollpane);

		// Create horizontal Split-Panel
		JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		horizontalSplit.setDividerLocation(560);
		horizontalSplit.setLeftComponent(fileListScrollpane);
		horizontalSplit.setRightComponent(verticalSplit);

		// Open window
		window.add(horizontalSplit, BorderLayout.CENTER);
		window.setVisible(true);
	}

	/**
	 * Erstellt die fehlenden Dateien im alten Projekt. Dabei werden lediglich
	 * leere Dateien angelegt. Nach Ablauf der Methode sollten alle Dateien die
	 * im neuen Projekt existieren auch im alten Projekt existieren.
	 * 
	 * @param missingFiles
	 */
	private void createMissingFilesInLocalizedProject(List<String> missingFiles) {
		log("Erstelle fehlende Dateien im Original-Projekt..."); //$NON-NLS-1$
		String projectPath = projectDirLocalized.getAbsolutePath();
		for (String missingItem : missingFiles) {
			String missingFilename = getLocalizedFilename(projectPath
					+ missingItem);
			File missingFile = new File(missingFilename);
			File missingDirectory = missingFile.getParentFile();
			if (!missingFile.exists()) {
				if (!missingDirectory.exists()) {
					missingDirectory.mkdirs();
				}
				if (missingDirectory.exists()) {
					try {
						log("Erstelle Datei...  " //$NON-NLS-1$
								+ missingFile.getAbsolutePath());
						missingFile.createNewFile();
					} catch (IOException e) {
						logError(e);
					}
				} else {
					// Verzeichnis konnte nicht erstellt werden...
					logError("Konnte Verzeichnis nicht erstellen: " //$NON-NLS-1$
							+ missingDirectory);
				}
			}
		}

	}

	/**
	 * Sucht alle Dateien, die im neuen Projekt zwar existieren, aber im alten
	 * Projekt nicht. Diese Dateien fehlen also im alten Projekt.
	 * 
	 * @param localizedFiles
	 * @param originalFiles
	 * @return
	 */
	private List<String> getMissingFiles(List<String> localizedFiles,
			List<String> originalFiles) {
		ArrayList<String> result = new ArrayList<String>();
		for (String originalFile : originalFiles) {
			if (!localizedFiles.contains(originalFile)) {
				result.add(originalFile);
			}
		}
		return result;
	}

	/**
	 * Sucht alle Dateien, die im neuen Projekt zwar existieren, aber im alten
	 * Projekt nicht. Diese Dateien fehlen also im alten Projekt.
	 * 
	 * @param listLocalized
	 * @param listOriginal
	 * @return
	 */
	private List<String> getMergedFilelist(List<String> listLocalized,
			List<String> listOriginal) {
		Set<String> fileSet = new HashSet<String>(listLocalized.size()
				+ listOriginal.size());
		fileSet.addAll(listLocalized);
		fileSet.addAll(listOriginal);
		String[] fileSetEntries = new String[fileSet.size()];
		List<String> result = Arrays.asList(fileSet.toArray(fileSetEntries));
		Collections.sort(result);
		return result;
	}

	/**
	 * Sucht alle Property-Dateien in einem bestimmten Projekt im Workspace.
	 * 
	 * @param projectName
	 * @return
	 */
	private List<String> findPropertyFiles(File projectDir) {
		// Alle Property-Dateien suchen
		ArrayList<File> files = new ArrayList<File>();
		findPropertyFiles(projectDir, files);
		// Liste mit den relativen Dateinamen erstellen
		ArrayList<String> result = new ArrayList<String>(files.size());
		String projectPath = projectDir.getAbsolutePath();
		for (File file : files) {
			String relativeName = file.getAbsolutePath().replace(projectPath,
					""); //$NON-NLS-1$
			// Blackliste alle Dateien im /bin/ Ordner.
			if (!relativeName.startsWith(File.separator + "bin" //$NON-NLS-1$
					+ File.separator)) {
				result.add(getNonlocalizedFilename(relativeName));
			}
		}
		return result;
	}

	/**
	 * Gibt den nicht-lokalisierten Dateinamen zu einem bestehenden Dateinamen
	 * zurueck. Falls der Dateiname bereits nicht-lokalisiert ist, wird er
	 * unveraendert zurueck gegeben.
	 * 
	 * @param filename
	 * @return
	 */
	private String getNonlocalizedFilename(String filename) {
		if (filename.endsWith(localizedSuffix)) {
			return filename.replace(localizedSuffix, nonlocalizedSuffix);
		} else {
			return filename;
		}
	}

	/**
	 * Gibt den lokalisierten Dateinamen zu einem bestehenden Dateinamen
	 * zurueck. Falls der Dateiname bereits lokalisiert ist, wird er
	 * unveraendert zurueck gegeben.
	 * 
	 * @param filename
	 * @return
	 */
	private String getLocalizedFilename(String filename) {
		if (!filename.endsWith(localizedSuffix)) {
			return filename.replace(nonlocalizedSuffix, localizedSuffix);
		} else {
			return filename;
		}
	}

	/**
	 * Sucht alle Property-Files die in einem Verzeichnis oder einem
	 * Unterverzeichnis von diesem liegen und schreibt das Ergebnis in die
	 * uebergebene Datei-Liste.
	 * 
	 * @param baseDir
	 * @param result
	 */
	private void findPropertyFiles(File baseDir, List<File> result) {
		// Start-Verzeichnis in WorkQueue legen
		Queue<File> workQueue = new LinkedList<File>();
		workQueue.add(baseDir);
		// Solange WorkQueue noch verzeichnisse enthaelt => weitersuchen
		// Wenn auf Unterverzeichnisse gestossen wird => in WorkQueue legen
		// Wenn auf Property-Files gestossen wird => in Result einfuegen
		while (!workQueue.isEmpty()) {
			File curDir = workQueue.poll();
			File[] files = curDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					workQueue.add(files[i]);
					File[] propertyFiles = findPropertyFilesNonrecursive(files[i]);
					for (int k = 0; k < propertyFiles.length; k++) {
						result.add(propertyFiles[k]);
					}
				}
			}
		}
	}

	/**
	 * Sucht alle Property Files in einem Verzeichnis. Unterverzeichnisse werden
	 * <b>nicht</b> durchsucht.
	 * 
	 * @param directory
	 * @return
	 */
	private File[] findPropertyFilesNonrecursive(File directory) {
		File[] propertyFiles = directory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".properties"); //$NON-NLS-1$
			}

		});
		return propertyFiles;
	}

	/**
	 * Gibt das Workspace-Verzeichnis zurueck in dem die Projekte liegen
	 * sollten.
	 * 
	 * @return
	 */
	private File getWorkspaceDirectory() {
		URL url = Localizator.class.getResource("."); //$NON-NLS-1$
		File workspace = null;
		try {
			workspace = new File(url.toURI());
		} catch (URISyntaxException e) {
			logFatal(e);
		}
		for (int i = 0; i < 5; i++) {
			workspace = workspace.getParentFile();
		}
		return workspace;
	}

	/**
	 * Gibt das Projekt-Verzeichnis mit einem bestimmten Namen zurueck
	 * 
	 * @param projectName
	 * @param workspaceDir
	 * @return
	 */
	private File getProjectDir(String projectName, File workspaceDir) {
		File projectDir = new File(workspaceDir.getAbsoluteFile()
				+ File.separator + projectName);
		return projectDir;
	}

	/**
	 * Fuegt Meldung in Log ein
	 * 
	 * @param msg
	 */
	private void log(String msg) {
		//System.out.println(msg);
		log.append(msg + "\n"); //$NON-NLS-1$
		log.repaint();
	}

	/**
	 * Fuegt Fehlermeldung in Log ein
	 * 
	 * @param msg
	 */
	private void logError(String errorMsg) {
		System.err.println(errorMsg);
		String dialogMsg = "An error occured:\n" + errorMsg; //$NON-NLS-1$
		JOptionPane.showMessageDialog(window, dialogMsg, "Error", //$NON-NLS-1$
				JOptionPane.ERROR_MESSAGE);
		// Neuzeilen Einruecken
		errorMsg = errorMsg.replaceAll("\n", "\n       "); //$NON-NLS-1$ //$NON-NLS-2$
		log.append("ERROR: " + errorMsg + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		log.repaint();
	}

	/**
	 * Fuegt Fehlermeldung in Log ein
	 * 
	 * @param e
	 */
	private void logError(Exception e) {
		logError(e.getMessage());
		e.printStackTrace();
	}

	/**
	 * Fuegt fatale Fehlermeldung in Log ein, durch die Programm nicht mehr
	 * weiterarbeiten kann.
	 * 
	 * @param e
	 */
	private void logFatal(Exception e) {
		logError(e.getMessage());
		e.printStackTrace();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Localizator();

	}

	/**
	 * Kapselt die Properties einer Datei, sowohl im alten als auch im neuen
	 * Projekt.
	 * 
	 * @author dellwo
	 * 
	 */
	private class PropFile extends AbstractTableModel {

		private String filename;
		private File localizedFile;
		private File originalFile;

		// private Map<String, Property> propertyMap = new HashMap<String,
		// Property>();
		private List<Property> propertyList = new ArrayList<Property>();
		private List<Property> removedItems = new ArrayList<Property>();

		/**
		 * 
		 * @param filename
		 * @param localizedProjectDir
		 * @param originalProjectDir
		 * @throws Exception
		 */
		public PropFile(String filename, File localizedProjectDir,
				File originalProjectDir) throws Exception {
			Map<String, Property> propertyMap = new HashMap<String, Property>();
			this.filename = filename;

			// Load original Properties
			Properties originalProps = new Properties();
			String nonlocalizedFilename = getNonlocalizedFilename(originalProjectDir
					.getAbsolutePath()
					+ filename);
			originalFile = new File(nonlocalizedFilename);
			if (originalFile.exists()) {
				FileReader reader = new FileReader(originalFile);
				originalProps.load(reader);
				reader.close();
			}
			// Transfer original Properties into property-map
			for (Object keyObj : originalProps.keySet()) {
				String key = (String) keyObj;
				String value = originalProps.getProperty(key, "").replace("\n", //$NON-NLS-1$ //$NON-NLS-2$
						"\\n"); //$NON-NLS-1$
				if (propertyMap.containsKey(key)) {
					propertyMap.get(key).setOriginalValue(value);
				} else {
					Property prop = new Property(key, "", value); //$NON-NLS-1$
					propertyMap.put(key, prop);
				}
			}

			// Load localized Properties
			Properties localizedProps = new Properties();
			String localizedFilename = getLocalizedFilename(localizedProjectDir
					.getAbsolutePath()
					+ filename);
			localizedFile = new File(localizedFilename);
			if (localizedFile.exists()) {
				FileReader reader = new FileReader(localizedFile);
				localizedProps.load(reader);
				reader.close();
			}
			// Transfer localized Properties into property-map
			for (Object keyObj : localizedProps.keySet()) {
				String key = (String) keyObj;
				String value = localizedProps.getProperty(key, "").replace( //$NON-NLS-1$
						"\n", "\\n"); //$NON-NLS-1$ //$NON-NLS-2$
				if (propertyMap.containsKey(key)) {
					propertyMap.get(key).setLocalizedValue(value);
				} else {
					Property prop = new Property(key, value, ""); //$NON-NLS-1$
					propertyMap.put(key, prop);
				}
			}

			propertyList.addAll(propertyMap.values());
			Collections.sort(propertyList);

			// Zu Beginn auf Status "unveraendert" setzen.
			setUnchanged();
		}

		/**
		 * Entfernt die veralteten Eintraege, d.h. solche, die zwar in der
		 * lokalisierten Datei vorhanden sind, aber nicht in der Original-Datei.
		 * 
		 * @return Anzahl der entfernten Eintraege
		 */
		public int removeDeprecatedProperties() {
			List<Property> deprecatedItems = new ArrayList<Property>();
			for (Property prop : propertyList) {
				if (prop.getState() == Property.DEPRECATED) {
					deprecatedItems.add(prop);
				}
			}
			for (Property deprecatedItem : deprecatedItems) {
				propertyList.remove(deprecatedItem);
			}
			removedItems.addAll(deprecatedItems);
			fireTableDataChanged();
			return deprecatedItems.size();
		}

		/**
		 * 
		 * @return
		 */
		public File getLocalizedFile() {
			return localizedFile;
		}

		/**
		 * Saves the localized properties file.
		 */
		public void saveLocalizedFile() {
			Properties props = getJavaProperties(true);
			saveFile(props, localizedFile);
		}

		/**
		 * Saves the original properties file.
		 */
		public void saveOriginalFile() {
			Properties props = getJavaProperties(false);
			saveFile(props, originalFile);
		}

		/**
		 * Gibt eine Java Properties-Objekt der Properties dieser Datei zurueck.
		 * 
		 * @param localizedProperties
		 *            Falls true werden die lokalisierten Properties zurueck
		 *            gegeben, andernfalls die Original-Properties.
		 * @return
		 */
		private Properties getJavaProperties(boolean localizedProperties) {
			Properties javaProps = new Properties();
			for (Property property : propertyList) {
				if (localizedProperties) {
					if (property.getLocalizedValue() != null
							&& property.getLocalizedValue().length() > 0) {
						String key = property.getName();
						String value = property.getLocalizedValue().replace(
								"\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						javaProps.put(key, value);
					}
				} else {
					if (property.getOriginalValue() != null
							&& property.getOriginalValue().length() > 0) {
						String key = property.getName();
						String value = property.getOriginalValue().replace(
								"\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
						javaProps.put(key, value);
					}
				}
			}
			return javaProps;
		}

		/**
		 * Speichert Properties in einer Datei ab.
		 * 
		 * @param javaProps
		 *            Zu speichernde Properties
		 * @param file
		 *            Ziel-Datei
		 */
		private void saveFile(Properties javaProps, File file) {
			log("Saving file: " + file.getAbsolutePath()); //$NON-NLS-1$
			String realFilename = file.getAbsolutePath();
			String tmpFilename = realFilename + ".tmp"; //$NON-NLS-1$
			String backupFilename = realFilename + ".orig"; //$NON-NLS-1$
			File tmpFile = new File(tmpFilename);
			File realFile = new File(realFilename);
			File backupFile = new File(backupFilename);
			try {
				// Temp-Datei erstellen
				if (tmpFile.exists()) {
					if (!tmpFile.delete()) {
						logError("Could not delete tmp file: " //$NON-NLS-1$
								+ tmpFile.getAbsolutePath() + "\n" //$NON-NLS-1$
								+ "File could not be saved!"); //$NON-NLS-1$
					}
				}
				tmpFile.createNewFile();
				// Properties in temporaere Datei schreiben
				FileWriter fileWriter = new FileWriter(tmpFile);
				javaProps.store(fileWriter, "Generated with SIVA Localizator"); //$NON-NLS-1$
				fileWriter.flush();
				fileWriter.close();
				// Alte Backup-Datei loeschen falls sie existiert
				backupFile.delete();
				// Original-Datei in Backup-Datei umbenennen
				if (realFile.renameTo(backupFile)) {
					if (!realFile.exists()) {
						// Temp-Datei in Original-Datei umbennen
						if (!tmpFile.renameTo(realFile)) {
							logError("Could not rename tmp file '" //$NON-NLS-1$
									+ tmpFile.getAbsolutePath() + "', " //$NON-NLS-1$
									+ "because original file '" + realFilename //$NON-NLS-1$
									+ "' still exists." + "\n" //$NON-NLS-1$ //$NON-NLS-2$
									+ "File could not be saved!"); //$NON-NLS-1$
						} else {
							// Falls alles geklappt hat, Backup-Datei loeschen
							backupFile.delete();
						}
					} else {
						logError("Could not rename tmp file, " //$NON-NLS-1$
								+ "because original file '" //$NON-NLS-1$
								+ realFile.getAbsolutePath()
								+ "' still exists." + "\n" //$NON-NLS-1$ //$NON-NLS-2$
								+ "File could not be saved!"); //$NON-NLS-1$
					}
				} else {
					logError("Could not rename original file '" + realFilename //$NON-NLS-1$
							+ "' to '" + backupFile.getName() + "'" + "\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ "File could not be saved!"); //$NON-NLS-1$
				}
			} catch (IOException e) {
				logError(e);
			}

		}

		/**
		 * Gibt an, ob die Datei uebersetzt, veraltet oder noch nicht uebersetzt
		 * ist.
		 * 
		 * @return
		 */
		public int getState() {
			if (localizedFile.exists() && !originalFile.exists()) {
				return Property.DEPRECATED;
			} else {
				for (Property prop : propertyList) {
					int state = prop.getState();
					if (!(state == Property.TRANSLATED || state == Property.EQUAL)) {
						return Property.UNTRANSLATED;
					}
				}
				return Property.TRANSLATED;
			}
		}

		/**
		 * Gibt an ob sich die lokalisierte Property-Datei veraendert hat, dies
		 * ist der Fall wenn sich mindestens eines der Properties der Datei
		 * geaendert hat.
		 * 
		 * @return
		 */
		public boolean hasLocalizedFileChanged() {
			for (Property prop : propertyList) {
				if (prop.hasLocalizedValueChanged()) {
					return true;
				}
			}
			return !removedItems.isEmpty();
		}

		/**
		 * Gibt an ob sich die Property-Datei veraendert hat, dies ist der Fall
		 * wenn sich mindestens eines der Properties der Datei geaendert hat.
		 * 
		 * @return
		 */
		public boolean hasChanged() {
			return hasLocalizedFileChanged() || hasOriginalFileChanged();
		}

		/**
		 * Gibt an ob sich die lokalisierte Property-Datei veraendert hat, dies
		 * ist der Fall wenn sich mindestens eines der Properties der Datei
		 * geaendert hat.
		 * 
		 * @return
		 */
		public boolean hasOriginalFileChanged() {
			for (Property prop : propertyList) {
				if (prop.hasOriginalValueChanged()) {
					return true;
				}
			}
			return false;
		}

		/**
		 * 
		 */
		public void setUnchanged() {
			for (Property prop : propertyList) {
				prop.setUnchanged();
			}
			removedItems.clear();
		}

		/**
		 * Reverts all property entries.
		 */
		public void revert() {
			propertyList.addAll(removedItems);
			Collections.sort(propertyList);
			removedItems.clear();
			for (Property prop : propertyList) {
				prop.revert();
			}
		}

		private static final int COL_KEY = 0;
		private static final int COL_ORIGINAL = 1;
		private static final int COL_LOCALIZED = 2;

		public Property getProperty(int rowNum) {
			return propertyList.get(rowNum);
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return propertyList.size();
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case COL_KEY:
				return "Key"; //$NON-NLS-1$
			case COL_ORIGINAL:
				return "Original text"; //$NON-NLS-1$
			case COL_LOCALIZED:
				return "Localized text"; //$NON-NLS-1$
			}
			return super.getColumnName(column);
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			Property prop = propertyList.get(rowIndex);
			switch (colIndex) {
			case COL_KEY:
				return prop.getName();
			case COL_ORIGINAL:
				return prop.getOriginalValue();
			case COL_LOCALIZED:
				return prop.getLocalizedValue();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == COL_LOCALIZED || columnIndex == COL_ORIGINAL) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Property prop = propertyList.get(rowIndex);
			if (columnIndex == COL_ORIGINAL) {
				prop.setOriginalValue(aValue.toString());
			} else if (columnIndex == COL_LOCALIZED) {
				prop.setLocalizedValue(aValue.toString());
			}
			fireTableRowsUpdated(rowIndex, rowIndex);
		}

		@Override
		public String toString() {
			return filename;
		}

	}

	/**
	 * Kapselt ein Property und bietet Methoden um abzufragen ob das Property
	 * bereits uebersetzt wurde etc.
	 * 
	 * @author dellwo
	 * 
	 */
	private class Property implements Comparable<Property> {

		private static final int UNTRANSLATED = 0;
		private static final int TRANSLATED = 1;
		private static final int DEPRECATED = 2;
		private static final int EQUAL = 3;

		private String name;
		private String localizedValue;
		private String originalValue;
		private String localizedValueBackup;
		private String originalValueBackup;

		public Property(String name, String localizedValue, String originalValue) {
			this.name = name;
			this.localizedValue = localizedValue;
			this.originalValue = originalValue;
			this.localizedValueBackup = localizedValue;
			this.originalValueBackup = originalValue;
		}

		/**
		 * Gibt Status des Properties zurueck
		 * 
		 * @return
		 */
		public int getState() {
			if (originalValue == null || originalValue.length() == 0) {
				return DEPRECATED;
			} else {
				if (localizedValue != null // 
						&& localizedValue.length() > 0) {
					if (localizedValue.equals(originalValue)) {
						return EQUAL;
					} else {
						return TRANSLATED;
					}
				} else {
					return UNTRANSLATED;
				}
			}
		}

		/**
		 * Gibt an, ob das lokalisierte Property veraendert wurde.
		 * 
		 * @return
		 */
		public boolean hasLocalizedValueChanged() {
			return !localizedValue.equals(localizedValueBackup);
		}

		/**
		 * Gibt an, ob das Original-Property veraendert wurde.
		 * 
		 * @return
		 */
		public boolean hasOriginalValueChanged() {
			return !originalValue.equals(originalValueBackup);
		}

		public String getLocalizedValue() {
			return localizedValue;
		}

		public void setLocalizedValue(String localizedValue) {
			this.localizedValue = localizedValue;
		}

		public String getName() {
			return name;
		}

		public String getOriginalValue() {
			return originalValue;
		}

		public void setOriginalValue(String originalValue) {
			this.originalValue = originalValue;
		}

		public void setUnchanged() {
			originalValueBackup = originalValue;
			localizedValueBackup = localizedValue;
		}

		public void revert() {
			originalValue = originalValueBackup;
			localizedValue = localizedValueBackup;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Property) {
				Property other = (Property) obj;
				return name.equals(other.getName());
			} else {
				return false;
			}
		}

		@Override
		public int compareTo(Property other) {
			return name.compareTo(other.getName());
		}

	}

	private class ColorRenderer extends DefaultListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			if (value instanceof PropFile && !isSelected) {
				PropFile propFile = (PropFile) value;
				Color color = getBackground();
				switch (propFile.getState()) {
				case Property.TRANSLATED:
				case Property.EQUAL:
					color = COLOR_TRANSLATED;
					break;
				case Property.UNTRANSLATED:
					color = COLOR_UNTRANSLATED;
					break;
				case Property.DEPRECATED:
					color = COLOR_DEPRECATED;
					break;

				}
				setBackground(color);
				setForeground(Color.BLACK);
				Border outerBorder = BorderFactory.createMatteBorder(1, 0, 0,
						0, Color.WHITE);
				Border innerBorder = BorderFactory.createLineBorder(color, 3);
				setBorder(BorderFactory.createCompoundBorder(outerBorder,
						innerBorder));
			}
			if (value != null) {
				String text = value.toString();
				// Cut away messages.properties to only show the path
				if (value instanceof PropFile) {
					int lastDirSep = text.lastIndexOf(System.getProperty("file.separator"));
					if (lastDirSep != -1) {
						text = text.substring(0, lastDirSep + 1);
					}
				}
				setText(text);
			} else {
				setText(""); //$NON-NLS-1$
			}
			if (isSelected) {
				setBackground(new Color(75, 107, 210));
				setForeground(Color.WHITE);
			}

			return this;
		}

	}

	/**
	 * Component to be used as tabComponent; Contains a JLabel to show the text
	 * and a JButton to close the tab it belongs to
	 */
	public class ButtonTabComponent extends JPanel implements
			TableModelListener {
		private final JTabbedPane pane;
		private JLabel titleLabel;
		private PropFile propFile;

		public ButtonTabComponent(final JTabbedPane pane, PropFile propFile) {
			// unset default FlowLayout' gaps
			super(new FlowLayout(FlowLayout.LEFT, 0, 0));
			if (pane == null) {
				throw new NullPointerException("TabbedPane is null"); //$NON-NLS-1$
			}
			this.propFile = propFile;
			this.pane = pane;
			setOpaque(false);

			// make JLabel read titles from JTabbedPane
			titleLabel = new JLabel() {
				public String getText() {
					int i = pane.indexOfTabComponent(ButtonTabComponent.this);
					if (i != -1) {
						// Wenn die Komponente als dirty markiert ist,
						// dann haenge ein Sternchen an den Namen damit
						// kenntlich ist, dass sie sich geaendert hat.
						if (ButtonTabComponent.this.propFile.hasChanged()) {
							return pane.getTitleAt(i) + "*"; //$NON-NLS-1$
						} else {
							return pane.getTitleAt(i);
						}
					}
					return null;
				}
			};
			titleLabel.setFont(pane.getFont());

			add(titleLabel);
			// add more space between the label and the button
			titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			// tab button
			JButton button = new TabButton();
			add(button);
			// add more space to the top of the component
			setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		}

		private class TabButton extends JButton implements ActionListener {
			public TabButton() {
				int size = 17;
				setPreferredSize(new Dimension(size, size));
				setToolTipText("close this tab"); //$NON-NLS-1$
				// Make the button looks the same for all Laf's
				setUI(new BasicButtonUI());
				// Make it transparent
				setContentAreaFilled(false);
				// No need to be focusable
				setFocusable(false);
				setBorder(BorderFactory.createEtchedBorder());
				setBorderPainted(false);
				// Making nice rollover effect
				// we use the same listener for all buttons
				addMouseListener(buttonMouseListener);
				setRolloverEnabled(true);
				// Close the proper tab by clicking the button
				addActionListener(this);
			}

			public void actionPerformed(ActionEvent e) {
				int i = pane.indexOfTabComponent(ButtonTabComponent.this);
				if (i != -1) {
					pane.remove(i);
				}
			}

			// we don't want to update UI for this button
			public void updateUI() {
			}

			// paint the cross
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				// shift the image for pressed buttons
				if (getModel().isPressed()) {
					g2.translate(1, 1);
				}
				g2.setStroke(new BasicStroke(2));
				g2.setColor(Color.BLACK);
				if (getModel().isRollover()) {
					g2.setColor(Color.MAGENTA);
				}
				int delta = 6;
				g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight()
						- delta - 1);
				g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight()
						- delta - 1);
				g2.dispose();
			}
		}

		private final MouseListener buttonMouseListener = new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				Component component = e.getComponent();
				if (component instanceof AbstractButton) {
					AbstractButton button = (AbstractButton) component;
					button.setBorderPainted(true);
				}
			}

			public void mouseExited(MouseEvent e) {
				Component component = e.getComponent();
				if (component instanceof AbstractButton) {
					AbstractButton button = (AbstractButton) component;
					button.setBorderPainted(false);
				}
			}
		};

		@Override
		public void tableChanged(TableModelEvent e) {
			// Wird aufgerufen wenn der Table editiert wurde.
			// Wenn sich dabei die Property-Datei geaendert hat,
			// dann setzen wir die Tab-Komponente auf dirty, damit sie
			// mit einem * im Namen als nicht gespeichert kenntlich ist.
			invalidate();
			repaint();
		}
	}

}

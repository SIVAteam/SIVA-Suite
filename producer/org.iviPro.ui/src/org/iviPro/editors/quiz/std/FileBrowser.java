package org.iviPro.editors.quiz.std;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;


/**
 * Klasse zum graphischen Auswaehlen von Dateien. (Typen mittels Filter
 * beschraenkt)
 * 
 * @author Sabine Gattermann
 * 
 */
public class FileBrowser {

    private static final String MEDIACONFIG = "Systemfiles"
	    + System.getProperty("file.separator") + "config.properties";
    private String[] fileList;

    /**
     * Konstruktor
     * 
     * @param type
     *            Name des Filters.
     */
    public FileBrowser(String type) {

	Properties prop = new Properties();
	try {
	    prop.load(new FileInputStream(MEDIACONFIG));

	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	String tmp = "";
	if (type.equals("video"))
	    tmp = prop.getProperty("videoExtensions");
	else if (type.equals("image"))
	    tmp = prop.getProperty("imageExtensions");
	else if (type.equals("audio"))
	    tmp = prop.getProperty("audioExtensions");

	tmp = tmp.replace(" ", "");
	String[] propExtentions = tmp.split(",");

	if (!(tmp.replace(",", "")).isEmpty()) {

	    fileList = new String[0];

	    FileDialog dialog = new FileDialog(QuizGenerator.getDefaultShell(),
		    SWT.OPEN);
	    String[] filter = new String[propExtentions.length];
	    String[] extentions = new String[propExtentions.length];

	    for (int i = 0; i < propExtentions.length; i++) {
		filter[i] = " *." + propExtentions[i];
		extentions[i] = "*." + propExtentions[i];
	    }

	    dialog.setFilterNames(filter);
	    dialog.setFilterExtensions(extentions);

	    String path = dialog.open();
	    if (path != null) {

		File file = new File(path);
		if (file.isFile())
		    displayFiles(new String[] { file.toString() });
		else
		    displayFiles(file.list());

	    }
	} else {
	    String mType = "";
	    if (type.equals("video"))
		mType = "Video";
	    else if (type.equals("audio"))
		mType = "Audio";
	    else if (type.equals("image"))
		mType = "Bild";

	    String msg = "Es ist ein Fehler aufgetreten!\n\n"
		    + "Die Liste der unterstützten "
		    + mType
		    + "-Formate konnte nicht geladen werden!\n\n"
		    + "Bitte überprüfen Sie die Konfiguration in der Datei 'config.properties'.";
	    MessageBox errDialog = new MessageBox(QuizGenerator
		    .getDefaultShell(), SWT.OK);
	    errDialog.setMessage(msg);
	    errDialog.open();
	    fileList = new String[0];
	}

    }

    /**
     * Zeigt Dateien an.
     * 
     * @param files
     *            Liste der Dateinamen.
     */
    private void displayFiles(String[] files) {
	for (int i = 0; files != null && i < files.length; i++) {
	    fileList = files;
	}
    }

    /**
     * Uebergibt Liste gewaehlter Dateien.
     * 
     * @return Die Dateiliste.
     */
    public String[] getFileList() {
	return fileList;
    }
}

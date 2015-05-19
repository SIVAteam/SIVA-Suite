/**
 * 
 */
package org.iviPro.model.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;
import org.iviPro.model.DummyFile;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.LocalizedElement;
import org.iviPro.model.Project;

/**
 * @author dellwo
 */
public class RichText extends IMediaObject implements IResource {

	/**
	 * Charset which is used by Richtexts for reading/writing
	 */
	public static final Charset CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

	transient private HTMLDocument document;
	
	/**
	 * Set of pictures contained in the media repository which are
	 * referenced in the richtext.
	 */
	private List<String> pictureIds = new ArrayList<String>();
	
	/**
	 * Indicates whether or not this richtext was created from an existent 
	 * media file.
	 */
	private boolean isFromMedia = false;

	private static Logger logger = Logger.getLogger(RichText.class);

	public RichText(File file, Project project) {
		super(file, project);
		isFromMedia = true;
	}

	/**
	 * Erstellt einen Richtext, der automatisch in einer bestimmten Datei im
	 * Projekt-Ordner gespeichert wird.
	 * 
	 * @param title
	 * @param project
	 */
	public RichText(String title, Project project) {
		this(DummyFile.DUMMY_FILE, project);
		isFromMedia = false;
		setTitle(title);
	}
	
	/**
	 * Creates a copy of the given richtext.
	 * @param toCopy richtext which needs to be copied
	 */
	public RichText(RichText toCopy) {
		this(toCopy.getTitle(), toCopy.getProject());
		setFile(toCopy.getFile());
		this.setContent(toCopy.getContent());
		for (String picId : toCopy.getPictureIds()) {
			pictureIds.add(picId);
		}		
	}
	
	/**
	 * Retrieves the list of IDs of the <code>Pictures</code> referenced
	 * by this richtext.
	 * <p>
	 * <b>Note:</b> This list may contain duplicates since it is meant for
	 * managing the adding/removal of images to the underlying html document.
	 * For any other reference to the pictures used in this richtext the usage
	 * of {@link getPictureSet()} is advised.
	 *  
	 * @return list of IDs of referenced pictures
	 */
	public List<String> getPictureIds() {
		return pictureIds;
	}
	
	/**
	 * Retrieves the set of pictures contained in the media repository which 
	 * are referenced by the richtext and therefore need to be exported. 
	 * 
	 * @return set of referenced pictures
	 */
	public Set<Picture> getPictureSet() {
		HashSet<Picture> pictures = new HashSet<Picture>();
		for (IAbstractBean obj : project.getMediaObjects()) {
			if (obj instanceof Picture) {
				Picture pic = (Picture) obj;
				if (getPictureIds().contains(pic.getId())) {
					pictures.add(pic);
				}
			}
		}
		return pictures;
	}

	/**
	 * Sets the set of pictures contained in the media repository which 
	 * are referenced by this richtext.
	 */
	public void setPictures(List<String> pictureIds) {
		this.pictureIds = pictureIds;
	}
	
	/**
	 * Erstellt eine automatisch generierte Datei im Projekt-Ordner fuer diesen
	 * Richtext.
	 * 
	 * @param title
	 * @param project
	 * @return
	 */
	public void createFileFromTitle() {
		String filename = escapeFilename(getTitle());
		File richtextDir = getProject().getSubdirectoryRichtext();
		File file = new File(richtextDir, filename + ".html"); //$NON-NLS-1$
		setFile(file);
		logger.debug("Creating Richtext-File: " + file.getAbsolutePath());//$NON-NLS-1$
		
	}

	/**
	 * TODO Florian DOK
	 */
	private void initializeDocument() {
		File file = getFile().getValue();
		// create a new document to avoid errors
		HTMLEditorKit htmlKit = new HTMLEditorKit();
		document = (HTMLDocument) htmlKit.createDefaultDocument();
		document.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
		// load existing file if exist
		if (file != null && !(file instanceof DummyFile)) {
			try {
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis, CHARSET);
				htmlKit.read(isr, document, 0);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		// Wenn die Datei ne Dummy-Datei ist legen wir jetzt automatisch
		// eine Datei im Projekt-Verzeichnis fuer den Richtext an
		File file = getFile().getValue();
		if (!(file instanceof DummyFile) && !isFromMedia) {
			// Save richtext to a file
			logger.debug("Setting content:\n" + getContent());//$NON-NLS-1$
			logger.debug("Saving richtext to file: " + file.getAbsolutePath());//$NON-NLS-1$
			Writer output = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), CHARSET));
			try {
				output.write(getContent());
				output.flush();
			} finally {
				output.close();
			}
		}
		// Call default serializer
		out.defaultWriteObject();
	}
	
	/**
	 * Returns whether or not this richtext is based on a loaded media file.
	 * @return true if the richtext is based on a media file
	 */
	public boolean isFromMedia() {
		return isFromMedia;
	}
	
	/**
	 * Gibt den Inhalt der Richtext-Datei zurueck
	 * 
	 * @return
	 */
	public String getContent() {
		if (document == null) {
			initializeDocument();
		}
		StringWriter writer = new StringWriter();
		try {
			HTMLEditorKit htmlKit = new HTMLEditorKit();
			htmlKit.write(writer, document, 0, document.getLength());			
		} catch (Exception e) {
			logger.error(e);
		}
		return writer.toString();
	}

	/**
	 * Replaces the document of this richtext with a new document containing
	 * the given <code>content</code> interpreted as HTML.
	 * 
	 * @param content HTML content of the new document
	 */
	public void setContent(String content) {
		try {
			HTMLEditorKit htmlKit = new HTMLEditorKit();
			// create a new document to avoid errors
			document = (HTMLDocument) htmlKit.createDefaultDocument();
			StringReader reader = new StringReader(content);
			// set content of the document
			htmlKit.read(reader, document, 0);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	/**
	 * Getter for the HTMLDocument
	 * 
	 * @return The document object, if document not exist null will be returned.
	 */
	public HTMLDocument getDocument() {
		if (document == null) {
			initializeDocument();
		}
		return document;
	}

	/**
	 * Setter for the HTMLDocument
	 * 
	 * @param document
	 *            HTMLDocument object.
	 */
	public void setDocument(HTMLDocument document) {
		this.document = document;
	}

	/**
	 * Removes the Element with the given ID from all Richtext-Files
	 * 
	 * @param contentId
	 *            ID of the HTML-Element.
	 */
	public static Map<String, String> removeAll(String contentId,
			Project project) {
		Map<String, String> restoreContainer = new HashMap<String, String>();
		// get list of all media objects
		List<IAbstractBean> mediaObjects = project.getMediaObjects();
		for (IAbstractBean richtext : mediaObjects) {
			if (richtext instanceof RichText) {
				// get only richtext objects
				RichText rt = ((RichText) richtext);
				HTMLDocument doc = rt.getDocument();
				// determine the element by the given id
				Element element = doc.getElement(contentId);
				if (element != null) {
					restoreContainer.put(rt.getId(), rt.getContent());
					// remove it
					while (element != null) {
						try {
							doc.remove(
									element.getStartOffset(),
									element.getEndOffset()
											- element.getStartOffset());
						} catch (BadLocationException e) {
							logger.error(
									"Error while removeing HTML elements.", e);
						}
						element = doc.getElement(contentId);
					}
				}
			}
		}
		return restoreContainer;
	}

	/**
	 * Sets the content for the given Richtext-Annotations.
	 * 
	 * @param container Map containing all ID => Content pairs.
	 * @param project Project
	 */
	public static void restoreAll(Map<String, String> container, Project project) {
		// get list of all media objects
		List<IAbstractBean> mediaObjects = project.getMediaObjects();
		for (IAbstractBean richtext : mediaObjects) {
			if (richtext instanceof RichText) {
				String content = container.get(((RichText) richtext).getId());
				if (content != null) {
					((RichText) richtext).setContent(content);
				}
			}
		}
	}

	@Override
	public List<LocalizedElement> getLocalizedContents() {
		return new ArrayList<LocalizedElement>(getFiles());
	}
}

package org.iviPro.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Provides methods to create and modify HTML files which are used by the SWING
 * HTML-Editor.
 * 
 * @author niederhuber
 * 
 */
public class HtmlDocumentUtil {

	private static final HTMLEditorKit editorKit = new HTMLEditorKit();

	/**
	 * Loads the given HTML file.
	 * 
	 * @param htmlFile
	 *            HTML file.
	 * @return HTML file as HTMLDocument.
	 * @throws IOException
	 * @throws BadLocationException
	 */
	public static HTMLDocument loadDocument(File htmlFile) throws IOException,
			BadLocationException {
		HTMLDocument doc = (HTMLDocument) editorKit.createDefaultDocument();
		FileInputStream fileStream = null;
		try {
			fileStream = new FileInputStream(htmlFile);
			editorKit.read(fileStream, doc, 0);
		} finally {
			if (fileStream != null) {
				fileStream.close();
			}
		}
		return doc;
	}

	/**
	 * Saves the given document content in the given file.
	 * 
	 * @param htmlFile
	 *            File to save in.
	 * @param doc
	 *            Document containing the content.
	 * @throws IOException
	 * @throws BadLocationException
	 */
	public static void saveDocument(File htmlFile, HTMLDocument doc)
			throws IOException, BadLocationException {
		FileOutputStream fileStream = null;
		try {
			fileStream = new FileOutputStream(htmlFile);
			editorKit.write(fileStream, doc, 0, doc.getLength());
		} finally {
			if (fileStream != null) {
				fileStream.close();
			}
		}
	}

	/**
	 * Set a Attribute in the HTMLDocument structure.
	 * 
	 * @param element
	 *            Element which contains the attribute.
	 * @param attr
	 *            Attribute to alter.
	 * @param value
	 *            Value of the attribute.
	 */
	public static void setAttribute(Element element, Attribute attr,
			Object value) {
		HTMLDocument doc = (HTMLDocument) element.getDocument();
		setAttribute(doc, element.getStartOffset(), element.getEndOffset(),
				attr, value);
	}

	/**
	 * Set a Attribute in the HTMLDocument structure.
	 * 
	 * @param doc
	 *            HTMLDocument.
	 * @param startOffset
	 *            Offset from the beginning of the document to the start of the
	 *            attribute in the HTML content.
	 * @param endOffset
	 *            Offset from the beginning of the document to the end of the
	 *            attribute in the HTML content.
	 * @param attr
	 *            Attribute to alter.
	 * @param value
	 *            Value of the attribute.
	 */
	public static void setAttribute(HTMLDocument doc, int startOffset,
			int endOffset, Attribute attr, Object value) {
		MutableAttributeSet attributeSet = new SimpleAttributeSet();
		attributeSet.addAttribute(attr, value);
		doc.setCharacterAttributes(startOffset, endOffset - startOffset,
				attributeSet, false);
	}

}

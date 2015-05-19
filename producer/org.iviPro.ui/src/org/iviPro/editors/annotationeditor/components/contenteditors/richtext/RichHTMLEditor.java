package org.iviPro.editors.annotationeditor.components.contenteditors.richtext;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.iviPro.editors.annotationeditor.components.contenteditors.richtext.dialog.InsertImageDialog;
import org.iviPro.editors.annotationeditor.components.contenteditors.richtext.dialog.InsertLinkDialog;
import org.iviPro.editors.annotationeditor.components.contenteditors.richtext.dialog.ResizeImageDialog;
import org.iviPro.editors.common.SivaComposite;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.theme.Icons;

/**
 * Richtext Editor fuer Annotationen.
 * 
 * @author niederhuber
 */
public class RichHTMLEditor extends SivaComposite {

	private static final Integer[] FONT_SIZE = { 6, 8, 10, 12, 14, 16, 18, 20,
			22, 24, 26, 28, 30, 32, 34, 48, 56, 64 };
	private static final String[] FONTS = { "Arial", "Comic Sans MS",
			"Courier", "Courier New", "Georgia", "Tahoma", "Times New Roman",
			"Verdana" };

	private static Logger logger = Logger.getLogger(RichHTMLEditor.class);
	public static final String ID = RichHTMLEditor.class.getName(); //$NON-NLS-1$

	private Frame frame;
	private JPanel rootPane;
	private HTMLDocument document = null;
	private JEditorPane editor;
	private boolean notify;
	private Element currentElement;
	private RichText richtext;
	
	public RichHTMLEditor(Composite parent, int style, RichText richtext) {
		// embedded ist wichtig damit die bridge zu awt funktioniert, boder
		// dient nur der einheitlichkeit
		super(parent, style | SWT.BORDER | SWT.EMBEDDED);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(new GridLayout(1, false));
		// Create a working copy of the original Richtext
		this.richtext = new RichText(richtext);
		createContent(this.richtext.getContent());
	}

	/**
	 * Erstellt die bruecke zwischen SWT- und SWING-Komponenten
	 * 
	 * @param html
	 *            Content als HTML-Code
	 */
	public void createContent(final String html) {
		// bridge zu awt
		frame = SWT_AWT.new_Frame(this);
		rootPane = new JPanel();
		rootPane.setLayout(new GridBagLayout());
		JRootPane rp = new JRootPane();
		rp.getContentPane().add(rootPane);
		rp.validate();
		frame.add(rp);
		frame.validate();
		
		// Componenten im AWT user interface thread erzeugen (deadlock
		// prevention)
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				initializeLookAndFeel();
				// content erstellen
				initializeToolbar();
				initializeHTMLEditor();
				setHTML(html, false);
			}
		});

		rootPane.repaint();
		rootPane.validate();
	}

	/**
	 * Setzt den Inhalt des editors.
	 * 
	 * @param html
	 *            Content als HTML-Code
	 * @param notify
	 *            Wenn true wird das Dokument als Dirty markiert, sonst nicht.
	 */
	public void setHTML(String html, boolean notify) {
		if (html == null) {
			html = "";
		}
		// document change listener wuerde das dokument sonst als dirty
		// markieren und das ist nicht immer gewollt
		this.notify = notify;
		editor.setText(html);
		this.notify = true;
	}

	/**
	 * Erstellt die Toolbar mit der der Editor bedient werden kann.
	 */
	private void initializeToolbar() {
		// Toolbar initialisieren
		JToolBar toolBar = new JToolBar();
		toolBar.setLayout(new GridBagLayout());

		/*
		 * Toolbar Buttons erstellen
		 */
		// Color picker vorbereiten
		final JColorChooser foregroundColorChooser = new JColorChooser();
		final JDialog foregroundColorChooserDialog = JColorChooser
				.createDialog(frame,
						Messages.RichHTMLEditor_ForegroundColorPicker_Name,
						true, foregroundColorChooser, new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								// Vordergrundfarbe mit vordefinierter funktion
								// aus dem
								// StyledEditorKit aendern
								new StyledEditorKit.ForegroundAction(null,
										foregroundColorChooser.getColor())
										.actionPerformed(e);
								editor.requestFocus();
							}
						}, null);
		foregroundColorChooserDialog.setIconImage(Icons.RICHEDITOR_COLOR
				.getImageIcon().getImage());
		// Button zum aendern der Vordergrundfarbe erstellen
		JButton btnForegroundColor = new JButton();
		btnForegroundColor
				.setToolTipText(Messages.RichHTMLEditor_ForegroundColorPicker_Name);
		btnForegroundColor.setIcon(Icons.RICHEDITOR_COLOR.getImageIcon());
		btnForegroundColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				foregroundColorChooserDialog.setVisible(true);
			}
		});

		// Button um einen Link hinzuzufuegen
		JButton btnLink = new JButton("Link");
		btnLink.setFocusable(false);
		final InsertLinkDialog dialog = new InsertLinkDialog(frame);
		dialog.addOkListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {

				try {
					// man muss die eventsource setzen, sonst funktioniert der
					// button erst beim 2ten mal...
					ae.setSource(editor);

					int selectionStart = editor.getSelectionStart();
					int selectionEnd = editor.getSelectionEnd();
					String text = editor.getSelectedText();
					if (text == null) {
						text = "Link";
					}
					editor.getDocument().remove(selectionStart,
							selectionEnd - selectionStart);
					editor.setSelectionStart(selectionStart);
					editor.setSelectionEnd(selectionStart);
					new HTMLEditorKit.InsertHTMLTextAction(null, "<a href=\""
							+ dialog.getInput() + "\" target=\"_blank\">"
							+ text + "</a>", HTML.Tag.P, HTML.Tag.A)
							.actionPerformed(ae);

				} catch (BadLocationException e) {
					logger.error("Error while inserting Link.", e);
				}
			}
		});

		btnLink.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				dialog.setVisible(true);
			}
		});

		// Button um Text als Fett zu markieren
		JToggleButton btnBold = new JToggleButton(
				new StyledEditorKit.BoldAction());
		btnBold.setText(null);
		btnBold.setFocusable(false);
		btnBold.setToolTipText(Messages.RichHTMLEditor_Bold_Name);
		btnBold.setIcon(Icons.RICHEDITOR_BOLD.getImageIcon());

		// Button um Text als Kursiv zu markieren
		JToggleButton btnItalic = new JToggleButton(
				new StyledEditorKit.ItalicAction());
		btnItalic.setText(null);
		btnItalic.setFocusable(false);
		btnItalic.setToolTipText(Messages.RichHTMLEditor_Italic_Name);
		btnItalic.setIcon(Icons.RICHEDITOR_ITALIC.getImageIcon());

		// Button um Text als Unterstrichen zu markieren
		JToggleButton btnUnderline = new JToggleButton(
				new StyledEditorKit.UnderlineAction());
		btnUnderline.setText(null);
		btnUnderline.setFocusable(false);
		btnUnderline.setToolTipText(Messages.RichHTMLEditor_Underline_Name);
		btnUnderline.setIcon(Icons.RICHEDITOR_UNDERLINE.getImageIcon());

		// Button zum Ausscheiden von Text
		JButton btnCut = new JButton(new DefaultEditorKit.CutAction());
		btnCut.setText(null);
		btnCut.setFocusable(false);
		btnCut.setToolTipText(Messages.RichHTMLEditor_Cut_Name);
		btnCut.setIcon(Icons.RICHEDITOR_CUT.getImageIcon());

		// Button zum Kopieren von Text
		JButton btnCopy = new JButton(new DefaultEditorKit.CopyAction());
		btnCopy.setText(null);
		btnCopy.setFocusable(false);
		btnCopy.setToolTipText(Messages.RichHTMLEditor_Copy_Name);
		btnCopy.setIcon(Icons.RICHEDITOR_COPY.getImageIcon());

		// Button zum Einfügen von Text
		JButton btnPaste = new JButton(new DefaultEditorKit.PasteAction());
		btnPaste.setText(null);
		btnPaste.setFocusable(false);
		btnPaste.setToolTipText(Messages.RichHTMLEditor_Paste_Name);
		btnPaste.setIcon(Icons.RICHEDITOR_PASTE.getImageIcon());

		// Button um den Text Linksbuendig auszurichten
		JButton btnAlignLeft = new JButton(new StyledEditorKit.AlignmentAction(
				null, StyleConstants.ALIGN_LEFT));
		btnAlignLeft.setFocusable(false);
		btnAlignLeft.setToolTipText(Messages.RichHTMLEditor_AlignLeft_Name);
		btnAlignLeft.setIcon(Icons.RICHEDITOR_ALIGN_LEFT.getImageIcon());

		// Button um den Text Rechtsbuendig auszurichten
		JButton btnAlignRight = new JButton(
				new StyledEditorKit.AlignmentAction(null,
						StyleConstants.ALIGN_RIGHT));
		btnAlignRight.setFocusable(false);
		btnAlignRight.setToolTipText(Messages.RichHTMLEditor_AlignRight_Name);
		btnAlignRight.setIcon(Icons.RICHEDITOR_ALIGN_RIGHT.getImageIcon());

		// Button um den Text Zentriert auszurichten
		JButton btnAlignCenter = new JButton(
				new StyledEditorKit.AlignmentAction(null,
						StyleConstants.ALIGN_CENTER));
		btnAlignCenter.setFocusable(false);
		btnAlignCenter.setToolTipText(Messages.RichHTMLEditor_AlignCenter_Name);
		btnAlignCenter.setIcon(Icons.RICHEDITOR_ALIGN_CENTER.getImageIcon());

		// Button um den Text auf Blocksatz auszurichten
		JButton btnAlignJustify = new JButton(
				new StyledEditorKit.AlignmentAction(null,
						StyleConstants.ALIGN_JUSTIFIED));
		btnAlignJustify.setFocusable(false);
		btnAlignJustify
				.setToolTipText(Messages.RichHTMLEditor_AlignJustify_Name);
		btnAlignJustify.setIcon(Icons.RICHEDITOR_ALIGN_JUSTIFY.getImageIcon());

		// Feld um die schriftgröße zu wählen
		final JComboBox cbFontSize = new JComboBox(FONT_SIZE);
		cbFontSize.setToolTipText(Messages.RichHTMLEditor_FontSize_Name);
		cbFontSize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new StyledEditorKit.FontSizeAction(null, (Integer) cbFontSize
						.getSelectedItem()).actionPerformed(e);
				editor.requestFocus();
			}
		});

		// Feld um die Schriftart zu wählen
		final JComboBox fontCombo = new JComboBox(FONTS);
		fontCombo.setToolTipText(Messages.RichHTMLEditor_Font_Name);
		fontCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new StyledEditorKit.FontFamilyAction(null, (String) fontCombo
						.getSelectedItem()).actionPerformed(e);
				editor.requestFocus();
			}
		});

		// Button zum einfuegen von Bildern aus der Medien Bibliothek
		final JButton btnInsertImage = new JButton();
		btnInsertImage.setIcon(Icons.OBJECT_MEDIA_PICTURE.getImageIcon());
		btnInsertImage.setToolTipText(Messages.RichHTMLEditor_InsertImage_Name);
		// Auswahldialog
		final InsertImageDialog imageDialog = new InsertImageDialog(frame);
		imageDialog.addInsertListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// editor als ort wo das bild eingefuegt werden soll angeben
				e.setSource(editor);
				// eigenschaften
				Picture selectedPic = imageDialog.getSelectedImage();
												
				String id = selectedPic.getId();
				String title = selectedPic.getTitle();
				URI src = selectedPic.getFile().getValue().toURI();
				int width = imageDialog.getOptionSize().width;
				int height = imageDialog.getOptionSize().height;
				/*
				 * file:/// gibt HTML an, dass das Bild sich auf einem lokalen
				 * Medium befident. Falls man UNC oder HTTP Pfade angeben
				 * moechte muss dies ergaenzt werden.
				 */

				new HTMLEditorKit.InsertHTMLTextAction(null, "<img id=\"" + id
						+ "\" title=\"" + title + "\" " + "src=\"" + src
						+ "\" width=\"" + width + "\" height=\"" + height
						+ "\">", HTML.Tag.BODY, HTML.Tag.IMG).actionPerformed(e);
				richtext.getPictureIds().add(id);
			}
		});
		btnInsertImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				editor.requestFocus();
				imageDialog.setVisible(true);
			}
		});

		/*
		 * Komponenten an die ToolBar heften
		 */

		toolBar.add(btnCut);
		toolBar.add(btnCopy);
		toolBar.add(btnPaste);
		toolBar.addSeparator();
		toolBar.add(cbFontSize);
		toolBar.add(fontCombo);
		toolBar.addSeparator();
		toolBar.add(btnBold);
		toolBar.add(btnItalic);
		toolBar.add(btnUnderline);
		toolBar.addSeparator();
		toolBar.add(btnAlignLeft);
		toolBar.add(btnAlignRight);
		toolBar.add(btnAlignCenter);
		toolBar.add(btnAlignJustify);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(btnForegroundColor);
		toolBar.add(btnInsertImage);
		toolBar.add(btnLink);

		toolBar.invalidate();
		toolBar.validate();
		toolBar.repaint();

		// verhalten im container festlegen
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.gridy = 0;
		// extra wrapping container um flicker zu vermeide
		Panel wrapperPane = new Panel();
		wrapperPane.add(toolBar);
		wrapperPane.validate();
		rootPane.add(wrapperPane, c);
		rootPane.validate();
	}

	/**
	 * Erstellt den Eingabeberech des Editors.
	 */
	private void initializeHTMLEditor() {

		editor = new JEditorPane();

		// popup menu when right clicking an image in richtext editor
		final JPopupMenu pMenu = new JPopupMenu();

		// menu item delete
		JMenuItem mItem = new JMenuItem(Messages.RichHTMLEditor_Image_Delete);
		mItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				removeElement(currentElement);
				String id = (String)currentElement.getAttributes().getAttribute(HTML.Attribute.ID);
				richtext.getPictureIds().remove(id);
			}
		});
		pMenu.add(mItem);

		// menu item resize
		mItem = new JMenuItem(Messages.RichHTMLEditor_Image_Resize);
		mItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showImgResizeDialogue(currentElement);
			}
		});
		pMenu.add(mItem);

		// mouse listener on richtext editor
		editor.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				HTMLDocument doc = (HTMLDocument) editor.getDocument();
				currentElement = doc.getCharacterElement(editor
						.viewToModel(arg0.getPoint()));

				// if right click on image
				if (currentElement.getName() == "img"
						&& SwingUtilities.isRightMouseButton(arg0)) {
					// open menu to delete and resize (see above)
					pMenu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
				}
			}

		});
	
		JScrollPane scrollPane = new JScrollPane(editor);
		// content type auf text/html für richtige ausgabe
		editor.setContentType("text/html");
		document = (HTMLDocument) editor.getDocument();
		document.setDocumentFilter(new DocumentFilter() {
			@Override
			public void remove(FilterBypass fb, int offset, int length)
					throws BadLocationException {
				Element elem = document.getCharacterElement(offset);
				if (elem.getName().equals(HTML.Tag.IMG.toString())) {
					String id = (String)elem.getAttributes().getAttribute(HTML.Attribute.ID);
					richtext.getPictureIds().remove(id);				
				}
				super.remove(fb, offset, length);
			}
		});
		// bei aenderungen am content swt componenten benachrichtigen
		document.addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				notifyContentChanged();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				notifyContentChanged();
			}
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				notifyContentChanged();
			}
		});
		this.richtext.setDocument(document);

		// verhalten im container festlegen
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridy = 1;
		// extra wrapping panel um focus fehler und flicker zu vermeiden
		Panel wrapperPane = new Panel();
		wrapperPane.setLayout(new java.awt.GridLayout(1, 1));
		wrapperPane.add(scrollPane);
		rootPane.add(wrapperPane, c);
	}

	private void showImgResizeDialogue(final Element currentElement) {
		final ResizeImageDialog rid = new ResizeImageDialog(frame,
				currentElement);

		rid.addResizeListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				e.setSource(editor);

				String id = currentElement
						.getAttributes()
						.getAttribute(
								javax.swing.text.html.HTML.Attribute.ID)
						.toString();
				String title = currentElement
						.getAttributes()
						.getAttribute(
								javax.swing.text.html.HTML.Attribute.TITLE)
						.toString();
				String src = currentElement.getAttributes()
						.getAttribute(javax.swing.text.html.HTML.Attribute.SRC)
						.toString();
				int width = rid.getWidth();
				int height = rid.getHeight();

				removeElement(currentElement);

				new HTMLEditorKit.InsertHTMLTextAction(null, "<img id=\"" + id
						+ "\" title=\"" + title + "\" " + "src=\"" + src
						+ "\" width=\"" + width + "\" height=\"" + height
						+ "\">", HTML.Tag.BODY, HTML.Tag.IMG)
						.actionPerformed(e);
			}
		});
		// document.setCharacterAttributes(currentElement.getStartOffset(),
		// currentElement.getEndOffset()-currentElement.getStartOffset(), true);

	}

	/**
	 * Removes the given element from the HTML-Document.
	 * 
	 * @param element
	 *            the element to remove.
	 * @author Tristan Schneider
	 */
	private void removeElement(Element element) {
		try {
			document.remove(
					currentElement.getStartOffset(),
					currentElement.getEndOffset()
							- currentElement.getStartOffset());
		} catch (BadLocationException e1) {
			logger.error("Cannot remove element from Richtext. (BadLocationException "
					+ currentElement.getStartOffset()
					+ " "
					+ currentElement.getEndOffset() + ")");
		}
	}

	/**
	 * Der Layout-Style wird an das System angepasst.
	 */
	private void initializeLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			String errorMsg = Messages.RichHTMLEditor_Error_MsgBoxText_LookAndFeel;
			logger.error(errorMsg);
			MessageDialog.openError(getParent().getShell(),
					Messages.RichHTMLEditor_Error_MsgBoxTitle, errorMsg);
			return;
		}
	}

	/**
	 * Teilt dem aktuellen Projekt eine aenderung im Content mit.
	 */
	private void notifyContentChanged() {
		// auf den thread der die event queue verwaltet kann die awt/swing
		// komponente nicht zugreifen. die bridge SWT->AWT verhindert dies.
		// daher dieser workaround bei dem der user-interface-thread angefordert
		// wird
		if (notify) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					SivaEvent edEvent = new SivaEvent(RichHTMLEditor.this,
							SivaEventType.EDITOR_CHANGED, richtext);
					notifySivaEventConsumers(edEvent);
				}
			});
		}
	}
}

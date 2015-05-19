package org.iviPro.editors.annotationeditor.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.iviPro.application.Application;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationContentType;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationFactory;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationType;
import org.iviPro.editors.annotationeditor.components.contenteditors.DragAndDropEditor;
import org.iviPro.editors.annotationeditor.components.contenteditors.PictureEditor;
import org.iviPro.editors.annotationeditor.components.contenteditors.SubtitleEditor;
import org.iviPro.editors.annotationeditor.components.contenteditors.TextEditor;
import org.iviPro.editors.annotationeditor.components.contenteditors.VideoEditor;
import org.iviPro.editors.annotationeditor.components.contenteditors.richtext.RichHTMLEditor;
import org.iviPro.editors.common.ScreenAreaSelector;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.ProjectSettings;
import org.iviPro.model.annotation.OverlayPathItem;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPdf;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.PdfDocument;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Subtitle;
import org.iviPro.model.resources.Video;

/**
 * Kapselt alles was zum Editieren von globalen und normalen Annotationen
 * benötigt wird die jeweiligen Subclasses kümmern sich um das Speichern und
 * Prüfen der Inhalte bzw. das erstellen des Layouts
 * 
 * @author juhoffma
 */
public abstract class AbstractAnnotationDefineWidget extends Composite
		implements PropertyChangeListener, SivaEventConsumerI {

	// die aktuelle Annotation
	// kann entweder eine neue Annotation oder eine
	// bereits vorhandene sein
	protected INodeAnnotation annotation = null;

	// der Auswahleditor für eine Annotation
	protected ScreenAreaSelector areaSelector = null;

	// die aktuellen OverlayPath Items
	protected List<OverlayPathItem> tmpOpItems;

	// das zur Annotation gehörende Tab Item
	protected CTabItem tabItem = null;

	// die Textbox zur Anzeige des Szenennamens
	protected Text tmpTitle = null;
	
	// Text field for annotation description
	protected Text tmpDescription = null;

	// die Editoren für den eigentlichen Inhalt (rechte Seite: Bild, Text,
	// Video, Subtitle...)	
	// Partially refactored by using a common DragAndDropEditor. However, structure
	// could be simplified by using a single editor variable relying on an abstract
	// class and factory. Got no time for that now.
	protected RichHTMLEditor tmpRichtext = null;
	protected TextEditor tmpText = null;
	protected SubtitleEditor tmpSubtitle = null;
	protected PictureEditor tmpPicture = null;
	protected DragAndDropEditor tmpAudio = null;
	protected DragAndDropEditor tmpVideo = null;
	protected DragAndDropEditor tmpPdf = null;

	// die aktuelle ScreenPosition
	protected ScreenArea tmpScreenArea;

	// der aktuelle Content des aktuellen Editors, bei Änderungen im jeweiligen
	// Editor
	// wird dieses Objekt automatisch aktualisiert
	protected IAbstractBean editorContent;

	// Button zum Einstellen ob das Video bei Audio Annotationen gemuted wird
	protected Button muteVideoButton;

	// Eingabefeld für Keywords
	protected Text tmpKeywords = null;

	// Composite für den Inhalts-Editor
	protected Composite contentEditorComposite = null;

	// Composite für den allgemeinen Editor Teil
	protected Composite editorComposite = null;

	// Annotationstyp, z.B. um festzustellen, welchen Content Editor man
	// benötigt.
	protected AnnotationType annotationType;

	// Buttons für die Auswahl zwischen Bildanno und Bildergalerieanno
	protected Button picButton;
	protected Button galButton;
	protected Text pictureAnnoColumnField;

	public AbstractAnnotationDefineWidget(Composite parent, int style,
			INodeAnnotation annotation, AnnotationType annotationType,
			CTabItem it) {
		super(parent, style);
		this.tmpOpItems = new LinkedList<OverlayPathItem>();
		this.tabItem = it;
		this.annotationType = annotationType;
		this.annotation = annotation;

		Application.getCurrentProject().getSettings()
				.addPropertyChangeListener(this);
	}

	/**
	 * erzeugt das Layout des Define Widgets
	 */
	protected abstract void createContent();

	public CTabItem getItem() {
		return tabItem;
	}

	/**
	 * initialisiert die Content Editoren = Editoren für den Inhalt der
	 * Annotation
	 */
	protected void initContentEditors() {
		// Die Annotation die den eigentlichen Content (Picture, Audio ..)
		// speichert
		// bei Mark Annotationen ist das die getriggerte Annotation
		INodeAnnotation contentAnnotation = AnnotationFactory
				.getContentAnnotation(annotation);
		AnnotationContentType contentType = annotationType.getContentType();

		switch (contentType) {

		// Audio Annotation wird angelegt
		case AUDIO:
			NodeAnnotationAudio audAnno = (NodeAnnotationAudio) contentAnnotation;
			if (audAnno.getContentType() == NodeAnnotationAudio.CONTENT_AUDIO) {
				editorContent = ((NodeAnnotationAudio) contentAnnotation)
						.getAudio();
			} else if (audAnno.getContentType() == NodeAnnotationAudio.CONTENT_AUDIOPART) {
				editorContent = ((NodeAnnotationAudio) contentAnnotation)
						.getAudioPart();
			}

			if (tmpAudio == null) {
				tmpAudio = new DragAndDropEditor(contentEditorComposite, SWT.NONE,
						editorContent, new Class[] {Audio.class, AudioPart.class});
				
				AbstractAnnotationDefineWidget.this.layout(true);
				
				tmpAudio.addSivaEventConsumer(this);
				// Remove listener when AnnotationDefineWidget is disposed
				addListener(SWT.Dispose, new Listener() {
					@Override
					public void handleEvent(Event event) {
						if (tmpAudio != null) {
							tmpAudio.removeSivaEventConsumer(
									AbstractAnnotationDefineWidget.this);
						}
					}
				});
			} else {
				tmpAudio.setContent(editorContent);
			}
			break;

		// Video Annotation wird angelegt
		case VIDEO:
			if (((NodeAnnotationVideo) contentAnnotation).getContentType() == NodeAnnotationVideo.CONTENT_SCENE) {
				editorContent = ((NodeAnnotationVideo) contentAnnotation)
						.getScene();
			} else {
				editorContent = ((NodeAnnotationVideo) contentAnnotation)
						.getVideo();
			}

			if (tmpVideo == null) {
				tmpVideo = new VideoEditor(contentEditorComposite, SWT.NONE,
						editorContent);
				AbstractAnnotationDefineWidget.this.layout(true);
				
				tmpVideo.addSivaEventConsumer(this);			
				// Remove listener when AnnotationDefineWidget is disposed
				addListener(SWT.Dispose, new Listener() {
					@Override
					public void handleEvent(Event event) {
						if (tmpVideo != null) {
							tmpVideo.removeSivaEventConsumer(
									AbstractAnnotationDefineWidget.this);
						}
					}
				});
			} else {
				tmpVideo.setContent(editorContent);
			}
			break;

		// Subtitle Annotation wird angelegt
		case SUBTITLE:
			editorContent = ((NodeAnnotationSubtitle) contentAnnotation)
					.getSubtitle();
			
			if (tmpSubtitle == null) {
				tmpSubtitle = new SubtitleEditor(contentEditorComposite,
						SWT.NONE, (Subtitle) editorContent);
				AbstractAnnotationDefineWidget.this.layout(true);
				
				tmpSubtitle.addSivaEventConsumer(this);
				// Remove listener when AnnotationDefineWidget is disposed
				addListener(SWT.Dispose, new Listener() {
					@Override
					public void handleEvent(Event event) {
						if (tmpSubtitle != null) {
							tmpSubtitle.removeSivaEventConsumer(
									AbstractAnnotationDefineWidget.this);
						}
					}
				});
			} else {
				tmpSubtitle.setSubtitle((Subtitle) editorContent);
			}
			break;

		// falls Richtext-Annotation
		case RICHTEXT:
			editorContent = ((NodeAnnotationRichtext) contentAnnotation)
						.getRichtext();
			
			if (tmpRichtext == null) {
				tmpRichtext = new RichHTMLEditor(contentEditorComposite,
						SWT.NONE, (RichText) editorContent);
				AbstractAnnotationDefineWidget.this.layout(true);
				
				tmpRichtext.addSivaEventConsumer(this);
				// Remove listener when AnnotationDefineWidget is disposed
				addListener(SWT.Dispose, new Listener() {
					@Override
					public void handleEvent(Event event) {
						if (tmpRichtext != null) {
							tmpRichtext
									.removeSivaEventConsumer(
											AbstractAnnotationDefineWidget.this);
						}
					}
				});
			} else {
				tmpRichtext.setHTML(((RichText)editorContent).getContent(), false);
			}
			break;
		// Picture Annotation
		case PICTURE:
			int pictureAnnotationType = ((NodeAnnotationPicture) contentAnnotation)
					.getContentType();
			// Bild / Bildergallerie
			switch (pictureAnnotationType) {
			case NodeAnnotationPicture.CONTENT_PICTURE:
				editorContent = ((NodeAnnotationPicture) contentAnnotation)
				.getPicture();
				break;
			case NodeAnnotationPicture.CONTENT_PICTUREGALLERY:
				editorContent = ((NodeAnnotationPicture) contentAnnotation)
				.getPictureGallery();
				break;
			}

			if (tmpPicture == null) {
				switch (pictureAnnotationType) {
				case NodeAnnotationPicture.CONTENT_PICTURE:
					tmpPicture = new PictureEditor(contentEditorComposite,
							SWT.NONE, (Picture) editorContent);
					picButton.setSelection(true);
					galButton.setSelection(false);
					break;
				case NodeAnnotationPicture.CONTENT_PICTUREGALLERY:
					tmpPicture = new PictureEditor(contentEditorComposite,
							SWT.NONE, (PictureGallery) editorContent,
							((PictureGallery) editorContent).getNumberColumns());
					picButton.setSelection(false);
					galButton.setSelection(true);
					if (annotation != null) {
						pictureAnnoColumnField
								.setText("" //$NON-NLS-1$
										+ ((NodeAnnotationPicture) contentAnnotation)
												.getPictureGallery()
												.getNumberColumns());
					} else {
						pictureAnnoColumnField.setText("" //$NON-NLS-1$
								+ PictureGallery.PICGAL_COLS_STD);
					}
					break;					
				}
				AbstractAnnotationDefineWidget.this.layout(true);
				
				tmpPicture.addSivaEventConsumer(this);				
				// Remove listener when AnnotationDefineWidget is disposed
				addListener(SWT.Dispose, new Listener() {
					@Override
					public void handleEvent(Event event) {
						if (tmpPicture != null) {
							tmpPicture
									.removeSivaEventConsumer(AbstractAnnotationDefineWidget.this);
						}
					}
				});
			} else {
				switch (pictureAnnotationType) {
				case NodeAnnotationPicture.CONTENT_PICTURE:
					if (editorContent instanceof Picture) {
						tmpPicture.setPicture((Picture) editorContent);
						picButton.setSelection(true);
						galButton.setSelection(false);
					}
					break;
				case NodeAnnotationPicture.CONTENT_PICTUREGALLERY:
					if (editorContent instanceof PictureGallery) {
						tmpPicture.setPictureGallery(
								(PictureGallery) editorContent,
								((PictureGallery) editorContent)
										.getNumberColumns());
						if (annotation != null) {
							pictureAnnoColumnField
									.setText("" //$NON-NLS-1$
											+ ((NodeAnnotationPicture) contentAnnotation)
													.getPictureGallery()
													.getNumberColumns());
						} else {
							pictureAnnoColumnField.setText("" //$NON-NLS-1$
									+ PictureGallery.PICGAL_COLS_STD);
						}
						picButton.setSelection(false);
						galButton.setSelection(true);
					}
					break;
				}
			}
			break;
		
		case PDF:
			editorContent = ((NodeAnnotationPdf) contentAnnotation).getPdf();
			if (tmpPdf == null) {
				tmpPdf = new DragAndDropEditor(contentEditorComposite, SWT.NONE,
						editorContent, new Class[] {PdfDocument.class});
				AbstractAnnotationDefineWidget.this.layout(true);
				
				tmpPdf.addSivaEventConsumer(this);
				// Remove listener when AnnotationDefineWidget is disposed
				addListener(SWT.Dispose, new Listener() {
					@Override
					public void handleEvent(Event event) {
						if (tmpPicture != null) {
							tmpPicture.removeSivaEventConsumer(
											AbstractAnnotationDefineWidget.this);
						}
					}
				});
			} else {
				tmpPdf.setContent(editorContent);
			}
			break;
		}
	}

	/*
	 * aktualisiert den Tabnamen wird bei jeder Änderungsaktion im Editor
	 * aufgerufen
	 */
	public void updateDirty() {
		if (isDisposed()) {
			return;
		}
		if (isDirty()) {
			if (tabItem.isDisposed()) {
				return;
			}
			// tab namen aendern falls ein name angegeben wurde, andernfalls
			// text zwischen index 0 und dem index des ersten '*' des tab namens
			// erhalten
			if (tmpTitle.getText().length() != 0) {
				tabItem.setText(tmpTitle.getText() + "*"); //$NON-NLS-1$
			} else {
				String newItemName = tabItem.getText();
				if (tabItem.getText().indexOf("*", 1) > 0) { //$NON-NLS-1$
					newItemName = tabItem.getText().substring(0,
							tabItem.getText().indexOf("*", 1)); //$NON-NLS-1$
				}
				tabItem.setText(newItemName + "*"); //$NON-NLS-1$
			}
		} else {
			if (!tabItem.isDisposed()) {
				tabItem.setText(tmpTitle.getText());
			}
		}
	}

	/**
	 * Tries to save the annotation and returns true if successful. 
	 * @return true if the save operation was successful - false otherwise
	 */
	public abstract boolean executeSaveOperation();

	/**
	 * prüft ob der Inhalt gesetzt wurde und gibt eine Fehlermeldung aus, wenn
	 * das noch nicht geschehen ist.
	 * 
	 * @return
	 */
	protected boolean checkContentSet() {
		// prüfe ob der Content gesetzt ist, falls nicht ist die Annotation
		// Dirty
		// und kann nicht gespeichert werden
		if (editorContent == null) {
			MessageBox messageBox = new MessageBox(Display.getCurrent()
					.getActiveShell(), SWT.ICON_ERROR);
			messageBox
					.setMessage(Messages.AbstractAnnotationDefineWidget_NoContent_Message);
			messageBox.open();
			return false;
		}
		return true;
	}

	public INodeAnnotation getAnnotation() {
		return annotation;
	}

	/**
	 * prüft ob sich der Content im Content Editor bzgl. der gespeicherten
	 * Annotation geändert hat
	 * 
	 * @return true falls Änderung vorhanden
	 */
	protected boolean hasContentChanged() {
		INodeAnnotation contentAnnotation = AnnotationFactory
				.getContentAnnotation(annotation);
		AnnotationContentType contentType = annotationType.getContentType();

		switch (contentType) {

		// prüfe ob sich ein Text geändert hat
		case SUBTITLE:
			Subtitle subtitle = null; //$NON-NLS-1$
			if (tmpSubtitle != null) {
				if (editorContent instanceof Subtitle) {
					subtitle = (Subtitle) editorContent;
					Subtitle savedSub = ((NodeAnnotationSubtitle) contentAnnotation)
							.getSubtitle();
					if (!(subtitle.getTitle().equals(savedSub.getTitle()) && subtitle
							.getDescription().equals(savedSub.getDescription()))) {
						return true;
					}
				}
			}
			break;

		// Prüfe ob sich das Bild geändert hat
		case PICTURE:

			NodeAnnotationPicture picAnnotation = (NodeAnnotationPicture) contentAnnotation;
			// der aktuell eingestellte ContentType
			int currentAnnoContentType = NodeAnnotationPicture.CONTENT_PICTURE;
			if (this.tmpPicture != null) {
				currentAnnoContentType = this.tmpPicture
						.getPicAnnoContentType();
			}
			// der gespeicherte Content Type
			int pictureAnnoContentType = ((NodeAnnotationPicture) contentAnnotation)
					.getContentType();

			// wenn sich der ContentType geändert hat hat sich der Inhalt
			// geändert
			if (currentAnnoContentType != pictureAnnoContentType) {
				return true;
			}
			if (pictureAnnoContentType == NodeAnnotationPicture.CONTENT_PICTURE) {
				Picture savedPicture = picAnnotation.getPicture();
				Picture editorPicture = null;
				if (editorContent != null) {
					if (editorContent instanceof Picture) {
						editorPicture = (Picture) editorContent;
					}
				}
				if (savedPicture == null && editorPicture == null) {
					return false;
				}
				if (savedPicture == null || editorPicture == null) {
					return true;
				}
				if (savedPicture != null && editorPicture != null) {
					if (!savedPicture.getFile().equals(editorPicture.getFile())) {
						return true;
					}
				}
			} else if (pictureAnnoContentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
				PictureGallery savedGallery = picAnnotation.getPictureGallery();
				PictureGallery editorGallery = null;
				if (editorContent != null) {
					if (editorContent instanceof PictureGallery) {
						editorGallery = (PictureGallery) editorContent;
					}
				}
				if (savedGallery == null && editorGallery == null) {
					return false;
				}
				if (savedGallery == null || editorGallery == null) {
					return true;
				}
				if (savedGallery != null && editorGallery != null) {
					if (!savedGallery.equalsGallery(editorGallery)) {
						return true;
					}
				}
			}
			break;

		// Prüfe ob sich das Audio-File geändert hat
		case AUDIO:
			NodeAnnotationAudio audioAnnotation = (NodeAnnotationAudio) contentAnnotation;
			Audio savedAudio = audioAnnotation.getAudio();
			Audio editorAudio = null;
			AudioPart savedAudioPart = audioAnnotation.getAudioPart();
			AudioPart editorAudioPart = null;
			if (editorContent instanceof Audio) {
				editorAudio = (Audio) editorContent;
			} else if (editorContent instanceof AudioPart) {
				editorAudioPart = (AudioPart) editorContent;
			}

			if (audioAnnotation.getContentType() == NodeAnnotationAudio.CONTENT_AUDIO) {
				if (savedAudio == null && editorAudio == null) {
					return false;
				}
				if (savedAudio == null || editorAudio == null) {
					return true;
				}
				if (savedAudio != null && editorAudio != null) {
					if (!savedAudio.getFile().equals(editorAudio.getFile())) {
						return true;
					}
				}
			} else if (audioAnnotation.getContentType() == NodeAnnotationAudio.CONTENT_AUDIOPART) {
				if (savedAudioPart == null && editorAudioPart == null) {
					return false;
				}
				if (savedAudioPart == null || editorAudioPart == null) {
					return true;
				}
				if (savedAudioPart != null && editorAudioPart != null) {
					if (!savedAudioPart.getAudio().getFile()
							.equals(editorAudioPart.getAudio().getFile())) {
						return true;
					} else {
						if (!(savedAudioPart.getStart() == editorAudioPart
								.getStart() && savedAudioPart.getEnd() == editorAudioPart
								.getEnd())) {
							return true;
						}
					}
				}
			}
			break;

		// Prüfe ob sich das Video-File geändert hat
		case VIDEO:
			NodeAnnotationVideo videoAnnotation = (NodeAnnotationVideo) contentAnnotation;

			Video savedVideo = videoAnnotation.getVideo();
			Video editorVideo = null;
			Scene savedScene = videoAnnotation.getScene();
			Scene editorScene = null;
			if (editorContent instanceof Video) {
				editorVideo = (Video) editorContent;
			} else if (editorContent instanceof Scene) {
				editorScene = (Scene) editorContent;
			}

			if (videoAnnotation.getContentType() == NodeAnnotationVideo.CONTENT_NONE) {
				return true;
			}
			if (videoAnnotation.getContentType() == NodeAnnotationVideo.CONTENT_VIDEO) {
				if (savedVideo == null && editorVideo == null) {
					return false;
				}
				if (savedVideo == null || editorVideo == null) {
					return true;
				}
				if (savedVideo != null && editorVideo != null) {
					if (!savedVideo.getFile().equals(editorVideo.getFile())) {
						return true;
					}
				}
			} else if (videoAnnotation.getContentType() == NodeAnnotationVideo.CONTENT_SCENE) {
				if (savedScene == null && editorScene == null) {
					return false;
				}
				if (savedScene == null || editorScene == null) {
					return true;
				}
				if (savedScene != null && editorScene != null) {
					if (!savedScene.getVideo().getFile()
							.equals(editorScene.getVideo().getFile())) {
						return true;
					} else {
						if (!(savedScene.getStart() == editorScene.getStart() && savedScene
								.getEnd() == editorScene.getEnd())) {
							return true;
						}
					}
				}
			}
			break;
		case RICHTEXT:
			NodeAnnotationRichtext richTextAnnotation = (NodeAnnotationRichtext) contentAnnotation;
			RichText richText = richTextAnnotation.getRichtext();
			String savedRichText = richText.getContent();
			String curRichText = ((RichText) editorContent).getContent();
			
			if (!curRichText.equals(savedRichText)) {
				return true;
			}
			break;
		default:
			return false;
		}
		return false;
	}

	/**
	 * prüft ob die in der Annotation gespeicherten Werte von den eingegebenen
	 * abweichen, falls ja => true
	 * 
	 * {@link localIsDirty()} kann implementiert werden um den isDirty-State in der Subklasse festzulegen
	 * zu legen.
	 * 
	 * @return
	 */
	public boolean isDirty() {
		INodeAnnotation contentAnnotation = AnnotationFactory
				.getContentAnnotation(annotation);
		// checkt erst ob die in der subklasse implementierte isDirty methode
		// ein ergebnis liefert
		boolean localDirty = localIsDirty();
		if (localDirty) {
			return true;
		}

		if (!annotation.getTitle().equals(tmpTitle.getText())) {
			return true;
		}
		
		
		// tmpDescription may be null if the annotation does not support descriptions
		if (tmpDescription != null
				// annotation doesn't contain a description yet but editor text field 
				// contains a description
				&& ((contentAnnotation.getDescription() == null 	
						&& !tmpDescription.getText().isEmpty())
				// annotation contains a description which differs from the
				// text in the editor text field
					|| (contentAnnotation.getDescription() != null 
						&& !contentAnnotation.getDescription()
							.equals(tmpDescription.getText()))
					)) {
			return true;
		}

		// Mute
		if (annotationType.getContentType().equals(AnnotationContentType.AUDIO)
				|| annotationType.getContentType().equals(
						AnnotationContentType.VIDEO)) {
			if (annotation.isMuteVideo() != muteVideoButton.getSelection()) {
				return true;
			}
		}
			
		if (!(annotationType.getContentType()
						.equals(AnnotationContentType.SUBTITLE))) {
			
			// Screenarea
			if (!annotation.getScreenArea().equals(tmpScreenArea)) {
				return true;
			}

			// Overlay path items
			if (tmpScreenArea.equals(ScreenArea.OVERLAY)) {
				List<OverlayPathItem> savedOverlayItems = annotation
						.getOverlayPath();

				// wenn die Listenlängen gleich sind vergleiche die Items
				// ansonsten hat sich etwas geändert
				if (savedOverlayItems.size() == tmpOpItems.size()) {
					for (int i = 0; i < tmpOpItems.size(); i++) {
						OverlayPathItem changed = tmpOpItems.get(i);
						// da pro Zeit nur ein OverlayPathItem existiert
						// prüfe ob die Zeit gleich ist
						boolean foundSameTime = false;
						for (int j = 0; j < savedOverlayItems.size(); j++) {
							OverlayPathItem saved = savedOverlayItems.get(j);
							if (saved.getTime() == changed.getTime()) {
								foundSameTime = true;
								boolean sameDim = true;
								// prüfte ob sich die Dimensionen geändert haben
								if (saved.getX() != changed.getX()) {
									sameDim = false;
								} else if (saved.getY() != changed.getY()) {
									sameDim = false;
								} else if (saved.getWidth() != changed
										.getWidth()) {
									sameDim = false;
								} else if (saved.getHeight() != changed
										.getHeight()) {
									sameDim = false;
								}
								if (!sameDim) {
									return true;
								}
							}
						}
						if (!foundSameTime) {
							return true;
						}
					}
				} else {
					return true;
				}
			}
		}
		return hasContentChanged();
	}

	@Override
	public void handleEvent(SivaEvent event) {
		editorContent = (IAbstractBean) event.getValue();
		updateDirty();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!evt.getPropertyName()
				.equals(ProjectSettings.PROP_DIMENSION)
				&& !isDisposed()) {
			if (!ScreenAreaSelector
					.checkScreenArea(tmpScreenArea)) {
				tmpScreenArea = ScreenAreaSelector
						.getAnnotationScreenArea();
				areaSelector.setScreenArea(tmpScreenArea);
				updateDirty();
			}
		}
	}

	protected abstract boolean localIsDirty();
}

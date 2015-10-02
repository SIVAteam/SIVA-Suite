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
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationContentType;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationFactory;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationType;
import org.iviPro.editors.annotationeditor.components.contenteditors.ContentEditor;
import org.iviPro.editors.annotationeditor.components.contenteditors.DragAndDropEditor;
import org.iviPro.editors.annotationeditor.components.contenteditors.PdfEditor;
import org.iviPro.editors.annotationeditor.components.contenteditors.PictureEditor;
import org.iviPro.editors.annotationeditor.components.contenteditors.SubtitleEditor;
import org.iviPro.editors.annotationeditor.components.contenteditors.VideoEditor;
import org.iviPro.editors.annotationeditor.components.contenteditors.richtext.RichHTMLEditor;
import org.iviPro.editors.common.ScreenAreaSelector;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.model.BeanList;
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
import org.iviPro.model.resources.IVideoResource;
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
	
	/**
	 * The editor in which this widget is used.
	 */
	private IAbstractEditor editor;

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
	// Partially refactored by using a common DragAndDropEditor and a ContentEditor 
	// interface. However, structure could be simplified by using a single editor 
	// variable relying on an abstract class and factory. Got no time for that now.
	protected ContentEditor contentEditor = null;
			
	/**
	 * Time of the thumbnail which can be altered in video or scene annotations.
	 * I'm really sorry for adding more of this non polymorphic b..s.. but time is 
	 * too scarce and this class is too f.. up to refactor everything to a 
	 * polymorphic solution. May the Lord forgive me.
	 */
	protected long tmpThumbnailTime;
	
	/**
	 * Description of content (used for e.g. PDF documents) 
	 */
	protected String tmpContentDescription = null;

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


	// Annotationstyp, z.B. um festzustellen, welchen Content Editor man
	// benötigt.
	protected AnnotationType annotationType;

	// Buttons für die Auswahl zwischen Bildanno und Bildergalerieanno
	protected Button picButton;
	protected Button galButton;
	protected Text pictureAnnoColumnField;

	public AbstractAnnotationDefineWidget(Composite parent, int style,
			INodeAnnotation annotation, AnnotationType annotationType,
			CTabItem it, IAbstractEditor editor) {
		super(parent, style);
		this.tmpOpItems = new LinkedList<OverlayPathItem>();
		this.tabItem = it;
		this.annotationType = annotationType;
		this.annotation = annotation;
		this.editor = editor;

		Application.getCurrentProject().getSettings()
				.addPropertyChangeListener(this);
		Application.getCurrentProject().getMediaObjects()
				.addPropertyChangeListener(this);
	}
	
	@Override
	public void dispose() {
		Application.getCurrentProject().getSettings()
				.removePropertyChangeListener(this);
		Application.getCurrentProject().getMediaObjects()
				.removePropertyChangeListener(this);
		super.dispose();
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
		
		// Update content and editor
		boolean editorExists = (contentEditor != null);
		switch (contentType) {
		case AUDIO:
			NodeAnnotationAudio audAnno = (NodeAnnotationAudio) contentAnnotation;
			if (audAnno.getContentType() == NodeAnnotationAudio.CONTENT_AUDIO) {
				editorContent = ((NodeAnnotationAudio) contentAnnotation)
						.getAudio();
			} else if (audAnno.getContentType() == NodeAnnotationAudio.CONTENT_AUDIOPART) {
				editorContent = ((NodeAnnotationAudio) contentAnnotation)
						.getAudioPart();
			}

			if (!editorExists) {
				contentEditor = new DragAndDropEditor(contentEditorComposite, SWT.NONE,
						editorContent, new Class[] {Audio.class, AudioPart.class});
			}
			break;

		case VIDEO:
			if (((NodeAnnotationVideo) contentAnnotation).getContentType() == NodeAnnotationVideo.CONTENT_SCENE) {
				editorContent = ((NodeAnnotationVideo) contentAnnotation)
						.getScene();
			} else {
				editorContent = ((NodeAnnotationVideo) contentAnnotation)
						.getVideo();
			}
			
			if (editorContent != null) {
				tmpThumbnailTime = ((IVideoResource)editorContent).getThumbnail().getTime();
			}

			if (!editorExists) {
				contentEditor = new VideoEditor(contentEditorComposite, SWT.NONE,
						editorContent);
			}			
			break;

		case SUBTITLE:
			editorContent = ((NodeAnnotationSubtitle) contentAnnotation)
					.getSubtitle();
			
			if (!editorExists) {
				contentEditor = new SubtitleEditor(contentEditorComposite,
						SWT.NONE, (Subtitle) editorContent);
			}
			break;

		case RICHTEXT:
			editorContent = ((NodeAnnotationRichtext) contentAnnotation)
						.getRichtext();
			
			if (!editorExists) {
				contentEditor = new RichHTMLEditor(contentEditorComposite,
						SWT.NONE, (RichText) editorContent);
			}
			break;

		case PICTURE:
			NodeAnnotationPicture pictureAnno = (NodeAnnotationPicture) contentAnnotation;
			int pictureAnnotationType = pictureAnno.getContentType();
			
			boolean isGallery = (pictureAnnotationType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY);
			picButton.setSelection(!isGallery);
			galButton.setSelection(isGallery);
			
			editorContent = (isGallery ? pictureAnno.getPictureGallery()
					: pictureAnno.getPicture());		

			if (!editorExists) {
				if (isGallery) {
					contentEditor = new PictureEditor(contentEditorComposite,
							SWT.NONE, (PictureGallery) editorContent);
				} else {
					contentEditor = new PictureEditor(contentEditorComposite,
							SWT.NONE, (Picture) editorContent);
				}
			}
			
			if (isGallery) {
				if (editorContent != null) {
					pictureAnnoColumnField
					.setText("" //$NON-NLS-1$
							+ ((PictureGallery)editorContent)
							.getNumberColumns());
				} else {
					pictureAnnoColumnField.setText("" //$NON-NLS-1$
							+ PictureGallery.PICGAL_COLS_STD);
				}
			}
			break;
		
		case PDF:
			editorContent = ((NodeAnnotationPdf) contentAnnotation).getPdf();
			if (editorContent != null) {
				tmpContentDescription = ((PdfDocument)editorContent).getSummary();
			}
			if (!editorExists) {
				contentEditor = new PdfEditor(contentEditorComposite, SWT.NONE,
						(PdfDocument)editorContent);
			}
			break;
		}
		
		if (editorExists) {
			contentEditor.setContent(editorContent);
		} else {
			AbstractAnnotationDefineWidget.this.layout(true);				
			contentEditor.addSivaEventConsumer(this);
		}
		
		// Listen for changes on the content
		if (editorContent != null) {
			editorContent.addPropertyChangeListener(this);
		}
		
		// Need to listen for deletion of an AudioPart/Scene on the related media 
		// object; they are not part of the projects' media objects list themselves
		if (editorContent instanceof AudioPart) {
			((AudioPart)editorContent).getAudio().getAudioParts()
					.addPropertyChangeListener(this);
		} else if (editorContent instanceof Scene) {
			((Scene)editorContent).getVideo().getScenes()
					.addPropertyChangeListener(this);
		}
		
		// Remove listener on disposal
		addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (contentEditor != null) {
					contentEditor.removeSivaEventConsumer(
							AbstractAnnotationDefineWidget.this);
				}

				if (editorContent != null) {
					editorContent.removePropertyChangeListener(
							AbstractAnnotationDefineWidget.this);
				}
							
				// Remove listeners monitoring deletion of the AudioPart/Scene
				if (editorContent instanceof AudioPart) {
					((AudioPart)editorContent).getAudio().getAudioParts()
							.removePropertyChangeListener(
									AbstractAnnotationDefineWidget.this);
				} else if (editorContent instanceof Scene) {
					((Scene) editorContent).getVideo().getScenes()
							.removePropertyChangeListener(
									AbstractAnnotationDefineWidget.this);
				}			
			}
		});
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
		editor.updateDirtyStatus();
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
			if (editorContent instanceof Subtitle) {
				subtitle = (Subtitle) editorContent;
				Subtitle savedSub = ((NodeAnnotationSubtitle) contentAnnotation)
						.getSubtitle();
				if (!(subtitle.getTitle().equals(savedSub.getTitle()) && subtitle
						.getDescription().equals(savedSub.getDescription()))) {
					return true;
				}
			}
			break;

		// Prüfe ob sich das Bild geändert hat
		case PICTURE:

			NodeAnnotationPicture picAnnotation = (NodeAnnotationPicture) contentAnnotation;
			// der aktuell eingestellte ContentType
			int currentAnnoContentType = NodeAnnotationPicture.CONTENT_PICTURE;
			if (contentEditor != null) {
				currentAnnoContentType = ((PictureEditor)contentEditor)
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

			if (videoAnnotation.getContentType() == NodeAnnotationVideo.CONTENT_VIDEO) {
				if (savedVideo == null && editorVideo == null) {
					return false;
				}
				if (savedVideo == null || editorVideo == null) {
					return true;
				}
				if (!savedVideo.getFile().equals(editorVideo.getFile())) {
						return true;
				}
				if (savedVideo.getThumbnail().getTime() != tmpThumbnailTime) {
					return true;
				}
			} else if (videoAnnotation.getContentType() == NodeAnnotationVideo.CONTENT_SCENE) {
				if (savedScene == null && editorScene == null) {
					return false;
				}
				if (savedScene == null || editorScene == null) {
					return true;
				}
				if (!savedScene.getVideo().getFile()
						.equals(editorScene.getVideo().getFile())) {
					return true;
				} 
				if (!(savedScene.getStart() == editorScene.getStart() && savedScene
								.getEnd() == editorScene.getEnd())) {
						return true;
				}
				if (savedScene.getThumbnail().getTime() != tmpThumbnailTime) {
					return true;
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
		case PDF:
			NodeAnnotationPdf pdfAnnotation = (NodeAnnotationPdf) contentAnnotation;
			PdfDocument savedPdf = pdfAnnotation.getPdf();
			if (savedPdf != (PdfDocument) editorContent) {
				return true;
			}
			if (savedPdf.getSummary() == null && tmpContentDescription != null) {
				return true;
			} else if (savedPdf.getSummary() != null 
					&& !savedPdf.getSummary().equals(tmpContentDescription)) {
				return true;
			}
			
			
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
		// Listen to events triggered by the content editors
		if (event.getEventType() == SivaEventType.CONTENT_CHANGED) {
			// Remove listener on old content
			if (editorContent != null) {
				editorContent.removePropertyChangeListener(this);
			}
			// Remove listeners monitoring deletion of the AudioPart/Scene
			if (editorContent instanceof AudioPart) {
				((AudioPart)editorContent).getAudio().getAudioParts()
						.removePropertyChangeListener(this);
			} else if (editorContent instanceof Scene) {
				((Scene) editorContent).getVideo().getScenes()
						.removePropertyChangeListener(this);
			}
			
			// Set new content
			editorContent = (IAbstractBean) event.getValue();			
			// Need to listen for deletion of an AudioPart/Scene on the related media 
			// object; they are not part of the projects' media objects list themselves
			if (editorContent instanceof AudioPart) {
				((AudioPart)editorContent).getAudio().getAudioParts()
						.addPropertyChangeListener(this);
			} else if (editorContent instanceof Scene) {
				((Scene)editorContent).getVideo().getScenes()
						.addPropertyChangeListener(this);
			}
			
			if (editorContent instanceof Video
					|| editorContent instanceof Scene) {
				tmpThumbnailTime = ((IVideoResource)editorContent).getThumbnail()
						.getTime();
			}
			if (editorContent instanceof PdfDocument) {
				tmpContentDescription = ((PdfDocument)editorContent).getSummary();
			}
			// Listen to changes of new content
			if (editorContent != null) {
				editorContent.addPropertyChangeListener(this);
			}
		} else if (event.getEventType() == SivaEventType.TIME_SELECTION) {
			tmpThumbnailTime = (Long) event.getValue();
		} else if (event.getEventType() == SivaEventType.DESCRIPTION_CHANGED) {
			tmpContentDescription = (String)event.getValue();
		}
		updateDirty();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (isDisposed()) {
			return;
		}
		// Media object or AudioPart/Scene used as content or the respective
		// parent object has been deleted
		if (event.getPropertyName().equals(BeanList.PROP_ITEM_REMOVED)) {
			Object deleted = event.getNewValue();
			if (deleted != null && (editorContent == deleted
				|| (editorContent instanceof AudioPart
					&& ((AudioPart)editorContent).getAudio() == deleted)
				|| (editorContent instanceof Scene
					&& ((Scene)editorContent).getVideo() == deleted))) {
				contentEditor.setContent(null);
			}
		}		

		
		// Video dimension has been changed. Adapt screen area.
		if (event.getSource() == Application.getCurrentProject().getSettings()) {
			if (event.getPropertyName()
					.equals(ProjectSettings.PROP_DIMENSION)) {
				if (tmpScreenArea != null && !ScreenAreaSelector
						.checkScreenArea(tmpScreenArea)) {
					tmpScreenArea = ScreenAreaSelector
							.getAnnotationScreenArea();
					areaSelector.setScreenArea(tmpScreenArea);
					updateDirty();
				}
			}
		}

		if (event.getSource() == editorContent) {	
			// Thumbnail has been changed in other editor (e.g. scene editor)
			if (event.getPropertyName().equals(Scene.PROP_THUMB) 
					|| event.getPropertyName().equals(Video.PROP_THUMB)) {
				if (editorContent instanceof IVideoResource) {
					contentEditor.setContent(editorContent);
					tmpThumbnailTime = ((IVideoResource)editorContent)
							.getThumbnail().getTime();
					this.layout();
				}

			}
			// PDF description changed
			if (event.getPropertyName().equals(PdfDocument.PROP_PDFDESCRIPTION)) {
				if (editorContent instanceof PdfDocument) {
					contentEditor.setContent(editorContent);
					tmpContentDescription = ((PdfDocument)editorContent)
							.getSummary();
					this.layout();
				}

			}
		}
	}

	protected abstract boolean localIsDirty();
}

package org.iviPro.operations.annotation;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.editors.common.BeanNameGenerator;
import org.iviPro.model.BeanList;
import org.iviPro.model.DummyFile;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.Project;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPdf;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.IVideoResource;
import org.iviPro.model.resources.PdfDocument;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Subtitle;
import org.iviPro.model.resources.Video;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Aendern der Zeit von Szenne und Annotationen.
 * 
 * @author hoffmanj
 * 
 */
public class ChangeContentOperation extends IAbstractOperation {


	private static Logger logger = Logger.getLogger(ChangeContentOperation.class);
	
	private final INodeAnnotation target;
	private IAbstractBean oldContent;
	private IAbstractBean newContent;
	private long oldThumbnailTime;
	private long newThumbnailTime;
	private String oldContentDescription;
	private String newContentDescription;

	// für Richtext Annotationen
	private boolean addToRepository;
		
	/**
	 * Erstellt eine neue Operation zum Aendern des Content einer Annotation
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Content geaendert werden soll.
	 * @param newTime
	 *            Die neuen Keywords
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter null ist.
	 */
	public ChangeContentOperation(INodeAnnotation target, IAbstractBean content, 
			String contentDescription, long thumbnailTime)
			throws IllegalArgumentException {
		super(Messages.ChangeScreenPositionOperation_Label);
		
		if (target == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
				
		if (target instanceof NodeAnnotationVideo) {
			NodeAnnotationVideo videoAnnotation = (NodeAnnotationVideo) target;
			if (content instanceof Video) {
				this.newContent = content;
			} else
			if (content instanceof Scene) {
				this.newContent = content;
			}
			newThumbnailTime = thumbnailTime;
						
			int contentType = videoAnnotation.getContentType();
			if (contentType == NodeAnnotationVideo.CONTENT_VIDEO) {
				Video oldVid = videoAnnotation.getVideo();
				this.oldContent = oldVid;				
			} else
			if (contentType == NodeAnnotationVideo.CONTENT_SCENE) {	
				Scene oldScene = videoAnnotation.getScene();
				this.oldContent = oldScene;
			}
			if (oldContent != null) {
				oldThumbnailTime = ((IVideoResource)oldContent).getThumbnail().getTime();
			}
		} else if (target instanceof NodeAnnotationAudio) {
			NodeAnnotationAudio audioAnno = (NodeAnnotationAudio) target;
			if (content instanceof Audio) {
				this.newContent = content;
			} else
			if (content instanceof AudioPart) {
				this.newContent = content;
			}
			
			int contentType = audioAnno.getContentType();
			if (contentType == NodeAnnotationAudio.CONTENT_AUDIO) {
				this.oldContent = audioAnno.getAudio();
			} else 
			if (contentType == NodeAnnotationAudio.CONTENT_AUDIOPART) {
				this.oldContent = audioAnno.getAudioPart();
			}				
		} else if (target instanceof NodeAnnotationPicture) {
			NodeAnnotationPicture pictureAnno = (NodeAnnotationPicture) target;
			if (content instanceof Picture) {
				this.newContent = content;
			} else if (content instanceof PictureGallery) {
				// die Galerie aus dem Editor
				PictureGallery contentGallery = (PictureGallery) content;
				PictureGallery newGallery = pictureAnno.getPictureGallery();
				newGallery.setPictures(((PictureGallery) content).getPictures());
				newGallery.setTitle(contentGallery.getTitle());
				newGallery.setNumberColumns(contentGallery.getNumberColumns());
				this.newContent = newGallery;
			}
			
			int contentType = pictureAnno.getContentType();
			if (contentType == NodeAnnotationPicture.CONTENT_PICTURE) {
				Picture old = pictureAnno.getPicture();
				this.oldContent = old;				
			} else
			if (contentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
				PictureGallery old = pictureAnno.getPictureGallery();
				this.oldContent = old;
			}
			
		} else if (target instanceof NodeAnnotationSubtitle) {
			if (content instanceof Subtitle) {
				Subtitle contentSubtitle = (Subtitle) content;
				Subtitle newSubtitle = ((NodeAnnotationSubtitle) target).getSubtitle();
				newSubtitle.setTitle(contentSubtitle.getTitle());
				newSubtitle.setDescription(contentSubtitle.getDescription());
				this.newContent = newSubtitle;
			} else {
				this.newContent = new Subtitle("", Application.getCurrentProject());
			}		
			this.oldContent = ((NodeAnnotationSubtitle) target).getSubtitle();
			
		} else if (target instanceof NodeAnnotationRichtext) {			
			NodeAnnotationRichtext richtextAnnotation = (NodeAnnotationRichtext) target;
			RichText annoRichtext = richtextAnnotation.getRichtext();			
			
			/* When the old content is backed by an imported html file and the new 
			 * content is a modified version of it, the new content needs to be 
			 * saved to a new file to not overwrite the content of the imported 
			 * file. In case, the document did not change we can keep the reference
			 * to the imported file. 
			 * If the annotation has not been saved yet at all, a new file
			 * has to be created as well. 
			 */
			boolean isDummy = annoRichtext.getFile().getValue() instanceof DummyFile;
			boolean contentChanged = annoRichtext != content;
			addToRepository = isDummy || (annoRichtext.isFromMedia() && contentChanged);
					
			oldContent = annoRichtext;	
			newContent = content;
		} else if (target instanceof NodeAnnotationPdf) {
			oldContent = ((NodeAnnotationPdf) target).getPdf();
			newContent = content;
			if (oldContent != null) {
				oldContentDescription = ((PdfDocument)oldContent).getSummary();
			}
			newContentDescription = contentDescription;
		}			
		this.target = target;
	}

	@Override
	public boolean canExecute() {
		return target != null && newContent != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ChangeScreenPositionOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (target instanceof NodeAnnotationVideo) {
			if (newContent instanceof Video) {
				((NodeAnnotationVideo) target).setVideo((Video) newContent);
			} else if (newContent instanceof Scene) {
				((NodeAnnotationVideo) target).setScene((Scene) newContent);
			}
			if (newContent != null) {
				((IVideoResource)newContent).changeThumbnailTime(newThumbnailTime);
			}
		} else if (target instanceof NodeAnnotationAudio) {
			if (newContent instanceof Audio) {
				((NodeAnnotationAudio) target).setAudio((Audio) newContent);
			} else if (newContent instanceof AudioPart) {
				((NodeAnnotationAudio) target).setAudioPart((AudioPart) newContent);
			}
		} else if (target instanceof NodeAnnotationPicture) {
			if (newContent instanceof Picture) {
				((NodeAnnotationPicture) target).setPicture((Picture) newContent);
			} else if (newContent instanceof PictureGallery) {	
				PictureGallery newGallery = (PictureGallery) newContent;
				// hole alle Gallerien 
				BeanList<IAbstractBean> beans = Application.getCurrentProject().getMediaObjects();
				BeanList<PictureGallery> galleries = new BeanList<PictureGallery>(Application.getCurrentProject());
				for (IAbstractBean bean : beans) {
					if (bean instanceof PictureGallery) {
						galleries.add((PictureGallery) bean);					
					}
				}	

				boolean found = false;
				PictureGallery foundGallery = null;
				// Suche ob die Galerie bereits vorhanden ist, falls nein wird
				// sie ins Repository hinzugefügt und ein Name generiert
				for (PictureGallery gal : galleries) {
					if (gal.getTitle().equals(newGallery.getTitle())) {
						found = true;
						foundGallery = gal;
						break;
					}
				}
				if (!found) {
					addToRepository = true;
					BeanNameGenerator bng = new BeanNameGenerator("", newGallery, galleries, target.getTitle());					
					String newName = bng.generateAuto();
					newGallery.setTitle(newName);
					Application.getCurrentProject().getMediaObjects().add(newGallery);
				}	
				// falls eine Gallerie bereits besteht verwendet die neue Annotation auch diese.
				// => Gallerie wird in mehreren Annotationen verwendet.
				if (foundGallery != null) {
					((NodeAnnotationPicture) target).setPictureGallery(foundGallery);
				} else {
					((NodeAnnotationPicture) target).setPictureGallery(newGallery);	
				}								
			}
		} else if (target instanceof NodeAnnotationSubtitle && newContent instanceof Subtitle) {	
			Subtitle newSubtitle = (Subtitle) newContent;
			BeanList<IAbstractBean> beans = Application.getCurrentProject().getMediaObjects();
			BeanList<Subtitle> subtitles = new BeanList<Subtitle>(Application.getCurrentProject());
			for (IAbstractBean bean : beans) {
				if (bean instanceof Subtitle) {
					subtitles.add((Subtitle) bean);					
				}
			}					
			boolean found = false;
			Subtitle foundSubtitle = null;
			// Suche ob der Subtitle bereits vorhanden ist, falls nein wird
			// er ins Repository hinzugefügt und ein Name generiert
			for (Subtitle sub : subtitles) {
				if (sub.getTitle().equals(newSubtitle.getTitle())) {
					found = true;
					foundSubtitle = sub;
					break;
				}
			}
			if (!found) {
				addToRepository = true;
				BeanNameGenerator bng = new BeanNameGenerator("", newSubtitle, subtitles, target.getTitle());
				String fileName = bng.generateAuto();
				newSubtitle.setTitle(fileName);
				Application.getCurrentProject().getMediaObjects().add(newSubtitle);
			}	
			// falls ein Subtitle bereits besteht verwendet die neue Annotation auch diesen.
			// => Subtitle werden in mehreren Annotationen verwendet.
			if (foundSubtitle != null) {
				((NodeAnnotationSubtitle) target).setSubtitle(foundSubtitle);
			} else {
				((NodeAnnotationSubtitle) target).setSubtitle(newSubtitle);	
			}
		} else if (target instanceof NodeAnnotationRichtext) {
			NodeAnnotationRichtext richtextAnnotation = (NodeAnnotationRichtext) target;
			RichText richtext = (RichText)newContent;
			Project project = Application.getCurrentProject();
			
			/* Using setRichtext() fires an event which triggers 
			 * AADW.initContentEditors() which in turn clones the current richtext
			 * to the RichHTMLEditor. Therefore, the new title has to be set before 
			 * setting the richtext via setRichtext().
			 */  
			if (addToRepository) {
				// Generate richtext title
				BeanList<RichText> rtList = new BeanList<RichText>(project);
				for (IAbstractBean obj : project.getMediaObjects()) {
					if (obj instanceof RichText) {
						rtList.add((RichText) obj);
					}
				}
				// Clear title to not reuse dummy title or title of imported file
				newContent.setTitle("");
				String titlePrefix; 
				if (richtextAnnotation.isTriggerAnnotation()) {
					titlePrefix = richtextAnnotation.getParentMarkAnno().getTitle(); 
				} else {
					titlePrefix = richtextAnnotation.getTitle();
				}
				BeanNameGenerator bng = new BeanNameGenerator("", newContent, rtList,
						titlePrefix);
				String richTextTitle = bng.generateAuto();
				newContent.setTitle(richTextTitle);
				((RichText)newContent).createFileFromTitle();
			}
			
			// Set richtext before modifying the media list to avoid deletion events
			// being sent to open editors.
			richtextAnnotation.setRichtext(richtext);
				
			if (addToRepository) {	
				// Add richtext to repository
				project.getMediaObjects().add(richtext);
				project.getUnusedFiles().remove(richtext.getFile().getValue());			
			} else if (newContent != oldContent) {
				project.getMediaObjects().remove(oldContent);
				project.getMediaObjects().add(newContent);
			}			
			
		} else if (target instanceof NodeAnnotationPdf) {
			((PdfDocument)newContent).setSummary(newContentDescription);
			((NodeAnnotationPdf)target).setPdf((PdfDocument)newContent);			
		}
		return Status.OK_STATUS;		
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (target instanceof NodeAnnotationVideo) {
			if (oldContent == null || oldContent instanceof Video) {
				((NodeAnnotationVideo) target).setVideo((Video) oldContent);
			} else if (oldContent instanceof Scene) {
				((NodeAnnotationVideo) target).setScene((Scene) oldContent);
			}
			if (oldContent != null) {
				((IVideoResource)oldContent).changeThumbnailTime(oldThumbnailTime);
			}
		} else if (target instanceof NodeAnnotationAudio) {
			if (oldContent == null || oldContent instanceof Audio) {
				((NodeAnnotationAudio) target).setAudio((Audio) oldContent);
			} else if (oldContent instanceof AudioPart) {
				((NodeAnnotationAudio) target).setAudioPart((AudioPart) oldContent);
			}
		} else if (target instanceof NodeAnnotationPicture) {
			if (oldContent == null || oldContent instanceof Picture) {
				((NodeAnnotationPicture) target).setPicture((Picture) oldContent);	
			} else if (oldContent instanceof PictureGallery) {
				((NodeAnnotationPicture) target).setPictureGallery((PictureGallery) oldContent);
				// falls die PictureGallery hinzugefügt wurde, war das die erste Annotation die diesen verwendet hat
				// => ihn nur wenn addToRepository gesetzt ist
				if (addToRepository) {
					Application.getCurrentProject().getMediaObjects().remove((PictureGallery) oldContent);
				}
			}
		} else if (target instanceof NodeAnnotationSubtitle) {
			((NodeAnnotationSubtitle) target).setSubtitle((Subtitle) oldContent);
			// falls der subtitle hinzugefügt wurde, war das die erste Annotation die diesen verwendet hat
			// => ihn nur wenn addToRepository gesetzt ist
			if (addToRepository) {
				Application.getCurrentProject().getMediaObjects().remove((Subtitle) oldContent);
			}
		} else if (target instanceof NodeAnnotationRichtext) {
			NodeAnnotationRichtext richtextAnnotation = (NodeAnnotationRichtext) target;			
			RichText richtext = (RichText)oldContent;
			Project project = Application.getCurrentProject();
			
			// Set richtext before modifying the media list to avoid deletion events
			// being sent to open editors.
			richtextAnnotation.setRichtext(richtext);	
			if (addToRepository) {
				project.getMediaObjects().remove(newContent);
				project.getUnusedFiles().add(((RichText)newContent).getFile().getValue());
			} else if (oldContent != newContent) {
				project.getMediaObjects().remove(newContent);
				project.getMediaObjects().add(oldContent);
			}			
					
		} else if (target instanceof NodeAnnotationPdf) {
			if (oldContent != null) {
				((PdfDocument)oldContent).setSummary(oldContentDescription);
			}
			((NodeAnnotationPdf)target).setPdf((PdfDocument)oldContent);
		}
		return Status.OK_STATUS;
	}
}

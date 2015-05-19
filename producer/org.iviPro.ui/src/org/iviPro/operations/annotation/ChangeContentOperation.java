package org.iviPro.operations.annotation;

import java.io.File;

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
import org.iviPro.model.graph.NodeAnnotationText;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
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
	public ChangeContentOperation(INodeAnnotation target, IAbstractBean content)
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
			
			if (videoAnnotation.getContentType() == NodeAnnotationVideo.CONTENT_VIDEO) {
				Video oldVid = videoAnnotation.getVideo();
				this.oldContent = oldVid;				
			} else
			if (videoAnnotation.getContentType() == NodeAnnotationVideo.CONTENT_SCENE) {	
				Scene oldScene = videoAnnotation.getScene();
				this.oldContent = oldScene;
			}
		} else if (target instanceof NodeAnnotationAudio) {
			if (content instanceof Audio) {
				this.newContent = content;
			} else
			if (content instanceof AudioPart) {
				this.newContent = content;
			}
			
			int contentType = ((NodeAnnotationAudio) target).getContentType();
			if (contentType == NodeAnnotationAudio.CONTENT_AUDIO) {
				this.oldContent = ((NodeAnnotationAudio) target).getAudio();
			} else 
			if (contentType == NodeAnnotationAudio.CONTENT_AUDIOPART) {
				this.oldContent = ((NodeAnnotationAudio) target).getAudioPart();
			}	
			
		} else if (target instanceof NodeAnnotationPicture) {
			if (content instanceof Picture) {
				this.newContent = content;
			} else if (content instanceof PictureGallery) {
				// die Galerie aus dem Editor
				PictureGallery contentGallery = (PictureGallery) content;
				PictureGallery newGallery = ((NodeAnnotationPicture) target).getPictureGallery();
				newGallery.setPictures(((PictureGallery) content).getPictures());
				newGallery.setTitle(contentGallery.getTitle());
				newGallery.setNumberColumns(contentGallery.getNumberColumns());
				this.newContent = newGallery;
			}
			
			int contentType = ((NodeAnnotationPicture) target).getContentType();
			if (contentType == NodeAnnotationPicture.CONTENT_PICTURE) {
				Picture old = ((NodeAnnotationPicture) target).getPicture();
				this.oldContent = old;				
			} else
			if (contentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
				PictureGallery old = ((NodeAnnotationPicture) target).getPictureGallery();
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
			
			boolean isDummy = annoRichtext.getFile().getValue() instanceof DummyFile;
			addToRepository = isDummy || annoRichtext.isFromMedia();
			
			/* When the current annotation richtext is backed by a loaded html
			 * file or has not been saved as a file yet, a file for the new 
			 * richtext needs to be created.
			 */
			if (addToRepository) {
				RichText castContent = (RichText)content;
				Project project = Application.getCurrentProject();
				BeanList<RichText> rtList = new BeanList<RichText>(project);
				for (IAbstractBean obj : project.getMediaObjects()) {
					if (obj instanceof RichText) {
						rtList.add((RichText) obj);
					}
				}
				// generiere den Dateinamen für den Richtext
				// prüfe ob es ein neuer Richtext ist und setze 
				// den Namen auf leer
				if (castContent.getTitle().contains("Unnamed_Richtext")) {
					castContent.setTitle("");
				}
				BeanNameGenerator bng = new BeanNameGenerator("", castContent, rtList, richtextAnnotation.getTitle());
				String richTextTitle = bng.generateAuto();
				castContent.setTitle(richTextTitle);
				castContent.createFileFromTitle();
			}	
			
			oldContent = annoRichtext;	
			newContent = new RichText((RichText)content);
		} else if (target instanceof NodeAnnotationPdf) {
			NodeAnnotationPdf pdfAnnotation = (NodeAnnotationPdf) target;
			oldContent = pdfAnnotation.getPdf();
			newContent = content;			
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
			if (addToRepository) {
				project.getMediaObjects().add(richtext);
				project.getUnusedFiles().remove(richtext.getFile().getValue());			
			} else {		
				project.getMediaObjects().remove(oldContent);
				project.getMediaObjects().add(richtext);
			}			
			richtextAnnotation.setRichtext(richtext);
		} else if (target instanceof NodeAnnotationPdf) {
			((NodeAnnotationPdf)target).setPdf((PdfDocument)newContent);
		}
		return Status.OK_STATUS;		
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (target instanceof NodeAnnotationVideo) {
			if (oldContent instanceof Video) {
				((NodeAnnotationVideo) target).setVideo((Video) oldContent);
			} else if (oldContent instanceof Scene) {
				((NodeAnnotationVideo) target).setScene((Scene) oldContent);
			} else { 
				((NodeAnnotationVideo) target).setVideo((Video) oldContent);
			}
		} else if (target instanceof NodeAnnotationAudio) {
			if (oldContent instanceof Audio) {
				((NodeAnnotationAudio) target).setAudio((Audio) oldContent);
			} else if (oldContent instanceof AudioPart) {
				((NodeAnnotationAudio) target).setAudioPart((AudioPart) oldContent);
			}
		} else if (target instanceof NodeAnnotationPicture) {
			if (oldContent instanceof Picture) {
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
			if (addToRepository) {
				project.getMediaObjects().remove(newContent);
				project.getUnusedFiles().add(((RichText)newContent).getFile().getValue());
			} else {
				project.getMediaObjects().remove(newContent);
				project.getMediaObjects().add(richtext);
			}			
			richtextAnnotation.setRichtext(richtext);			
		} else if (target instanceof NodeAnnotationPdf) {
			((NodeAnnotationPdf)target).setPdf((PdfDocument)oldContent);
		}
		return Status.OK_STATUS;
	}
}

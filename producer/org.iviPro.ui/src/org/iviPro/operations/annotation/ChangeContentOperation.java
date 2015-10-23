package org.iviPro.operations.annotation;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.presentations.util.ReplaceDragHandler;
import org.iviPro.application.Application;
import org.iviPro.editors.common.BeanNameGenerator;
import org.iviPro.model.BeanList;
import org.iviPro.model.DummyFile;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.Project;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
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
	/**
	 * Replacement content refers to the content which was used to replace 
	 * oldContent, but without any changes which might have been applied 
	 * after the replacement took place (replacement + changes = newContent).   
	 */
	private IAbstractBean replacementContent;
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
			IAbstractBean replacementContent, String contentDescription,
			long thumbnailTime)	throws IllegalArgumentException {
		super(Messages.ChangeScreenPositionOperation_Label);
		
		if (target == null) {
			throw new IllegalArgumentException(
					"Target annotation may not be null."); //$NON-NLS-1$
		}
		this.replacementContent = replacementContent;
				
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
			NodeAnnotationPicture picAnno = (NodeAnnotationPicture) target;
			this.newContent = content;
						
			int contentType = picAnno.getContentType();
			if (contentType == NodeAnnotationPicture.CONTENT_PICTURE) {
				this.oldContent = picAnno.getPicture();			
			} else
			if (contentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
				this.oldContent = picAnno.getPictureGallery();
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
			NodeAnnotationPicture picAnno = ((NodeAnnotationPicture) target);
			if (newContent instanceof Picture) {
				picAnno.setPicture((Picture) newContent);
			} else if (newContent instanceof PictureGallery) {
				PictureGallery newGallery = (PictureGallery) newContent;
				Project project = Application.getCurrentProject();
				
				// Check if the gallery of the annotation is already stored in the 
				// media repository. In this case we will just update its properties. 
				addToRepository = true;
				BeanList<IAbstractBean> beans = Application.getCurrentProject().getMediaObjects();
				BeanList<PictureGallery> galleries = new BeanList<PictureGallery>(Application.getCurrentProject());
				// Check if the actual gallery of the annotation is already part of media repository
				for (IAbstractBean bean : beans) {
					if (bean instanceof PictureGallery) {
						galleries.add((PictureGallery)bean);
						if (bean == oldContent) {
							addToRepository = false;
						}
					}
				}
				
				if (addToRepository) {
					// Create title for new gallery					
					String titlePrefix; 
					if (picAnno.isTriggerAnnotation()) {
						titlePrefix = picAnno.getParentMarkAnno().getTitle(); 
					} else {
						titlePrefix = picAnno.getTitle();
					}
					
					BeanNameGenerator bng = new BeanNameGenerator("", newGallery, galleries, titlePrefix);					
					String newName = bng.generateAuto();
					newGallery.setTitle(newName);
				}
				
				// Set gallery and update references before modifying the media list to avoid 
				// deletion events being sent to open editors.
				picAnno.setPictureGallery(newGallery);
				if (replacementContent == null) {
					updatePictureGalleryReferences((PictureGallery)oldContent,
							newGallery);
				} else {
					updatePictureGalleryReferences((PictureGallery)replacementContent,
							newGallery);
					
				}
					
				if (addToRepository) {	
					project.getMediaObjects().add(newGallery);
				} else if (newContent != oldContent) {
					if (replacementContent == null) {
						project.getMediaObjects().remove(oldContent);
						project.getMediaObjects().add(newContent);
					}
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
			RichText newRichtext = (RichText)newContent;
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
			
			// Set richtext and update references before modifying the media list to avoid 
			// deletion events being sent to open editors.
			richtextAnnotation.setRichtext(newRichtext);
			updateRichtextReferences((RichText)oldContent, newRichtext);
				
			if (addToRepository) {	
				project.getMediaObjects().add(newRichtext);
				project.getUnusedFiles().remove(newRichtext.getFile().getValue());			
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
				PictureGallery oldGallery = (PictureGallery) oldContent;
				PictureGallery newGallery = (PictureGallery) newContent;
				Project project = Application.getCurrentProject();
					
				// Set gallery before modifying the media list to avoid deletion events
				// being sent to open editors.
				((NodeAnnotationPicture) target).setPictureGallery(oldGallery);
				if (replacementContent == null) {
					updatePictureGalleryReferences(newGallery, oldGallery);
				} else {
					updatePictureGalleryReferences(newGallery, 
							(PictureGallery) replacementContent);
				}
				
				if (addToRepository) {
					project.getMediaObjects().remove(newContent);
					project.getUnusedFiles().add(((RichText)newContent).getFile().getValue());
				} else if (oldContent != newContent) {
					if (replacementContent == null) {
						project.getMediaObjects().remove(newContent);
						project.getMediaObjects().add(oldContent);
					}
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
			RichText oldRichtext = (RichText)oldContent;
			Project project = Application.getCurrentProject();
			
			// Set richtext before modifying the media list to avoid deletion events
			// being sent to open editors.
			((NodeAnnotationRichtext) target).setRichtext(oldRichtext);
			updateRichtextReferences((RichText)newContent, oldRichtext);
			
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
	
	/**
	 * Update all richtext annotations referring to <code>oldRef</code> by setting
	 * <code>newRef</code> as the new richtext.  
	 * @param oldRef richtext reference which should be substituted 
	 * @param nweRef richtext reference after the update
	 */
	private void updateRichtextReferences(RichText oldRef, RichText newRef) {
		Graph scenegraph = Application.getCurrentProject().getSceneGraph();
		List<IGraphNode> contentAnnotations = 
				scenegraph.searchNodes(INodeAnnotationLeaf.class);
		contentAnnotations.addAll(Application.getCurrentProject().getGlobalAnnotations());
		for (IGraphNode node : contentAnnotations) {
			if (node instanceof NodeAnnotationRichtext) {
				NodeAnnotationRichtext anno = (NodeAnnotationRichtext) node;
				if (anno.getRichtext() == oldRef) {
					anno.setRichtext(newRef);
				}
			}
		}
	}
	
	/**
	 * Update all picture gallery annotations referring to <code>oldRef</code> by 
	 * setting <code>newRef</code> as the new picture gallery.  
	 * @param oldRef picture gallery reference which should be substituted 
	 * @param nweRef picture gallery reference after the update
	 */
	private void updatePictureGalleryReferences(PictureGallery oldRef, 
			PictureGallery newRef) {
		Graph scenegraph = Application.getCurrentProject().getSceneGraph();
		List<IGraphNode> contentAnnotations = 
				scenegraph.searchNodes(INodeAnnotationLeaf.class);
		contentAnnotations.addAll(Application.getCurrentProject().getGlobalAnnotations());
		for (IGraphNode node : contentAnnotations) {
			if (node instanceof NodeAnnotationPicture) {
				NodeAnnotationPicture anno = (NodeAnnotationPicture) node;
				if (anno.getPictureGallery() == oldRef) {
					anno.setPictureGallery(newRef);
				}
			}
		}
	}
}

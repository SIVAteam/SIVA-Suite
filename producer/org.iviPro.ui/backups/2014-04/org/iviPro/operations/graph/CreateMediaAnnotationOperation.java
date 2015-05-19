package org.iviPro.operations.graph;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.tweaklets.DummyTitlePathUpdater;
import org.iviPro.application.Application;
import org.iviPro.editors.common.BeanNameGenerator;
import org.iviPro.model.Audio;
import org.iviPro.model.AudioPart;
import org.iviPro.model.BeanList;
import org.iviPro.model.DummyFile;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Picture;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.Project;
import org.iviPro.model.RichText;
import org.iviPro.model.Scene;
import org.iviPro.model.Subtitle;
import org.iviPro.model.Video;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IConnection;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Erstellen von Medien-Annotationen auf einem Szenen-Knoten.
 * 
 * @author dellwo
 * 
 */
public class CreateMediaAnnotationOperation extends IAbstractOperation {

	/** Der Szenen-Knoten auf dem die Annotation erstellt werden soll. */
	private NodeScene sceneNode;

	/** Das Medien-Objekt das als Annotation verwendet werden soll. */
	private IAbstractBean mediaObject;

	/** Die erstellte Annotation */
	private INodeAnnotationLeaf annotation;

	/**
	 * Erstellt eine neue Operation zum Anlegen einer Medien-Annotation auf
	 * einem Szenenknoten.
	 * 
	 * @param sceneNode
	 *            Der Szenen-Knoten.
	 * @param mediaObject
	 *            Das Medien-Objekt fuer die Annotation.
	 * @throws IllegalArgumentException
	 *             Falls Szenen-Knoten oder Medien-Objekt null ist.
	 */
	public CreateMediaAnnotationOperation(NodeScene sceneNode,
			IAbstractBean mediaObject) throws IllegalArgumentException {
		super(Messages.CreateMediaAnnotationOperation_UndoRedoLabel);
		if (sceneNode == null || mediaObject == null) {
			throw new IllegalArgumentException(
					"Neither of the parameters may be null."); //$NON-NLS-1$
		}
		this.sceneNode = sceneNode;
		this.mediaObject = mediaObject;
	}

	@Override
	public boolean canExecute() {
		return sceneNode != null && mediaObject != null;
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.CreateMediaAnnotationOperation_ErrorMsg
				+ e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		// Annotation dem Szenen-Knoten hinzufuegen.
		Project project = Application.getCurrentProject();
		Graph graph = sceneNode.getGraph();
		annotation = createAnnotationNode(sceneNode, mediaObject);
		if (annotation != null) {
			graph.addNode(annotation);
			IConnection conn = IConnection.createConnection(sceneNode, annotation,
				project);
			graph.addConnection(conn);
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Graph graph = sceneNode.getGraph();
		if (annotation != null) {
			graph.removeNode(annotation);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Erstellt zu einem Szenen-Knoten und einem Mediaobjekt eine passende
	 * Annotation.
	 * 
	 * @param nodeScene
	 * @param mediaObject
	 * @return
	 */
	private INodeAnnotationLeaf createAnnotationNode(NodeScene nodeScene,
			IAbstractBean mediaObject) {
		// Titel der Annotation aus Namen des Szenen-Knotens und des
		// Medienobjekts erstellen
		Project project = Application.getCurrentProject();
		String annoTitle = "";			
		// Je nach Medien-Typ eine bestimmte Annotation erstellen.
		INodeAnnotationLeaf anno;
		if (mediaObject instanceof Audio) {
			anno = new NodeAnnotationAudio(annoTitle, project);
			((NodeAnnotationAudio) anno).setAudio((Audio) mediaObject);
		} else if (mediaObject instanceof AudioPart) {
			anno = new NodeAnnotationAudio(annoTitle, project);
			((NodeAnnotationAudio) anno).setAudioPart((AudioPart) mediaObject);
		} else if (mediaObject instanceof Video) {
			anno = new NodeAnnotationVideo(annoTitle, project);
			((NodeAnnotationVideo) anno).setVideo((Video) mediaObject);
		} else if (mediaObject instanceof RichText) {
			anno = new NodeAnnotationRichtext(annoTitle, project);
			((NodeAnnotationRichtext) anno).setRichtext((RichText) mediaObject);
		} else if (mediaObject instanceof Picture) {
			anno = new NodeAnnotationPicture(annoTitle, project);
			((NodeAnnotationPicture) anno).setPicture((Picture) mediaObject);
		} else if (mediaObject instanceof PictureGallery) {
			anno = new NodeAnnotationPicture(annoTitle, project);
			((NodeAnnotationPicture) anno).setPictureGallery(((PictureGallery) mediaObject));
		} else if (mediaObject instanceof Subtitle) {
			anno = new NodeAnnotationSubtitle(annoTitle, project);
			((NodeAnnotationSubtitle) anno).setSubtitle((Subtitle) mediaObject);
		} else {
			throw new IllegalArgumentException("Unknown mediaobject type."); //$NON-NLS-1$
		}
		List<INodeAnnotation> annos = nodeScene.getAnnotations();
		BeanList<INodeAnnotation> annoList = new BeanList<INodeAnnotation>(Application.getCurrentProject());
		annoList.addAll(annos);
		BeanNameGenerator nameGen = new BeanNameGenerator(annoTitle, anno, annoList, Messages.AnnotationPrefix);
		String newTitle = nameGen.generate();
		if (!nameGen.getCancelState()) {
			anno.setTitle(newTitle);
			// Annotation ueber ganze Szene erstrecken lassen.
			Scene scene = nodeScene.getScene();
			anno.setStart(scene.getStart());
			anno.setEnd(scene.getEnd());
			anno.setScreenArea(ScreenArea.LEFT);
			return anno;
		}
		return null;
	}

}
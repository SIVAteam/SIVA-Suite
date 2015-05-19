package org.iviPro.model.graph;

import java.util.ArrayList;
import java.util.List;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.resources.IResource;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Video;

/**
 * Annotationsknoten fuer Video-Annotationen.
 * 
 * @author dellwo
 */
public class NodeAnnotationVideo extends INodeAnnotationLeaf {

	/**
	 * Das Video dem die Annotation zu Grunde liegt.
	 * 
	 * @uml.property name="video"
	 */
	private Video video;
	
	/**
	 * Die Szene dem die Annotation zu Grunde liegt.
	 * 
	 * @uml.property name="scene"
	 */
	private Scene scene;	
	
	/**
	 * Der Name des "mute"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_SETMUTE = "mute"; //$NON-NLS-1$
	
	/**
	 * der Content-Typ, Video bzw. Szene 
	 */
	private int contentType = CONTENT_NONE;
	
	public static final int CONTENT_NONE = -1;
	public static final int CONTENT_VIDEO = 0;
	public static final int CONTENT_SCENE = 1;

	/**
	 * Erstellt eine neue Video-Annotation
	 * 
	 * @param title
	 *            Der Titel der Annotation.
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	public NodeAnnotationVideo(LocalizedString title, Project project) {
		super(title, project);
	}

	/**
	 * Erstellt eine neue Video-Annotation
	 * 
	 * @param title
	 *            Der Titel der Annotation.
	 * @param project
	 *            Das zugehoerige Projekt.
	 */
	public NodeAnnotationVideo(String title, Project project) {
		super(title, project);
	}
	
	/**
	 * Gibt das mit der Annotation verknuepfte Video zurueck.
	 * 
	 * @return Mit der Annotation verknuepftes Video.
	 * @uml.property name="video"
	 */
	public Video getVideo() {
		return video;
	}
	
	/**
	 * Gibt die mit der Annotation verknuepfte Szene zurueck.
	 * 
	 * @return Mit der Annotation verknuepfte Szene.
	 * @uml.property name="scene"
	 */
	public Scene getScene() {
		return scene;
	}
	
	public int getContentType() {
		return this.contentType;
	}

	/**
	 * Setzt das mit der Annotation verknuepfte Video.
	 * 
	 * @param video
	 *            Mit der Annotaiton verknuepftes Video.
	 * @uml.property name="video"
	 */
	public void setVideo(Video video) {
		this.video = video;
		this.scene = null;
		this.contentType = CONTENT_VIDEO;
		firePropertyChange(PROP_SETCONTENT, null, video);
	}
	
	/**
	 * Setzt die mit der Annotation verknuepfte Szene.
	 * 
	 * @param scene
	 *            Mit der Annotaiton verknuepfte Szene.
	 * @uml.property name="scene"
	 */
	public void setScene(Scene scene) {
		this.scene = scene;
		this.video = null;
		this.contentType = CONTENT_SCENE;
		firePropertyChange(PROP_SETCONTENT, null, scene);
	}

	@Override
	public List<IResource> getResources() {
		ArrayList<IResource> resources = new ArrayList<IResource>(1);
		if (contentType == CONTENT_VIDEO && video != null) {
			resources.add(video);
		} else if (contentType == CONTENT_SCENE && scene != null) {
			resources.add(scene);
		}
		return resources;
	}
	
	@Override
	public boolean isDependentOn(IAbstractBean object) {
		// Video-Annotation ist abhaengig von ihrem Video
		return object != null && (object == video || object == scene);
	}
}

/**
 * 
 */
package org.iviPro.model.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.eclipse.draw2d.geometry.Point;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.annotation.OverlayPathItem;
import org.iviPro.model.resources.IResource;

/**
 * @author dellwo
 * @uml.dependency supplier="org.iviPro.model.graph.OverlayPathItem"
 */
public abstract class INodeAnnotation extends IGraphNode {

	/**
	 * Absolute start time of annotation in nanoseconds
	 * @uml.property name="start"
	 */
	private Long start;

	/**
	 * Absolute end time of annotation in nanoseconds
	 * @uml.property name="end"
	 */
	private Long end;

	/**
	 * @uml.property name="pauseVideo"
	 */
	private boolean pauseVideo = false;
	
	private boolean disableable = true;
	
	/**
	 * @uml.property name="muteVideo"
	 */
	private boolean muteVideo = false;

	/**
	 * Der Name des "time"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_TIME = "time"; //$NON-NLS-1$	
	
	/**
	 * Der Name des "pause"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_PAUSE = "pause"; //$NON-NLS-1$
	
	/**
	 * Der Name des "disableable"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_DISABLEABLE ="disableable"; //$NON-NLS-1$
	
	/**
	 * Der Name des "pause"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_ADDITEM = "addopitem"; //$NON-NLS-1$
	
	/**
	 * Der Name des "pause"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_SETCONTENT = "addcontent"; //$NON-NLS-1$

	/**
	 * Der Name des "pause"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_SETSA = "setscreen"; //$NON-NLS-1$

	/**
	 * Der Name des "mute"-Properties, so wie es in PropertyChangeEvents
	 * verwendet wird.
	 */
	public static final String PROP_SETMUTE = "mute"; //$NON-NLS-1$
	
	/**
	 * @uml.property name="overlayPath" readOnly="true"
	 */
	private List<OverlayPathItem> overlayPath;

	/**
	 * @uml.property name="screenArea"
	 */
	private ScreenArea screenArea;

	/**
	 * 
	 * @param graphPosition
	 */
	public INodeAnnotation(LocalizedString title, Project project,
			ConnectionConstraints connectionConstraints) {
		// TODO: Soll INodeAnnotation einen korrenten Wert erhalten?
		super(title, new Point(-1, -1), project, connectionConstraints);
		overlayPath = new LinkedList<OverlayPathItem>();
	}

	/**
	 * 
	 * @param graphPosition
	 */
	public INodeAnnotation(String title, Project project,
			ConnectionConstraints connectionConstraints) {
		// TODO: Soll INodeAnnotation einen korrenten Wert erhalten?
		super(title, new Point(-1, -1), project, connectionConstraints);
		overlayPath = new LinkedList<OverlayPathItem>();
	}

	/**
	 * Getter of the property <tt>start</tt>
	 * 
	 * @return Returns the start.
	 * @uml.property name="start"
	 */
	public Long getStart() {
		return start;
	}

	/**
	 * Gibt die Start-Zeit der Annotation relativ zum Beginn der zugehoerigen
	 * Szenen zurueck.
	 * 
	 * @return
	 */
	public Long getStartRelativeToScene() {
		NodeScene nodeScene = getParentScene();
		if (nodeScene != null) {
			if (nodeScene.getScene() != null
					&& nodeScene.getScene().getStart() != null) {
				return getStart() - nodeScene.getScene().getStart();
			}
		}
		return null;
	}

	/**
	 * Gibt die End-Zeit der Annotation relativ zum Beginn der zugehoerigen
	 * Szenen zurueck.
	 * 
	 * @return
	 */
	public Long getEndRelativeToScene() {
		NodeScene nodeScene = getParentScene();
		if (nodeScene != null) {
			if (nodeScene.getScene() != null
					&& nodeScene.getScene().getEnd() != null) {
				return getEnd() - nodeScene.getScene().getEnd();
			}
		}
		return null;
	}

	/**
	 * Setter of the property <tt>start</tt>
	 * 
	 * @param start
	 *            The start to set.
	 * @uml.property name="start"
	 */
	public void setStart(Long nstart) {
		Long oldVal = null;
		if (start != null) {
			oldVal = new Long(start);
		}
		this.start = nstart;
		firePropertyChange(PROP_TIME, oldVal, start);
	}

	/**
	 * Getter of the property <tt>end</tt>
	 * 
	 * @return Returns the end.
	 * @uml.property name="end"
	 */
	public Long getEnd() {
		return end;
	}

	/**
	 * Setter of the property <tt>end</tt>
	 * 
	 * @param end
	 *            The end to set.
	 * @uml.property name="end"
	 */
	public void setEnd(Long nend) {
		Long oldVal = null;
		if (end != null) {
			oldVal = new Long(end);
		}
		this.end = nend;
		firePropertyChange(PROP_TIME, oldVal, end);
	}

	/**
	 * Getter of the property <tt>pauseVideo</tt>
	 * 
	 * @return Returns the pauseVideo.
	 * @uml.property name="pauseVideo"
	 */
	public boolean isPauseVideo() {
		return pauseVideo;
	}
	
	/**
	 * Getter of the property <tt>disableable</tt>
	 * 
	 * @return Returns the disableable.
	 * @uml.property name="disableable"
	 */
	public boolean isDisableable() {
		return disableable;
	}

	/**
	 * Setter of the property <tt>pauseVideo</tt>
	 * 
	 * @param pauseVideo
	 *            The pauseVideo to set.
	 * @uml.property name="pauseVideo"
	 */
	public void setPauseVideo(boolean pauseVideo) {
		boolean oldVal = this.pauseVideo;
		this.pauseVideo = pauseVideo;
		firePropertyChange(PROP_PAUSE, oldVal, pauseVideo);
	}
	
	/**
	 * Setter of the property <tt>disableable</tt>
	 * 
	 * @param disableable
	 *            The disableable to set.
	 * @uml.property name="disableable"
	 */
	public void setDisableable(boolean disableable) {
		boolean oldVal = this.disableable;
		this.disableable = disableable;
		firePropertyChange(PROP_DISABLEABLE, oldVal, disableable);
	}
	
	public boolean isMuteVideo() {
		return muteVideo;
	}

	public void setMuteVideo(boolean muteVideo) {
		boolean oldVal = this.muteVideo;
		this.muteVideo = muteVideo;
		firePropertyChange(PROP_SETMUTE, oldVal, muteVideo);
	}	

	/**
	 * Getter of the property <tt>overlayPath</tt>
	 * 
	 * @return Returns the overlayPath.
	 * @uml.property name="overlayPath"
	 */
	public List<OverlayPathItem> getOverlayPath() {
		return overlayPath;
	}

	public void addOverlayPathItem(OverlayPathItem item) {
		overlayPath.add(item);
		firePropertyChange(PROP_ADDITEM, null, item);
	}

	public void removeOverlayPathItem(OverlayPathItem item) {
		overlayPath.remove(item);
	}

	/**
	 * Getter of the property <tt>displayPosition</tt>
	 * 
	 * @return Returns the displayPosition.
	 * @uml.property name="screenArea"
	 */
	public ScreenArea getScreenArea() {
		return screenArea;
	}

	/**
	 * Setter of the property <tt>displayPosition</tt>
	 * 
	 * @param displayPosition
	 *            The displayPosition to set.
	 * @uml.property name="screenArea"
	 */
	public void setScreenArea(ScreenArea screenArea) {
		ScreenArea oldArea = this.screenArea;
		this.screenArea = screenArea;
		firePropertyChange(PROP_SETSA, oldArea, screenArea);
	}

	/**
	 * Gibt den Szenen-Knoten zurueck, der dieser Annotation zugeordnet ist.
	 * 
	 * @return
	 */
	public NodeScene getParentScene() {
		Collection<IGraphNode> directParents = getParents();
		Queue<IGraphNode> workingQueue = new LinkedList<IGraphNode>();
		workingQueue.addAll(directParents);

		HashSet<IGraphNode> visited = new HashSet<IGraphNode>();

		while (!workingQueue.isEmpty()) {
			IGraphNode current = workingQueue.poll();
			// Nur unbesuchte Knoten bearbeiten
			if (!visited.contains(current)) {
				visited.add(current);
				if (current instanceof NodeScene) {
					// Knoten ist ein Szenen-Knoten => Hurra
					return (NodeScene) current;
				//TODO Das sieht nach nem Bug aus ... der elseif fall wird nie erreicht!
				} else if (!visited.contains(current)) {
					// Kein NodeScene => In Arbeitsschlange einfuegen
					workingQueue.addAll(current.getParents());
				}
			}
		}
		// Kein NodeScene als Vorgaenger gefunden...
		return null;

	}
	
	public void adjustTimeToScene(NodeScene nodeScene) {
		setStart(nodeScene.getScene().getStart());
		setEnd(nodeScene.getScene().getEnd());
	}
}

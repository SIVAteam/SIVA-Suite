package org.iviPro.editors.scenegraph.actions;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.iviPro.application.Application;
import org.iviPro.editors.scenegraph.editparts.IEditPartNode;
import org.iviPro.editors.scenegraph.layout.ElementChangeReport;
import org.iviPro.editors.scenegraph.layout.LayoutManager;
import org.iviPro.editors.scenegraph.SceneGraphEditor;
import org.eclipse.gef.editparts.*;
import org.iviPro.model.ProjectSettings;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.theme.Icons;

/**
 * Action zum semantischen Zoomen.
 * 
 * @author grillc
 * 
 */
public class SemanticZoomOutAction extends SelectionAction {

	public static final String ID = SemanticZoomOutAction.class.getName();
	List<IEditPartNode> selectedZoomableObjects;
	List<ElementChangeReport> editPartSizeChangedList = new LinkedList<ElementChangeReport>();

	public SemanticZoomOutAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		setText("Semantic Zoom Out"); //$NON-NLS-1$
		setToolTipText(Messages.SemanticZoomOutAction_SemanticZoomOut);
		setImageDescriptor(Icons.GRAPH_CONTEXTMENU_SEMANTICZOOMOUT.getImageDescriptor());
		this.addPropertyChangeListener(new LayoutManager());
	}

	@Override
	public void run() {
		int currentNodeZoomLevel;
		int newNodeZoomLevel;
		boolean zoomLvlChanged = false;
		editPartSizeChangedList = new LinkedList<ElementChangeReport>();

		try {
			selectedZoomableObjects = calculateObjectsToZoom();
			ProjectSettings ps = Application.getCurrentProject().getSettings();
			
			//Bei selektierten Objekt und "Volle Zoomstufen" aus
			if(selectedZoomableObjects.size() != 0 && !ps.isFullSemanticZoomLevels()) {
				//Ändere Zoomlevel
				for(int i=0; i < selectedZoomableObjects.size(); i++) {
					IEditPartNode editPartNode = (IEditPartNode)selectedZoomableObjects.get(i);
					IGraphNode igraphNode = (IGraphNode)editPartNode.getModel();
					
					//hole aktuelle größe
					int oldWidth = editPartNode.getFigure().getBounds().width;
					int oldHeight = editPartNode.getFigure().getBounds().height;

					currentNodeZoomLevel = igraphNode.getSemZoomlevel();
					if(currentNodeZoomLevel <= 1) {
						newNodeZoomLevel = 1;
					} else {	
						newNodeZoomLevel = currentNodeZoomLevel - 1;
						zoomLvlChanged = true;
					}

					igraphNode.setSemZoomlevel(newNodeZoomLevel);
					editPartNode.refresh();
					
					//hole neue größe
					int newWidth = editPartNode.getFigure().getBounds().width;
					int newHeight = editPartNode.getFigure().getBounds().height;

					//create changereport for layoutmanager
					ElementChangeReport ecp = new ElementChangeReport(editPartNode,newWidth-oldWidth,newHeight-oldHeight);
					editPartSizeChangedList.add(ecp);
				}
				// Ohne selektiertes Objekt -> Ändere Zoomstufe aller Objekte
			} else {
				ScalableFreeformRootEditPart rootEditPart = ((SceneGraphEditor)getWorkbenchPart()).getRootEditPart();
				AbstractGraphicalEditPart editPartGraph = (AbstractGraphicalEditPart)rootEditPart.getChildren().get(0);

				//Ändere Zoomlevel
				for(int i=0; i < editPartGraph.getChildren().size(); i++) {
					IEditPartNode editPartNode = (IEditPartNode)editPartGraph.getChildren().get(i);
					IGraphNode igraphNode = (IGraphNode)editPartNode.getModel();
					
					//hole aktuelle größe
					int oldWidth = editPartNode.getFigure().getBounds().width;
					int oldHeight = editPartNode.getFigure().getBounds().height;

					currentNodeZoomLevel = igraphNode.getSemZoomlevel();
					if(currentNodeZoomLevel <= 1) {
						newNodeZoomLevel = 1;
					} else {	
						newNodeZoomLevel = currentNodeZoomLevel - 1;
						zoomLvlChanged = true;
					}

					igraphNode.setSemZoomlevel(newNodeZoomLevel);
					editPartNode.refresh();
					
					//hole neue größe
					int newWidth = editPartNode.getFigure().getBounds().width;
					int newHeight = editPartNode.getFigure().getBounds().height;

					//create changereport for layoutmanager
					ElementChangeReport ecp = new ElementChangeReport(editPartNode,newWidth-oldWidth,newHeight-oldHeight);
					editPartSizeChangedList.add(ecp);
				}
			}
			
			//relocate der figures über feuern eines events
			if(zoomLvlChanged) {		
				this.firePropChange(LayoutManager.PROP_SEMANTIC_ZOOM_OUT, null, editPartSizeChangedList);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean calculateEnabled() {
		selectedZoomableObjects = calculateObjectsToZoom();
		return true;
	}


	/**
	 * Gibt das Objekt zurueck, welches umbenannt werden soll. Dies wird anhand
	 * der aktuell selektierten Edit-Parts ermittelt. Nur wenn genau ein
	 * umbennenbarer Edit-Part selektiert wurde, wird das damit verknuepfte
	 * Model-Objekt zurueck gegeben.
	 * 
	 * @return Das umzubenennden Objekt oder null, falls keiner gemaess den
	 *         obigen Vorgaben gefunden wurde.
	 */
	private List<IEditPartNode> calculateObjectsToZoom() {
		List<?> selectedObjects = getSelectedObjects();
		// Suche zoombare EditParts in den selektierten Objekten.
		List<IEditPartNode> zoomableObjects = new LinkedList<IEditPartNode>();
		for (Object selectedObject : selectedObjects) {
			if (selectedObject instanceof IEditPartNode) {
				IEditPartNode editPart = (IEditPartNode) selectedObject;
				zoomableObjects.add(editPart);
			}
		}

		return zoomableObjects;
	}

	/**
	 * Dieses Objekt wird benutzt um die PropertyChangeListener zu verwalten und
	 * sie ueber Aenderungen zu informieren. Es wird erst instantiiert wenn sich
	 * zum ersten Mal ein Listener bei dieser Klasse registriert.
	 * 
	 * @uml.property name="changeSupport"
	 */
	private PropertyChangeSupport changeSupport = null;

	/**
	 * Fuegt einen neuen PropertyChangeListener zu dieser Klasse hinzu. Der
	 * PropertyChangeListener wird ueber Aenderungen der Klasse informiert.
	 * 
	 * @param listener
	 *            Der neue Listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		// Falls noch kein Listener registriert ist bei dieser Klasse erstellen
		// wir zuerst den PropertyChangeSupport der die Listener verwaltet.
		if (changeSupport == null) {
			changeSupport = new PropertyChangeSupport(this);
		}
		// Listener hinzufuegen
		changeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Entfernt einen PropertyChangeListener wieder von dieser Klasse.
	 * 
	 * @param listener
	 *            Der Listener der getrennt werden soll.
	 */
	public synchronized void removePropertyChangeListener(
			PropertyChangeListener listener) {
		// Entferne den Listener, falls ueberhaupt einer registriert ist
		if (changeSupport != null) {
			changeSupport.removePropertyChangeListener(listener);
		}
	}

	/**
	 * Feuert einen PropertyChangeEvent an alle registrierten Listener.
	 * 
	 * @param propName
	 *            Der Name des Properties die sich geaendert hat.
	 * @param oldValue
	 *            Der alte Wert des Properties.
	 * @param newValue
	 *            Der neue Wert des Properties.
	 */
	protected void firePropChange(String propName, Object oldValue,
			Object newValue) {
		if (changeSupport != null) {
			changeSupport.firePropertyChange(propName, oldValue, newValue);	
		}
	}
}

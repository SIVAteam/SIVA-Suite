package org.iviPro.editors.scenegraph.layout;

import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.iviPro.editors.scenegraph.SceneGraphEditor;
import org.iviPro.editors.scenegraph.editparts.IEditPartNode;
import org.iviPro.model.graph.IGraphNode;

/**
 * Alternativer Algorithmus zum Graphlayouting
 * -> Verschiebe Knoten unter dem erweiterten Knoten um die gleiche Distanz
 * nach unten, Elemente unterhalb-neben dem erweiterten Knoten um die halbe Distanz
 * 
 * @author grillc
 *
 */
public class fanaYAlgorithm implements Algorithm {

	//Privates Klassenattribut, einzige Instanz der Klasse wird erzeugt.
	private static final fanaYAlgorithm INSTANCE = new fanaYAlgorithm();

	//Konstruktor ist privat, darf nicht von außen instanziiert werden.
	private fanaYAlgorithm() {	
	}

	//Statische Methode „getInstance()“ liefert die einzige Instanz der Klasse zurück.
	public static fanaYAlgorithm getInstance() { 
		return INSTANCE;
	}
	
	private static int MAX_DISTANCE = 30;

	@Override
	public void expandNodes(List<ElementChangeReport> list) {
		IWorkbenchPart workbenchpart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ScalableFreeformRootEditPart rootEditPart = ((SceneGraphEditor) workbenchpart).getRootEditPart();
		AbstractGraphicalEditPart editPartGraph = (AbstractGraphicalEditPart)rootEditPart.getChildren().get(0);

		if(editPartGraph != null) {

			Collections.sort(list);

			//Plaziere Knoten neu
			for(int i=0; i < list.size(); i++) {
				//um wieviel wird der knoten größer/kleiner?
				int difference = 0;
				difference = list.get(i).getYchange();
				IEditPartNode affectedNode = list.get(i).getEditpart();


				move(affectedNode, difference, editPartGraph);
				
				
			}
		}
	}

	@Override
	public void minimizeNodes(List<ElementChangeReport> list) {

		IWorkbenchPart workbenchpart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ScalableFreeformRootEditPart rootEditPart = ((SceneGraphEditor) workbenchpart).getRootEditPart();
		AbstractGraphicalEditPart editPartGraph = (AbstractGraphicalEditPart)rootEditPart.getChildren().get(0);

		if(editPartGraph != null) {
			
			Collections.sort(list);

			//Plaziere Knoten neu
			for(int i=0; i < list.size(); i++) {
				//um wieviel wird der knoten größer/kleiner?
				int difference = 0;
				difference = list.get(i).getYchange();
				IEditPartNode affectedNode = list.get(i).getEditpart();


				//verschiebe die knoten im y-tunnel unterhalb
				IEditPartNode currentNode;
				for(int j=0; j < editPartGraph.getChildren().size(); j++) {
					currentNode = (IEditPartNode)editPartGraph.getChildren().get(j);
					//für alle knoten die unterhalb des betrachteten liegen
					if(currentNode.getFigure().getBounds().y > affectedNode.getFigure().getBounds().y) {
						//wenn eine überschneidung in x-richtung
						if(currentNode.getFigure().getBounds().x <= affectedNode.getFigure().getBounds().x + affectedNode.getFigure().getBounds().width && currentNode.getFigure().getBounds().x + currentNode.getFigure().getBounds().width >= affectedNode.getFigure().getBounds().x) {
							//setzen der figure position
							IGraphNode igraphNode = (IGraphNode)currentNode.getModel();
							Point p = new Point(igraphNode.getPosition().x, currentNode.getFigure().getBounds().y + difference);
							igraphNode.setPosition(p);
						} else {
							int xDistance = xDistance(affectedNode, currentNode);
							if(xDistance >= 0 && xDistance <= MAX_DISTANCE) {
								//setzen der figure position
								IGraphNode igraphNode = (IGraphNode)currentNode.getModel();
								Point p = new Point(igraphNode.getPosition().x, currentNode.getFigure().getBounds().y + Math.round(difference/2));
								igraphNode.setPosition(p);
							}
						}
					}
				}
			}

		}
	}

	@Override
	public void newNode(IGraphNode node) {

	}

	@Override
	public void nodeMoved(IGraphNode node) {

	}

	@Override
	public void nodeResized(List<ElementChangeReport> list) {

		//Hole SceneGraphEditor
		IEditorReference[] editorArray = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		IWorkbenchPart workbenchpart = null;
		for(int i=0; i < editorArray.length; i++) {
			String editorID = editorArray[i].getId();
			if(editorID.equals(SceneGraphEditor.ID)) {
				workbenchpart = editorArray[i].getPart(true);
			}
		}

		//Wenn SceneGraphEditor nicht offen => Kein Relocate
		if(workbenchpart != null) {
			ScalableFreeformRootEditPart rootEditPart = ((SceneGraphEditor) workbenchpart).getRootEditPart();
			AbstractGraphicalEditPart editPartGraph = (AbstractGraphicalEditPart)rootEditPart.getChildren().get(0);

			if(editPartGraph != null) {
				
				Collections.sort(list);

				//Plaziere Knoten neu
				for(int i=0; i < list.size(); i++) {
					//um wieviel wird der knoten größer/kleiner?
					int difference = 0;
					difference = list.get(i).getYchange();
					IEditPartNode affectedNode = list.get(i).getEditpart();


					//verschiebe die knoten im y-tunnel unterhalb
					IEditPartNode currentNode;
					for(int j=0; j < editPartGraph.getChildren().size(); j++) {
						currentNode = (IEditPartNode)editPartGraph.getChildren().get(j);
						//für alle knoten die unterhalb des betrachteten liegen
						if(currentNode.getFigure().getBounds().y > affectedNode.getFigure().getBounds().y) {
							//wenn eine überschneidung in x-richtung
							if(currentNode.getFigure().getBounds().x <= affectedNode.getFigure().getBounds().x + affectedNode.getFigure().getBounds().width && currentNode.getFigure().getBounds().x + currentNode.getFigure().getBounds().width >= affectedNode.getFigure().getBounds().x) {
								//setzen der figure position
								IGraphNode igraphNode = (IGraphNode)currentNode.getModel();
								Point p = new Point(igraphNode.getPosition().x, currentNode.getFigure().getBounds().y + difference);
								igraphNode.setPosition(p);
							} else {
								int xDistance = xDistance(affectedNode, currentNode);
								if(xDistance >= 0 && xDistance <= MAX_DISTANCE) {
									//setzen der figure position
									IGraphNode igraphNode = (IGraphNode)currentNode.getModel();
									Point p = new Point(igraphNode.getPosition().x, currentNode.getFigure().getBounds().y + Math.round(difference/2));
									igraphNode.setPosition(p);
								}
							}
						}
					}
				}
			}


		}

	}

	//Berechne den X-Abstand zw. 2 Knoten
	private int xDistance(IEditPartNode affectedNode, IEditPartNode currentNode) {
		if(affectedNode.getFigure().getBounds().x > currentNode.getFigure().getBounds().x) {
			return affectedNode.getFigure().getBounds().x - (currentNode.getFigure().getBounds().x + currentNode.getFigure().getBounds().width);
		}
		else if(affectedNode.getFigure().getBounds().x < currentNode.getFigure().getBounds().x) {
			return currentNode.getFigure().getBounds().x - (affectedNode.getFigure().getBounds().x + affectedNode.getFigure().getBounds().width);
		}
		return 0;
	}
	
	
	private void move(IEditPartNode affectedNode, int difference, AbstractGraphicalEditPart editPartGraph) {
		//verschiebe die knoten im y-tunnel unterhalb
		IEditPartNode currentNode;
		for(int j=0; j < editPartGraph.getChildren().size(); j++) {
			currentNode = (IEditPartNode)editPartGraph.getChildren().get(j);
			//für alle knoten die unterhalb des betrachteten liegen
			if(currentNode.getFigure().getBounds().y > affectedNode.getFigure().getBounds().y) {
				//wenn eine überschneidung in x-richtung
				if(currentNode.getFigure().getBounds().x <= affectedNode.getFigure().getBounds().x + affectedNode.getFigure().getBounds().width && currentNode.getFigure().getBounds().x + currentNode.getFigure().getBounds().width >= affectedNode.getFigure().getBounds().x) {
					//setzen der figure position
					IGraphNode igraphNode = (IGraphNode)currentNode.getModel();
					Point p = new Point(igraphNode.getPosition().x, currentNode.getFigure().getBounds().y + difference);
					igraphNode.setPosition(p);
				} else {
					int xDistance = xDistance(affectedNode, currentNode);
					if(xDistance >= 0 && xDistance <= MAX_DISTANCE) {
						//setzen der figure position
						IGraphNode igraphNode = (IGraphNode)currentNode.getModel();
						Point p = new Point(igraphNode.getPosition().x, currentNode.getFigure().getBounds().y + Math.round(difference));
						igraphNode.setPosition(p);
					}
				}
			}
		}
	}
}

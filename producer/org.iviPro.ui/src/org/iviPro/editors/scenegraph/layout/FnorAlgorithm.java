package org.iviPro.editors.scenegraph.layout;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.iviPro.application.Application;
import org.iviPro.editors.scenegraph.SceneGraphEditor;
import org.iviPro.editors.scenegraph.editparts.IEditPartNode;
import org.iviPro.editors.scenegraph.layout.algo.FNOR.QPRectanglePlacement;
import org.iviPro.model.ProjectSettings;
import org.iviPro.model.graph.IGraphNode;

/**
 * Algorithmus zum Fast-Overlap-Removal
 * -> Platziere Knoten möglichst ähnlich dem vorherigen Layout
 * aber ohne Überlappungen
 * @author fana
 *
 */
public class FnorAlgorithm implements Algorithm {
	
	//Mindestabstand der Knoten in Pixel
	private static final int FIGURESOFFSET = 30;
	
	//Privates Klassenattribut, einzige Instanz der Klasse wird erzeugt.
	private static final FnorAlgorithm INSTANCE = new FnorAlgorithm();
	
	//Konstruktor ist privat, darf nicht von außen instanziiert werden.
	private FnorAlgorithm() {	
	}
	
	//Statische Methode „getInstance()“ liefert die einzige Instanz der Klasse zurück.
	public static FnorAlgorithm getInstance() {
		return INSTANCE;
	}

	private static LayoutGraph layoutGraph = new LayoutGraph();

	@Override
	public void expandNodes(List<ElementChangeReport> list) {
		IWorkbenchPart workbenchpart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ScalableFreeformRootEditPart rootEditPart = ((SceneGraphEditor) workbenchpart).getRootEditPart();
		AbstractGraphicalEditPart editPartGraph = (AbstractGraphicalEditPart)rootEditPart.getChildren().get(0);
		
		if(editPartGraph != null) {

			double xgap = FIGURESOFFSET;
			double ygap = FIGURESOFFSET;

			//Baue den LayoutGraph auf
			for(int i=0; i < editPartGraph.getChildren().size(); i++) {
				IEditPartNode editPartNode = (IEditPartNode)editPartGraph.getChildren().get(i);

				Rectangle figureBounds = editPartNode.getFigure().getBounds();
				layoutGraph.addNode("node" + i, figureBounds.x, figureBounds.y, figureBounds.width, figureBounds.height, editPartNode);
			}

			//Plaziere Knoten neu
			QPRectanglePlacement r = new QPRectanglePlacement(xgap,ygap);
			r.place(layoutGraph.getRectangles());

			//setzen der figure position
			for(int i=0; i < layoutGraph.getNodes().size(); i++) {
				LayoutNode n = layoutGraph.getNodes().get("node" + i);
				IEditPartNode editPartNode = n.getEditPartNode();
				IGraphNode igraphNode = (IGraphNode)editPartNode.getModel();

				Point position = new Point(n.getX(), n.getY());
				
				// lvl 1 -> lvl 2
				if(igraphNode.getSemZoomlevel() == 2) {
					//Speichern der Position der vorherigen Stufe
					Point p = new Point();
					p.x = igraphNode.getPosition().x;
					p.y = igraphNode.getPosition().y;
					igraphNode.setPos_semzoomlvl1(p);
					
					//Setzen der neuen Position im Graph
					igraphNode.setPosition(position);
					
					//Speichern der Position der jetzigen Stufe
					p = new Point();
					p.x = position.x;
					p.y = position.y;
					igraphNode.setPos_semzoomlvl2(p);
				}
				// lvl 2 -> lvl 3
				else if(igraphNode.getSemZoomlevel() == 3) {
					//Speichern der Position der vorherigen Stufe
					Point p = new Point();
					p.x = igraphNode.getPosition().x;
					p.y = igraphNode.getPosition().y;
					igraphNode.setPos_semzoomlvl2(p);
					
					//Setzen der neuen Position im Graph
					igraphNode.setPosition(position);
					
					//Speichern der Position der jetzigen Stufe
					p = new Point();
					p.x = position.x;
					p.y = position.y;
					igraphNode.setPos_semzoomlvl3(p);
				}
				//tritt ein wenn nur ein element gezoomt wurde
				//-> anderen Elemente hier ohne Zooming neu setzen
				else {
					//Setzen der neuen Position im Graph
					igraphNode.setPosition(position);
				}
			}
			
			
		}
	}

	@Override
	public void minimizeNodes(List<ElementChangeReport> list) {
		ProjectSettings ps = Application.getCurrentProject().getSettings();

		IWorkbenchPart workbenchpart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ScalableFreeformRootEditPart rootEditPart = ((SceneGraphEditor) workbenchpart).getRootEditPart();
		AbstractGraphicalEditPart editPartGraph = (AbstractGraphicalEditPart)rootEditPart.getChildren().get(0);
		
		if(editPartGraph != null && ps.isFullSemanticZoomEnabled()) {

			//setzen der figure position
			for(int i=0; i < editPartGraph.getChildren().size(); i++) {
				IEditPartNode editPartNode = (IEditPartNode)editPartGraph.getChildren().get(i);
				IGraphNode igraphNode = (IGraphNode)editPartNode.getModel();

				// lvl 3 -> lvl 2
				if(igraphNode.getSemZoomlevel() == 2) {
					//Holen der Position aus der vorherigen Stufe und setzen
					Point p = new Point();
					p.x = igraphNode.getPos_semzoomlvl2().x;
					p.y = igraphNode.getPos_semzoomlvl2().y;
					igraphNode.setPosition(p);
				}
				// lvl 2 -> lvl 1
				if(igraphNode.getSemZoomlevel() == 1) {
					//Holen der Position aus der vorherigen Stufe und setzen
					Point p = new Point();
					p.x = igraphNode.getPos_semzoomlvl1().x;
					p.y = igraphNode.getPos_semzoomlvl1().y;
					igraphNode.setPosition(p);
				}
			}
			
		}
	}

	@Override
	public void newNode(IGraphNode node) {
		Point p = new Point();
		p.x = node.getPosition().x;
		p.y = node.getPosition().y;	

		node.setPos_semzoomlvl1(p);
		node.setPos_semzoomlvl2(p);
		node.setPos_semzoomlvl3(p);
	}

	@Override
	public void nodeMoved(IGraphNode node) {
		Point p = new Point();
		p.x = node.getPosition().x;
		p.y = node.getPosition().y;	

		node.setPos_semzoomlvl1(p);
		node.setPos_semzoomlvl2(p);
		node.setPos_semzoomlvl3(p);
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

			double xgap = FIGURESOFFSET;
			double ygap = FIGURESOFFSET;

			//Baue den LayoutGraph auf
			for(int i=0; i < editPartGraph.getChildren().size(); i++) {
				IEditPartNode editPartNode = (IEditPartNode)editPartGraph.getChildren().get(i);

				Rectangle figureBounds = editPartNode.getFigure().getBounds();
				layoutGraph.addNode("node" + i, figureBounds.x, figureBounds.y, figureBounds.width, figureBounds.height, editPartNode);
			}

			//Plaziere Knoten neu
			QPRectanglePlacement r = new QPRectanglePlacement(xgap,ygap);
			r.place(layoutGraph.getRectangles());

			//setzen der figure position
			for(int i=0; i < layoutGraph.getNodes().size(); i++) {
				LayoutNode n = layoutGraph.getNodes().get("node" + i);
				IEditPartNode editPartNode = n.getEditPartNode();
				IGraphNode igraphNode = (IGraphNode)editPartNode.getModel();

				Point position = new Point(n.getX(), n.getY());
				
				igraphNode.setPosition(position);
				
				//lvl 1
				if(igraphNode.getSemZoomlevel() == 1) {
					igraphNode.setPos_semzoomlvl1(position);
				}
				//lvl 2
				if(igraphNode.getSemZoomlevel() == 2) {
					igraphNode.setPos_semzoomlvl2(position);
				}
				//lvl 3
				if(igraphNode.getSemZoomlevel() == 3) {
					igraphNode.setPos_semzoomlvl3(position);
				}
			}
			
			
		}
		
	}

}

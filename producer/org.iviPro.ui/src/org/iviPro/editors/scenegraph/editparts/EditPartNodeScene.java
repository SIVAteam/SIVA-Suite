package org.iviPro.editors.scenegraph.editparts;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.iviPro.actions.nondestructive.OpenAnnotationEditorAction;
import org.iviPro.application.Application;
import org.iviPro.editors.scenegraph.figures.FigureNodeScene;
import org.iviPro.editors.scenegraph.figures.FigureNodeSceneContents;
import org.iviPro.editors.scenegraph.figures.FigureNodeSemanticAdditionAnnoDetails;
import org.iviPro.editors.scenegraph.figures.FigureNodeSemanticAdditionAnnoOverview;
import org.iviPro.editors.scenegraph.figures.FigureNodeSemanticAdditionFisheye;
import org.iviPro.editors.scenegraph.figures.FigureNodeSemanticAdditionFramepreview;
import org.iviPro.editors.scenegraph.figures.FigureNodeSemanticAdditionSceneInfo;
import org.iviPro.editors.scenegraph.policies.DefaultConnectionPolicy;
import org.iviPro.editors.scenegraph.policies.DefaultEditPolicy;
import org.iviPro.editors.scenegraph.policies.NodeSceneEditPolicy;
import org.iviPro.model.SivaImage;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.theme.Icons;
import org.iviPro.utils.ImageHelper;

public class EditPartNodeScene extends IEditPartNode {

	FigureNodeSemanticAdditionFramepreview semanticAdditionFigureFramepreview;
	FigureNodeSemanticAdditionAnnoOverview semanticAdditionFigureAnnoOverview;
	FigureNodeSemanticAdditionSceneInfo semanticAdditionFigureSceneInfo;
	FigureNodeSemanticAdditionAnnoDetails semanticAdditionFigureAnnoDetails;
	FigureNodeSemanticAdditionFisheye semanticAdditionFigureFisheye;
	FigureNodeScene stdFigure;
	FigureNodeSceneContents figureNodeSceneContents;
	LinkedList<SivaImage> previewFrames;
	Image transitionPreviewFrame;
	List<INodeAnnotation> annotations;

	private boolean semanticFigureLevel2AlreadyAdded = false;
	private boolean semanticFigureLevel3AlreadyAdded = false;
	private boolean semanticFisheye = false;

	//Model des EditParts
	NodeScene nodeScene;

	@Override
	protected DefaultConnectionPolicy getNodeConnectionPolicy() {
		return new DefaultConnectionPolicy();
	}

	@Override
	protected DefaultEditPolicy getNodeEditPolicy() {
		return new NodeSceneEditPolicy(true);
	}

	@Override
	protected IFigure createFigure() {
		nodeScene = getCastedModel();

		/***************************************/
		/*	Inhaltsfigures setzen			   */
		/***************************************/
		int annoCount = nodeScene.getChildren(INodeAnnotationLeaf.class).size();
		stdFigure = new FigureNodeScene(nodeScene.getPosition());
		figureNodeSceneContents = new FigureNodeSceneContents(nodeScene.getPosition(),nodeScene.getTitle(), annoCount);
		if (nodeScene != null) {
			stdFigure.setScene(nodeScene.getScene());
			figureNodeSceneContents.setScene(nodeScene.getScene());
		}

		//Listener für den Annotationsbutton/editor setzen
		figureNodeSceneContents.addAnnotateButtonActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				onAnnotateButtonClick();
			}
		});
		stdFigure.add(figureNodeSceneContents);

		/********************************************/
		/*	Darstellungselemente setzen (Sem. Zoom)	*/
		/********************************************/

		previewFrames = this.getCastedModel().getScene().getImages();
		annotations = getCastedModel().getAnnotations();

		//Setze Zoomlevelpunkte
		figureNodeSceneContents.setZoomlevelPoints(nodeScene.getSemZoomlevel());

		/***************************************/
		/*	SEMANTIC FISHEYE				   */
		/***************************************/
		updateSemanticFisheyeFigure();

		/***************************************/
		/*	SEMANTIC ZOOMLEVEL 1			   */
		/***************************************/
		if(nodeScene.getSemZoomlevel() == 1) {
			createSemZoomlevel1Figures();
		}

		/***************************************/
		/*	SEMANTIC ZOOMLEVEL 2			   */
		/***************************************/
		if(nodeScene.getSemZoomlevel() == 2 && !semanticFigureLevel2AlreadyAdded) {			
			createSemZoomlevel2Figures();
		}
		//Wenn Figure bereits gesetzt: Aktualisiere nur die Annotationen (Overview)
		if(nodeScene.getSemZoomlevel() == 2) {
			semanticAdditionFigureAnnoOverview.setAnnotationTypes(annotations);
		}

		/***************************************/
		/*	SEMANTIC ZOOMLEVEL 3			   */
		/***************************************/
		if(nodeScene.getSemZoomlevel() == 3 && !semanticFigureLevel3AlreadyAdded) {
			createSemZoomlevel2Figures();
			createSemZoomlevel3Figures();
		}
		//Wenn Figure bereits gesetzt: Aktualisiere nur die Annotationen (Detail)
		if(nodeScene.getSemZoomlevel() == 3) {
			semanticAdditionFigureAnnoDetails.setAnnotations(annotations);
			Dimension d = new Dimension(140, semanticAdditionFigureAnnoDetails.getBounds().height + figureNodeSceneContents.getBounds().height + semanticAdditionFigureFramepreview.getBounds().height + semanticAdditionFigureSceneInfo.getBounds().height);
			stdFigure.setSize(d);
		}

		/**********************************************************************/
		/* Listener installieren, der auf Aenderungen in den Projekt-Settings */
		/* horcht. Ist dort eine Aenderung aufgetreten, zeichnet sich der     */
		/* EditPart neu, um eventuellen Aenderungen zu entsprechen.			  */
		/* Zum Beispiel: Änderung des Annotationsgitters			          */
		/**********************************************************************/
		Application.getCurrentProject().getSettings()
		.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				refreshVisuals();
			}
		});



		return stdFigure;
	}

	@Override
	protected void refreshVisuals() {
		//Hole Grundinformationen
		nodeScene = getCastedModel();
		stdFigure = (FigureNodeScene) getFigure();
		previewFrames = this.getCastedModel().getScene().getImages();
		annotations = getCastedModel().getAnnotations();

		//Setze Zoomlevelpunkte
		figureNodeSceneContents.setZoomlevelPoints(nodeScene.getSemZoomlevel());

		/***************************************/
		/*	SEMANTIC ZOOMLEVEL 1			   */
		/***************************************/
		if(nodeScene.getSemZoomlevel() == 1) {
			createSemZoomlevel1Figures();
		}

		/***************************************/
		/*	SEMANTIC ZOOMLEVEL 2			   */
		/***************************************/
		if(nodeScene.getSemZoomlevel() == 2 && !semanticFigureLevel2AlreadyAdded) {			
			createSemZoomlevel2Figures();
		}
		//Wenn Figure bereits gesetzt: Aktualisiere nur die Annotationen (Overview)
		if(nodeScene.getSemZoomlevel() == 2) {
			semanticAdditionFigureAnnoOverview.setAnnotationTypes(annotations);
		}

		/***************************************/
		/*	SEMANTIC ZOOMLEVEL 3			   */
		/***************************************/
		if(nodeScene.getSemZoomlevel() == 3 && !semanticFigureLevel3AlreadyAdded) {
			createSemZoomlevel3Figures();
		}
		//Wenn Figure bereits gesetzt: Aktualisiere nur die Annotationen (Detail)
		if(nodeScene.getSemZoomlevel() == 3) {
			semanticAdditionFigureAnnoDetails.setAnnotations(annotations);
			Dimension d = new Dimension(140, semanticAdditionFigureAnnoDetails.getBounds().height + figureNodeSceneContents.getBounds().height + semanticAdditionFigureFramepreview.getBounds().height + semanticAdditionFigureSceneInfo.getBounds().height);
			stdFigure.setSize(d);
		}

		/***************************************/
		/*	GRUNDLEGENDES					   */
		/***************************************/
		//set #annotations and title
		int numAnnos = nodeScene.getChildren(INodeAnnotationLeaf.class).size();
		numAnnos += nodeScene.getChildren(NodeMark.class).size();
		figureNodeSceneContents.setAnnotationCount(numAnnos);
		figureNodeSceneContents.setText(nodeScene.getTitle());
		// set new location of the figure
		FigureNodeScene figure = (FigureNodeScene) getFigure();
		figure.setLocation(nodeScene.getPosition());
		// notify parent container of changed position & location
		Rectangle bounds = figure.getBounds();
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, figure,
				bounds);
	}

	public FigureNodeScene getCastedFigure() {
		return (FigureNodeScene) getFigure();
	}

	private NodeScene getCastedModel() {
		return (NodeScene) getModel();
	}

	private void onAnnotateButtonClick() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		new OpenAnnotationEditorAction(window, getCastedModel()).run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.editors.scenegraph.gef.parts.IEditPartNode#isRenameable()
	 */
	@Override
	public boolean isRenameable() {
		return true;
	}

	public boolean getSemanticFisheye() {
		return semanticFisheye;
	}

	public void setSemanticFisheye(boolean semanticFisheye) {
		this.semanticFisheye = semanticFisheye;
	}

	public void updateSemanticFisheyeFigure() {
		nodeScene = getCastedModel();
		previewFrames = this.getCastedModel().getScene().getImages();
		annotations = getCastedModel().getAnnotations();

		semanticAdditionFigureFisheye = new FigureNodeSemanticAdditionFisheye();

		if(semanticFisheye) {
			semanticAdditionFigureFisheye.setTitle(nodeScene.getTitle());
			if(previewFrames != null && previewFrames.get(0).getBufferedImage() != null && previewFrames.get(1).getBufferedImage() != null) {
				BufferedImage scaledBufimgStart = ImageHelper.getScaledImage(previewFrames.get(0).getBufferedImage(), new java.awt.Dimension(140,140), true);
				BufferedImage scaledBufimgEnd = ImageHelper.getScaledImage(previewFrames.get(1).getBufferedImage(), new java.awt.Dimension(140,140), true);
				semanticAdditionFigureFisheye.setTransitionPreviewFrame(ImageHelper.transition(scaledBufimgStart, scaledBufimgEnd, 260, semanticAdditionFigureFisheye.LIGHTORANGE));
			} else {
				semanticAdditionFigureFisheye.setTransitionPreviewFrame(Icons.GRAPH_FISHEYE_NOFRAMEPREVIEW.getImage());
			}
			semanticAdditionFigureFisheye.setAnnotations(annotations);
			stdFigure.setToolTip(semanticAdditionFigureFisheye);
		} else {
			stdFigure.setToolTip(null);
		}		
	}

	private void createSemZoomlevel1Figures() {
		if(semanticFigureLevel2AlreadyAdded) {
			stdFigure.remove(semanticAdditionFigureFramepreview);
			stdFigure.remove(semanticAdditionFigureAnnoOverview);
			semanticFigureLevel2AlreadyAdded = false;
		}
		//Groesse der Parent-Figure setzen
		Dimension d = new Dimension(140, 40);
		stdFigure.setSize(d);
	}

	private void createSemZoomlevel2Figures() {
		if(semanticFigureLevel3AlreadyAdded) {
			stdFigure.remove(semanticAdditionFigureSceneInfo);
			stdFigure.remove(semanticAdditionFigureAnnoDetails);
			stdFigure.remove(semanticAdditionFigureFramepreview);
			semanticFigureLevel3AlreadyAdded = false;
		}

		//Positionierung der Framevorschau
		Point pos = new Point(getCastedModel().getPosition().x,getCastedModel().getPosition().y);
		pos.y += figureNodeSceneContents.getBounds().height;
		semanticAdditionFigureFramepreview = new FigureNodeSemanticAdditionFramepreview(pos);

		if(transitionPreviewFrame != null) {
			semanticAdditionFigureFramepreview.setTransitionPreviewFrame(transitionPreviewFrame);
		} else if(previewFrames != null && previewFrames.get(0).getBufferedImage() != null && previewFrames.get(1).getBufferedImage() != null) {
			BufferedImage scaledBufimgStart = ImageHelper.getScaledImage(previewFrames.get(0).getBufferedImage(), new java.awt.Dimension(80,80), true);
			BufferedImage scaledBufimgEnd = ImageHelper.getScaledImage(previewFrames.get(1).getBufferedImage(), new java.awt.Dimension(80,80), true);
			transitionPreviewFrame = ImageHelper.transition(scaledBufimgStart, scaledBufimgEnd, 140, semanticAdditionFigureFramepreview.classColor);
			semanticAdditionFigureFramepreview.setTransitionPreviewFrame(transitionPreviewFrame);
		} else {
			semanticAdditionFigureFramepreview.setTransitionPreviewFrame(Icons.GRAPH_ZOOM_NOFRAMEPREVIEW.getImage());
		}

		stdFigure.add(semanticAdditionFigureFramepreview);

		//Positionierung der Annotationstypen
		pos.y += semanticAdditionFigureFramepreview.getBounds().height;
		semanticAdditionFigureAnnoOverview = new FigureNodeSemanticAdditionAnnoOverview(pos);
		semanticAdditionFigureAnnoOverview.setAnnotationTypes(annotations);

		stdFigure.add(semanticAdditionFigureAnnoOverview);

		semanticFigureLevel2AlreadyAdded = true;

		//Groesse der Parent-Figure setzen
		Dimension d = new Dimension(140, figureNodeSceneContents.getSize().height + semanticAdditionFigureFramepreview.getBounds().height + semanticAdditionFigureAnnoOverview.getBounds().height);
		stdFigure.setSize(d);
	}

	private void createSemZoomlevel3Figures() {
		if(semanticFigureLevel2AlreadyAdded) {
			stdFigure.remove(semanticAdditionFigureAnnoOverview);
			semanticFigureLevel2AlreadyAdded = false;
		}



		//Positionierung für Dauer und Quellvideo
		Point pos = new Point(getCastedModel().getPosition().x,getCastedModel().getPosition().y);
		pos.y += figureNodeSceneContents.getBounds().height + semanticAdditionFigureFramepreview.getBounds().height;
		semanticAdditionFigureSceneInfo = new FigureNodeSemanticAdditionSceneInfo(pos);
		//setze Dauer und Quellvideo
		long duration = getCastedModel().getScene().getEnd() - getCastedModel().getScene().getStart();
		String source = getCastedModel().getScene().getVideo().getTitle();
		semanticAdditionFigureSceneInfo.setDuration(duration);
		semanticAdditionFigureSceneInfo.setSource(source);

		stdFigure.add(semanticAdditionFigureSceneInfo);

		//Positionierung für Annotationen
		pos.y += 45;
		semanticAdditionFigureAnnoDetails = new FigureNodeSemanticAdditionAnnoDetails(pos);
		//setze Annotationen
		semanticAdditionFigureAnnoDetails.setAnnotations(annotations);

		stdFigure.add(semanticAdditionFigureAnnoDetails);

		semanticFigureLevel3AlreadyAdded = true;

		//Groesse der Parent-Figure setzen (Abhaengig von #Annotationen)
		Dimension d = new Dimension(140, semanticAdditionFigureAnnoDetails.getBounds().height + figureNodeSceneContents.getBounds().height + semanticAdditionFigureFramepreview.getBounds().height + semanticAdditionFigureSceneInfo.getBounds().height);
		stdFigure.setSize(d);
	}
}
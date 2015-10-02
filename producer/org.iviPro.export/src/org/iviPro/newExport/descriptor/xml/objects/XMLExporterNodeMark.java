package org.iviPro.newExport.descriptor.xml.objects;

import java.util.ArrayList;
import java.util.Set;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.annotation.EllipseShape;
import org.iviPro.model.annotation.IMarkShape;
import org.iviPro.model.annotation.PolygonShape;
import org.iviPro.model.annotation.PolygonShape.Position;
import org.iviPro.model.annotation.PositionalShape;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.resources.Scene;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.iviPro.newExport.descriptor.xml.IdManager.LabelType;
import org.iviPro.newExport.util.SivaTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLExporterNodeMark extends IXMLExporter {

	private NodeMark nodeMark;
	
	XMLExporterNodeMark(NodeMark exportObj) {
		super(exportObj);
		nodeMark = exportObj;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeMark.class;
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IdManager idManager, Project project,
			Set<Object> alreadyExported) throws ExportException {

		INodeAnnotationLeaf contentAnnotation = 
				(INodeAnnotationLeaf) nodeMark.getTriggerAnnotation();
		Scene scene = nodeMark.getParentScene().getScene();
		long sceneStartTime = 0L;
		if (scene != null) {
			sceneStartTime = scene.getStart();
		}

		// exportiere zuerst die zugehörige normale Annotation
		IXMLExporter exp = ExporterFactory.createExporter(contentAnnotation);
		IXMLExporterNodeAnnotationLeaf leafExp = (IXMLExporterNodeAnnotationLeaf) exp;
		leafExp.exportContentAnno(doc, idManager, project, alreadyExported,
				nodeMark, contentAnnotation);

		// Entsprechenden <trigger> Eintrag in <storyboard>-Liste erstellen
		Element trigger = doc.createElement(TAG_TRIGGER);
		trigger.setAttribute(ATTR_REF_ACTION_ID,
				idManager.getActionID(nodeMark));
		trigger.setAttribute(ATTR_TRIGGER_STARTTIME,
				SivaTime.getSivaXMLTime(nodeMark.getStart()
						- sceneStartTime));
		trigger.setAttribute(ATTR_TRIGGER_ENDTIME,
				SivaTime.getSivaXMLTime(nodeMark.getEnd()
						- sceneStartTime));
		trigger.setAttribute(ATTR_TRIGGER_ID,
				idManager.getTriggerID(nodeMark));

		// Einhängen der Trigger in Storyboard
		Element storyBoard = getStoryboard(doc, nodeMark.getParentScene(),
				idManager);
		storyBoard.appendChild(trigger);

		// Entsprechenden <showMarkControl> Eintrag in <actions>-Liste erstellen
		Element showMarkAction = doc.createElement(TAG_SHOWMARKCONTROL);
		showMarkAction.setAttribute(ATTR_ACTIONID,
				idManager.getActionID(nodeMark));
		showMarkAction.setAttribute(ATTR_REF_ACTION_ID,
				idManager.getActionID(nodeMark.getTriggerAnnotation()));
		showMarkAction.setAttribute(ATTR_STYLE, nodeMark.getStyle());
		showMarkAction.setAttribute(ATTR_DISABLEABLE, 
				Boolean.toString(nodeMark.isDisableable()));
		showMarkAction.setAttribute(ATTR_DURATION,
				SivaTime.getSivaXMLTime(nodeMark.getDuration()));

		// Create shape information
		Element shapeElement = null;
		switch (nodeMark.getType()) {
		case ELLIPSE:
			shapeElement = doc.createElement(TAG_ELLIPSE);
			break;
		case BUTTON:
			/* Create label for button text. Since button text is not localized
			 * yet, a Collection<LocalizedString> with the current project language
			 * needs to be created. */
			LocalizedString localButton = 
				new LocalizedString(nodeMark.getButtonLabel(), project);
			ArrayList<LocalizedString> tmpList = new ArrayList<LocalizedString>(1);
			tmpList.add(localButton);
			String buttonLabelId = createLabel(nodeMark,doc, idManager, tmpList,
					LabelType.BUTTON);
			
			shapeElement = doc.createElement(TAG_BUTTON);
			shapeElement.setAttribute(ATTR_REF_RES_ID,
					buttonLabelId);
			break;
		case POLYGON:
			shapeElement = doc.createElement(TAG_POLYGON);
			break;
		}

		if (shapeElement != null) {
			// exportiere die Positionen, Zeit etc.
			for (IMarkShape shape : nodeMark.getShapes()) {
				if (shape instanceof EllipseShape) {
					shapeElement.appendChild(createEllipsePathElement(doc,
							(EllipseShape) shape, sceneStartTime));
				} else if (shape instanceof PositionalShape) {
					shapeElement.appendChild(createButtonPathElement(doc,
							(PositionalShape) shape, sceneStartTime));
				} else if (shape instanceof PolygonShape) {
					shapeElement.appendChild(createPolygonChainElement(doc,
							(PolygonShape) shape, sceneStartTime));
				}
			}
			showMarkAction.appendChild(shapeElement);
		}

		// hängte showMarkAction als Action ein
		Element actions = getActions(doc);
		actions.appendChild(showMarkAction);
	}

	private Element createButtonPathElement(Document doc,
			PositionalShape shapeButton, long sceneStartTime) {
		Element pathElement = doc.createElement(TAG_BUTTON_PATH);
		float x = shapeButton.getX();
		float y = shapeButton.getY();
		long time = shapeButton.getTime();
		pathElement.setAttribute(ATTR_POINT_XPOS, String.valueOf(x));
		pathElement.setAttribute(ATTR_POINT_YPOS, String.valueOf(y));
		pathElement
				.setAttribute(ATTR_POINT_TIME, SivaTime.getSivaXMLTime(time
						- sceneStartTime));
		return pathElement;
	}

	private Element createEllipsePathElement(Document doc,
			EllipseShape shapeEllipse, long sceneStartTime) {
		Element pathElement = doc.createElement(TAG_ELLIPSE_PATH);
		float x = shapeEllipse.getX();
		float y = shapeEllipse.getY();
		long time = shapeEllipse.getTime();
		float lengthA = shapeEllipse.getLengthA();
		float lengthB = shapeEllipse.getLengthB();
		pathElement.setAttribute(ATTR_POINT_XPOS, String.valueOf(x));
		pathElement.setAttribute(ATTR_POINT_YPOS, String.valueOf(y));
		pathElement
				.setAttribute(ATTR_POINT_TIME, SivaTime.getSivaXMLTime(time 
						- sceneStartTime));
		pathElement.setAttribute(ATTR_ELL_LENGTHA, String.valueOf(lengthA));
		pathElement.setAttribute(ATTR_ELL_LENGTHB, String.valueOf(lengthB));
		return pathElement;
	}

	private Element createPolygonChainElement(Document doc,
			PolygonShape shapePolygon, long sceneStartTime) {
		Element chainElement = doc.createElement(TAG_POLYGON_CHAIN);
		long time = shapePolygon.getTime();
		chainElement.setAttribute(ATTR_POINT_TIME,
				SivaTime.getSivaXMLTime(time
						- sceneStartTime));
		for (Position pos : shapePolygon.getVertices()) {
			Element verticesElement = doc
					.createElement(TAG_POLYGON_CHAIN_VERTICES);
			float x = pos.getX();
			float y = pos.getY();
			verticesElement.setAttribute(ATTR_POINT_XPOS, String.valueOf(x));
			verticesElement.setAttribute(ATTR_POINT_YPOS, String.valueOf(y));
			chainElement.appendChild(verticesElement);
		}
		return chainElement;
	}
}

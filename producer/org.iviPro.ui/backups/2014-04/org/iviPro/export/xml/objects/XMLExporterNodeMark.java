package org.iviPro.export.xml.objects;

import java.util.Set;
import org.iviPro.export.ExportException;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.annotation.EllipseShape;
import org.iviPro.model.annotation.IMarkShape;
import org.iviPro.model.annotation.PolygonShape;
import org.iviPro.model.annotation.PositionalShape;
import org.iviPro.model.annotation.PolygonShape.Position;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.utils.SivaTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLExporterNodeMark extends IXMLExporter {

	XMLExporterNodeMark(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeMark.class;
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported) throws ExportException {

		NodeMark nodeMark = (NodeMark) exportObj;

		// exportiere zuerst die zugehörige normale Annotation
		nodeMark.getTriggerAnnotation().setScreenArea(nodeMark.getScreenArea());
		IXMLExporter exp = ExporterFactory.createExporter(nodeMark
				.getTriggerAnnotation());
		exp.exportObject(doc, idManager, project, alreadyExported);

		// Entsprechenden <showMarkControl> Eintrag in <actions>-Liste erstellen
		Element showMarkAction = doc.createElement(TAG_SHOWMARKCONTROL);
		showMarkAction.setAttribute(ATTR_ACTIONID,
				idManager.getActionID(nodeMark));
		showMarkAction.setAttribute(ATTR_REF_ACTION_ID,
				idManager.getActionID(nodeMark.getTriggerAnnotation()));
		showMarkAction.setAttribute(ATTR_STYLE, nodeMark.getStyle());
		showMarkAction.setAttribute(ATTR_PAUSEVIDEO,
				"" + nodeMark.isPauseVideo());
		showMarkAction.setAttribute(ATTR_DURATION, "" + nodeMark.getDuration());

		Element buttonLabel = doc.createElement(TAG_LABEL);
		buttonLabel.setAttribute(ATTR_RES_ID,
				buttonLabel.getAttribute(ATTR_RES_ID));

		Element labelContent = doc.createElement(TAG_CONTENT);
		buttonLabel.setAttribute(ATTR_LANGCODE, LocalizedString.getSivaLangcode(project.getCurrentLanguage()));
		buttonLabel.setTextContent(nodeMark.getButtonLabel());

		// einhängen des Labels in ressource
		Element ressource = getRessources(doc);
		ressource.appendChild(buttonLabel);
		buttonLabel.appendChild(labelContent);

		Element shapeElement = null;
		// only one type is allowed, determine element Button, Ellipse or
		// Polygon
		switch (nodeMark.getType()) {
		case ELLIPSE:
			shapeElement = doc.createElement(TAG_ELLIPSE);
			break;
		case BUTTON:
			shapeElement = doc.createElement(TAG_BUTTON);
			shapeElement.setAttribute(ATTR_RES_ID,
					buttonLabel.getAttribute(ATTR_RES_ID));
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
							(EllipseShape) shape));
				} else if (shape instanceof PositionalShape) {
					shapeElement.appendChild(createButtonPathElement(doc,
							(PositionalShape) shape));
				} else if (shape instanceof PolygonShape) {
					shapeElement.appendChild(createPolygonChainElement(doc,
							(PolygonShape) shape));
				}
			}

			showMarkAction.appendChild(shapeElement);
		}

		// hängte showMarkAction als Action ein
		Element actions = getActions(doc);
		actions.appendChild(showMarkAction);
	}

	private Element createButtonPathElement(Document doc,
			PositionalShape shapeButton) {
		Element pathElement = doc.createElement(TAG_BUTTON_PATH);
		float x = shapeButton.getX();
		float y = shapeButton.getY();
		long time = shapeButton.getTime();
		pathElement.setAttribute(ATTR_POINT_XPOS, "" + x);
		pathElement.setAttribute(ATTR_POINT_YPOS, "" + y);
		pathElement.setAttribute(ATTR_POINT_TIME,
				"" + SivaTime.getSivaXMLTime(time));
		return pathElement;
	}

	private Element createEllipsePathElement(Document doc,
			EllipseShape shapeEllipse) {
		Element pathElement = doc.createElement(TAG_ELLIPSE_PATH);
		float x = shapeEllipse.getX();
		float y = shapeEllipse.getY();
		long time = shapeEllipse.getTime();
		float lengthA = shapeEllipse.getLengthA();
		float lengthB = shapeEllipse.getLengthB();
		pathElement.setAttribute(ATTR_POINT_XPOS, "" + x);
		pathElement.setAttribute(ATTR_POINT_YPOS, "" + y);
		pathElement.setAttribute(ATTR_POINT_TIME,
				"" + SivaTime.getSivaXMLTime(time));
		pathElement.setAttribute(ATTR_ELL_LENGTHA, "" + lengthA);
		pathElement.setAttribute(ATTR_ELL_LENGTHB, "" + lengthB);
		return pathElement;
	}

	private Element createPolygonChainElement(Document doc,
			PolygonShape shapePolygon) {
		Element chainElement = doc.createElement(TAG_POLYGON_CHAIN);
		long time = shapePolygon.getTime();
		chainElement.setAttribute(ATTR_POINT_TIME,
				"" + SivaTime.getSivaXMLTime(time));
		for (Position pos : shapePolygon.getVertices()) {
			Element verticesElement = doc
					.createElement(TAG_POLYGON_CHAIN_VERTICES);
			float x = pos.getX();
			float y = pos.getY();
			verticesElement.setAttribute(ATTR_POINT_XPOS, "" + x);
			verticesElement.setAttribute(ATTR_POINT_YPOS, "" + y);
			chainElement.appendChild(verticesElement);
		}
		return chainElement;
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}
}

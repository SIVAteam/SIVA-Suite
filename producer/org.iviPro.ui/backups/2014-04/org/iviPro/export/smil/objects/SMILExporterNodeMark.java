package org.iviPro.export.smil.objects;

import java.util.List;
import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.annotation.EllipseShape;
import org.iviPro.model.annotation.IMarkShape;
import org.iviPro.model.annotation.PolygonShape;
import org.iviPro.model.annotation.PositionalShape;
import org.iviPro.model.annotation.PolygonShape.Position;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeMark;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SMILExporterNodeMark extends SMILExporter {

	final static float ONE_HALF = (float) 0.5;
	final static float ONE_THIRD = (float) 0.333333;
	final static float ONE_FOURTH = (float) 0.25;

	SMILExporterNodeMark(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeMark.class;
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document document,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported, Element parent)
			throws ExportException {

		NodeMark mark = (NodeMark) exportObj;
		String markID = idManager.getID(mark);

		List<IMarkShape> shapes = mark.getShapes();
		String beginString = "";
		String lastTime = "";

		for (int i = shapes.size() - 1; i >= 0; i--) {
			IMarkShape shape = shapes.get(i);

			String shapeString = "";
			String coords = "";

			if (shape instanceof EllipseShape) {
				shapeString = "poly";
				coords = shapeEllipse((EllipseShape) shape);
			} else if (shape instanceof PositionalShape) {
				shapeString = "circle";
				coords = shapeButton((PositionalShape) shape);
			} else if (shape instanceof PolygonShape) {
				shapeString = "poly";
				coords = shapePolygon((PolygonShape) shape);
			}

			// Build the marker id and the string that is used later for the
			// begin-attribute of the annotation
			String markerID = markID + ADDITION_MARKER + "_" + i;
			if (i > 0) {
				beginString += markerID + CLICK_EVENT + "; ";
			} else {
				beginString += markerID + CLICK_EVENT;
			}

			// Create the area-elements to trigger the new annotation
			Element sceneSwitch = (Element) parent.getFirstChild();
			NodeList children = sceneSwitch.getChildNodes();
			if (children.getLength() > 0) {
				String time = toNanoString(shape.getTime());

				for (int j = 0; j < children.getLength(); j++) {
					Element videoElement = (Element) children.item(j);

					Element area = document.createElement(TAG_AREA);
					area.setAttribute(ATTR_ID, markerID);
					area.setAttribute(ATTR_SHAPE, shapeString);
					area.setAttribute(ATTR_COORDINATES, coords);
					/*
					 * Does not work in most Ambulant-players, is normally used
					 * to pause when a marker has been pressed
					 * 
					 * if(mark.isPauseVideo()) {
					 * area.setAttribute(ATTR_SOURCE_PLAYSTATE, VAL_PAUSE); }
					 */
					area.setAttribute(ATTR_BEGIN, time);
					if (!lastTime.isEmpty()) {
						area.setAttribute(ATTR_END, lastTime);
					}

					videoElement.appendChild(area);
				}
				lastTime = time;
			} else {
				throw new ExportException("Error when exporting " + mark
						+ " because the video-switch was incorrect.");
			}
		}

		// Now export the annotation
		INodeAnnotation annotation = mark.getTriggerAnnotation();
		SMILExporter exporter = SMILExporterFactory
				.createSMILExporter(annotation);
		exporter.exportObject(document, idManager, project, alreadyExported,
				parent);

		long duration = mark.getDuration();
		String durationString = String.valueOf(duration) + ADDITION_SECONDS;
		
		// The last child of this parent now is the switch-element, that
		// contains the exported trigger annotation. There, a begin-event has to
		// be placed
		Element triggerAnnotation = (Element) parent.getLastChild();
		NodeList annotationChildren = triggerAnnotation.getChildNodes();
		if (annotationChildren.getLength() > 0) {
			for (int i = 0; i < annotationChildren.getLength(); i++) {
				Element annotationElement = (Element) annotationChildren
						.item(i);

				annotationElement.setAttribute(ATTR_BEGIN, beginString);
				annotationElement.setAttribute(ATTR_DURATION, durationString);

				// The duration has to be added for the animation elements
				// aswell
				NodeList animationChildren = annotationElement.getChildNodes();
				if (animationChildren.getLength() > 0) {
					for (int j = 0; j < animationChildren.getLength(); j++) {
						Element animation = (Element) animationChildren.item(j);

						animation.setAttribute(ATTR_DURATION, durationString);
					}
				}
			}
		}

		if (mark.isPauseVideo()) {
			handlePauseAnnotation(parent, triggerAnnotation, document);
		}
	}

	/**
	 * Method to create the coordinates for one ellipse-shaped mark. Because
	 * ellipse-forms are not supported in SMIL, the ellipse will be approximated
	 * through eight points.
	 * 
	 * @param shape
	 *            The MarkShapeEllipse for the mark.
	 * @return A String corresponding to the coordinates for the shape.
	 */
	private String shapeEllipse(EllipseShape shape) {
		String coords = "";

		float x = shape.getX();
		float y = shape.getY();
		float lengthA = shape.getLengthA();
		float lengthB = shape.getLengthB();

		String pointOne = String
				.valueOf((int) ((x - ONE_THIRD * lengthB) * 100))
				+ "%, "
				+ String.valueOf((int) ((y - ONE_HALF * lengthA) * 100)) + "%";
		String pointTwo = String
				.valueOf((int) ((x + ONE_THIRD * lengthB) * 100))
				+ "%, "
				+ String.valueOf((int) ((y - ONE_HALF * lengthA) * 100)) + "%";
		String pointThree = String
				.valueOf((int) ((x + ONE_HALF * lengthB) * 100))
				+ "%, "
				+ String.valueOf((int) ((y - ONE_FOURTH * lengthA) * 100))
				+ "%";
		String pointFour = String
				.valueOf((int) ((x + ONE_HALF * lengthB) * 100))
				+ "%, "
				+ String.valueOf((int) ((y + ONE_FOURTH * lengthA) * 100))
				+ "%";
		String pointFive = String
				.valueOf((int) ((x + ONE_THIRD * lengthB) * 100))
				+ "%, "
				+ String.valueOf((int) ((y + ONE_HALF * lengthA) * 100)) + "%";
		String pointSix = String
				.valueOf((int) ((x - ONE_THIRD * lengthB) * 100))
				+ "%, "
				+ String.valueOf((int) ((y + ONE_HALF * lengthA) * 100)) + "%";
		String pointSeven = String
				.valueOf((int) ((x - ONE_HALF * lengthB) * 100))
				+ "%, "
				+ String.valueOf((int) ((y + ONE_FOURTH * lengthA) * 100))
				+ "%";
		String pointEight = String
				.valueOf((int) ((x - ONE_HALF * lengthB) * 100))
				+ "%, "
				+ String.valueOf((int) ((y - ONE_FOURTH * lengthA) * 100))
				+ "%";

		coords = pointOne + ", " + pointTwo + ", " + pointThree + ", "
				+ pointFour + ", " + pointFive + ", " + pointSix + ", "
				+ pointSeven + ", " + pointEight;

		return coords;
	}

	/**
	 * Method to create the coordinates for one button-shaped mark.
	 * 
	 * @param shape
	 *            The MarkShapeButton for the mark.
	 * @return A String corresponding to the coordinates for the shape.
	 */
	private String shapeButton(PositionalShape shape) {
		String coords = "";

		float x = shape.getX();
		float y = shape.getY();
		String xString = Integer.toString((int) (x * 100)) + "%";
		String yString = Integer.toString((int) (y * 100)) + "%";

		coords = xString + ", " + yString + ", "
				+ Integer.toString(MARK_SHAPE_BUTTON_SIZE);

		return coords;
	}

	/**
	 * Method to create the coordinates for one polygon-shaped mark.
	 * 
	 * @param shape
	 *            The MarkShapePolygon for the mark.
	 * @return A String corresponding to the coordinates for the shape.
	 */
	private String shapePolygon(PolygonShape shape) {
		String coords = "";

		for (Position pos : shape.getVertices()) {
			int x = (int) pos.getX();
			int y = (int) pos.getY();

			coords += Integer.toString(x * 100) + ", "
					+ Integer.toString(y * 100) + ", ";
		}

		coords = (String) coords.subSequence(0, coords.length() - 2);

		return coords;
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}

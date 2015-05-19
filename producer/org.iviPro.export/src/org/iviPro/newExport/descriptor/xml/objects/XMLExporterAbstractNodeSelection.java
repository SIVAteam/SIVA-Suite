package org.iviPro.newExport.descriptor.xml.objects;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.AbstractNodeSelection;
import org.iviPro.model.graph.AbstractNodeSelectionControl;
import org.iviPro.model.graph.AbstractNodeSelectionControl.SelectionControlType;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeCondSelectionControl;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.resources.Picture;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.iviPro.newExport.descriptor.xml.resources.IXMLResourceExporter;
import org.iviPro.newExport.descriptor.xml.resources.ResourceExporterFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLExporterAbstractNodeSelection extends IXMLExporter {

	private AbstractNodeSelection selection;
	
	XMLExporterAbstractNodeSelection(AbstractNodeSelection exportObj) {
		super(exportObj);
		selection = exportObj;
	}

	// <showSelectionControl type="alert" REFcontrolIDdefault="button2"
	// actionID="selcontrol">
	// <area screenArea="bottom" />
	// <controls REFactionID="load-scene-00" REFresID="label-button1"
	// controlID="button1" />
	// <controls REFactionID="load-scene-00" REFresID="label-button1"
	// controlID="button2" />
	// </showSelectionControl>

	@Override
	protected void exportObjectImpl(Object exportObj, Document doc,
			IdManager idManager, Project project,
			Set<Object> alreadyExported) throws ExportException {

		AbstractNodeSelectionControl defaultControl = selection.getDefaultControl();
		
		Element actions = getActions(doc);
		Element showSelControl = doc.createElement(TAG_SHOWSELECTIONCONTROL);
		String type = selection.getButtonType().toString().toLowerCase();
		showSelControl.setAttribute(ATTR_SHOWSELECTIONCONTROL_TYPE, type);
		showSelControl.setAttribute(ATTR_ACTIONID,
				idManager.getActionID(selection));
		String labelID = createTitleLabels(selection, doc, idManager);
		showSelControl.setAttribute(ATTR_REF_RES_ID, labelID);
		if (defaultControl != null) {
			showSelControl.setAttribute(
					ATTR_SHOWSELECTIONCONTROL_DEFAULTCONTROL,
					idManager.getID(defaultControl));
			Date timeout = new Date(selection.getTimeout() * 1000);
			SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss"); //$NON-NLS-1$
			showSelControl.setAttribute(ATTR_SHOWSELECTIONCONTROL_TIMEOUT,
					"00:" + dateFormat.format(timeout)); //$NON-NLS-1$
		}
		actions.appendChild(showSelControl);

		// Export selection alternatives (controls) connected to the selection
		List<IGraphNode> controls = selection
				.getChildren(AbstractNodeSelectionControl.class);
		HashMap<Integer, AbstractNodeSelectionControl> controlRanks =
				new HashMap<Integer, AbstractNodeSelectionControl>(controls.size(), 1);
		for (IGraphNode controlNode : controls) {
			AbstractNodeSelectionControl control = (AbstractNodeSelectionControl) controlNode;
			controlRanks.put(control.getRank(), control);
		}
		for (int i=1; i<=controlRanks.size(); i++) {
			AbstractNodeSelectionControl control = controlRanks.get(i);

			if (control.getChildren().size() == 0) {
				throw new ExportException(
						String.format(
								"Fork control '%s' has no successor.", control.getTitle())); //$NON-NLS-1$
			}

			// Zuerst mit diesem Control verbundenen Knoten exportieren
			IGraphNode successor = control.getChildren().get(0);
			IXMLExporter exporter = ExporterFactory.createExporter(successor);
			exporter.exportObject(doc, idManager, project, alreadyExported);

			// Dann Control selbst exportieren
			Element controlElement = doc.createElement(TAG_CONTROL);
			
			controlElement.setAttribute(ATTR_COND_CONTROL_VISIBLE,
					Boolean.toString(control.isVisible()));
			String messageID = createDescriptionLabels(control, doc, idManager);
			if (messageID != null) {
				controlElement.setAttribute(ATTR_COND_REF_MSG, messageID);
			}
			
			if (control.getType().equals(SelectionControlType.CONDITIONAL)) {
				NodeCondSelectionControl condControl = 
						(NodeCondSelectionControl) control;
				if (!condControl.getPrerequisiteScenes().isEmpty()) {
					Element scenes = doc.createElement(TAG_CONDITION);
					scenes.setAttribute(ATTR_COND_TYPE, "watchedScenes"); //$NON-NLS-1$
					for (NodeScene scene : condControl.getPrerequisiteScenes()) {
						Element watchedScene = doc.createElement(TAG_WATCHEDSCENE);
						watchedScene.setAttribute(ATTR_WATCHEDSCENE_SCENEID,
								idManager.getID(scene));
						scenes.appendChild(watchedScene);
					}
					controlElement.appendChild(scenes);
				}
				
			}
			String controlType = control.getType().toString().toLowerCase();
			controlElement.setAttribute(ATTR_CONTROL_TYPE, controlType);
			controlElement.setAttribute(ATTR_POSITION, String.valueOf(control.getRank()));
			String controlID = idManager.getID(control);
			controlElement.setAttribute(ATTR_CONTROL_ID, controlID);
			String controlActionID = idManager.getActionID(successor);
			controlElement.setAttribute(ATTR_REF_ACTION_ID, controlActionID);
			
			// Export title and/or title image			
			String conLabelID = createTitleLabels(control, doc,
					idManager);
			String conImageID = createButtonImage(control, doc, idManager,
					alreadyExported);
		
			if (conLabelID != null) {
				controlElement.setAttribute(ATTR_REF_RES_ID, conLabelID);
				if (conImageID != null) {
					controlElement.setAttribute(ATTR_REF_RES_ID_SECONDARY,
							conImageID);
				}
			} else {
				controlElement.setAttribute(ATTR_REF_RES_ID, conImageID);
			}
			
			showSelControl.appendChild(controlElement);
		}
	}

	/**
	 * Exports the image resource associated with the given 
	 * <code>SelectionControl</code>. Returns the ID of that image resource. If no
	 * image is associated with the control, null is returned and the given 
	 * document remains unchanged.
	 * 
	 * @param control <code>SelectionControl</code> for which the image should be
	 * processed
	 * @param doc XML document to which the XML code is added
	 * @param idManager generator for resource IDs
	 * @param alreadyExported set of already exported objects
	 * @return ID of the image of the given control or <code>null</code> if no image
	 * is associated with the control
	 * @throws ExportException - if an error occurs during export of the image
	 */
	private String createButtonImage(AbstractNodeSelectionControl control,
			Document doc, IdManager idManager, Set<Object> alreadyExported) 
					throws ExportException {
		
		Picture buttonImage = control.getButtonImage();
		// Return if no image has been selected
		if (buttonImage == null) {
			return null;
		}		
		// Export image resource
		IXMLResourceExporter exporter = ResourceExporterFactory.createExporter(buttonImage);
		exporter.exportResource(doc, idManager, alreadyExported);
		return idManager.getID(buttonImage);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return AbstractNodeSelection.class;
	}
}

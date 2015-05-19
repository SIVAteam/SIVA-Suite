package org.iviPro.export.xml.objects;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.Exporter;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeSelection;
import org.iviPro.model.graph.NodeSelectionControl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLExporterNodeSelection extends IXMLExporter {

	XMLExporterNodeSelection(IAbstractBean exportObj) {
		super(exportObj);
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
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported) throws ExportException {

		NodeSelection selection = (NodeSelection) exportObj;
		NodeSelectionControl defaultControl = selection.getDefaultControl();
		String type = selection.getButtonType().toString().toLowerCase();

		// Dann Label fuer Control exportieren
		String labelID = createTitleLabels(selection, doc, idManager);

		Element actions = getActions(doc);
		Element showSelControl = doc.createElement(TAG_SHOWSELECTIONCONTROL);
		showSelControl.setAttribute(ATTR_SHOWSELECTIONCONTROL_TYPE, type);
		showSelControl.setAttribute(ATTR_ACTIONID, idManager
				.getActionID(selection));
		showSelControl.setAttribute(ATTR_REF_RES_ID, labelID);
		if (defaultControl != null) {
			showSelControl.setAttribute(
					ATTR_SHOWSELECTIONCONTROL_DEFAULTCONTROL, idManager
							.getID(defaultControl));
			Date timeout = new Date(selection.getTimeout() * 1000);
			SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss"); //$NON-NLS-1$
			showSelControl.setAttribute(ATTR_SHOWSELECTIONCONTROL_TIMEOUT,
					"00:" + dateFormat.format(timeout)); //$NON-NLS-1$
		}
		actions.appendChild(showSelControl);

		Element posInfo = createPositionInfo(selection, doc, idManager, project);
		showSelControl.appendChild(posInfo);

		List<IGraphNode> controls = selection
				.getChildren(NodeSelectionControl.class);
		for (IGraphNode controlNode : controls) {
			NodeSelectionControl control = (NodeSelectionControl) controlNode;

			if (control.getChildren().size() == 0) {
				// Control hat keinen Nachfolger => Fehler
				throw new ExportException("Fork control '" + control.getTitle() //$NON-NLS-1$
						+ "' has no successor."); //$NON-NLS-1$
			}

			// Zuerst mit diesem Control verbundenen Knoten exportieren
			IGraphNode successor = control.getChildren().get(0);
			IXMLExporter exporter = ExporterFactory.createExporter(successor);
			exporter.exportObject(doc, idManager, project, alreadyExported);

			// Dann Label fuer Control exportieren

			String controlLabelRefResID = createTitleLabels(control, doc,
					idManager);
			String controlImageRefResID = createImageButton(control, doc,
					idManager);

			// Dann Control selbst exportieren
			Element controlElement = doc.createElement(TAG_CONTROL);
			String controlID = idManager.getID(control);
			controlElement.setAttribute(ATTR_CONTROL_ID, controlID);
			String controlActionID = idManager.getActionID(successor);
			controlElement.setAttribute(ATTR_REF_ACTION_ID, controlActionID);
			controlElement.setAttribute(ATTR_REF_RES_ID, controlLabelRefResID);
			if (controlImageRefResID != null) {
				controlElement.setAttribute(ATTR_REF_RES_ID_SECONDARY,
						controlImageRefResID);
			}
			// String controlImageID = idManager.getRessourceID(control);
			// if (controlImageID != null) {
			// controlElement.setAttribute(ATTR_REF_RES_ID_SECONDARY,
			// controlImageID);
			// }
			showSelControl.appendChild(controlElement);
		}
	}

	private String createImageButton(NodeSelectionControl selectionControl,
			Document doc, IDManager idManager) throws ExportException {
		if (selectionControl.getButtonImage() == null) {
			return null;
		}

		// ID der Ressource holen
		String resID = idManager.getRessourceID(selectionControl);

		// Ressource-Element anlegen.
		Element imgResource = doc.createElement(TAG_IMAGE);
		imgResource.setAttribute(ATTR_RES_ID, resID);

		IFileBasedObject fileObject = selectionControl.getButtonImage();

		// Dateibasierte Annotation
		Collection<LocalizedFile> files = fileObject.getFiles();
		if (files.isEmpty())
			return null; // Mind. ein File muss existieren!
		for (LocalizedFile file : files) {
			Locale language = file.getLanguage();
			String filename = Exporter.EXPORT_SUBDIR_PICTURES + "/" //$NON-NLS-1$
					+ idManager.getFilename(fileObject, language, getExportParameters());
			Element content = doc.createElement(TAG_CONTENT);
			content.setAttribute(ATTR_HREF, filename);
			content.setAttribute(ATTR_LANGCODE, file.getSivaLangcode());
			imgResource.appendChild(content);
		}
		getRessources(doc).appendChild(imgResource);

		return resID;

	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeSelection.class;
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}

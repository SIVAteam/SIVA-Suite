package org.iviPro.newExport.descriptor.xml.objects;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeRandomSelection;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLExporterNodeRandomSelection extends IXMLExporter {
	
	private NodeRandomSelection randomSelection;
	
	XMLExporterNodeRandomSelection(NodeRandomSelection exportObj) {
		super(exportObj);
		randomSelection = exportObj;
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeRandomSelection.class;
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IdManager idManager, Project project,
			Set<Object> alreadyExported) throws ExportException {
		
		Element actions = getActions(doc);
		Element randomSelectionElement = doc.createElement(TAG_RANDOMSELECTION);
		randomSelectionElement.setAttribute(ATTR_ACTIONID,
				idManager.getActionID(randomSelection));
		
		actions.appendChild(randomSelectionElement);

		List<IGraphNode> successors = randomSelection.getChildren();
		
		for (IGraphNode successor : successors) {
			// Export successor 
			IXMLExporter exporter = ExporterFactory.createExporter(successor);
			exporter.exportObject(doc, idManager, project, alreadyExported);
			
			// Add random entry for successor
			Element randomPathElement = doc.createElement(TAG_RANDOM);
			String randomActionID = idManager.getActionID(successor);
			randomPathElement.setAttribute(ATTR_REF_ACTION_ID, randomActionID);
			Double prob = 0.0;
			if (randomSelection.useEqualProbability()) {
				prob = (1.0 / successors.size());
			} else {
				prob = randomSelection.getProbabilityMap().get(successor) / 100.0;
			}
			randomPathElement.setAttribute(ATTR_PROBABILITY,
					String.format(Locale.ENGLISH, "%.2f" , prob));
			randomSelectionElement.appendChild(randomPathElement);
		}
	}
}

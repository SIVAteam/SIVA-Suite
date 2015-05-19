package org.iviPro.newExport.descriptor.xml.objects;

import java.util.Set;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.NodeEnd;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class XMLExporterNodeEnd extends IXMLExporter {

	private NodeEnd end;
	
	XMLExporterNodeEnd(NodeEnd exportObj) {
		super(exportObj);
		end = exportObj;
	}

	@Override
	protected void exportObjectImpl(Object exportObj, Document doc,
			IdManager idManager, Project project,
			Set<Object> alreadyExported) throws ExportException {
				
		Element endSiva = doc.createElement(TAG_ENDSIVA);
		endSiva.setAttribute(ATTR_ACTIONID, idManager.getEndActionID());
		endSiva.setIdAttribute(ATTR_ACTIONID, true);
		
		String labelID = createTitleLabels(end, doc, idManager);
		endSiva.setAttribute(ATTR_REF_RES_ID, labelID);
		
		Element actions = getActions(doc);
		actions.appendChild(endSiva);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeEnd.class;
	}
}

package org.iviPro.newExport.descriptor.xml.objects;

import java.util.Set;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.TocItem;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Exporter fuer Eintraege im Inhaltsverzeichnis
 * 
 * @author dellwo
 * 
 */
class XMLExporterTocItem extends IXMLExporter {

	XMLExporterTocItem(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	protected void exportObjectImpl(Object exportObj, Document doc,
			IdManager idManager, Project project,
			Set<Object> alreadyExported) throws ExportException {
		// Das zu exportierende Toc-Item
		TocItem tocItem = (TocItem) exportObj;

		// Eintrag fuer diesen Punkt des Inhaltsverzeichnis erstellen
		Element tocNode = doc.createElement(TAG_TOC_CONTENTS);
		String tocNodeLabelID = createTitleLabels(tocItem, doc, idManager);
		tocNode.setAttribute(ATTR_REF_RES_ID, tocNodeLabelID);
		String tocNodeID = idManager.getID(tocItem);
		tocNode.setAttribute(ATTR_TOC_CONTENTSNODEID, tocNodeID);
		if (tocItem.getScene() != null) {
			String tocREFActionID = idManager.getActionID(tocItem.getScene());
			tocNode.setAttribute(ATTR_REF_ACTION_ID, tocREFActionID);
		}

		// Adjazenz-Liste mit den Verbindungen zu den Kind-Knoten erstellen.
		for (TocItem tocChild : tocItem.getChildren()) {
			Element childElem = doc.createElement(TAG_TOC_ADJACENCY_LIST);
			String childId = idManager.getID(tocChild);
			childElem.setAttribute(ATTR_TOC_REF_CONTENTSNODEID, childId);
			tocNode.appendChild(childElem);
		}

		// Eintrag in das Inhaltsverzeichnis einfuegen
		Element tocRoot = getTableOfContents(doc);
		tocRoot.appendChild(tocNode);

		// Alle Kinder exportieren
		for (TocItem tocChild : tocItem.getChildren()) {
			IXMLExporter exporter = ExporterFactory.createExporter(tocChild);
			exporter.exportObject(doc, idManager, project, alreadyExported);
		}
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return TocItem.class;
	}
}

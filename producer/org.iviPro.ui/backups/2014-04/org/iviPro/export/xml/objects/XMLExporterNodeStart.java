package org.iviPro.export.xml.objects;

import java.util.List;
import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeStart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class XMLExporterNodeStart extends IXMLExporter {

	XMLExporterNodeStart(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported) throws ExportException {
		NodeStart nodeStart = (NodeStart) exportObj;
		List<IGraphNode> children = nodeStart.getChildren(NodeScene.class);

		if (children.size() != 1) {
			throw new ExportException(
					Messages.XMLExporterNodeStart_ErrorMsg_MoreThanOneStartScene);
		} else {
			NodeScene startScene = (NodeScene) children.get(0);
			IXMLExporter exporter = ExporterFactory.createExporter(startScene);
			exporter.exportObject(doc, idManager, project, alreadyExported);

			// Start-Szene eintragen
			Element sceneList = getSceneList(doc);
			sceneList.setAttribute(ATTR_REF_STARTSCENE, idManager
					.getID(startScene));
		}

	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeStart.class;
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}

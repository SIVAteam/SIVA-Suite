package org.iviPro.newExport.descriptor.xml.objects;

import java.util.List;
import java.util.Set;

import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeStart;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.descriptor.xml.IdManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class XMLExporterNodeStart extends IXMLExporter {

	private NodeStart nodeStart;
	
	XMLExporterNodeStart(NodeStart exportObj) {
		super(exportObj);
		nodeStart = exportObj;
	}

	@Override
	protected void exportObjectImpl(Object exportObj, Document doc,
			IdManager idManager, Project project,
			Set<Object> alreadyExported) throws ExportException {
		
		List<IGraphNode> children = nodeStart.getChildren(NodeScene.class);

		if (children.size() != 1) {
			throw new ExportException("The project could not be exported" //$NON-NLS-1$
					+ " because your scene graph contains more than one" //$NON-NLS-1$
					+ " start scene. Please make sure that there is " //$NON-NLS-1$
					+ "only one start scene defined in your scene graph."); //$NON-NLS-1$
		} else {
			NodeScene startScene = (NodeScene) children.get(0);
			IXMLExporter exporter = ExporterFactory.createExporter(startScene);
			exporter.exportObject(doc, idManager, project, alreadyExported);

			// Start-Szene eintragen
			Element sceneList = getSceneList(doc);
			sceneList.setAttribute(ATTR_REF_STARTSCENE,
					idManager.getID(startScene));
		}

	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeStart.class;
	}
}

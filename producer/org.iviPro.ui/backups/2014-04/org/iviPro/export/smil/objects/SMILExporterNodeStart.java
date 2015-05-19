package org.iviPro.export.smil.objects;

import java.util.List;
import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.export.xml.objects.Messages;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeStart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SMILExporterNodeStart extends SMILExporter {

	SMILExporterNodeStart(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeStart.class;
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported, Element parent) throws ExportException {
		NodeStart start = (NodeStart) exportObj;
		List<IGraphNode> children = start.getChildren(NodeScene.class);

		if (children.size() != 1) {
			throw new ExportException(
					Messages.XMLExporterNodeStart_ErrorMsg_MoreThanOneStartScene);
		} else {
			NodeScene startScene = (NodeScene) children.get(0);
			SMILExporter exporter = SMILExporterFactory
					.createSMILExporter(startScene);
			exporter.exportObject(doc, idManager, project, alreadyExported, null);
		}
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}

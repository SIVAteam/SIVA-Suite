package org.iviPro.export.xml.objects;

import java.util.Set;
import org.iviPro.export.ExportException;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.NodeEnd;
import org.w3c.dom.Document;

class XMLExporterNodeEnd extends IXMLExporter {

	XMLExporterNodeEnd(IAbstractBean exportObj) {
		super(exportObj);
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported) throws ExportException {
		// Nothing to do here...

	}

	@Override
	Class<? extends IAbstractBean> getExportObjectType() {
		return NodeEnd.class;
	}

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}

}

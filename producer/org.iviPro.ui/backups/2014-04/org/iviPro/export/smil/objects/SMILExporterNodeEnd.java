package org.iviPro.export.smil.objects;

import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.graph.NodeEnd;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SMILExporterNodeEnd extends SMILExporter {

    SMILExporterNodeEnd(IAbstractBean exportObj) {
	super(exportObj);
    }

    @Override
    Class<? extends IAbstractBean> getExportObjectType() {
	return NodeEnd.class;
    }

    @Override
    protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
	    IDManager idManager, Project project,
	    Set<IAbstractBean> alreadyExported, Element parent) throws ExportException {
    	// Nothing to be done
	
    }

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}
    
}

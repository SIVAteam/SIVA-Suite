package org.iviPro.export.smil.objects;

import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.xml.ExportParameters;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.TocItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SMILExporterTocItem extends SMILExporter {

    SMILExporterTocItem(IAbstractBean exportObj) {
	super(exportObj);
    }

    @Override
    Class<? extends IAbstractBean> getExportObjectType() {
	return TocItem.class;
    }

    @Override
    protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
	    IDManager idManager, Project project,
	    Set<IAbstractBean> alreadyExported, Element parent) throws ExportException {
	
    }

	@Override
	protected ExportParameters getExportParameters() {
		return this.parameters;
	}
}

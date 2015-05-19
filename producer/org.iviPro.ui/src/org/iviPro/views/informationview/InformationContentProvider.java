package org.iviPro.views.informationview;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Diese Klasse stellt die Funktionen bereit, wie die Elemente für die Ansicht
 * Information beschaffen sein müssen.
 * 
 * @author Florian Stegmaier
 */
public class InformationContentProvider implements IStructuredContentProvider {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(InformationContentProvider.class);
	public InformationContentProvider(){
	}
	@Override
	public Object[] getElements(Object inputElement) {
		return ((InformationContainer)inputElement).getList().toArray();
	}

	@Override
	public void dispose() {		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {		
	}

}

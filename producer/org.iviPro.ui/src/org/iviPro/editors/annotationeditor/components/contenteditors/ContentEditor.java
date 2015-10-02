package org.iviPro.editors.annotationeditor.components.contenteditors;

import org.eclipse.swt.widgets.Composite;
import org.iviPro.editors.common.SivaComposite;
import org.iviPro.model.IAbstractBean;

public abstract class ContentEditor extends SivaComposite {
	
	public ContentEditor(Composite parent, int style) {
		super(parent, style);
	}

	public abstract void setContent(IAbstractBean newContent);
}

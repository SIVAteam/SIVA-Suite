package org.iviPro.editors.subtitleviewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.editors.annotationeditor.components.contenteditors.SubtitleEditor;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Subtitle;
import org.iviPro.theme.Icons;

public class SubtitleViewer extends IAbstractEditor implements
		PropertyChangeListener {

	public static final String ID = SubtitleViewer.class.getName();

	private Subtitle subtitle;
	
	private SubtitleEditor editor;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public Image getDefaultImage() {
		return Icons.EDITOR_SUBTITLE.getImage();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		subtitle = ((SubtitleViewerInput) input).getSubtitle();
		subtitle.addPropertyChangeListener(IAbstractBean.PROP_TITLE, this);
		setPartName(subtitle.getTitle());		
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControlImpl(Composite parent) {
		editor = new SubtitleEditor(parent, SWT.CENTER, subtitle);
		editor.setEnabled(false);
	}

	@Override
	public void setFocus() {
		if (editor != null) {
			editor.setFocus();
		}
	}

	public void dispose() {
		subtitle.removePropertyChangeListener(this);
		super.dispose();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				// Update Editor-Titel wenn Richtext umbenannt wird.
				setPartName(subtitle.getTitle());
			}
		});
	}
}

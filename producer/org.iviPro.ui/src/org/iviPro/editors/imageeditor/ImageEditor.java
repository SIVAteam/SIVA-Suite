package org.iviPro.editors.imageeditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.application.Application;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.model.resources.Picture;
import org.iviPro.theme.Colors;
import org.iviPro.theme.Icons;

public class ImageEditor extends IAbstractEditor implements PropertyChangeListener {

	public static final String ID = ImageEditor.class.getName();
	public static final String PREFIX_IMAGEEDITOR = "Bildeditor "; //$NON-NLS-1$

	private Image img;
	private Picture picture;
	private ImageEditWidget edit;

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (edit != null) {
			picture.setObjects(edit.getObjectsForSave());
		}
	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public Image getDefaultImage() {
		return Icons.EDITOR_IMAGE.getImage();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);

		picture = ((ImageWidgetInput) input).getPicture();
		picture.addPropertyChangeListener(this);
		this.img = picture.getImage();
		setPartName(PREFIX_IMAGEEDITOR + picture.getTitle(Application.getCurrentLanguage()));				
	}

	@Override
	public boolean isDirty() {
		if (edit != null) {
			return edit.isDirty();
		} else {
			return false;
		}
	}

	@Override
	public boolean isSaveAsAllowed() {

		return false;
	}

	@Override
	public void createPartControlImpl(Composite parent) {

		Composite root = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		root.setLayout(layout);
		root.setBackground(Colors.EDITOR_BG.getColor());

		if (Application.getDefault().isProjectOpen() && img != null) {
			edit = new ImageEditWidget(root, SWT.CENTER, picture, img);
		}
	}

	@Override
	public void setFocus() {
		if (edit != null) {
			edit.setFocus();
		}
	}

	public void dispose() {
		picture.removePropertyChangeListener(this);
		if (img != null && !img.isDisposed()) {
			img.dispose();
		}
		super.dispose();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				setPartName(PREFIX_IMAGEEDITOR
						+ picture.getTitle(Application.getCurrentLanguage()));
			}
		});
	}
}
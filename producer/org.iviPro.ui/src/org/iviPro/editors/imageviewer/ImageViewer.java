package org.iviPro.editors.imageviewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.application.Application;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.imageeditor.ImageObject;
import org.iviPro.model.resources.Picture;
import org.iviPro.theme.Colors;
import org.iviPro.theme.Icons;

/**
 * Image Betrachter + Bildergalerie Betrachter
 * 
 * aktuell geht erst das Betrachten eines Bildes!! TODO
 * @author juhoffma
 *
 */
public class ImageViewer extends IAbstractEditor implements PropertyChangeListener {

	public static final String ID = ImageViewer.class.getName();
	public static final String PREFIX_IMAGEEDITOR = Messages.PREFIX_IMAGEEDITOR; //$NON-NLS-1$

	// input ist entweder ein Bild oder eine Bildergalerie
	private IAbstractBean viewerInput;

	@Override
	public void doSave(IProgressMonitor monitor) {
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
		viewerInput = ((ImageWidgetInput) input).getInput();
		viewerInput.addPropertyChangeListener(this);
		setPartName(PREFIX_IMAGEEDITOR + viewerInput.getTitle(Application.getCurrentLanguage()));	
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
		Composite root = new Composite(parent, SWT.NONE);
		root.setBackground(Colors.EDITOR_BG.getColor());
		root.setEnabled(false);
		
		if (viewerInput instanceof Picture) {		
			root.setLayout(new GridLayout(1, false));			
			Picture picture = (Picture) viewerInput;
			addImageComposite(root, picture, 600, -1);
		} else 
		
		if (viewerInput instanceof PictureGallery) {
			PictureGallery gal = (PictureGallery) viewerInput;
			root.setLayout(new GridLayout(gal.getNumberColumns(), false));
			for (Control c : root.getChildren()) {
				c.dispose();
			}
			for (Picture pic : gal.getPictures()) {
				addImageComposite(root, pic, 100, 100);
			}
			root.layout(true);
		}
	}	
	
	private void addImageComposite(Composite parent, Picture picture, int width , int height) {
		Image img = picture.getImage();
		Composite imageComposite = new Composite(parent, SWT.CENTER);
		GridData gd = new GridData();
		gd.widthHint = width;
		if (height == -1) {			
			gd.heightHint = (int) ((((double) img.getImageData().height) / ((double) img.getImageData().width)) * gd.widthHint);			
		} else {
			gd.heightHint = 100;
		}
		imageComposite.setLayoutData(gd);
		final Image scaledImage = new Image(Display.getCurrent(), img.getImageData().scaledTo(gd.widthHint, gd.heightHint));
		GC gc = new GC(scaledImage);
		for (ImageObject imgObj : picture.getObjects()) {
			org.iviPro.editors.imageeditor.ImageEditWidget.drawObject(gc, imgObj, scaledImage);
		}			
		imageComposite.setBackgroundImage(scaledImage);
		// das Bild wird nicht mehr gebraucht
		img.dispose();	
		
		imageComposite.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				if (scaledImage != null) {
					scaledImage.dispose();
				}
			}
		});
	}

	@Override
	public void setFocus() {	
	}

	public void dispose() {
		viewerInput.removePropertyChangeListener(this);
		super.dispose();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				setPartName(PREFIX_IMAGEEDITOR
						+ viewerInput.getTitle(Application.getCurrentLanguage()));
			}
		});
	}
}
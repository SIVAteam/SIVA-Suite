package org.iviPro.editors.richtextviewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.htmltext.HtmlComposer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.application.Application;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.ProjectSettings;
import org.iviPro.model.RichText;
import org.iviPro.theme.Colors;
import org.iviPro.theme.Icons;

public class RichtextViewer extends IAbstractEditor implements
		PropertyChangeListener {

	private static Logger logger = Logger.getLogger(RichtextViewer.class);
	public static final String ID = RichtextViewer.class.getName();

	private RichText richtext;
	
	private HtmlComposer viewer;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public Image getDefaultImage() {
		return Icons.EDITOR_RICHTEXT.getImage();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		richtext = ((RichtextViewerInput) input).getRichtext();
		richtext.addPropertyChangeListener(IAbstractBean.PROP_TITLE, this);
		setPartName(richtext.getTitle());		
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

		try {
			viewer = new HtmlComposer(parent, SWT.NONE);
			
	        // passe den Hintergrund entsprechend dem Skin an
	        int curSkin = Application.getCurrentProject().getSettings().getSkin();
	        Color backgroundColor = Colors.SKIN_SIMPLE_BG.getColor();
	        Color foregroundColor = Colors.SKIN_SIMPLE_FG.getColor();
	        if (curSkin == ProjectSettings.SKIN_SIMPLE) {
	        	backgroundColor = Colors.SKIN_SIMPLE_BG.getColor();
	        	foregroundColor = Colors.SKIN_SIMPLE_FG.getColor();
	        } else
	        if (curSkin == ProjectSettings.SKIN_DARK) {
	        	backgroundColor = Colors.SKIN_DARK_BG.getColor();
	        	foregroundColor = Colors.SKIN_DARK_FG.getColor();
	        } 
	        
	        viewer.setBackground(backgroundColor);
	        viewer.setForeground(foregroundColor);
	        viewer.setHtml(richtext.getContent());
	        viewer.setEnabled(false);
		} catch (SWTError e) {
			String errorMsg = Messages.RichtextViewer_Error_MsgBoxText_NoBrowserAvailable;
			logger.error(errorMsg);
			MessageDialog.openError(parent.getShell(), Messages.RichtextViewer_Error_MsgBoxTitle, errorMsg);
			return;
		}
	}

	@Override
	public void setFocus() {
		if (viewer != null) {
			viewer.setFocus();
		}
	}

	public void dispose() {
		richtext.removePropertyChangeListener(this);
		super.dispose();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				// Update Editor-Titel wenn Richtext umbenannt wird.
				setPartName(richtext.getTitle());
			}
		});
	}
}

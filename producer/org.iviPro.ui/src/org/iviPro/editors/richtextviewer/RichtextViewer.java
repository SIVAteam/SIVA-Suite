package org.iviPro.editors.richtextviewer;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.RichText;
import org.iviPro.theme.Icons;

public class RichtextViewer extends IAbstractEditor {

	private static Logger logger = Logger.getLogger(RichtextViewer.class);
	public static final String ID = RichtextViewer.class.getName();

	private Composite bridgeComposite;
	private Frame frame;
	private JPanel contentPane;
	private JEditorPane editor;

	private RichText richtext;

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
		super.init(site, input);
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
		bridgeComposite = new Composite(parent, SWT.EMBEDDED);
		// bridge zu awt
		frame = SWT_AWT.new_Frame(bridgeComposite);

		// Create AWT components
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				contentPane = new JPanel();
				contentPane.setLayout(new GridBagLayout());
				JRootPane rp = new JRootPane();
				rp.getContentPane().add(contentPane);
				frame.add(rp);

				editor = new JEditorPane();
				editor.setEditable(false);
				editor.setContentType("text/html");
				editor.setDocument(richtext.getDocument());

				JScrollPane scrollPane = new JScrollPane(editor);
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.BOTH;
				c.weightx = 1.0;
				c.weighty = 1.0;
				c.gridy = 1;
				contentPane.add(scrollPane, c);
				frame.validate();
			}
		});
	}

	@Override
	public void setFocus() {
		if (bridgeComposite != null) {
			bridgeComposite.setFocus();
		}
	}

	public void dispose() {
		richtext.removePropertyChangeListener(this);
		super.dispose();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				// Update Editor-Titel wenn Richtext umbenannt wird.
				setPartName(richtext.getTitle());
			}
		});
	}

	@Override
	protected IAbstractBean getKeyObject() {
		return richtext;
	}
}

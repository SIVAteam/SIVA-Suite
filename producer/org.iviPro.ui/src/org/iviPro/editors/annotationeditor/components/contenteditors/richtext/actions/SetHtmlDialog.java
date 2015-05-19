/*******************************************************************************
 * Copyright (c) 2007 Tom Seidel, Spirit Link GmbH
 * All rights reserved.
 * 
 * Contributors:
 *     Tom Seidel - initial API and implementation
 *******************************************************************************/
package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.actions;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author Tom Seidel <tom.seidel@spiritlink.de>
 * 
 */
public class SetHtmlDialog extends TitleAreaDialog {

    private String html = null;
    private Text htmlText;
    /**
     * @param parentShell
     */
    public SetHtmlDialog(Shell parentShell) {
        super(parentShell);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Insert HTML");
        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle("Insert HTML");
        setMessage("Type your html");
        Composite comp = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
        comp.setLayout(new GridLayout(1,false));
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        comp.setLayoutData(gd);
        
        Label srcLabel = new Label(comp, SWT.NONE);
        srcLabel.setText("HTML");
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        srcLabel.setLayoutData(gd);
        
        this.htmlText = new Text(comp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.htmlText.setLayoutData(gd);
        
        return comp;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        this.html = this.htmlText.getText();
        super.okPressed();
    }
    /**
     * @return the html
     */
    public String getHtml() {
        return this.html;
    }

}

package org.iviPro.editors.annotationeditor.components.positioneditors;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * ein Canvas, das als Overlay gelegt werden kann
 * auf dem Canvas wird die OverlayFigure gelegt und ein Content gezeichnet,
 * falls gewünscht also Bilder, Video etc.
 * @author juhoffma
 */
public class OverlayCanvas extends FigureCanvas {
	
    private LightweightSystem lws;
    private Composite contentComposite;
    
    // Begrenzung für die Figur, das Canvas kann größere sein trotzdem kann der 
    // Bereich der Figure eingegrenzt werden
    private Rectangle figureBounds;
    
    // gibt an ob der Content angezeigt werden soll, beim OverlayPfad Editor
    private boolean useContent;
        			
	public OverlayCanvas(Composite parent, int figMinX, int figMinY, int figMaxX, int figMaxY, boolean useContent) {
		super(parent, SWT.NONE);
		if (parent.getLayout() instanceof GridLayout) {
			GridData gd = new GridData();
			gd.widthHint = figMaxX;
			gd.heightHint = figMaxY;		
			setLayoutData(gd);
		}
		figureBounds = new Rectangle(figMinX, figMinY, figMaxX, figMaxY);

	    this.lws = new LightweightSystem(this);
	    this.useContent = useContent;
	    
	    // erstelle das Content Composite falls noch nicht passiert
		if (useContent) {
			setLayout(new GridLayout(1, false));
			contentComposite = new Composite(this, SWT.CENTER);
			contentComposite.setEnabled(false);
			contentComposite.setLayout(new GridLayout(1, false));
		}		
	}

	public OverlayCanvas(Composite parent, int figMinX, int figMinY, int figMaxX, int figMaxY) {
		this(parent, figMinX, figMinY, figMaxX, figMaxY, false);
	}
	
	public Rectangle getFigureBounds() {
		return this.figureBounds;
	}
	
	public Composite getContentComposite() {
		return this.contentComposite;
	}
		
	/**
	 * falls sie die Content Figure der OverlayFigure geändert hat 
	 * wird das Canvas benachrichtigt => wenn der Content angezeigt werden
	 * soll wird das Composite für den Content auf die entsprechende Position gesetzt
	 * @param figure
	 */
	public void notifyContentFigureChanged(IFigure figure) {
		if (useContent && contentComposite != null) {
			contentComposite.setBounds(figure.getBounds().x+4, figure.getBounds().y+4, 					
						               figure.getBounds().width-8, figure.getBounds().height-8);
		}
	}
	
	public void setFigure(OverlayFigure figure) {
		this.lws.setContents(figure);
	}	
}

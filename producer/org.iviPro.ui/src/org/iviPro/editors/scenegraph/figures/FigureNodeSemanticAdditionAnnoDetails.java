package org.iviPro.editors.scenegraph.figures;

import java.util.List;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPdf;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.theme.Icons;

/**
 * Klasse zur Anzeige von einzelnen Annotationen
 * (Verwendet in der sem. Zoomstufe 3)
 * @author grillc
 *
 */
public class FigureNodeSemanticAdditionAnnoDetails extends IFigureNode {

	public static Color classColor = new Color(null,240,240,240);
	public static Color BORDERCOLOR = new Color(null,180,180,180);
	public Font classFont = new Font(null, "Arial", 10, SWT.BOLD); //$NON-NLS-1$

	public FigureNodeSemanticAdditionAnnoDetails(Point pos) {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);	
		setBorder(new CustomBorder());
		setBackgroundColor(classColor);
		setOpaque(true);
		setLocation(pos);
		setSize(new Dimension(140,0));
	}

	/**
	 * Setzen der Annotation für die Figure
	 * 
	 * @param annotations Annotationen als Liste
	 */
	public void setAnnotations(List<INodeAnnotation> annotations) {
		removeAll();
		IFigure imgfig;
		Label label;
		int count = 0;

		Point pos = getLocation();
		pos.x += 5;
		pos.y += 5;
		//Sichere Anfangs x-Wert
		int xBegin = pos.x;

		//Unterscheidung zw. den einzelnen Annotationstyepn und ihrer Darstellung
		for(INodeAnnotation item: annotations) {

			if(item instanceof INodeAnnotationLeaf) {
				//Erhöhe y-Wert wenn neuer Eintrag
				if(count > 0) {
					pos.y += 21;
				}

				//Füge Typenbild der Annotation hinzu
				if(item instanceof NodeAnnotationPicture) {
					imgfig = new ImageFigure(Icons.OBJECT_MEDIA_PICTURE.getImage());
					imgfig.setSize(16, 16);
					imgfig.setLocation(pos);
					add(imgfig);
				}
				if(item instanceof NodeAnnotationVideo) {
					imgfig = new ImageFigure(Icons.OBJECT_MEDIA_VIDEO.getImage());
					imgfig.setSize(16, 16);
					imgfig.setLocation(pos);
					add(imgfig);
					
				}
				if(item instanceof NodeAnnotationAudio) {
					imgfig = new ImageFigure(Icons.OBJECT_MEDIA_AUDIO.getImage());
					imgfig.setSize(16, 16);
					imgfig.setLocation(pos);
					add(imgfig);
					
				}
				if(item instanceof NodeAnnotationRichtext) {
					imgfig = new ImageFigure(Icons.OBJECT_MEDIA_TEXT_RICH.getImage());
					imgfig.setSize(16, 16);
					imgfig.setLocation(pos);
					add(imgfig);
					
				}
				if(item instanceof NodeAnnotationSubtitle) {
					imgfig = new ImageFigure(Icons.OBJECT_MEDIA_TEXT_SUBTITLE.getImage());
					imgfig.setSize(16, 16);
					imgfig.setLocation(pos);
					add(imgfig);
				}
				if(item instanceof NodeAnnotationPdf) {
					imgfig = new ImageFigure(Icons.OBJECT_MEDIA_PDF.getImage());
					imgfig.setSize(16, 16);
					imgfig.setLocation(pos);
					add(imgfig);
				}
				
				//Füge Gitter und Titel der Annotation hinzu
				pos.x += 19;
				imgfig = getGraphAnnotationPositionImage(item, pos);
				imgfig.setSize(21, 16);
				add(imgfig);
				
				pos.x += 25;
				label = new Label(item.getTitle());
				label.setSize(80, 20);
				label.setLabelAlignment(Label.LEFT);
				label.setLocation(pos);
				add(label);	
				
				//Setze x-Wert zurück u. erhöhe Count
				pos.x = xBegin;
				count++;
			}
		}

		//Falls keine Annotationen
		if(count == 0) {
			label = new Label();
			label.setText(Messages.FigureNodeSemanticAdditionAnnoDetails_NoAnnotations);
			add(label);
			count++;
		}

		//Figure-Size an #Annotationen anpassen
		this.setSize(new Dimension(140,count * 21 + 8));
	}

	//Gibt das Positionsgitter der Annotation zurück
	private FigureScreenAreaSelector getGraphAnnotationPositionImage(INodeAnnotation annotation, Point pos) {
		ScreenArea position = annotation.getScreenArea();
		FigureScreenAreaSelector screenAreaSelector = new FigureScreenAreaSelector(pos, position);
		return screenAreaSelector;
	}
	
	/**
	 * Selbst erstellter Rahmen für die Figure
	 * @author grillc
	 *
	 */
	class CustomBorder extends AbstractBorder {
		public Insets getInsets(IFigure figure) {
			return new Insets(0,0,0,0);
		}
		public void paint(IFigure figure, Graphics graphics, Insets insets) {
			graphics.setLineWidth(2);
			graphics.setForegroundColor(BORDERCOLOR);
			graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), getPaintRectangle(figure, insets).getBottomLeft());			
			graphics.drawLine(getPaintRectangle(figure, insets).getTopRight(), getPaintRectangle(figure, insets).getBottomRight());
			graphics.drawLine(getPaintRectangle(figure, insets).getBottomLeft(), getPaintRectangle(figure, insets).getBottomRight());
		}
	}
}
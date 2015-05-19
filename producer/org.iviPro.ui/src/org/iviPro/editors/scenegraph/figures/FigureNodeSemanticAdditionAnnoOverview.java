package org.iviPro.editors.scenegraph.figures;

import java.util.List;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPdf;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.theme.Icons;

/**
 * Klasse zur Darstellung der #Annotationen, sortiert nach ihrem Typ
 *  (Verwendet in der sem. Zoomstufe 2)
 * @author grillc
 *
 */
public class FigureNodeSemanticAdditionAnnoOverview extends IFigureNode {

	public static Color classColor = new Color(null,240,240,240);
	public static Color BORDERCOLOR = new Color(null,180,180,180);
	public Font classFont = new Font(null, "Arial", 10, SWT.BOLD); //$NON-NLS-1$

	ImageFigure imageIcon;
	ImageFigure videoIcon;
	ImageFigure textIcon;
	ImageFigure audioIcon;
	ImageFigure subtitleIcon;
	ImageFigure pdfIcon;
	Label imageLabel;
	Label videoLabel;
	Label textLabel;
	Label audioLabel;
	Label subtitleLabel;
	Label pdfLabel;

	public FigureNodeSemanticAdditionAnnoOverview(Point pos) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 6;
		setLayoutManager(layout);	
		setBorder(new CustomBorder());
		setBackgroundColor(classColor);
		setOpaque(true);
		setLocation(pos);
		setSize(new Dimension(140,50));

		//Pictures
		imageIcon = new ImageFigure(Icons.OBJECT_MEDIA_PICTURE.getImage());
		imageIcon.setSize(40, 40);
		add(imageIcon);
		imageLabel = new Label();
		imageLabel.setText("[" + 0 + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		add(imageLabel);

		//Video
		videoIcon = new ImageFigure(Icons.OBJECT_MEDIA_VIDEO.getImage());
		videoIcon.setSize(40, 40);
		add(videoIcon);
		videoLabel = new Label();
		videoLabel.setText("[" + 0 + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		add(videoLabel);

		//Audio
		audioIcon = new ImageFigure(Icons.OBJECT_MEDIA_AUDIO.getImage());
		audioIcon.setSize(40, 40);
		add(audioIcon);
		audioLabel = new Label();
		audioLabel.setText("[" + 0 + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		add(audioLabel);

		//Richtext
		textIcon = new ImageFigure(Icons.OBJECT_MEDIA_TEXT_RICH.getImage());
		textIcon.setSize(40, 40);
		add(textIcon);
		textLabel = new Label();
		textLabel.setText("[" + 0 + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		add(textLabel);

		//Subtitle
		subtitleIcon = new ImageFigure(Icons.OBJECT_MEDIA_TEXT_SUBTITLE.getImage());
		subtitleIcon.setSize(40, 40);
		add(subtitleIcon);
		subtitleLabel = new Label();
		subtitleLabel.setText("[" + 0 + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		add(subtitleLabel);
		
		//Pdf
		pdfIcon = new ImageFigure(Icons.OBJECT_MEDIA_PDF.getImage());
		pdfIcon.setSize(40, 40);
		add(pdfIcon);
		pdfLabel = new Label();
		pdfLabel.setText("[" + 0 + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		add(pdfLabel);
	}

	/**
	 * Setzen der Annotationstypen
	 * 
	 * @param annotations Annotationen als Liste
	 */
	public void setAnnotationTypes(List<INodeAnnotation> annotations) {
		int picCount = 0;
		int vidCount = 0;
		int audCount = 0;
		int txtCount = 0;
		int subCount = 0;
		int pdfCount = 0;

		for(INodeAnnotation item: annotations) {

			if(item instanceof NodeAnnotationPicture) {
				picCount++;
			}
			if(item instanceof NodeAnnotationVideo) {
				vidCount++;
			}
			if(item instanceof NodeAnnotationAudio) {
				audCount++;
			}
			if(item instanceof NodeAnnotationRichtext) {
				txtCount++;
			}
			if(item instanceof NodeAnnotationSubtitle) {
				subCount++;
			}
			if(item instanceof NodeAnnotationPdf) {
				pdfCount++;
			}
		}

		//Pictures
		imageLabel.setText("[" + picCount + "]"); //$NON-NLS-1$ //$NON-NLS-2$

		//Video
		videoLabel.setText("[" + vidCount + "]"); //$NON-NLS-1$ //$NON-NLS-2$

		//Audio
		audioLabel.setText("[" + audCount + "]"); //$NON-NLS-1$ //$NON-NLS-2$

		//Richtext
		textLabel.setText("[" + txtCount + "]"); //$NON-NLS-1$ //$NON-NLS-2$

		//Subtitle
		subtitleLabel.setText("[" + subCount + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Subtitle
		pdfLabel.setText("[" + pdfCount + "]"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Selbst erstellter Rahmen für die Figure
	 * @author grillc
	 *
	 */
	public class CustomBorder extends AbstractBorder {
		public Insets getInsets(IFigure figure) {
			return new Insets(0,0,0,0);
		}
		public void paint(IFigure figure, Graphics graphics, Insets insets) {
			graphics.setLineWidth(2);
			graphics.setForegroundColor(BORDERCOLOR);
			graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), getPaintRectangle(figure, insets).getTopRight());
			graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), getPaintRectangle(figure, insets).getBottomLeft());			
			graphics.drawLine(getPaintRectangle(figure, insets).getTopRight(), getPaintRectangle(figure, insets).getBottomRight());
			graphics.drawLine(getPaintRectangle(figure, insets).getBottomLeft(), getPaintRectangle(figure, insets).getBottomRight());
		}
	}
}
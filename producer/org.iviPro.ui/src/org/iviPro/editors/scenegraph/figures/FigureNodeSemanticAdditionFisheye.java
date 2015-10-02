package org.iviPro.editors.scenegraph.figures;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.SivaImage;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPdf;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.theme.Icons;
import org.iviPro.utils.Html2TextParser;
import org.iviPro.utils.ImageHelper;

/**
 * Klasse zur Erstellung der semantischen Fisheye/Tooltip Figure
 * @author grillc
 *
 */
public class FigureNodeSemanticAdditionFisheye extends IFigureNode {

	public static final Color DARKORANGE = new Color(Display.getDefault(),
			243, 201, 123);
	public static final Color LIGHTORANGE = new Color(Display.getDefault(),
			253, 222, 134);
	public static final Color LIGHTGREY = new Color(Display.getDefault()
			,247,247,247);
	public static final Color MIDDLEGREY = new Color(Display.getDefault(),
			150,150,150);
	public static final Color DARKGREY = new Color(Display.getDefault(),
			28,28,28);
	public Font classFont = new Font(null, "Arial", 10, SWT.BOLD); //$NON-NLS-1$

	public FigureNodeSemanticAdditionFisheye() {
		setBorder(new LineBorder(MIDDLEGREY,1));
		setOpaque(true);
		setSize(new Dimension(260,45));
	}

	//Setzen des Szenentitel
	public void setTitle(String title) {
		Label label = new Label();
		label.setForegroundColor(DARKGREY);
		label.setFont(classFont);
		label.setText(title);
		label.setSize(260, 30);
		add(label);
	}

	//Setzen einzelner Bilder mit Breite/Höhe ausgehend vom linken oberen Eck des Tooltips
	private void setImage(Image img, Dimension OFFSET, int width, int height) {
		ImageFigure imgfig = new ImageFigure(img);
		imgfig.setSize(width, height);
		Point pos = getLocation();
		pos.x += OFFSET.width;
		pos.y += OFFSET.height;
		imgfig.setLocation(pos);
		add(imgfig);
	}

	
	/**
	 * Setzen der Framevorschau
	 * @param img Vorschaubild
	 */
	public void setTransitionPreviewFrame(Image img) {
		Dimension OFFSET = new Dimension(0,30);
		setImage(img, OFFSET, 260, 120);
	}

	/**
	 * Setzen der Annotationen
	 * @param annotations Annotationen als Liste
	 */
	public void setAnnotations(List<INodeAnnotation> annotations) {
		
		//Initialisierung des Bildcontainers
		int imgWidth = 40;
		int imgHeight = 40;
		GridLayout imgLayout = new GridLayout();
		imgLayout.numColumns = 5;
		imgLayout.makeColumnsEqualWidth = true;
		imgLayout.horizontalSpacing = 10;
		imgLayout.verticalSpacing = 5;
		imgLayout.marginWidth = 10;
		imgLayout.marginHeight = 5;
		Figure imgContainer = new Figure();
		imgContainer.setLayoutManager(imgLayout);
		imgContainer.setSize(260, imgHeight + 2 + imgLayout.marginHeight * 2); //+2 wegen Border oben/unten
		imgContainer.setBorder(new CustomLineBorder());
		int imgCount = 0;

		//Initialisierung des Videocontainers
		int videoWidth = 80;
		int videoHeight = 40;
		GridLayout videoLayout = new GridLayout();
		videoLayout.numColumns = 3;
		videoLayout.makeColumnsEqualWidth = true;
		videoLayout.horizontalSpacing = 10;
		videoLayout.verticalSpacing = 5;
		videoLayout.marginWidth = 10;
		videoLayout.marginHeight = 5;
		Figure videoContainer = new Figure();
		videoContainer.setLayoutManager(videoLayout);
		videoContainer.setSize(260, videoHeight + videoLayout.marginHeight * 2);
		videoContainer.setBorder(new CustomLineBorder());
		int videoCount = 0;

		//Initialisierung des Textcontainers
		GridLayout textLayout = new GridLayout();
		textLayout.numColumns = 2;
		textLayout.makeColumnsEqualWidth = false;
		textLayout.horizontalSpacing = 3;
		textLayout.verticalSpacing = 3;
		textLayout.marginWidth = 10;
		textLayout.marginHeight = 3;
		Figure textContainer = new Figure();
		textContainer.setLayoutManager(textLayout);
		textContainer.setSize(260, textLayout.marginHeight + textLayout.verticalSpacing);
		textContainer.setBorder(new CustomLineBorder());
		int textCount = 0;

		//Initialisierung des Untertitelcontainers
		GridLayout subtitleLayout = new GridLayout();
		subtitleLayout.numColumns = 2;
		subtitleLayout.makeColumnsEqualWidth = false;
		subtitleLayout.horizontalSpacing = 3;
		subtitleLayout.verticalSpacing = 3;
		subtitleLayout.marginWidth = 10;
		subtitleLayout.marginHeight = 3;
		Figure subtitleContainer = new Figure();
		subtitleContainer.setLayoutManager(subtitleLayout);
		subtitleContainer.setSize(260, subtitleLayout.marginHeight + subtitleLayout.verticalSpacing);
		subtitleContainer.setBorder(new CustomLineBorder());
		int subtitleCount = 0;

		//Initialisierung des Audiocontainers
		GridLayout audioLayout = new GridLayout();
		audioLayout.numColumns = 2;
		audioLayout.makeColumnsEqualWidth = false;
		audioLayout.horizontalSpacing = 3;
		audioLayout.verticalSpacing = 3;
		audioLayout.marginWidth = 10;
		audioLayout.marginHeight = 3;
		Figure audioContainer = new Figure();
		audioContainer.setLayoutManager(audioLayout);
		audioContainer.setSize(260, audioLayout.marginHeight + audioLayout.verticalSpacing);
		audioContainer.setBorder(new CustomLineBorder());
		int audioCount = 0;
		
		// Pdf container
		GridLayout pdfLayout = new GridLayout();
		pdfLayout.numColumns = 2;
		pdfLayout.makeColumnsEqualWidth = false;
		pdfLayout.horizontalSpacing = 3;
		pdfLayout.verticalSpacing = 3;
		pdfLayout.marginWidth = 10;
		pdfLayout.marginHeight = 3;
		Figure pdfContainer = new Figure();
		pdfContainer.setLayoutManager(pdfLayout);
		pdfContainer.setSize(260, pdfLayout.marginHeight + pdfLayout.verticalSpacing);
		pdfContainer.setBorder(new CustomLineBorder());
		int pdfCount = 0;

		Image img = null;
		ImageFigure imgfig = null;

		for(INodeAnnotation item: annotations) {
			
			//Setzen der Bildelemente innerhalb des Containers
			if(item instanceof NodeAnnotationPicture) {
				if(imgCount == 0) {
					add(imgContainer);
				}
				img = ((NodeAnnotationPicture) item).getPicture().getImage();
				img = ImageHelper.getScaledImage(img, new java.awt.Dimension(imgWidth,imgWidth), true);
				imgfig = new ImageFigure(img);
				imgfig.setSize(imgWidth, imgHeight);
				imgfig.setBorder(new LineBorder());
				imgContainer.add(imgfig);
				imgCount++;

				//Container-Resizing
				if(imgCount > imgLayout.numColumns) {
					imgCount = 1;
					imgContainer.setSize(260, imgContainer.getSize().height + (imgHeight + 2 + imgLayout.verticalSpacing)); //+2 wegen Border oben/unten
				}
			}
			//Setzen der Videoelemente innerhalb des Containers
			if(item instanceof NodeAnnotationVideo) {
				if(videoCount == 0) {
					add(videoContainer);
				}
				LinkedList<SivaImage> previewFrames = ((NodeAnnotationVideo) item).getImages();
				if(previewFrames.get(0).getBufferedImage() != null && previewFrames.get(1).getBufferedImage() != null) {
					BufferedImage scaledBufimgStart = ImageHelper.getScaledImage(previewFrames.get(0).getBufferedImage(), new java.awt.Dimension(80,80), true);
					BufferedImage scaledBufimgEnd = ImageHelper.getScaledImage(previewFrames.get(1).getBufferedImage(), new java.awt.Dimension(80,80), true);
					Image transitionImg = ImageHelper.transition(scaledBufimgStart, scaledBufimgEnd, 100, LIGHTGREY);
					imgfig = new ImageFigure(ImageHelper.getScaledImage(transitionImg, new java.awt.Dimension(videoWidth,videoHeight), true));
				}
				imgfig.setSize(videoWidth, videoHeight);
				videoContainer.add(imgfig);
				videoCount++;

				//Container-Resizing
				if(videoCount > videoLayout.numColumns) {
					videoCount = 1;
					videoContainer.setSize(260, videoContainer.getSize().height + (videoHeight + videoLayout.verticalSpacing));
				}

			}
			//Setzen der Textelemente innerhalb des Containers
			if(item instanceof NodeAnnotationRichtext) {
				if(textCount == 0) {
					add(textContainer);
				}
				//Richtext-Icon
				img = Icons.OBJECT_MEDIA_TEXT_RICH.getImage();
				imgfig = new ImageFigure(img);
				textContainer.add(imgfig);
				//Annotationstextanfang
				String text = ""; //$NON-NLS-1$
				try {
					Html2TextParser html2text = new Html2TextParser();
					File annotationTextFile = ((NodeAnnotationRichtext) item).getRichtext().getFile().getValue();
					text = html2text.parse(new FileReader(annotationTextFile));
					text = this.getTruncatedText(text, classFont, 240, false);
				} catch (Exception e) {

				}
				Label label = new Label();
				label.setText(text);
				textContainer.add(label);
				textCount++;

				//Container-Resizing
				textContainer.setSize(260, textContainer.getSize().height + 19);
			}
			//Setzen der Untertitelelemente innerhalb des Containers
			if(item instanceof NodeAnnotationSubtitle) {
				if(subtitleCount == 0) {
					add(subtitleContainer);
				}
				//Subtitle-Icon
				img = Icons.OBJECT_MEDIA_TEXT_SUBTITLE.getImage();
				imgfig = new ImageFigure(img);
				subtitleContainer.add(imgfig);
				//Annotationstitelanfang
				String text = ""; //$NON-NLS-1$
				LocalizedString localizedString = ((NodeAnnotationSubtitle) item).getLocalizedTitle();
				if(localizedString != null) {
					text = localizedString.getValue();
				}
				Label label = new Label();
				label.setText(text);
				subtitleContainer.add(label);
				subtitleCount++;

				//Container-Resizing
				subtitleContainer.setSize(260, subtitleContainer.getSize().height + 19);
			}
			//Setzen der Audioelemente innerhalb des Containers
			if(item instanceof NodeAnnotationAudio) {
				if(audioCount == 0) {
					add(audioContainer);
				}
				//Audio-Icon
				img = Icons.OBJECT_MEDIA_AUDIO.getImage();
				imgfig = new ImageFigure(img);
				audioContainer.add(imgfig);
				//Annotationstitelanfang
				String text = ""; //$NON-NLS-1$
				LocalizedString localizedString = ((NodeAnnotationAudio) item).getLocalizedTitle();
				if(localizedString != null) {
					text = localizedString.getValue();
				}
				Label label = new Label();
				label.setText(text);
				audioContainer.add(label);
				audioCount++;

				//Container-Resizing
				audioContainer.setSize(260, audioContainer.getSize().height + 19);
			}
			
			// Pdf elements
			if(item instanceof NodeAnnotationPdf) {
				if(pdfCount == 0) {
					add(pdfContainer);
				}
				//Audio-Icon
				img = Icons.OBJECT_MEDIA_PDF.getImage();
				imgfig = new ImageFigure(img);
				pdfContainer.add(imgfig);
				//Annotationstitelanfang
				String text = ""; //$NON-NLS-1$
				LocalizedString localizedString = ((NodeAnnotationPdf) item).getLocalizedTitle();
				if(localizedString != null) {
					text = localizedString.getValue();
				}
				Label label = new Label();
				label.setText(text);
				pdfContainer.add(label);
				pdfCount++;

				//Container-Resizing
				pdfContainer.setSize(260, pdfContainer.getSize().height + 19);
			}
		}

		//Positionierung der befüllten Container
		//Starten bei Breite 0px, Höhe 160px
		Point pos = getLocation();
		pos.y += 160;
		if(imgCount != 0) {
			imgContainer.setLocation(pos);
			pos.y += imgContainer.getSize().height;
		}
		if(videoCount != 0) {
			videoContainer.setLocation(pos);
			pos.y += videoContainer.getSize().height;
		}
		if(textCount != 0) {
			textContainer.setLocation(pos);
			pos.y += textContainer.getSize().height;
		}
		if(subtitleCount != 0) {
			subtitleContainer.setLocation(pos);
			pos.y += subtitleContainer.getSize().height;
		}
		if(audioCount != 0) {
			audioContainer.setLocation(pos);
			pos.y += audioContainer.getSize().height;
		}
		if(pdfCount != 0) {
			pdfContainer.setLocation(pos);
			pos.y += pdfContainer.getSize().height;
		}

		//Figure-Size an #Annotationen anpassen
		this.setSize(new Dimension(260,pos.y));
	}

	@Override
	protected void paintFigure(Graphics g) {
		//Tooltip bekommt Farbverlauf
		AdvancedPath path = new AdvancedPath();
		path.addRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
		g.setClip(path);

		g.setBackgroundColor(DARKORANGE);
		g.setForegroundColor(LIGHTORANGE);
		g.fillGradient(bounds, true);
	}

	/**
	 * Selbst erstellter Rahmen für die Figure
	 * @author grillc
	 *
	 */
	public class CustomLineBorder extends AbstractBorder {

		public Insets getInsets(IFigure figure) {
			return new Insets(0,0,0,0);
		}

		public void paint(IFigure figure, Graphics graphics, Insets insets) {
			graphics.setLineWidth(2);
			graphics.setForegroundColor(MIDDLEGREY);
			graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(), getPaintRectangle(figure, insets).getTopRight());	
		}
	}
}


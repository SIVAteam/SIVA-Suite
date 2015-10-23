package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.iviPro.application.Application;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.resources.Scene;
import org.iviPro.theme.Icons;
import org.iviPro.views.scenerepository.SceneRepository;

/**
 * Figure zur Anzeige von Videoszenen
 * @author fana
 *
 */
public class FigureNodeSceneContents extends IFigureNode {

	private static final Color DARKBLACK = new Color(Display.getDefault(),
			48, 48, 48);
	private static final Color LIGHTBLACK = new Color(Display.getDefault(),
			96, 96, 96);
	private static final Color DARKORANGE = new Color(Display.getDefault(),
			245, 180, 65);
	private static final Color LIGHTORANGE = new Color(Display.getDefault(),
			255, 205, 55);

	private static final Color BORDER = ColorConstants.gray;
	private static final Color FONT_COLOR_WHITE = ColorConstants.white;
	private static final Color FONT_COLOR_BLACK = ColorConstants.black;

	private static final Dimension SIZE = new Dimension(140, 40);
	private static final int CORNER_RADIUS = 10;
	private static final int SEPARATOR = 2;
	

	private static final Font TITLE_FONT = new Font(Display.getDefault(),
			new FontData("Sans serif", 10, SWT.BOLD)); //$NON-NLS-1$

	private String text = ""; //$NON-NLS-1$
	private int annotationCount = 0;

	// für die Markierung und das setzen der Selekion im Szenenrepositorium
	private boolean isSelected;
	// die zur Figure gehörende Scene
	private Scene scene;
	private NodeScene nodeScene;

	//zoomlevelinformation zum zeichnen der zoomstufen-punkte
	private int zoomlevel;
	private Image graph_zoom_white_one = Icons.GRAPH_ZOOM_WHITE_ONE.getImage();
	private Image graph_zoom_white_two = Icons.GRAPH_ZOOM_WHITE_TWO.getImage();
	private Image graph_zoom_white_three = Icons.GRAPH_ZOOM_WHITE_THREE.getImage();
	private Image graph_zoom_black_one = Icons.GRAPH_ZOOM_BLACK_ONE.getImage();
	private Image graph_zoom_black_two = Icons.GRAPH_ZOOM_BLACK_TWO.getImage();
	private Image graph_zoom_black_three = Icons.GRAPH_ZOOM_BLACK_THREE.getImage();
	
	private Button annoEditorButton;

	public FigureNodeSceneContents(final NodeScene nodeScene, int annotationCount) {
		this.nodeScene = nodeScene;
		setText(nodeScene.getTitle());
		this.annotationCount = annotationCount;
		setLocation(nodeScene.getPosition());
		setSize(SIZE);
		setLayoutManager(new XYLayout());
		annoEditorButton = createAnnoEditorButton();
		add(annoEditorButton);


		// setze den Listener auf das Szenenrep und markiere die Figures, 
		// die zur gewählten Szene gehören
		IViewPart rep = Application.getDefault().getView(SceneRepository.ID);
		if(rep != null) {
			final SceneRepository sRep = (SceneRepository) rep;
			if (sRep != null) { 
				sRep.addSelectionListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent arg0) {
						Scene selectedScene = sRep.getCurrentSelection();					
						if (selectedScene != null && scene != null
								&& scene.getVideo().equals(selectedScene.getVideo())
								&& scene.getTitle().equals(selectedScene.getTitle())) {
							isSelected = true;
						} else {
							isSelected = false;
						}
						repaint();
					}					
				});						
			}
		}
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	private Image getAnnoEditorButtonImage() {
		if (annotationCount > 0) {
			return Icons.GRAPH_ANNOTATIONS_EXISTENT.getImage();
		} else {
			return Icons.GRAPH_ANNOTATIONS_NONEXISTENT.getImage();
		}
	}

	private Button createAnnoEditorButton() {
		Image img = getAnnoEditorButtonImage();
		Dimension imgSize = new Dimension(img.getBounds().width, img
				.getBounds().height);
		Button button = new Button(img);
		button.setBorder(new MarginBorder(0));
		button.setOpaque(false);
		button.setRolloverEnabled(true);
		getLayoutManager().setConstraint(
				button,
				new Rectangle(CORNER_RADIUS - 4, CORNER_RADIUS - 6, imgSize.width,
						imgSize.height));
		return button;
	}

	public void addAnnotateButtonActionListener(ActionListener listener) {
		annoEditorButton.addActionListener(listener);
	}

	@Override
	protected void paintFigure(Graphics g) {
		if(zoomlevel < 1) {
			zoomlevel = 1;
		}
		// Kreis mit Gradient Fuellung zeichnen
		AdvancedPath path = new AdvancedPath();
		if(zoomlevel == 1) {
			path.addRoundRectangle(bounds, CORNER_RADIUS); //rund
		} else {
			path.addHalfRoundRectangle(bounds, CORNER_RADIUS); //halb eckig
		}
		//path.addRectangle(bounds.x, bounds.y, bounds.width, bounds.height); //eckig
		g.setClip(path);

		if (!isSelected) {
			g.setBackgroundColor(DARKBLACK);
			g.setForegroundColor(LIGHTBLACK);
		} else {
			g.setBackgroundColor(DARKORANGE);
			g.setForegroundColor(LIGHTORANGE);
		}
		g.fillGradient(bounds, true);

		// Rahmen zeichnen
		g.setForegroundColor(BORDER);
		g.setClip(getBounds());
		g.setAntialias(SWT.ON);
		Rectangle drawBounds = new Rectangle(bounds);
		drawBounds.width = drawBounds.width - 1;
		drawBounds.height = drawBounds.height - 1;
		if(zoomlevel == 1) {
			g.drawRoundRectangle(drawBounds, CORNER_RADIUS, CORNER_RADIUS); //rund
		} else {
			path.addHalfRoundRectangle(drawBounds, CORNER_RADIUS); //halb eckig
			g.drawPath(path); //halb eckig
		}
		//g.drawRectangle(drawBounds); //eckig

		// Titel zeichnen
		if(!isSelected) {
			g.setForegroundColor(FONT_COLOR_WHITE);
		} else {
			g.setForegroundColor(FONT_COLOR_BLACK);
		}
		g.setFont(TITLE_FONT);
		int indent = bounds.x + 30 + CORNER_RADIUS;
		int vertIndent = bounds.y + CORNER_RADIUS + SEPARATOR;
		g.drawText(getTruncatedText(text, TITLE_FONT, bounds.width - 30 - 2
				* CORNER_RADIUS, false), indent, vertIndent);
		
		// Zoompunkte zeichnen
		vertIndent += getFontHeight(TITLE_FONT) + SEPARATOR;
		indent = bounds.x + CORNER_RADIUS + 32;
		g.drawImage(getZoomlevelpointsIcon(), indent, vertIndent);

		super.paintFigure(g);
	}

	public void setText(String text) {
		this.text = text;
		repaint();
	}

	public void setAnnotationCount(int annotationCount) {
		this.annotationCount = annotationCount;
		Label label = (Label) annoEditorButton.getChildren().get(0);
		label.setIcon(getAnnoEditorButtonImage());
		repaint();
	}

	public void setZoomlevelPoints(int zoomlevel) {
		this.zoomlevel = zoomlevel;
		repaint();
	}

	public Image getZoomlevelpointsIcon() {
		if(!isSelected) {
			switch(zoomlevel){
			case 1:
				return graph_zoom_white_one;
			case 2:
				return graph_zoom_white_two;
			case 3:
				return graph_zoom_white_three;
			}
		} else {
			switch(zoomlevel){
			case 1:
				return graph_zoom_black_one;
			case 2:
				return graph_zoom_black_two;
			case 3:
				return graph_zoom_black_three;
			}
		}
		//default case
		return graph_zoom_white_one;
	}
}


package org.iviPro.editors.scenegraph.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.ui.IViewPart;
import org.iviPro.application.Application;
import org.iviPro.model.resources.Scene;
import org.iviPro.views.scenerepository.SceneRepository;

/**
 * Container-Figure für Videoszenen (Parent)
 * (Inhalt der Videoszene befindet sich in FigureNodeSceneContents)
 * @author fana
 *
 */
public class FigureNodeScene extends IFigureNode {

	private static final Dimension SIZE = new Dimension(140, 40);
	
	// die zur Figure gehörende Scene
	private Scene scene;
	
	public FigureNodeScene(Point pos) {
		setLocation(pos);
		setSize(SIZE);
		setLayoutManager(new XYLayout());
		
		//Zum Hervorheben einer Szene im Szenenrepository
		IViewPart rep = Application.getDefault().getView(SceneRepository.ID);
		if (rep != null) {
			final SceneRepository sRep = (SceneRepository) rep;					
			addMouseListener(new MouseListener() {
				@Override
				public void mouseDoubleClicked(MouseEvent arg0) {
				}
	
				@Override
				public void mousePressed(MouseEvent arg0) {			
					sRep.selectScene(scene);
				}
	
				@Override
				public void mouseReleased(MouseEvent arg0) {
				}			
			});
		}
	}
	
	public void setScene(Scene scene) {
		this.scene = scene;
	}

	@Override
	protected void paintFigure(Graphics g) {
		super.paintFigure(g);
	}

}
package org.iviPro.editors.scenegraph.layout.algo.FNOR;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

public class QPRectanglePlacement extends Observable implements
RectanglePlacement, Observer {
	static Logger logger = Logger.getLogger(QPRectanglePlacement.class
			.getName());

	private boolean animate;

	private Hashtable<Rectangle2D, Color> rectangleColourMap;

	private boolean splitRefinement;

	public ConstraintGenerator constraintGenerator;

	/**
	 * @param completeConstraints
	 * @param animate
	 */
	public QPRectanglePlacement(double xgap,
			double ygap) {
		this.constraintGenerator = new ConstraintGenerator();
		XChunk.g = xgap;
		YChunk.g = ygap;
	}

	void placeX(ArrayList<Rectangle2D> rectangles,
			Hashtable<Rectangle2D, Color> colourMap) {

		rectangleColourMap = colourMap;

		XChunk[] xs = new XChunk[rectangles.size()];
		logger.fine("*****************Placing X");
		for (int i = 0; i < rectangles.size(); i++) {
			Rectangle2D r = rectangles.get(i);
			xs[i] = new XChunk(r, new YChunk(r));
		}
		constraintGenerator.initVarsAndConstraints(xs,false);
		setChanged();
		notifyObservers();

		placement();
	}

	void place(ArrayList<Rectangle2D> rectangles,
			Hashtable<Rectangle2D, Color> colourMap) {

		rectangleColourMap = colourMap;
		XChunk.g += 0.01;
		placeX(rectangles, colourMap);
		XChunk.g -= 0.01;
		placeY(rectangles, colourMap);

	}

	void placeY(ArrayList<Rectangle2D> rectangles,
			Hashtable<Rectangle2D, Color> colourMap) {
		YChunk[] ys = new YChunk[rectangles.size()];
		logger.fine("*****************Placing Y");
		for (int i = 0; i < rectangles.size(); i++) {
			Rectangle2D r = rectangles.get(i);
			ys[i] = new YChunk(r, new XChunk(r));
		}
		constraintGenerator.initVarsAndConstraints(ys,true);
		setChanged();
		notifyObservers();
		placement();
	}

	double placement() {
		double cost = 0;
		Chunk[] chunks=constraintGenerator.getChunks();
		Variable[] vs = new Variable[chunks.length];
		for(int i=0;i<chunks.length;i++) vs[i]=chunks[i].v;
		Placement p = null;

		p = new ActiveSetPlacement(vs);
		((ActiveSetPlacement) p).split = splitRefinement;
		((ActiveSetPlacement) p).debugAnimation = animate;
		((ActiveSetPlacement) p).addObserver(this);

		for (Constraint c : constraintGenerator.getConstraints()) {
			p.addConstraint(c.left.name, c.right.name, c.separation);
		}
		try {
			cost = p.solve();
			/*
			 * if (splitRefinement) { p = new MosekPlacement(p.getVariables(),
			 * p.getConstraints()); double mcost = p.solve();
			 * System.out.println("cost=" + cost + ", mcost=" + mcost); assert
			 * (2 * Math.abs(cost - mcost) / (1 + mcost + cost) < 0.001) :
			 * "Solver did not find optimal solution!"; }
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Chunk c : chunks) {
			c.setMin(c.v.getPosition());
		}
		return cost;
	}

	public void addObserver(Observer o) {
		super.addObserver(o);
	}

	public void update(Observable arg0, Object arg1) {
		for (Chunk c : constraintGenerator.getChunks()) {
			c.setMin(c.v.getPosition());
			if (c.v.colour != null)
				rectangleColourMap.put(c.rect, c.v.colour);
		}
		setChanged();
		notifyObservers();
	}

	public void place(ArrayList<Rectangle2D> rectangles) {
		place(rectangles, null);
	}

}


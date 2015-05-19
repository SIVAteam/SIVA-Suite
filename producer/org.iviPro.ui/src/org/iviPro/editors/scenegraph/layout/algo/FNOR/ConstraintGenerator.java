package org.iviPro.editors.scenegraph.layout.algo.FNOR;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ConstraintGenerator {

	private Chunk[] chunks;
	private Constraints constraints;
	boolean allOverlaps;

	void initVarsAndConstraints(Chunk[] chunks, boolean allOverlaps) {
		this.chunks=chunks;
		this.allOverlaps=allOverlaps;
		constraints=new Constraints();

		initVarsAndConstraintsComplete();
	}

	// n^2 time and potentially n^2 constraints
	void initVarsAndConstraintsComplete() {
		Arrays.sort(chunks, Chunk.comparator);
		for (int i = 0; i < chunks.length; i++) {
			Chunk r = chunks[i];
			r.v = new Variable("r" + i, r.getMin(), 1.0);
			r.v.data.put(Chunk.class, r);
			r.v.data.put(Rectangle2D.class, r.rect);
		}
		constraints = new Constraints();
		for (int i = 0; i < chunks.length - 1; i++) {
			Chunk l = chunks[i];
			for (int j = i + 1; j < chunks.length; j++) {
				Chunk r = chunks[j];
				if (needConstraint(l, r, allOverlaps)) {
					Variable vl = l.v;
					Variable vr = r.v;
					constraints.add(new Constraint(vl, vr, l.getLength()));
				}
			}
		}
	}

	/**
	 * test if there needs to be a constraint between l and r if: - we are
	 * removing all overlaps and there is overlap in the conjugate axis - there
	 * is overlap in the conjugate axis but not in this axis - there is overlap
	 * in this axis and that overlap is less than that in the conjugate
	 * 
	 * @param l
	 *            leftChunk
	 * @param r
	 *            rightChunk
	 * @param all
	 *            true if we need to remove all overlaps
	 */
	boolean needConstraint(Chunk l, Chunk r, boolean all) {
		boolean overlap = l.overlap(r) > 0;
		boolean conjOverlap = l.conjugateOverlap(r) > 0;
		boolean conjOverlapGreaterThanThis = l.overlap(r) > 0
				&& l.overlap(r) < l.conjugateOverlap(r);
		if (all && conjOverlap)
			return true;
		if (conjOverlap && !overlap)
			return true;
		if (overlap && conjOverlapGreaterThanThis)
			return true;
		return false;
	}

	public ConstraintGenerator() {
	}

	public Constraints getConstraints() {
		return constraints;
	}

	public Chunk[] getChunks() {
		return chunks;
	}

}

abstract class Chunk<T extends Chunk> {
	T conj;

	Rectangle2D rect;

	Variable v;

	Chunk leftNeighbour;

	Chunk rightNeighbour;

	ArrayList<Chunk> leftNeighbours = new ArrayList<Chunk>();

	ArrayList<Chunk> rightNeighbours = new ArrayList<Chunk>();

	abstract double getMax();

	abstract double getMin();

	abstract void setMin(double d);

	abstract double getLength();

	Chunk(Rectangle2D r) {
		this.rect = r;
	}

	Chunk(Rectangle2D r, T conjugate) {
		this.rect = r;
		this.conj = conjugate;
	}

	void addLeftNeighbour(Chunk n) {
		if (!leftNeighbours.contains(n)) {
			leftNeighbours.add(n);
		}
	}

	void addRightNeighbour(Chunk n) {
		if (!rightNeighbours.contains(n)) {
			rightNeighbours.add(n);
		}
	}

	void setNeighbours(ArrayList<Chunk> leftv, ArrayList<Chunk> rightv) {
		leftNeighbours = leftv;
		for (Chunk u : leftv) {
			u.addRightNeighbour(this);
		}
		rightNeighbours = rightv;
		for (Chunk u : rightv) {
			u.addLeftNeighbour(this);
		}
	}

	double overlap(Chunk b) {
		assert (b.getClass() == this.getClass());
		if (getMin() < b.getMin() && b.getMin() < getMax())
			return getMax() - b.getMin();
		if (b.getMin() < getMin() && getMin() < b.getMax())
			return b.getMax() - getMin();
		return 0;
	}

	double conjugateOverlap(Chunk c) {
		T a = conj;
		T b = (T) c.conj;
		assert (c.getClass() == this.getClass());
		if (a.getMin() <= b.getMin() && b.getMin() < a.getMax())
			return a.getMax() - b.getMin();
		if (b.getMin() <= a.getMin() && a.getMin() < b.getMax())
			return b.getMax() - a.getMin();
		return 0;
	}

	static Comparator<Chunk> comparator = new Comparator<Chunk>() {
		public int compare(Chunk a, Chunk b) {
			if (a.getMin() > b.getMin())
				return 1;
			if (a.getMin() < b.getMin())
				return -1;
			Chunk ac = a.conj;
			Chunk bc = b.conj;
			if (ac.getMin() > bc.getMin())
				return 1;
			if (ac.getMin() < bc.getMin())
				return -1;
			if (a == b && ac == bc)
				return 0;
			// Having identical coords, due to int rounding or something, is not
			// good!
			a.setMin(a.getMin() + 0.000001);
			ac.setMin(ac.getMin() + 0.000001);
			return 1;
		}
	};

	static Comparator<Chunk> conjComparator = new Comparator<Chunk>() {
		public int compare(Chunk a, Chunk b) {
			Chunk ac = a.conj;
			Chunk bc = b.conj;
			if (ac.getMin() > bc.getMin())
				return 1;
			if (ac.getMin() < bc.getMin())
				return -1;
			if (a.getMin() > b.getMin())
				return 1;
			if (a.getMin() < b.getMin())
				return -1;
			if (a == b && ac == bc)
				return 0;
			// Having identical coords, due to int rounding or something, is not
			// good!
			a.setMin(a.getMin() + 0.000001);
			ac.setMin(ac.getMin() + 0.000001);
			return 1;
		}
	};

	public String toString() {
		return v.toString();
	}
}

class YChunk extends Chunk<XChunk> {
	static double g = 0;

	YChunk(Rectangle2D r, XChunk conjugate) {
		super(r, conjugate);
	}

	YChunk(Rectangle2D r) {
		super(r);
	}

	public double getMax() {
		return rect.getMaxY() + g;
	}

	public double getMin() {
		return rect.getMinY();
	}

	public double getLength() {
		return rect.getHeight() + g;
	}

	void setMin(double min) {
		if (rect instanceof java.awt.Rectangle) {
			min = Math.ceil(min);
		}
		rect.setRect(rect.getMinX(), min, rect.getWidth(), rect.getHeight());
	}
}

class XChunk extends Chunk<YChunk> {
	static double g = 0;

	XChunk(Rectangle2D r, YChunk conjugate) {
		super(r, conjugate);
	}

	XChunk(Rectangle2D r) {
		super(r);
	}

	public double getMax() {
		return rect.getMaxX() + g;
	}

	public double getMin() {
		return rect.getMinX();
	}

	public double getLength() {
		return rect.getWidth() + g;
	}

	void setMin(double min) {
		if (rect instanceof java.awt.Rectangle) {
			// because awt Rectangles have int coords!
			min = Math.ceil(min);
		}
		rect.setRect(min, rect.getMinY(), rect.getWidth(), rect.getHeight());
	}

}

class ChunkEdge implements Comparable<ChunkEdge> {
	Chunk chunk;

	boolean isStart;

	double position;

	ChunkEdge(Chunk c, boolean s, double p) {
		chunk = c;
		isStart = s;
		position = p;
	}

	public int compareTo(ChunkEdge arg) {
		if (this.position > arg.position)
			return 1;
		if (this.position < arg.position)
			return -1;
		return 0;
	}
}

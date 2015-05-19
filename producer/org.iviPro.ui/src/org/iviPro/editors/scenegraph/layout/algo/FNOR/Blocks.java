/*
 * Created on 24/02/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.iviPro.editors.scenegraph.layout.algo.FNOR;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A set of blocks defined over variables and operations for building and
 * manipulating those blocks
 * 
 * @author dwyer
 */
class Blocks extends HashSet<Block> {
	static Logger logger = Logger.getLogger("placement");

	/**
	 * Examine the active constraints of each block. If a constraint is found,
	 * across which its container block may be split such that the new blocks
	 * can be moved without violating the constraint to better satisfy the
	 * desired positions, then the split and moves are carried out. Returns
	 * after the first such operation.
	 * 
	 * @return Constraint across which split occured or null if there was no
	 *         split.
	 */
	Constraint splitOnce(ActiveSetPlacement debug) {
		for (Block b : this) {
			b.inConstraintsPriorityQueue = null;
			b.outConstraintsPriorityQueue = null;
		}
		for (Block b : this) {
			b.computeLagrangeMultipliers();
			Constraint c = null;
			double minLM = Double.MAX_VALUE;
			for (Constraint a : b.activeConstraints) {
				if (a.lagrangeMultiplier < minLM) {
					minLM = a.lagrangeMultiplier;
					c = a;
				}
			}
			if (c != null && c.lagrangeMultiplier < 0) {
				int prevBlockCount = size();
				Block l = new Block(), r = new Block();
				b.setUpInConstraints();
				b.setUpOutConstraints();
				b.split(c, l, r);
				remove(b);
				add(l);
				add(r);
				r.position = b.position;
				r.weightedPosition = r.position * r.weight;
				assert (prevBlockCount == size() - 1);
				mergeLeft(l, debug);
				// r may have been merged!
				r = c.right.container;
				r.position = r.desiredPosition();
				r.weightedPosition = r.position * r.weight;
				mergeRight(r, debug);
				r.setUpInConstraints();
				for (Constraint s : r.inConstraintsPriorityQueue.getAll()) {
					assert (!s.isViolated());
				}
				return c;
			}
		}
		return null;
	}

	synchronized void mergeLeft(Block b, ActiveSetPlacement debug) {
		if (logger.isLoggable(Level.FINER))
			logger.finer("arg block=" + b);
		b.setUpInConstraints();
		Constraint c = b.findMaxInConstraint();
		while (c != null && c.isViolated()) {
			if (logger.isLoggable(Level.FINER))
				logger.finer("merging on constraint:" + c);

			int prevBlockCount = size();
			c = b.inConstraintsPriorityQueue.deleteMax();
			Block l = c.left.container;
			assert (l != b);

			double distToL = c.left.offset + c.separation - c.right.offset;
			if (b.variables.size() > l.variables.size()) {
				b.merge(l, c, -distToL);
			} else {
				l.merge(b, c, distToL);
				Block tmp = b;
				b = l;
				l = tmp;
			}
			remove(l);
			assert (prevBlockCount == size() + 1);
			assert (b.activeConstraints.violated().isEmpty());
			c = b.findMaxInConstraint();
		}
		if (logger.isLoggable(Level.FINER))
			logger.finer("Merged block=" + b);
	}

	synchronized void mergeRight(Block b, ActiveSetPlacement debug) {
		if (logger.isLoggable(Level.FINER))
			logger.finer("arg block=" + b);
		b.setUpOutConstraints();
		Constraint c = b.findMaxOutConstraint();
		while (c != null && c.isViolated()) {
			if (logger.isLoggable(Level.FINER))
				logger.finer("merging on constraint:" + c);
			int prevBlockCount = size();
			c = b.outConstraintsPriorityQueue.deleteMax();
			Block r = c.right.container;
			double distToR = c.left.offset + c.separation - c.right.offset;
			if (b.variables.size() > r.variables.size()) {
				b.merge(r, c, distToR);
			} else {
				r.merge(b, c, -distToR);
				Block tmp = b;
				b = r;
				r = tmp;
			}
			remove(r);
			assert (prevBlockCount == size() + 1);
			// b.setUpOutConstraints();
			c = b.findMaxOutConstraint();
		}
		if (logger.isLoggable(Level.FINER))
			logger.finer("Merged block=" + b);
	}

	Blocks(Variable[] vars) {
		for (Variable v : vars) {
			add(new Block(v));
		}
	}

	double cost() {
		double c = 0;
		for (Block b : this) {
			c += b.cost();
		}
		return c;
	}

	Variables getAllVariables() {
		Variables vs = new Variables();
		for (Block b : this) {
			vs.addAll(b.variables);
		}
		return vs;
	}

	public String toString() {
		String s = "";
		for (Block b : this) {
			s += b + ",";
		}
		return s + "Size=" + size();
	}

	/**
	 * DFS search of variables in the constraint DAG. From each variable
	 * constraints are processed from largest separation to smallest, and the
	 * constraint traversed if the current end depth is less than the depth
	 * computed from this DFS.
	 * 
	 * @param v
	 *            current dfs node
	 */
	private void dfsVisit(Variable v, LinkedList<Variable> order) {
		v.visited = true;
		// Assumes no merging has yet been done.
		assert v.container.variables.size() == 1;
		for (Constraint c : v.outConstraints) {
			assert c.left == v;
			if (!c.right.visited) {
				dfsVisit(c.right, order);
			}
		}
		order.addFirst(v);
	}

	/**
	 * Computes a total ordering of constraints by depth-first search of the
	 * directed acyclic graph where nodes are variables and constraints b>=a+1
	 * are edges directed from a to b. Assumes no merging has yet been done.
	 */
	public LinkedList<Variable> totalOrder() {
		Variables vars = getAllVariables();
		LinkedList<Variable> order = new LinkedList<Variable>();
		for (Variable v : vars) {
			v.visited = false;
		}
		for (Variable v : vars.getSources()) {
			dfsVisit(v, order);
		}
		return order;
	}
}

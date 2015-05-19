package org.iviPro.editors.scenegraph.layout.algo.FNOR;

import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActiveSetPlacement extends Observable implements Placement {
	static Logger logger = Logger.getLogger("placement");

	Blocks blocks;

	boolean debugAnimation = false;

	/** list of constraints waiting to be processed (not yet assigned to blocks) */
	Constraints activeConstraints = new Constraints();

	/** canonical list of constraints */
	private Constraints constraints = new Constraints();

	HashMap<String, Variable> vlookup = new HashMap<String, Variable>();

	public boolean split = true;

	public Constraint addConstraint(String u, String v, double sep) {
		Constraint c = new Constraint(vlookup.get(u), vlookup.get(v), sep);
		constraints.add(c);
		return c;
	}

	/**
	 * Gives a feasible - though not necessarily optimal - solution by
	 * examining blocks in the partial order defined by the directed acyclic
	 * graph of constraints. For each block (when processing left to right) we
	 * maintain the invariant that all constraints to the left of the block
	 * (incoming constraints) are satisfied. This is done by repeatedly merging
	 * blocks into bigger blocks across violated constraints (most violated
	 * first) fixing the position of variables inside blocks relative to one
	 * another so that constraints internal to the block are satisfied.
	 */
	void satisfyConstraints() {
		List<Variable> vs = blocks.totalOrder();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("block order: " + blocks);
		}
		for (Variable v : vs) {
			blocks.mergeLeft(v.container, this);
		}
	}

	public ActiveSetPlacement(Variable[] vs) {
		for (Variable v : vs) {
			v.inConstraints = new Constraints();
			v.outConstraints = new Constraints();
		}
		blocks = new Blocks(vs);
		for (Variable v : vs) {
			vlookup.put(v.name, v);
		}
	}

	/**
	 * Calculate the optimal solution. After using satisfy() to produce a
	 * feasible solution, solve() examines each block to see if further
	 * refinement is possible by splitting the block. This is done repeatedly
	 * until no further improvement is possible.
	 */
	public double solve() {
		// activeConstraints = blocks.getAllConstraints();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("variables: " + blocks.getAllVariables());
			logger.fine("sorted constraints: " + activeConstraints);
		}
		satisfyConstraints();
		assert constraints.violated().isEmpty() : "Violated constraints not resolved";
		if (logger.isLoggable(Level.FINER))
			logger.finer("merged->" + blocks);
		if (logger.isLoggable(Level.FINER))
			logger.finer("Cost:" + blocks.cost());

		while (split) {
			Constraint splitConstraint = blocks.splitOnce(this);
			if (splitConstraint == null)
				break;
			assert constraints.violated().isEmpty() : "Violated constraints not resolved";
			activeConstraints.add(splitConstraint);
			if (logger.isLoggable(Level.FINER))
				logger.finer("split->" + blocks);
			if (logger.isLoggable(Level.FINER))
				logger.finer("Cost:" + blocks.cost());
		}

		assert constraints.violated().isEmpty() : "Violated constraints not resolved";
		if (logger.isLoggable(Level.FINER))
			logger.finer("Final->" + blocks);
		if (logger.isLoggable(Level.FINE))
			logger.fine("Cost:" + blocks.cost());
		return blocks.cost();
	}

	public Constraints getConstraints() {
		return constraints;
	}

	public Variables getVariables() {
		return blocks.getAllVariables();
	}
}

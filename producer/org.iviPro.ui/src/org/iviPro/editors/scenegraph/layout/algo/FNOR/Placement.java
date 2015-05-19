package org.iviPro.editors.scenegraph.layout.algo.FNOR;

public interface Placement {

	public abstract Constraint addConstraint(String u, String v, double sep);

	public abstract double solve() throws Exception;
	
	public abstract Constraints getConstraints();
	public abstract Variables getVariables();

}
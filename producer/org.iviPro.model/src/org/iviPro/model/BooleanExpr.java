package org.iviPro.model;

public class BooleanExpr {
	
	public enum RelationOperator {
		LESS,
		LEQ,
		EQUAL,
		HEQ,
		HIGHER
	}
	
	public enum BooleanOperator {
		AND,
		OR,
		NOT
	}
	
	public BooleanExpr(String var, RelationOperator op, double value) {
		
	}
	
	public BooleanExpr(BooleanExpr first, BooleanOperator op, BooleanExpr second){
		
	}
}

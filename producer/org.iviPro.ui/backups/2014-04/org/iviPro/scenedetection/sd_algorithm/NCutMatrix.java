package org.iviPro.scenedetection.sd_algorithm;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;

public class NCutMatrix extends DenseDoubleMatrix2D {

	private static final long serialVersionUID = -3202841246388131087L;

	private double[][] values;

	public NCutMatrix(double[][] values) {
		super(values);
		this.values = values;
	}
	
	public double[][] getValues() {
		return values;
	}

	public NCutMatrix SQRTMatrix() {
		double[][] currentValues = new double[values.length][values[0].length];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				if(values[i][j] != 0) {
					currentValues[i][j] = (1 / Math.sqrt(values[i][j]));
				} else {
					currentValues[i][j] = 0;
				}
			}
		}
		NCutMatrix matrix = new NCutMatrix(currentValues);
		return matrix;
	}

	public NCutMatrix subtractMatrix(NCutMatrix sub) {
		NCutMatrix result = new NCutMatrix(new double[rows][columns]);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				result.setQuick(i, j, getQuick(i, j) - sub.getQuick(i, j));
			}
		}
		return result;
	}
}
package org.iviPro.scenedetection.sd_algorithm;

import java.util.List;
import org.iviPro.scenedetection.sd_main.CutTypes;
import org.iviPro.scenedetection.sd_main.Shot;

public class GraphGrammar {

	private static final double ALPHASPLITVALUE = 0.75;

	private static final double BETASPLITVALUE = 0.25;

	private static final double TRANSITIONCUT = 0.9437;

	private static final double TRANSITIONFADE = 0.2217;

	private static final double TRANSITIONDISSOLVE = 0.6753;

	private static final double RTHRESHOLD = 10;

	private static final double FTHRESHOLD = 0.2;

	private List<Shot> shotList;

	public GraphGrammar(List<Shot> shotList) {
		this.shotList = shotList;
	}

	public double involveGrammarValue(double simVal, int shotNrInList1,
			int shotNrInList2) {
		return ALPHASPLITVALUE * simVal + BETASPLITVALUE
				* overallAttraction(shotNrInList1, shotNrInList2);
//		return simVal * overallAttraction(shotNrInList1, shotNrInList2);
	}

	private double overallAttraction(int shotNrInList1, int shotNrInList2) {
		double min = Double.MAX_VALUE;
		for (int k = shotNrInList1; k <= shotNrInList2 - 1; k++) {
			double val = calcAttractionTransition(k) + calcMergingEffect(k);
			if (val < min) {
				min = val;
			}
		}
		return min;
	}

	private double calcAttractionTransition(int k) {
		if (shotList.get(k).getCut2Type() == CutTypes.HardCut) {
			return TRANSITIONCUT;
		} else if (shotList.get(k).getCut2Type() == CutTypes.Fade) {
			return TRANSITIONFADE;
		} else {
			return TRANSITIONDISSOLVE;
		}
	}

	private double calcMergingEffect(int k) {
		double temporalAtt = RTHRESHOLD;
		if((k - temporalAtt) < 0) {
			temporalAtt = k;
		}
		if((k + temporalAtt > (shotList.size() - 1))) {
			temporalAtt = shotList.size() - 1 - k;
		}
		double val = 0;
		for (double i = 1; i <= temporalAtt; i++) {
			val += FTHRESHOLD
					* ((temporalAtt + 1.0 - i) / temporalAtt)
					* (2.0 * TRANSITIONCUT
							- calcAttractionTransition((int) (k - i)) - calcAttractionTransition((int) (k + i)));
		}
		return val;
	}
}

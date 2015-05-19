package org.iviPro.scenedetection.sd_main;

public class PerformanceCalculation {

	// Correct Boundaries Heat
	// public static final int[][] correctBoundaries = {
	// { 1, 28, 1083, 1114, 2172, 5188, 5221, 6359, 6388, 7361, 7390,
	// 7861, 7890, 10465, 12530, 12560, 13470, 13499, 14608,
	// 14637, 15903, 15935, 16546, 16775 },
	// { 1, 28, 118, 1083, 1114, 2172, 2549, 2715, 5188, 5221, 6359, 6388,
	// 7361, 7390, 7861, 7890, 10465, 12530, 12560, 13470, 13499,
	// 14608, 14637, 15343, 15903, 15935, 16546, 16775 },
	// { 1, 28, 118, 765, 1083, 1114, 2172, 2549, 2715, 5188, 5221, 6359,
	// 6388, 7361, 7390, 7861, 7890, 8313, 10314, 10465, 10744,
	// 11942, 12393, 12436, 12530, 12560, 13470, 13499, 14608,
	// 14637, 14900, 15343, 15903, 15935, 16546, 16775
	//
	// } };

	// // Correct Boundaries Werbung
	// public static final int[][] correctBoundaries = {
	// { 1, 94, 267, 338, 840, 910, 1283, 1351, 1728, 1801, 2532, 2613,
	// 3377, 3453, 4208, 4275, 4774, 4845, 5594, 5665, 6166, 6236,
	// 6987, 7057, 7811, 7882, 8382, 8455, 9579, 9651, 10026 },
	// { 1, 94, 198, 267, 338, 428, 840, 910, 1283, 1351, 1429, 1639,
	// 1728, 1801, 2444, 2532, 2613, 2778, 3377, 3453, 3885, 4208,
	// 4275, 4349, 4369, 4384, 4399, 4451, 4528, 4567, 4774, 4845,
	// 4920, 4942, 4981, 5532, 5594, 5665, 5758, 5897, 5967, 5988,
	// 6013, 6074, 6166, 6812, 6912, 6987, 7057, 7207, 7449, 7512,
	// 7732, 7811, 7882, 8279, 8356, 8382, 8455, 8792, 8880, 9314,
	// 9538, 9579, 9651, 9751, 9925, 10026 },
	// { 1, 94, 198, 234, 267, 338, 388, 428, 748, 840, 910, 1283, 1351,
	// 1429, 1525, 1639, 1728, 1801, 2444, 2532, 2613, 2778, 3377,
	// 3453, 3885, 4208, 4275, 4317, 4349, 4369, 4384, 4399, 4451,
	// 4528, 4567, 4774, 4845, 4920, 5039, 5091, 5138, 5194, 5234,
	// 5287, 5326, 5400, 5427, 5457, 5488, 5532, 5594, 5665, 5758,
	// 5897, 5967, 5988, 6013, 6074, 6166, 6812, 6912, 6987, 7057,
	// 7144, 7207, 7236, 7345, 7449, 7512, 7533, 7594, 7732, 7811,
	// 7882, 8279, 8356, 8382, 8455, 8792, 8880, 9314, 9538, 9579,
	// 9651, 9751, 9925, 10026 } };
	//
	// // Correct Boundaries SDtry
	// public static final int[][] correctBoundaries = {
	// { 1, 271, 1067, 5418, 5895, 6361, 7151, 7751, 8938, 9214, 9314,
	// 9503, 10130, 10184, 10949, 11107, 11510, 11597, 11658,
	// 11911, 12509 },
	// { 1, 271, 1067, 3916, 5418, 5895, 6361, 7151, 8938, 9214, 9314,
	// 9503, 10130, 10184, 10565, 10949, 11107, 11510, 11597,
	// 11658, 11911, 12509 },
	// { 1, 271, 1067, 3916, 5418, 5636, 5895, 6361, 7151, 8938, 9214,
	// 9314, 9503, 10130, 10184, 10565, 10949, 11107, 11510,
	// 11597, 11658, 11911, 12509 } };
	//
	// // Correct Boundaries Nachrichten
	// public static final int[][] correctBoundaries = {
	// { 1, 163, 970, 2123, 5191, 5837, 8118, 8709, 12327, 13234, 13393,
	// 13812, 14285 },
	// { 1, 163, 666, 970, 2123, 2837, 3374, 3767, 3856, 4199, 4693, 4786,
	// 5025, 5837, 8118, 8709, 12327, 13234, 13393, 13812, 14285 },
	// { 1, 163, 328, 666, 970, 2123, 2837, 3374, 3767, 3856, 4199, 4573,
	// 4693, 4786, 5025, 5837, 8118, 8709, 12327, 13234, 13393,
	// 13812, 14285 } };
	//
	// // Correct Boundaries Matrix
	// public static final int[][] correctBoundaries = {
	// { 1, 1724, 2511, 3175, 3545, 4071, 4527, 4895, 5289, 5320, 6125,
	// 6785, 7845, 8835, 10846 },
	// { 1, 458, 749, 764, 1985, 2511, 2676, 3545, 4071, 4527, 4895, 5289,
	// 5320, 6125, 6785, 7845, 8835, 10846 },
	// { 1, 458, 689, 749, 764, 1194, 1278, 1985, 2511, 2676, 3545, 4071,
	// 4527, 4895, 5289, 5320, 6125, 6785, 7845, 8835, 10846 } };
	//
	// // Correct Boundaries BigBang
	// public static final int[][] correctBoundaries = { { 1, 2716, 2747, 3046,
	// 3076, 4221, 4252, 4840, 4871, 5389, 5420, 6671, 6702, 7809, 7840,
	// 8449, 8480, 9985, 10016, 10363, 10394, 10967, 10999, 13195, 13227,
	// 14863, 14893, 15483, 15514, 16002, 16033, 16786, 16816, 18157,
	// 18186, 22534 } };

	public static final int[][] correctBoundaries = {
			{ 1, 283, 2059, 2148, 4682, 6092, 8412, 10232, 10759, 11299, 12596,
					12752, 14725, 15610, 15682 },
			{ 1, 283, 2059, 2148, 3941, 4682, 6092, 8412, 10058, 10232, 10759,
					11299, 12596, 12752, 14725, 15610, 15682 },
			{ 1, 283, 884, 2059, 2148, 3941, 4682, 5047, 6092, 8412, 10058,
					10232, 10386, 10759, 11299, 12596, 12752, 12929, 14725,
					15610, 15682 } };

	private int[] detectedBoundaries;

	public static final double alpha = 0.65;

	private int amountScenes;

	public PerformanceCalculation(String bounds) {
		createBoundaryArray(bounds);
		double[] opiOverall = new double[correctBoundaries.length];
		for (int i = 0; i < correctBoundaries.length; i++) {
			this.amountScenes = correctBoundaries[i].length - 1;
			double fpi = calcFPI(i);
			double fni = calcFNI(i);
			double opi = calcOPI(fpi, fni);
			double recall = calcRecall(i);
			double precision = calcPrecision(i);
			double f1 = calcF1(recall, precision);
			opiOverall[i] = opi;
			System.out.println("fpi: " + (1 - fpi));
			System.out.println("fni: " + (1 - fni));
			System.out.println("Opi: " + opi);
			System.out.println("Recall: " + recall);
			System.out.println("Precision: " + precision);
			System.out.println("F1: " + f1);
		}
		double result = 0;
		int posi = 0;
		for (int i = 0; i < opiOverall.length; i++) {
			if (opiOverall[i] > result) {
				result = opiOverall[i];
				posi = i;
			}
			result += opiOverall[i];
		}
		// result /= opiOverall.length;
		System.out.println("OverallResult: " + result + "An position: " + posi);
	}

	private double calcFPI(int val) {
		double allFPI = 0;
		double fpi[] = new double[amountScenes];
		for (int i = 0; i < amountScenes; i++) {
			int wrongBoundaries = 0;
			int start = correctBoundaries[val][i];
			int end = correctBoundaries[val][i + 1];
			// Calc DSB
			for (int j = 0; j < detectedBoundaries.length; j++) {
				if (detectedBoundaries[j] > start
						&& detectedBoundaries[j] < end) {
					wrongBoundaries++;
				}
			}
			// fpi[i] = Math.log(wrongBoundaries + 1) / Math.log(3);
			fpi[i] = (1 - Math.pow(alpha, wrongBoundaries)) / (1 - alpha);
		}
		int amountNullValues = 0;
		for (int i = 0; i < fpi.length; i++) {
			System.out.println("Resultadding: " + fpi[i]);
			allFPI += fpi[i];
			if (fpi[i] == 0) {
				amountNullValues++;
			}
		}

		System.out.println("AmountNull" + amountNullValues);
		// double result = allFPI / fpi.length;
		// if (result > 1) {
		// result = 1;
		// }
		// return result;
		double tempresult = ((1 - alpha) / ((amountScenes)) * allFPI);
		return tempresult;
	}

	private double calcFNI(int val) {
		double fniOverall = 0;
		double fni[] = new double[amountScenes - 1];
		for (int i = 1; i < correctBoundaries[val].length - 1; i++) {
			int sceneLengthfirst = correctBoundaries[val][i]
					- correctBoundaries[val][i - 1];
			int sceneLengthsecond = correctBoundaries[val][i + 1]
					- correctBoundaries[val][i];
			int start = correctBoundaries[val][i - 1];
			int end = correctBoundaries[val][i + 1];
			int distance = Integer.MAX_VALUE;
			for (int j = 0; j < detectedBoundaries.length; j++) {
				if (detectedBoundaries[j] > start
						&& detectedBoundaries[j] < end) {
					if (Math.abs(correctBoundaries[val][i]
							- detectedBoundaries[j]) < distance) {
						distance = Math.abs(correctBoundaries[val][i]
								- detectedBoundaries[j]);
					}
				}
			}
			fni[i - 1] = Math
					.min(1.0,
							(((double) distance / (double) sceneLengthfirst) + ((double) distance / (double) sceneLengthsecond)));
		}
		for (int i = 0; i < fni.length; i++) {
			fniOverall += fni[i];
		}

		return ((double) 1 / (double) (amountScenes - 1)) * fniOverall;
	}

	private double calcOPI(double fpi, double fni) {
		return 1.0 - (0.5 * fpi + 0.5 * fni);
	}

	private double calcRecall(int val) {
		int[] correct = correctBoundaries[val];
		int value = 0;
		for (int i = 0; i < detectedBoundaries.length; i++) {
			for (int j = 0; j < correct.length; j++) {
				if (detectedBoundaries[i] == correct[j]) {
					value++;
				}
			}
		}
		return (((double) value) / ((double) correctBoundaries[val].length));
	}

	private double calcPrecision(int val) {
		int[] correct = correctBoundaries[val];
		int value = 0;
		for (int i = 0; i < detectedBoundaries.length; i++) {
			for (int j = 0; j < correct.length; j++) {
				if (detectedBoundaries[i] == correct[j]) {
					value++;
				}
			}
		}
		return (((double) value) / ((double) detectedBoundaries.length));
	}

	private double calcF1(double recall, double precision) {
		return ((2 * recall * precision) / (recall + precision));
	}

	private void createBoundaryArray(String str) {
		String[] split = str.split(",");
		this.detectedBoundaries = new int[split.length];
		for (int i = 0; i < split.length; i++) {
			this.detectedBoundaries[i] = Integer.parseInt(split[i]);
		}
	}

	public static void main(String[] args) {
		int[] boundaries = { 1, 392, 627, 995, 1636, 1951, 2059, 2285, 3036,
				3270, 3434, 3639, 4546, 4766, 6092, 7009, 7214, 7640, 7734,
				8412, 8794, 9596, 10058, 10232, 10310, 10603, 10759, 11299,
				12929, 13067, 13509, 14080, 14247, 14725, 14873, 15682 };
		String string = "";
		int position = 0;
		int left = 0;
		for (int i = 0; i < boundaries.length; i++) {
			if (i < boundaries.length) {
				string += boundaries[i];
				if (i < boundaries.length - 1) {
					string += ",";
				}
			}
		}
		// for (int i = 1; i <= 16775; i++) {
		// if (i == boundaries[position]) {
		// string += boundaries[position];
		// string += ",";
		// if (left % 2 == 0) {
		// string += (boundaries[position] + 1);
		// string += ",";
		// string += (boundaries[position] + 2);
		// string += ",";
		// string += (boundaries[position] + 3);
		// string += ",";
		// // string += (boundaries[position] + 4);
		// // string += ",";
		// }
		// position++;
		// left++;
		// }
		// string += i+",";
		// }
		System.out.println(string);
		new PerformanceCalculation(string);
	}
}

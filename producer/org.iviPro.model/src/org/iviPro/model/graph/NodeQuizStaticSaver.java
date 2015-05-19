package org.iviPro.model.graph;

import java.io.IOException;
import java.io.Serializable;

class StaticSaver implements Serializable {

	static int testCounter = 0;
	
	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		testCounter = stream.readInt();
	}

	private void writeObject(java.io.ObjectOutputStream stream)
			throws IOException {
		stream.writeInt(testCounter);
	}
	
	public int getSaver() {
		return testCounter;
	}
	
	public void setTestCouner(int counter) {
		testCounter = counter;
	}
}
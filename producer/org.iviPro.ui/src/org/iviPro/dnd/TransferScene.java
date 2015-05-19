package org.iviPro.dnd;

public class TransferScene extends IAbstractTransfer {

	private static final TransferScene instance = new TransferScene();
	private static String TYPE = TransferScene.class.getName();
	private static int TYPE_ID = registerType(TYPE);

	private TransferScene() {
	}

	public static TransferScene getInstance() {
		return instance;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public int getTypeID() {
		return TYPE_ID;
	}

}

package org.iviPro.dnd;

public class TransferMedia extends IAbstractTransfer {

	private static final TransferMedia instance = new TransferMedia();
	private static String TYPE = TransferMedia.class.getName();
	private static int TYPE_ID = registerType(TYPE);

	private TransferMedia() {
	}

	public static TransferMedia getInstance() {
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

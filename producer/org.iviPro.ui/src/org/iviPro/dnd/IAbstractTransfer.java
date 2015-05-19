package org.iviPro.dnd;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

public abstract class IAbstractTransfer extends ByteArrayTransfer {

	public abstract String getType();

	public abstract int getTypeID();

	public void javaToNative(Object object, TransferData transferData) {
		if (!validate(object) || !isSupportedType(transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}
		String transferKey = (String) object;
		byte[] buffer = transferKey.getBytes();
		super.javaToNative(buffer, transferData);
	}

	public Object nativeToJava(TransferData transferData) {
		if (isSupportedType(transferData)) {
			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null) {
				return null;
			}
			String transferKey = new String(buffer);
			return transferKey;
		} else {
			return null;
		}
	}

	protected String[] getTypeNames() {
		return new String[] { getType() };
	}

	protected int[] getTypeIds() {
		return new int[] { getTypeID() };
	}

	protected boolean validate(Object obj) {
		return (obj instanceof String);
	}

}

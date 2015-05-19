package org.iviPro.mediaaccess.mediameta.interfaces;

import org.iviPro.model.IMediaObject;

// Interface für eine Klasse, die Meta Informationen eines Media-Objets setzt
public interface I_MediaMetaFiller<T extends IMediaObject> {

	public void addMetaInformation(T mediaObject, I_MediaMetaAccessor metaAccessor);
}

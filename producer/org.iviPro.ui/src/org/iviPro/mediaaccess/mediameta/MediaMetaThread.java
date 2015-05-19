package org.iviPro.mediaaccess.mediameta;

import org.iviPro.mediaaccess.mediameta.interfaces.I_MediaMetaAccessor;
import org.iviPro.mediaaccess.mediameta.interfaces.I_MediaMetaFiller;
import org.iviPro.mediaaccess.mediameta.metaaccessors.MediaAccessorFactory;
import org.iviPro.mediaaccess.mediameta.metafiller.MetaFillerFactory;
import org.iviPro.model.IMediaObject;

/**
 * Thread holt Meta-Informationen für ein Medien Objekt
 * @author juhoffma
 */
public class MediaMetaThread implements Runnable {
	
	 // Das Media-Object fuer diesen Thread	 
	private IMediaObject mediaObject;
	
	public MediaMetaThread(IMediaObject mediaObject) {
		this.mediaObject = mediaObject;						
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		I_MediaMetaAccessor mediaMetaAccessor = MediaAccessorFactory.getAccessor(mediaObject);
		I_MediaMetaFiller mediaMetaFiller = MetaFillerFactory.getMediaMetaFiller(mediaObject);
		if (mediaMetaFiller != null && mediaMetaAccessor != null) {
			mediaMetaFiller.addMetaInformation(mediaObject, mediaMetaAccessor);
		}
	}
}

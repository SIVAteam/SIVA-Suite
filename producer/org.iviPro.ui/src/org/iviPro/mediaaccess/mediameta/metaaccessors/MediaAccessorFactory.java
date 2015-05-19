package org.iviPro.mediaaccess.mediameta.metaaccessors;

import org.iviPro.mediaaccess.mediameta.interfaces.I_MediaMetaAccessor;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.Video;

public class MediaAccessorFactory {

	// einfache Factory die einen Accessor für einen bestimmten Medientyp zurück gibt.
	public static I_MediaMetaAccessor getAccessor(IMediaObject mediaObject) {
		if (mediaObject instanceof Video || mediaObject instanceof Audio) {
			return new FFProbeAccessor(mediaObject);
		} else
		if (mediaObject instanceof Picture) {
			return new ImageAccessor((Picture) mediaObject);
		}
		return null;
	}
}

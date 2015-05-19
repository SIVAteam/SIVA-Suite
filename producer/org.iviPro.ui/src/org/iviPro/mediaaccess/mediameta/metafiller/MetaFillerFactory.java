package org.iviPro.mediaaccess.mediameta.metafiller;

import org.iviPro.mediaaccess.mediameta.interfaces.I_MediaMetaFiller;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.Video;

// gibt einen Meta-Filler für ein Media-Object zurück
public class MetaFillerFactory {
	
	public static I_MediaMetaFiller getMediaMetaFiller(IMediaObject mediaObject) {
		if (mediaObject instanceof Video) {
			return new VideoMetaFiller();
		} else
		if (mediaObject instanceof Audio) {
			return new AudioMetaFiller();
		} else
		if (mediaObject instanceof Picture) {
			return new PictureMetaFiller();
		}
		return null;				
	}
}

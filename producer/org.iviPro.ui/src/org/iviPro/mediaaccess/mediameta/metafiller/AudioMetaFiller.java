package org.iviPro.mediaaccess.mediameta.metafiller;

import org.iviPro.mediaaccess.mediameta.interfaces.I_MediaMetaAccessor;
import org.iviPro.mediaaccess.mediameta.interfaces.I_MediaMetaFiller;
import org.iviPro.model.resources.Audio;

public class AudioMetaFiller implements I_MediaMetaFiller<Audio> {

	@Override
	public void addMetaInformation(Audio audio, I_MediaMetaAccessor metaAccessor) {		
		audio.setCodec(metaAccessor.getCodec());
		audio.setDuration(metaAccessor.getMediaLengthNano());				
	}
}

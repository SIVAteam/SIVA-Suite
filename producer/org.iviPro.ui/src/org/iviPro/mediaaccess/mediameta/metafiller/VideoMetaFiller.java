package org.iviPro.mediaaccess.mediameta.metafiller;

import org.iviPro.mediaaccess.mediameta.interfaces.I_MediaMetaAccessor;
import org.iviPro.mediaaccess.mediameta.interfaces.I_MediaMetaFiller;
import org.iviPro.model.resources.Video;

public class VideoMetaFiller implements I_MediaMetaFiller<Video> {

	@Override
	public void addMetaInformation(Video video, I_MediaMetaAccessor metaAccessor) {		
		video.setCodec(metaAccessor.getCodec());
		video.setDuration(metaAccessor.getMediaLengthNano());
		
		//Dimension wird extern gesetzt
		if(video.getDimension() == null){
			video.setDimension(metaAccessor.getDimension());
		}
		
		video.setFrameRate(metaAccessor.getFrameRate());
	}
}

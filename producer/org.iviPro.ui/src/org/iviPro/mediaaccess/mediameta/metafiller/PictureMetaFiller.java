package org.iviPro.mediaaccess.mediameta.metafiller;

import org.iviPro.mediaaccess.mediameta.interfaces.I_MediaMetaAccessor;
import org.iviPro.mediaaccess.mediameta.interfaces.I_MediaMetaFiller;
import org.iviPro.model.resources.Picture;

public class PictureMetaFiller implements I_MediaMetaFiller<Picture> {

	@Override
	public void addMetaInformation(Picture picture, I_MediaMetaAccessor metaAccessor) {		
		picture.setCodec(metaAccessor.getCodec());
		picture.setDimension(metaAccessor.getDimension());
	}
}

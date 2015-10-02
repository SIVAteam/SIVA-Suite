package org.iviPro.transcoder;

import org.iviPro.transcoding.exception.TranscodingException;

public interface Transcoder {

	public void transcodeAudio(AudioDescriptor audioDescriptor)
			throws TranscodingException, InterruptedException;

	public void transcodeVideo(VideoDescriptor videoDescriptor)
			throws TranscodingException, InterruptedException;
}

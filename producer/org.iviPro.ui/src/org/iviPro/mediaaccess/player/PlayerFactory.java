package org.iviPro.mediaaccess.player;

import org.iviPro.model.IMediaObject;

/**
 * Factory zum Erstellen eines Players ... falls mal noch was anderes als VLC kommt... 
 * @author juhoffma
 */
public class PlayerFactory {

	private static enum PlayerTypes{VLC}
	
	private static PlayerTypes typeToUse = PlayerTypes.VLC;
	
	/**
	 * Factory method to create a <code>I_MediaPlayer</code> object. The player
	 * will not be instantiated for the whole <code>mediaObject</code> but for
	 * the part defined by the given start and end time.
	 * <p><b>Note:</b> Thanks to the original author for not leaving any 
	 * comments in the first place.<p>
	 * @param mediaObject media object which should be loaded in the player 
	 * @param startTime time in the <code>mediaObject</code> defining the 
	 * start point of the media part shown in the player 
	 * @param endTime time in the <code>mediaObject</code> defining the 
	 * end point of the media part shown in the player
	 * @return media player instance for a part of the <code>mediaObject</code>
	 */
	public static MediaPlayer getPlayer(IMediaObject mediaObject, long startTime, long endTime) {
		switch (typeToUse) {
			case VLC: return new VLCBasedMediaPlayer(mediaObject, startTime, endTime);
		}
		return null;		
	}
	
	public static MediaPlayer getPlayer(IMediaObject mediaObject) {
		switch (typeToUse) {
			case VLC: return new VLCBasedMediaPlayer(mediaObject);
		}
		return null;		
	}
}

package org.iviPro.mediaaccess.player;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.iviPro.editors.mediaplayer.messages"; //$NON-NLS-1$

	// Tooltips für die Buttons
	public static String MediaPlayer_Tooltip_FB;
	public static String MediaPlayer_Tooltip_B;
	public static String MediaPlayer_Tooltip_Play;
	public static String MediaPlayer_Tooltip_Stop;
	public static String MediaPlayer_Tooltip_F;
	public static String MediaPlayer_Tooltip_FF;
	public static String MediaPlayer_Tooltip_Volume;
	public static String MediaPlayer_Tooltip_Mute;
	public static String MediaPlayer_Tooltip_Aspect;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}

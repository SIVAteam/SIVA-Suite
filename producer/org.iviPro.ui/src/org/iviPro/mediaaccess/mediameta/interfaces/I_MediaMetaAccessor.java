package org.iviPro.mediaaccess.mediameta.interfaces;

import java.awt.Dimension;

/*
 * Interface for Media Access
 * Schreibt f�r Media Objekte ben�tigte Meta Daten vor
 * Die aktuelle Implementierung basiert auf vlcj (Klasse MediaAccess)
 * F�r jeden Media-Typ wird nur dieses Interface verwendet, hier ist zu beachten, dass nicht 
 * alle Datene f�r jeden Typ relevant sind z.B. haben Bilder keine L�nge oder Framerate
 * Wird verwendet um beim Hinzuf�gen von Media-Objekten diese mit den entsprechenden Meta-Daten zu bef�llen
 * (im Media-Information-Thread)
 * Weiterhin wird f�r Videos der Zugriff auf einzelne Frames erm�glicht (als BufferedImage)
 */
public interface I_MediaMetaAccessor {

	public long getMediaLengthNano();
	public long getSize();	
	public int getFrameRate();	
	public String getCodec();	
	public Dimension getDimension();
	public double getAspectRatio();
	public double getBitRate();
}

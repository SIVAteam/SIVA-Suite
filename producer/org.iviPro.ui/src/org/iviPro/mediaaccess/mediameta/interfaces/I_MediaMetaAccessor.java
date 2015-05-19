package org.iviPro.mediaaccess.mediameta.interfaces;

import java.awt.Dimension;

/*
 * Interface for Media Access
 * Schreibt für Media Objekte benötigte Meta Daten vor
 * Die aktuelle Implementierung basiert auf vlcj (Klasse MediaAccess)
 * Für jeden Media-Typ wird nur dieses Interface verwendet, hier ist zu beachten, dass nicht 
 * alle Datene für jeden Typ relevant sind z.B. haben Bilder keine Länge oder Framerate
 * Wird verwendet um beim Hinzufügen von Media-Objekten diese mit den entsprechenden Meta-Daten zu befüllen
 * (im Media-Information-Thread)
 * Weiterhin wird für Videos der Zugriff auf einzelne Frames ermöglicht (als BufferedImage)
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

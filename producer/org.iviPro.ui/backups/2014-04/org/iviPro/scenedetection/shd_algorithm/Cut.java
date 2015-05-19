package org.iviPro.scenedetection.shd_algorithm;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

import org.iviPro.scenedetection.sd_main.SDTime;

/**
 * Die Klasse repraesentiert einen Cut im Video. Pro Cut wird ein Objekt dieser
 * Klasse angelegt, welches die Kategorie (0=Hardcut, 1=Fade, 2=Dissolve) des
 * Cuts speichert. Unter Umstaenden kann es vorkommen dass die Kategorie 3
 * angegeben wird. Dies bedeutet dass der Detector sich nicht zu 100 prozent
 * sicher ist ob es ein Hardcut ist oder zb. ein Dissolve. Es wird zwar zuerst
 * als HardCut gespeichert, jedoch ist der Algorithmus in der Lage dies mit der
 * Update Methode noch nachtraeglich zu aendern.
 * 
 * 
 * @author Stefan Zwicklbauer
 * 
 */
public class Cut implements Comparable<Cut>, Cloneable {

	// Eine Liste der Bilder welche zum Cut gehoeren
	private LinkedList<BufferedImage> imageList;
	// FrameNr bei der der Cut festgestellt wurde
	private long frameNr;
	// Klassifizierung. Hardcut, Fade oder Dissolve
	private int category;
	// Kann der Cut nachtraeglich noch geaendert werden?
	private boolean updateable;
	// Zeit an der der Cut aufgetreten ist.
	private SDTime[] cutTime;

	private long startPossibility;

	private long endPossibility;

	/**
	 * Konstruktor - Erstellt ein neues CutObjekt
	 * 
	 * @param list
	 *            Liste der zum Cut gehoerenden Bilder
	 * @param category
	 *            Die Cut-Kategorie: Hardcut, Fade oder Dissolve
	 * @param frameNr
	 *            Die FrameNr an der der Cut aufgetreten ist.
	 */
	public Cut(BufferedImage[] list, int category, long frameNr, SDTime[] time,
			long startPossibility, long endPossibility) {
		cutTime = time;
		imageList = new LinkedList<BufferedImage>();
		for (int i = 0; i < list.length; i++) {
			imageList.add(list[i]);
		}
		this.frameNr = frameNr;
		if (category == 3) {
			this.category = 0;
			this.updateable = true;
		} else {
			this.updateable = false;
			this.category = category;
		}
		this.startPossibility = startPossibility;
		this.endPossibility = endPossibility;
	}

	/**
	 * Aehnlich wie der Konstruktor - Die Updatefunktion wird idr. nur
	 * aufgerufen falls vorher ein Cut nicht eindeutig einer Kategorie
	 * zuzuordnen war
	 * 
	 * @param list
	 *            Liste der zum Cut gehoerenden Bilder
	 * @param category
	 *            Die Cut-Kategorie: Hardcut, Fade oder Dissolve
	 * @param frameNr
	 *            Die FrameNr an der der Cut aufgetreten ist.
	 */

	public void update(BufferedImage[] list, int category, long frameNr) {
		if (updateable && category != 3) {
			imageList = new LinkedList<BufferedImage>();
			for (int i = 0; i < list.length; i++) {
				imageList.add(list[i]);
			}
			this.category = category;
			this.frameNr = frameNr;
		}
	}

	/**
	 * Aehnlich wie der Konstruktor - Die Updatefunktion wird idr. nur
	 * aufgerufen falls vorher ein Cut nicht eindeutig einer Kategorie
	 * zuzuordnen war
	 * 
	 * @param list
	 *            Liste der zum Cut gehoerenden Bilder
	 * @param category
	 *            Die Cut-Kategorie: Hardcut, Fade oder Dissolve
	 * @param frameNr
	 *            Die FrameNr an der der Cut aufgetreten ist.
	 */

	public void update(int category) {
		this.category = category;
	}

	/**
	 * Liefert die Cutbilder
	 * 
	 * @return Cutbilder
	 */
	public BufferedImage getImage(boolean start) {
		if (category == 0) {
			if (start) {
				return imageList.get(1);
			} else {
				return imageList.get(0);
			}
		} else {
			return imageList.get(0);
		}
	}

	/**
	 * Liefert die Framenummer des Cuts
	 * 
	 * @return Framenummer
	 */
	public long getCutFrameNr() {
		return frameNr;
	}

	/**
	 * Liefert die Kategorie des Cuts
	 * 
	 * @return Kategorie
	 */
	public int getCutCategory() {
		return category;
	}
	
	public SDTime getFirstTime() {
		return cutTime[0];
	}
	
	public SDTime getSecondTime() {
		return cutTime[1];
	}

	public String getCategoryName() {
		String name = "";
		if (category == 0) {
			name = "Hard Cut";
		} else if (category == 1) {
			name = "Fade";
		} else if (category == 2) {
			name = "Dissolve";
		}
		return name;
	}

	public long getStartPossibility() {
		return startPossibility;
	}

	public long getEndPossibility() {
		return endPossibility;
	}

	@Override
	public Cut clone() {
		BufferedImage[] images = new BufferedImage[imageList.size()];
		for (int i = 0; i < imageList.size(); i++) {
			if (imageList.get(i) != null) {
				images[i] = imageList.get(i).getSubimage(0, 0,
						imageList.get(i).getWidth(),
						imageList.get(i).getHeight());
			} else {
				images[i] = null;
			}
		}
		SDTime[] timeArr = new SDTime[cutTime.length];
		for (int i = 0; i < timeArr.length; i++) {
			timeArr[0] = new SDTime(cutTime[i].getNanoseconds());
		}
		Cut clone = new Cut(images, category, frameNr, timeArr,
				startPossibility, endPossibility);
		clone.updateable = this.updateable;
		return clone;
	}

	@Override
	public int compareTo(Cut o) {
		if (this.frameNr < o.frameNr) {
			return -1;
		} else if (this.frameNr == o.frameNr) {
			return 0;
		} else {
			return 1;
		}
	}
}

package org.iviPro.scenedetection.sd_main;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.swt.graphics.Image;

public class Scene implements Cloneable {

	private long startFrame;

	private long endFrame;

	private int sceneId;

	private List<Content> contentList;

	private List<Image> startList;

	private List<Image> endList;
	
	private String sceneName;

	public Scene(int id) {
		this.startList = new LinkedList<Image>();
		this.endList = new LinkedList<Image>();
		this.sceneId = id;
		this.contentList = new LinkedList<Content>();
	}

	public void addShots(Shot shot, boolean ambiguous) {
		contentList.add(new Content(shot, ambiguous));
	}

	public void removeShot(int pos) {
		contentList.remove(pos);
		startList.remove(pos);
		endList.remove(pos);
	}

	public void incrementId() {
		sceneId++;
	}

	public void decrementId() {
		sceneId--;
	}

	public void setSettings() {
		Collections.sort(contentList);
	}

	public long getStartFrame() {
		return startFrame;
	}

	public void setAddSwtStartImage(Image start) {
		startList.add(start);
	}

	public void setAddSwtEndImage(Image end) {
		endList.add(end);
	}

	public Image getStartSwtImage(int position) {
		return startList.get(position);
	}

	public Image getEndSwtImage(int position) {
		return endList.get(position);
	}

	public void clearSwtImages() {
		startList.clear();
		endList.clear();
	}

	public long getEndFrame() {
		return endFrame;
	}

	public int getSceneId() {
		return sceneId;
	}

	public int getNumberofShots() {
		return contentList.size();
	}

	public Shot getShotWithNr(int i) {
		if (i < 0 || i >= contentList.size()) {
			return null;
		}
		return contentList.get(i).getShot();
	}

	public boolean getAmbiguousShotWithNr(int i) {
		if (i < 0 || i >= contentList.size()) {
			return false;
		}
		return contentList.get(i).isAmbiguous();
	}

	public void test() {
		for (int i = 0; i < contentList.size(); i++) {
			System.out.println("Meine ID: "
					+ contentList.get(i).getShot().getShotId());
		}
	}

	public long getStartTimeNano() {
		return contentList.get(0).getShot().getStartTimeNano();
	}

	public long getEndTimeNano() {
		return contentList.get(contentList.size() - 1).getShot()
				.getEndTimeNano();
	}

	public String getDuration() {
		String timeStart = contentList.get(0).getStartTime().split("T")[1]
				.split("F")[0];
		String timeEnd = contentList.get(contentList.size() - 1).getEndTime()
				.split("T")[1].split("F")[0];
		String[] values1 = timeStart.split(":");
		String[] values2 = timeEnd.split(":");
		int[] intValues1 = new int[values1.length];
		int[] intValues2 = new int[values2.length];
		for (int i = 0; i < values1.length; i++) {
			intValues1[i] = Integer.valueOf(values1[i]);
			intValues2[i] = Integer.valueOf(values2[i]);
		}
		int hun, sek, min, hours;
		boolean checkhun = false;
		boolean checksek = false;
		boolean checkmin = false;
		if(intValues2[3] < intValues1[3]) {
			checkhun = true;
			hun = 1000 - (intValues1[3] - intValues2[3]);
		} else {
			hun = intValues2[3] - intValues1[3];
		}
		
		if(checkhun) {
			if((intValues2[2]) < intValues1[2] + 1) {
				checksek = true;
				sek = 60 - (intValues1[2] - intValues2[2] - 1);
			} else {
				sek = intValues2[2] - intValues1[2] - 1;
			}
		} else {
			if(intValues2[2] < intValues1[2]) {
				checksek = true;
				sek = 60 - (intValues1[2] - intValues2[2]);
			} else {
				sek = intValues2[2] - intValues1[2];
			}
		}
		
		if(checksek) {
			if((intValues2[1]) < (intValues1[1] + 1)) {
				checkmin = true;
				min = 60 - (intValues1[1] - intValues2[2] - 1);
			} else {
				min = intValues2[1] - intValues1[1] - 1;
			}
		} else {
			if(intValues2[1] < intValues1[1]) {
				checkmin = true;
				min = 60 - (intValues1[1] - intValues2[1]);
			} else {
				min = intValues2[1] - intValues1[1];
			}
		}
		
		if(checkmin) {
			if((intValues2[0]) < (intValues1[0] + 1)) {
				hours = 60 - (intValues1[0] - intValues2[0] - 1);
			} else {
				hours = intValues2[0] - intValues1[0] - 1;
			}
		} else {
			if(intValues2[0] < intValues1[0]) {
				hours = 60 - (intValues1[0] - intValues2[0]);
			} else {
				hours = intValues2[0] - intValues1[0];
			}
		}

		int amountdays = (int) Math.floor(hours / 24);
		hours = hours % 24;
		String stdvalue, minvalue, sekvalue, hunvalue, days;

		stdvalue = hours < 10 ? "0" + hours : "" + hours;
		minvalue = min < 10 ? "0" + min : "" + min;
		sekvalue = sek < 10 ? "0" + sek : "" + sek;
		hunvalue = hun < 10 ? "0" + hun : "" + hun;
		days = amountdays < 10 ? "0" + amountdays : "" + amountdays;

		return "PT" + stdvalue + "H" + minvalue + "M" + sekvalue + "S"
				+ hunvalue + "N" + "1000F";
	}
	
	public void setName(String name) {
		this.sceneName = name;
	}
	
	public String getName() {
		return sceneName;
	}

	@Override
	public Scene clone() {
		Scene scene = new Scene(this.getSceneId());
		for (int i = 0; i < contentList.size(); i++) {
			scene.addShots(contentList.get(i).clone().getShot(), false);
		}
		scene.setSettings();
		return scene;
	}

	class Content implements Comparable<Content>, Cloneable {

		private Shot shot;

		private boolean ambiguous;

		Content(Shot shot, boolean ambiguous) {
			this.shot = shot.clone();
			this.ambiguous = ambiguous;
		}

		Shot getShot() {
			return shot;
		}

		String getStartTime() {
			return shot.getStartTime();
		}

		String getEndTime() {
			return shot.getEndTime();
		}

		boolean isAmbiguous() {
			return ambiguous;
		}

		@Override
		public int compareTo(Content o) {
			if (shot.compareTo(o.getShot()) == 0) {
				return 0;
			} else if (shot.compareTo(o.getShot()) == 1) {
				return 1;
			} else {
				return -1;
			}
		}

		@Override
		public Content clone() {
			Shot cloneShot = shot.clone();
			Content content = new Content(cloneShot, ambiguous);
			return content;
		}
	}
}

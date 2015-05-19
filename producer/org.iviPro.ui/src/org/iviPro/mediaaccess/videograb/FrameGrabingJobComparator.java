package org.iviPro.mediaaccess.videograb;

import java.util.Comparator;

public class FrameGrabingJobComparator implements Comparator<FrameGrabingJob> {

	@Override
	public int compare(FrameGrabingJob o1, FrameGrabingJob o2) {
		if (o1.getTimestampAsNanos() < o2.getTimestampAsNanos()) {
			return -1;
		} else
		if (o1.getTimestampAsNanos() > o2.getTimestampAsNanos()) {
			return 1;
		}
		return 0;
	}
}

package org.iviPro.transcoder;

public class MediaSection {

	public static final MediaSection ENTIRE_SECTION = new MediaSection(-1, -1);

	private final long startTime;
	private final long duration;

	public MediaSection() {
		this.startTime = -1;
		this.duration = -1;
	}

	public MediaSection(long startTime, long duration) {
		this.startTime = startTime;
		this.duration = duration;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getDuration() {
		return duration;
	}

	@Override
	public String toString() {
		return "MediaSection [startTime=" + startTime + ", duration="
				+ duration + "]";
	}

}

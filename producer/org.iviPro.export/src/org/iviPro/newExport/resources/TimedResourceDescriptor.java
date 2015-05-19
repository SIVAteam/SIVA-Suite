package org.iviPro.newExport.resources;

import java.io.File;

public class TimedResourceDescriptor extends ResourceDescriptor {

	private final long startTime;
	private final long endTime;

	public TimedResourceDescriptor(File source, String target, long startTime,
			long endTime) {
		super(source, target);
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	@Override
	public String toString() {
		return "TimedResourceDescriptor [" + super.toString() + ", startTime=" //$NON-NLS-1$ //$NON-NLS-2$
				+ startTime + ", endTime=" + endTime + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}

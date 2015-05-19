package org.iviPro.newExport.resources;

import java.io.File;

public class ResourceDescriptor {

	private final File source;
	private final String target;

	public ResourceDescriptor(File source, String target) {
		this.source = source;
		this.target = target;
	}

	public File getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return "ResourceDescriptor [source=" + source + ", target=" + target //$NON-NLS-1$ //$NON-NLS-2$
				+ "]"; //$NON-NLS-1$
	}
}

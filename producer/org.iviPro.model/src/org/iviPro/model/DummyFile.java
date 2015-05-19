package org.iviPro.model;

import java.io.File;

/**
 * Dummy-Datei die darstellt, dass es fuer einen IFileBaseObject, der eine Datei
 * dieses Typs hat noch keine reale Datei gibt.
 * 
 * @author dellwo
 * 
 */
public final class DummyFile extends File {

	public static final DummyFile DUMMY_FILE = new DummyFile();

	private DummyFile() {
		super(""); //$NON-NLS-1$
	}

	@Override
	public String getAbsolutePath() {
		return DummyFile.class.getName();
	}

	@Override
	public String getName() {
		return DummyFile.class.getName();
	}

	@Override
	public String getPath() {
		return DummyFile.class.getName();
	}

}
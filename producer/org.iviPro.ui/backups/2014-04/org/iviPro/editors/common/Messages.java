package org.iviPro.editors.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.iviPro.editors.common.messages"; //$NON-NLS-1$

	public static String EditTime_Label_EndTime;
	public static String EditTime_Label_StartTime;
	public static String BeanNameGenerator_ScenePostfix;
	public static String BeanNameGenerator_AudioPartPostfix;
	public static String BeanNameGenerator_AnnotationPostfix;
	public static String BeanNameGenerator_GeneratedNameLabel;
	public static String BeanNameGenerator_ShellLabel;
	public static String BeanNameGenerator_OK_BUTTON;
	public static String BeanNameGenerator_CANCEL_BUTTON;
	public static String BeanComparator_SortByDuration;
	public static String BeanComparator_SortByEndTime;
	public static String BeanComparator_SortByName;
	public static String BeanComparator_SortByStarttime;
	public static String BeanComparator_SortByType;
	public static String BeanComparator_ShellMessage;
	public static String AudioPrefix;
	public static String SubtitlePrefix;
	public static String RichtextPrefix;
	public static String VideoPrefix;
	public static String PicturePrefix;
	public static String TextPrefix;
	public static String Label_Sort_By;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}

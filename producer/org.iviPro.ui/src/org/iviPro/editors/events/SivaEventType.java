package org.iviPro.editors.events;

/**
 * Value Typen, diese Typen werden von den Value Provider bzw. Consumer
 * zur Kommunikation und den Datenaustausch verwendet
 * @author juhoffma
 *
 */
public enum SivaEventType {
	
	NO_TYPE, 
	STARTTIME_CHANGED, 
	ENDTIME_CHANGED, 
	MARK_POINT_START, 
	MARK_POINT_END, 
	MEDIATIME_CHANGED, 
	VIDEO_STARTED, 
	VIDEO_STOPPED, 
	VIDEO_END_REACHED, 
	VOLUME_CHANGED, 
	CONTENT_CHANGED,
	CONTENT_REPLACED,
	DESCRIPTION_CHANGED,
	TIME_SELECTION,
	SCREEN_AREA_CHANGED, 
	OVERLAY_PATH_CHANGED, 
	OVERLAY_MARKS_CHANGED,
	BEANCOMPARATOR_CHANGED;
}

package org.iviPro.editors.annotationeditor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.iviPro.editors.annotationeditor.messages"; //$NON-NLS-1$

	public static String GlobalAnnotationEditor_CreateGlobalAnnotation2_Tooltip;

	public static String GlobalAnnotationEditor_Label;

	public static String GlobalAnnotationEditor_Remove;

	public static String GlobalAnnotationEditor_TreeNodeAudio;

	public static String GlobalAnnotationEditor_TreeNodePicture;

	public static String GlobalAnnotationEditor_TreeNodeRichtext;

	public static String GlobalAnnotationEditor_TreeNodeSubtitle;

	public static String GlobalAnnotationEditor_TreeNodeText;

	public static String GlobalAnnotationEditor_TreeNodeVideo;

	public static String AnnotationEditor_MsgBox_SaveAnnotation_Text;

	public static String AnnotationEditor_MsgBox_SaveAnnotation_Title;

	public static String AnnotationEditor_Sort_Tooltip;

	public static String AnnotationEditor_TabTitle_UnnamedAnnotation1;

	public static String AnnotationEditor_TabTitle_UnnamedAnnotation2;
	
	public static String GlobalAnnotationEditorName;

	public static String LabelSearch;
	public static String LabelClear;
	
	public static String AnnotationType_CreateButtonText1;
	public static String AnnotationType_CreateButtonText2;
	
	public static String AnnotationType_GROUP1_NAME;
	public static String AnnotationType_GROUP2_NAME;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}

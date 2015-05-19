package org.iviPro.editors.annotationeditor.annotationfactory;


/**
 * Auflistung der Annotationstypen, für normale Annotationen, wird verwendet
 * um die Annotationen im Annotationseditor zu unterscheiden und um die Annotationsobjekte 
 * zu erstellen
 * Der ContentType wird seperat behandelt, da versch. Annotationstypen den gleichen Inhalt haben können
 * z.B. Bild - Standard Annotation / Bild - Mark Annotation
 * @author juhoffma
 */
public enum AnnotationType {

	// Annotationstypen die erstellt werden können, wird zum Anlegen der neuen Annotation
	// und für die Menüauswahl verwendet etc ...
	// Parameter 3 gibt an ob der Annotationstyp auch für globale Annotationen verwendet werden kann
	AUDIO(Messages.TypeName_Audio, AnnotationGroup.STANDARD, AnnotationContentType.AUDIO, true, true), 
	PICTURE(Messages.TypeName_Picture, AnnotationGroup.STANDARD, AnnotationContentType.PICTURE, true, true), 
	RICHTEXT(Messages.TypeName_Richtext, AnnotationGroup.STANDARD, AnnotationContentType.RICHTEXT, true, true), 
	SUBTITLE(Messages.TypeName_Subtitle, AnnotationGroup.STANDARD, AnnotationContentType.SUBTITLE, true, true), 
	TEXT(Messages.TypeName_Text, AnnotationGroup.STANDARD, AnnotationContentType.TEXT, true, false), 
	VIDEO(Messages.TypeName_Video, AnnotationGroup.STANDARD, AnnotationContentType.VIDEO, true, true),
	PDF(Messages.TypeName_Pdf, AnnotationGroup.STANDARD, AnnotationContentType.PDF, true, true),
	
	MARK_AUDIO(Messages.TypeName_Audio_Mark, AnnotationGroup.MARK, AnnotationContentType.AUDIO, true), 
	MARK_PICTURE(Messages.TypeName_Picture_Mark, AnnotationGroup.MARK, AnnotationContentType.PICTURE, true), 
	MARK_RICHTEXT(Messages.TypeName_Richtext_Mark, AnnotationGroup.MARK, AnnotationContentType.RICHTEXT, true), 
	MARK_TEXT(Messages.TypeName_Text_Mark, AnnotationGroup.MARK, AnnotationContentType.TEXT, false), 
	MARK_VIDEO(Messages.TypeName_Video_Mark, AnnotationGroup.MARK, AnnotationContentType.VIDEO, true),
	MARK_PDF(Messages.TypeName_Pdf_Mark, AnnotationGroup.MARK, AnnotationContentType.PDF, true);
			
	// gibt an ob der Annotationstyp auch für globale Annotationen verwendet werden soll
	private boolean allowGlobal = false;
	
	// die Gruppe zu der die Annotation gehört
	private AnnotationGroup annotationGroup;
	
	// der Content Type der Annotation
	private AnnotationContentType contentType;
	
	// der Name des Annotationstyp z.B. für die Label zum Erstellen einer Annotation
	private String typeName;
	
	// Flag damit eine Annotation nicht in der Neu-Erstellungsauswahl auftaucht
	private boolean use;
	
	private AnnotationType(String typeName, AnnotationGroup group, AnnotationContentType contentType, boolean inUse) {
		this.annotationGroup = group;
		this.contentType = contentType;
		this.use = inUse;
		this.typeName = typeName;
	}
	
	private AnnotationType(String typeName, AnnotationGroup group, AnnotationContentType contentType, boolean allowGlobal, boolean inUse) {
		this.annotationGroup = group;
		this.contentType = contentType;
		this.allowGlobal = allowGlobal;
		this.use = inUse;
		this.typeName = typeName;
	}
	
	public boolean inUse() {
		return this.use;
	}
	
	/**
	 * liefert für den aktuellen AnnotationsTyp einen Namen
	 * wird für das Menü zum Erstellen einer Annotation verwendet
	 * @return
	 */
	public String getName() {
		return this.typeName;
	}
	
	public String getMenuName() {
		if (this.name().contains("MARK_")) { //$NON-NLS-1$
			return this.name().substring(this.name().indexOf("MARK_") + 5); //$NON-NLS-1$
		}
		return this.name();
	}
	
	/**
	 * liefert die möglichen Annotationsgruppen
	 * @return
	 */
	public static AnnotationGroup[] getAnnotationGroups() {
		return AnnotationGroup.values();
	}
	
	/**
	 * liefert die Gruppe der Annotation zurück
	 * @return
	 */
	public AnnotationGroup getAnnotationGroup() {
		return this.annotationGroup;
	}
	
	/**
	 * liefert den Content Typ der Annotation
	 * @return
	 */
	public AnnotationContentType getContentType() {
		return this.contentType;
	}
	
	/**
	 * prüft ob die Annotation einen bestimmten Content Type hat
	 */
	
	
	/**
	 * gibt an ob die Annotation auch global angelegt werden kann
	 * @return
	 */
	public boolean allowGlobal() {
		return this.allowGlobal;
	}	
}

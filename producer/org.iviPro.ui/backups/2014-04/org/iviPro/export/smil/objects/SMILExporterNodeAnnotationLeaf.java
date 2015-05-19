package org.iviPro.export.smil.objects;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.iviPro.export.ExportException;
import org.iviPro.export.xml.IDManager;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IFileBasedObject;
import org.iviPro.model.LocalizedFile;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.Project;
import org.iviPro.model.annotation.OverlayPathItem;
import org.iviPro.model.graph.INodeAnnotationLeaf;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.ScreenArea;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.iviPro.model.graph.NodeAnnotationRichtext;

public abstract class SMILExporterNodeAnnotationLeaf extends SMILExporter {

	public SMILExporterNodeAnnotationLeaf(IAbstractBean object) {
		super(object);
	}

	@Override
	protected void exportObjectImpl(IAbstractBean exportObj, Document doc,
			IDManager idManager, Project project,
			Set<IAbstractBean> alreadyExported, Element parent)
			throws ExportException {

		INodeAnnotationLeaf annotation = (INodeAnnotationLeaf) exportObj;
		
		// Create a switch-element for the annotation, because only one of its
		// children will be displayed
		Element switchElement = doc.createElement(TAG_SWITCH);

		ScreenArea screenArea = annotation.getScreenArea();
		if(screenArea == null) {
			screenArea = ScreenArea.OVERLAY;
		}
		String annotationRegion = screenArea.toString().toLowerCase();

		if (annotationRegion.equals("overlay")) {
			annotationRegion = VAL_MAIN_REGION_ID;
		} else {
			annotationRegion += "_region";
		}

		long startTime = 0;
		long annotationDuration = 0;
		// Annotations that are not corresponding to a mark, are considered
		// normal
		boolean normalAnnotation = false;

		if (annotation.getEnd() != null && annotation.getStart() != null) {
			startTime = annotation.getStartRelativeToScene();
			annotationDuration = (annotation.getEnd() - annotation.getStart());
			normalAnnotation = true;
		}

		IFileBasedObject fileObject = getFileBasedObject();
		if (fileObject == null) {
			// Text-based annotation
			Collection<LocalizedString> texts = null;
			if (annotation instanceof NodeAnnotationSubtitle) {
				texts = ((NodeAnnotationSubtitle) annotation).getSubtitle()
						.getDescriptions();
			} else {
				texts = annotation.getDescriptions();
			}
			if (texts.isEmpty()) {
				// There has to be at least one file
			} else {
				for (LocalizedString text : texts) {
					String curLangCode = text.getSivaLangcode();
					Element languageChoice = doc.createElement(getTagName());
					languageChoice.setAttribute(ATTR_SYSTEMLANGUAGE,
							curLangCode);
					languageChoice.setAttribute(ATTR_REGION, annotationRegion);
					languageChoice.setTextContent(text.getValue());
					switchElement.appendChild(languageChoice);
				}
			}
		} else {
			// File-based annotation
			Collection<LocalizedFile> files = fileObject.getFiles();
			LocalizedFile defFile = null;
			Locale defLanguage = project.getDefaultLanguage();
			if (files.isEmpty()) {
				// There has to be at least one file
			} else {
				for (LocalizedFile file : files) {
					Locale language = file.getLanguage();
					String filename = "../"
							+ getSubDirectory()
							+ "/"
							+ idManager.getFilename(fileObject, language,
									getExportParameters());
					String curLangCode = LocalizedString
							.getSivaLangcode(language);
					String id = idManager.getID(annotation) + "_" + curLangCode;
					Element languageChoice = doc.createElement(getTagName());
					languageChoice.setAttribute(ATTR_ID, id);
					languageChoice.setAttribute(ATTR_SYSTEMLANGUAGE,
							curLangCode);
					if(annotation instanceof NodeAnnotationRichtext) {
						if(language == defLanguage) {
							defFile = file;
						}
						String textContent = htmlContent(file, annotation);
						
						languageChoice.setTextContent(textContent);
					} else {
						languageChoice.setAttribute(ATTR_SOURCE, filename);						
					}
					if (normalAnnotation) {
						languageChoice.setAttribute(ATTR_DURATION,
								toNanoString(annotationDuration));
						languageChoice
								.setAttribute(ATTR_BEGIN,
										toNanoString(startTime));
					}
					generatePositioning(languageChoice, annotation, doc,
							annotationDuration, normalAnnotation);
					switchElement.appendChild(languageChoice);
				}

				// Default element - added here, because it must be the last
				// child of the switch-element in order to be selected only if
				// none of the language-choices has been taken
				String filename = "../"
						+ getSubDirectory()
						+ "/"
						+ idManager.getFilename(fileObject, defLanguage,
								getExportParameters());
				String id = idManager.getID(annotation) + "_def";
				Element languageChoice = doc.createElement(getTagName());
				languageChoice.setAttribute(ATTR_ID, id);
				if(annotation instanceof NodeAnnotationRichtext) {
					if(defFile != null) {
						String textContent = htmlContent(defFile, annotation);	
						
						languageChoice.setTextContent(textContent);
					} else {
						throw new ExportException("Error while building default Richtext-file.");
					}
					
				} else {					
					languageChoice.setAttribute(ATTR_SOURCE, filename);
				}
				if (normalAnnotation) {
					languageChoice.setAttribute(ATTR_DURATION,
							toNanoString(annotationDuration));
					languageChoice.setAttribute(ATTR_BEGIN,
							toNanoString(startTime));
				}
				generatePositioning(languageChoice, annotation, doc,
						annotationDuration, normalAnnotation);
				switchElement.appendChild(languageChoice);
			}
		}

		if (!annotation.isPauseVideo()) {
			parent.appendChild(switchElement);
		} else {
			handlePauseAnnotation(parent, switchElement, doc);
		}

	}
	
	/**
	 * Method reads out the content of a given HTML-File and alters it according to be placeable as text content for an element. The result is given as a String.
	 * 
	 * @param file	The HTML-File to be read.
	 * @param annotation	The annotation that is supposed to show the content.
	 * @return	A String with the content of the HTML-File.
	 * @throws ExportException
	 */
	private String htmlContent (LocalizedFile file, INodeAnnotationLeaf annotation) throws ExportException {
		StringBuffer buffer = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(new FileInputStream(file.getValue()), "UTF8"));
			
			String line = null;
			
			while((line = in.readLine()) != null) {
				line = changeFileLine(line);
				buffer.append(line);
			}
			
			in.close();						
		} catch (FileNotFoundException e) {
			throw new ExportException("Fehler beim Export von " + annotation.getTitle() + " (FileNotFound)");
		} catch (IOException e) {
			throw new ExportException("Fehler beim Export von " + annotation.getTitle() + " (IO)");
		}
		
		return buffer.toString();
	}

	/**
	 * Creates the specific positioning for the given annotationElement and its
	 * corresponding annotation.
	 * 
	 * @param annotationElement
	 *            The element that has been created for this annotation.
	 * @param annotation
	 *            The annotation that is to be exported.
	 * @param doc
	 *            The document where the SMIL-file is written.
	 */
	private void generatePositioning(Element annotationElement,
			INodeAnnotationLeaf annotation, Document doc, long duration,
			boolean normalAnnotation) {
		if (requiresPositionInfo()) {

			ScreenArea screenArea = annotation.getScreenArea();
			if (screenArea == null) {
				// If no screenarea has been set for the annotation, it will be
				// an overlayed annotation
				screenArea = ScreenArea.OVERLAY;
			}

			if (screenArea.equals(ScreenArea.OVERLAY)) {
				List<OverlayPathItem> overlayPath = annotation.getOverlayPath();

				annotationElement.setAttribute(ATTR_REGION,
						VAL_ANIMATION_REGION_ID);

				if (!overlayPath.isEmpty()) {

					if (overlayPath.size() > 1) {

						Element animateTop = doc.createElement(TAG_ANIMATION);
						animateTop.setAttribute(ATTR_TARGETELEMENT,
								VAL_ANIMATION_REGION_ID);
						animateTop.setAttribute(ATTR_ATTRIBUTENAME, VAL_TOP);
						animateTop.setAttribute(ATTR_CALCMODE, VAL_DISCRETE);

						Element animateLeft = doc.createElement(TAG_ANIMATION);
						animateLeft.setAttribute(ATTR_TARGETELEMENT,
								VAL_ANIMATION_REGION_ID);
						animateLeft.setAttribute(ATTR_ATTRIBUTENAME, VAL_LEFT);
						animateLeft.setAttribute(ATTR_CALCMODE, VAL_DISCRETE);

						Element animateWidth = doc.createElement(TAG_ANIMATION);
						animateWidth.setAttribute(ATTR_TARGETELEMENT,
								VAL_ANIMATION_REGION_ID);
						animateWidth
								.setAttribute(ATTR_ATTRIBUTENAME, VAL_WIDTH);
						animateWidth.setAttribute(ATTR_CALCMODE, VAL_DISCRETE);

						Element animateHeight = doc
								.createElement(TAG_ANIMATION);
						animateHeight.setAttribute(ATTR_TARGETELEMENT,
								VAL_ANIMATION_REGION_ID);
						animateHeight.setAttribute(ATTR_ATTRIBUTENAME,
								VAL_HEIGHT);
						animateHeight.setAttribute(ATTR_CALCMODE, VAL_DISCRETE);

						String topValues = "";
						String leftValues = "";
						String widthValues = "";
						String heightValues = "";

						for (int i = 0; i < overlayPath.size(); i++) {
							OverlayPathItem item = overlayPath.get(i);

							String itemTopValue = changeToPercentageString(item
									.getY());
							String itemLeftValue = changeToPercentageString(item
									.getX());
							String itemWidthValue = changeToPercentageString(item
									.getWidth());
							String itemHeightValue = changeToPercentageString(item
									.getHeight());

							topValues += itemTopValue;
							leftValues += itemLeftValue;
							widthValues += itemWidthValue;
							heightValues += itemHeightValue;

							if (i < overlayPath.size() - 1) {
								topValues += "; ";
								leftValues += "; ";
								widthValues += "; ";
								heightValues += "; ";
							}
						}

						if (normalAnnotation) {
							String durationString = toNanoString(duration);
							animateLeft.setAttribute(ATTR_DURATION,
									durationString);
							animateTop.setAttribute(ATTR_DURATION,
									durationString);
							animateWidth.setAttribute(ATTR_DURATION,
									durationString);
							animateHeight.setAttribute(ATTR_DURATION,
									durationString);
						}

						animateLeft.setAttribute(ATTR_VALUES, leftValues);

						animateTop.setAttribute(ATTR_VALUES, topValues);

						animateWidth.setAttribute(ATTR_VALUES, widthValues);

						animateHeight.setAttribute(ATTR_VALUES, heightValues);

						annotationElement.appendChild(animateTop);
						annotationElement.appendChild(animateLeft);
						annotationElement.appendChild(animateWidth);
						annotationElement.appendChild(animateHeight);
					} else {
						OverlayPathItem onlyItem = overlayPath.get(0);

						annotationElement.setAttribute(ATTR_TOP,
								changeToPercentageString(onlyItem.getY()));
						annotationElement.setAttribute(ATTR_LEFT,
								changeToPercentageString(onlyItem.getX()));
						annotationElement.setAttribute(ATTR_WIDTH,
								changeToPercentageString(onlyItem.getWidth()));
						annotationElement.setAttribute(ATTR_HEIGHT,
								changeToPercentageString(onlyItem.getHeight()));
					}

				} else {

				}
			} else {
				String displayAnnotationRegion = screenArea.toString()
						.toLowerCase() + "_region";
				annotationElement.setAttribute(ATTR_REGION,
						displayAnnotationRegion);
			}
		} else {
			// Subtitles are placed in a special subregion in the main region,
			// audio-annotations do not need a region at all
			if (annotation instanceof NodeAnnotationSubtitle) {
				annotationElement.setAttribute(ATTR_REGION, VAL_MAIN_REGION_ID);
				annotationElement.setAttribute(ATTR_LEFT, VAL_TWENTY_PERCENT);
				annotationElement.setAttribute(ATTR_TOP, VAL_EIGHTY_PERCENT);
			}
		}
	}
	
	/**
	 * Method changes some HTML-sequences into something that SMIL can display and work with.
	 * 
	 * @param line	The line that is to be checked for sequences.
	 */
	private String changeFileLine (String line) {
		String replacement = line;
		
		replacement = replacement.replaceAll("&szlig;", "ss");
		replacement = replacement.replaceAll("&uuml;", "ue");
		replacement = replacement.replaceAll("&auml;", "ae");
		replacement = replacement.replaceAll("&ouml;", "oe");
		replacement = replacement.replaceAll("&Uuml;", "Ue");
		replacement = replacement.replaceAll("&Auml;", "Ae");
		replacement = replacement.replaceAll("&Ouml;", "Oe");
		replacement = replacement.replaceAll("<p>", "");
		replacement = replacement.replaceAll("</p>", "<br/>");
		replacement = replacement.replaceAll("<br />", "<br/>");
		
		return replacement;
	}

	/**
	 * Method changes a given float number (coming from the SIVA-Model) into a
	 * String representation that SMIL can use.
	 * 
	 * @param number
	 *            The number to be changed.
	 * @return A String-representation of the given number.
	 */
	private String changeToPercentageString(float number) {
		int percentage = (int) (number * 100);

		return percentage + "%";
	}

	/**
	 * Returns the tag-name for the specific annotation.
	 * 
	 * @return The tag-name for the specific annotation.
	 */
	protected abstract String getTagName();

	/**
	 * Returns the name for the subdirectory, where the files for the annotation
	 * can be found.
	 * 
	 * @return The name of the subdirectory.
	 */
	protected abstract String getSubDirectory();

	/**
	 * Method to question if the specific annotation needs its own position.
	 * 
	 * @return True, if the annotation requires a position.
	 */
	protected abstract boolean requiresPositionInfo();

	/**
	 * Returns the file corresponding to the specific annotation.
	 * 
	 * @return The file that corresponds to the specific annotation.
	 */
	protected abstract IFileBasedObject getFileBasedObject();

}

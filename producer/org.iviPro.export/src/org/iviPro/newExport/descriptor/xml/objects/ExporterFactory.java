package org.iviPro.newExport.descriptor.xml.objects;

import org.iviPro.model.Project;
import org.iviPro.model.TocItem;
import org.iviPro.model.graph.AbstractNodeSelection;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPdf;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.graph.NodeEnd;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeRandomSelection;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeStart;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;

/**
 * A factory used to create xml exporters for all objects in the project which
 * contain information which needs to be exported to xml.
 */
public class ExporterFactory {

	/**
	 * Creates an xml exporter for the submitted object.
	 * 
	 * @param objectToExport
	 *            The object to export.
	 * @return The corresponding xml exporter
	 * @throws ExportException
	 *             If there is no exporter defined for the submitted object.
	 */
	public static IXMLExporter createExporter(Object objectToExport)
			throws ExportException {
		if (objectToExport instanceof Project) {
			return new XMLExporterProject((Project)objectToExport);
		} else if (objectToExport instanceof NodeScene) {
			return new XMLExporterNodeScene((NodeScene)objectToExport);
		} else if (objectToExport instanceof NodeAnnotationAudio) {
			return new XMLExporterNodeAnnotationAudio((NodeAnnotationAudio)objectToExport);
		} else if (objectToExport instanceof NodeAnnotationPicture) {
			return new XMLExporterNodeAnnotationPicture((NodeAnnotationPicture)objectToExport);
		} else if (objectToExport instanceof NodeAnnotationRichtext) {
			return new XMLExporterNodeAnnotationRichtext((NodeAnnotationRichtext)objectToExport);
		} else if (objectToExport instanceof NodeAnnotationSubtitle) {
			return new XMLExporterNodeAnnotationSubtitle((NodeAnnotationSubtitle)objectToExport);
		} else if (objectToExport instanceof NodeAnnotationVideo) {
			return new XMLExporterNodeAnnotationVideo((NodeAnnotationVideo)objectToExport);
		} else if (objectToExport instanceof NodeAnnotationPdf) {
			return new XMLExporterNodeAnnotationPdf((NodeAnnotationPdf)objectToExport);
		} else if (objectToExport instanceof NodeStart) {
			return new XMLExporterNodeStart((NodeStart)objectToExport);
		} else if (objectToExport instanceof NodeEnd) {
			return new XMLExporterNodeEnd((NodeEnd)objectToExport);
		} else if (objectToExport instanceof AbstractNodeSelection) {
			return new XMLExporterAbstractNodeSelection((AbstractNodeSelection)objectToExport);
		} else if (objectToExport instanceof NodeMark) {
			return new XMLExporterNodeMark((NodeMark)objectToExport);
		} else if (objectToExport instanceof TocItem) {
			return new XMLExporterTocItem((TocItem)objectToExport);
		} else if (objectToExport instanceof NodeQuiz) {
			return new XMLExporterNodeQuiz((NodeQuiz)objectToExport);
		} else if (objectToExport instanceof NodeRandomSelection) {
			return new XMLExporterNodeRandomSelection((NodeRandomSelection)objectToExport);
		} else {
			throw new ExportException(String.format(
					Messages.Exception_UnknownExportEntity, objectToExport
							.getClass().getSimpleName()));
		}
	}
}

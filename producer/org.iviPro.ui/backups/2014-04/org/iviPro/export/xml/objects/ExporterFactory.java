package org.iviPro.export.xml.objects;

import org.apache.log4j.Logger;
import org.iviPro.export.ExportException;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.TocItem;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationText;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.graph.NodeEnd;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeSelection;
import org.iviPro.model.graph.NodeStart;

public class ExporterFactory {

	private static Logger logger = Logger.getLogger(ExporterFactory.class);

	/**
	 * Erstellt einen Exporter fuer einen bestimmten Graph-Knoten
	 * 
	 * @param exportObj
	 *            Das Model-Objekt das exportiert werden soll.
	 * @return
	 * @throws ExportException
	 *             Falls kein Exporter erstellt werden konnte.
	 */
	public static IXMLExporter createExporter(IAbstractBean exportObj)
			throws ExportException {
		IXMLExporter exporter = null;

		if (exportObj == null) {
			throw new ExportException(
					"Cant create Exporter: Node must not be null."); //$NON-NLS-1$

		} else if (exportObj instanceof Project) {
			exporter = new XMLExporterProject(exportObj);

		} else if (exportObj instanceof NodeScene) {
			exporter = new XMLExporterNodeScene(exportObj);

		} else if (exportObj instanceof NodeAnnotationAudio) {
			exporter = new XMLExporterNodeAnnotationAudio(exportObj);

		} else if (exportObj instanceof NodeAnnotationPicture) {
			exporter = new XMLExporterNodeAnnotationPicture(exportObj);

		} else if (exportObj instanceof NodeAnnotationRichtext) {
			exporter = new XMLExporterNodeAnnotationRichtext(exportObj);

		} else if (exportObj instanceof NodeAnnotationSubtitle) {
			exporter = new XMLExporterNodeAnnotationSubtitle(exportObj);

		} else if (exportObj instanceof NodeAnnotationText) {
			exporter = new XMLExporterNodeAnnotationText(exportObj);

		} else if (exportObj instanceof NodeAnnotationVideo) {
			exporter = new XMLExporterNodeAnnotationVideo(exportObj);

		} else if (exportObj instanceof NodeStart) {
			exporter = new XMLExporterNodeStart(exportObj);

		} else if (exportObj instanceof NodeEnd) {
			exporter = new XMLExporterNodeEnd(exportObj);

		} else if (exportObj instanceof NodeSelection) {
			exporter = new XMLExporterNodeSelection(exportObj);

		} else if (exportObj instanceof NodeMark) {
			exporter = new XMLExporterNodeMark(exportObj);
		} else if (exportObj instanceof TocItem) {
			exporter = new XMLExporterTocItem(exportObj);
		} else if(exportObj instanceof NodeQuiz) {
			exporter = new XMLExporterNodeQuiz(exportObj);
		}
		if (exporter == null) {
			String errorMsg = "The Export of " //$NON-NLS-1$
					+ exportObj.getClass().getSimpleName()
					+ " is not implemented yet!"; //$NON-NLS-1$
			logger.error(errorMsg);
			throw new ExportException(errorMsg);
		} else {
			return exporter;
		}
	}
}

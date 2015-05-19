package org.iviPro.export.smil.objects;

import org.apache.log4j.Logger;
import org.iviPro.export.ExportException;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.Project;
import org.iviPro.model.TocItem;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.graph.NodeEnd;
import org.iviPro.model.graph.NodeMark;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeSelection;
import org.iviPro.model.graph.NodeStart;

/**
 * Factory that is used to export a smil file. Therefore the factory creates a
 * different exporter according to the node of the modelgraph it is used on.
 * 
 * @author Berndl Emanuel
 * 
 */
public class SMILExporterFactory {

	private static Logger logger = Logger.getLogger(SMILExporterFactory.class);

	/**
	 * Creates and exporter depending on the object that is given.
	 * 
	 * @param exportObj
	 *            The object of the model that is to be exported
	 * @return
	 * @throws ExportException
	 *             If no exporter could be created.
	 */
	public static SMILExporter createSMILExporter(IAbstractBean exportObj)
			throws ExportException {
		SMILExporter exporter = null;

		if (exportObj == null) {
			throw new ExportException(
					"Cant create SMILExporter: Node must not be null.");

		} else if (exportObj instanceof Project) {
			exporter = new SMILExporterProject(exportObj);

		} else if (exportObj instanceof NodeScene) {
			exporter = new SMILExporterNodeScene(exportObj);

		} else if (exportObj instanceof NodeAnnotationAudio) {
			exporter = new SMILExporterNodeAnnotationAudio(exportObj);

		} else if (exportObj instanceof NodeAnnotationPicture) {
			exporter = new SMILExporterNodeAnnotationPicture(exportObj);

		} else if (exportObj instanceof NodeAnnotationRichtext) {
			exporter = new SMILExporterNodeAnnotationRichtext(exportObj);

		} else if (exportObj instanceof NodeAnnotationSubtitle) {
			exporter = new SMILExporterNodeAnnotationSubtitle(exportObj);

		} else if (exportObj instanceof NodeAnnotationVideo) {
			exporter = new SMILExporterNodeAnnotationVideo(exportObj);

		} else if (exportObj instanceof NodeStart) {
			exporter = new SMILExporterNodeStart(exportObj);

		} else if (exportObj instanceof NodeEnd) {
			exporter = new SMILExporterNodeEnd(exportObj);

		} else if (exportObj instanceof NodeSelection) {
			exporter = new SMILExporterNodeSelection(exportObj);

		} else if (exportObj instanceof NodeMark) {
			exporter = new SMILExporterNodeMark(exportObj);

		} else if (exportObj instanceof TocItem) {
			exporter = new SMILExporterTocItem(exportObj);
			
		} else if (exportObj instanceof NodeQuiz) {
			exporter = new SMILExporterNodeQuiz(exportObj);
			
		}
		if (exporter == null) {
			String errorMsg = "The SMIL-Export of " //$NON-NLS-1$
					+ exportObj.getClass().getSimpleName()
					+ " is not implemented yet!"; //$NON-NLS-1$
			logger.error(errorMsg);
			throw new ExportException(errorMsg);
		} else {
			return exporter;
		}
	}
}

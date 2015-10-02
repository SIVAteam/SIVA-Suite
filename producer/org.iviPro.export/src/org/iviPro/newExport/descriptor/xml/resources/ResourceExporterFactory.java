package org.iviPro.newExport.descriptor.xml.resources;

import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.IResource;
import org.iviPro.model.resources.PdfDocument;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Subtitle;
import org.iviPro.model.resources.Video;
import org.iviPro.model.resources.VideoThumbnail;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;

public class ResourceExporterFactory {
	public static IXMLResourceExporter createExporter(IResource objectToExport)
			throws ExportException {
		if (objectToExport instanceof Video) {
			return new XMLResourceExporterVideo((Video)objectToExport);
		} else if (objectToExport instanceof Scene) {
			Scene scene = (Scene)objectToExport;
			return new XMLResourceExporterVideo(scene, 
					scene.getStart(), scene.getEnd());
		} else if (objectToExport instanceof VideoThumbnail) {
			return new XMLResourceExporterVideoThumbnail(
					(VideoThumbnail)objectToExport);
		} else if (objectToExport instanceof Audio) {
			return new XMLResourceExporterAudio((Audio)objectToExport);
		} else if (objectToExport instanceof AudioPart) {
			AudioPart audioPart = (AudioPart) objectToExport;
			return new XMLResourceExporterAudio(audioPart, 
					audioPart.getStart(), audioPart.getEnd());
		} else if (objectToExport instanceof Picture) {
			return new XMLResourceExporterPicture((Picture)objectToExport);
		} else if (objectToExport instanceof RichText) {
			return new XMLResourceExporterRichtext((RichText)objectToExport);
		} else if (objectToExport instanceof Subtitle) {
			return new XMLResourceExporterSubtitle((Subtitle)objectToExport);
		} else if (objectToExport instanceof PdfDocument) {
			return new XMLResourceExporterPdf((PdfDocument) objectToExport);
		} else {
			throw new ExportException(String.format(
					Messages.Exception_UnknownExportEntity, objectToExport
							.getClass().getSimpleName()));
		}
	}
}

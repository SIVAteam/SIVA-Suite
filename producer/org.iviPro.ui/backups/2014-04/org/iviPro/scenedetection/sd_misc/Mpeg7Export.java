package org.iviPro.scenedetection.sd_misc;

import java.io.File;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.iviPro.scenedetection.sd_main.Scene;
import org.iviPro.scenedetection.sd_main.Shot;
import mpeg7.ControlledTermUseType;
import mpeg7.CreationInformationType;
import mpeg7.CreationToolType;
import mpeg7.CreationType;
import mpeg7.KeywordAnnotationType;
import mpeg7.MediaFormatType;
import mpeg7.MediaIdentificationType;
import mpeg7.MediaInformationType;
import mpeg7.MediaInstanceType;
import mpeg7.MediaLocatorType;
import mpeg7.MediaProfileType;
import mpeg7.MediaTimeType;
import mpeg7.Mpeg7;
import mpeg7.ObjectFactory;
import mpeg7.SegmentDecompositionType;
import mpeg7.SpatioTemporalSegmentDecompositionType;
import mpeg7.StillRegionType;
import mpeg7.TermUseType;
import mpeg7.TextualType;
import mpeg7.TitleType;
import mpeg7.UniqueIDType;
import mpeg7.VideoSegmentSpatioTemporalDecompositionType;
import mpeg7.VideoSegmentTemporalDecompositionType;
import mpeg7.VideoSegmentType;
import mpeg7.VideoType;
import mpeg7.MediaFormatType.VisualCoding;
import mpeg7.MediaFormatType.VisualCoding.Format;
import mpeg7.KeywordAnnotationType.Keyword;
import mpeg7.SegmentType.TextAnnotation;

public class Mpeg7Export {

	private Mpeg7 mpeg7;

	private ObjectFactory objectFactory;

	private VideoType videoType;

	public Mpeg7Export() {
		objectFactory = new ObjectFactory();
		mpeg7 = objectFactory.createMpeg7();
		videoType = objectFactory.createVideoType();
		videoType.setVideo(objectFactory.createVideoSegmentType());
		mpeg7.setDescriptionUnit(videoType);
	}

	public void writeXmlFile(File fileInput) {

		File file;
		File outdir = new File("mpeg7output");
		if (!outdir.isDirectory()) {
			outdir.mkdir();
		}

		file = new File(outdir, fileInput.toString());
		System.out.println(fileInput.toString());
		try {
			JAXBContext jc = JAXBContext.newInstance("mpeg7");
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(mpeg7, file);
		} catch (JAXBException ex) {
			ex.printStackTrace();
		}
	}

	void addGeneralMediaInfo(MediaInfo m) {
		MediaInformationType mit = objectFactory.createMediaInformationType();

		MediaIdentificationType midt = objectFactory
				.createMediaIdentificationType();
		UniqueIDType uidt = objectFactory.createUniqueIDType();
		midt.setEntityIdentifier(uidt);
		mit.setMediaIdentification(midt);

		MediaProfileType mpt = objectFactory.createMediaProfileType();
		if (m.mediaFormatParams()) {
			MediaFormatType mft = objectFactory.createMediaFormatType();

			if (m.getFileSize() != null) {
				mft.setFileSize(m.getFileSize());
			}

			if (m.getContent() != null) {
				ControlledTermUseType ctut = objectFactory
						.createControlledTermUseType();
				ctut.setHref("urn:mpeg:mpeg7:cs:ContentCS:2001:4:2");
				TextualType tt = objectFactory.createTextualType();
				tt.setValue(m.getContent());
				ctut.getDefinition().add(tt);

				mft.setContent(ctut);
			}

			if (m.getColorDomain() != null) {
				VisualCoding vc = new VisualCoding();
				Format f = objectFactory
						.createMediaFormatTypeVisualCodingFormat();
				f.setColorDomain(m.getColorDomain());
				vc.setFormat(f);
				mft.setVisualCoding(vc);
			}

			if (m.getFileType() != null) {
				ControlledTermUseType ctut = objectFactory
						.createControlledTermUseType();
				ctut.setHref("urn:mpeg:mpeg7:cs:FileFormatCS:2001:7");
				TextualType tt = objectFactory.createTextualType();
				tt.setValue(m.getFileType());
				ctut.getDefinition().add(tt);
				mft.setFileFormat(ctut);
			}
			mpt.setMediaFormat(mft);
		}
		if (m.mediaInstanceParams()) {
			MediaInstanceType mint = objectFactory.createMediaInstanceType();

			if (m.getMediaUID() != null) {
				UniqueIDType uidt2 = objectFactory.createUniqueIDType();
				uidt2.setValue(m.getMediaUID());
				mint.setInstanceIdentifier(uidt2);
			}

			if (m.getUri() != null) {
				MediaLocatorType mlt = objectFactory.createMediaLocatorType();
				mlt.setMediaUri(m.getUri());
				mint.setMediaLocator(mlt);
			}
			mint.setInstanceIdentifier(uidt);
			mpt.getMediaInstance().add(mint);
		}
		mit.getMediaProfile().add(mpt);
		videoType.getVideo().setMediaInformation(mit);
	}

	public void addMetadataCreationInfo(String title, String tool) {

		if (title == null && tool == null) {
			return;
		}
		CreationInformationType cit = objectFactory
				.createCreationInformationType();
		CreationType ct = objectFactory.createCreationType();

		if (title != null) {
			TitleType tit = objectFactory.createTitleType();
			tit.setValue(title);
			ct.getTitle().add(tit);
		}

		if (tool != null) {
			CreationToolType ctt = objectFactory.createCreationToolType();
			TermUseType tut = objectFactory.createTermUseType();
			TextualType tt = objectFactory.createTextualType();
			tt.setValue(tool);
			tut.getDefinition().add(tt);
			ctt.setTool(tut);
			ct.getCreationTool().add(ctt);
		}
		cit.setCreation(ct);
		videoType.getVideo().setCreationInformation(cit);
	}

	public void setScenes(List<Scene> scenes) {
		VideoSegmentTemporalDecompositionType vstdt = objectFactory
				.createVideoSegmentTemporalDecompositionType();
		for (int i = 0; i < scenes.size(); i++) {
			Scene scene = scenes.get(i);
			VideoSegmentSpatioTemporalDecompositionType videoseg = objectFactory
					.createVideoSegmentSpatioTemporalDecompositionType();
			VideoSegmentType segment = objectFactory.createVideoSegmentType();

			Shot first = scene.getShotWithNr(0);
			StillRegionType stillRegionType = workShot(first, true);
			videoseg.getMovingRegionOrMovingRegionRefOrStillRegion().add(
					new JAXBElement<StillRegionType>(new QName(
							"urn:mpeg:mpeg7:schema:2004", "StillRegion"),
							StillRegionType.class, stillRegionType));

			Shot last = scene.getShotWithNr(scene.getNumberofShots() - 1);
			stillRegionType = workShot(last, false);
			videoseg.getMovingRegionOrMovingRegionRefOrStillRegion().add(
					new JAXBElement<StillRegionType>(new QName(
							"urn:mpeg:mpeg7:schema:2004", "StillRegion"),
							StillRegionType.class, stillRegionType));

			MediaTimeType mediaTime = objectFactory.createMediaTimeType();
			mediaTime.setMediaTimePoint(scene.getShotWithNr(0)
					.getStartTime());
			mediaTime.setMediaDuration(scene.getDuration());
			segment.setMediaTime(mediaTime);
			segment.setId("ID_" + scene.getSceneId());

			SpatioTemporalSegmentDecompositionType spatio = videoseg;
			spatio.setGap(false);
			spatio.setOverlap(false);
			segment.getSpatialDecompositionOrTemporalDecompositionOrSpatioTemporalDecomposition()
					.add((SegmentDecompositionType) spatio);
			vstdt.getVideoSegmentOrVideoSegmentRefOrStillRegion().add(segment);

		}
		videoType
				.getVideo()
				.getSpatialDecompositionOrTemporalDecompositionOrSpatioTemporalDecomposition()
				.add(vstdt);
	}

	private StillRegionType workShot(Shot shot, boolean first) {

		if (first) {
			StillRegionType region = objectFactory.createStillRegionType();
			CreationInformationType cit = objectFactory
					.createCreationInformationType();
			CreationType ct = objectFactory.createCreationType();
			TitleType tit = objectFactory.createTitleType();
			tit.setType("Frame " + shot.getStartFrame());
			ct.getTitle().add(tit);
			cit.setCreation(ct);
			region.setCreationInformation(cit);

			region.setMediaTimePoint(shot.getStartTime());
			Keyword keyword = objectFactory
					.createKeywordAnnotationTypeKeyword();
			keyword.setValue(shot.getCut1Type().toString());
			KeywordAnnotationType key = objectFactory
					.createKeywordAnnotationType();
			key.getKeyword().add(keyword);
			TextAnnotation anno = new TextAnnotation();
			anno.getFreeTextAnnotationOrStructuredAnnotationOrDependencyStructure()
					.add(key);
			region.getTextAnnotation().add(anno);
			return region;
		} else {
			StillRegionType region = objectFactory.createStillRegionType();
			CreationInformationType cit = objectFactory
					.createCreationInformationType();
			CreationType ct = objectFactory.createCreationType();
			TitleType tit = objectFactory.createTitleType();
			tit.setType("Frame " + shot.getEndFrame());
			ct.getTitle().add(tit);
			cit.setCreation(ct);
			region.setCreationInformation(cit);
			region.setMediaTimePoint(shot.getEndTime());

			Keyword keyword = objectFactory
					.createKeywordAnnotationTypeKeyword();
			keyword.setValue(shot.getCut2Type().toString());
			KeywordAnnotationType key = objectFactory
					.createKeywordAnnotationType();
			key.getKeyword().add(keyword);
			TextAnnotation anno = new TextAnnotation();
			anno.getFreeTextAnnotationOrStructuredAnnotationOrDependencyStructure()
					.add(key);
			region.getTextAnnotation().add(anno);
			return region;
		}
		// List<StillRegionType> stillregionList = new
		// LinkedList<StillRegionType>();
		//
		// // StartFrame
		// StillRegionType region = objectFactory.createStillRegionType();
		// CreationInformationType cit = objectFactory
		// .createCreationInformationType();
		// CreationType ct = objectFactory.createCreationType();
		// TitleType tit = objectFactory.createTitleType();
		// tit.setType("Frame " + shot.getStartFrame());
		// ct.getTitle().add(tit);
		// cit.setCreation(ct);
		// region.setCreationInformation(cit);
		//
		// region.setMediaTimePoint(shot.getStartTime());
		//
		// Keyword keyword = objectFactory.createKeywordAnnotationTypeKeyword();
		// keyword.setValue(shot.getCut1Type().toString());
		// KeywordAnnotationType key =
		// objectFactory.createKeywordAnnotationType();
		// key.getKeyword().add(keyword);
		// TextAnnotation anno = new TextAnnotation();
		// anno.getFreeTextAnnotationOrStructuredAnnotationOrDependencyStructure()
		// .add(key);
		// region.getTextAnnotation().add(anno);
		// stillregionList.add(region);
		//
		// // Endframe
		// region = objectFactory.createStillRegionType();
		// cit = objectFactory.createCreationInformationType();
		// ct = objectFactory.createCreationType();
		// tit = objectFactory.createTitleType();
		// tit.setType("Frame " + shot.getEndFrame());
		// ct.getTitle().add(tit);
		// cit.setCreation(ct);
		// region.setCreationInformation(cit);
		// region.setMediaTimePoint(shot.getEndTime());
		//
		// keyword = objectFactory.createKeywordAnnotationTypeKeyword();
		// keyword.setValue(shot.getCut2Type().toString());
		// key = objectFactory.createKeywordAnnotationType();
		// key.getKeyword().add(keyword);
		// anno = new TextAnnotation();
		// anno.getFreeTextAnnotationOrStructuredAnnotationOrDependencyStructure()
		// .add(key);
		// region.getTextAnnotation().add(anno);
		// stillregionList.add(region);
		// return stillregionList;
	}
}

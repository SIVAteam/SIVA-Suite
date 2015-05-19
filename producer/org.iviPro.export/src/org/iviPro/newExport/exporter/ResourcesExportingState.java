package org.iviPro.newExport.exporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.iviPro.model.resources.Picture;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.AudioProfile;
import org.iviPro.newExport.profile.AudioVariant;
import org.iviPro.newExport.profile.ExportDirectories;
import org.iviPro.newExport.profile.ExportProfile;
import org.iviPro.newExport.profile.VideoBitRateType;
import org.iviPro.newExport.profile.VideoProfile;
import org.iviPro.newExport.profile.VideoVariant;
import org.iviPro.newExport.resources.PictureResourceDescriptor;
import org.iviPro.newExport.resources.ProjectResources;
import org.iviPro.newExport.resources.ResourceDescriptor;
import org.iviPro.newExport.resources.TimedResourceDescriptor;
import org.iviPro.newExport.resources.VideoResourceDescriptor;
import org.iviPro.newExport.util.FileUtils;
import org.iviPro.newExport.util.PathHelper;
import org.iviPro.transcoder.AudioDescriptor;
import org.iviPro.transcoder.AudioFormat;
import org.iviPro.transcoder.AudioQuality;
import org.iviPro.transcoder.FrameSettings;
import org.iviPro.transcoder.MediaSection;
import org.iviPro.transcoder.Transcoder;
import org.iviPro.transcoder.TranscodingPath;
import org.iviPro.transcoder.VideoDescriptor;
import org.iviPro.transcoder.VideoDimension;
import org.iviPro.transcoder.VideoFormat;
import org.iviPro.transcoding.exception.TranscodingException;
import org.iviPro.transcoding.ffmpeg.FfmpegTranscoder;
import org.iviPro.transcoding.format.VideoQuality;

public class ResourcesExportingState extends ExporterState {

	// @formatter:off
	private static final String LOG_EXPORT_RESOURCES = "%s Exporting the project's resources."; //$NON-NLS-1$
	private static final String LOG_EXPORT_STATIC_RESOURCES = "%s Exporting the static resources."; //$NON-NLS-1$
	private static final String LOG_EXPORT_IMAGE_RESOURCES = "%s Exporting the image resources."; //$NON-NLS-1$
	private static final String LOG_RENDER_IMAGE_RESOURCE = "%s Rendering the image '%s' to '%s'."; //$NON-NLS-1$
	private static final String LOG_EXPORT_RICH_PAGE_RESOURCES = "%s Exporting the rich page resources."; //$NON-NLS-1$
	private static final String LOG_COPY_RICH_PAGE_RESOURCE = "%s Copying the rich page '%s' to '%s'."; //$NON-NLS-1$
	private static final String LOG_COPY_PDF_RESOURCE = "%s Copying the pdf document '%s' to '%s'."; //$NON-NLS-1$
	private static final String LOG_CREATE_THUMBNAIL = "%s Creating a thumbnail for image '%s'."; //$NON-NLS-1$
	private static final String LOG_EXPORT_DYNAMIC_RESOURCES = "%s Exporting the dynamic resources."; //$NON-NLS-1$
	private static final String LOG_EXPORT_AUDIO_RESOURCES = "%s Exporting the audio resources."; //$NON-NLS-1$
	private static final String LOG_TRANSCODE_AUDIO_RESOURCE = "%s Trancoding the audio '%s' to '%s'."; //$NON-NLS-1$
	private static final String LOG_TRANSCODE_AUDIO_VARIANT = "%s Trancoding audio variant '%s'."; //$NON-NLS-1$
	private static final String LOG_TRANSCODE_AUDIO_PROFILE = "%s Using the '%s' for the following audio transcoding."; //$NON-NLS-1$
	private static final String LOG_EXPORT_VIDEO_RESOURCES = "%s Exporting the video resources."; //$NON-NLS-1$
	private static final String LOG_TRANSCODE_VIDEO_RESOURCE = "%s Trancoding the video '%s' to '%s'."; //$NON-NLS-1$
	private static final String LOG_TRANSCODE_VIDEO_VARIANT = "%s Trancoding video variant '%s'."; //$NON-NLS-1$
	private static final String LOG_TRANSCODE_VIDEO_PROFILE = "%s Using the '%s' for the following video transcoding."; //$NON-NLS-1$
	private static final String LOG_COPY_VIDEO_RESOURCE = "%s Copying video from '%s' to '%s'."; //$NON-NLS-1$
	private static final String LOG_COPY_VIDEO_FAILED = "%s FAILED copying video from '%s' to '%s'."; //$NON-NLS-1$
	
	private static final String THUMBNAIL_SUFFIX = "_thumb"; //$NON-NLS-1$
	private static final float THUMBSIZE = 150.0f;
	// @formatter:on

	private static final Logger logger = Logger
			.getLogger(ResourcesExportingState.class);

	private final ProjectResources projectResources;
	private final ExportProfile exportProfile;

	public ResourcesExportingState(ExportProfile exportProfile,
			ExportDirectories exportDirectories,
			ProjectResources projectResources) {
		super(TaskSettings.ANNOTATIONS, exportProfile, exportDirectories);
		this.projectResources = projectResources;
		this.exportProfile = exportProfile;
	}

	@Override
	public void run(Exporter exporter, IProgressMonitor monitor)
			throws ExportException {
		logger.debug(String.format(LOG_EXPORT_RESOURCES, loggerPrefix));

		try {
			monitor.beginTask(TaskSettings.ANNOTATIONS.getName(),
					TaskSettings.ANNOTATIONS.getDuration());

			if (exportProfile.isExportResources()) {
				checkCanceled(monitor);
				monitor.subTask(TaskSettings.ANNOTATIONS_STATIC.getName());
				exportStaticAnnotations(new SubProgressMonitor(monitor,
						TaskSettings.ANNOTATIONS_STATIC.getDuration(),
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

				checkCanceled(monitor);
				monitor.subTask(TaskSettings.ANNOTATIONS_MEDIA.getName());
				exportMediaAnnotations(new SubProgressMonitor(monitor,
						TaskSettings.ANNOTATIONS_MEDIA.getDuration(),
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			}
		} catch (InterruptedException cause) {
			logger.error(String.format(LOG_EXCEPTION_FORWARD, loggerPrefix,
					cause.getMessage()));
			exporter.switchState(new CleanupState(exportProfile,
					exportDirectories));
		} finally {
			monitor.done();
		}

		exporter.switchState(new PlayerExportingState(exportProfile,
				exportDirectories));
	}

	/**
	 * Exports files which do not need to be transformed.
	 * @param monitor progress monitor
	 * @throws InterruptedException
	 * @throws ExportException
	 */
	private void exportStaticAnnotations(IProgressMonitor monitor)
			throws InterruptedException, ExportException {
		logger.debug(String.format(LOG_EXPORT_STATIC_RESOURCES, loggerPrefix));

		Set<PictureResourceDescriptor> pictures = projectResources
				.getPictures();
		Set<ResourceDescriptor> richPages = projectResources.getRichPages();
		Set<ResourceDescriptor> pdfDocuments = projectResources.getPdfDocuments();

		File imageDirectory = null;
		File richPageDirectory = null;
		File pdfDirectory = null;
		try {
			imageDirectory = FileUtils.createSubdirectory(
					exportDirectories.getTmpOutputFolder(), exportProfile
							.getProfile().getGeneral().getImageDirectory());
			richPageDirectory = FileUtils.createSubdirectory(
					exportDirectories.getTmpOutputFolder(), exportProfile
							.getProfile().getGeneral().getRichPageDirectory());
			pdfDirectory = FileUtils.createSubdirectory(
					exportDirectories.getTmpOutputFolder(), exportProfile
							.getProfile().getGeneral().getPdfDirectory());
		} catch (IOException cause) {
			logger.error(String.format(LOG_EXCEPTION_FORWARD, loggerPrefix,
					cause.getMessage()));
			throw new ExportException(
					Messages.Exception_CreatingStaticAnnotationDirectoryFailed,
					cause);
		}

		try {
			checkCanceled(monitor);
			monitor.beginTask(TaskSettings.ANNOTATIONS_STATIC.getName(),
					TaskSettings.ANNOTATIONS_STATIC.getDuration());
			int tick = pictures.size() + richPages.size() + pdfDocuments.size() == 0
					? TaskSettings.ANNOTATIONS_STATIC.getDuration() 
					: TaskSettings.ANNOTATIONS_STATIC.getDuration() 
						/(pictures.size() + richPages.size() + pdfDocuments.size());

			checkCanceled(monitor);
			monitor.beginTask(TaskSettings.ANNOTATIONS_STATIC.getName(),
					TaskSettings.ANNOTATIONS_STATIC.getDuration());
			logger.debug(String
					.format(LOG_EXPORT_IMAGE_RESOURCES, loggerPrefix));

			for (PictureResourceDescriptor resourceDescriptor : pictures) {
				checkCanceled(monitor);
				monitor.subTask(resourceDescriptor.getTarget());

				File input = resourceDescriptor.getSource();
				File output = new File(imageDirectory + File.separator
						+ resourceDescriptor.getTarget());

				logger.debug(String.format(LOG_RENDER_IMAGE_RESOURCE,
						loggerPrefix, input.getAbsolutePath(),
						output.getAbsolutePath()));

				renderPicture(resourceDescriptor.getPicture(), output);
				monitor.worked(tick);
			}

			logger.debug(String.format(LOG_EXPORT_RICH_PAGE_RESOURCES,
					loggerPrefix));

			for (ResourceDescriptor resourceDescriptor : richPages) {
				checkCanceled(monitor);
				monitor.subTask(resourceDescriptor.getTarget());

				File input = resourceDescriptor.getSource();
				File output = new File(richPageDirectory + File.separator
						+ resourceDescriptor.getTarget());

				logger.debug(String.format(LOG_COPY_RICH_PAGE_RESOURCE,
						loggerPrefix, input.getAbsolutePath(),
						output.getAbsolutePath()));

				try {
					FileUtils.copy(input, output);
				} catch (IOException cause) {
					logger.error(String.format(LOG_EXCEPTION_FORWARD,
							loggerPrefix, cause.getMessage()));
					throw new ExportException(
							String.format(Messages.Exception_CopyRichPageFailed, 
									input.getAbsolutePath(), 
									output.getAbsolutePath()),
							cause);
				}
				monitor.worked(tick);
			}
			
			for (ResourceDescriptor resourceDescriptor : pdfDocuments) {
				checkCanceled(monitor);
				monitor.subTask(resourceDescriptor.getTarget());

				File input = resourceDescriptor.getSource();
				File output = new File(pdfDirectory + File.separator
						+ resourceDescriptor.getTarget());

				logger.debug(String.format(LOG_COPY_PDF_RESOURCE,
						loggerPrefix, input.getAbsolutePath(),
						output.getAbsolutePath()));

				try {
					FileUtils.copy(input, output);
				} catch (IOException cause) {
					logger.error(String.format(LOG_EXCEPTION_FORWARD,
							loggerPrefix, cause.getMessage()));
					throw new ExportException(
							String.format(Messages.Exception_CopyPdfFailed, 
									input.getAbsolutePath(), 
									output.getAbsolutePath()),
							cause);
				}
				monitor.worked(tick);
			}
		} finally {
			monitor.done();
		}
	}

	private void renderPicture(Picture fileBasedObject, File outputFile)
			throws ExportException {

//		Badly working SWT version of thumbnail creation:
		
//		ImageLoader loader = new ImageLoader();
//		ImageData[] imgDataArray = loader.load(fileBasedObject.getFile()
//				.getAbsolutePath());
//		if (imgDataArray.length == 0) {
//			throw new ExportException(String.format(
//					Messages.Exception_ReadingImageFailed, fileBasedObject
//							.getFile().getAbsolutePath()));
//		}
//
//		int imageType = imgDataArray[0].type;
//		for (int i = 0; i < imgDataArray.length; i++) {
//			Image img = new Image(Display.getDefault(), imgDataArray[i]);
//			GC gc = new GC(img);
//			gc.setAlpha(255);
//			for (ImageObject o : ((Picture) fileBasedObject).getObjects()) {
//				DrawingUtils.drawObject(gc, o, img);
//			}
//			imgDataArray[i] = img.getImageData();
//
//			logger.debug(String.format(LOG_CREATE_THUMBNAIL, loggerPrefix,
//					outputFile.getName()));
//
//			// create thumbnail
//			int imgWidth = img.getImageData().width;
//			int imgHeight = img.getImageData().height;
//
//			if (imgWidth > imgHeight) {
//				imgWidth = imgHeight;
//			} else {
//				imgHeight = imgWidth;
//			}
//			Image image = new Image(Display.getCurrent(), imgWidth, imgHeight);
//			gc.copyArea(image, 0, 0);
//			ImageLoader imageLoader = new ImageLoader();
//			imageLoader.data = new ImageData[] { image.getImageData().scaledTo(
//					150, 150) };
//			String path = outputFile.getAbsolutePath();
//			int dotIndex = path.lastIndexOf("."); //$NON-NLS-1$
//			String newPath = path.substring(0, dotIndex) + THUMBNAIL_SUFFIX
//					+ path.substring(dotIndex);
//			imageLoader.save(newPath, imageType);
//			gc.dispose();
//		}
//		loader.save(outputFile.getAbsolutePath(), imageType);
		
		BufferedImage img;
		BufferedImage thumb;
		
		try {
			// copy original file
			FileUtils.copy(fileBasedObject.getFile().getValue(),
					outputFile);
			
			//create thumbnail
			img = ImageIO.read(fileBasedObject.getFile().getValue());
			if (img == null) {
				//Used for convenience cause IOExceptions need to be caught anyway
				throw new IOException();
			}
			float xFactor = THUMBSIZE/img.getWidth();
			float yFactor = THUMBSIZE/img.getHeight();
			float usedFactor = Math.min(xFactor, yFactor);
			int thumbWidth = Math.round(usedFactor*img.getWidth());
			int thumbHeight = Math.round(usedFactor*img.getHeight());
			logger.debug(String.format(LOG_CREATE_THUMBNAIL, loggerPrefix,
					outputFile.getName()));
			java.awt.Image scaledImg = img.getScaledInstance(thumbWidth,
					thumbHeight, BufferedImage.SCALE_AREA_AVERAGING);
			thumb = new BufferedImage(thumbWidth, thumbHeight, img.getType());
			thumb.getGraphics().drawImage(scaledImg, 0, 0, null);
			String outPath = outputFile.getAbsolutePath();
			int indexOfDot = outPath.lastIndexOf('.');
			String format = outPath.substring(indexOfDot + 1);
			File outputThumb = new File(outPath.substring(0, indexOfDot) 
					+ THUMBNAIL_SUFFIX + outPath.substring(indexOfDot));
			if (!ImageIO.write(thumb, format, outputThumb)) {
				//Used for convenience cause IOExceptions need to be caught anyway
				throw new IOException();
			}
		} catch (IOException e) {
			throw new ExportException(String.format(
					Messages.Exception_ReadingImageFailed, fileBasedObject
					.getFile().getAbsolutePath()));
		}

	}

	/**
	 * Exports media files, i.e. files which might need transformation 
	 * (e.g. transcoding).
	 * @param monitor progress monitor
	 * @throws InterruptedException 
	 * @throws ExportException
	 */
	private void exportMediaAnnotations(IProgressMonitor monitor)
			throws InterruptedException, ExportException {
		logger.debug(String.format(LOG_EXPORT_DYNAMIC_RESOURCES, loggerPrefix));

		Transcoder transcoder = null;
		try {
			transcoder = new FfmpegTranscoder(
					PathHelper.FFMPEG_EXECUTABLE, false);
		} catch (TranscodingException e) {
			throw new ExportException(e.getMessage(), e);
		}

		Set<TimedResourceDescriptor> audioAnnotations = projectResources
				.getAudios();
		Set<VideoResourceDescriptor> videoAnnotations = projectResources
				.getVideos();

		int tick = computeMediaAnnotationTick(audioAnnotations.size(),
				videoAnnotations.size());
		try {
			checkCanceled(monitor);
			monitor.beginTask(TaskSettings.ANNOTATIONS_MEDIA.getName(),
					TaskSettings.ANNOTATIONS_MEDIA.getDuration());
			exportAudioAnnotations(transcoder, audioAnnotations, monitor, tick);
			exportVideoAnnotations(transcoder, videoAnnotations, monitor, tick);
		} finally {
			monitor.done();
		}
	}

	private void exportAudioAnnotations(Transcoder transcoder,
			Set<TimedResourceDescriptor> audioAnnotations,
			IProgressMonitor monitor, int tick) throws InterruptedException,
			ExportException {
		logger.debug(String.format(LOG_EXPORT_AUDIO_RESOURCES, loggerPrefix));

		for (AudioVariant av : exportProfile.getProfile().getAudio()
				.getAudioVariants()) {
			logger.debug(String.format(LOG_TRANSCODE_AUDIO_VARIANT,
					loggerPrefix, av.getTitle()));

			File variantOutputFolder = null;
			if (exportProfile.getProfile().getAudio().getAudioVariants().size() > 1) {
				variantOutputFolder = new File(exportDirectories
						.getTmpOutputFolder().getAbsolutePath()
						+ File.separator
						+ exportProfile.getProfile().getGeneral()
								.getAudioDirectory()
						+ File.separator
						+ av.getTitle() + File.separator);
			} else {
				variantOutputFolder = new File(exportDirectories
						.getTmpOutputFolder().getAbsolutePath()
						+ File.separator
						+ exportProfile.getProfile().getGeneral()
								.getAudioDirectory() + File.separator);
			}
			variantOutputFolder.mkdirs();
			for (AudioProfile ap : av.getAudioProfiles()) {
				logger.debug(String.format(LOG_TRANSCODE_AUDIO_PROFILE,
						loggerPrefix, ap.toString()));
				for (TimedResourceDescriptor trd : audioAnnotations) {
					checkCanceled(monitor);
					monitor.subTask(String.format("%s (%s/%s)", //$NON-NLS-1$
							trd.getTarget(), av.getTitle(), ap
									.getAudioContainer().toString()));

					Sync sync = new Sync();
					AudioDescriptor audioDescriptor;
					try {
						audioDescriptor = new AudioDescriptor(new AudioFormat(
								ap.getAudioContainer(), ap.getAudioCodec()),
								new AudioQuality(ap.getBitRate(), ap
										.getSampleRate(), ap.getSampleSize(),
										ap.getChannels()), new TranscodingPath(
										trd.getSource(), trd.getTarget(),
										variantOutputFolder), true,
								getMediaSection(trd));
					} catch (TranscodingException cause) {
						logger.error(String.format(LOG_EXCEPTION_FORWARD,
								loggerPrefix, cause.getMessage()));
						throw new ExportException(
								Messages.Exception_InvalidAudioParameters,
								cause);
					}
					logger.debug(String.format(LOG_TRANSCODE_AUDIO_RESOURCE,
							loggerPrefix, audioDescriptor.getInputFile()
									.getAbsolutePath(), audioDescriptor
									.getOutputFile().getAbsolutePath()));

					transcode(new AudioExecutor(transcoder, audioDescriptor,
							sync), monitor, sync);
					monitor.worked(tick);
				}
			}
		}
	}

	private void transcode(Runnable runnable, IProgressMonitor monitor,
			final Sync sync) throws ExportException, InterruptedException {
		Thread worker = new Thread(runnable);
		worker.start();
		while (!sync.transcoded && !sync.failed) {
			Thread.sleep(1000);
			if (monitor.isCanceled()) {
				worker.interrupt();
				throw new InterruptedException(
						Messages.Exception_TranscodingCanceledByUser);
			}
		}
		if (sync.failed) {
			throw new ExportException(Messages.Exception_TranscodingFailed,
					sync.cause);
		}
	}

	private class AudioExecutor implements Runnable {

		private final Transcoder transcoder;
		private final AudioDescriptor audioDescriptor;
		private final Sync sync;

		private AudioExecutor(Transcoder transcoder,
				AudioDescriptor audioDescriptor, Sync sync) {
			this.transcoder = transcoder;
			this.audioDescriptor = audioDescriptor;
			this.sync = sync;
		}

		@Override
		public void run() {
			try {
				transcoder.transcodeAudio(audioDescriptor);
				sync.transcoded = true;
			} catch (TranscodingException cause) {
				logger.error(String.format(LOG_EXCEPTION_FORWARD, loggerPrefix,
						cause.getMessage()));
				sync.failed = true;
				sync.cause = cause;
			}
		}

	}

	private class VideoExecutor implements Runnable {

		private final Transcoder transcoder;
		private final VideoDescriptor videoDescriptor;
		private final Sync sync;

		private VideoExecutor(Transcoder transcoder,
				VideoDescriptor videoDescriptor, Sync sync) {
			this.transcoder = transcoder;
			this.videoDescriptor = videoDescriptor;
			this.sync = sync;
		}

		@Override
		public void run() {
			try {
				transcoder.transcodeVideo(videoDescriptor);
				sync.transcoded = true;
			} catch (TranscodingException cause) {
				logger.error(String.format(LOG_EXCEPTION_FORWARD, loggerPrefix,
						cause.getMessage()));
				sync.failed = true;
				sync.cause = cause;
			}
		}

	}

	private class Sync {
		boolean transcoded;
		boolean failed;
		TranscodingException cause;
	}

	private void exportVideoAnnotations(Transcoder transcoder,
			Set<VideoResourceDescriptor> videoAnnotations,
			IProgressMonitor monitor, int tick) throws InterruptedException,
			ExportException {
		logger.debug(String.format(LOG_EXPORT_VIDEO_RESOURCES, loggerPrefix));

		// Check if converting is required, just copy files if not
		if(exportProfile.isConvert()){
			for (VideoVariant vv : exportProfile.getProfile().getVideo()
					.getVideoVariants()) {
				logger.debug(String.format(LOG_TRANSCODE_VIDEO_VARIANT,
						loggerPrefix, vv.getTitle()));
	
				File variantOutputFolder = null;
				if (exportProfile.getProfile().getVideo().getVideoVariants().size() > 1) {
					variantOutputFolder = new File(exportDirectories
							.getTmpOutputFolder().getAbsolutePath()
							+ File.separator
							+ exportProfile.getProfile().getGeneral()
									.getVideoDirectory()
							+ File.separator
							+ vv.getTitle() + File.separator);
				} else {
					variantOutputFolder = new File(exportDirectories
							.getTmpOutputFolder().getAbsolutePath()
							+ File.separator
							+ exportProfile.getProfile().getGeneral()
									.getVideoDirectory() + File.separator);
				}
				variantOutputFolder.mkdirs();
	
				for (VideoProfile vp : vv.getVideoProfiles()) {
					logger.debug(String.format(LOG_TRANSCODE_VIDEO_PROFILE,
							loggerPrefix, vp.toString()));
	
					VideoDimension videoDimension = null;
					videoDimension = new VideoDimension(
							exportProfile.getProjectDimension().getWidth(),
							exportProfile.getProjectDimension().getHeight());
							
					int videoBitrate = -1;
					if (vp.getVideoBitRateType() == VideoBitRateType.FIXED) {
						videoBitrate = vp.getVideoBitRate();
					}
	
					for (VideoResourceDescriptor vrd : videoAnnotations) {
						checkCanceled(monitor);
						monitor.subTask(String.format("%s (%s/%s)", //$NON-NLS-1$
								vrd.getTarget(), vv.getTitle(), vp
										.getVideoContainer().toString()));
	
						Sync sync = new Sync();
						VideoDescriptor videoDescriptor;
						try {
							videoDescriptor = new VideoDescriptor(vrd.getVideo().getDimension(), new VideoFormat(
									vp.getVideoContainer(), vp.getVideoCodec(), 
									vp.getVideoCodecQuality(),
									vp.getAudioCodec()), new VideoQuality(
									videoBitrate, new FrameSettings(videoDimension,
											vp.getFrameRate())), new AudioQuality(
									vp.getAudioBitRate(), vp.getSampleRate(),
									vp.getSampleSize(), vp.getChannels()),
									new TranscodingPath(vrd.getSource(), vrd
											.getTarget(), variantOutputFolder),
									true, getMediaSection(vrd));
						} catch (TranscodingException cause) {
							logger.error(String.format(LOG_EXCEPTION_FORWARD,
									loggerPrefix, cause.getMessage()));
							throw new ExportException(
									Messages.Exception_InvaliedVideoParameters,
									cause);
						}
						logger.debug(String.format(LOG_TRANSCODE_VIDEO_RESOURCE,
								loggerPrefix, videoDescriptor.getInputFile()
										.getAbsolutePath(), videoDescriptor
										.getOutputFile().getAbsolutePath()));
	
						transcode(new VideoExecutor(transcoder, videoDescriptor,
								sync), monitor, sync);
						monitor.worked(tick);
					}
				}
			}
		} else{
			File variantOutputFolder = new File(exportDirectories
					.getTmpOutputFolder().getAbsolutePath()
					+ File.separator
					+ exportProfile.getProfile().getGeneral()
							.getVideoDirectory() + File.separator);
			variantOutputFolder.mkdir();
			for (VideoResourceDescriptor vrd : videoAnnotations) {
				checkCanceled(monitor);
				monitor.subTask(vrd.getTarget());

				
				TranscodingPath path = new TranscodingPath(vrd.getSource(), vrd
						.getTarget(), variantOutputFolder);
				String target = path.getOutputFolder().getAbsolutePath()
						+ File.separator + path.getOutputFileName()  + "." //$NON-NLS-1$
						+ FilenameUtils.getExtension(path.getInputFile().getAbsolutePath());
				logger.debug(String.format(LOG_COPY_VIDEO_RESOURCE,
						loggerPrefix, path.getInputFile()
								.getAbsolutePath(), target));
				try {
					FileUtils.copy(path.getInputFile(), new File(target));
				} catch (IOException e) {
					e.printStackTrace();
					logger.debug(String.format(LOG_COPY_VIDEO_FAILED,
							loggerPrefix, path.getInputFile()
									.getAbsolutePath(), target));
				}
				
				monitor.worked(tick);
			}
		}
	}

	private int computeMediaAnnotationTick(int audioAnnotations,
			int videoAnnotations) {
		int audioVariants = 0;
		for (AudioVariant av : exportProfile.getProfile().getAudio()
				.getAudioVariants()) {
			audioVariants += av.getAudioProfiles().size();
		}

		int videoVariants = 0;
		for (VideoVariant vv : exportProfile.getProfile().getVideo()
				.getVideoVariants()) {
			videoVariants += vv.getVideoProfiles().size();
		}

		int mediaCount = audioAnnotations * audioVariants + videoAnnotations
				* videoVariants;

		return mediaCount <= 0 ? TaskSettings.ANNOTATIONS_MEDIA.getDuration()
				: TaskSettings.ANNOTATIONS_MEDIA.getDuration() / mediaCount;
	}

	private MediaSection getMediaSection(
			TimedResourceDescriptor resourceDescriptor) {
		return new MediaSection(resourceDescriptor.getStartTime() / 1000000L,
				(resourceDescriptor.getEndTime() - resourceDescriptor
						.getStartTime()) / 1000000L);
	}
}

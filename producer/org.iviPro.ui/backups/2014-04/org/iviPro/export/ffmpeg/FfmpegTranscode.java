package org.iviPro.export.ffmpeg;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.iviPro.application.Application;
import org.iviPro.export.ExportException;
import org.iviPro.export.ExportType;
import org.iviPro.model.ProjectSettings;
import org.iviPro.utils.PathHelper;

/**
 * Diese Klasse spricht das Programm FFMPEG Export an.
 * Mit diesem ist es möglich jede beliebige Sequenz
 * unter Angabe der Startposition und der Dauer der Sequenz ein beliebiges Video
 * in ein Flashmovie umzuwandeln.
 * 
 * @author dellwo
 */
public class FfmpegTranscode {

	private static Logger logger = Logger.getLogger(FfmpegTranscode.class);

	/**
	 * Default audio bitrate in bits/s
	 */
	private static final int DEFAULT_AUDIO_BITRATE = 32 * 1024;

	/**
	 * Default audio sampling frequency in Hz
	 */
	private static final int DEFAULT_AUDIO_SAMPLE_FREQUENCY = 22050;

	/**
	 * Default video bitrate in bit/s
	 */
	private static final int DEFAULT_VIDEO_BITRATE = 2000 * 1024;

	/**
	 * Default video framerate in Hz
	 */
	private static final int DEFAULT_VIDEO_FRAMERATE = 25;

	/**
	 * Default video size in pixel (width x height)
	 */
	private static final String DEFAULT_VIDEO_SIZE = "640x360"; //$NON-NLS-1$

	/**
	 * Default video aspect ratio
	 */
//	private static final String DEFAULT_VIDEO_ASPECTRATIO = "16:9"; //$NON-NLS-1$

	/**
	 * Parameter which will be replaced with the path to the ffmpeg executable
	 */
	private static final String PARAM_FFMPEGEXE = "{0}"; //$NON-NLS-1$

	/**
	 * Parameter which will be replaced with the path to the input file
	 */
	private static final String PARAM_INPUTFILE = "{1}"; //$NON-NLS-1$

	/**
	 * Parameter which will be replaced with the start timestamp of the
	 * transcodede video sequence (in seconds e.g 54.202)
	 */
	private static final String PARAM_STARTTIME = "{2}"; //$NON-NLS-1$

	/**
	 * Parameter which will be replaced with the duration of the transcoded
	 * video sequence (in seconds e.g 14.8)
	 */
	private static final String PARAM_DURATION = "{3}"; //$NON-NLS-1$

	/**
	 * Parameter which will be replaced with the path to the output file
	 */
	private static final String PARAM_OUTPUTFILE = "{4}"; //$NON-NLS-1$

	/**
	 * Parameter which will be replaced with the video codec
	 */
	private static final String PARAM_VIDEOCODEC = "{5}"; //$NON-NLS-1$
	


	/**
	 * Parameter which will be replaced with the audio codec
	 */
	private static final String PARAM_AUDIOCODEC = "{6}"; //$NON-NLS-1$

	/**
	 * Parameter which will be replaced with the container format
	 */
	private static final String PARAM_CONTAINERFORMAT = "{7}"; //$NON-NLS-1$
	
	private static final String PARAM_VIDEOBITRATE = "{8}"; //$NON-NLS-1$
	private static final String PARAM_VIDEOFRAMERATE = "{9}"; //$NON-NLS-1$
	private static final String PARAM_VIDEOSIZE = "{10}"; //$NON-NLS-1$
//	private static final String PARAM_VIDEOASPECTRATIO = "{11}"; //$NON-NLS-1$
	private static final String PARAM_AUDIO_SAMPLE_FREQUENCY = "{12}"; //$NON-NLS-1$
	private static final String PARAM_AUDIO_BITRATE = "{13}"; //$NON-NLS-1$
	
	private static final String X264_OPTION = "{14}"; //$NON-NLS-1$

	/**
	 * FFMPEG command to be executed for video transcoding. The parameters in it
	 * will be replaced later.
	 */
	private static final String FFMPEG_CMD_VIDEO = "" // //$NON-NLS-1$
			+ "\"" + PARAM_FFMPEGEXE + "\"" //  //$NON-NLS-1$ //$NON-NLS-2$
			+ " -i \"" + PARAM_INPUTFILE + "\"" // //$NON-NLS-1$ //$NON-NLS-2$
			+ " -f " + PARAM_CONTAINERFORMAT //  //$NON-NLS-1$
			+ " -ss " + PARAM_STARTTIME //  //$NON-NLS-1$
			+ " -t " + PARAM_DURATION // //$NON-NLS-1$
			+ " -vcodec " + PARAM_VIDEOCODEC // //$NON-NLS-1$
			+ X264_OPTION
			+ " -s " + PARAM_VIDEOSIZE // //$NON-NLS-1$
			+ " -b " + PARAM_VIDEOBITRATE // //$NON-NLS-1$
			+ " -r " + PARAM_VIDEOFRAMERATE // //$NON-NLS-1$
//			+ " -aspect " + PARAM_VIDEOASPECTRATIO // //$NON-NLS-1$
			+ " -acodec " + PARAM_AUDIOCODEC // //$NON-NLS-1$
			+ " -ar " + PARAM_AUDIO_SAMPLE_FREQUENCY // //$NON-NLS-1$
			+ " -ab " + PARAM_AUDIO_BITRATE // //$NON-NLS-1$
			+ " \"" + PARAM_OUTPUTFILE + "\""; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Windows-Version of the FFMPEG-Command for video transcoding.
	 */
	private static final String FFMPEG_CMD_VIDEO_WIN = FFMPEG_CMD_VIDEO;

	/**
	 * FFMPEG command to be executed for audio transcoding. The parameters in it
	 * will be replaced later.
	 */
	private static final String FFMPEG_CMD_AUDIO = "" // //$NON-NLS-1$
			+ "\"" + PARAM_FFMPEGEXE + "\"" //  //$NON-NLS-1$ //$NON-NLS-2$
			+ " -i \"" + PARAM_INPUTFILE + "\"" // //$NON-NLS-1$ //$NON-NLS-2$
			+ " -f " + PARAM_CONTAINERFORMAT //  //$NON-NLS-1$
			+ " -ss " + PARAM_STARTTIME //  //$NON-NLS-1$
			+ " -t " + PARAM_DURATION // //$NON-NLS-1$
			//			+ " -vcodec " + PARAM_VIDEOCODEC // //$NON-NLS-1$
			//			+ " -s " + DEFAULT_VIDEO_SIZE // //$NON-NLS-1$
			//			+ " -b " + DEFAULT_VIDEO_BITRATE // //$NON-NLS-1$
			//			+ " -r " + DEFAULT_VIDEO_FRAMERATE // //$NON-NLS-1$
			//			+ " -aspect " + DEFAULT_VIDEO_ASPECTRATIO // //$NON-NLS-1$
			+ " -acodec " + PARAM_AUDIOCODEC // //$NON-NLS-1$
			+ " -ar " + DEFAULT_AUDIO_SAMPLE_FREQUENCY // //$NON-NLS-1$
			+ " -ab " + DEFAULT_AUDIO_BITRATE // //$NON-NLS-1$
			+ " \"" + PARAM_OUTPUTFILE + "\""; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Windows-Version of the FFMPEG-Command for audio transcofing
	 */
	private static final String FFMPEG_CMD_AUDIO_WIN = FFMPEG_CMD_AUDIO;

	// "cmd.exe /C \"" + FFMPEG_CMD + "\"";

	/**
	 * Wandelt ein Video in ein Flashvideo um. Dabei sind Startzeit und Länger
	 * der Sequenz, sowie die Größe (Breite x Höhe) anzugeben.
	 * 
	 * @param inputFile
	 *            umzuwandelndes Video
	 * @param outputFile
	 *            transkodiertes Vieo
	 * @param startTime
	 *            Startzeit (Sekunden, bsp. 10.2)
	 * @param endTime
	 *            Endzeit
	 * @param size
	 *            Breite x Höhe (Bsp. 320x280)
	 * @throws Falls
	 *             ein Fehler beim Export auftritt wird eine Export-Exception
	 *             geworfen.
	 */
	public static void transcodeVideo(File inputFile, File outputFile,
			ExportType type, long startTime, long endTime)
			throws ExportException {
		try {
			// Betriebssystem abfragen, um spezifische Commands abzufragen
			String osName = System.getProperty("os.name"); //$NON-NLS-1$
			String cmd = null;

			String ffmpeg = PathHelper.getPathToFFMpegExportExe().getAbsolutePath();
			String inputFilename = inputFile.getAbsolutePath();
			String outputFilename = outputFile.getAbsolutePath();			
			String startStr = getTimeStr(startTime);
			String durationStr = getTimeStr(endTime - startTime);
			
			// Custom Settings are fetched in the ExportType
			String videoCodec = type.getFFmpegVideoCodec();
			String audioCodec = type.getFFmpegAudioCodec();
			String containerFormat = type.getFFmpegVideoContainerFormat();
			String x264 = videoCodec.equals("libx264") ? " -level 12 -coder 0 " + //$NON-NLS-1$ //$NON-NLS-2$
					"-flags +loop -cmp +chroma " + //$NON-NLS-1$
					"-partitions +parti8x8+parti4x4+partp8x8+partb8x8 " + //$NON-NLS-1$
					"-me_method hex -subq 7 -me_range 16 -g 250 " + //$NON-NLS-1$
					"-keyint_min 25 -sc_threshold 40 -i_qfactor 0.71 " + //$NON-NLS-1$
					"-b_strategy 1 -qcomp 0.6 -qmin 10 -qmax 51 " + //$NON-NLS-1$
					"-qdiff 4 -bf 2 -refs 1 -directpred 1 -trellis 0 " + //$NON-NLS-1$
					"-flags2 +bpyramid+wpred+dct8x8+fastpskip" : ""; //$NON-NLS-1$ //$NON-NLS-2$

			// TODO auch für andere Systeme anpassen
			if (osName.startsWith("Windows")) { //$NON-NLS-1$
				cmd = FFMPEG_CMD_VIDEO_WIN;
			} else {
				String errorMsg = "Export is currently only working under Windows."; //$NON-NLS-1$
				logger.error(errorMsg);
				throw new ExportException(errorMsg);
			}
			// Replace parameter in ffmpeg-command
			cmd = cmd.replace(PARAM_FFMPEGEXE, ffmpeg);
			cmd = cmd.replace(PARAM_INPUTFILE, inputFilename);
			cmd = cmd.replace(PARAM_OUTPUTFILE, outputFilename);
			cmd = cmd.replace(PARAM_VIDEOCODEC, videoCodec);
			cmd = cmd.replace(X264_OPTION, x264);
			cmd = cmd.replace(PARAM_CONTAINERFORMAT, containerFormat);
			cmd = cmd.replace(PARAM_AUDIOCODEC, audioCodec);
			
			if (startTime == -1) {
				cmd = cmd.replace(" -ss " + PARAM_STARTTIME, "");
			} else {
				cmd = cmd.replace(PARAM_STARTTIME, startStr);
			}
			if (endTime == -1) {
				cmd = cmd.replace(" -t " + PARAM_DURATION, "");
			} else {
				cmd = cmd.replace(PARAM_DURATION, durationStr);
			}
			
			String settingsType = null;
			HashMap<String, String> projectExport = null;
			if (type.equals(ExportType.CUSTOM)) {
				settingsType = ProjectSettings.PROJECT;
				projectExport = Application.getCurrentProject().getSettings().getExportSettings(settingsType);
			} 
			if (settingsType != null && projectExport != null) {
				ProjectSettings settings = Application.getCurrentProject().getSettings();
				cmd = cmd.replace(PARAM_VIDEOBITRATE, projectExport.get(settings.VIDEOBITRATE));
				cmd = cmd.replace(PARAM_VIDEOFRAMERATE, projectExport.get(settings.VIDEOFRAMERATE));
				cmd = cmd.replace(PARAM_VIDEOSIZE, projectExport.get(settings.VIDEOSIZE));
//				cmd = cmd.replace(PARAM_VIDEOASPECTRATIO, projectExport.get(settings.ASPECTRATIO));
				cmd = cmd.replace(PARAM_AUDIO_SAMPLE_FREQUENCY, projectExport.get(settings.AUDIOSAMPLEFREQUENCY));
				cmd = cmd.replace(PARAM_AUDIO_BITRATE, projectExport.get(settings.AUDIOBITRATE));
			} else {
				cmd = cmd.replace(PARAM_VIDEOSIZE, DEFAULT_VIDEO_SIZE);
				cmd = cmd.replace(PARAM_VIDEOBITRATE, String.valueOf(DEFAULT_VIDEO_BITRATE));
				cmd = cmd.replace(PARAM_VIDEOFRAMERATE, String.valueOf(DEFAULT_VIDEO_FRAMERATE));
//				cmd = cmd.replace(PARAM_VIDEOASPECTRATIO, DEFAULT_VIDEO_ASPECTRATIO);
				cmd = cmd.replace(PARAM_AUDIO_SAMPLE_FREQUENCY, String.valueOf(DEFAULT_AUDIO_SAMPLE_FREQUENCY));
				cmd = cmd.replace(PARAM_AUDIO_BITRATE, String.valueOf(DEFAULT_AUDIO_BITRATE));
			}

			// holen und Ausführen der Runtime
			logger.info("Transcoding: " + cmd); //$NON-NLS-1$
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec(cmd);

			// Abfragen der Streams
			StreamHandler errorReader = new StreamHandler(p.getErrorStream(),
					"stderr"); //$NON-NLS-1$
			StreamHandler outputReader = new StreamHandler(p.getInputStream(),
					"stdout"); //$NON-NLS-1$
			errorReader.start();
			outputReader.start();

			// warten auf Beendigung
			int exitVal = p.waitFor();

			// Falls ein Fehler auftrat liefert ffmpeg einen exit-Wert != 0
			if (exitVal != 0) {
				String errorMsg = "FFmpeg failed with error code: " + exitVal; //$NON-NLS-1$
				logger.error(errorMsg);
				throw new ExportException(errorMsg);
			}

		} catch (Throwable t) {
			if (t instanceof ExportException) {
				throw (ExportException) t;
			} else {
				logger.error(t.getMessage(), t);
				throw new ExportException(t.getMessage(), new Exception(t));
			}
		}
	}

	public static void transcodeAudio(File inputFile, File outputFile,
			ExportType type, long startTime, long endTime) throws ExportException {
		try {
			// Betriebssystem abfragen, um spezifische Commands abzufragen
			String osName = System.getProperty("os.name"); //$NON-NLS-1$
			String cmd = null;

			String ffmpeg = PathHelper.getPathToFFMpegExportExe().getAbsolutePath();
			String inputFilename = inputFile.getAbsolutePath();
			String outputFilename = outputFile.getAbsolutePath();
			String audioCodec = type.getFFmpegAudioCodec();
			String containerFormat = type.getFFmpegAudioContainerFormat();
			String startStr = getTimeStr(startTime);
			String durationStr = getTimeStr(endTime - startTime);

			// TODO auch für andere Systeme anpassen
			if (osName.startsWith("Windows")) { //$NON-NLS-1$
				cmd = FFMPEG_CMD_AUDIO_WIN;
			} else {
				String errorMsg = "Export is currently only working under Windows."; //$NON-NLS-1$
				logger.error(errorMsg);
				throw new ExportException(errorMsg);
			}
			// Replace parameter in ffmpeg-command
			cmd = cmd.replace(PARAM_FFMPEGEXE, ffmpeg);
			cmd = cmd.replace(PARAM_INPUTFILE, inputFilename);
			cmd = cmd.replace(PARAM_OUTPUTFILE, outputFilename);
			cmd = cmd.replace(PARAM_AUDIOCODEC, audioCodec);
			cmd = cmd.replace(PARAM_CONTAINERFORMAT, containerFormat);
			
			// falls keine Start und Endzeit gesetzt ist entferne die Zeitoptionen
			if (startTime == -1) {
				cmd = cmd.replace(" -ss " + PARAM_STARTTIME, "");
			} else {
				cmd = cmd.replace(PARAM_STARTTIME, startStr);
			}
			if (endTime == -1) {
				cmd = cmd.replace(" -t " + PARAM_DURATION, "");
			} else {
				cmd = cmd.replace(PARAM_DURATION, durationStr);
			}

			// holen und Ausführen der Runtime
			logger.info("Transcoding: " + cmd); //$NON-NLS-1$
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec(cmd);

			// Abfragen der Streams
			StreamHandler errorReader = new StreamHandler(p.getErrorStream(),
					"stderr"); //$NON-NLS-1$
			StreamHandler outputReader = new StreamHandler(p.getInputStream(),
					"stdout"); //$NON-NLS-1$
			errorReader.start();
			outputReader.start();

			// warten auf Beendigung
			int exitVal = p.waitFor();

			// Falls ein Fehler auftrat liefert ffmpeg einen exit-Wert != 0
			if (exitVal != 0) {
				String errorMsg = "FFmpeg failed with error code: " + exitVal; //$NON-NLS-1$
				logger.error(errorMsg);
				throw new ExportException(errorMsg);
			}

		} catch (Throwable t) {
			if (t instanceof ExportException) {
				throw (ExportException) t;
			} else {
				logger.error(t.getMessage(), t);
				throw new ExportException(t.getMessage(), new Exception(t));
			}
		}

	}

	/**
	 * Konvertiert einen Zeitstempel in einen String den FFMPEG als Paramter
	 * versteht.
	 * 
	 * @param time
	 *            Time in nanoseconds
	 * @return
	 */
	private static String getTimeStr(long time) {
		float timeInSeconds = time / 1000000000.0f;
		return Float.toString(timeInSeconds);
	}

}

package org.iviPro.transcoding.ffmpeg;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.iviPro.transcoder.AudioDescriptor;
import org.iviPro.transcoder.AudioQuality;
import org.iviPro.transcoder.MediaSection;
import org.iviPro.transcoder.Transcoder;
import org.iviPro.transcoder.VideoDescriptor;
import org.iviPro.transcoding.exception.TranscodingException;
import org.iviPro.transcoding.exception.TranscodingReason;
import org.iviPro.transcoding.exception.TranscodingReasonDescriptor;
import org.iviPro.transcoding.format.AudioBitRate;
import org.iviPro.transcoding.format.AudioCodec;
import org.iviPro.transcoding.format.Channels;
import org.iviPro.transcoding.format.FrameRate;
import org.iviPro.transcoding.format.SampleRate;
import org.iviPro.transcoding.format.VideoCodec;
import org.iviPro.transcoding.format.VideoCodecQuality;
import org.iviPro.transcoding.format.VideoQuality;
import org.iviPro.transcoding.process.ProcessExecutor;
import org.iviPro.transcoding.util.Constant;

public class FfmpegTranscoder implements Transcoder {

	private static final String[] SUPPORTED_OPERATING_SYSTEMS = new String[] { "Windows" };
	private static final File DIRECTORY_TMP = new File(
			System.getProperty("java.io.tmpdir") + File.separator
					+ "iviProTranscoder");

	private final File ffmpegExecutable;
	private final boolean debug;

	public FfmpegTranscoder(File ffmpegExecutable, boolean debug)
			throws TranscodingException {
		if (!checkTempDirectory()) {
			throw new TranscodingException(new TranscodingReason(
					TranscodingReasonDescriptor.TMP_NOT_WRITEABLE,
					new String[] { DIRECTORY_TMP.toString() }));
		}

		String operatingSystem = System.getProperty("os.name", "");
		if (!checkOperatingSystem(operatingSystem)) {
			throw new TranscodingException(new TranscodingReason(
					TranscodingReasonDescriptor.OS_NOT_SUPPORTED,
					new String[] { operatingSystem }));
		}

		this.ffmpegExecutable = ffmpegExecutable;

		if (!ffmpegExecutable.canExecute()) {
			throw new TranscodingException(new TranscodingReason(
					TranscodingReasonDescriptor.TRANSCODING_EXE_NOT_EXECUTABLE,
					new String[] { ffmpegExecutable.toString() }));
		}

		this.debug = debug;
	}

	private boolean checkTempDirectory() {
		return DIRECTORY_TMP.exists() || DIRECTORY_TMP.mkdir();
	}

	private boolean checkOperatingSystem(String operatingSystem) {
		for (String os : SUPPORTED_OPERATING_SYSTEMS) {
			if (operatingSystem.startsWith(os))
				return true;
		}
		return false;
	}

	@Override
	public void transcodeAudio(AudioDescriptor descriptor)
			throws TranscodingException, InterruptedException {
		descriptor.validateFiles();
		ProcessExecutor processExecutor = new ProcessExecutor(
				buildAudioCommand(descriptor));
		transcode(processExecutor, descriptor.getInputFile(),
				descriptor.getOutputFile());
	}

	private List<String> buildAudioCommand(AudioDescriptor descriptor) {
		List<String> command = new ArrayList<String>();
		// Executable
		command.add(ffmpegExecutable.toString());
		// Overwrite output files without asking
		command.add("-y");
		// Input file
		command.add("-i");
		command.add(descriptor.getInputFile().toString());
		// Container
		command.add("-f");
		command.add(descriptor.getAudioFormat().getAudioContainer()
				.getTranscoderParameter());
		// AudioSettings
		appendAudioSettings(command, descriptor.getAudioFormat()
				.getAudioCodec(), descriptor.getAudioQuality());
		// Section settings
		appendSectionSettings(command, descriptor.getMediaSection());
		// Output file
		command.add(descriptor.getOutputFile().toString());
		return command;
	}

	private void appendSectionSettings(List<String> command,
			MediaSection section) {
		// Start time
		if (section.getStartTime() >= 0) {
			command.add("-ss");
			command.add(formatMillisecondString(section.getStartTime()));
		}
		// Duration
		if (section.getDuration() >= 0) {
			command.add("-t");
			command.add(formatMillisecondString(section.getDuration()));
		}
	}

	private String formatMillisecondString(long milliseconds) {
		long seconds = milliseconds / 1000L;
		milliseconds -= seconds * 1000L;
		return String.valueOf(seconds) + "." + String.valueOf(milliseconds);
	}

	private void appendAudioSettings(List<String> command,
			AudioCodec audioCodec, AudioQuality audioQuality) {
		// Codec
		command.add("-acodec");
		command.add(audioCodec.getTranscoderParameter());
		// Bit rate
		if (audioQuality.getBitRate() != AudioBitRate.ORIGINAL) {
			command.add("-ab");
			command.add(String.valueOf(audioQuality.getBitRate().getBitRate())
					+ "k");
		}
		// Sample rate
		if (audioQuality.getSampleRate() != SampleRate.ORIGINAL) {
			command.add("-ar");
			command.add(String.valueOf(audioQuality.getSampleRate()
					.getSampleRate()));
		}
		// Sample size
		// if (audioQuality.getSampleSize() != SampleSize.ORIGINAL) {
		// audioCommand.add("-aq");
		// audioCommand.add(String.valueOf(audioProfile.getAudioSampleSize()
		// .getSampleSize()));
		// }
		// Channels
		if (audioQuality.getChannels() != Channels.ORIGINAL) {
			command.add("-ac");
			command.add(String
					.valueOf(audioQuality.getChannels().getChannels()));
		}
	}

	@Override
	public void transcodeVideo(VideoDescriptor descriptor)
			throws TranscodingException, InterruptedException {
		descriptor.validateFiles();
		ProcessExecutor processExecutor = new ProcessExecutor(
				buildVideoCommand(descriptor));
		transcode(processExecutor, descriptor.getInputFile(),
				descriptor.getOutputFile());
	}

	private List<String> buildVideoCommand(VideoDescriptor descriptor) {
		List<String> command = new ArrayList<String>();
		// Executable
		command.add(ffmpegExecutable.toString());
		// Overwrite output files without asking
		command.add("-y");
		// Input file
		command.add("-i");
		command.add(descriptor.getInputFile().toString());
		// Container
		command.add("-f");
		command.add(descriptor.getVideoFormat().getVideoContainer()
				.getTranscoderParameter());
		// Audio settings
		appendAudioSettings(command, descriptor.getVideoFormat()
				.getAudioCodec(), descriptor.getAudioQuality());
		// Video settings
		appendVideoSettings(command, descriptor.getOriginalResolution(),
				descriptor.getVideoFormat().getVideoCodec(), 
				descriptor.getVideoFormat().getVideoCodecQuality(), 
				descriptor.getVideoQuality());
		// Section settings
		appendSectionSettings(command, descriptor.getMediaSection());
		// Output file
		command.add(descriptor.getOutputFile().toString());
		return command;
	}

	private void appendVideoSettings(List<String> command, Dimension orgResolution,
			VideoCodec codec, VideoCodecQuality codecQuality, VideoQuality videoQuality) {
		// Codec
		command.add("-vcodec");
		command.add(codec.getTranscoderParameter());
		
		// Use quality profile if available
		if (codecQuality != null) {
			command.add("-profile:v");
			command.add(codecQuality.getTranscoderParameter());
		}
		
		// Bit rate
		if (videoQuality.getBitRate() > 0) {
			command.add("-vb");
			command.add(String.valueOf(videoQuality.getBitRate()) + "k");
		}
		
		// Scale video and use padding if desired aspect ratio differs from original
		int desiredWidth = videoQuality.getFrameSettings().getVideoDimension().getWidth();
		int desiredHeight = videoQuality.getFrameSettings().getVideoDimension().getHeight();
		int effWidth = desiredWidth;
		int effHeight = desiredHeight;
		int xPad = 0;
		int yPad = 0;
		if (desiredWidth > 0 && desiredHeight > 0) {
			float desiredRatio = (float)desiredWidth / desiredHeight;
			float orgRatio = (float)orgResolution.width / orgResolution.height;
			
			if (desiredRatio > orgRatio) {
				effWidth = (int)(desiredHeight * orgRatio);
				xPad = (int)(desiredWidth - effWidth) / 2;
			} else {
				effHeight = (int)(desiredWidth / orgRatio);
				yPad = (int)(desiredHeight - effHeight) / 2;
			}
			command.add("-vf");
			command.add("scale=" + effWidth + ":" + effHeight + ", " +
					"pad=" + desiredWidth + ":" + desiredHeight + ":" + xPad + ":" + yPad + "");
		}
				
		// Frame rate
		if (videoQuality.getFrameSettings().getFrameRate() != FrameRate.ORIGINAL) {
			command.add("-r");
			command.add(String.valueOf(videoQuality.getFrameSettings()
					.getFrameRate().getFrameRate()));
		}
	}

	private void transcode(ProcessExecutor processExecutor, File input,
			File output) throws TranscodingException, InterruptedException {
		int exitCode = -1;
		try {
			exitCode = processExecutor.execute();
		} catch (IOException e) {
			processExecutor.destroy();
			transcodingFailed(input, processExecutor.getErrorOutput(),
					processExecutor.getCommand());
		} catch (InterruptedException e) {
			processExecutor.destroy();
			throw e;
		}
		if (exitCode != 0) {
			transcodingFailed(input, processExecutor.getErrorOutput(),
					processExecutor.getCommand());
		}
		if (!output.exists() || output.length() == 0) {
			transcodingFailed(input, processExecutor.getErrorOutput(),
					processExecutor.getCommand());
		}
		if (debug) {
			debug(processExecutor);
		}
	}

	private void transcodingFailed(File input, List<String> errorOutput,
			List<String> command) throws TranscodingException {
		throw new TranscodingException(new TranscodingReason(
				TranscodingReasonDescriptor.TRANSCODING_FAILED, new String[] {
						input.toString(),
						(errorOutput.size() > 0 ? String.valueOf(errorOutput
								.get(errorOutput.size() - 1)) : "")
								+ "\n"
								+ mergeStringList(command) }));
	}

	private void debug(ProcessExecutor processExecutor) {
		System.out.println("ERROR");
		System.out.println(mergeStringList(processExecutor.getErrorOutput()));
		System.out.println("STANDARD");
		System.out
				.println(mergeStringList(processExecutor.getStandardOutput()));
	}

	private String mergeStringList(List<String> errorOutput) {
		StringBuilder sb = new StringBuilder("");
		for (String s : errorOutput) {
			sb.append(s + Constant.LINE_FEED);
		}
		return sb.toString();
	}
}

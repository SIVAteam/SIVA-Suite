package org.iviPro.transcoding.exception;

public enum TranscodingReasonDescriptor {
	// @formatter:off
	INPUT_FILE_NOT_READABLE(11, "The given input file '%s' is not readable.", "INPUT_FILE_NOT_READABLE"),
	OUTPUT_FILE_NOT_WRITEABLE(12, "The given output file '%s' is not writeable.", "OUTPUT_FILE_NOT_WRITEABLE"),
	OUTPUT_FILE_ALREADY_EXISTS(13, "The given output file '%s' already exists.", "OUTPUT_FILE_ALREADY_EXISTS"),
	OS_NOT_SUPPORTED(14, "The running operation system '%s' is not supported by the transcoding library.", "OS_NOT_SUPPORTED"),
	TMP_NOT_WRITEABLE(15, "The temporary directory '%s' is not writeable.", "TMP_NOT_WRITEABLE"),
	
	CONTAINER_DOES_NOT_SUPPORT_CODEC(21, "The container '%s' does not support the specified codec '%s'.", "CONTAINER_DOES_NOT_SUPPORT_CODEC"),
	
	TRANSCODING_EXE_NOT_EXECUTABLE(31, "The given transcoding executable '%s' does not exist or is not executable.", "TRANSCODING_EXE_NOT_EXECUTEABLE"),
	TRANSCODING_FAILED(32, "Transcoding the input file '%s' failed. (%s)", "TRANSCODING_FAILED"),
	
	
	
	AUDIO_CONTAINER_NOT_FOUND(41, "Could not find a audio container matching transcoder parameter '%s'.", "AUDIO_CONTAINER_NOT_FOUND"),
	AUDIO_CODEC_NOT_FOUND(42, "Could not find a audio codec matching transcoder parameter '%s'", "AUDIO_CODEC_NOT_FOUND"),
	VIDEO_CONTAINER_NOT_FOUND(43, "Could not find a video container matching transcoder parameter '%s'.", "VIDEO_CONTAINER_NOT_FOUND"),
	VIDEO_CODEC_NOT_FOUND(44, "Could not find a audio codec matching transcoder parameter '%s'.", "VIDEO_CODEC_NOT_FOUND"),
	CHANNELS_NOT_SUPPORTED(45, "The number of channels '%s' is not supported.", "CHANNELS_NOT_SUPPORTED");
	// @formatter:on

	private final int errorCode;
	private final String message;
	private final String identifier;

	private TranscodingReasonDescriptor(int errorCode, String message,
			String identifier) {
		this.errorCode = errorCode;
		this.message = message;
		this.identifier = identifier;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getMessage() {
		return message;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String toString() {
		return "[" + String.valueOf(errorCode) + "] " + identifier;
	}

}

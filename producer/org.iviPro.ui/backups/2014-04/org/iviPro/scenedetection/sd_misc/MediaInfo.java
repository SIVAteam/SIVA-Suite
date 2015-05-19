package org.iviPro.scenedetection.sd_misc;

import java.math.BigInteger;

public class MediaInfo {

	private BigInteger fileSize;
	
	private String content;
	
	private String colorDomain;
	
	private String fileType;
	
	private String mediaUID;
	
	private String uri;

	public MediaInfo(String content, String colorDomain, String uri) {
		this.content = content;
		this.colorDomain = colorDomain;
		this.uri = uri;
	}

	public BigInteger getFileSize() {
		return fileSize;
	}

	public void setFileSize(BigInteger fileSize) {
		this.fileSize = fileSize;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getColorDomain() {
		return colorDomain;
	}

	public void setColorDomain(String colorDomain) {
		this.colorDomain = colorDomain;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getMediaUID() {
		return mediaUID;
	}

	public void setMediaUID(String mediaUID) {
		this.mediaUID = mediaUID;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean allParamsNull() {
		return fileSize == null && content == null && colorDomain == null && fileType != null && mediaUID == null
				&& uri == null;
	}

	public boolean mediaFormatParams() {
		return fileSize != null || content != null || colorDomain != null || fileType != null;
	}

	public boolean mediaInstanceParams() {
		return mediaUID != null || uri != null;
	}
}
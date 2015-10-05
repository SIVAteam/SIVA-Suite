package hu.model;

public class Settings {
    private String videoFormat;
    private String audioFormat;
    private int width;
    private int height;
    private String screenResolution;
    private String videoFormatSettings;

    public Settings(){
	
    }
    
    public String getVideoFormat() {
	return videoFormat;
    }

    public void setVideoFormat(String videoFormat) {
	this.videoFormat = videoFormat;
    }

    public String getAudioFormat() {
	return audioFormat;
    }

    public void setAudioFormat(String audioFormat) {
	this.audioFormat = audioFormat;
    }

    public int getWidth() {
	return width;
    }

    public void setWidth(int width) {
	this.width = width;
    }

    public int getHeight() {
	return height;
    }

    public void setHeight(int height) {
	this.height = height;
    }

    public String getScreenResolution() {
	return screenResolution;
    }

    public void setScreenResolution(String screenResolution) {
	this.screenResolution = screenResolution;
    }

    public String getVideoFormatSettings() {
	return videoFormatSettings;
    }

    public void setVideoFormatSettings(String videoFormatSettings) {
	this.videoFormatSettings = videoFormatSettings;
    }
}

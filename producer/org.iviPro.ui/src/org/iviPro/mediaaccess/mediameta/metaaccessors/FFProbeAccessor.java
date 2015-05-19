package org.iviPro.mediaaccess.mediameta.metaaccessors;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iviPro.mediaaccess.StreamHandler;
import org.iviPro.mediaaccess.mediameta.interfaces.I_MediaMetaAccessor;
import org.iviPro.model.IMediaObject;
import org.iviPro.utils.PathHelper;

/*
 * ließt die Medien Informationen mit Hilfe von FFMpeg ein
 */
public class FFProbeAccessor implements I_MediaMetaAccessor {
	
	private double aspect;
	private String codec;
	private long length;
	private Dimension dim;
	private long size;
	private int rate;
	private double bitRate;	
	private IMediaObject mediaObject;
	
	public FFProbeAccessor(IMediaObject mediaObject) {
		this.mediaObject = mediaObject;
		this.extractData();
	}
	
	private void extractData() {
		String ffprobe = PathHelper.getPathToFFProbeExe().getAbsolutePath();		
		String inputFilename = mediaObject.getFile().getAbsolutePath();
		
		File file = new File(inputFilename);
		this.size = file.length();
		
		// holen und Ausführen der Runtime
		Runtime rt = Runtime.getRuntime();
		Process p;
		
		try {
			p = rt.exec(ffprobe + " -i \"" + inputFilename + "\" -print_format default -show_format -show_streams");
			StreamHandler errorReader = new StreamHandler(p.getErrorStream(), "stderr");
			StreamHandler outputReader = new StreamHandler(p.getInputStream(), "stdout"); 
			errorReader.start();
			outputReader.start();			
			// warten auf Beendigung
			try {
				int exitVal = p.waitFor();
				
				if (exitVal == 0) {
					errorReader.join();
					outputReader.join();
										
					// Auslesen der Daten
					String stdout = outputReader.getString();
					FormatInfo fi = parseShowFormat(stdout);					
					List<StreamInfo> streaminfos = parseShowStreams(stdout);
					setMetaData(fi, streaminfos);				
				}
				
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			

		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private String checkNullValue(String str) {
		if (str == null) {
			return "";
		}
		return str;
	}
	
	private void setDimension(String width, String height) {
		this.dim = new Dimension();
		int w = 0;
		int h = 0;
		try {			
			w = Integer.parseInt(width);
			h = Integer.parseInt(height);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		this.dim.width = w;
		this.dim.height = h;
	}
	
	private void setDuration(String durationString) {
		Double seconds = 0d;
		try {			
			seconds = Double.parseDouble(durationString);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		this.length = (long) (seconds * 1000000000L);	
	}
	
	private void setAspect(String aspectString) {
		if (aspectString.contains(":")) {
			String[] split = aspectString.split(":");
			double w = 0;
			double h = 1;
			try {			
				w = Double.parseDouble(split[0]);
				h = Double.parseDouble(split[1]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			if (h == 0) {
				this.aspect = 1;
				return;
			}
			this.aspect = w/h;
		}
	}
	
	private void setFrameRate(String fr) {
		double frames = 0;
		double seconds = 1;
		String[] split = fr.split("/");
		try {			
			if (split.length == 2) {
				frames = Double.parseDouble(split[0]);
				seconds = Double.parseDouble(split[1]);
				if (seconds == 0) {
					seconds = 1;
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		this.rate = (int) (frames / seconds);	
		if (this.rate == 0) {
			this.rate = 25;
		}
	}
	
	private void setBitRate(String br) {
		double bitR = 0;
		try {			
			bitR = Double.parseDouble(br);		
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}	
		this.bitRate = bitR;
	}
	
	private void setMetaData(FormatInfo fi, List<StreamInfo> streaminfos) {
		String duration = checkNullValue(fi.getValue(FormatInfoParams.duration));
		setDuration(duration);
		this.codec = checkNullValue(fi.getValue(FormatInfoParams.format_name));
		for (StreamInfo si : streaminfos) {
			if (si.getValue(StreamInfoParams.codec_type).equals("video")) {
				String aspString = checkNullValue(si.getValue(StreamInfoParams.display_aspect_ratio));				
				String frameRate = checkNullValue(si.getValue(StreamInfoParams.r_frame_rate));				
				String width = checkNullValue(si.getValue(StreamInfoParams.width));
				String height = checkNullValue(si.getValue(StreamInfoParams.height));
				setFrameRate(frameRate);
				setAspect(aspString);
				setDimension(width, height);
			} else 
			if (si.getValue(StreamInfoParams.codec_type).equals("audio")) {
				String frameRate = checkNullValue(si.getValue(StreamInfoParams.bit_rate));
				setBitRate(frameRate);
			}
		}
	}
	
	private List<StreamInfo> parseShowStreams(String input) {
		List<StreamInfo> infos = new ArrayList<StreamInfo>();
		
		String[] lines = input.split("\n");
		boolean inStreamBlock = false;
		
		StreamInfo si = null;
		
		for (String line : lines) {
			if (line.contains("[STREAM]")) {
				si = new StreamInfo();
				inStreamBlock = true;
			}
			if (line.contains("[/STREAM]")) {
				infos.add(si);
				inStreamBlock = false;
			}
			if (inStreamBlock) {
				String[] info = line.split("=");
				if (info.length == 2) {
					String key = info[0];
					String value = info[1];
					for (StreamInfoParams param : StreamInfoParams.values()) {						
						if (param.name().toLowerCase().equals(key)) {
							if (si != null) {
								si.addValue(param, value);
							}
						}
					}
				}
			}
		}	
		return infos;
	}
	
	private FormatInfo parseShowFormat(String input) {
		String[] lines = input.split("\n");
		boolean inFormatBlock = false;
		FormatInfo formatInfo = new FormatInfo();
		for (String line : lines) {
			if (line.contains("[FORMAT]")) {
				inFormatBlock = true;
			}
			if (line.contains("[/FORMAT]")) {
				inFormatBlock = false;
			}
			if (inFormatBlock) {
				String[] info = line.split("=");
				if (info.length == 2) {
					String key = info[0];
					String value = info[1];
					
					for (FormatInfoParams param : FormatInfoParams.values()) {						
						if (param.name().toLowerCase().equals(key)) {
							formatInfo.addValue(param, value);
						}
					}
				}
			}
		}
		return formatInfo;
	}

	@Override
	public long getMediaLengthNano() {
		return this.length;
	}

	@Override
	public long getSize() {
		return this.size;
	}

	@Override
	public int getFrameRate() {
		return this.rate;
	}

	@Override
	public String getCodec() {
		return this.codec;
	}

	@Override
	public Dimension getDimension() {
		return this.dim;
	}

	@Override
	public double getAspectRatio() {
		return this.aspect;
	}
	
	private enum FormatInfoParams {
		filename,
		nb_streams,
		format_name,
		format_nong_name,
		start_time,
		duration,
		size,
		bit_rate	
	}
	
	private enum StreamInfoParams {
		index,
		codec_name,
		codec_long_name,
		profile,
		codec_type,
		codec_time_base,
		codec_tag_string,
		codec_tag,
		sample_fmt,
		sample_rate,
		channels,
		bits_per_sample,
		id,
		r_frame_rate,
		avg_frame_rate,
		time_base,
		start_time,
		duration,
		bit_rate,
		nb_frames,
		nb_read_frames,
		nb_read_packets,	
		width,
		height,
		sample_aspect_ratio,
		display_aspect_ratio,
		pix_fmt,
		level,
		timecode,
		r_frame,
		has_b_frame
	};
	
	/**
	 * Kapselt Format Informationen
	 * @author juhoffma
	 */
	private class FormatInfo {
		HashMap<FormatInfoParams, String> values;
		
		public FormatInfo() {
			this.values = new HashMap<FormatInfoParams, String>();
		}	
		
		public void addValue(FormatInfoParams param, String value) {
			this.values.put(param, value);
		}
		
		public String getValue(FormatInfoParams key) {
			return this.values.get(key);
		}
	}
	
	/**
	 * Kapselt Stream Informationen
	 * @author juhoffma
	 */
	private class StreamInfo {
		HashMap<StreamInfoParams, String> values;
		
		public StreamInfo() {
			this.values = new HashMap<StreamInfoParams, String>();
		}	
		
		public void addValue(StreamInfoParams param, String value) {
			this.values.put(param, value);
		}
		
		public String getValue(StreamInfoParams key) {
			return this.values.get(key);
		}
	}

	@Override
	public double getBitRate() {
		return this.bitRate;
	}
}

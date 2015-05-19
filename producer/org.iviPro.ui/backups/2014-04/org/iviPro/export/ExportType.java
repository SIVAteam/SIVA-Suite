package org.iviPro.export;

import org.apache.log4j.Logger;
import org.iviPro.application.Application;
import org.iviPro.model.ProjectSettings;

/**
 * Enum der die Art des Exports kapselt (Flash/Siverlight/...)
 * 
 * @author dellwo
 * 
 */
public enum ExportType {

	FLASH, WEBM, SILVERLIGHT, OGGTHEORA, H264, HTML5, SMIL, CUSTOM;

	private static Logger logger = Logger.getLogger(ExportType.class);

	/**
	 * Gibt einen String an zur Anzeige dieses Typs auf der Oberflaeche dient.
	 * 
	 * @return
	 */
	String getDisplayString() {
		switch (this) {
		case FLASH:
			return "Flash Video";//$NON-NLS-1$
		case SILVERLIGHT:
			return "Windows Media Video";//$NON-NLS-1$
		case OGGTHEORA:
			return "Ogg/Theora Video"; //$NON-NLS-1$
		case H264:
			return "H.264 Video"; //$NON-NLS-1$
		case WEBM:
			return "WebM"; //$NON-NLS-1$
		case HTML5:
			return "HTML5"; //$NON-NLS-1$
		case SMIL:
			return "SMIL";
		case CUSTOM:
			return Messages.ExportType_Name_Custom_Settings;
		default:
			String enumName = toString();
			String displayStr = enumName.charAt(0)
					+ enumName.toLowerCase().substring(1);
			return displayStr;
		}
	}

	/**
	 * Gibt zu einem Anzeige-String (siehe {@link #getDisplayString()}) den
	 * Export-Type zurueck.
	 * 
	 * @param displayStr
	 * @return
	 */
	static ExportType fromDisplayString(String displayStr) {
		for (ExportType type : ExportType.values()) {
			if (displayStr.equals(type.getDisplayString())) {
				return type;
			}
		}
		logger.fatal("fromDisplayString(): No ExportType for given String '" //$NON-NLS-1$
				+ displayStr + "' found."); //$NON-NLS-1$
		throw new RuntimeException("Unknown ExportType Dispay-String: '" //$NON-NLS-1$
				+ displayStr + "'"); //$NON-NLS-1$
	}

	/**
	 * Gibt den Video-Codec Format an, in dem FFMPEG die Videos fuer diesen
	 * Export-Type exportieren soll.
	 * 
	 * @return Zu benutzender FFMPEG-Videocodec.
	 */
	public String getFFmpegVideoCodec() {
		// Video-Codec fuer die verschiedenen Export-Typen zurueck geben
		// Siehe 'ffmpeg -formats' fuer eine liste von moeglichen Codecs
		// oder den Kommentar am Ende dieser Datei.
		switch (this) {
		case FLASH:
			return "flv";//$NON-NLS-1$
		case SILVERLIGHT:
			return "wmv2";//$NON-NLS-1$
		case OGGTHEORA:
			return "libtheora"; //$NON-NLS-1$
		case H264:
			return "libx264"; //$NON-NLS-1$
		case WEBM:
			return "libvpx";
		case HTML5:
			return ""; //$NON-NLS-1$
		case SMIL:
			return "flv";
		case CUSTOM:
			return getSettingsValue(ProjectSettings.VIDEOCODEC);
		default:
			logger.fatal("getFFmpegVideoCodec(): No entry for ExportType '" //$NON-NLS-1$
					+ this + "'"); //$NON-NLS-1$
			throw new RuntimeException("Unknown Video-Codec for ExportType '" //$NON-NLS-1$
					+ this + "'"); //$NON-NLS-1$
		}
	}

	/**
	 * Gibt den Audio-Codec an, in dem FFMPEG die Audio-Spur des Videos fuer
	 * diesen Export-Type exportieren soll.
	 * 
	 * @return Zu benutzender FFMPEG-Audiocodec.
	 */
	public String getFFmpegAudioCodec() {
		// Audio-Codec fuer die verschiedenen Export-Typen zurueck geben
		// Siehe 'ffmpeg -formats' fuer eine liste von moeglichen Codecs
		// oder den Kommentar am Ende dieser Datei.
		switch (this) {
		case FLASH:
			return "libmp3lame";//$NON-NLS-1$
		case SILVERLIGHT:
			return "wmav2";//$NON-NLS-1$
		case OGGTHEORA:
			return "libvorbis"; //$NON-NLS-1$
		case H264:
			return "libvo_aacenc"; //$NON-NLS-1$
		case WEBM:
			return "libvorbis";
		case HTML5:
			return ""; //$NON-NLS-1$
		case SMIL:
			return "libmp3lame";
		case CUSTOM:
			return getSettingsValue(ProjectSettings.AUDIOCODEC); //$NON-NLS-1$
		default:
			logger.fatal("getFFmpegAudioCodec(): No entry for ExportType '" //$NON-NLS-1$
					+ this + "'"); //$NON-NLS-1$
			throw new RuntimeException("Unknown Audio-Codec for ExportType '" //$NON-NLS-1$
					+ this + "'"); //$NON-NLS-1$

		}
	}

	/**
	 * Gibt das FFmpeg-Containerformat fuer diesen Export-Type zurueck.
	 * 
	 * @return
	 */
	public String getFFmpegVideoContainerFormat() {
		// Container-Formate fuer die verschiedenen Export-Typen zurueck geben
		// Siehe 'ffmpeg -formats' fuer eine liste von moeglichen Formaten
		// oder den Kommentar am Ende dieser Datei.
		switch (this) {
		case FLASH:
			return "flv";//$NON-NLS-1$
		case SILVERLIGHT:
			return "asf";//$NON-NLS-1$
		case OGGTHEORA:
			return "ogg"; //$NON-NLS-1$
		case H264:
			return "mp4"; //$NON-NLS-1$
		case WEBM:
			return "webm";
		case HTML5:
			return ""; //$NON-NLS-1$
		case SMIL:
			return "flv";
		case CUSTOM:
			return getSettingsValue(ProjectSettings.VIDEOCONTAINER);
		default:
			logger
					.fatal("getFFmpegContainerFormat(): No entry for ExportType '" //$NON-NLS-1$
							+ this + "'"); //$NON-NLS-1$
			throw new RuntimeException(
					"Unknown Container-Format for ExportType '" + this + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Gibt das FFmpeg-Containerformat fuer diesen Export-Type zurueck.
	 * 
	 * @return
	 */
	public String getFFmpegAudioContainerFormat() {
		// Container-Formate fuer die verschiedenen Export-Typen zurueck geben
		// Siehe 'ffmpeg -formats' fuer eine liste von moeglichen Formaten
		// oder den Kommentar am Ende dieser Datei.
		switch (this) {
		case FLASH:
			return "mp3";//$NON-NLS-1$
		case SILVERLIGHT:
			return "asf";//$NON-NLS-1$
		case OGGTHEORA:
			return "ogg"; //$NON-NLS-1$
		case H264:
			return "mp4"; //$NON-NLS-1$
		case WEBM:
			return "ogg";
		case HTML5:
			return ""; //$NON-NLS-1$
		case SMIL:
			return "mp3";
		case CUSTOM:
			return getSettingsValue(ProjectSettings.AUDIOCONTAINER); //$NON-NLS-1$
		default:
			logger
					.fatal("getFFmpegContainerFormat(): No entry for ExportType '" //$NON-NLS-1$
							+ this + "'"); //$NON-NLS-1$
			throw new RuntimeException(
					"Unknown Container-Format for ExportType '" + this + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Gibt die Dateinamens-Erweiterung der Videodateien fuer diesen Export-Type
	 * zurueck.
	 * 
	 * @return
	 */
	public String getVideoExtension() {
		// Dateinamens-Erweiterung fuer die verschiedenen Export-Typen zurueck
		// geben
		switch (this) {
		case FLASH:
			return "flv";//$NON-NLS-1$
		case SILVERLIGHT:
			return "wmv";//$NON-NLS-1$
		case OGGTHEORA:
			return "ogv"; //$NON-NLS-1$
		case H264:
			return "mp4"; //$NON-NLS-1$
		case WEBM:
			return "webm";
		case HTML5:
			return ""; //$NON-NLS-1$
		case SMIL:
			return "flv";
		case CUSTOM:
			return getSettingsValue(ProjectSettings.VIDEOEXTENSION); //$NON-NLS-1$
		default:
			logger.fatal("getVideoExtension(): No entry for ExportType '" //$NON-NLS-1$
					+ this + "'"); //$NON-NLS-1$
			throw new RuntimeException(
					"Unknown Video-Extension for ExportType '" + this + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Gibt die Dateinamens-Erweiterung der Audiodateien fuer diesen Export-Type
	 * zurueck.
	 * 
	 * @return
	 */
	public String getAudioExtension() {
		// Dateinamens-Erweiterung fuer die verschiedenen Export-Typen zurueck
		// geben
		switch (this) {
		case FLASH:
			return "mp3";//$NON-NLS-1$
		case SILVERLIGHT:
			return "wma";//$NON-NLS-1$
		case OGGTHEORA:
			return "ogg"; //$NON-NLS-1$
		case H264:
			return "m4a"; //$NON-NLS-1$
		case WEBM:
			return "ogg";
		case HTML5:
			return ""; //$NON-NLS-1$
		case SMIL:
			return "mp3";
		case CUSTOM:
			return getSettingsValue(ProjectSettings.AUDIOEXTENSION);
		default:
			logger.fatal("getAudioExtension(): No entry for ExportType '" //$NON-NLS-1$
					+ this + "'"); //$NON-NLS-1$
			throw new RuntimeException(
					"Unknown Audio-Extension for ExportType '" + this + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private String getSettingsValue(String key) {
		return Application.getCurrentProject().getSettings().getExportSettings(null).get(key);
	}
	
	public static String[] getVideoCodecs() {
		String codecs = "flv;libtheora;libvpx;libx264;mjpeg;mpeg1video;mpeg2video;mpeg4;wmv2"; //$NON-NLS-1$
		return codecs.split(";"); //$NON-NLS-1$
	}
	
	public static String[] getVideoContainerFormats() {
		String formats = "3gp;asf;avi;flv;h264;matroska;mjpeg;mpeg;mov;mp4;mpeg;ogg;webm;wmv"; //$NON-NLS-1$
		return formats.split(";"); //$NON-NLS-1$
	}
	
	public static String[] getVideoFileExtensions() {
		String extensions = "avi;flv;mp4;mpeg;ogg;webm;wmv"; //$NON-NLS-1$
		return extensions.split(";"); //$NON-NLS-1$
	}
	
	public static String[] getAudioCodecs() {
		String codecs = "libmp3lame;libvorbis;wmav2"; //$NON-NLS-1$
		return codecs.split(";"); //$NON-NLS-1$
	}
	
	public static String[] getAudioContainerFormats() {
		String formats = "asf;mp3;ogg"; //$NON-NLS-1$
		return formats.split(";"); //$NON-NLS-1$
	}
	
	public static String[] getAudioFileExtensions() {
		String extensions = "mp3;m4a;ogg;wma"; //$NON-NLS-1$
		return extensions.split(";"); //$NON-NLS-1$
	}

	/**
	 * FFmpeg Dateiformate und Codecs!<br>
	 * <code>

File formats:
  E 3g2             3GP2 format
  E 3gp             3GP format
 D  4xm             4X Technologies format
 D  IFF             IFF format
 D  MTV             MTV format
 DE RoQ             id RoQ format
 D  aac             ADTS AAC
 DE ac3             raw AC-3
  E adts            ADTS AAC
 DE aiff            Audio IFF
 DE alaw            pcm A law format
 DE amr             3GPP AMR file format
 D  apc             CRYO APC format
 D  ape             Monkey's Audio
 DE asf             ASF format
  E asf_stream      ASF format
 DE au              SUN AU format
 DE avi             AVI format
  E avm2            Flash 9 (AVM2) format
 D  avs             AVISynth
 D  bethsoftvid     Bethesda Softworks VID format
 D  bfi             Brute Force & Ignorance
 D  c93             Interplay C93
  E crc             CRC testing format
 D  daud            D-Cinema audio format
 DE dirac           raw Dirac
 D  dsicin          Delphine Software International CIN format
 DE dts             raw DTS
 DE dv              DV video format
  E dvd             MPEG-2 PS format (DVD VOB)
 D  dxa             DXA
 D  ea              Electronic Arts Multimedia Format
 D  ea_cdata        Electronic Arts cdata
 DE ffm             ffm format
 D  film_cpk        Sega FILM/CPK format
 DE flac            raw FLAC
 D  flic            FLI/FLC/FLX animation format
 DE flv             FLV format
  E framecrc        framecrc testing format
 DE gif             GIF Animation
 D  gsm             GSM
 DE gxf             GXF format
 DE h261            raw H.261
 DE h263            raw H.263
 DE h264            raw H.264 video format
 D  idcin           id CIN format
 DE image2          image2 sequence
 DE image2pipe      piped image2 sequence
 D  ingenient       Ingenient MJPEG
 D  ipmovie         Interplay MVE format
  E ipod            iPod H.264 MP4 format
 D  lmlm4           lmlm4 raw format
 DE m4v             raw MPEG-4 video format
 DE matroska        Matroska file format
 DE mjpeg           MJPEG video
 D  mlp             raw MLP
 D  mm              American Laser Games MM format
 DE mmf             mmf format
  E mov             MOV format
 D  mov,mp4,m4a,3gp,3g2,mj2 QuickTime/MPEG-4/Motion JPEG 2000 format
  E mp2             MPEG audio layer 2
 DE mp3             MPEG audio layer 3
  E mp4             MP4 format
 D  mpc             Musepack
 D  mpc8            Musepack SV8
 DE mpeg            MPEG-1 System format
  E mpeg1video      MPEG video
  E mpeg2video      MPEG-2 video
 DE mpegts          MPEG-2 transport stream format
 D  mpegtsraw       MPEG-2 raw transport stream format
 D  mpegvideo       MPEG video
  E mpjpeg          Mime multipart JPEG format
 D  msnwctcp        MSN TCP Webcam stream
 DE mulaw           pcm mu law format
 D  mxf             MXF format
 D  nsv             NullSoft Video format
  E null            null video format
 DE nut             NUT format
 D  nuv             NuppelVideo format
 DE ogg             Ogg
 D  oma             Sony OpenMG audio
  E psp             PSP MP4 format
 D  psxstr          Sony Playstation STR format
 D  pva             TechnoTrend PVA file and stream format
 DE rawvideo        raw video format
 D  redir           Redirector format
 D  rl2             rl2 format
 DE rm              RM format
 D  rpl             RPL/ARMovie format
  E rtp             RTP output format
 D  rtsp            RTSP input format
 DE s16be           pcm signed 16 bit big endian format
 DE s16le           pcm signed 16 bit little endian format
 DE s8              pcm signed 8 bit format
 D  sdp             SDP
 D  shn             raw Shorten
 D  siff            Beam Software SIFF
 D  smk             Smacker video
 D  sol             Sierra SOL format
  E svcd            MPEG-2 PS format (VOB)
 DE swf             Flash format
 D  thp             THP
 D  tiertexseq      Tiertex Limited SEQ format
 D  tta             True Audio
 D  txd             txd format
 DE u16be           pcm unsigned 16 bit big endian format
 DE u16le           pcm unsigned 16 bit little endian format
 DE u8              pcm unsigned 8 bit format
 D  vc1             raw VC-1
 D  vc1test         VC-1 test bitstream format
  E vcd             MPEG-1 System format (VCD)
 D  vfwcap          VFW video capture
 D  vmd             Sierra VMD format
  E vob             MPEG-2 PS format (VOB)
 DE voc             Creative Voice file format
 DE wav             WAV format
 D  wc3movie        Wing Commander III movie format
 D  wsaud           Westwood Studios audio format
 D  wsvqa           Westwood Studios VQA format
 D  wv              WavPack
 D  xa              Maxis XA File Format
 DE yuv4mpegpipe    YUV4MPEG pipe format

Codecs:
 D V    4xm             4X Movie
 D V D  8bps            QuickTime 8BPS video
 D A    8svx_exp        8SVX exponential
 D A    8svx_fib        8SVX fibonacci
 D V D  aasc            Autodesk RLE
  EA    ac3             ATSC A/52 / AC-3
 D A    adpcm_4xm       4X Movie ADPCM
 DEA    adpcm_adx       SEGA CRI ADX
 D A    adpcm_ct        Creative Technology ADPCM
 D A    adpcm_ea        Electronic Arts ADPCM
 D A    adpcm_ea_maxis_xa Electronic Arts Maxis CDROM XA ADPCM
 D A    adpcm_ea_r1     Electronic Arts R1 ADPCM
 D A    adpcm_ea_r2     Electronic Arts R2 ADPCM
 D A    adpcm_ea_r3     Electronic Arts R3 ADPCM
 D A    adpcm_ea_xas    Electronic Arts XAS ADPCM
 D A    adpcm_ima_amv   IMA AMV ADPCM
 D A    adpcm_ima_dk3   IMA Duck DK3 ADPCM
 D A    adpcm_ima_dk4   IMA Duck DK4 ADPCM
 D A    adpcm_ima_ea_eacs IMA Electronic Arts EACS ADPCM
 D A    adpcm_ima_ea_sead IMA Electronic Arts SEAD ADPCM
 DEA    adpcm_ima_qt    IMA QuickTime ADPCM
 D A    adpcm_ima_smjpeg IMA Loki SDL MJPEG ADPCM
 DEA    adpcm_ima_wav   IMA Wav ADPCM
 D A    adpcm_ima_ws    IMA Westwood ADPCM
 DEA    adpcm_ms        Microsoft ADPCM
 D A    adpcm_sbpro_2   Sound Blaster Pro 2-bit ADPCM
 D A    adpcm_sbpro_3   Sound Blaster Pro 2.6-bit ADPCM
 D A    adpcm_sbpro_4   Sound Blaster Pro 4-bit ADPCM
 DEA    adpcm_swf       Shockwave Flash ADPCM
 D A    adpcm_thp       Nintendo Gamecube THP ADPCM
 D A    adpcm_xa        CDROM XA ADPCM
 DEA    adpcm_yamaha    Yamaha ADPCM
 D A    alac            ALAC (Apple Lossless Audio Codec)
 D V    amv             AMV Video
 D A    ape             Monkey's Audio
 DEV D  asv1            ASUS V1
 DEV D  asv2            ASUS V2
 D A    atrac3          Atrac 3 (Adaptive TRansform Acoustic Coding 3)
 D V D  avs             AVS (Audio Video Standard) video
 D V    bethsoftvid     Bethesda VID video
 D V    bfi             Brute Force & Ignorance
 DEV    bmp             BMP image
 D V D  c93             Interplay C93
 D V D  camstudio       CamStudio
 D V D  camtasia        TechSmith Screen Capture Codec
 D V D  cavs            Chinese AVS video (AVS1-P2, JiZhun profile)
 D V D  cinepak         Cinepak
 D V D  cljr            Cirrus Logic AccuPak
 D A    cook            COOK
 D V D  cyuv            Creative YUV (CYUV)
 D A    dca             DCA (DTS Coherent Acoustics)
 DEV D  dnxhd           VC3/DNxHD
 D A    dsicinaudio     Delphine Software International CIN audio
 D V D  dsicinvideo     Delphine Software International CIN video
 DES    dvbsub          DVB subtitles
 DES    dvdsub          DVD subtitles
 DEV D  dvvideo         DV (Digital Video)
 D V    dxa             Feeble Files/ScummVM DXA
 D V D  escape124       Escape 124
 DEV D  ffv1            FFmpeg codec #1
 DEVSD  ffvhuff         Huffyuv FFmpeg variant
 DEA    flac            FLAC (Free Lossless Audio Codec)
 DEV D  flashsv         Flash Screen Video
 D V D  flic            Autodesk Animator Flic video
 DEVSD  flv             Flash Video
 D V D  fraps           Fraps
 DEA    g726            G.726 ADPCM
 DEV    gif             GIF (Graphics Interchange Format)
 DEV D  h261            H.261
 DEVSDT h263            H.263
 D VSD  h263i           H.263i
  EV    h263p           H.263+ / H.263 version 2
 D V DT h264            H.264 / AVC / MPEG-4 AVC / MPEG-4 part 10
 DEVSD  huffyuv         Huffyuv / HuffYUV
 D V D  idcinvideo      id Quake II CIN video
 D A    imc             IMC (Intel Music Coder)
 D V D  indeo2          Intel Indeo 2
 D V    indeo3          Intel Indeo 3
 D A    interplay_dpcm  Interplay DPCM
 D V D  interplayvideo  Interplay MVE Video
 DEV D  jpegls          JPEG-LS
 D V    kmvc            Karl Morton's video codec
 D A    liba52          liba52 ATSC A/52 / AC-3
  EA    libfaac         libfaac AAC (Advanced Audio Codec)
 D A    libfaad         libfaad AAC (Advanced Audio Codec)
 DEA    libgsm          libgsm GSM
 DEA    libgsm_ms       libgsm GSM Microsoft variant
  EA    libmp3lame      libmp3lame MP3 (MPEG audio layer 3)
  EV    libtheora       libtheora Theora
  EA    libvorbis       libvorbis Vorbis
  EV    libx264         libx264 H.264 / AVC / MPEG-4 AVC / MPEG-4 part 10
  EV    libxvid         libxvidcore MPEG-4 part 2
  EV    ljpeg           Lossless JPEG
 D V D  loco            LOCO
 D A    mace3           MACE (Macintosh Audio Compression/Expansion) 3:1
 D A    mace6           MACE (Macintosh Audio Compression/Expansion) 6:1
 D V D  mdec            Sony PlayStation MDEC (Motion DECoder)
 D V D  mimic           Mimic
 DEV D  mjpeg           MJPEG (Motion JPEG)
 D V D  mjpegb          Apple MJPEG-B
 D V D  mmvideo         American Laser Games MM Video
 DEA    mp2             MP2 (MPEG audio layer 2)
 D A    mp3             MP3 (MPEG audio layer 3)
 D A    mp3adu          ADU (Application Data Unit) MP3 (MPEG audio layer 3)
 D A    mp3on4          MP3onMP4
 D A    mpc7            Musepack SV7
 D A    mpc8            Musepack SV8
 DEVSDT mpeg1video      MPEG-1 video
 DEVSDT mpeg2video      MPEG-2 video
 DEVSDT mpeg4           MPEG-4 part 2
 D A    mpeg4aac        libfaad AAC (Advanced Audio Codec)
 D VSDT mpegvideo       MPEG-1 video
 DEVSD  msmpeg4         MPEG-4 part 2 Microsoft variant version 3
 DEVSD  msmpeg4v1       MPEG-4 part 2 Microsoft variant version 1
 DEVSD  msmpeg4v2       MPEG-4 part 2 Microsoft variant version 2
 D V D  msrle           Microsoft RLE
 D V D  msvideo1        Microsoft Video 1
 D V D  mszh            LCL (LossLess Codec Library) MSZH
 D A    nellymoser      Nellymoser Asao
 D V D  nuv             NuppelVideo
 DEV    pam             PAM (Portable AnyMap) image
 DEV    pbm             PBM (Portable BitMap) image
 DEA    pcm_alaw        A-law PCM
 D A    pcm_dvd         signed 16|20|24-bit big-endian PCM
 DEA    pcm_mulaw       mu-law PCM
 DEA    pcm_s16be       signed 16-bit big-endian PCM
 DEA    pcm_s16le       signed 16-bit little-endian PCM
 D A    pcm_s16le_planar 16-bit little-endian planar PCM
 DEA    pcm_s24be       signed 24-bit big-endian PCM
 DEA    pcm_s24daud     D-Cinema audio signed 24-bit PCM
 DEA    pcm_s24le       signed 24-bit little-endian PCM
 DEA    pcm_s32be       signed 32-bit big-endian PCM
 DEA    pcm_s32le       signed 32-bit little-endian PCM
 DEA    pcm_s8          signed 8-bit PCM
 DEA    pcm_u16be       unsigned 16-bit big-endian PCM
 DEA    pcm_u16le       unsigned 16-bit little-endian PCM
 DEA    pcm_u24be       unsigned 24-bit big-endian PCM
 DEA    pcm_u24le       unsigned 24-bit little-endian PCM
 DEA    pcm_u32be       unsigned 32-bit big-endian PCM
 DEA    pcm_u32le       unsigned 32-bit little-endian PCM
 DEA    pcm_u8          unsigned 8-bit PCM
 DEA    pcm_zork        Zork PCM
 D V    pcx             PC Paintbrush PCX image
 DEV    pgm             PGM (Portable GrayMap) image
 DEV    pgmyuv          PGMYUV (Portable GrayMap YUV) image
 DEV    png             PNG image
 DEV    ppm             PPM (Portable PixelMap) image
 D V    ptx             V.Flash PTX image
 D A    qdm2            QDesign Music Codec 2
 D V D  qdraw           Apple QuickDraw
 D V D  qpeg            Q-team QPEG
 DEV D  qtrle           QuickTime Animation (RLE) video
 DEV    rawvideo        raw video
 D A    real_144        RealAudio 1.0 (14.4K)
 D A    real_288        RealAudio 2.0 (28.8K)
 D V D  rl2             RL2 video
 DEA    roq_dpcm        id RoQ DPCM
 DEV D  roqvideo        id RoQ video
 D V D  rpza            QuickTime video (RPZA)
 DEV D  rv10            RealVideo 1.0
 DEV D  rv20            RealVideo 2.0
 DEV    sgi             SGI image
 D A    shorten         Shorten
 D A    smackaud        Smacker audio
 D V    smackvid        Smacker video
 D V D  smc             QuickTime Graphics (SMC)
 DEV    snow            Snow
 D A    sol_dpcm        Sol DPCM
 DEA    sonic           Sonic
  EA    sonicls         Sonic lossless
 D V D  sp5x            Sunplus JPEG (SP5X)
 D V    sunrast         Sun Rasterfile image
 DEV D  svq1            Sorenson Vector Quantizer 1
 D VSD  svq3            Sorenson Vector Quantizer 3
 DEV    targa           Truevision Targa image
 D V    theora          Theora
 D V D  thp             Nintendo Gamecube THP video
 D V D  tiertexseqvideo Tiertex Limited SEQ video
 DEV    tiff            TIFF image
 D V D  truemotion1     Duck TrueMotion 1.0
 D V D  truemotion2     Duck TrueMotion 2.0
 D A    truespeech      DSP Group TrueSpeech
 D A    tta             True Audio
 D V    txd             Renderware TXD (TeXture Dictionary) image
 D V D  ultimotion      IBM UltiMotion
 D V    vb              Beam Software VB
 D V    vc1             SMPTE VC-1
 D V D  vcr1            ATI VCR1
 D A    vmdaudio        Sierra VMD audio
 D V D  vmdvideo        Sierra VMD video
 D V    vmnc            VMware Screen Codec / VMware Video
 DEA    vorbis          Vorbis
 D V    vp3             On2 VP3
 D V D  vp5             On2 VP5
 D V D  vp6             On2 VP6
 D V D  vp6a            On2 VP6 (Flash version, with alpha channel)
 D V D  vp6f            On2 VP6 (Flash version)
 D V D  vqavideo        Westwood Studios VQA (Vector Quantized Animation) video
 D A    wavpack         WavPack
 DEA    wmav1           Windows Media Audio 1
 DEA    wmav2           Windows Media Audio 2
 DEVSD  wmv1            Windows Media Video 7
 DEVSD  wmv2            Windows Media Video 8
 D V    wmv3            Windows Media Video 9
 D V D  wnv1            Winnov WNV1
 D A    ws_snd1         Westwood Audio (SND1)
 D A    xan_dpcm        Xan DPCM
 D V D  xan_wc3         Wing Commander III / Xan
 D V D  xl              Miro VideoXL
 D S    xsub            XSUB
 DEV D  zlib            LCL (LossLess Codec Library) ZLIB
 DEV    zmbv            Zip Motion Blocks Video

Bitstream filters:
 text2movsub remove_extra noise mov2textsub mp3decomp mp3comp mjpegadump imxdump h264_mp4toannexb dump_extra
Supported file protocols:
 file: http: pipe: rtp: tcp: udp:
Frame size, frame rate abbreviations:
 ntsc pal qntsc qpal sntsc spal film ntsc-film sqcif qcif cif 4cif

Note, the names of encoders and decoders do not always match, so there are
several cases where the above table shows encoder only or decoder only entries
even though both encoding and decoding are supported. For example, the h263
decoder corresponds to the h263 and h263p encoders, for file formats it is even
worse.

	  
	 * </code>
	 */

}

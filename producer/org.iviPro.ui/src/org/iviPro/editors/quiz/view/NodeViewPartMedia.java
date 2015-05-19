package org.iviPro.editors.quiz.view;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.iviPro.editors.quiz.std.FileBrowser;
import org.iviPro.editors.quiz.std.NodeModel;
import org.iviPro.model.quiz.AdditionalInfo;

/**
 * Diese Klasse implementiert die graphische Darstellung der Datei-ZusatzInfos
 * (als ExpandBarItem). Es koennen Dateien mittels Dateibrowser ausgewaehlt
 * werden.
 * 
 * @author Sabine Gattermann
 * 
 */
public class NodeViewPartMedia {

	private static Composite parent;
	private static int loadFromDB;
	private static Composite compositeMedia;
	private static NodeModel iFace;
	private static ExpandItem item;
	private static Composite compositeMediaShow;
	private static Composite compositeMediaShowVideo;
	private static Composite compositeMediaShowAudio;
	private static Composite compositeMediaShowImage;
	private static LinkedList<Button> removeVideoButtons;
	private static LinkedList<Button> removeAudioButtons;
	private static LinkedList<Button> removeImageButtons;
	private static LinkedList<Text> audioTextFields;
	private static LinkedList<Text> imageTextFields;
	private static LinkedList<Text> videoTextFields;
	private static int audioCounter;
	private static int videoCounter;
	private static int imageCounter;
	private static LinkedList<Label> videoLabelList;
	private static LinkedList<Label> audioLabelList;
	private static LinkedList<Label> imageLabelList;
	private static Color highlightColor;

	/**
	 * Konstruktor
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 * @param iFace
	 *            Die Model-Klasse.
	 */
	public NodeViewPartMedia(Composite parent, NodeModel iFace) {
		NodeViewPartMedia.parent = parent;
		NodeViewPartMedia.iFace = iFace;
		highlightColor = new Color(parent.getDisplay(), 211, 211, 211);
	}

	/**
	 * Oeffnet die graphische Ausgabe in einer ExpandBar.
	 * 
	 * @param expandIt
	 *            Der Indikator (true/false) ob ExpandBarItem expandiert ist.
	 */
	public static void open(boolean expandIt) {
		loadFromDB = 0;

		videoLabelList = new LinkedList<Label>();
		videoTextFields = new LinkedList<Text>();
		removeVideoButtons = new LinkedList<Button>();
		videoCounter = 0;

		audioLabelList = new LinkedList<Label>();
		audioTextFields = new LinkedList<Text>();
		removeAudioButtons = new LinkedList<Button>();
		audioCounter = 0;

		imageLabelList = new LinkedList<Label>();
		imageTextFields = new LinkedList<Text>();
		removeImageButtons = new LinkedList<Button>();
		imageCounter = 0;

		item = new ExpandItem((ExpandBar) parent, SWT.NONE, 2);

		// Basis-Composite
		compositeMedia = new Composite(parent, SWT.NONE);
		GridLayout mediaLayout = new GridLayout(1, false);
		compositeMedia.setLayout(mediaLayout);
		GridData mediaGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		compositeMedia.setLayoutData(mediaGridData);
		compositeMedia.setBackground(highlightColor);

		compositeMediaShow = new Composite(compositeMedia, SWT.NONE);
		GridLayout cmsLayout = new GridLayout(2, false);
		compositeMediaShow.setLayout(cmsLayout);
		compositeMediaShow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		compositeMediaShow.setBackground(highlightColor);

		compositeMediaShowVideo = new Composite(compositeMedia, SWT.NONE);
		GridLayout cmsvLayout = new GridLayout(3, false);
		compositeMediaShowVideo.setLayout(cmsvLayout);
		compositeMediaShowVideo.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		compositeMediaShowVideo.setBackground(highlightColor);

		GridData sepGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		sepGridData.horizontalSpan = 3;

		// erstellt Maske Video
		Button videoButton = new Button(compositeMediaShowVideo, SWT.PUSH);

		GridData vlGridData = new GridData();
		vlGridData.horizontalSpan = 3;
		vlGridData.horizontalAlignment = GridData.BEGINNING;
		videoButton.setLayoutData(vlGridData);

		GridData vbGridData = new GridData();
		vbGridData.horizontalSpan = 1;
		vbGridData.horizontalAlignment = GridData.CENTER;
		videoButton.setText(" Video hinzufügen ");
		videoButton.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				LinkedList<AdditionalInfo> video;
				FileBrowser browser;

				if (loadFromDB <= 3) {
					video = iFace.getCurrentVideoList();
					for (int i = 0; i < video.size(); i++) {
						String text = video.get(i).getMediaName();
						text = text.substring(1 + text.indexOf('?'));
						generateMediaRow(text, video.get(i)
								.getIdAdditionalInfo());
					}
					loadFromDB++;
				} else {
					browser = new FileBrowser("video");

					if (browser.getFileList().length > 0) {
						generateMediaRow(browser.getFileList()[0].toString(),
								-1);
					}
				}

			}

			private void generateMediaRow(String text, int id) {
				// neue Media-Zeile einfuegen
				videoCounter++;

				Label l = new Label(compositeMediaShowVideo, SWT.NONE);
				l.setText("Video " + videoCounter + ": ");
				l.setBackground(highlightColor);
				videoLabelList.add(l);

				Text t = new Text(compositeMediaShowVideo, SWT.READ_ONLY
						| SWT.WRAP | SWT.BORDER);
				GridData tGridData = new GridData();
				tGridData.widthHint = 350;
				tGridData.horizontalSpan = 1;
				t.setLayoutData(tGridData);
				t.setText(text);
				t.setData("videoCounter", String.valueOf(videoCounter));
				videoTextFields.add(t);

				Button b = new Button(compositeMediaShowVideo, SWT.PUSH);
				b.setText("entfernen");
				b.setData("videoCounter", videoCounter);
				b.setData("idMedia", id);
				removeVideoButtons.add(b);
				b.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event arg0) {

						String counter = String.valueOf(arg0.widget
								.getData("videoCounter"));
						if (arg0.widget.getData("idMedia") != null) {
							String str = String.valueOf(arg0.widget
									.getData("idMedia"));
							int delId = Integer.parseInt(str);
							if (delId > 0)
								iFace.deleteMedia(delId);
						}
						Text t = null;
						int atIndex = 0;
						boolean found = false;

						while (!found && atIndex < videoTextFields.size()) {
							t = videoTextFields.get(atIndex);
							if (t.getData("videoCounter").equals(counter))
								found = true;
							atIndex++;
						}

						if (found == true) {
							Label labelToDel = videoLabelList.get(atIndex - 1);
							videoLabelList.remove(atIndex - 1);
							for (int i = 0; i < videoLabelList.size(); i++) {
								Label l = videoLabelList.get(i);
								l.setText("Video " + (i + 1) + ": ");
							}
							labelToDel.dispose();

							videoTextFields.remove(atIndex - 1);
							t.dispose();
							arg0.widget.dispose();
							videoCounter--;
						}

						compositeMediaShowVideo.layout();
						// Neue Groesse berechnen
						compositeMedia.setSize(compositeMediaShowVideo
								.computeSize(SWT.DEFAULT, SWT.DEFAULT));
						compositeMediaShowVideo.layout();
						item.setHeight(compositeMedia.computeSize(SWT.DEFAULT,
								SWT.DEFAULT).y);
						parent.layout();
					}

				});

				// Composite-Updaten
				compositeMediaShowVideo.layout(new Control[] { t, b });
				// Neue Groesse berechnen
				compositeMediaShowVideo.setSize(compositeMediaShowVideo
						.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				compositeMediaShowVideo.layout();
				compositeMediaShow.layout();
				item.setHeight(compositeMedia.computeSize(SWT.DEFAULT,
						SWT.DEFAULT).y);
				parent.layout();
			}
		});

		compositeMediaShowAudio = new Composite(compositeMedia, SWT.NONE);
		GridLayout cmsaLayout = new GridLayout(3, false);
		compositeMediaShowAudio.setLayout(cmsaLayout);
		compositeMediaShowAudio.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		compositeMediaShowAudio.setBackground(highlightColor);

		Label sepLabel_audio = new Label(compositeMediaShowAudio, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		sepLabel_audio.setLayoutData(sepGridData);

		compositeMediaShowImage = new Composite(compositeMedia, SWT.NONE);
		GridLayout cmsiLayout = new GridLayout(3, false);
		compositeMediaShowImage.setLayout(cmsiLayout);
		compositeMediaShowImage.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		compositeMediaShowImage.setBackground(highlightColor);

		Label sepLabel_image = new Label(compositeMediaShowImage, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		sepLabel_image.setLayoutData(sepGridData);

		// erstellt Maske Audio

		GridData alGridData = new GridData();
		alGridData.horizontalSpan = 3;
		alGridData.horizontalAlignment = GridData.BEGINNING;

		Button audioButton = new Button(compositeMediaShowAudio, SWT.PUSH);
		GridData abGridData = new GridData();
		abGridData.horizontalSpan = 1;
		abGridData.horizontalAlignment = GridData.CENTER;
		audioButton.setLayoutData(alGridData);
		audioButton.setText("  Audio hinzufügen  ");
		audioButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				LinkedList<AdditionalInfo> audio;
				FileBrowser browser;

				if (loadFromDB <= 3) {
					audio = iFace.getCurrentAudioList();
					for (int i = 0; i < audio.size(); i++) {
						String text = audio.get(i).getMediaName();
						text = text.substring(1 + text.indexOf('?'));
						generateMediaRow(text, audio.get(i)
								.getIdAdditionalInfo());
					}
					loadFromDB++;
				} else {
					browser = new FileBrowser("audio");

					if (browser.getFileList().length > 0) {
						generateMediaRow(browser.getFileList()[0].toString(),
								-1);
					}
				}
			}

			private void generateMediaRow(String text, int id) {
				// neue Media-Zeile einfuegen
				audioCounter++;

				Label l = new Label(compositeMediaShowAudio, SWT.NONE);
				l.setText("Audio " + audioCounter + ": ");
				l.setBackground(highlightColor);
				audioLabelList.add(l);

				Text t = new Text(compositeMediaShowAudio, SWT.READ_ONLY
						| SWT.WRAP | SWT.BORDER);
				GridData tGridData = new GridData();
				tGridData.widthHint = 350;
				tGridData.horizontalSpan = 1;
				t.setLayoutData(tGridData);
				t.setText(text);
				t.setData("audioCounter", String.valueOf(audioCounter));
				audioTextFields.add(t);

				Button b = new Button(compositeMediaShowAudio, SWT.PUSH);
				b.setText("entfernen");
				b.setData("audioCounter", audioCounter);
				b.setData("idMedia", id);
				removeAudioButtons.add(b);
				b.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event arg0) {

						String counter = String.valueOf(arg0.widget
								.getData("audioCounter"));
						// bei null wurde Audio noch nicht in die DB
						// geschrieben!!
						if (arg0.widget.getData("idMedia") != null) {
							String str = String.valueOf(arg0.widget
									.getData("idMedia"));
							int delId = Integer.parseInt(str);
							if (delId > 0)
								iFace.deleteMedia(delId);
						}

						Text t = null;
						int atIndex = 0;
						boolean found = false;

						while (!found && atIndex < audioTextFields.size()) {
							t = audioTextFields.get(atIndex);
							if (t.getData("audioCounter").equals(counter))
								found = true;
							atIndex++;
						}

						if (found == true) {
							Label labelToDel = audioLabelList.get(atIndex - 1);
							audioLabelList.remove(atIndex - 1);
							for (int i = 0; i < audioLabelList.size(); i++) {
								Label l = audioLabelList.get(i);
								l.setText("Audio " + (i + 1) + ": ");
							}
							labelToDel.dispose();
							audioTextFields.remove(atIndex - 1);
							t.dispose();
							arg0.widget.dispose();
							audioCounter--;
						}

						compositeMediaShow.layout();
						// Neue Groesse berechnen
						compositeMedia.setSize(compositeMediaShowAudio
								.computeSize(SWT.DEFAULT, SWT.DEFAULT));
						compositeMediaShowAudio.layout();
						compositeMediaShow.layout();
						item.setHeight(compositeMedia.computeSize(SWT.DEFAULT,
								SWT.DEFAULT).y);
						parent.layout();
					}

				});

				// Composite-Updaten
				compositeMediaShowAudio.layout(new Control[] { t, b });
				// Neue Groesse berechnen
				compositeMediaShowAudio.setSize(compositeMediaShowAudio
						.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				compositeMediaShowAudio.layout();
				compositeMediaShow.layout();
				item.setHeight(compositeMedia.computeSize(SWT.DEFAULT,
						SWT.DEFAULT).y);
				parent.layout();
			}
		});

		// erstellt Maske Image

		GridData ilGridData = new GridData();
		ilGridData.horizontalSpan = 3;
		ilGridData.horizontalAlignment = GridData.BEGINNING;

		Button imageButton = new Button(compositeMediaShowImage, SWT.PUSH);
		imageButton.setLayoutData(ilGridData);
		imageButton.setText(" Bild hinzufügen ");
		imageButton.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				LinkedList<AdditionalInfo> image;
				FileBrowser browser;

				if (loadFromDB <= 3) {
					image = iFace.getCurrentImageList();
					for (int i = 0; i < image.size(); i++) {
						String text = image.get(i).getMediaName();
						text = text.substring(1 + text.indexOf('?'));
						generateMediaRow(text, image.get(i)
								.getIdAdditionalInfo());
					}
					loadFromDB++;
				} else {
					browser = new FileBrowser("image");

					if (browser.getFileList().length > 0) {
						generateMediaRow(browser.getFileList()[0].toString(),
								-1);
					}
				}
			}

			private void generateMediaRow(String text, int id) {
				// neue Media-Zeile einfuegen
				imageCounter++;

				Label l = new Label(compositeMediaShowImage, SWT.NONE);
				l.setText("    Bild " + imageCounter + ": ");
				l.setBackground(highlightColor);
				imageLabelList.add(l);

				Text t = new Text(compositeMediaShowImage, SWT.READ_ONLY
						| SWT.WRAP | SWT.BORDER);
				GridData tGridData = new GridData();
				tGridData.widthHint = 350;
				tGridData.horizontalSpan = 1;
				t.setLayoutData(tGridData);
				t.setText(text);
				t.setData("imageCounter", String.valueOf(imageCounter));
				imageTextFields.add(t);

				Button b = new Button(compositeMediaShowImage, SWT.PUSH);
				b.setText("entfernen");
				b.setData("imageCounter", imageCounter);
				b.setData("idMedia", id);
				removeImageButtons.add(b);
				b.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event arg0) {

						String counter = String.valueOf(arg0.widget
								.getData("imageCounter"));

						// bei null wurde bild noch nicht in die DB
						// geschrieben!!
						if (arg0.widget.getData("idMedia") != null) {
							String str = String.valueOf(arg0.widget
									.getData("idMedia"));
							int delId = Integer.parseInt(str);
							if (delId > 0)
								iFace.deleteMedia(delId);
						}

						Text t = null;
						int atIndex = 0;
						boolean found = false;

						while (!found && atIndex < imageTextFields.size()) {
							t = imageTextFields.get(atIndex);
							if (t.getData("imageCounter").equals(counter))
								found = true;
							atIndex++;
						}

						if (found == true) {
							Label labelToDel = imageLabelList.get(atIndex - 1);
							imageLabelList.remove(atIndex - 1);
							for (int i = 0; i < imageLabelList.size(); i++) {
								Label l = imageLabelList.get(i);
								l.setText("Bild " + (i + 1) + ": ");
							}
							labelToDel.dispose();
							imageTextFields.remove(atIndex - 1);
							t.dispose();
							arg0.widget.dispose();
							imageCounter--;
						}

						compositeMediaShow.layout();
						// Neue Groesse berechnen
						compositeMedia.setSize(compositeMediaShowImage
								.computeSize(SWT.DEFAULT, SWT.DEFAULT));
						compositeMediaShowImage.layout();
						compositeMediaShow.layout();
						item.setHeight(compositeMedia.computeSize(SWT.DEFAULT,
								SWT.DEFAULT).y);
						parent.layout();
					}

				});

				// Composite-Updaten
				compositeMediaShowImage.layout(new Control[] { t, b });
				// Neue Groesse berechnen
				compositeMediaShowImage.setSize(compositeMediaShowImage
						.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				compositeMediaShowImage.layout();
				compositeMediaShow.layout();
				item.setHeight(compositeMedia.computeSize(SWT.DEFAULT,
						SWT.DEFAULT).y);
				parent.layout();
			}
		});

		videoButton.setSelection(true);
		Event vEvent = new Event();
		vEvent.type = SWT.Selection;
		vEvent.widget = videoButton;
		videoButton.notifyListeners(SWT.Selection, vEvent);

		audioButton.setSelection(true);
		Event aEvent = new Event();
		aEvent.type = SWT.Selection;
		aEvent.widget = audioButton;
		audioButton.notifyListeners(SWT.Selection, aEvent);

		imageButton.setSelection(true);
		Event iEvent = new Event();
		iEvent.type = SWT.Selection;
		iEvent.widget = imageButton;
		imageButton.notifyListeners(SWT.Selection, iEvent);

		loadFromDB++;

		compositeMedia.setSize(compositeMedia.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

		item.setText("Media-Dateien");
		item.setHeight(compositeMedia.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setControl(compositeMedia);
		item.setData("position", "2");
		item.setExpanded(expandIt);

	}

	/**
	 * Getter fuer gewaehlte / gespeicherte Videos.
	 * 
	 * @return Die Liste der Videos.
	 * 
	 *         BugFixing Links wurden rausgelöscht. Die Linkliste ist somit
	 *         immer leer die zurückgegeben wird!
	 */
	public static LinkedList<String> getVideoFromTextFields() {
//		int count = videoTextFields.size();
//		LinkedList<String> video = new LinkedList<String>();
//
//		for (int i = 0; i < count; i++) {
//			video.add(i, videoTextFields.get(i).getText());
//		}
//
//		return video;
		return new LinkedList<String>();
	}

	/**
	 * Getter fuer gewaehlte / gespeicherte Audios.
	 * 
	 * @return Die Liste der Audios.
	 * LinkedList<String> video
	 * 
	 * BugFixing Links wurden rausgelöscht. Die Linkliste ist somit
	 * immer leer die zurückgegeben wird!
	 */
	public static LinkedList<String> getAudioFromTextFields() {
//		int count = audioTextFields.size();
//		LinkedList<String> audio = new LinkedList<String>();
//
//		for (int i = 0; i < count; i++) {
//			audio.add(i, audioTextFields.get(i).getText());
//		}
//
//		return audio;
		return new LinkedList<String>();
	}

	/**
	 * Getter fuer gewaehlte / gespeicherte Images.
	 * 
	 * @return Die Liste der Imagess.
	 * 
	 * BugFixing Links wurden rausgelöscht. Die Linkliste ist somit
	 * immer leer die zurückgegeben wird!
	 */
	public static LinkedList<String> getImageFromTextFields() {
//		int count = imageTextFields.size();
//		LinkedList<String> image = new LinkedList<String>();
//
//		for (int i = 0; i < count; i++) {
//			image.add(i, imageTextFields.get(i).getText());
//		}
//
//		return image;
		return new LinkedList<String>();
	}

}

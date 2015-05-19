/**
 * 
 */
package org.iviPro.mediaaccess.mediameta;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.iviPro.application.Application;
import org.iviPro.application.ApplicationListener;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.IMediaObject;
import org.iviPro.model.Project;

/**
 * <b>Media Information System:</b><br>
 * Ueber diese Klasse kann man auf Metadaten und Inhalte der verschiedenen
 * Medien-Objekte im Projekt zugreifen. So koennen Frames gegrabbt werden, die
 * Laenge von Medien-Objekten ermittelt werden, usw usf.
 * 
 * @author juhoffma
 */
public class MediaMetaSystem {

	/**
	 * Instanz-Pointer fuer Singleton-Pattern.
	 */
	private static MediaMetaSystem instance = null;
	
	private static ExecutorService exe;

	/**
	 * Konstruktor, der das MediaInfoSystem erstellt und initialisiert. Danach
	 * kann es die Arbeit aufnehmen.
	 */
	private MediaMetaSystem() {

		// Registrieren als Application-Listener
		// => Um Projekt oeffnen/schliessen mitzukriegen
		Application.getDefault().addApplicationListener(
				new ApplicationListener() {

					@Override
					public void onProjectClosed(Project project) {
						MediaMetaSystem.this.onProjectClosed(project);
					}

					@Override
					public void onProjectOpened(Project project) {
						MediaMetaSystem.this.onProjectOpened(project);
					}
				});
		exe = Executors.newCachedThreadPool();
	}

	/**
	 * Gibt die aktuelle Instanz des MediaInfoSystems zurueck.
	 * 
	 * @return Instanz des MediaInfoSystems.
	 */
	public static synchronized MediaMetaSystem getInstance() {
		if (instance == null) {
			instance = new MediaMetaSystem();
		}
		return instance;
	}

	/**
	 * Wird aufgerufen wenn ein Projekt geoeffnet wurde. Dann laden wir alle
	 * MediaObjects des Projekts auch im MIS
	 * 
	 * @param project
	 */
	private void onProjectOpened(Project project) {
		// Wenn ein Projekt geoeffnet wird, erstellen wir fuer alle MediaObjekte
		// einen MediaInformationThread
		Iterator<IAbstractBean> it = project.getMediaObjects().iterator();
		while (it.hasNext()) {
			onMediaLoaded(it.next());
		}
		// Registrieren als Listener, um mitzukriegen ob ein MedienObjekt
		// dem Projekt hinzugefuegt oder entfernt wird.
		BeanList<IAbstractBean> mediaObjList = project.getMediaObjects();
		mediaObjList.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getNewValue() instanceof IMediaObject) {
					IMediaObject mediaObj = (IMediaObject) evt.getNewValue();
					if (evt.getPropertyName().equals(BeanList.PROP_ITEM_ADDED)) {
						onMediaLoaded(mediaObj);
					} else if (evt.getPropertyName().equals(BeanList.PROP_ITEM_REMOVED)) {
						onMediaRemoved(mediaObj);
					}
				}
			}
		});
	}
	
	private void onProjectClosed(Project project) {
		
	}

	/**
	 * Wird aufgerufen, wenn ein MediaObject zum Projekt hinzugefuegt wurde.
	 * Starte einen Meta Thread für jedes Objekt
	 * 
	 * @param mediaObj
	 */
	private void onMediaLoaded(IAbstractBean mediaObj) {
		if (mediaObj instanceof IMediaObject) {
			MediaMetaThread miThread = new MediaMetaThread((IMediaObject) mediaObj);
			exe.execute(miThread);
		}
	}

	/**
	 * Wird aufgerufen, wenn ein MediaObject aus dem Projekt entfernt wurde.
	 * Dann entfernen wir es auch aus dem MIS.
	 * 
	 * @param mediaObj
	 */
	private void onMediaRemoved(IMediaObject mediaObj) {
	}
}

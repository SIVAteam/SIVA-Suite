package org.iviPro.views.mediarepository;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.PdfDocument;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Subtitle;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Icons;

/**
 * Diese Klasse wird benötigt, um die Anzeige des Treeviewers für die View
 * org.iviPro.views.MediaRepository zu ermöglichen.
 * 
 * @author Florian Stegmaier
 */
public class MediaAdapterFactory implements IAdapterFactory {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MediaAdapterFactory.class);
	
	public MediaAdapterFactory() {
	}

	/**
	 * Definiert die Anzeige der Elemente vom Typ
	 * org.iviPro.internObjects.MediaTypeGroup
	 */
	private IWorkbenchAdapter mediaTypeAdapter = new IWorkbenchAdapter() {

		@Override
		public Object getParent(Object o) {
			return ((MediaTreeGroup) o).getParent();
		}

		@Override
		public String getLabel(Object o) {
			return ((MediaTreeGroup) o).getName();
		}

		@Override
		public ImageDescriptor getImageDescriptor(Object o) {
			return getIcon(o);
		}

		@Override
		public Object[] getChildren(Object o) {
			return ((MediaTreeGroup) o).getEntries().toArray();
		}
	};

	/**
	 * Definiert die Anzeige der Elemente vom Typ
	 * org.iviPro.internObjects.MediaLeaf
	 */
	private IWorkbenchAdapter mediaAdapter = new IWorkbenchAdapter() {

		@Override
		public Object[] getChildren(Object o) {
			return new Object[0];
		}

		@Override
		public ImageDescriptor getImageDescriptor(Object object) {
			return getIcon(object);
		}

		@Override
		public String getLabel(Object o) {
			return ((MediaTreeLeaf) o).getName();
		}

		@Override
		public Object getParent(Object o) {
			return ((MediaTreeLeaf) o).getParent();
		}

	};

	/**
	 * Holt den entsprechenden Adapter.
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {

		if (adapterType == IWorkbenchAdapter.class
				&& adaptableObject instanceof MediaTreeGroup) {
			return mediaTypeAdapter;
		}

		if (adapterType == IWorkbenchAdapter.class
				&& adaptableObject instanceof MediaTreeLeaf) {
			return mediaAdapter;
		}

		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class };
	}

	/**
	 * Setzt die richtigen Icons für die Ansicht, abhängig von den Gruppen/Media Typen
	 * 
	 * @param o
	 *            entweder Typ MediaLeaf oder MediaTypeGroup
	 * @return
	 */
	private ImageDescriptor getIcon(Object o) {

		if (o instanceof MediaTreeGroup) {
			return ((MediaTreeGroup) o).getIcon().getImageDescriptor();
		} else if (o instanceof MediaTreeLeaf) {

			IAbstractBean mediaObject = ((MediaTreeLeaf) o).getMediaObject();
			if (mediaObject instanceof Audio) {
				return Icons.VIEW_MEDIAREPOSITORY_ITEM_AUDIO
						.getImageDescriptor();
			} else if (mediaObject instanceof AudioPart) {
				return Icons.VIEW_MEDIAREPOSITORY_ITEM_AUDIOPART
						.getImageDescriptor();
			} else if (mediaObject instanceof Picture) {
				Image img = ((Picture) mediaObject)
						.getThumbnail(Picture.THUMBNAIL_MED_REP);
				if (img != null) {
					return ImageDescriptor.createFromImage(((Picture) mediaObject)
						.getThumbnail(Picture.THUMBNAIL_MED_REP));
				} else {
					return Icons.VIEW_MEDIAREPOSITORY_ITEM_PICTURE
							.getImageDescriptor();
				}
			} else if (mediaObject instanceof PictureGallery) {
				return Icons.VIEW_MEDIAREPOSITORY_ITEM_PICTURE
						.getImageDescriptor();
			} else if (mediaObject instanceof Video) {
				return Icons.VIEW_MEDIAREPOSITORY_ITEM_VIDEO
						.getImageDescriptor();
			} else if (mediaObject instanceof RichText) {
				return Icons.VIEW_MEDIAREPOSITORY_ITEM_TEXT
						.getImageDescriptor();
			} else if (mediaObject instanceof Subtitle) {
				return Icons.VIEW_MEDIAREPOSITORY_ITEM_SUBTITLE
						.getImageDescriptor();
			} else if (mediaObject instanceof PdfDocument) {
				return Icons.VIEW_MEDIAREPOSITORY_ITEM_PDF
						.getImageDescriptor();
			}
		}
		// Wenn keins der obigen ist, dann stimmt was nicht
		// => Platzhalter-Bild zurueck geben
		return Icons.DEFAULT.getImageDescriptor();
	}
}

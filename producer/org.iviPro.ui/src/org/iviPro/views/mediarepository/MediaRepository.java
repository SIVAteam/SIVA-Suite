package org.iviPro.views.mediarepository;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.iviPro.actions.nondestructive.MediaViewAction;
import org.iviPro.actions.nondestructive.OpenEditorAction;
import org.iviPro.actions.undoable.MediaDeleteAction;
import org.iviPro.actions.undoable.MediaLoadAction;
import org.iviPro.actions.undoable.MediaRenameAction;
import org.iviPro.actions.undoable.SceneFromVideoAction;
import org.iviPro.application.Application;
import org.iviPro.dnd.TransferMedia;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.Project;
import org.iviPro.model.imageeditor.ImageObject;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.PdfDocument;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Subtitle;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Icons;
import org.iviPro.views.IAbstractRepositoryFilter;
import org.iviPro.views.IAbstractRepositoryView;

/**
 * View für das Medien-Repository.
 * 
 * @author Christian Dellwo
 */
public class MediaRepository extends IAbstractRepositoryView {

	private static Logger logger = Logger.getLogger(MediaRepository.class);
	public static final String ID = MediaRepository.class.getName();

	/** Unterstuetzte Transfer-Typen fuer ausgehende Drag-Events */
	public static final Transfer[] TRANSFER_DRAG_TYPE = new Transfer[] { TransferMedia
			.getInstance() };
	/** Unterstuetzte Transfer-Typen fuer eingehende Drop-Events */
	public static final Transfer[] TRANSFER_DROP_TYPE = new Transfer[] {
			FileTransfer.getInstance(), TransferMedia.getInstance() };

	// Die einzelnen Untergruppen im Baum
	private MediaTreeGroup groupVideo;
	private MediaTreeGroup groupAudio;
	private MediaTreeGroup groupPicture;
	private MediaTreeGroup groupText;
	private MediaTreeGroup groupSubtitles;
	private MediaTreeGroup groupPdf;
	

	/**
	 * Change-Listener der auf Aenderungen auf den Medien-Objekten hoert und
	 * ggfs den Tree updated, wenn sich z.B. der Name eines Objekts geaendert
	 * hat oder wenn ein Medien-Objekt hinzugefuegt oder geloescht wird.
	 */
	private final PropertyChangeListener changeListener;

	// Previewer wenn man über ein Item geht
	private PreviewPopup previewPopup;

	/**
	 * Erstellt einen neuen Media-Repository View
	 */
	public MediaRepository() {
		// Listener initalisieren
		changeListener = initChangeListener();
		previewPopup = new PreviewPopup();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.views.IAbstractRepositoryView#registerTreeItemsAdapter()
	 */
	@Override
	protected void registerTreeItemsAdapter() {
		Platform.getAdapterManager().registerAdapters(
				new MediaAdapterFactory(), MediaTreeNode.class);
	}

	/*
	 * (non-Javadoc) zusätzlich zu den Context Actions werden auch die Actions
	 * für die Toolbar gesetzt!
	 * 
	 * @see
	 * org.iviPro.views.IAbstractRepositoryView#createContextMenu(org.eclipse
	 * .jface.viewers.TreeViewer)
	 */
	@Override
	protected MenuManager createContextMenu(TreeViewer treeViewer) {
		IWorkbenchWindow window = this.getSite().getWorkbenchWindow();

		MediaViewAction viewAction = new MediaViewAction(window);
		MediaRenameAction renameAction = new MediaRenameAction(window,
				treeViewer);
		OpenEditorAction openSceneAction = new OpenEditorAction(window);
		SceneFromVideoAction sfvAction = new SceneFromVideoAction(window);
//		SceneDetectionAction sdAction = new SceneDetectionAction(window);
//		SceneDetectionOptionEditorAction nsdAction = new SceneDetectionOptionEditorAction(
//				window);
		MediaLoadAction mlAction = new MediaLoadAction(window);
		final MediaDeleteAction mdAction = new MediaDeleteAction(window);
//		NewFolderAction newFolderAction = new NewFolderAction(window,
//				treeViewer, this);
//		FolderRenameAction folderRenameAction = new FolderRenameAction(window,
//				treeViewer, this);

		MenuManager menuManager = new MenuManager();
		menuManager.add(viewAction);
		menuManager.add(renameAction);
		menuManager.add(new Separator());
		menuManager.add(openSceneAction);
		menuManager.add(sfvAction);
//		menuManager.add(sdAction);
//		menuManager.add(nsdAction);
		menuManager.add(new Separator());
		menuManager.add(mlAction);
		menuManager.add(mdAction);
		menuManager.add(new Separator());
//		menuManager.add(newFolderAction);
//		menuManager.add(folderRenameAction);

		treeViewer.getTree().addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					logger.info("DEL pressed"); //$NON-NLS-1$
					mdAction.run();
				}
			}
		});

		// die Actions für die Toolbar
		toolBarManager.add(mlAction);
		toolBarManager.add(mdAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(viewAction);
		toolBarManager.add(renameAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(openSceneAction);
		toolBarManager.add(sfvAction);
//		toolBarManager.add(sdAction);
//		toolBarManager.add(new Separator());
//		toolBarManager.add(newFolderAction);
		toolBarManager.update(true);
		return menuManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.views.IAbstractRepositoryView#createRepositoryFilter()
	 */
	@Override
	protected IAbstractRepositoryFilter createRepositoryFilter() {
		return new MediaRepositoryFilter();
	}

	/**
	 * Wird vom ChangeListener aufgerufen wenn ein Media-Objekt zum Projekt
	 * hinzugefuegt wird. Fuegt dann dieses Media-Objekt in den entsprechenden
	 * Unterbaum im TreeView ein.
	 * 
	 * @param mediaObj
	 *            Das Media-Objekt das im Tree hinzugefuegt werden soll.
	 * @throws IllegalArgumentException if the given object has an unknown media type
	 */
	private void onMediaAdded(IAbstractBean mediaObj) {
		mediaObj.addPropertyChangeListener(changeListener);

		if (mediaObj instanceof Audio) {
			BeanList<AudioPart> audioParts = ((Audio) mediaObj).getAudioParts(); 
			audioParts.addPropertyChangeListener(changeListener);
			MediaTreeLeaf ml = new MediaTreeLeaf(groupAudio, mediaObj);
			groupAudio.addElement(ml);
			for (AudioPart audioPart : audioParts) {
				groupAudio.addElement(new MediaTreeLeaf(groupAudio, audioPart));
			}
		} else if (mediaObj instanceof AudioPart) {
			MediaTreeLeaf ml = new MediaTreeLeaf(groupAudio, mediaObj);
			groupAudio.addElement(ml);
		} else if (mediaObj instanceof Video) {
			MediaTreeLeaf ml = new MediaTreeLeaf(groupVideo, mediaObj);
			groupVideo.addElement(ml);
		} else if (mediaObj instanceof Picture
				|| mediaObj instanceof PictureGallery) {
			MediaTreeLeaf ml = new MediaTreeLeaf(groupPicture, mediaObj);
			groupPicture.addElement(ml);
		} else if (mediaObj instanceof Subtitle) {
			MediaTreeLeaf ml = new MediaTreeLeaf(groupSubtitles, mediaObj);
			groupSubtitles.addElement(ml);
		} else if (mediaObj instanceof RichText) {
			MediaTreeLeaf ml = new MediaTreeLeaf(groupText, mediaObj);
			groupText.addElement(ml);
		} else if (mediaObj instanceof PdfDocument) {
			MediaTreeLeaf ml = new MediaTreeLeaf(groupPdf, mediaObj);
			groupPdf.addElement(ml);
		} else {
			throw new IllegalArgumentException("Tried to add an unknown media type to the repository view."); //$NON-NLS-1$
		}
		getTreeViewer().expandAll();
	}

	/**
	 * Wird vom ChangeListener aufgerufen wenn ein Media-Objekt vom Projekt
	 * entfernt wird. Entfernt ein Medienobjekt aus den betreffenden
	 * Programmobjekten.
	 * 
	 * @param mediaObj
	 *            Das Media-Objekt das aus dem Tree entfernt werden soll.
	 */
	private void onMediaRemoved(IAbstractBean mediaObj) {
		mediaObj.removePropertyChangeListener(changeListener);
		
		// Bei einigen Medien müssen mehrere, mit dem Medium in Verbindung
		// stehende Einträge entfernt werden. Daher anlegen eines Sets. 
		HashSet<IAbstractBean> mediaToRemove = new HashSet<IAbstractBean>();
		mediaToRemove.add(mediaObj);
		
		if (mediaObj instanceof Audio) {
			BeanList<AudioPart> audioParts = ((Audio) mediaObj).getAudioParts(); 
			audioParts.removePropertyChangeListener(
					changeListener);
			mediaToRemove.addAll(audioParts);
		}

		MediaTreeGroup root = (MediaTreeGroup) getTreeRoot();
		List<Object> groups = root.getEntries();
		
		for (Object obj : groups) {
			MediaTreeGroup mtg = (MediaTreeGroup) obj;

			ListIterator<Object> entries = mtg.getEntries().listIterator();
			while (entries.hasNext()) {
				MediaTreeLeaf ml = (MediaTreeLeaf) entries.next();
				if (mediaToRemove.contains(ml.getMediaObject())) {
					entries.remove();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.views.IAbstractRepositoryView#onDoubleClick(java.lang.Object)
	 */
	@Override
	protected void onDoubleClick(Object selectedElement) {
		if (selectedElement instanceof MediaTreeLeaf) {
			MediaTreeLeaf leaf = (MediaTreeLeaf) selectedElement;
			IAbstractBean mediaObject = leaf.getMediaObject();
			new MediaViewAction(MediaRepository.this.getSite()
					.getWorkbenchWindow(), mediaObject).run();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.iviPro.views.IAbstractRepositoryView#onDrop(org.eclipse.swt.dnd.
	 * DropTargetEvent)
	 */
	@Override
	protected void onDrop(DropTargetEvent event) {
		if (Application.getCurrentProject() == null) {
			return;
		}
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		if (fileTransfer.isSupportedType(event.currentDataType)) {
			String[] files = (String[]) event.data;
			MediaLoadAction mediaLoadAction = new MediaLoadAction(getSite()
					.getWorkbenchWindow(), files);
			mediaLoadAction.run();
		}

		String transferKey = event.data.toString();
		Object[] objects = Application.getDragDropManager().endTransfer(
				transferKey);
		if (objects != null && objects.length > 0) {
			if (objects[0] instanceof IAbstractBean) {
				TreeItem targetFolder = (TreeItem) event.item;
				if (targetFolder != null 
						&& targetFolder.getData() instanceof MediaTreeGroup) {
					TreeItem sourceItem = getTreeViewer().getTree()
							.getSelection()[0];

					MediaTreeGroup targetGroup = (MediaTreeGroup) targetFolder
							.getData();
					List<Object> targetChilds = targetGroup.getEntries();
					MediaTreeGroup sourceGroup = ((MediaTreeLeaf) sourceItem
							.getData()).getParent();
					List<Object> sourceChilds = sourceGroup.getEntries();

					MediaTreeGroup sourceTest = sourceGroup;
					MediaTreeGroup targetTest = targetGroup;
					// test ob gleiche Oberkategorie
					while (!(sourceTest == groupVideo
							||sourceTest == groupAudio
							|| sourceTest == groupPicture
							|| sourceTest == groupSubtitles
							|| sourceTest == groupText 
							|| targetTest == groupPdf)) {
						sourceTest = sourceTest.getParent();
					}
					while (!(targetTest == groupVideo
							|| targetTest == groupAudio
							|| targetTest == groupPicture
							|| targetTest == groupSubtitles
							|| targetTest == groupText 
							|| targetTest == groupPdf)) {
						targetTest = targetTest.getParent();
					}

					if (sourceTest == targetTest) {
						targetChilds.add(sourceItem.getData());
						sourceChilds.remove(sourceItem.getData());
						((MediaTreeLeaf) sourceItem.getData())
								.setParent(targetGroup);
						treeViewer.expandToLevel(targetGroup, 1);
						updateTreeviewer();
					}
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.views.IAbstractRepositoryView#onProjectOpened(org.iviPro.model
	 * .Project)
	 */
	@Override
	protected void onProjectOpened(Project project) {
		if (project != null) {
			project.getMediaObjects().addPropertyChangeListener(changeListener);
			logger.debug("Registering at " + project + " as change " //$NON-NLS-1$ //$NON-NLS-2$
					+ "listener"); //$NON-NLS-1$
			List<IAbstractBean> media = project.getMediaObjects();
			for (IAbstractBean medium : media) {
				onMediaAdded(medium);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.views.IAbstractRepositoryView#onProjectClosed(org.iviPro.model
	 * .Project)
	 */
	@Override
	protected void onProjectClosed(Project project) {
		logger.info("Project closed."); //$NON-NLS-1$
		project.removePropertyChangeListener(changeListener);
		logger.debug("Deregistering from " + project + " as change listener"); //$NON-NLS-1$ //$NON-NLS-2$		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.views.IAbstractRepositoryView#getDragTransferTypes()
	 */
	@Override
	protected Transfer[] getDragTransferTypes() {
		return TRANSFER_DRAG_TYPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.views.IAbstractRepositoryView#getDropTransferTypes()
	 */
	@Override
	protected Transfer[] getDropTransferTypes() {
		return TRANSFER_DROP_TYPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.views.IAbstractRepositoryView#getObjectsToTransfer(org.eclipse
	 * .swt.widgets.TreeItem[])
	 */
	@Override
	protected IAbstractBean[] getObjectsToTransfer(TreeItem[] selItems) {
		List<IAbstractBean> selMediaObjects = new ArrayList<IAbstractBean>(
				selItems.length);
		for (int i = 0; i < selItems.length; i++) {
			Object selItem = selItems[i].getData();
			if (selItem instanceof MediaTreeLeaf) {
				IAbstractBean mediaObject = ((MediaTreeLeaf) selItem)
						.getMediaObject();
				selMediaObjects.add(mediaObject);
			}
		}
		IAbstractBean[] transferObjects = new IAbstractBean[selMediaObjects
				.size()];
		transferObjects = selMediaObjects.toArray(transferObjects);
		return transferObjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.views.IAbstractRepositoryView#getDefaultImage()
	 */
	@Override
	protected Image getDefaultImage() {
		return Icons.VIEW_MEDIAREPOSITORY.getImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.iviPro.views.IAbstractRepositoryView#isDragAllowed(java.lang.Object)
	 */
	@Override
	protected boolean isDragAllowed(Object selItem) {
		return selItem instanceof MediaTreeLeaf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.iviPro.views.IAbstractRepositoryView#initTreeRoot()
	 */
	@Override
	protected Object initTreeRoot() {
		MediaTreeGroup root = new MediaTreeGroup(null, "root", Icons.DEFAULT); //$NON-NLS-1$
		groupAudio = new MediaTreeGroup(root, Messages.MediaTypes_Audio,
				Icons.VIEW_MEDIAREPOSITORY_GROUP_AUDIO);
		groupText = new MediaTreeGroup(root, Messages.MediaTypes_Text,
				Icons.VIEW_MEDIAREPOSITORY_GROUP_TEXT);
		groupPicture = new MediaTreeGroup(root, Messages.MediaTypes_Picture,
				Icons.VIEW_MEDIAREPOSITORY_GROUP_PICTURE);
		groupVideo = new MediaTreeGroup(root, Messages.MediaTypes_Video,
				Icons.VIEW_MEDIAREPOSITORY_GROUP_VIDEO);
		groupSubtitles = new MediaTreeGroup(root, Messages.MediaTypes_Subtitle,
				Icons.VIEW_MEDIAREPOSITORY_GROUP_SUBTITLE);
		groupPdf = new MediaTreeGroup(root, Messages.MediaTypes_Pdf, 
				Icons.VIEW_MEDIAREPOSITORY_GROUP_PDF);
		root.addElement(groupAudio);
		root.addElement(groupText);
		root.addElement(groupPicture);
		root.addElement(groupVideo);
		root.addElement(groupSubtitles);
		root.addElement(groupPdf);
		return root;
	}

	/**
	 * Initialisiert den Listener, der dazu verwendet wird, Aenderungen an den
	 * Medien im Treeview zu detektieren, z.B. die Aenderung des Namens eines
	 * Medien-Objekts oder das Entfernen/Loeschen eines Medien-Objekts aus dem
	 * Projekt. Dies dient dazu, um diese Aenderung im Treeview sofort
	 * widerspiegeln zu koennen.
	 * 
	 * @return Listener der auf den Medien-Objekten registriert werden soll, um
	 *         deren Aenderungen mitbekommen zu koennen.
	 */
	private PropertyChangeListener initChangeListener() {
		PropertyChangeListener listener = new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				Object newValue = e.getNewValue();

				if (e.getPropertyName() != null) {
					if (e.getPropertyName().equals(BeanList.PROP_TITLE)) {
						updateTreeviewer();
					}
				}

				if (newValue instanceof IAbstractBean) {
					IAbstractBean mediaObj = (IAbstractBean) newValue;
					if (e.getPropertyName().equals(BeanList.PROP_ITEM_ADDED)) {
						onMediaAdded(mediaObj);
						updateTreeviewer();
					} else if (e.getPropertyName().equals(
							BeanList.PROP_ITEM_REMOVED)) {
						onMediaRemoved(mediaObj);
						updateTreeviewer();
					}
				}
			}
		};
		return listener;
	}

	@Override
	protected void handleMouseMove(Event e) {
		TreeItem item = treeViewer.getTree().getItem(new Point(e.x, e.y));
		if (item != null) {
			Object itemData = item.getData();
			if (itemData instanceof MediaTreeLeaf) {
				MediaTreeLeaf leaf = (MediaTreeLeaf) itemData;
				IAbstractBean mObject = leaf.getMediaObject();
				if (mObject instanceof Picture) {
					Picture picture = (Picture) mObject;
					Rectangle r = item.getBounds();
					double fac = 1.5;
					Point p = new Point(r.x - (int) fac * r.width, r.y
							+ r.height);
					p = treeViewer.getTree().toDisplay(p);
					previewPopup.show(picture, p);
				}
			} else {
				previewPopup.hide();
			}
		} else {
			previewPopup.hide();
		}
	}

	public IAbstractBean getCurrentSelection() {
		TreeSelection selection = (TreeSelection) (getTreeViewer()
				.getSelection());
		if (selection.isEmpty()) {
			return null;
		} else {
			if (selection.getFirstElement() instanceof MediaTreeLeaf) {
				return ((MediaTreeLeaf) selection.getFirstElement())
						.getMediaObject();
			}
		}
		return null;
	}

	public void addSelectionListener(ISelectionChangedListener listener) {
		getTreeViewer().addSelectionChangedListener(listener);
	}

	/**
	 * Selects a bean in the media repository.
	 * 
	 * @param bean
	 *            The bean that should be selected or null if no bean should be
	 *            selected.
	 */
	public void selectBean(IAbstractBean bean) {
		if (bean == null) {
			logger.debug("Unselecting all scenes."); //$NON-NLS-1$
			getTreeViewer().setSelection(TreeSelection.EMPTY);
		} else {
			MediaTreeGroup root = (MediaTreeGroup) this.getTreeRoot();
			for (Object o : root.getEntries()) {
				MediaTreeGroup mtg = (MediaTreeGroup) o;

				for (Object obj2 : mtg.getEntries()) {
					MediaTreeLeaf ml = (MediaTreeLeaf) obj2;
					if (ml.getMediaObject().getTitle().equals(bean.getTitle())) {
						getTreeViewer().setSelection(
								new StructuredSelection(ml));
						break;
					}
				}
			}
		}
	}

	private class PreviewPopup {
		private Shell shell;
		private Picture picture;
		private Image image;
		private int shellWidth = 150;
		private int shellHeight;
		private String mediaName;

		public PreviewPopup() {
			shell = new Shell(Display.getCurrent(), SWT.NO_TRIM | SWT.ON_TOP);
			shell.addPaintListener(new PaintListener() {
				@Override
				public void paintControl(PaintEvent pe) {
					if (image != null) {
						pe.gc.drawImage(image, 0, 0);
						for (ImageObject imgObj : picture.getObjects()) {
							org.iviPro.editors.imageeditor.ImageEditWidget
									.drawObject(pe.gc, imgObj, image);
						}
						pe.gc.dispose();
					}
				}
			});
			shell.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent arg0) {
					if (image != null) {
						image.dispose();
					}
				}
			});
		}

		public void show(Picture picture, Point p) {
			this.picture = picture;
			if (treeViewer.getTree().isDisposed()) {
				return;
			}
			if (picture.getTitle().equals(mediaName)) {
				return;
			}
			this.image = picture.getThumbnail(Picture.THUMBNAIL_MED_POP);
			if (this.image != null) {
				shellWidth = image.getBounds().width;
				shellHeight = image.getBounds().height;
			} else {
				return;
			}
			shell.setSize(shellWidth, shellHeight);
			shell.setLocation(p.x - shellWidth / 2, p.y);
			shell.redraw();
			shell.setVisible(true);
		}

		public void hide() {
			shell.setVisible(false);
		}
	}

	@Override
	protected void handleMouseExit(Event arg0) {
		previewPopup.hide();
	}
}

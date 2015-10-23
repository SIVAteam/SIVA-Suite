package org.iviPro.editors.annotationeditor.components.contenteditors;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.application.Application;
import org.iviPro.dnd.TransferMedia;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.resources.Picture;
import org.iviPro.theme.Colors;

/**
 * Editor zum hinzufügen eines Bilders und zum Editieren der Bildergalerie
 * @author juhoffma
 */
public class PictureEditor extends ContentEditor {
		
	// die Bildergallerie
	private PictureGallery gallery;
	
	// die Bilder die angzeigt werden
	// enthält ein Einzelbild oder die Bilder der Gallery
	private ArrayList<Picture> pictures;

	// Main Composite hält alle weiteren
	private ScrolledComposite mainComposite;
	
	// das Content Composite hält die einzelnen Picture Composites
	private Composite contentComposite;
	
	// der eingestellte ContentType
	private int picAnnoContentType = -1;
	
	// selektierte Bilder
	private ArrayList<Integer> selectedPictures;
	
	// aktuell gedrückte Taste, für die Selektion
	private int pressedKeyCode;
	// zuletzt gewählte Selektion
	private int lastSelectedIndex;
	
	// für Drag and Drop falls eine Galerie hinzufügt wird
	private final int GALLERY_APPEND = 0;
	private final int GALLERY_REPLACE = 1;
	private int dropGalleryParam = -1;
		
	public PictureEditor(Composite parent, int style, final PictureGallery gallery) {
		super(parent, style);
		init();	
		setPicAnnoContentType(NodeAnnotationPicture.CONTENT_PICTUREGALLERY);
		setContent(gallery);
	}

	public PictureEditor(Composite parent, int style, final Picture picture) {
		super(parent, style);
		init();
		setPicAnnoContentType(NodeAnnotationPicture.CONTENT_PICTURE);
		setContent(picture);
	}
	
	private void init() {
		this.pictures = new ArrayList<Picture>();
		this.selectedPictures = new ArrayList<Integer>();		
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(new GridLayout(1, false));
		mainComposite = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(new GridLayout(1, false));
		contentComposite = new Composite(mainComposite, SWT.CENTER);
		contentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setContent(contentComposite);
	}
	
	/**
	 * berechnet die Größe des ContentComposite für das ScrolledComposite
	 */
	private void adjustScrolledComposite() {
		if (picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
			int pics = this.pictures.size()+1;
			int columns = 1; 
			if (this.gallery != null) {
				this.gallery.getNumberColumns();
			}
			int facX = pics > columns ? columns : pics;
			int facY = pics / columns + 1;			
			int width = facX * 120;
			int height = facY * 120;
			contentComposite.setSize(width, height);
			mainComposite.setMinSize(width, height);
			mainComposite.setExpandHorizontal(true);
			mainComposite.setExpandVertical(true);		
		} else {
			if (contentComposite.getChildren().length == 0) {
				contentComposite.setSize(300, 300);				
			} else {				
				Control c = contentComposite.getChildren()[0];
				int width = c.getBounds().width;
				int height = c.getBounds().height;
				if (width == 0 || height == 0) {
					width = 300;
					height = 300;
				}
				contentComposite.setSize(width, height);
				mainComposite.setMinSize(width, height);
				mainComposite.setExpandHorizontal(true);
				mainComposite.setExpandVertical(true);				
			}
		}		
		contentComposite.layout(true);
		mainComposite.layout(true);
	}
		
	/**
	 * gibt den Content Type Bild oder Galerie zurück
	 * @return
	 */
	public int getPicAnnoContentType() {
		return this.picAnnoContentType;
	}
	
	public void setPicAnnoContentType(int contentType) {
		this.picAnnoContentType = contentType;
	}
	
	/**
	 * setzt den PictureEditor mit einem neuen Bild in den Bildannotationsmodus
	 * @param picture
	 */
	private void setPicture(Picture picture) {	
		if (contentComposite.isDisposed() || mainComposite.isDisposed()) {
			return;
		}
		if (this.gallery != null) {
			this.gallery = null;
		}
		this.pictures.clear();
		
		if (picture != null) {
			this.pictures.add(picture);
		}
		contentComposite.setLayout(new GridLayout(1, false));
							
		this.contentComposite.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				pressedKeyCode = arg0.keyCode;				
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				pressedKeyCode = -1;
			}
		});
		initPictures();		
	}	

	/**
	 * setzt den PictureEditor mit einer neuen Galerie in den Bildgaleriemodus
	 * @param gallery
	 */
	private void setPictureGallery(final PictureGallery newGal) {	
		
		if (contentComposite.isDisposed() || mainComposite.isDisposed()) {
			return;
		}
		
		// erstelle eine Kopie der Galerie damit nicht das Orginalobjekt in der Annotation 
		// bearbeitet wird
		if (newGal != null) {
			this.gallery = new PictureGallery(newGal);
		} else {
			this.gallery = new PictureGallery("", Application.getCurrentProject());
		}
		this.pictures = gallery.getPictures();
		contentComposite.setLayout(new GridLayout(gallery.getNumberColumns(), false));
		initPictures();
	}
	
	/**
	 * erstelle ein Delete Menü
	 * @return
	 */
	public Menu createDeleteMenu() {
		if (picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
			// das Popup-Menü zum Löschen von Objekten
			Menu deleteMenu = new Menu(contentComposite);
			// Menü zum Löschen von Bildern
			deleteMenu = new Menu(contentComposite);	
			final MenuItem deleteItem = new MenuItem(deleteMenu, SWT.CASCADE);
			deleteItem.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {				
				}
				@Override
				public void widgetSelected(SelectionEvent se) {
					removeSelectedPictures();   
				}	    	
			});
			deleteMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuHidden(MenuEvent arg0) {
					
				}

				@Override
				public void menuShown(MenuEvent arg0) {
					if (selectedPictures.size() == 0) {
						deleteItem.setEnabled(false);
						deleteItem.setText(Messages.PictureEditor_SelectElement_None);
					} else
					if (selectedPictures.size() == 1) {
						deleteItem.setEnabled(true);
						deleteItem.setText(Messages.PictureEditor_Delete_Picture);
					} else
					if (selectedPictures.size() > 1) {
						deleteItem.setEnabled(true);
						deleteItem.setText(Messages.PictureEditor_Delete_AllPictures);
					}
				}
			});
			contentComposite.setMenu(deleteMenu);
			return deleteMenu;
		}	
		return null;
	}
	
	/**
	 * initialisiert für alle Bilder im Picture-Array pictures ein PictureComposite
	 * @param reset falls reset true ist, werden alle bereits vorhandene PictureComposites entfernt
	 * 	            ansonsten werden diese wieder verwendet
	 */
	private void initPictures() {
		// entferne alle PictureComposites
		if (this.contentComposite != null){
			for (Control con : this.contentComposite.getChildren()){
				con.dispose();
			}
		}
		
		for (int i = 0; i < pictures.size(); i++) {	
			PictureComposite newComp = new PictureComposite(this.contentComposite, SWT.CENTER, pictures.get(i), i);
			createDropTarget(newComp);
			createDragSource(newComp);			
		}
		// füge einen Platzhalter ein, bei einer normalen Bildannotation wird 
		// nur einer hinzugefügt falls noch kein Bild gesetzt wurde
		if (this.picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTURE) {
			if (pictures.size() == 0) {
				addPlaceHolder();
			}
		} else
		// füge bei einer Galerie auf jeden Fall einen Platzhalter für ein neues Bild ein
		if (this.picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
			addPlaceHolder();
		}
		adjustScrolledComposite();
	}	
	
	/**
	 * entfernt alle selektierten Bilder
	 */
	private void removeSelectedPictures() {			
		// sortiere die Indexe aufsteigen
		Collections.sort(this.selectedPictures);
		
		// korrigiere die Indizes, da beim Löschen Objekte aus der Liste entfernt 
		// werden passt der Index nicht mehr, deshalb wird jeder Index um so viele Stelle reduziert
		// wie vor diesem Index Objekte entfernt wurden
		ArrayList<Integer> correctedDeleteIndex = new ArrayList<Integer>();
		for (int i = 0; i < this.selectedPictures.size(); i++) {
			correctedDeleteIndex.add(this.selectedPictures.get(i)-i);
		}
		for (Integer deleteIndex : correctedDeleteIndex) {
			this.pictures.remove((int) deleteIndex);
		}
		if (this.picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
			SivaEvent edEvent = new SivaEvent(PictureEditor.this, SivaEventType.CONTENT_CHANGED, this.gallery);
			notifySivaEventConsumers(edEvent);
		}
		this.selectedPictures.clear();		
		initPictures();
	}
	
	/**
	 * fügt einen Platzhalter für ein neues Bild ein
	 */
	private void addPlaceHolder() {
		if (this.contentComposite != null && this.pictures != null) {
			createDropTarget(new PictureComposite(this.contentComposite, SWT.CENTER, null, this.pictures.size()));
			this.contentComposite.layout(true);
		}
	}
	
	/**
	 * handled drag-Objekte
	 * @param target
	 * @param objects
	 */
	private void handleDrop(PictureComposite target, Object[] objects) {
		// hebe alle Selektionen auf
		this.selectedPictures.clear();
		
		/**
		 * bei einer BildAnnotation wird nur ein Bild akzeptiert und das erste
		 * Bild der Selektion verwendet
		 */
		if (picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTURE) {
			// ein Bild wird herein gezogen bei einer Bildannotation
			if (objects[0] instanceof Picture) {
				Picture newPic = (Picture) objects[0];
				Picture curPic = pictures.size() > 0 ? pictures.get(0) : null;
				if (!newPic.equals(curPic)) {
					pictures.clear();
					pictures.add(newPic);
					target.setPicture(newPic);
					SivaEvent edEvent = new SivaEvent(PictureEditor.this, SivaEventType.CONTENT_CHANGED, newPic);
					notifySivaEventConsumers(edEvent);
				}
			}
		}					
		/**
		 * bei einer Bildgallerie können sowohl Bildergallerien und auch Bilder hineingezogen werden
		 */
		if (picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
			// prüfe ob das DropTarget für ein neues Bild verwendet wurde
			int targetIndex = target.getIndex();
			boolean placeHolderUsed = (targetIndex == pictures.size());
			// wenn der Platzhalter verwendet wurde, adde alle Bilder am Ende der Galerie
			if (placeHolderUsed) {
				boolean refreshGallery = false;
				for (Object o : objects) {					
					if (o instanceof Picture) {
						Picture pic = (Picture) o;
						pictures.add(pic);		
						refreshGallery = true;
					} else
					if (o instanceof PictureGallery) {
						handleDropPictureGallery((PictureGallery) o);
						refreshGallery = true;
					} else
					if (o instanceof Integer) {
						Control c = this.contentComposite.getChildren()[(Integer) o];
						c.redraw();
						c.update();																	
					}
				}
				if (refreshGallery) {
					initPictures();
				}
			} 
			// falls kein Platzhalter verwendet wurde, werden falls mehrere Bilder eingefügt werden diese an dieser Stelle 
			// eingefügt, falls nur eins eingefügt wird, wird das Bild ersetzt
			else {
				// handle Bilder
				if (objects.length == 1) {
					Object o = objects[0];
					if (o instanceof Picture) {
						pictures.set(targetIndex, (Picture) o);
						target.setPicture((Picture) o);
					} else
					if (o instanceof PictureGallery) {
						handleDropPictureGallery((PictureGallery) o);
						initPictures();
					} else 
					if (o instanceof Integer) {
						Integer sourceIndex = (Integer) o;
						Picture sourcePic = pictures.get(sourceIndex);
						Picture targetPic = pictures.get(targetIndex);
						pictures.set(sourceIndex, targetPic);
						pictures.set(targetIndex, sourcePic);
						target.setPicture(sourcePic);
						((PictureComposite) contentComposite.getChildren()[sourceIndex]).setPicture(targetPic);
					}					
				} else {
					for (int i=0; i < objects.length; i++) {
						Object o = objects[i];
						if (o instanceof Picture) {
							Picture pic = (Picture) o;
							pictures.add(targetIndex+i, pic);
						} else
						if (o instanceof PictureGallery) {
							handleDropPictureGallery((PictureGallery) o);	
						}
					}
					initPictures();
				}				
			}
			SivaEvent edEvent = new SivaEvent(PictureEditor.this,
					SivaEventType.CONTENT_CHANGED, gallery);
			notifySivaEventConsumers(edEvent);
		}	
		adjustScrolledComposite();
	}
	
	/**
	 * Methode die das dropen von Bildergalerien bearbeitet
	 * @param droppedGallery
	 */
	private void handleDropPictureGallery(PictureGallery droppedGallery) {
		
		// die Bilder einer gedropten Galerie werden entweder eingefügt oder
		// die gedropte Galerie ersetzt die andere
		// beim Ersetzen wird die gedropte Galerie weiter bearbeitet => auch andere Annotationen
		// welche die Galerie verwenden sind von Änderungen betroffen.
	    final Shell confirm = new Shell(Display.getCurrent(), SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
	    confirm.setText(Messages.PictureEditor_AddPicGal_Add);
	    confirm.setSize(350, 150);

	    final Button buttonReplace = new Button(confirm, SWT.PUSH);
	    buttonReplace.setText(Messages.PictureEditor_AddPicGal_Replace);
	    buttonReplace.setBounds(20, 55, 80, 25);
	    
	    final Button buttonAppend = new Button(confirm, SWT.PUSH);
	    buttonAppend.setText(Messages.PictureEditor_AddPicGal_Append);
	    buttonAppend.setBounds(120, 55, 80, 25);	    

	    Button buttonCancel = new Button(confirm, SWT.PUSH);
	    buttonCancel.setText(Messages.PictureEditor_AddPicGal_Cancel);
	    buttonCancel.setBounds(220, 55, 80, 25);

	    Label label = new Label(confirm, SWT.NONE);
	    label.setText(Messages.PictureEditor_AddPicGal_Text);
	    label.setBounds(20, 15, 100, 20);
		    
	    Listener listener = new Listener() {
	        public void handleEvent(Event event) {
	          if (event.widget == buttonReplace) {
	        	  dropGalleryParam = GALLERY_REPLACE;
	          } else 
	          if (event.widget == buttonAppend) {
	        	  dropGalleryParam = GALLERY_APPEND;
	          } else {
	        	  dropGalleryParam = -1;
	          } 
	          confirm.close();
	        }
	    };
	    
	    buttonReplace.addListener(SWT.Selection, listener);
	    buttonAppend.addListener(SWT.Selection, listener);
	    buttonCancel.addListener(SWT.Selection, listener);
	    
	    confirm.open();
	    
	    while (!confirm.isDisposed()) {
	        if (!Display.getCurrent().readAndDispatch())
	        	Display.getCurrent().sleep();
	      }
	    
	    switch (dropGalleryParam) {
	    	case GALLERY_APPEND:
				for (Picture pic : droppedGallery.getPictures()){
					this.pictures.add(pic);
				}
				break;
	    	case GALLERY_REPLACE:
	    		setPictureGallery(droppedGallery);
	    		// Send event to store original replacement gallery
	    		SivaEvent event = new SivaEvent(PictureEditor.this,
	    				SivaEventType.CONTENT_REPLACED, droppedGallery);
	    		notifySivaEventConsumers(event);	    		
	    		break;
	    }
	}
	
	/**
	 * Drag and Drop Unterstützung
	 * @param dropTarget
	 */
	private void createDropTarget(final Composite dropTarget) {		
		if (dropTarget.isDisposed()) {
			return;
		}		
		DropTarget target = new DropTarget(dropTarget, DND.DROP_MOVE);
		Transfer[] supportedTransferTypes = new Transfer[] {TransferMedia.getInstance()};
		target.setTransfer(supportedTransferTypes);
		target.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (event.widget instanceof DropTarget) {
					if (event.widget.isDisposed()) {
						return;
					}
					String transferKey = event.data.toString();
					Object[] objects = Application.getDragDropManager()
							.endTransfer(transferKey);
					if (objects.length > 0) {
						if (dropTarget instanceof PictureComposite) {
							handleDrop((PictureComposite) dropTarget, objects);
						}
					}
				}				
			};
		});
	}	
	
	/**
	 * Für das Tauschen von Bildern wird eine Drag Quelle gesetzt
	 * @param picComp
	 */
	private void createDragSource(final PictureComposite picComp) {
		if (picComp.isDisposed()) {
			return;
		}
		Transfer[] transferTypes = new Transfer[] {TransferMedia.getInstance()};

		// TreeViewer als Drag-Quelle definieren
		final DragSource source = new DragSource(picComp, DND.DROP_COPY | DND.DROP_MOVE);

		// Transfer-Typ fuer DND setzen
		source.setTransfer(transferTypes);

		// Den Listener fuer Drag-Events einrichten:
		// Dieser Stellt die Daten fuer das Drag-and-Drop zur Verfuegung
		source.addDragListener(new DragSourceAdapter() {

			@Override
			public void dragStart(DragSourceEvent event) {
				super.dragStart(event);
				Integer pictureIndex = picComp.getIndex();
								
				if (pictureIndex == pictures.size()) {			
					event.doit = false;
				}
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				Integer pictureIndex = picComp.getIndex();
				Integer[] transferObjects = new Integer[1];
				transferObjects[0] = pictureIndex;
				String transferKey = Application.getDragDropManager()
						.startTransfer(transferObjects);
				event.data = transferKey;
			}
		});
	}	
	
	/**
	 * Composite für ein einzelnes Bild. Wird für eine Bildannotation für das Bild hergenommen
	 * und bei der Picture Gallery für die einzelnen Bilder
	 * @author juhoffma	 
	 */
	private class PictureComposite extends Composite {
		
		// Breite und Größe des PictureComposite
		private int pcWidth = 100;
		private int pcHeight = 100;
		
		// das Bild das angezeigt wird
		private Picture picture;
		private Image image;
		
		// Startposition ab der das Bild gezeichnet werden soll
		private int imgX = 0;
		private int imgY = 0;
		
		// Text für leeres Bildfeld
		private String insertText = Messages.ImageEditor_0;		
		private int textX = 20;
		private int textY = 10;	
		
		// der Index des Composites, wird bei der Bildergalerie verwendet
		// zum Tauschen der Bilder und bei Änderungen eines beliebigen Bildes
		private int index;
				
		public PictureComposite(final Composite parent, int style, final Picture picture, final int index) {
			super(parent, style);
			this.picture = picture;
			this.index = index;
			this.insertText = this.insertText.replaceAll(" ", "\n");
			this.addPaintListener(new PaintListener() {	
				@Override
				public void paintControl(PaintEvent pe) {
					if (image == null && picture == null) {
						pe.gc.drawText(insertText, textX, textY, SWT.DRAW_DELIMITER);
					} else {
						pe.gc.drawImage(image, imgX, imgY);
											
					}					
					pe.gc.setLineWidth(4);
					pe.gc.setForeground(parent.getBackground());
					pe.gc.drawRectangle(2, 2, pcWidth-4, pcHeight-4);
					if (isSelected()) {
						pe.gc.setForeground(Colors.VIDEO_OVERVIEW_ITEM_BG_SELECTED.getColor());
					} else {
						pe.gc.setForeground(Colors.VIDEO_OVERVIEW_ITEM_BG.getColor());
					}					
					pe.gc.drawRoundRectangle(2, 2, pcWidth-4, pcHeight-4, 20, 20);
					pe.gc.dispose();
				}			
			});	
			
			// falls die Bildergalerie verwendet wird erlaube Selektion
			// und initialisieren das Delete-Popup Menü
			if (picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
				this.addKeyListener(new KeyListener() {
					@Override
					public void keyPressed(KeyEvent arg0) {
						pressedKeyCode = arg0.keyCode;
					}

					@Override
					public void keyReleased(KeyEvent arg0) {	
						pressedKeyCode = -1;
					}
				});
				
				this.addMouseListener(new MouseListener() {
					@Override
					public void mouseDoubleClick(MouseEvent arg0) {
					}
	
					@Override
					public void mouseDown(MouseEvent arg0) {
						// das Drop Target kann nicht selektiert werden und hat immer den Index
						// pictures.size()
						if (index == pictures.size()) {
							return;
						}
						// nur die linke Maustast hebt die Selektion wieder auf
						// nimmt man die rechte öffnet sich das Menü zum löschen
						// und das aktuelle Bild wird auf jeden Fall mit gelöscht
						if (arg0.button == 1) {
							if (isSelected()) {
								selectedPictures.remove(new Integer(index));
								lastSelectedIndex = -1;
								redraw();
								update();
							} else {
								ArrayList<Integer> toRedraw = new ArrayList<Integer>();
								// hebt die Selektion der anderen Elemente auf.
								if (!selectedPictures.contains(new Integer(index))) {
									// falls CTRL nicht gedrückt wird, werden die anderen Selektionen aufgehoben
									if (pressedKeyCode != SWT.CTRL) {
										for (Integer integer : selectedPictures) {
											toRedraw.add(new Integer(integer));
										}
										selectedPictures.clear();
									}
									// falls Shift gedrückt wird, werden alle Bilder zwischen 2 angeklickten hinzugefügt
									if (pressedKeyCode == SWT.SHIFT) {
										selectedPictures.clear();
										if (lastSelectedIndex != -1) {
											int start = 0;
											int end = 0;
											if (lastSelectedIndex < index) {
												start = lastSelectedIndex;
												end = index;
											} else {
												start = index;
												end = lastSelectedIndex;
											}
											// füge alle Indexe hinzu bis auf den aktuellen, dieser wird
											// noch seperat hinzugefügt
											for (int i = start; i <= end; i++) {
												if (i != index) {
													selectedPictures.add(new Integer(i));
													toRedraw.add(new Integer(i));													
												}
											}
										}
									}							
									// füge das aktuelle Element hinzu
									selectedPictures.add(new Integer(index));	
									lastSelectedIndex = index;	
									redraw();
									update();
									for (int i = 0; i < toRedraw.size(); i++) {	
										final Integer drawIndex = toRedraw.get(i);
										if (drawIndex != index) {
											Display.getDefault().asyncExec(new Runnable() {
												@Override
												public void run() {
													contentComposite.getChildren()[drawIndex].redraw();
													contentComposite.getChildren()[drawIndex].update();
												}
											});
										}
									}
								}
							}
						} else {		
							if (!selectedPictures.contains(new Integer(index))) {
								selectedPictures.add(new Integer(index));
								lastSelectedIndex = index;
							}
							redraw();
							update();
						}
					}
	
					@Override
					public void mouseUp(MouseEvent arg0) {
					}
				});
			}
			setPicture(picture);
		}
		
		private boolean isSelected() {
			if (selectedPictures.contains(index)) {
				return true;
			}
			return false;
		}
		
		/**
		 * gibt den Index dieses PictureComposite zurück
		 * @return
		 */
		public int getIndex() {
			return this.index;
		}
		
		public void setPicture(Picture picture) {
			setMenu(createDeleteMenu());			
			this.picture = picture;		
			prepare();
		}
		
		private void prepare() {
			if (this.isDisposed()) {
				return;
			}
			
			if (image == null && picture == null) {
				if (picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTURE) {
					this.pcWidth = 300;
					this.pcHeight = 300;
					refreshPCLayoutData();
				} else {
					this.pcWidth = 100;
					this.pcHeight = 100;
					refreshPCLayoutData();
				}				
				return;
			}
						
			Image thumb;
			if (picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTURE) {
				thumb = picture.getThumbnail(Picture.THUMBNAIL_PIC_ANNO);
				this.pcWidth = thumb.getImageData().width;
				this.pcHeight = thumb.getImageData().height;
				this.imgX = 4;
				this.imgY = 4;
			} else {
				thumb = picture.getThumbnail(Picture.THUMBNAIL_MED_PICGAL);
				imgX = -(thumb.getImageData().width - pcWidth) / 2;
				imgY = -(thumb.getImageData().height - pcHeight) / 2;
				this.pcWidth = 100;
				this.pcHeight = 100;
			}
			this.image = thumb;
			refreshPCLayoutData();						
		}
		
		private void refreshPCLayoutData() {
			boolean grabVert = true;
			boolean grabHor = true;
			if (picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
				grabVert = false;
				grabHor = false;
			}
			// setze die Größe des PictureComposite
			GridData pcGD = new GridData(SWT.CENTER, SWT.CENTER, grabHor, grabVert);
			pcGD.widthHint = this.pcWidth;
			pcGD.heightHint = this.pcHeight;
			setLayoutData(pcGD);
			redraw();
			update();
			mainComposite.layout(true);
		}
	}	

	/**
	 * ändert die Spaltenzahl der Gallerie und setzt die Edit-Galerie auf die neue Spaltenzahl und 
	 * benachrichtigt die Editoren über Änderungen
	 * @param cols
	 * @param notify if true, notify about content change
	 */
	public void setColumns(Integer cols, boolean notify) {		
		if (this.picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
			this.contentComposite.setLayout(new GridLayout(cols, false));
			if (this.gallery != null) {
				this.gallery.setNumberColumns(cols);
			}
			if (notify) {
				SivaEvent edEvent = new SivaEvent(PictureEditor.this, SivaEventType.CONTENT_CHANGED, this.gallery);
				notifySivaEventConsumers(edEvent);
			}
			adjustScrolledComposite();	
		}
	}

	@Override
	public void setContent(IAbstractBean newContent) {
		if (picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTURE) {
			setPicture((Picture)newContent);
		} else if (picAnnoContentType == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
			setPictureGallery((PictureGallery)newContent);
		}		
	}	
}

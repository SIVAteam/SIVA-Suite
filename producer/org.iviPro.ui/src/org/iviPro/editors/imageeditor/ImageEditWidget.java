package org.iviPro.editors.imageeditor;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.model.imageeditor.ICircle;
import org.iviPro.model.imageeditor.IRectangle;
import org.iviPro.model.imageeditor.IText;
import org.iviPro.model.imageeditor.ImageObject;
import org.iviPro.model.resources.Picture;
import org.iviPro.theme.Icons;

public class ImageEditWidget extends Composite {

	// List of image's overlay
	protected LinkedList<OverlayWidget> overlays = new LinkedList<OverlayWidget>();
	private LinkedList<Button> buttons = new LinkedList<Button>();
	private Picture picture;
	private Image img;
	protected Composite imgComp;

	// Clicked position
	private Point clickPos;
	// Currently selected widget
	private OverlayWidget curWid;
	private boolean selectionMode = true;

	// Possible selections for a new object
	private static final int RECTANGLE = 1;
	private static final int CIRCLE = 2;
	private static final int TEXT = 3;

	// Selected object (one of the above defined finals)
	private int choosenForm;
	// Toolbar stuff (colorfield, delete button and line width selection)
	private Label tbColorField = null;
	private Button tbDelete = null;
	private Combo sizeSelection = null;
	// Size of image
	private int imgWidth;
	private int imgHeight;
	// State if something has changend
	protected boolean dirty = false;

	public ImageEditWidget(Composite parent, int style, Picture picture,
			final Image img) {
		super(parent, style);
		this.picture = picture;
		this.img = img;
		this.imgWidth = img.getBounds().width;
		this.imgHeight = img.getBounds().height;
		this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		setLayoutData(gd);		

		GridLayout layout = new GridLayout();
		setLayout(layout);

		Composite tbComp = new Composite(this, SWT.None);
		tbComp.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WHITE));
		GridData tbCompGD = new GridData();
		tbCompGD.horizontalAlignment = SWT.FILL;
		tbCompGD.grabExcessHorizontalSpace = true;
		tbCompGD.heightHint = 30;
		tbComp.setLayoutData(tbCompGD);

		GridLayout tbLayout = new GridLayout(11, false);
		tbComp.setLayout(tbLayout);

		addButtons(tbComp);

		// Listener used for spanning up an object on creation
		final Listener span = new Listener() {

			@Override
			public void handleEvent(Event event) {
				int x = event.x;
				int y = event.y;
				int xDif = x - clickPos.x;
				int yDif = y - clickPos.y;
				// Check in which direction the user expands the new object
				if (xDif < 0 && yDif > 0) {
					xDif = 0 - xDif;
					curWid.xPos = x;
				} else if (yDif < 0 && xDif > 0) {
					yDif = 0 - yDif;
					curWid.yPos = y;
				} else if (xDif < 0 && yDif < 0) {
					xDif = 0 - xDif;
					yDif = 0 - yDif;
					curWid.xPos = x;
					curWid.yPos = y;
				}
				curWid.setSize(xDif, yDif);
				redraw();
			}
		};

		// Listener invoked on click, ensures a minimum mouse movement before
		// a new object is created
		final Listener createSpan = new Listener() {

			@Override
			public void handleEvent(Event event) {
				int x = event.x;
				int y = event.y;
				int xDif = Math.abs(clickPos.x - x);
				int yDif = Math.abs(clickPos.y - y);

				// Create new object if minimum size is reached (avoid object
				// creation on simple click)
				if (xDif >= 5 || yDif >= 5) {

					// Calculate coordinates according to image size
					float positionX = getHorizontalItF(clickPos.x);
					float positionY = getVerticalItF(clickPos.y);
					float width = getHorizontalItF(xDif);
					float height = getVerticalItF(yDif);
					ImageObject imgObj = null;
					if (choosenForm == CIRCLE) {
						imgObj = new ICircle(positionX, positionY, width,
								height, tbColorField.getBackground().getRGB(),
								getVerticalItF(sizeSelection
										.getSelectionIndex()));
					} else if (choosenForm == RECTANGLE) {
						imgObj = new IRectangle(positionX, positionY, width,
								height, tbColorField.getBackground().getRGB(),
								getVerticalItF(sizeSelection
										.getSelectionIndex()));
					} else if (choosenForm == TEXT) {
						imgObj = new IText(positionX, positionY, "", //$NON-NLS-1$
								tbColorField.getBackground().getRGB(), false,
								false, false, getHorizontalItF(10));

					}
					if (imgObj != null) {
						OverlayWidget wgt = new OverlayWidget(imgComp,
								SWT.TRANSPARENT, imgObj, ImageEditWidget.this);
						curWid = wgt;
						overlays.add(wgt);
						imgComp.removeListener(SWT.MouseMove, this);
						if (choosenForm != TEXT) {
							imgComp.addListener(SWT.MouseMove, span);
						}
						if (imgObj instanceof IText) {
							new OverlayTextEditor(ImageEditWidget.this,
									(IText) imgObj, wgt, true);
						}
						dirty = true;
					}
				}
				redraw();
			}
		};
		
		// Scrolling für das Bild
		final ScrolledComposite scrollComposite = new ScrolledComposite(this, SWT.V_SCROLL | SWT.H_SCROLL);
		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollComposite.setLayout(new GridLayout(1, true));						
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setExpandVertical(true);		
		scrollComposite.addListener(SWT.Activate, new Listener() {
	        public void handleEvent(Event e) {
	        	scrollComposite.setFocus();
	        }
	    });
		scrollComposite.getVerticalBar().setIncrement(10);	

		/*
		 * 2 Layer, Baselayer with image (drawn with GC) and transparent overlay
		 * which contains all overlay objects. Both layers use the same
		 * GridData. This is a workaround to avoid flickering objects after
		 * deleting one object.
		 */
		GridData imgCompGD = new GridData();
		imgCompGD.widthHint = img.getBounds().width;
		imgCompGD.heightHint = img.getBounds().height;

		Canvas myImg = new Canvas(scrollComposite, SWT.None);
		scrollComposite.setMinSize(img.getBounds().width, img.getBounds().height);
		scrollComposite.setContent(myImg);
		myImg.setLayoutData(imgCompGD);
		myImg.setSize(img.getBounds().width, img.getBounds().height);
		myImg.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(img, 0, 0);
			}
		});

		// Create image composite (work to be done here)
		imgComp = new Composite(myImg, SWT.TRANSPARENT);
		imgComp.setLayoutData(imgCompGD);
		imgComp.setSize(img.getBounds().width, img.getBounds().height);

		// Listener for creation of a new object
		imgComp.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				int x = event.x;
				int y = event.y;
				clickPos = new Point(x, y);
				if (!selectionMode) {
					// Invoke Listener for creating object
					imgComp.addListener(SWT.MouseMove, createSpan);
				} else {
					// If in selection mode, unselect the current object
					selectObject(null);
					curWid = null;
					tbDelete.setEnabled(false);
				}
			}
		});

		imgComp.addListener(SWT.MouseUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				imgComp.removeListener(SWT.MouseMove, span);
				imgComp.removeListener(SWT.MouseMove, createSpan);
				// if (!selectionMode) {
				// selectObject(curWid);
				// curWid.selected = true;
				// }
			}
		});
		loadObjects(picture.getObjects());
		drawObjects();
		ImageData imgData = picture.getImageData();
		if (imgData.type == SWT.IMAGE_PNG || imgData.type == SWT.IMAGE_GIF) {
			Shell warning = new Shell(Display.getCurrent());			
			MessageBox mb = new MessageBox(warning, SWT.ICON_WARNING | SWT.OK);
			mb.setMessage(Messages.ImageEditWidget_WarningTransparentRegion);
			mb.open();
			
		}
	}

	/**
	 * Adds buttons to the given toolbar
	 * 
	 * @param tbComp
	 *            toolbar to which buttons should be added
	 */
	private void addButtons(Composite tbComp) {
		final Button tbSelect = new Button(tbComp, SWT.TOGGLE);
		tbSelect.setSize(50, 20);
		tbSelect.setImage(Icons.IMAGE_EDITOR_SELECTION.getImage());
		tbSelect.setToolTipText(Messages.ImageEditWidget_Selection);
		tbSelect.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				selectionMode = true;
				selectButton(tbSelect);
			}
		});
		buttons.add(tbSelect);

		tbDelete = new Button(tbComp, SWT.None);
		tbDelete.setSize(40, 30);
		tbDelete.setImage(Icons.IMAGE_EDTIOR_DELETE.getImage());
		tbDelete.setToolTipText(Messages.ImageEditWidget_Delete);
		tbDelete.setEnabled(false);
		tbDelete.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				overlays.remove(curWid);
				curWid.dispose();
				curWid = null;
				selectObject(null);
				tbDelete.setEnabled(false);
				drawObjects();
				dirty = true;
			}
		});
		
	    Label lab = new Label(tbComp, SWT.SEPARATOR | SWT.VERTICAL);
	    GridData labGD = new GridData(1, 25);
	    lab.setLayoutData(labGD);

		final Button tbCircle = new Button(tbComp, SWT.TOGGLE);
		tbCircle.setSize(30, 20);
		tbCircle.setImage(Icons.IMAGE_EDITOR_ELLIPSE.getImage());
		tbCircle.setToolTipText(Messages.ImageEditWidget_Circle);
		tbCircle.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				choosenForm = CIRCLE;
				selectionMode = false;
				selectButton(tbCircle);
				selectObject(null);
				tbDelete.setEnabled(false);
			}
		});
		buttons.add(tbCircle);

		final Button tbRectangle = new Button(tbComp, SWT.TOGGLE);
		tbRectangle.setSize(40, 20);
		tbRectangle.setImage(Icons.IMAGE_EDITOR_RECTANGLE.getImage());
		tbRectangle.setToolTipText(Messages.ImageEditWidget_Rectangle);
		tbRectangle.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				choosenForm = RECTANGLE;
				selectionMode = false;
				selectButton(tbRectangle);
				selectObject(null);
				tbDelete.setEnabled(false);
			}
		});
		buttons.add(tbRectangle);

		final Button tbText = new Button(tbComp, SWT.TOGGLE);
		tbText.setSize(40, 20);
		tbText.setImage(Icons.IMAGE_EDITOR_TEXT.getImage());
		tbText.setToolTipText(Messages.ImageEditWidget_Text);
		tbText.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				choosenForm = TEXT;
				selectionMode = false;
				selectButton(tbText);
				selectObject(null);
				tbDelete.setEnabled(false);
			}
		});
		buttons.add(tbText);

		Composite tbColorFieldBG = new Composite(tbComp, SWT.None);
		tbColorFieldBG.setSize(20, 20);
		tbColorFieldBG.setLayout(new GridLayout(1, false));
		tbColorFieldBG.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_GRAY));

		tbColorField = new Label(tbColorFieldBG, SWT.None);
		tbColorField.setToolTipText(Messages.ImageEditWidget_Color_select);
		tbColorField.setSize(10, 10);
		tbColorField.setText("    "); //$NON-NLS-1$

		tbColorField.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WHITE));
		tbColorField.addListener(SWT.MouseDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// Create a window to select color for object
				Shell shell = new Shell(Display.getCurrent());
				ColorDialog cc = new ColorDialog(shell);
				cc.setRGB(tbColorField.getBackground().getRGB());
				cc.setText(Messages.ImageEditWidget_Color_select);
				RGB rgb = cc.open();
				if (rgb != null) {
					tbColorField.setBackground(new Color(Display.getCurrent(),
							rgb));
					if (curWid != null) {
						curWid.imgObj.color = tbColorField.getBackground()
								.getRGB();
						dirty = true;
					}
				}
			}
		});

		Label label = new Label(tbComp, SWT.CENTER);
		label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		label.setText(Messages.ImageEditWidget_LineWidth);
		GridData labelGD = new GridData();
		labelGD.horizontalIndent = 10;
		label.setLayoutData(labelGD);
		
		sizeSelection = new Combo(tbComp, SWT.DROP_DOWN);
		for (int i = 0; i < 5; i++) {
			sizeSelection.add(String.valueOf(i + 1) + " Pixel", i); //$NON-NLS-1$
		}
		sizeSelection.select(0);
		sizeSelection.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (curWid != null) {
					curWid.imgObj.linewidth = getHorizontalItF(sizeSelection
							.getSelectionIndex());
					dirty = true;
				}
			}
		});

		final Button tbBack = new Button(tbComp, SWT.None);
		tbBack.setSize(40, 30);
		tbBack.setImage(Icons.IMAGE_EDITOR_FORBACK.getImage());
		tbBack.setToolTipText(Messages.ImageEditWidget_Move_Below);
		tbBack.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				curWid.moveBelow(null);

			}
		});

		final Button tbSave = new Button(tbComp, SWT.NONE);
		tbSave.setText(Messages.ImageEditWidget_Save);
		tbSave.setSize(40, 30);
		tbSave.setImage(Icons.IMAGE_EDITOR_SAVE.getImage());
		tbSave.setToolTipText(Messages.ImageEditWidget_Save);
		GridData tbSaveGD = new GridData();
		tbSaveGD.grabExcessHorizontalSpace = true;
		tbSaveGD.horizontalAlignment = SWT.RIGHT;
		tbSave.setLayoutData(tbSaveGD);
		tbSave.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				picture.setObjects(getObjectsForSave());
				dirty = false;
			}
		});
	}

	/**
	 * Redraws all widgets
	 */
	protected void drawObjects() {
		for (OverlayWidget w : overlays) {
			w.redraw();
		}
		redraw();
	}

	/**
	 * Selects an clicked object and sets the fields in the toolbar to the
	 * corresponding values
	 * 
	 * @param select
	 *            object to select
	 */
	public void selectObject(OverlayWidget select) {
		for (OverlayWidget w : overlays) {
			if (!w.equals(select)) {
				w.unselect();
			} else {
				curWid = w;
				tbColorField.setBackground(new Color(Display.getCurrent(),
						w.imgObj.color));
				sizeSelection.select(getHorizontalFtI(w.imgObj.linewidth));
				curWid.moveAbove(null);
			}
		}
	}

	/**
	 * Selects an button and unselect all the other ones
	 * 
	 * @param select
	 *            button to select
	 */
	private void selectButton(Button select) {
		for (Button b : buttons) {
			if (b != select) {
				b.setSelection(false);
			}
		}
		select.setSelection(false);
	}

	/**
	 * Returns if the editor is in selection mode or not
	 * 
	 * @return true if editor is in selection mode
	 */
	public boolean isSelection() {
		return selectionMode;
	}

	/**
	 * Calculates a vertical coordinate corresponding to the position on the
	 * image
	 * 
	 * @param y
	 *            Position as absolute Integer
	 * @return vertical position as relative float
	 */
	public float getVerticalItF(int y) {
		return ((float) y) / ((float) imgHeight);
	}

	/**
	 * Calculates a horizontal coordinate corresponding to the position on the
	 * image
	 * 
	 * @param y
	 *            Position as absolute integer
	 * @return horizontal position as relative float
	 */
	public float getHorizontalItF(int x) {
		return ((float) x) / ((float) imgWidth);
	}

	/**
	 * Calculate a vertical absolute position according to the relative position
	 * on the image
	 * 
	 * @param y
	 *            Position as relative float
	 * @return vertical position as absolute integer
	 */
	public int getVerticalFtI(float y) {
		return (int) (y * imgHeight);
	}
	private static int getVerticalFtI(float y, int imageHeight) {
		return (int) (y * imageHeight);
	}

	/**
	 * Calculate a horizontal absolute position according to the relative
	 * position on the image
	 * 
	 * @param y
	 *            Position as relative float
	 * @return horizontal position as absolute integer
	 */
	public int getHorizontalFtI(float x) {
		return (int) (x * imgWidth);
	}
	private static int getHorizontalFtI(float x, int imageWidth) {
		return (int) (x * imageWidth);
	}

	/**
	 * Enables the delete button if an object is selected
	 */
	public void enableDelete() {
		tbDelete.setEnabled(true);
	}
	
	/**
	 * Draws an Object on a Widget
	 * @param gc	GC to be drawn with
	 * @param imgObj	Object to be drawn
	 */
	public void drawObject (GC gc, ImageObject imgObj) {
		drawObject(gc, imgObj, img, new Point(5,5));
	}
	
	/**
	 * Draws an Object with a given GC directly on a image
	 * @param gc	GC to be drawn with
	 * @param imgObj	Object to be drawn
	 * @param image		Image to be drawn on
	 */
	public static void drawObject (GC gc, ImageObject imgObj, Image image) {
		int imageWidth= image.getBounds().width;
		int imageHeight = image.getBounds().height;
		int x = getHorizontalFtI(imgObj.x, imageWidth);
		int y = getVerticalFtI(imgObj.y, imageHeight);
		drawObject(gc, imgObj, image, new Point(x, y));
		
	}

	/**
	 * Does the actual drawing
	 */
	private static void drawObject(GC gc, ImageObject imgObj, Image image, Point position) {
		int imageWidth= image.getBounds().width;
		int imageHeight = image.getBounds().height;
		int x = position.x;
		int y = position.y;
		int width = getHorizontalFtI(imgObj.width, imageWidth);
		int height = getVerticalFtI(imgObj.height, imageHeight);
		gc.setForeground(new Color(Display.getCurrent(), imgObj.color));
		gc.setLineStyle(SWT.LINE_SOLID);
		// Draw object according to class
		if (imgObj instanceof IRectangle) {
			gc.setLineWidth(getHorizontalFtI(imgObj.linewidth, imageWidth) + 1);
			gc.drawRectangle(x, y, width, height);
		} else if (imgObj instanceof ICircle) {
			gc.setLineWidth(getHorizontalFtI(imgObj.linewidth, imageWidth) + 1);
			gc.drawOval(x, y, width, height);
		} else if (imgObj instanceof IText) {
			IText imgText = (IText) imgObj;
			int style = SWT.NORMAL;
			if (imgText.bold && imgText.italic) {
				style = (SWT.BOLD | SWT.ITALIC);
			} else if (imgText.bold && !imgText.italic) {
				style = SWT.BOLD;
			} else if (!imgText.bold && imgText.italic) {
				style = SWT.ITALIC;
			}
			Font font = new Font(Display.getCurrent(), "Arial",  //$NON-NLS-1$
					getHorizontalFtI(imgText.fontsize, imageWidth), style);
			gc.setFont(font);
			Point extent = gc.textExtent(imgText.text);
			width = extent.x;
			height = extent.y;
			gc.drawText(imgText.text, x, y, true);
			gc.setLineWidth(1);
			if (imgText.underline) {
				gc
						.drawLine(x, y + extent.y - 1, x + extent.x,
								y + extent.y - 1);
			}
			font.dispose();
		}
	}

	private void loadObjects(LinkedList<ImageObject> objects) {
		for (ImageObject io : objects) {

			OverlayWidget wgt = new OverlayWidget(imgComp, SWT.TRANSPARENT, io,
					ImageEditWidget.this);
			overlays.add(wgt);
		}
		redraw();
	}

	public LinkedList<ImageObject> getObjectsForSave() {
		LinkedList<ImageObject> objects = new LinkedList<ImageObject>();
		for (OverlayWidget ow : overlays) {
			objects.add(ow.imgObj);
		}
		return objects;
	}

	public boolean isDirty() {
		return dirty;
	}
}
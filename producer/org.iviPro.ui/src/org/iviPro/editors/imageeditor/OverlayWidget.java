package org.iviPro.editors.imageeditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.model.imageeditor.ICircle;
import org.iviPro.model.imageeditor.IRectangle;
import org.iviPro.model.imageeditor.IText;
import org.iviPro.model.imageeditor.ImageObject;

public class OverlayWidget extends Composite {

	// Widget's position in absolute pixels
	protected int width;
	protected int height;
	protected int xPos;
	protected int yPos;

	// Click positions for the widget, needed for resizing
	private final int TOPLEFT = 1;
	private final int TOP = 2;
	private final int TOPRIGHT = 3;
	private final int RIGHT = 4;
	private final int RIGHTBOT = -1;
	private final int BOT = -2;
	private final int LEFTBOT = -3;
	private final int LEFT = -4;
	private final int CENTER = 5;

	// current click position according to list above
	private int clickPosCent = 0;

	private Point curClickPos;
	public boolean selected = false;
	private boolean resizeEvent = false;

	// OverlayObject obj;
	public ImageObject imgObj;

	// Editor
	protected ImageEditWidget editor;


	public OverlayWidget(Composite parent, int style, ImageObject object,
			final ImageEditWidget editor) {
		super(parent, style);
		this.imgObj = object;
		this.editor = editor;
		this.xPos = editor.getHorizontalFtI(object.x);
		this.yPos = editor.getVerticalFtI(object.y);
		this.width = editor.getHorizontalFtI(object.width);
		this.height = editor.getVerticalFtI(object.height);
		setBounds(xPos - 5, yPos - 5, width + 1, height + 1);

		this.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				setBounds((xPos - 5), (yPos - 5), width + 12, height + 12);
				// Draw selection rectangle
				if (selected) {
					e.gc.setForeground(Display.getCurrent().getSystemColor(
							SWT.COLOR_WHITE));
					e.gc.setLineStyle(SWT.LINE_DOT);
					e.gc.setLineCap(SWT.CAP_SQUARE);
					e.gc.drawRectangle(0, 0, width + 10, height + 10);
				}
				//drawObject(e.gc);
				editor.drawObject(e.gc, imgObj);
				if (imgObj instanceof IText) {
					setSize(e.gc.textExtent(((IText) imgObj).text));
				}

			}
		});

		setEvents();
		redraw();
	}

	/**
	 * Method to draw the embedded object, necessary due MVC pattern
	 * 
	 * @param gc
	 *            The widgets PaintListeners GC
	 */
	@SuppressWarnings("unused")
	private void drawObject(GC gc) {
		gc.setForeground(new Color(Display.getCurrent(), imgObj.color));
		// Draw object according to class
		if (imgObj instanceof IRectangle) {
			gc.setLineStyle(SWT.LINE_SOLID);
			gc.setLineWidth(editor.getHorizontalFtI(imgObj.linewidth) + 1);
			gc.drawRectangle(5, 5, width, height);
		} else if (imgObj instanceof ICircle) {
			gc.setLineStyle(SWT.LINE_SOLID);
			gc.setLineWidth(editor.getHorizontalFtI(imgObj.linewidth) + 1);
			gc.drawOval(5, 5, width, height);
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
			Font font = new Font(Display.getCurrent(), "Arial", editor //$NON-NLS-1$
					.getHorizontalFtI(imgText.fontsize), style);
			gc.setFont(font);
			Point extent = gc.textExtent(imgText.text);
			width = extent.x;
			height = extent.y;
			setSize(extent);
			gc.drawText(imgText.text, 5, 5, true);
			if (imgText.underline) {
				gc
						.drawLine(4, 4 + extent.y - 1, 4 + extent.x,
								4 + extent.y - 1);
			}
			font.dispose();
		}
	}

	/**
	 * Adds Listeners for all different possible actions like resizing or 
	 * moving.
	 */
	private void setEvents() {
		final Listener resize = new Listener() {

			@Override
			public void handleEvent(Event event) {
				// Ensure that a single click on a not selected widget does't
				// trigger a resize event (second click for resize/moving)
				boolean changed = false;
				if (!resizeEvent) {
					resizeEvent = true;
					changed = true;
				}
				if (resizeEvent && changed) {
					int posX = event.x;
					int posY = event.y;
					// Text can only be moved around (-> set correct mouse pointer)
					if (imgObj instanceof IText) {
						clickPosCent = CENTER;
					} else {
						resizer(posX, posY);

						// If size drops below 0, switch over and recalculate
						// size and position
						if (width <= 0 || height <= 0) {
							boolean widthChange = false;
							boolean heightChange = false;
							if (width <= 0 && posX <= 0) {
								width = Math.abs(posX);
								xPos = xPos + posX;
								widthChange = true;
							}
							if (width <= 0 && posX > 0) {
								width = Math.abs(width);
								xPos = xPos - width;
								widthChange = true;
							}
							if (height <= 0 && posY <= 0) {
								height = Math.abs(posY);
								yPos = yPos + posY;
								heightChange = true;
							}
							if (height <= 0 && posY > 0) {
								height = Math.abs(height);
								yPos = yPos - height;
								heightChange = true;
							}
							// Switch positions when user moves one side over
							// the opposite side
							if (clickPosCent == LEFT) {
								clickPosCent = RIGHT;
							} else if (clickPosCent == RIGHT) {
								clickPosCent = LEFT;
							} else if (clickPosCent == TOPLEFT && widthChange
									&& !heightChange) {
								clickPosCent = TOPRIGHT;
							} else if (clickPosCent == TOPLEFT && !widthChange
									&& heightChange) {
								clickPosCent = LEFTBOT;
							} else if (clickPosCent == TOPLEFT && widthChange
									&& heightChange) {
								clickPosCent = RIGHTBOT;
							} else if (clickPosCent == RIGHTBOT && widthChange
									&& !heightChange) {
								clickPosCent = LEFTBOT;
							} else if (clickPosCent == RIGHTBOT && !widthChange
									&& heightChange) {
								clickPosCent = TOPRIGHT;
							} else if (clickPosCent == RIGHTBOT && widthChange
									&& heightChange) {
								clickPosCent = TOPLEFT;
							} else if (clickPosCent == TOP) {
								clickPosCent = BOT;
							} else if (clickPosCent == BOT) {
								clickPosCent = TOP;
							} else if (clickPosCent == TOPRIGHT && widthChange
									&& !heightChange) {
								clickPosCent = TOPLEFT;
							} else if (clickPosCent == TOPRIGHT && !widthChange
									&& heightChange) {
								clickPosCent = RIGHTBOT;
							} else if (clickPosCent == TOPRIGHT && widthChange
									&& heightChange) {
								clickPosCent = LEFTBOT;
							} else if (clickPosCent == LEFTBOT && widthChange
									&& !heightChange) {
								clickPosCent = RIGHTBOT;
							} else if (clickPosCent == LEFTBOT && !widthChange
									&& heightChange) {
								clickPosCent = TOPLEFT;
							} else if (clickPosCent == LEFTBOT && widthChange
									&& heightChange) {
								clickPosCent = TOPRIGHT;
							}

							redraw();
						}
					}
					// Move widget around
					if (clickPosCent == CENTER) {

						int difX = posX - curClickPos.x;
						int difY = posY - curClickPos.y;
						int newX = xPos + difX;
						int newY = yPos + difY;
						if (newX + width <= editor.imgComp.getBounds().width
								&& newY + height <= editor.imgComp.getBounds().height
								&& newX >= 0 && newY >= 0) {
							xPos = newX;
							yPos = newY;
						}

					}
					// Set embedded objects size (calculation needed)
					setObjSize();
					redraw();
					resizeEvent = false;
				}
				editor.dirty = true;
			}

			/**
			 * Method to resize the widget
			 * 
			 * @param posX
			 * @param posY
			 */
			private void resizer(int posX, int posY) {
				if (clickPosCent == LEFT || clickPosCent == LEFTBOT
						|| clickPosCent == TOPLEFT) {
					xPos = xPos + posX;
					width = width - posX;
				}

				if (clickPosCent == RIGHT || clickPosCent == RIGHTBOT
						|| clickPosCent == TOPRIGHT) {
					width = width + (posX - width);
				}

				if (clickPosCent == TOP || clickPosCent == TOPRIGHT
						|| clickPosCent == TOPLEFT) {
					yPos = yPos + posY;
					height = height - posY;
				}

				if (clickPosCent == LEFTBOT || clickPosCent == RIGHTBOT
						|| clickPosCent == BOT) {
					height = height + (posY - height);
				}
			}
		};

		addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event e) {
				// If widget is not selected, dispatch the mouse event to the
				// underlying editor
				if (selected) {
					removeListener(SWT.MouseMove, resize);
					setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_ARROW));
					clickPosCent = 0;
				} else {
					Event dispatch = new Event();
					dispatch.x = xPos + e.x;
					dispatch.y = yPos + e.y;
					editor.imgComp.notifyListeners(SWT.MouseUp, dispatch);
				}
			}
		});

		addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				// If editor is not in selection mode, dispatch the mouse event 
				// to the underlying editor
				if (editor.isSelection()) {
					editor.selectObject(OverlayWidget.this);
					selected = true;
					editor.enableDelete();
					int x = e.x;
					int y = e.y;
					curClickPos = new Point(x, y);
					// calculate clicked position number 
					clickPosCent = calcClickPos(x, y);
					addListener(SWT.MouseMove, resize);

				} else {
					Event dispatch = new Event();
					dispatch.x = xPos + e.x;
					dispatch.y = yPos + e.y;
					editor.imgComp.notifyListeners(SWT.MouseDown, dispatch);
				}
			}
		});

		if (imgObj instanceof IText) {
			addListener(SWT.MouseDoubleClick, new Listener() {

				@Override
				public void handleEvent(Event event) {
					if (selected) {
						new OverlayTextEditor(editor, (IText) imgObj, OverlayWidget.this,
								false);
					}
				}
			});
		}

		// Set the mouse pointer for current mouse position
		addListener(SWT.MouseMove, new Listener() {
			public void handleEvent(Event e) {
				int x = e.x;
				int y = e.y;

				if (selected) {
					int curMovPos = calcClickPos(x, y);
					if (imgObj instanceof IText) {
						setCursor(new Cursor(Display.getCurrent(),
								SWT.CURSOR_SIZEALL));
					} else {
						if (curMovPos == TOPLEFT || curMovPos == RIGHTBOT) {
							setCursor(new Cursor(Display.getCurrent(),
									SWT.CURSOR_SIZENW));
						} else if (curMovPos == TOP || curMovPos == BOT) {
							setCursor(new Cursor(Display.getCurrent(),
									SWT.CURSOR_SIZENS));
						} else if (curMovPos == TOPRIGHT
								|| curMovPos == LEFTBOT) {
							setCursor(new Cursor(Display.getCurrent(),
									SWT.CURSOR_SIZENE));
						} else if (curMovPos == RIGHT || curMovPos == LEFT) {
							setCursor(new Cursor(Display.getCurrent(),
									SWT.CURSOR_SIZEWE));
						} else if (curMovPos == CENTER) {
							setCursor(new Cursor(Display.getCurrent(),
									SWT.CURSOR_SIZEALL));
						} else {
							setCursor(new Cursor(Display.getCurrent(),
									SWT.CURSOR_ARROW));
						}
					}
				} else {
					Event dispatch = new Event();
					dispatch.x = xPos + e.x;
					dispatch.y = yPos + e.y;
					editor.imgComp.notifyListeners(SWT.MouseMove, dispatch);
				}
			}
		});

		// Reset mouse pointer if mouse pointer leaves the widget
		addListener(SWT.MouseExit, new Listener() {

			@Override
			public void handleEvent(Event event) {
				setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_ARROW));
			}
		});
	}

	/**
	 * Unselects the widget
	 */
	public void unselect() {
		selected = false;
		redraw();
	}

	/**
	 * Sets the size of the widget and the embedded object
	 * @param width Width to be set
	 * @param height Height to be set
	 */
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		setObjSize();
		redraw();
	}

	/**
	 * Sets the size of the embedded object, converts coordinates from 
	 * absolute integer to relative float
	 */
	private void setObjSize() {
		imgObj.x = editor.getHorizontalItF(xPos);
		imgObj.width = editor.getHorizontalItF(width);
		imgObj.y = editor.getVerticalItF(yPos);
		imgObj.height = editor.getVerticalItF(height);
	}

	/**
	 * Calculates which side of the widget is click or on which position the
	 * mouse pointer is currently
	 * @param x	x coordinate of event
	 * @param y	y coordinate of event
	 * @return	position
	 */
	private int calcClickPos(int x, int y) {
		// tolerance range
		int range = 5;

		if (x < 0 + range && y < 0 + range) {
			return TOPLEFT;
		} else if (x > 0 + width - 1 - range && y > 0 + height - 1 - range) {
			return RIGHTBOT;
		} else if (x < 0 + range && y > 0 + height - range) {
			return LEFTBOT;
		} else if (x > 0 + width - range && y < 0 + range) {
			return TOPRIGHT;
		} else if (x < 0 + range) {
			return LEFT;
		} else if (y < 0 + range) {
			return TOP;
		} else if (x > 0 + width - range) {
			return RIGHT;
		} else if (y > 0 + height - range) {
			return BOT;
		} else {
			return CENTER;
		}
	}

	public String toString() {
		return xPos + "/" + yPos + " - " + width + "*" + height + ": " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ imgObj.getClass();
	}

	public boolean equals(Object o) {
		boolean equal = false;
		// Two widgets are equal if they are at the same position, have the same
		// size, the same imgObj, the same color and the same linewidth
		if (o instanceof OverlayWidget) {
			OverlayWidget wgt = (OverlayWidget) o;
			equal = this.xPos == wgt.xPos && this.yPos == wgt.yPos
					&& this.width == wgt.width && this.height == wgt.height
					&& this.imgObj.getClass().equals(this.imgObj.getClass())
					&& this.imgObj.color.equals(wgt.imgObj.color)
					&& this.imgObj.linewidth == wgt.imgObj.linewidth;
		}
		return equal;
	}

}

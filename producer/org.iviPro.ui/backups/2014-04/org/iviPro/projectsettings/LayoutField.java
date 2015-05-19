package org.iviPro.projectsettings;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class LayoutField {

	private static Logger logger = Logger.getLogger(LayoutField.class);
	
	protected final static int LEFT = 0;
	protected final static int TOP = 1;
	protected final static int CENTER = 2;
	protected final static int BOTTOM = 3;
	protected final static int RIGHT = 4;
	
	protected final static int BORDERLEFT = 10;
	protected final static int BORDERRIGHT = 11;
	protected final static int BORDERTOP = 12;
	protected final static int BORDERBOTTOM = 13;
	
	private float width;
	private float height;
	private float x;
	private float y;
	private int id;
	private Composite canvas;
	private int position;
	private LayoutPane pane;
	
	public LayoutField(LayoutPane pane, int id) {
		this.pane = pane;
		this.id = id;
		canvas = new Composite(pane, SWT.BORDER);
		canvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		canvas.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
				if (LayoutField.this.id == LEFT) {
					gc.drawText(Messages.LayoutField_Title_Left, 0, 0);
				} else if (LayoutField.this.id == TOP) {
					gc.drawText(Messages.LayoutField_Title_Top, 0, 0);
				} else if (LayoutField.this.id == CENTER) {
					gc.drawText(Messages.LayoutField_Title_Center, 0, 0);
				} else if (LayoutField.this.id == BOTTOM) {
					gc.drawText(Messages.LayoutField_Title_Bottom, 0, 0);
				} else if (LayoutField.this.id == RIGHT) {
					gc.drawText(Messages.LayoutField_Title_Right, 0, 0);
				} 	
				gc.dispose();
			}
		});
		
		setListener();
	}
	
	private void setListener() {
		
		final Listener resize = new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				int x = event.x;
				int y = event.y;
				// Umrechnen der Höhe/Breite inkl. Rahmen
				int height = pane.verticalFtI(LayoutField.this.height) - canvas.getBorderWidth();
				int width = pane.horizontalFtI(LayoutField.this.width) - canvas.getBorderWidth();
				// Größe des Feldes aktualisieren
				if ((id == CENTER || id == BOTTOM )&& position == BORDERTOP) {
					height = (height - y) + canvas.getBorderWidth();
					pane.arrange(id, position, 0, pane.verticalItF(height));
				} else if ((id == CENTER || id == TOP) && position == BORDERBOTTOM) {
					height = height - (height - y) + canvas.getBorderWidth();
					pane.arrange(id, position, 0, pane.verticalItF(height));
				} else if ((id == CENTER || id == BOTTOM || id == TOP || id == RIGHT) && position == BORDERLEFT) {
					int tempid = id;
					if (id == BOTTOM || id == TOP) {
						tempid = CENTER;
					}
					width = width - x;
					pane.arrange(tempid, position, pane.horizontalItF(width), 0);
				} else if ((id == CENTER || id == BOTTOM || id == TOP || id == LEFT) && position == BORDERRIGHT) {
					int tempid = id;
					if (id == BOTTOM || id == TOP) {
						tempid = CENTER;
					}
					width = width - (width -x);
					pane.arrange(tempid, position, pane.horizontalItF(width), 0);
				}
				
				
			}
		};		
		if (true) {
			canvas.addListener(SWT.MouseDown, new Listener() {
				
				@Override
				public void handleEvent(Event event) {
					
					position = calcClickPos(event.x, event.y);
					if (position != 0) {
						canvas.addListener(SWT.MouseMove, resize);
						canvas.addListener(SWT.MouseUp, new Listener() {
							
							@Override
							public void handleEvent(Event event) {
								canvas.removeListener(SWT.MouseMove, resize);
								canvas.removeListener(SWT.MouseUp, this);
								position = 0;
								pane.checkFieldSizes();
							}
						});
					}
					
				}
			});
			canvas.addListener(SWT.MouseMove, new Listener() {
				
				@Override
				public void handleEvent(Event event) {
					int pos = calcClickPos(event.x, event.y);
					if ((id == CENTER || id == BOTTOM ) && pos == BORDERTOP) {
						canvas.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_SIZEN));
					} else if ((id == CENTER || id == TOP) && pos == BORDERBOTTOM) {
						canvas.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_SIZES));
					} else if ((id == CENTER || id == BOTTOM || id == TOP || id == RIGHT) && pos == BORDERLEFT) {
						canvas.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_SIZEW));
					} else if ((id == CENTER || id == BOTTOM || id == TOP || id == LEFT) && pos == BORDERRIGHT) {
						canvas.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_SIZEE));
					} else {
						canvas.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_ARROW));
					}
					
				}
			});
		}
	}
	
	public void setBounds(int x, int y, int width, int height) {
		this.height = pane.verticalItF(height);
		this.width = pane.horizontalItF(width);
		this.x = pane.horizontalItF(x);
		this.y = pane.verticalItF(y);
		canvas.setBounds(x, y, width, height);
		canvas.setVisible(true);
		canvas.redraw();
	}
	
	public void setBounds(float x, float y, float width, float height) {
		setBounds(pane.horizontalFtI(x), pane.verticalFtI(y), pane.horizontalFtI(width), pane.verticalFtI(height));
	}
	
	
	public void setHeight(float y, float height) {
		this.height = height;
		Rectangle bounds = canvas.getBounds();
		bounds.height = pane.verticalFtI(height);
		if (y >= 0) {
			this.y = y;
			bounds.y = pane.verticalFtI(y);
		}
		canvas.setBounds(bounds);
	}
	
	public void resetSize() {
		logger.debug(id + ": " + x + " - " + y + " - " + width + " - " + height); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		canvas.setBounds(pane.horizontalFtI(x), pane.verticalFtI(y), pane.horizontalFtI(width), pane.verticalFtI(height));
		Rectangle rect = canvas.getBounds();
		logger.debug(id + ": " + rect.x + "/" + rect.y + " - " + rect.width + " * " + rect.height); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	public float getHeight() {
		return height;
	}
	
	public void setWidth(float x, float width) {
		this.width = width;
		Rectangle bounds = canvas.getBounds();
		bounds.width = pane.horizontalFtI(width);
		if (x >= 0) {
			this.x = x;
			bounds.x = pane.horizontalFtI(x);
		}
		canvas.setBounds(bounds);
	}
	public float getWidth() {
		return width;
	}
	public Rectangle getBounds() {
		return canvas.getBounds();
	}
	
	private int calcClickPos(int x, int y) {
		int range = 10;
		int height = pane.verticalFtI(this.height) - canvas.getBorderWidth();
		int width = pane.horizontalFtI(this.width) - canvas.getBorderWidth();
		if (id == CENTER) {
			if (y < 0 + range) {
				return BORDERTOP;
			} else if (y > 0 + height - range) {
				return BORDERBOTTOM;
			} else if (x < 0 + range) {
				return BORDERLEFT;
			} else if (x > 0 + width - range) {
				return BORDERRIGHT;
			}
		} else if (id == LEFT) {
			if (x > 0 + width - range ) {
				return BORDERRIGHT;
			}
		} else if (id == RIGHT) {
			if (x < 0 + range) {
				return BORDERLEFT;
			}
		} else if (id == TOP) {
			if (y > 0 + height - range) {
				return BORDERBOTTOM;
			} else if (x < 0 + range) {
				return BORDERLEFT;
			} else if (x > 0 + width - range) {
				return BORDERRIGHT;
			}
		} else if (id == BOTTOM) {
			if (y < 0 + range) {
				return BORDERTOP;
			} else if (x < 0 + range) {
				return BORDERLEFT;
			} else if (x > 0 + width - range) {
				return BORDERRIGHT;
			}
		}
		

//		if (x < 0 + range) {
//			return BORDERLEFT;
//		} else if (y < 0 + range) {
//			return BORDERTOP;
//		} else if (x > 0 + width - range) {
//			return BORDERRIGHT;
//		} else if (y > 0 + height - range) {
//			return BORDERBOTTOM;
//		}
		
		return 0;
	}
	
	public String toString() {
		return id + ": " + width + "/" + height; //$NON-NLS-1$ //$NON-NLS-2$
	}
}

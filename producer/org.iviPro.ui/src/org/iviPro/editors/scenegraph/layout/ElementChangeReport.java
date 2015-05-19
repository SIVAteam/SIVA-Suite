package org.iviPro.editors.scenegraph.layout;

import org.iviPro.editors.scenegraph.editparts.IEditPartNode;

/**
 * "Bean"-Klasse zur Übermittlung von Änderungen an den Layoutmanager
 * (wird in PropertyChangeEvents verschickt)
 * @author grillc
 *
 */
public class ElementChangeReport implements Comparable<ElementChangeReport> {
	
	private IEditPartNode editpart;
	private int xchange;
	private int ychange;

	public ElementChangeReport(IEditPartNode editpart, int xchange, int ychange) {
		this.editpart = editpart;
		this.xchange = xchange;
		this.ychange = ychange;
	}

	public int getXchange() {
		return xchange;
	}

	public int getYchange() {
		return ychange;
	}

	public IEditPartNode getEditpart() {
		return editpart;
	}

	@Override
	public int compareTo(ElementChangeReport compareEcp) {
		if (this.getEditpart().getFigure().getBounds().y < compareEcp.getEditpart().getFigure().getBounds().y) return -1;
		else if (this.getEditpart().getFigure().getBounds().y == compareEcp.getEditpart().getFigure().getBounds().y) return 0;
		else return 1;
	}
}

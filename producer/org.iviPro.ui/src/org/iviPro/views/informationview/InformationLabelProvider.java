package org.iviPro.views.informationview;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.iviPro.theme.Icons;

/**
 * Diese Klasse stellt Funktionen bereit, wie die Objekte und Bilder der View
 * Information angezeigt werden.
 * 
 * @author Florian Stegmaier
 */
public class InformationLabelProvider extends LabelProvider implements
		ITableLabelProvider {
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(InformationLabelProvider.class);
	private static final String INFO_DESC = Messages.InformationLabelProvider_Description;
	private static final String INFO_VALUE = Messages.InformationLabelProvider_Value;

	public InformationLabelProvider() {
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {

		Image toShow = null;
		
		if (columnIndex == 0) {
			toShow = Icons.VIEW_INFORMATION.getImage();
		}

		return toShow;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {

		String colText = null;

		if (columnIndex == 0) {
			colText = ((Information) element).getInfoDesc();
		} else if (columnIndex == 1) {
			colText = ((Information) element).getInfoText();
		}

		return colText;
	}

	public void createColumns(Table table) {
		String[] titles = { INFO_DESC, INFO_VALUE };
		int[] bounds = { 150, 150 };

		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.LEFT, i);
			column.setText(titles[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
		}
	}

}

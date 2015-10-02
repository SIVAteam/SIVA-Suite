package org.iviPro.utils.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Utility class for easy creation of a widget consisting of a sized text
 * field which is immediately followed by a label indicating the unit used
 * inside the text field.
 * @author John
 */
public class SizedTextWithUnit extends Composite {

	private Text text;
	private Label unitLabel;
	
	/**
	 * Creates a text field which allows the given number of input characters
	 * and is sized to fit this number. The given unit is displayed as a 
	 * <code>Label</code> behind the text field. The given style is applied 
	 * to the text field and has to be appropriate according to 
	 * {@link org.eclipse.swt.widgets.Text}.
	 * @param parent parent composite
	 * @param textLength allowed input size of the text field
	 * @param unit unit displayed behind the text field
	 */
	public SizedTextWithUnit(Composite parent, int style, int textLength, String unit) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 1;
		this.setLayout(layout);
			
		SizedText sText = new SizedText(this, style, textLength, true);
		text = sText.getTextField();
		if (unit != null) {
			unitLabel = new Label(this, SWT.LEFT);
			unitLabel.setText(unit);
		}
	}
	
	/**
	 * Returns the text field of this widget.
	 * @return text field of the widget
	 */
	public Text getTextField() {
		return text;
	}
}

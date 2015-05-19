package org.iviPro.utils.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Utility class for creation of a text field which allows only a limited number
 * of input characters and is sized to fit this number.
 * @author John
 *
 */
public class SizedText extends Composite {
	private Text text;
	
	/**
	 * Creates a text field which allows the given number of input characters
	 * and is sized to fit this number. The given style is applied to the text
	 * field and has to be appropriate according to 
	 * {@link org.eclipse.swt.widgets.Text}.
	 * @param parent parent composite
	 * @param style style of the text field
	 * @param textLength allowed input size of the text field
	 */
	public SizedText(Composite parent, int style, int textLength) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		this.setLayout(layout);
		text = new Text(this, style);
		
		GridData textLayout = new GridData(SWT.FILL, SWT.CENTER, false, false);
		GC gc = new GC(text);
		int width = gc.getFontMetrics().getAverageCharWidth() * textLength;
		gc.dispose();
		textLayout.widthHint = width;
		text.setLayoutData(textLayout);
		text.setTextLimit(textLength);
	}
	
	/**
	 * Returns the text field of this widget.
	 * @return text field of the widget
	 */
	public Text getTextField() {
		return text;
	}
	
	/**
	 * Returns the content of the text field of this widget.
	 * @return content of the text field of the widget
	 */
	public String getText() {
		return text.getText();
	}
	
	/**
	 * Sets the content of the text field of this widget.
	 * @param newText content to be set
	 */
	public void setText(String newText) {
		text.setText(newText);
	}
}

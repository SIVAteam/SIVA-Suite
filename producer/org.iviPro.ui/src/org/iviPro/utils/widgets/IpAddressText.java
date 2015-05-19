package org.iviPro.utils.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * Utility class for creation of a composite which facilitates input of an IP
 * address.
 * @author John
 *
 */
public class IpAddressText extends Composite {
	
	SizedText[] parts = new SizedText[4];
	Label[] dots = new Label[3];
		
	/**
	 * Creates a composite containing four text fields entering IP addresses.
	 * The given style is applied to each of the text fields and has to be
	 * appropriate according to {@link org.eclipse.swt.widgets.Text}.
	 * @param parent parent composite
	 * @param style style of the text fields
	 */
	public IpAddressText (Composite parent, int style) {
		super(parent, SWT.NONE);

		// Defining container
		GridLayout layout = new GridLayout(7, false);
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		this.setLayout(layout);

		// Color needs not to be disposed
		Color white = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		// Defining controls
		parts[0] = new SizedText(this, style, 3);
		dots[0] = new Label(this, SWT.LEFT);
		dots[0].setText("."); //$NON-NLS-1$
		
		parts[1] = new SizedText(this, style, 3);
		dots[1] = new Label(this, SWT.LEFT);
		dots[1].setText("."); //$NON-NLS-1$
		
		parts[2] = new SizedText(this, style, 3);
		dots[2] = new Label(this, SWT.LEFT);
		dots[2].setText("."); //$NON-NLS-1$
		
		parts[3] = new SizedText(this, style, 3);
		
		setBackground(white);		
	}

	/**
	 * Returns the IP address currently stored in the widgets' text fields or 
	 * an empty string if the stored values do not represent a valid address. 
	 * @return either a string representing a valid IP address or the empty
	 * string
	 */
	public String getIpAddress() {
		String ip = ""; //$NON-NLS-1$
		if (isValidAddress()) {
			ip = parts[0].getText();
			for (int i = 1; i < parts.length; i++) {
				ip += "." + parts[i].getText(); //$NON-NLS-1$
			}
		}
		return ip;
	}

	/**
	 * Sets the content of this widgets text fields to the given IP address.
	 * Correctness of the address is assumed and is not checked.
	 * @param ip string representing an IP address
	 */
	public void setIpAddress(String ip) {
		if (ip.equals("")) { //$NON-NLS-1$
			for (SizedText part : parts) {
				part.setText(""); //$NON-NLS-1$
			}
		} else {
			String[] strings = ip.split("\\."); //$NON-NLS-1$
			for (int i = 0; i < parts.length; i++) {
				parts[i].setText(strings[i]);
			}
		}
	}

	/**
	 * Checks whether the IP address currently stored in the widgets' text
	 * fields is valid.
	 * @return true if the widget contains a valid IP address
	 */
	public boolean isValidAddress() {
		for (SizedText part : parts) {
			if (part.getText().isEmpty()) {
				return false;
			}
		}
		int firstValue = Integer.valueOf(parts[0].getText());
		if (firstValue < 1 || firstValue > 255) {
			return false;
		}
		for (int i = 1; i < parts.length; i++) {
			int value = Integer.valueOf(parts[i].getText());
			if (value < 0 || value > 255) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Return whether or not all fields of the IP address are empty.
	 * @return true if all fields are empty - false otherwise
	 */
	public boolean isEmpty() {
		for (SizedText part : parts) {
			if (!part.getText().isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Sets the background color for all components of this widget.
	 */
	public void setBackground(Color color) {
		for (SizedText part : parts) {
			part.getTextField().setBackground(color);
		}
		for (Label dot : dots) {
			dot.setBackground(color);
		}
	}
	
	/**
	 * Adds the given <code>ModifyListener</code> to the text fields of this composite.
	 */
	public void addModifyListeners(ModifyListener listener) {
		for (SizedText part : parts) {
			part.getTextField().addModifyListener(listener);
		}
	}
	
	/**
	 * Adds the given <code>VerifyListener</code> to the text fields of this composite.
	 */
	public void addVerifyListeners(VerifyListener listener) {
		for (SizedText part : parts) {
			part.getTextField().addVerifyListener(listener);
		}
	}
}

package org.iviPro.utils;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

/**
 * Listener verifying that only numeric input (digits only) may be entered in 
 * the text field adding this listener.
 * @author John
 */
public class NumericInputListener implements VerifyListener {
		
	@Override
	public void verifyText(VerifyEvent e) {
		if (!e.text.matches("\\d*")) { //$NON-NLS-1$
			e.doit = false;
			return;
		}
	}
}

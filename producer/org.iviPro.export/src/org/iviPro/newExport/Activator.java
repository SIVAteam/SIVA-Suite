package org.iviPro.newExport;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Controls the life cycle of the export plug-in.
 * 
 * @author Codebold
 * 
 */
public class Activator extends AbstractUIPlugin {

	/** The plug-in id. */
	public static final String PLUGIN_ID = "org.iviPro.export"; //$NON-NLS-1$

	/** The shared instance. */
	private static Activator plugin;

	/** Default constructor. */
	public Activator() {
	}

	/** {@inheritDoc} */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/** {@inheritDoc} */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return The shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

}

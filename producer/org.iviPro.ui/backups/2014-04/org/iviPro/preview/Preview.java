package org.iviPro.preview;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.iviPro.editors.IAbstractEditor;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.theme.Colors;

/**
 * Editorkomponete für die Vorschau
 * 
 * @author langa
 * 
 */
public class Preview extends IAbstractEditor {

	public static String ID = Preview.class.getName();
	private NodeScene scene;
	AbstractPlayer player;

	@Override
	protected void createPartControlImpl(Composite parent) {
		// parent.setLayout(new GridLayout(1, false));
		parent.setLayout(new FillLayout());
		parent.setBackground(Colors.EDITOR_BG.getColor());
		// ProjectSettings settings =
		// Application.getCurrentProject().getSettings();
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);

		// if (settings.isFullscreen()) {
		//
		// } else {
		// new WindowedPlayer(sc, SWT.BORDER, scene);
		// }

		player = new WindowedPlayer(sc, SWT.BORDER, scene);
		sc.setContent(player);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		sc.setMinSize(player.computeSize(1000, 1000));
	}

	@Override
	public void dispose() {
		player.dispose();
		super.dispose();
	}

	@Override
	public void doSave(IProgressMonitor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite arg0, IEditorInput arg1)
			throws PartInitException {
		setSite(arg0);
		setInput(arg1);

		scene = ((PreviewInput) arg1).getScene();
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}

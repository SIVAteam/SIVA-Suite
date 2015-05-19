package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.mylyn.htmltext.HtmlComposer;
import org.eclipse.mylyn.htmltext.commands.formatting.SetSizeCommand;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class SetSizeDropdownAction extends Action implements IMenuCreator {
	private Menu fMenu;

	private List<SelectSingleSizeAction> actionList;

	private final SetSizeCommand setSizeCommand;

	/**
	 * @param string
	 * @param as_drop_down_menu
	 */
	public SetSizeDropdownAction(final String string, HtmlComposer composer) {
		super(string, IAction.AS_DROP_DOWN_MENU);
		setSizeCommand = new SetSizeCommand();
		setSizeCommand.setComposer(composer);
		setSizeCommand.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				if ("sizeOfSelection".equals(evt.getPropertyName())) {
					if ("-1.0".equals(evt.getNewValue())) {

						setText(string);
					}
				}

			}
		});
		setMenuCreator(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets
	 * .Control)
	 */
	public Menu getMenu(final Control parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fMenu = new Menu(parent);
		List<SelectSingleSizeAction> actions = getActions();
		for (SelectSingleSizeAction selectSingleFormatAction : actions) {
			addActionToMenu(fMenu, selectSingleFormatAction);
		}
		return fMenu;
	}

	private List<SelectSingleSizeAction> getActions() {
		if (actionList == null) {
			actionList = new ArrayList<SetSizeDropdownAction.SelectSingleSizeAction>();
			String[] availableSizes = setSizeCommand.getAvailableSizes();
			for (String string : availableSizes) {
				SelectSingleSizeAction selectSingleSizeAction = new SelectSingleSizeAction(
						string);
				setSizeCommand
						.addPropertyChangeListener(selectSingleSizeAction);
				actionList.add(selectSingleSizeAction);
			}
		}
		return actionList;
	}

	private void addActionToMenu(final Menu parent, final Action action) {
		final ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	public Menu getMenu(final Menu parent) {
		return null;
	}

	class SelectSingleSizeAction extends Action implements
			PropertyChangeListener {

		public SelectSingleSizeAction(final String size) {
			super(size, IAction.AS_RADIO_BUTTON);
			setId(size);
		}

		@Override
		public void run() {
			setSizeCommand.setSizeToWidget(getId());
			setSizeCommand.execute();

		}

		public void propertyChange(PropertyChangeEvent evt) {
			if ("sizeOfSelection".equals(evt.getPropertyName())) {
				boolean equals = getId().equals(evt.getNewValue());
				setChecked(equals);
				if (equals) {
					SetSizeDropdownAction.this.setText(getText());
				}
			}

		}

	}

}
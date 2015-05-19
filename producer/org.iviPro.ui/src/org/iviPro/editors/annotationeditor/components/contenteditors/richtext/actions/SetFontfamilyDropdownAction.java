package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.mylyn.htmltext.HtmlComposer;
import org.eclipse.mylyn.htmltext.commands.formatting.SetFontFamilyCommand;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class SetFontfamilyDropdownAction extends Action implements IMenuCreator {
	private Menu fMenu;

	private List<SelectSingleFontfamilyAction> actionList;

	private final SetFontFamilyCommand setFontfamilyCommand;

	/**
	 * @param string
	 * @param as_drop_down_menu
	 */
	public SetFontfamilyDropdownAction(final String string,
			HtmlComposer composer) {
		super(string, IAction.AS_DROP_DOWN_MENU);
		setFontfamilyCommand = new SetFontFamilyCommand();
		setFontfamilyCommand.setComposer(composer);
		setFontfamilyCommand
				.addPropertyChangeListener(new PropertyChangeListener() {

					public void propertyChange(PropertyChangeEvent evt) {
						if ("fontfamilyOfSelection".equals(evt
								.getPropertyName())) {
							if ("".equals(evt.getNewValue().toString().trim())) {
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
		List<SelectSingleFontfamilyAction> actions = getActions();
		for (SelectSingleFontfamilyAction selectSingleFormatAction : actions) {
			addActionToMenu(fMenu, selectSingleFormatAction);					
		}
		return fMenu;
	}

	private List<SelectSingleFontfamilyAction> getActions() {
		if (actionList == null) {
			actionList = new ArrayList<SetFontfamilyDropdownAction.SelectSingleFontfamilyAction>();
			Map<String, String> availableFonts = setFontfamilyCommand
					.getAvailableFontfamilies();
			Set<String> keySet = availableFonts.keySet();
			for (String string : keySet) {
				SelectSingleFontfamilyAction selectSingleSizeAction = new SelectSingleFontfamilyAction(
						string, availableFonts.get(string));
				setFontfamilyCommand
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

	class SelectSingleFontfamilyAction extends Action implements
			PropertyChangeListener {

		private final String family;

		public SelectSingleFontfamilyAction(final String name, String family) {
			super(name, IAction.AS_RADIO_BUTTON);
			this.family = family;
			setId(name);
		}

		@Override
		public void run() {
			setFontfamilyCommand.setFontfamilyToWidget(family);
			setFontfamilyCommand.execute();
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if ("fontfamilyOfSelection".equals(evt.getPropertyName())) {
				boolean equals = family.equals(evt.getNewValue());
				setChecked(equals);
				if (equals) {
					SetFontfamilyDropdownAction.this.setText(getText());
				}
			}
		}
	}
}
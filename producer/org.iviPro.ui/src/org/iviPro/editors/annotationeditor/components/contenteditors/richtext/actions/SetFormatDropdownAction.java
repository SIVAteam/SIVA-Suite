package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.mylyn.htmltext.HtmlComposer;
import org.eclipse.mylyn.htmltext.commands.formatting.SetFormatCommand;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class SetFormatDropdownAction extends Action implements IMenuCreator {
	private Menu fMenu;

	private final Map<String, String> formatMappings = new LinkedHashMap<String, String>();

	private List<SelectSingleFormatAction> actionList;

	private final SetFormatCommand setFormatCommand;

	/**
	 * @param string
	 * @param as_drop_down_menu
	 */
	public SetFormatDropdownAction(final String string, HtmlComposer composer) {
		super("Paragraph", IAction.AS_DROP_DOWN_MENU);
		formatMappings.put("p", "Paragraph");
		formatMappings.put("adress", "Adress");
		formatMappings.put("pre", "Preformatted");
		formatMappings.put("h1", "Heading 1");
		formatMappings.put("h2", "Heading 2");
		formatMappings.put("h3", "Heading 3");
		formatMappings.put("h4", "Heading 4");
		formatMappings.put("h5", "Heading 5");
		formatMappings.put("h6", "Heading 6");
		setFormatCommand = new SetFormatCommand();
		setFormatCommand.setComposer(composer);
		setFormatCommand
				.addPropertyChangeListener(new PropertyChangeListener() {

					public void propertyChange(PropertyChangeEvent evt) {
						if ("formatOfSelection".equals(evt.getPropertyName())) {
							if ("".equals(evt.getNewValue().toString().trim())) {
								setText("Paragraph");
							}
						}
					}
				});
		getActions();
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
		List<SelectSingleFormatAction> actions = getActions();
		for (SelectSingleFormatAction selectSingleFormatAction : actions) {
			addActionToMenu(fMenu, selectSingleFormatAction);
		}
		return fMenu;
	}

	private List<SelectSingleFormatAction> getActions() {
		if (actionList == null) {
			actionList = new ArrayList<SetFormatDropdownAction.SelectSingleFormatAction>();
			Set<String> keySet = formatMappings.keySet();
			for (String string : keySet) {
				SelectSingleFormatAction selectSingleFormatAction = new SelectSingleFormatAction(
						string, formatMappings.get(string));
				setFormatCommand
						.addPropertyChangeListener(selectSingleFormatAction);
				actionList.add(selectSingleFormatAction);
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

	class SelectSingleFormatAction extends Action implements
			PropertyChangeListener {

		private final String style;

		public SelectSingleFormatAction(final String style, String description) {
			super(description, IAction.AS_RADIO_BUTTON);
			this.style = style;
			setId(this.style);
		}

		@Override
		public void run() {
			setFormatCommand.setFormatToWidget(style);
			setFormatCommand.execute();

		}

		public void propertyChange(PropertyChangeEvent evt) {
			if ("formatOfSelection".equals(evt.getPropertyName())) {				
				boolean equals = style.equals(evt.getNewValue());
				setChecked(equals);
				if (equals) {
					SetFormatDropdownAction.this.setText(getText());
				}
			}
		}
	}

}
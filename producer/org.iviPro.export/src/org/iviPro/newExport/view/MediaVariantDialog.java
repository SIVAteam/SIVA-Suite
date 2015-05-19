package org.iviPro.newExport.view;

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.jface.databinding.dialog.TitleAreaDialogSupport;
import org.eclipse.jface.databinding.dialog.ValidationMessageProvider;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.MediaVariant;
import org.iviPro.newExport.view.convert.StringTrimConverter;
import org.iviPro.newExport.view.validate.StringLengthValidator;
import org.iviPro.newExport.view.validate.StringNotEmptyValidator;
import org.iviPro.newExport.view.validate.StringProhibitedValueValidator;
import org.iviPro.newExport.view.validate.ValidatorChain;

public class MediaVariantDialog extends TitleAreaDialog {

	private static final int TITLE_MAX_LENGTH = 96;
	private static final int DESCRIPTION_MAX_LENGTH = 128;

	private final String title;
	private final String message;
	private final MediaVariant mediaVariant;
	private final List<String> titlesInUse;
	private Text titleText;
	private ControlDecoration titleDecoration;
	private Text descriptionText;
	private ControlDecoration descriptionDecoration;

	public MediaVariantDialog(Shell parentShell, String title, String message,
			MediaVariant mediaVariant, List<String> titlesInUse) {
		super(parentShell);
		this.title = title;
		this.message = message;
		this.mediaVariant = mediaVariant;
		this.titlesInUse = titlesInUse;
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(message);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite container = new Composite(parent, SWT.NULL);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(container);

		createTitleControl(container);
		createDescriptionControl(container);
		bindValues();

		return parent;
	}

	private void createTitleControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.Title);
		label.setToolTipText("The title of the variant...");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		titleText = new Text(parent, SWT.SINGLE);
		titleText
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		titleDecoration = new ControlDecoration(titleText, SWT.LEFT | SWT.TOP);
		titleDecoration.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage());
	}

	private void createDescriptionControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.Description);
		label.setToolTipText("The description of the variant");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		descriptionText = new Text(parent, SWT.SINGLE);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false));
		descriptionDecoration = new ControlDecoration(titleText, SWT.LEFT
				| SWT.TOP);
		descriptionDecoration.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage());
	}

	private void bindValues() {
		DataBindingContext ctx = new DataBindingContext();
		TitleAreaDialogSupport.create(this, ctx).setValidationMessageProvider(
				new ValidationMessageProvider() {
					@Override
					public String getMessage(
							ValidationStatusProvider statusProvider) {
						if (statusProvider == null) {
							return message;
						}
						return super.getMessage(statusProvider);
					}

					@Override
					public int getMessageType(
							ValidationStatusProvider statusProvider) {
						int type = super.getMessageType(statusProvider);
						if (getButton(IDialogConstants.OK_ID) != null) {
							getButton(IDialogConstants.OK_ID).setEnabled(
									type != IMessageProvider.ERROR);
						}
						return type;
					}
				});
		;

		IConverter trimConverter = new StringTrimConverter();
		UpdateValueStrategy titleStrategy = new UpdateValueStrategy()
				.setConverter(trimConverter).setAfterConvertValidator(
						new ValidatorChain(new StringNotEmptyValidator(
								titleDecoration, Messages.Warning_TitleEmpty),
								new StringLengthValidator(titleDecoration,
										String.format(
												Messages.Warning_TitleTooLong,
												TITLE_MAX_LENGTH),
										TITLE_MAX_LENGTH),
								new StringProhibitedValueValidator(
										titleDecoration,
										Messages.Warning_TitleAlreadyInUse,
										titlesInUse, true)));
		ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(titleText),
				BeanProperties.value(MediaVariant.class, "title").observe( //$NON-NLS-1$
						mediaVariant), titleStrategy, titleStrategy);

		ctx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(descriptionText),
				BeanProperties
						.value(MediaVariant.class, "description").observe( //$NON-NLS-1$
								mediaVariant),
				new UpdateValueStrategy()
						.setConverter(trimConverter)
						.setAfterConvertValidator(
								new StringLengthValidator(
										descriptionDecoration,
										String.format(
												Messages.Warning_DescriptionTooLong,
												DESCRIPTION_MAX_LENGTH),
										DESCRIPTION_MAX_LENGTH)), null);
	}
}

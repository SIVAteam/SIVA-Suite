package org.iviPro.newExport.view;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.GeneralConfiguration;
import org.iviPro.newExport.view.convert.StringTrimConverter;
import org.iviPro.newExport.view.validate.StringLengthValidator;
import org.iviPro.newExport.view.validate.StringNotEmptyValidator;
import org.iviPro.newExport.view.validate.StringProhibitedValueValidator;
import org.iviPro.newExport.view.validate.ValidatorChain;

public class GeneralPage extends WizardPage {

	private static final String VIDEO = "Video"; //$NON-NLS-1$
	private static final String AUDIO = "Audio"; //$NON-NLS-1$
	private static final String HTML = "HTML"; //$NON-NLS-1$
	private static final String FLASH = "Flash"; //$NON-NLS-1$
	private static final String SMIL = "SMIL"; //$NON-NLS-1$
	private static final String XML = "XML"; //$NON-NLS-1$

	private static final int TITLE_MAX_LENGTH = 96;
	private static final int DESCRIPTION_MAX_LENGTH = 128;
	private static final int DIRECTORY_MAX_LENGTH = 128;

	private static Logger logger = Logger.getLogger(GeneralPage.class);

	private GeneralConfiguration generalConfiguration;
	private final List<String> titlesInUse;

	private Text titleText;
	private ControlDecoration titleDecoration;
	private Text descriptionText;
	private ControlDecoration descriptionDecoration;
	private Button exportXmlCheckbox;
	private Button exportSmilCheckbox;
	private Button exportFlashPlayerCheckbox;
	private Button exportHtmlPlayerCheckbox;
	private Button exportAudioExtensionsCheckbox;
	private Button exportVideoExtensionsCheckbox;
	private Text descriptorDirectoryText;
	private ControlDecoration descriptorDirectoryDecoration;
	private Text imageDirectoryText;
	private ControlDecoration imageDirectoryDecoration;
	private Text richPageDirectoryText;
	private ControlDecoration richPageDirectoryDecoration;
	private Text audioDirectoryText;
	private ControlDecoration audioDirectoryDecoration;
	private Text videoDirectoryText;
	private ControlDecoration videoDirectoryDecoration;

	public GeneralPage(GeneralConfiguration generalConfiguration,
			List<String> titlesInUse) {
		super(ProfileWizard.WIZARD_TITLE);
		setTitle(Messages.GeneralPage_Title);
		setDescription(Messages.GeneralPage_Description);
		this.generalConfiguration = generalConfiguration;
		this.titlesInUse = titlesInUse;
		this.setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NULL);
		GridLayoutFactory.swtDefaults().numColumns(2).margins(10, 5)
				.spacing(20, 5).applyTo(container);

		createMetaControls(container);
		createDescriptorsControls(container);
		createPlayersControls(container);
		createExtensionsControls(container);
		createDirectoriesControls(container);

		addTitleChangeListener();
		bindValues();

		setControl(container);
	}

	protected void addTitleChangeListener() {
		titleText.addVerifyListener(new VerifyListener() {

			@Override
			public void verifyText(VerifyEvent event) {
				event.doit = true;
				String newTitle = ((Text) event.widget).getText();

				if (titlesInUse.contains(newTitle)) {
					event.doit = false;
				}
				if (newTitle.length() > 128) {
					event.doit = false;
				}
			}
		});
	}

	private void createMetaControls(Composite parent) {
		final Label titleLabel = new Label(parent, SWT.NONE);
		titleLabel.setText(Messages.Title);
		titleLabel.setToolTipText(Messages.TitleToolTip);
		titleText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		titleText
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		titleDecoration = new ControlDecoration(titleText, SWT.LEFT | SWT.TOP);
		titleDecoration.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage());

		titleText.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {
			}

			@Override
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {
				logger.debug("Enabled: " + titleText.isEnabled()); //$NON-NLS-1$
				// logger.debug("Disposed: " + titleText.isDisposed());
				// logger.debug("FocusControl: " + titleText.isFocusControl());
				// titleText.setEditable(true);
				titleDecoration.hide();
				titleDecoration.hideHover();
			}
		});

		final Label descriptionLabel = new Label(parent, SWT.NONE);
		descriptionLabel.setText(Messages.Description);
		descriptionLabel.setToolTipText(Messages.DescriptionToolTip);
		descriptionText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		descriptionDecoration = new ControlDecoration(descriptionText, SWT.LEFT
				| SWT.TOP);
		descriptionDecoration.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage());
	}

	private void createDescriptorsControls(Composite parent) {
		final Label descriptorsLabel = new Label(parent, SWT.NONE);
		descriptorsLabel.setText(Messages.Descriptors);
		descriptorsLabel.setToolTipText(Messages.DescriptorsToolTip);
		final Composite descriptorsContainer = new Composite(parent, SWT.NULL);
		descriptorsContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		descriptorsContainer.setLayout(new RowLayout());

		exportXmlCheckbox = new Button(descriptorsContainer, SWT.CHECK);
		exportXmlCheckbox.setText(XML);

		exportSmilCheckbox = new Button(descriptorsContainer, SWT.CHECK);
		exportSmilCheckbox.setText(SMIL);
	}

	private void createPlayersControls(Composite parent) {
		final Label playerLabel = new Label(parent, SWT.NONE);
		playerLabel.setText(Messages.Players);
		playerLabel.setToolTipText(Messages.PlayersToolTip);
		final Composite playersContainer = new Composite(parent, SWT.NULL);
		playersContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		playersContainer.setLayout(new RowLayout());

		exportFlashPlayerCheckbox = new Button(playersContainer, SWT.CHECK);
		exportFlashPlayerCheckbox.setText(FLASH);

		exportHtmlPlayerCheckbox = new Button(playersContainer, SWT.CHECK);
		exportHtmlPlayerCheckbox.setText(HTML);
	}

	private void createExtensionsControls(Composite parent) {
		final Label extensionsLabel = new Label(parent, SWT.NONE);
		extensionsLabel.setText(Messages.Extensions);
		extensionsLabel.setToolTipText(Messages.ExtensionsToolTip);
		final Composite extensionsContainer = new Composite(parent, SWT.NULL);
		extensionsContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		extensionsContainer.setLayout(new RowLayout());

		exportAudioExtensionsCheckbox = new Button(extensionsContainer,
				SWT.CHECK);
		exportAudioExtensionsCheckbox.setText(AUDIO);
		exportVideoExtensionsCheckbox = new Button(extensionsContainer,
				SWT.CHECK);
		exportVideoExtensionsCheckbox.setText(VIDEO);
	}

	private void createDirectoriesControls(Composite parent) {
		final Label descriptorDirectoryLabel = new Label(parent, SWT.NONE);
		descriptorDirectoryLabel.setText(Messages.DescriptorDirectory);
		descriptorDirectoryLabel
				.setToolTipText(Messages.DescriptorDirectoryToolTip);
		descriptorDirectoryText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		descriptorDirectoryText.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false));
		descriptorDirectoryDecoration = new ControlDecoration(
				descriptorDirectoryText, SWT.LEFT | SWT.TOP);
		descriptorDirectoryDecoration.setImage(FieldDecorationRegistry
				.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage());

		final Label imageDirectoryLabel = new Label(parent, SWT.NONE);
		imageDirectoryLabel.setText(Messages.ImageDirectory);
		imageDirectoryLabel.setToolTipText(Messages.ImageDirectoryToolTip);
		imageDirectoryText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		imageDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		imageDirectoryDecoration = new ControlDecoration(imageDirectoryText,
				SWT.LEFT | SWT.TOP);
		imageDirectoryDecoration.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage());

		final Label richPageDirectoryLabel = new Label(parent, SWT.NONE);
		richPageDirectoryLabel.setText(Messages.RichPageDirectory);
		richPageDirectoryLabel
				.setToolTipText(Messages.RichPageDirectoryToolTip);
		richPageDirectoryText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		richPageDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		richPageDirectoryDecoration = new ControlDecoration(
				richPageDirectoryText, SWT.LEFT | SWT.TOP);
		richPageDirectoryDecoration.setImage(FieldDecorationRegistry
				.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage());

		final Label audioDirectoryLabel = new Label(parent, SWT.NONE);
		audioDirectoryLabel.setText(Messages.AudioDirectory);
		audioDirectoryLabel.setToolTipText(Messages.AudioDirectoryToolTip);
		audioDirectoryText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		audioDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		audioDirectoryDecoration = new ControlDecoration(audioDirectoryText,
				SWT.LEFT | SWT.TOP);
		audioDirectoryDecoration.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage());

		final Label videoDirectoryLabel = new Label(parent, SWT.NONE);
		videoDirectoryLabel.setText(Messages.VideoDirectory);
		videoDirectoryLabel.setToolTipText(Messages.VideoDirectoryToolTip);
		videoDirectoryText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		videoDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		videoDirectoryDecoration = new ControlDecoration(videoDirectoryText,
				SWT.LEFT | SWT.TOP);
		videoDirectoryDecoration.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage());
	}

	private void bindValues() {
		DataBindingContext ctx = new DataBindingContext();
		WizardPageSupport.create(this, ctx);

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
				BeanProperties.value(GeneralConfiguration.class, "title") //$NON-NLS-1$
						.observe(generalConfiguration), titleStrategy,
				titleStrategy);

		ctx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(descriptionText),
				BeanProperties.value(GeneralConfiguration.class, "description") //$NON-NLS-1$
						.observe(generalConfiguration),
				new UpdateValueStrategy()
						.setConverter(trimConverter)
						.setAfterConvertValidator(
								new StringLengthValidator(
										descriptionDecoration,
										String.format(
												Messages.Warning_DescriptionTooLong,
												DESCRIPTION_MAX_LENGTH),
										DESCRIPTION_MAX_LENGTH)), null);

		ctx.bindValue(WidgetProperties.selection().observe(exportXmlCheckbox),
				BeanProperties.value(GeneralConfiguration.class, "exportXml") //$NON-NLS-1$
						.observe(generalConfiguration));
		ctx.bindValue(WidgetProperties.selection().observe(exportSmilCheckbox),
				BeanProperties.value(GeneralConfiguration.class, "exportSmil") //$NON-NLS-1$
						.observe(generalConfiguration));

		ctx.bindValue(
				WidgetProperties.selection().observe(exportFlashPlayerCheckbox),
				BeanProperties.value(GeneralConfiguration.class,
						"exportFlashPlayer").observe(generalConfiguration)); //$NON-NLS-1$
		ctx.bindValue(
				WidgetProperties.selection().observe(exportHtmlPlayerCheckbox),
				BeanProperties.value(GeneralConfiguration.class,
						"exportHtmlPlayer").observe(generalConfiguration)); //$NON-NLS-1$

		ctx.bindValue(
				WidgetProperties.selection().observe(
						exportAudioExtensionsCheckbox),
				BeanProperties.value(GeneralConfiguration.class,
						"exportAudioExtensions").observe(generalConfiguration)); //$NON-NLS-1$
		ctx.bindValue(
				WidgetProperties.selection().observe(
						exportVideoExtensionsCheckbox),
				BeanProperties.value(GeneralConfiguration.class,
						"exportVideoExtensions").observe(generalConfiguration)); //$NON-NLS-1$

		ctx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(
						descriptorDirectoryText),
				BeanProperties.value(GeneralConfiguration.class,
						"descriptorDirectory").observe(generalConfiguration), //$NON-NLS-1$
				new UpdateValueStrategy()
						.setConverter(trimConverter)
						.setAfterConvertValidator(
								new StringLengthValidator(
										descriptorDirectoryDecoration,
										String.format(
												Messages.Warning_DirectoryTooLong,
												DIRECTORY_MAX_LENGTH),
										DIRECTORY_MAX_LENGTH)), null);
		ctx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(imageDirectoryText),
				BeanProperties.value(GeneralConfiguration.class,
						"imageDirectory").observe(generalConfiguration), //$NON-NLS-1$
				new UpdateValueStrategy()
						.setConverter(trimConverter)
						.setAfterConvertValidator(
								new StringLengthValidator(
										imageDirectoryDecoration,
										String.format(
												Messages.Warning_DirectoryTooLong,
												DIRECTORY_MAX_LENGTH),
										DIRECTORY_MAX_LENGTH)), null);
		ctx.bindValue(
				WidgetProperties.text(SWT.Modify)
						.observe(richPageDirectoryText),
				BeanProperties.value(GeneralConfiguration.class,
						"richPageDirectory").observe(generalConfiguration), //$NON-NLS-1$
				new UpdateValueStrategy()
						.setConverter(trimConverter)
						.setAfterConvertValidator(
								new StringLengthValidator(
										richPageDirectoryDecoration,
										String.format(
												Messages.Warning_DirectoryTooLong,
												DIRECTORY_MAX_LENGTH),
										DIRECTORY_MAX_LENGTH)), null);
		ctx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(audioDirectoryText),
				BeanProperties.value(GeneralConfiguration.class,
						"audioDirectory").observe(generalConfiguration), //$NON-NLS-1$
				new UpdateValueStrategy()
						.setConverter(trimConverter)
						.setAfterConvertValidator(
								new StringLengthValidator(
										audioDirectoryDecoration,
										String.format(
												Messages.Warning_DirectoryTooLong,
												DIRECTORY_MAX_LENGTH),
										DIRECTORY_MAX_LENGTH)), null);
		ctx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(videoDirectoryText),
				BeanProperties.value(GeneralConfiguration.class,
						"videoDirectory").observe(generalConfiguration), //$NON-NLS-1$
				new UpdateValueStrategy()
						.setConverter(trimConverter)
						.setAfterConvertValidator(
								new StringLengthValidator(
										videoDirectoryDecoration,
										String.format(
												Messages.Warning_DirectoryTooLong,
												DIRECTORY_MAX_LENGTH),
										DIRECTORY_MAX_LENGTH)), null);
	}
}

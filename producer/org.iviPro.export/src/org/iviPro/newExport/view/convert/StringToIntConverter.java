package org.iviPro.newExport.view.convert;

import org.eclipse.core.databinding.conversion.IConverter;

public class StringToIntConverter implements IConverter {

	@Override
	public Object getFromType() {
		return String.class;
	}

	@Override
	public Object getToType() {
		return int.class;
	}

	@Override
	public Object convert(Object fromObject) {
		return Integer.valueOf(((String) fromObject)).intValue();
	}

}
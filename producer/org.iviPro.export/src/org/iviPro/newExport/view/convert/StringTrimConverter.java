package org.iviPro.newExport.view.convert;

import org.eclipse.core.databinding.conversion.IConverter;

public class StringTrimConverter implements IConverter {

	@Override
	public Object getFromType() {
		return String.class;
	}

	@Override
	public Object getToType() {
		return String.class;
	}

	@Override
	public Object convert(Object fromObject) {
		return ((String) fromObject).trim();
	}

}
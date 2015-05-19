package org.iviPro.theme;

import org.iviPro.theme.colorprovider.DefaultColorProvider;
import org.iviPro.theme.colorprovider.IColorProvider;
import org.iviPro.theme.colorprovider.SystemColorProvider;

public enum Themes {

	DEFAULT("themedef", ""), //$NON-NLS-1$ //$NON-NLS-2$
	SYSTEM("themesys", ""); //$NON-NLS-1$ //$NON-NLS-2$
	
	private String vmParam = ""; //$NON-NLS-1$
	
	// gibt den Ordner der Icons im Verzeichnis orgivipro/theme/icons an
	// da die Default Icons direkt im Icons Ordner liegen muss hier nicht unbedingt was angegeben werden
	private String iconFolder = ""; //$NON-NLS-1$
	
	private Themes(String vmParam, String iconFolder) {
		this.vmParam = vmParam;
		this.iconFolder = iconFolder;
	}
		
	public IColorProvider getColorProvider() {
		switch(this) {
			case SYSTEM: return new SystemColorProvider();
			case DEFAULT: return new DefaultColorProvider();
		}
		return new DefaultColorProvider();
	}
	
	public static Themes getThemes(String param) {
		if (param != null) {
			for (Themes theme : Themes.values()) {
				if (theme.vmParam.toLowerCase().equals(param.toLowerCase())) {
					return theme;
				}
			}
		}
		return DEFAULT;
	}
	
	public String getIconFolder() {
		return this.iconFolder;
	}
}

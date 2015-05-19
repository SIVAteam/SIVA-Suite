package org.iviPro.export.xml;

import java.util.HashMap;

/**
 * Key-Value Pair
 * h�lt zus�tzliche Parameter f�r den Export
 * @author juhoffma
 */
public class ExportParameters {

	private HashMap<String, Object> map;
	
	public ExportParameters() {
		this.map = new HashMap<String, Object>();
	}
	
	public Object getValue(String key) {
		return this.map.get(key);
	}
	
	public void addValue(String key, Object value) {
		this.map.put(key, value);
	}
}

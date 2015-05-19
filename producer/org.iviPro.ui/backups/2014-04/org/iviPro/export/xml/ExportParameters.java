package org.iviPro.export.xml;

import java.util.HashMap;

/**
 * Key-Value Pair
 * hält zusätzliche Parameter für den Export
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

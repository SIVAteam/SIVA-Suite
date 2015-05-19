package org.iviPro.newExport.xml2jsonconvertor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Create a node that contains either a String or a LinkedHashMap
 * with further JSONNodes.
 */
public class JSONNode {
	
	private String value;
	private boolean isQuotableInput = true;
	private LinkedHashMap<String, JSONNode> map;
	private ArrayList<JSONNode> list;
	
	/**
	 * Create a JSONNode with a String value. The output will be quoted.
	 * @param value contains the String value.
	 */
	public JSONNode(String value){
		this.value = value;
	}
	
	/**
	 * Create a JSONNode with a String value.
	 * @param value contains the String value.
	 * @param isQuotableInput is false if the given String should not be quoted in the output.
	 */
	public JSONNode(String value, boolean isQuotableInput){
		this.value = value;
		this.isQuotableInput = isQuotableInput;
	}
	
	/**
	 * Create a JSONNode with a LinkedHashMap containing further JSONNodes.
	 * @param map contains the LinkedHashMap.
	 */
	public JSONNode(LinkedHashMap<String, JSONNode> map){
		this.map = map;
	}
	
	/**
	 * Create a JSONNode with an ArrayList containing further JSONNodes.
	 * @param list contains the ArrayList.
	 */
	public JSONNode(ArrayList<JSONNode> list){
		this.list = list;
	}
	
	/**
	 * Return whether the current node is of type String .
	 * @return true if the current node is of type String, false otherwise.
	 */
	public boolean isValue(){
		return (this.value != null);
	}
	
	/**
	 * Return whether the current node is of type LinkedHashMap.
	 * @return true if the current node is of type LinkedHashMap, false otherwise.
	 */
	public boolean isMap(){
		return (this.map != null);
	}
	
	/**
	 * Return whether the current node is of type ArrayList.
	 * @return true if the current node is of type ArrayList, false otherwise.
	 */
	public boolean isList(){
		return (this.list != null);
	}
	
	/**
	 * Get the value of the JSONNode.
	 * @return the value of the JSONNode as String.
	 * @throws XML2JSONConvertorException 
	 */
	public String getValue() throws XML2JSONConvertorException{
		
		// Check if current node represents a value, throw an exception if not
		if(!this.isValue()){
			throw new XML2JSONConvertorException("This node does not represent a value. So you cannot use this get method.");
		}
		
		return this.value;
	}
	
	/**
	 * Get the map, the JSONNode contains.
	 * @return the LinkedHashMap, the JSONNode contains.
	 * @throws XML2JSONConvertorException 
	 */
	public LinkedHashMap<String, JSONNode> getMap() throws XML2JSONConvertorException{
		
		// Check if current node represents a LinkedHashMap, throw an exception if not
		if(!this.isMap()){
			throw new XML2JSONConvertorException("This node does not represent a LinkedHashMap. So you cannot use this get method.");
		}
		
		return this.map;
	}
	
	/**
	 * Get the list, the JSONNode contains.
	 * @return the ArrayList, the JSONNode contains.
	 * @throws XML2JSONConvertorException 
	 */
	public ArrayList<JSONNode> getList() throws XML2JSONConvertorException{
		
		// Check if current node represents an ArrayList, throw an exception if not
		if(!this.isList()){
			throw new XML2JSONConvertorException("This node does not represent an ArrayList. So you cannot use this get method.");
		}
		
		return this.list;
	}
	
	/**
	 * Add another JSONNode using the specified key to the LinkedHashMap.
	 * @param key of the new entry.
	 * @param node of the new entry.
	 * @throws XML2JSONConvertorException 
	 */
	public void appendEntry(String key, JSONNode node) throws XML2JSONConvertorException{
		
		// Check if current node represents a LinkedHashMap, throw an exception if not
		if(!this.isMap()){
			throw new XML2JSONConvertorException("This node does not represent a LinkedHashMap. So you cannot use this appendEntry method.");
		}
		
		this.map.put(key, node);
	}
	
	/**
	 * Add another JSONNode to the ArrayList at the end of the list.
	 * @param node of the new entry.
	 * @throws XML2JSONConvertorException 
	 */
	public void appendEntry(JSONNode node) throws XML2JSONConvertorException{
		
		// Check if current node represents an ArrayList, throw an exception if not
		if(!this.isList()){
			throw new XML2JSONConvertorException("This node does not represent an ArrayList. So you cannot use this appendEntry method.");
		}
				
		this.list.add(node);
	}
	
	/**
	 * Create compressed JSON String of the current JSONNode and all of its children.
	 * @return JSON String.
	 */
	public String toString(){
		return toString(-1);
	}	
		
	/**
	 * Create JSON String of the current JSONNode and all of its children and uses
	 * the specified level for indent.
	 * @param level for indent to be used in first level. Use -1 to avoid any
	 * 			obsolete spaces and to generate a compressed output.
	 * @return JSON String.
	 */
	public String toString(int level){
		StringBuilder string = new StringBuilder();
		
		// Genereate tabs for current level if not deactivated
		StringBuilder tabs = new StringBuilder();
		if(level > -1){
			for(int i = 0; i < level; i++){
				tabs.append(" ");
			}
		}
		
		// Check whether the current node is a JS Object, JS Array or JS Value to create
		// adequate output 
		if(this.isValue()){
			
			// Check if value is meant not to be quoted or if it's a numeric value to just append it
			// if so, otherwise escape value before appending it
			if(!this.isQuotableInput || JSONNode.isNumeric(this.value)){
				string.append(this.value);
			}
			else{
				string.append('"' + StringEscapeUtils.escapeJava(this.value).replace("/", "\\/").replaceAll("\\s+", " ") + '"');
			}
		}
		else if(this.isMap()){
			
			// Create JS Object with all of it's child nodes
			string.append('{');
			if(level > -1){
				string.append("\n");
			}
			for(Iterator<Entry<String, JSONNode>> it = this.map.entrySet().iterator(); it.hasNext(); ){
				
				// Append child node after getting it's String representation 
				Entry<String, JSONNode> entry = it.next();
				string.append(tabs);
				if(level > -1){
					string.append(' ');
				}
				string.append('"' + entry.getKey() + "\":");
				if(level > -1){
					string.append(' ');
				}
				string.append(entry.getValue().toString(level + ((level > -1) ? 1 : 0)));
				if(it.hasNext()){
					string.append(',');
				}
				if(level > -1){
					string.append("\n");
				}
			}
			string.append(tabs);
			string.append("}");
		}
		else{
			
			// Create JS Array with all of it's elements
			string.append('[');
			for(Iterator<JSONNode> it = this.list.iterator(); it.hasNext(); ){
				
				// Append element after getting it's String representation 
				JSONNode node = it.next();
				string.append(node.toString(level));
				if(it.hasNext()){
					string.append(",");
					if(level > -1){
						string.append(' ');
					}
				}
			}
			string.append(']');	
		}
		return string.toString();
	}
	
	/**
	 * Check if the given String represents a Integer, Double and so on.
	 * @param value contains the String that should be checked.
	 * @return true if the String represents a numeric value, false otherwise.
	 */
	private static boolean isNumeric(String value){
		try{
			
			// Check if it's a numeric value by trying to parse the String as Double
			@SuppressWarnings("unused")
			double d = Double.parseDouble(value);
		} catch(NumberFormatException e){
			return false;
		}
		return true;
	}
}

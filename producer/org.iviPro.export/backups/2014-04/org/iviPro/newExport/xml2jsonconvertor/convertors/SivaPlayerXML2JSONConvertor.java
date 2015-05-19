package org.iviPro.newExport.xml2jsonconvertor.convertors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.iviPro.newExport.xml2jsonconvertor.JSONNode;
import org.iviPro.newExport.xml2jsonconvertor.XML2JSONConvertorException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Convertor for creating a Siva Player JSON configuration file out of a Siva
 * Player XML configuration file.
 * 
 * @author Christian
 * 
 */
public class SivaPlayerXML2JSONConvertor implements IXML2JSONConvertor {

	private static String[] AVAILABLE_VIDEO_FILE_FORMATS = {"webm", "mp4", "ogg"};
	
	private Document xml;
	private JSONNode json;
	private LinkedHashMap<String, JSONNode> resources;
	private ArrayList<String> languages;
	
	@Override
	public void convert(Document xmlDocument, JSONNode rootNode) throws XML2JSONConvertorException {
		this.xml = xmlDocument;
		this.json = rootNode;
		
		this.json.appendEntry("configPath", new JSONNode("document.getElementsByTagName('script')[document.getElementsByTagName('script').length - 1].src", false));
		this.setLanguages();
		this.setResources();
		this.setCommonSettings();
		LinkedHashMap<String, Node> actions = this.getActions();
		this.setSceneNodes(actions);
		LinkedHashMap<String, Element> fileFormats = this.getFileFormats();
		this.setScenes(fileFormats);
		this.setAnnotations(actions, fileFormats);
		this.setTableOfContents(actions);
		this.setIndex();
	}

	/**
	 * Get list of nodes matching the specified selector in the XML document.
	 * @param selector contains a path to the target node separated by spaces.
	 * @return the list of nodes matching the specified selector.
	 */
	private ArrayList<Node> getDOMElements(String selector){
		ArrayList<Node> documentTree = new ArrayList<Node>();
		NodeList nodes = this.xml.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++){
			documentTree.add(nodes.item(i));
		}
		return getDOMElements(selector.split("\\s+"), documentTree, false);
	}
	
	/**
	 * Get list of nodes matching the specified selector.
	 * @param selector contains a path to the target node separated by spaces.
	 * @param subTrees contains a list of DOM sub trees wherein the search has to be performed. 
	 * @return the list of nodes matching the specified selector.
	 */
	private ArrayList<Node> getDOMElements(String selector, ArrayList<Node> subTrees){
		return getDOMElements(selector.split("\\s+"), subTrees, false);
	}
	
	/**
	 * Get list of child nodes whose parents are matching the specified selector in the XML document.
	 * @param selector contains a path to the target node separated by spaces.
	 * @return the list of child nodes whose parents are matching the specified selector.
	 */
	private ArrayList<Node> getDOMChildElements(String selector){
		ArrayList<Node> documentTree = new ArrayList<Node>();
		NodeList nodes = this.xml.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++){
			documentTree.add(nodes.item(i));
		}
		return getDOMElements(selector.split("\\s+"), documentTree, true);
	}
	
	/**
	 * Get list of child nodes whose parents are matching the specified selector.
	 * @param selector contains a path to the target node separated by spaces.
	 * @param subTrees contains a list of DOM sub trees wherein the search has to be performed. 
	 * @return the list of child nodes whose parents are matching the specified selector.
	 */
	private ArrayList<Node> getDOMChildElements(String selector, ArrayList<Node> subTrees){
		return getDOMElements(selector.split("\\s+"), subTrees, true);
	}
	
	/**
	 * Get list of nodes matching the specified selector.
	 * @param selector contains a path to the target node as an ArrayList.
	 * @param subTrees contains a list of DOM sub trees wherein the search has to be performed. 
	 * @param retrieveChildElements is true if not the element itself but its child elements
	 * 			should be retrieved.
	 * @return the list of nodes matching the specified selector.
	 */
	private ArrayList<Node> getDOMElements(String[] selector, ArrayList<Node> subTrees, boolean retrieveChildElements){
		ArrayList<Node> foundSubTrees = new ArrayList<Node>();
		
		// Iterate through sub trees and find matching elements for first selector
		for(Iterator<Node> it = subTrees.iterator(); it.hasNext(); ){
			Node node = it.next();
			if(node.getNodeType() == Node.ELEMENT_NODE){
				Element element = (Element)node;
				NodeList foundElements = element.getElementsByTagName(selector[0]);
				for(int j = 0; j < foundElements.getLength(); j++){
					foundSubTrees.add(foundElements.item(j));
				}
			}
		}
		
		// Create new selector path by removing fist element of path if there are any
		// path elements left
		if(selector.length > 1){
			String[] leftSelector = new String[selector.length - 1];
			for(int i = 1; i < selector.length; i++){
				leftSelector[i - 1] = selector[i];
			}
			return getDOMElements(leftSelector, foundSubTrees, retrieveChildElements);
		}
		
		// Check if child elements of found element should be retrieved and generate
		// a list of those elements if so
		if(retrieveChildElements){
			ArrayList<Node> childElements = new ArrayList<Node>();
			for(Iterator<Node> it = foundSubTrees.iterator(); it.hasNext(); ){
				NodeList nodes = it.next().getChildNodes();
				for(int i = 0; i < nodes.getLength(); i++){
					if(nodes.item(i).getNodeType() == Node.ELEMENT_NODE){
						childElements.add(nodes.item(i));
					}
				}
			}
			return childElements;
		}
		
		// Retrieve found elements
		return foundSubTrees;
	}

	/**
	 * Extract resources from the XML document for further usage.
	 * @throws XML2JSONConvertorException 
	 */
	private void setResources() throws XML2JSONConvertorException{
		this.resources = new LinkedHashMap<String, JSONNode>();
		
		// Extract child elements of resources tag from XML Document and throw an exception if no one exists
		ArrayList<Node> resources = this.getDOMChildElements("ressources");
		if(resources.size() == 0){
			throw new XML2JSONConvertorException("Could not find any resources in XML file.");
		}
		
		// Iterate through child elements of resources
		for(Iterator <Node> it = resources.iterator(); it.hasNext(); ){
				
			// Iterate through all existing language versions of the current resource
			Node node = it.next();
			Element resource = (Element)node;
			LinkedHashMap<String, JSONNode> languages = new LinkedHashMap<String, JSONNode>();
			ArrayList<Node> tmpTree = new ArrayList<Node>();
			tmpTree.add(node);
			ArrayList<Node> contentNodes = this.getDOMElements("content", tmpTree);
			ArrayList<String> requiredLanguages = new ArrayList<String>(this.languages);
			for(Iterator <Node> it2 = contentNodes.iterator(); it2.hasNext(); ){
					
				// Get required attributes and add them if existing to an LinkedHashMap
				Element content = (Element)it2.next();
				LinkedHashMap<String, JSONNode> map = new LinkedHashMap<String, JSONNode>();
					
				String href = content.getAttribute("href").replaceAll("Files\\\\", "/").replaceAll("\\\\", "/").replaceAll("//", "/");
				if(href.length() > 0){
					map.put("href", new JSONNode(href));
				}
					
				String text = content.getTextContent();
				if(text.length() > 0){
					map.put("content", new JSONNode(text));
				}
					
				// Remove language form list of required languages
				String language = content.getAttribute("langCode");
				requiredLanguages.remove(language);
					
				// Put the LinkedHashMap converted to a JSONNode to the languages LinkedHashMap
				languages.put(language, new JSONNode(map));
			}
			
			String key = resource.getAttribute("resID");
			
			// Check if resource is available for all required languages and throw an exception if not
			if(requiredLanguages.size() > 0){
				throw new XML2JSONConvertorException("Resource '" + key + "' is not available for the following specified languages: " + requiredLanguages.toString());
			}
			
			// Put all languages converted to a JSONNode the a LinkedHashMap entry having the resource ID as key
			this.resources.put(key, new JSONNode(languages));
		}
	}
	
	/**
	 * Get resource for the given key.
	 * @param key contains the key for the resource to be returned.
	 * @return an JSONNode containing the given resource.
	 * 			If there is no resource an empty LinkedHashMap JSONNode will be
	 *          returned.
	 */
	private JSONNode getResource(String key){
		JSONNode resource = this.resources.get(key);
		if(resource == null){
			resource = new JSONNode(new LinkedHashMap<String, JSONNode>());
		}
		return resource;
	}
	
	/**
	 * Extract language information from XML document and add them to JSON object.
	 * @throws XML2JSONConvertorException
	 */
	private void setLanguages() throws XML2JSONConvertorException{
		
		this.languages = new ArrayList<String>();
		
		// Extract default language
		ArrayList<Node> languages = this.getDOMElements("languages");
		if(languages.size() == 0){
			throw new XML2JSONConvertorException("Could not find the languages tag in XML file.");
		}
		String defaultLanguage = ((Element)languages.get(0)).getAttribute("defaultLangCode");

		// Extract available languages and check if the default language is one of them
		ArrayList<JSONNode> extractedLanguages = new ArrayList<JSONNode>();
		languages = this.getDOMElements("language", languages);
		boolean defaultLanguageFound = false;
		if(languages.size() == 0){
			throw new XML2JSONConvertorException("Could not find any language tag in XML file.");
		}
		
		for(Iterator<Node> it = languages.iterator(); it.hasNext(); ){
				Element language = (Element)it.next();
				String tmpLanguage = language.getAttribute("langCode");
				
				if(tmpLanguage.equals(defaultLanguage)){
					defaultLanguageFound = true;
				}

				extractedLanguages.add(new JSONNode(tmpLanguage));
				this.languages.add(tmpLanguage);
		}
		
		if(!defaultLanguageFound){
			throw new XML2JSONConvertorException("Could not find default language in available languages.");
		}
		
		// Add languages and default language information to JSON object
		this.json.appendEntry("languages", new JSONNode(extractedLanguages));
		this.json.appendEntry("defaultLanguage", new JSONNode(defaultLanguage));
	}
	
	/**
	 * Extract common settings like the video's title or style settings from XML document 
	 * and add them to JSON object.
	 * @throws XML2JSONConvertorException
	 */
	private void setCommonSettings() throws XML2JSONConvertorException{
		
		// Extract the title's key for the video title from the XML document
		ArrayList<Node> node = this.getDOMElements("projectInformation");
		
		// Check if there has been a title specified and add it to the JSON object as 
		// the video's title if so
		if(node.size() > 0){
			String key = ((Element)node.get(0)).getAttribute("REFresIDvideoName");
			this.json.appendEntry("videoTitle", this.getResource(key));
		}
		
		// Extract settings from XML document
		LinkedHashMap<String, JSONNode> styles = new LinkedHashMap<String, JSONNode>();
		styles.put("annotationSidebarWidth", new JSONNode("0,3"));
		styles.put("nodeSelectionSidebarWidth", new JSONNode("0,3"));
		
		// Iterate through settings and write required settings to JSON object
		ArrayList<Node> settings = this.getDOMElements("projectInformation settings");
		for(Iterator<Node> it = settings.iterator(); it.hasNext(); ){
			Element setting = (Element)it.next();
			String name = setting.getAttribute("name");
			if(name.equals("design_primarycolor")){
				styles.put("primaryColor", new JSONNode("#" + setting.getAttribute("value")));
			}
			else if(name.equals("design_secondarycolor")){
				styles.put("secondaryColor", new JSONNode("#" + setting.getAttribute("value")));
			}
			else if(name.equals("area_right_width")){
				styles.put("annotationSidebarWidth", new JSONNode(setting.getAttribute("value")));
			}
			else if(name.equals("area_left_width")){
				styles.put("nodeSelectionSidebarWidth", new JSONNode(setting.getAttribute("value")));
			}
			else if(name.equals("autoplay")){
				this.json.appendEntry("autoStart", new JSONNode(setting.getAttribute("value"), false));
			}
		}
		
		this.json.appendEntry("style", new JSONNode(styles));
		this.json.appendEntry("annotationSidebarOverlay", new JSONNode("true", false));
	}
	
	/**
	 * Extract settings from XML document for further usage.
	 * @return a LinkedHashMap containing all actions. The actions can be adressed by their ids.
	 * @throws XML2JSONConvertorException
	 */
	private LinkedHashMap<String, Node> getActions() throws XML2JSONConvertorException{
		LinkedHashMap<String, Node> actions = new LinkedHashMap<String, Node>();
		
		// Extract actions from the XML document and add them to the LinkedHashMap
		ArrayList<Node> nodes = this.getDOMChildElements("actions");
		
		if(nodes.size() == 0){
			throw new XML2JSONConvertorException("Could not find any action element.");
		}
		
		for(Iterator<Node> it = nodes.iterator(); it.hasNext(); ){
			Node node = it.next();
			String key = ((Element)node).getAttribute("actionID");
			actions.put(key, node);
		}
		
		return actions;
	}
	
	/**
	 * Extract provided file formats from XML document for further usage.
	 * @return a LinkedHashMap containing the provided file formats.
	 * @throws XML2JSONConvertorException
	 */
	private LinkedHashMap<String, Element> getFileFormats() throws XML2JSONConvertorException{
		LinkedHashMap<String, Element> fileFormats = new LinkedHashMap<String, Element>();
		
		// Extract file formats from the XML document and add them to the ArrayList
		ArrayList<Node> nodes = this.getDOMElements("projectInformation resourceSettings");
		
		if(nodes.size() == 0){
			throw new XML2JSONConvertorException("Could not find any resourceSettings element.");
		}
		
		for(Iterator<Node> it = nodes.iterator(); it.hasNext(); ){
			Element element = (Element)it.next();
			if(!element.getAttribute("videoCodec").equals("")){
				fileFormats.put(element.getAttribute("fileFormat"), element);
			}
		}
		
		return fileFormats;
	}
	
	/**
	 * Extract scene nodes from XML document and add them to JSON object.
	 * @param actions contains a LinkedHashMap with all available actions and their ids.
	 * @throws XML2JSONConvertorException
	 */
	private void setSceneNodes(LinkedHashMap<String, Node> actions) throws XML2JSONConvertorException{
		LinkedHashMap<String, JSONNode> sceneNodes = new LinkedHashMap<String, JSONNode>();
		
		// Get actions of type showSelectionControl and prepare them for usage in JSON object
		for(Entry<String, Node> action: actions.entrySet()){
			if(action.getValue().getNodeName().equals("showSelectionControl")){
				Element element = (Element)action.getValue();
				LinkedHashMap<String, JSONNode> node = new LinkedHashMap<String, JSONNode>();
				node.put("title", this.getResource(element.getAttribute("REFresID")));
				
				// Get target nodes 
				ArrayList<JSONNode> nextNodes = new ArrayList<JSONNode>();
				ArrayList<Node> tmpNode = new ArrayList<Node>();
				tmpNode.add(action.getValue());
				ArrayList<Node> controls = this.getDOMElements("controls", tmpNode);
				for(Iterator<Node> it = controls.iterator(); it.hasNext(); ){
					Element controlElement = (Element)it.next();
					LinkedHashMap<String, JSONNode> nextNode = new LinkedHashMap<String, JSONNode>();
					nextNode.put("title", this.getResource(controlElement.getAttribute("REFresID")));
					if(!element.getAttribute("timeout").equals("")){
						nextNode.put("timeout", new JSONNode(timeToFloat(element.getAttribute("timeout")) + ""));
					}
					if(controlElement.getAttribute("REFactionID").split("-")[0].equals("load")){
						nextNode.put("node", new JSONNode(((Element)actions.get(controlElement.getAttribute("REFactionID"))).getAttribute("REFsceneID")));
						nextNode.put("type", new JSONNode("scene"));
					}
					else{
						nextNode.put("node", new JSONNode(controlElement.getAttribute("REFactionID")));
						nextNode.put("type", new JSONNode("node"));
					}
					nextNodes.add(new JSONNode(nextNode));
				}
				node.put("next", new JSONNode(nextNodes));
				
				
				sceneNodes.put(action.getKey(), new JSONNode(node));
			}
		}
		
		// Add scene nodes to JSON object
		this.json.appendEntry("sceneNodes", new JSONNode(sceneNodes));
	}
	
	/**
	 * Extract scenes from XML document and add them to JSON object.
	 * @param actions contains a LinkedHashMap with all available actions and their ids.
	 * @param fileFormats contains a LinkedHashMap with all available file formats.
	 * @throws XML2JSONConvertorException
	 */
	private void setScenes(LinkedHashMap<String, Element> fileFormats) throws XML2JSONConvertorException{
		LinkedHashMap<String, JSONNode> scenes = new LinkedHashMap<String, JSONNode>();
		
		// Extract scene list element from XML document in a single first step as we need it later
		// for the start scene
		ArrayList<Node> sceneList = this.getDOMElements("sceneList");
		
		
		// Extract scenes and releated information from XML document
		int triggerCounter = 0;
		ArrayList<Node> sceneNodes = this.getDOMElements("scene", sceneList);
		if(sceneNodes.size() == 0){
			throw new XML2JSONConvertorException("Could not find any scene element.");
		}
		for(Iterator<Node> it = sceneNodes.iterator(); it.hasNext(); ){
			Node node = it.next();
			Element element = (Element)node;
			LinkedHashMap<String, JSONNode> scene = new LinkedHashMap<String, JSONNode>();
			scene.put("title", this.getResource(element.getAttribute("REFresIDname")));
			ArrayList<Node> tmpTree = new ArrayList<Node>();
			tmpTree.add(node);
			ArrayList<Node> storyBoardNodes = this.getDOMElements("storyBoard", tmpTree);
			if(storyBoardNodes.size() == 0){
				throw new XML2JSONConvertorException("Could not find any storyboard element for one or more scenes.");
			}
			Element storyBoard = (Element)storyBoardNodes.get(0);
			scene.put("next", new JSONNode(storyBoard.getAttribute("REFactionIDend").replace("load-", "")));
			ArrayList<JSONNode> files = new ArrayList<JSONNode>();
			for(int i = 0; i < AVAILABLE_VIDEO_FILE_FORMATS.length; i++){
				String format = AVAILABLE_VIDEO_FILE_FORMATS[i];
				if(fileFormats.containsKey(format)){
					LinkedHashMap<String, JSONNode> file = new LinkedHashMap<String, JSONNode>();
					file.put("url", this.getResource(element.getAttribute("REFresID")));
					file.put("format", new JSONNode(format));
					file.put("type", new JSONNode(fileFormats.get(format).getAttribute("contentType") + "; codec='" + fileFormats.get(format).getAttribute("videoCodec") + ", " + fileFormats.get(format).getAttribute("audioCodec") + "'"));
					files.add(new JSONNode(file));
				}
			}
			scene.put("files", new JSONNode(files));
			
			ArrayList<JSONNode> annotations = new ArrayList<JSONNode>();
			ArrayList<Node> triggerNodes = this.getDOMElements("trigger", storyBoardNodes);
			for(Iterator<Node> it2 = triggerNodes.iterator(); it2.hasNext(); ){
				Element trigger = (Element)it2.next();
				LinkedHashMap<String, JSONNode> annotation = new LinkedHashMap<String, JSONNode>();
				annotation.put("start", new JSONNode(timeToFloat(trigger.getAttribute("startTime")) + ""));
				annotation.put("end", new JSONNode(Math.ceil(timeToFloat(trigger.getAttribute("endTime"))) + ""));
				annotation.put("annotationId", new JSONNode(trigger.getAttribute("REFactionID")));
				annotation.put("triggerId", new JSONNode(triggerCounter + ""));
				triggerCounter++;
				annotations.add(new JSONNode(annotation));
			}
			Collections.sort(annotations, new Comparator<JSONNode>(){
				@Override
				public int compare(JSONNode a, JSONNode b){
					try{
						if(Float.parseFloat(a.getMap().get("start").getValue()) < Float.parseFloat(b.getMap().get("start").getValue()))
							return -1;
						else if(Float.parseFloat(a.getMap().get("start").getValue()) > Float.parseFloat(b.getMap().get("start").getValue()))
							return 1;
						else if(Float.parseFloat(a.getMap().get("end").getValue()) < Float.parseFloat(b.getMap().get("end").getValue()))
							return -1;
						else if(Float.parseFloat(a.getMap().get("end").getValue()) > Float.parseFloat(b.getMap().get("end").getValue()))
							return 1;
						return 0;
					}
					catch(XML2JSONConvertorException e){
						throw new AssertionError(e);
					}
				}
			});
			scene.put("annotations", new JSONNode(annotations));
			scenes.put(element.getAttribute("sceneID"), new JSONNode(scene));
		}
		
		// Add scene nodes to JSON object
		this.json.appendEntry("scenes", new JSONNode(scenes));
		
		// Add start and end scene to JSON object
		String start = ((Element)sceneList.get(0)).getAttribute("REFsceneIDstart");
		if(start.equals("")){
			throw new XML2JSONConvertorException("Could not find start scene.");
		}
		this.json.appendEntry("startScene", new JSONNode(start));
		
		ArrayList<Node> endNode = this.getDOMElements("actions endSiva");
		if(endNode.size() == 0){
			throw new XML2JSONConvertorException("Could not find any endSiva element.");
		}
		this.json.appendEntry("endScene", new JSONNode(((Element)endNode.get(0)).getAttribute("actionID")));
	}
	
	/**
	 * Extract annotations from XML document and add them to JSON object.
	 * @param actions contains a LinkedHashMap with all available actions and their ids.
	 * @param fileFormats contains a LinkedHashMap with all available file formats.
	 * @throws XML2JSONConvertorException
	 */
	private void setAnnotations(LinkedHashMap<String, Node> actions, LinkedHashMap<String, Element> fileFormats) throws XML2JSONConvertorException{

		// Extract global annotations from XML document
		ArrayList<JSONNode> globalAnnotations = new ArrayList<JSONNode>();
		ArrayList<Node> globalAnnotationNodes = this.getDOMElements("projectInformation projectRessources");
		for(Iterator<Node> it = globalAnnotationNodes.iterator(); it.hasNext(); ){
			globalAnnotations.add(new JSONNode(((Element)it.next()).getAttribute("REFactionID")));
		}
		
		// Add global annotations to JSON object
		this.json.appendEntry("globalAnnotations", new JSONNode(globalAnnotations));
		
		// Extract local annotations from XML document
		LinkedHashMap<String, JSONNode> annotations = new LinkedHashMap<String, JSONNode>();
		for(Entry<String, Node> action: actions.entrySet()){
			String type = null;
			String name = action.getValue().getNodeName();
			if(name.equals("showImage")){
				type = "image";
			}
			else if(name.equals("showRichPage")){
				type = "richText";
			}
			else if(name.equals("showImages")){
				type = "gallery";
			}
			else if(name.equals("showVideo")){
				type = "video";
			}
			else if(name.equals("showSubTitle")){
				type = "subTitle";
			}
			else if(name.equals("showMarkControl")){
				type = "marker";
			}
			else{
				continue;
			}
			Element element = (Element)action.getValue();
			LinkedHashMap<String, JSONNode> annotation = new LinkedHashMap<String, JSONNode>();
			annotation.put("id", new JSONNode(element.getAttribute("actionID")));
			annotation.put("type", new JSONNode(type));
			if(!element.getAttribute("REFresID").equals("")){
				if(type.equals("video")){
					ArrayList<JSONNode> files = new ArrayList<JSONNode>();
					for(int i = 0; i < AVAILABLE_VIDEO_FILE_FORMATS.length; i++){
						String format = AVAILABLE_VIDEO_FILE_FORMATS[i];
						if(fileFormats.containsKey(format)){
							LinkedHashMap<String, JSONNode> file = new LinkedHashMap<String, JSONNode>();
							file.put("url", this.getResource(element.getAttribute("REFresID")));
							file.put("format", new JSONNode(format));
							file.put("type", new JSONNode(fileFormats.get(format).getAttribute("contentType") + "; codec='" + fileFormats.get(format).getAttribute("videoCodec") + ", " + fileFormats.get(format).getAttribute("audioCodec")));
							file.put("extension", new JSONNode(fileFormats.get(format).getAttribute("contentType").replace("video/", "")));
							files.add(new JSONNode(file));
						}
					}
					annotation.put("files", new JSONNode(files));
				}
				else{
					annotation.put("content", this.getResource(element.getAttribute("REFresID")));
				}
			}
			
			ArrayList<Node> tmpTree = new ArrayList<Node>();
			tmpTree.add(action.getValue());
			ArrayList<Node> areas = this.getDOMElements("area", tmpTree);
			annotation.put("isSidebarAnnotation", new JSONNode((areas.size() > 0 && !((Element)areas.get(0)).getAttribute("screenArea").equals("")) + "", false));
			annotation.put("pauseVideo", new JSONNode(element.getAttribute("pauseVideo"), false));
			annotation.put("muteVideo", new JSONNode(((!element.getAttribute("muteVideo").equals("")) ? element.getAttribute("muteVideo") : "false"), false));
			ArrayList<Node> buttons = this.getDOMElements("button", tmpTree);
			if(buttons.size() > 0){
				type = "markerButton";
				annotation.put("title", this.getResource(((Element)buttons.get(0)).getAttribute("REFresID")));
			}
			ArrayList<Node> ellipses = this.getDOMElements("ellipse", tmpTree);
			if(ellipses.size() > 0){
				type = "markerEllipse";
			}
			if(type.equals("markerButton") || type.equals("markerEllipse")){
				annotation.put("type", new JSONNode(type));
				annotation.put("target", new JSONNode(element.getAttribute("REFactionID")));
				ArrayList<JSONNode> path = new ArrayList<JSONNode>();
				ArrayList<Node> pathNodes = this.getDOMElements((type.equals("markerButton") ? "buttonPath" : "ellipsePath"), tmpTree);
				for(Iterator<Node> it = pathNodes.iterator(); it.hasNext(); ){
					Element pathElement = (Element)it.next();
					LinkedHashMap<String, JSONNode> point = new LinkedHashMap<String, JSONNode>();
					point.put("start", new JSONNode(timeToFloat(pathElement.getAttribute("time")) + ""));
					point.put("top", new JSONNode(pathElement.getAttribute("yPos")));
					point.put("left", new JSONNode(pathElement.getAttribute("xPos")));
					if(!pathElement.getAttribute("lengthA").equals("")){
						point.put("width", new JSONNode(pathElement.getAttribute("lengthA")));
					}
					if(!pathElement.getAttribute("lengthB").equals("")){
						point.put("height", new JSONNode(pathElement.getAttribute("lengthB")));
					}
					path.add(new JSONNode(point));
				}
				annotation.put("path", new JSONNode(path));
			}
			else if(type.equals("subTitle")){
				ArrayList<JSONNode> path = new ArrayList<JSONNode>();
				LinkedHashMap<String, JSONNode> point = new LinkedHashMap<String, JSONNode>();
				point.put("start", new JSONNode(0 + ""));
				point.put("top", new JSONNode(0 + ""));
				point.put("left", new JSONNode(0 + ""));
				path.add(new JSONNode(point));
				annotation.put("path", new JSONNode(path));
			}
			else if(this.getDOMChildElements("path", tmpTree).size() > 0){
				ArrayList<JSONNode> path = new ArrayList<JSONNode>();
				ArrayList<Node> pathNodes = this.getDOMChildElements("path", tmpTree);
				for(Iterator<Node> it = pathNodes.iterator(); it.hasNext(); ){
					Element pathElement = (Element)it.next();
					LinkedHashMap<String, JSONNode> point = new LinkedHashMap<String, JSONNode>();
					point.put("start", new JSONNode(timeToFloat(pathElement.getAttribute("time")) + ""));
					point.put("top", new JSONNode(pathElement.getAttribute("yPos")));
					point.put("left", new JSONNode(pathElement.getAttribute("xPos")));
					if(!pathElement.getAttribute("xSize").equals("")){
						point.put("width", new JSONNode(pathElement.getAttribute("xSize")));
					}
					if(!pathElement.getAttribute("ySize").equals("")){
						point.put("height", new JSONNode(pathElement.getAttribute("ySize")));
					}
					path.add(new JSONNode(point));
				}
				annotation.put("path", new JSONNode(path));
			}
			if(annotation.containsKey("path")){
				Collections.sort(annotation.get("path").getList(), new Comparator<JSONNode>(){
					@Override
					public int compare(JSONNode a, JSONNode b){
						try{
							if(Float.parseFloat(a.getMap().get("start").getValue()) < Float.parseFloat(b.getMap().get("start").getValue()))
								return -1;
							else if(Float.parseFloat(a.getMap().get("start").getValue()) > Float.parseFloat(b.getMap().get("start").getValue()))
								return 1;
							return 0;
						}
						catch(XML2JSONConvertorException e){
							throw new AssertionError(e);
						}
					}
				});
			}
			ArrayList<Node> galleryNodes = this.getDOMChildElements("galleryRessources", tmpTree);
			if(galleryNodes.size() > 0){
				annotation.put("columns", new JSONNode(element.getAttribute("columnCount")));
				ArrayList<JSONNode> images = new ArrayList<JSONNode>();
				for(Iterator<Node> it = galleryNodes.iterator(); it.hasNext(); ){
					images.add(this.getResource(((Element)it.next()).getAttribute("REFresID")));
				}
				annotation.put("images", new JSONNode(images));
			}
			annotations.put(element.getAttribute("actionID"), new JSONNode(annotation));
		}
		
		// Add local annotations to JSON object
		this.json.appendEntry("annotations", new JSONNode(annotations));
	}
	
	/**
	 * Extract annotations from XML document and add them to JSON object.
	 * @param actions contains a LinkedHashMap with all available actions and their ids.
	 * @throws XML2JSONConvertorException
	 */
	private void setTableOfContents(LinkedHashMap<String, Node> actions) throws XML2JSONConvertorException{
		
		// Extract table of contents from XML document if defined
		ArrayList<Node> tableOfContents = this.getDOMElements("tableOfContents");
		if(tableOfContents.size() > 0){
			LinkedHashMap<String, JSONNode> table = new LinkedHashMap<String, JSONNode>();
			ArrayList<String> subNodes = new ArrayList<String>();
			table.put("title", this.getResource(((Element)tableOfContents.get(0)).getAttribute("REFresID")));
			ArrayList<Node> entryNodes = this.getDOMElements("tableOfContents contents");
			LinkedHashMap<String, JSONNode> entries = new LinkedHashMap<String, JSONNode>();
			for(Iterator<Node> it = entryNodes.iterator(); it.hasNext(); ){
				Node entryNode = it.next();
				Element element = (Element)entryNode;
				LinkedHashMap<String, JSONNode> entry = new LinkedHashMap<String, JSONNode>();
				entry.put("title", this.getResource(element.getAttribute("REFresID")));
				entry.put("isFirstLevelEntry", new JSONNode("true", false));
				if(!element.getAttribute("REFactionID").equals("") && element.getAttribute("REFactionID").split("-")[0].equals("load")){
					entry.put("target", new JSONNode(((Element)actions.get(element.getAttribute("REFactionID"))).getAttribute("REFsceneID").replace("load-", "")));
				}
				ArrayList<Node> tmpTree = new ArrayList<Node>();
				tmpTree.add(entryNode);
				ArrayList<Node> subEntryNodes = this.getDOMElements("adjacencyRefListNode", tmpTree);
				ArrayList<JSONNode> subEntries = new ArrayList<JSONNode>();
				for(Iterator<Node> it2 = subEntryNodes.iterator(); it2.hasNext(); ){
					Element subEntryElement = (Element)it2.next();
					subEntries.add(new JSONNode(subEntryElement.getAttribute("REFcontentsNodeID")));
					subNodes.add(subEntryElement.getAttribute("REFcontentsNodeID"));
				}
				entry.put("subEntries", new JSONNode(subEntries));
				entries.put(element.getAttribute("contentsNodeID"), new JSONNode(entry));
			}
			
			// Update level information for sub entries in second step
			for(Iterator<String> it = subNodes.iterator(); it.hasNext(); ){
				entries.get(it.next()).getMap().put("isFirstLevelEntry", new JSONNode("false", false));
			}
			
			table.put("entries", new JSONNode(entries));
			
			// Add table of contents to JSON object
			this.json.appendEntry("tableOfContents", new JSONNode(table));
		}
	}
	
	/**
	 * Extract index information from XML document and add it to JSON object.
	 * @throws XML2JSONConvertorException
	 */
	private void setIndex() throws XML2JSONConvertorException{
		
		LinkedHashMap<String, JSONNode> index = new LinkedHashMap<String, JSONNode>();
		
		// Extract table of contents from XML document if defined
		ArrayList<Node> keywords = this.getDOMChildElements("index");
		for(Iterator<Node> it = keywords.iterator(); it.hasNext(); ){
			Node node = it.next();
			Element element = (Element)node;
			ArrayList<JSONNode> scenes = new ArrayList<JSONNode>();
			ArrayList<Node> tmpTree = new ArrayList<Node>();
			tmpTree.add(node);
			ArrayList<Node> sceneNodes = this.getDOMElements("scene", tmpTree);
			for(Iterator<Node> it2 = sceneNodes.iterator(); it2.hasNext(); ){
				Element sceneNode = (Element)it2.next();
				LinkedHashMap<String, JSONNode> scene = new LinkedHashMap<String, JSONNode>();
				scene.put("id", new JSONNode(sceneNode.getAttribute("REFsceneID")));
				if(!sceneNode.getAttribute("REFtriggerID").equals("")){
					scene.put("annotation", new JSONNode(sceneNode.getAttribute("REFtriggerID").replace("trigger-", "show-")));
				}
				scenes.add(new JSONNode(scene));
			}
			index.put(element.getAttribute("word"), new JSONNode(scenes));
		}
			
		// Add index to JSON object
		this.json.appendEntry("index", new JSONNode(index));
	}
	
	/*
	 * Parses a time string (hh:mm:ss.ms) into a float representing seconds.
	 * @param time contains the time string.
	 * @return the float representing seconds.
	 */
	private static float timeToFloat(String time){
		String[] parts = time.split("\\.");
		float milliSeconds = 0; 
		if(parts.length > 1){
			milliSeconds = Float.parseFloat(parts[1]) / 1000;
		}
		parts = parts[0].split(":");
		float seconds = 0;
		for(int i = 0; i < parts.length; i++){
			seconds += Integer.parseInt(parts[i]) * ((i == 0) ? 3600 : ((i == 1) ? 60 : 1));
		}
		return seconds + milliSeconds;
	}
}

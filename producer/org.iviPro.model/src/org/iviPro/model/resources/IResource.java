package org.iviPro.model.resources;

import java.util.List;

import org.iviPro.model.LocalizedElement;

/**
 * IResources encapsulate the multilingual media resources used in a 
 * hypervideo. For consistency purposes, all resources defined by the XML 
 * scheme should be implemented as IResources.
 * 
 * @author John
 *
 */
public interface IResource {
	public List<LocalizedElement> getLocalizedContents();
}

/**
 * 
 */
package org.pathvisio.complexviz;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * @author anwesha
 *
 */
public class ComplexUtilities {
	
	private Map<String, Color> COMPLEX_COLOUR_MAP;
	private Map<String, Color> COMPLEX_BORDER_COLOUR_MAP;
	
	ComplexUtilities(){
		COMPLEX_COLOUR_MAP = new HashMap<String, Color>();
		COMPLEX_BORDER_COLOUR_MAP = new HashMap<String, Color>();
	}
	
	public void setComplexColours(){
		COMPLEX_COLOUR_MAP.put("REACT_150924", Color.RED);
		COMPLEX_COLOUR_MAP.put("REACT_152489", Color.BLUE);
	}
	
public Map<String, Color> getComplexColours(){
	return COMPLEX_COLOUR_MAP;
	}

	public void setComplexBorderColours(){
		COMPLEX_BORDER_COLOUR_MAP.put("REACT_150924", Color.RED);
		COMPLEX_BORDER_COLOUR_MAP.put("REACT_152489", Color.BLUE);
	}
	
public Map<String, Color> getComplexBorderColours(){
		return COMPLEX_BORDER_COLOUR_MAP;
	}
}

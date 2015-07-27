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
public class ComplexColours {
	
	private static final Color DEFAULT_COLOR_RULE_MET = Color.BLUE;
	private static final Color DEFAULT_COLOR_RULE_NOT_MET = Color.GRAY;
	public static Color COLOR_RULE_MET;
	public static Color COLOR_RULE_NOT_MET;
	private Map<String, Color> COMPLEX_COLOUR_MAP;
	private Map<String, Color> COMPLEX_BORDER_COLOUR_MAP;
	
	ComplexColours(){
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

public void setComplexRuleColour(Color rc, Color rnc){
	if(rc != null){
		COLOR_RULE_MET = rc;	
	}
	else{
		COLOR_RULE_MET = DEFAULT_COLOR_RULE_MET;
	}
	if(rnc != null){
		COLOR_RULE_NOT_MET = rnc;
	}
	else{
		COLOR_RULE_NOT_MET = DEFAULT_COLOR_RULE_NOT_MET;
	}
}

}

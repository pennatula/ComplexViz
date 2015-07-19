// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.pathvisio.complexviz.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bridgedb.Xref;
import org.pathvisio.core.data.XrefWithSymbol;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.util.PathwayParser;
import org.pathvisio.core.util.PathwayParser.ParseException;
import org.xml.sax.XMLReader;

/**
 * A collection of pathways, parsed quickly using PathwayParser
 */
public class ComplexMap {
	public static class ComplexInfo {
		private Set<Xref> complexIdComponentRefMap;
		private String complexid;
		
		public String getComplexId() {
			return complexid;
		}

		public Set<Xref> getSrcRefs() {
			return complexIdComponentRefMap;
		}
	}
	private Set<String> complexidset;
	private final List<ComplexInfo> complexes = new ArrayList<ComplexInfo>();

	private final XMLReader xmlReader = null;

	/**
	 * @param pwFile
	 *            directory with pathway files. All complexes are read
	 *            recursively
	 * 
	 */
	public ComplexMap(File pwFile) {
		complexidset = new HashSet<String>();
		final Pathway pwy = new Pathway();
		try {
			pwy.readFromXml(pwFile, true);
			final Set<Xref> componentrefs = new HashSet<Xref>();
			final Set<String> comidset = new HashSet<String>();

			for (PathwayElement pwe : pwy.getDataObjects()) {
				if(pwe.getObjectType() == ObjectType.DATANODE)
				{
					if(pwe.getDataNodeType().equalsIgnoreCase("complex")){
						comidset.add(pwe.getElementID());
						}
				}
			}
			for(String comid : comidset){
				for (PathwayElement pwe : pwy.getDataObjects()) {
					if(pwe.getObjectType() == ObjectType.DATANODE)
					{
						if(pwe.getDynamicProperty("complex_id").equalsIgnoreCase(comid)){
							componentrefs.add(pwe.getXref());
							}
					}
				}
				System.out.println("ComplexInfo");
				final ComplexInfo ci = new ComplexInfo();
				ci.complexid = comid;
				ci.complexIdComponentRefMap = componentrefs;
				getComplexes().add(ci);
			
		} 
		}catch (final ConverterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	
	public List<ComplexInfo> getComplexes() {
		return complexes;
	}

	public Set<Xref> getComponentRefs() {
		final Set<Xref> result = new HashSet<Xref>();
		for (final ComplexInfo pi : getComplexes()) {
			result.addAll(pi.complexIdComponentRefMap);
		}
		return result;
	}
}

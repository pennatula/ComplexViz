// ComplexViz Plugin for PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2015 BiGCaT Bioinformatics
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

import org.pathvisio.desktop.util.PropertyColumn;

/**
 * Enum for possible columns in the statistics result table
 */
public enum Column implements PropertyColumn
{
	X("X"),
	Y("Y"),
	COMPLEX_ID("Identifier"),
	COMPLEX_NAME("Complex"),
	PERCENT ("Percent");

	String title;

	private Column(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}
}
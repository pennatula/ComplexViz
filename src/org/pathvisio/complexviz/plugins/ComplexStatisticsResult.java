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

import org.pathvisio.desktop.util.RowWithProperties;

/**
 * Statistics calculation for a single pathway,
 * to be shown as a row in the statistics result table
 */
public class ComplexStatisticsResult implements RowWithProperties<Column>
{
	private int r = 0;
	private int total = 0;
	private String name;
	private String id;
	private double z = 0;
	
	ComplexStatisticsResult (String name, String id, int r, int total, double z)
	{
		this.r = r;
		this.total = total;
		this.name = name;
		this.id = id;
		this.z = z;
	}

	public String getProperty(Column prop)
	{
		switch (prop)
		{
		case R: return "" + r;
		case TOTAL: return "" + total;
		case COMPLEX_NAME: return name;
		case COMPLEX_ID: return id;
		case PERCENT: return String.format ("%3.2f", (float)z);
		default : throw new IllegalArgumentException("Unknown property");
		}
	}

	public double getZScore()
	{
		return z;
	}
}
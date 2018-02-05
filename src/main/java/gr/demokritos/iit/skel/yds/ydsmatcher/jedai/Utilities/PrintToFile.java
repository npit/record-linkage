/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    Copyright (C) 2015 George Antony Papadakis (gpapadis@yahoo.gr)
 */
package Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import DataModel.EquivalenceCluster;

/**
 *
 * @author gap2
 */
public class PrintToFile {

    public static void toCSV(List<EquivalenceCluster> entityClusters, String filename) throws FileNotFoundException {
    	PrintWriter pw = new PrintWriter(new File(filename));
        StringBuilder sb = new StringBuilder();
        
    	for (EquivalenceCluster eqc : entityClusters)
        {
    		if (eqc.getEntityIdsD1().isEmpty()) continue;
        	for (int ent : eqc.getEntityIdsD1())
        	{
        		sb.append(ent);
                sb.append(',');
        	}
        	sb.deleteCharAt(sb.length()-1);
            sb.append('\n');
    		if (eqc.getEntityIdsD2().isEmpty()) continue;
        	sb.deleteCharAt(sb.length()-1);
        	for (int ent : eqc.getEntityIdsD2())
        	{
        		sb.append(ent);
                sb.append(',');
        	}
        	sb.deleteCharAt(sb.length()-1);
            sb.append('\n');
        }
    	pw.write(sb.toString());
        pw.close();
    }
}

package edu.ucsf.PercolateValuesThroughGraph;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.collect.HashBasedTable;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class PercolateValuesThroughGraphLauncher{

	public static void main(String rgsArgs[]) {
		
		//arg1 = arguments
		//datValues = values to percolate
		//datGraph = list of edges to consider
		//sValueHeader = value header
		//sVertexHeader = key header
		//sValueToPercolate = value to percolate
		//sCategoryHeader = category header
		//lstOut = output
		//pcg1 = percolation graph
		//tbl1 = table from categories (rows), vertices (columns) to values
		//sValue = current value
		//sCateogry = current category
		//sVertex = current vertex
		//setVertices = set of vertices to consider
		
		HashSet<String> setVertices;
		String sValue;
		PercolationGraph pcg1;
		ArrayList<String> lstOut;
		ArgumentIO arg1;
		DataIO datValues;
		DataIO datGraph;
		String sValueHeader;
		String sVertexHeader;
		String sCategoryHeader;
		String sValueToPercolate;
		HashBasedTable<String,String,String> tbl1;
		String sCategory;
		String sVertex;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		datValues = new DataIO(arg1.getValueString("sValuesPath"));
		datGraph = new DataIO(arg1.getValueString("sGraphPath"));
		sValueHeader = arg1.getValueString("sValueHeader");
		sVertexHeader = arg1.getValueString("sVertexHeader");
		sCategoryHeader = arg1.getValueString("sCategoryHeader");
		sValueToPercolate = arg1.getValueString("sValueToPercolate");
		
		
		
		//loading percolation graph
		pcg1 = new PercolationGraph(datGraph.getStringColumn(sVertexHeader + "_1"), datGraph.getStringColumn(sVertexHeader + "_2"));
	
		//loading set of vertices to consider
		setVertices = new HashSet<String>(datValues.getStringColumn(sVertexHeader));
		
		//loading table
		tbl1 = HashBasedTable.create(1000, 5000);
		for(int i=1;i<datValues.iRows;i++) {
			sValue = datValues.getString(i,sValueHeader);
			sCategory = datValues.getString(i,sCategoryHeader);
			sVertex = datValues.getString(i,sVertexHeader);
			if(sValue.equals(sValueToPercolate)) {
				tbl1.put(sCategory,sVertex,sValue);
				for(String s:pcg1.neighbors(sVertex)) {
					if(setVertices.contains(s)) {
						tbl1.put(sCategory,s,sValue);
					}
				}
			}else {
				if(!tbl1.contains(sCategory,sVertex)) {
					tbl1.put(sCategory,sVertex,sValue);
				}
			}
			
		}
		
		//outputting results
		lstOut = new ArrayList<String>(tbl1.rowKeySet().size()*tbl1.columnKeySet().size()+1);
		lstOut.add(sCategoryHeader + "," + sVertexHeader + "," + sValueHeader);
		for(String s:tbl1.rowKeySet()) {
			for(String t:tbl1.columnKeySet()) {
				lstOut.add(s + "," + t + "," + tbl1.get(s,t));
			}
		}
		
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
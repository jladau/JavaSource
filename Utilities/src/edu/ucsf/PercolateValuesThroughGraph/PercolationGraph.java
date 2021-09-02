package edu.ucsf.PercolateValuesThroughGraph;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.collect.HashMultimap;

public class PercolationGraph{

	/**Map from each vertex id to its neighbors**/
	private HashMultimap<String,String> mapNeighbors;
	
	public PercolationGraph(ArrayList<String> lstVertices1, ArrayList<String> lstVertices2) {
		
		mapNeighbors = HashMultimap.create(lstVertices1.size(),lstVertices1.size());
		for(int i=0;i<lstVertices1.size();i++) {
			mapNeighbors.put(lstVertices1.get(i),lstVertices2.get(i));
			mapNeighbors.put(lstVertices2.get(i),lstVertices1.get(i));
		}
	}
	
	public HashSet<String> neighbors(String sVertex){
		if(mapNeighbors.containsKey(sVertex)) {
			return new HashSet<String>(mapNeighbors.get(sVertex));
		}else {
			return new HashSet<String>();
		}
	}
}

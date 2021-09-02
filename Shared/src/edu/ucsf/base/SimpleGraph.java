package edu.ucsf.base;

import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("rawtypes")
public class SimpleGraph<T extends Comparable> {

	/**Set of vertices**/
	public HashSet<T> setVertices;
	
	/**Set of edges**/
	public HashSet<SemiOrderedPair<T>> setEdges;
	
	public SimpleGraph(){
		setEdges = new HashSet<SemiOrderedPair<T>>();
		setVertices = new HashSet<T>();
	}

	/**
	 * Orders edges in graph
	 * @param mapVertexOrder Ordering for edges: edges will go from lower to higher vertices
	 */
	public void orderGraph(HashMap<T,Integer> mapOrder){
		
		//gphOut = output
	
		SimpleGraph<T> gphOut;
		
		//loading set of edges
		gphOut = new SimpleGraph<T>();
		
		//updating edges
		for(SemiOrderedPair<T> sop1:this.setEdges){
			if(mapOrder.get(sop1.o1)<mapOrder.get(sop1.o2)){
				gphOut.addEdge(new SemiOrderedPair<T>(sop1.o1, sop1.o2));
			}else{
				gphOut.addEdge(new SemiOrderedPair<T>(sop1.o2, sop1.o1));
			}
		}
		setEdges=gphOut.setEdges;
		setVertices=gphOut.setVertices;
	}
	
	/**
	 * Adds an edge
	 * @param sopEdge Edge
	 */
	public void addEdge(SemiOrderedPair<T> sopEdge){
		if(!setVertices.contains(sopEdge.o1)){
			setVertices.add(sopEdge.o1);
		}
		if(!setVertices.contains(sopEdge.o2)){
			setVertices.add(sopEdge.o2);
		}
		setEdges.add(sopEdge);
	}
	
	/**
	 * Adds an edge
	 * @param sopEdge Edge
	 */
	public void removeEdge(SemiOrderedPair<T> sopEdge) throws Exception{
		if(setEdges.contains(sopEdge)){
			setEdges.remove(sopEdge);
		}
	}
	
	/**
	 * Removes all edges from graph.
	 */
	public void removeAllEdges(){
		setEdges = new HashSet<SemiOrderedPair<T>>(setVertices.size()*10+1); 
	}
}

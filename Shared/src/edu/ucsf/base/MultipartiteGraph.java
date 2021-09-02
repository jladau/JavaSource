package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import com.google.common.collect.HashMultimap;

/**
 * Code for implementing a k-partite graph
 * @author jladau
 */
@SuppressWarnings("rawtypes")
public class MultipartiteGraph<T extends Comparable> extends SimpleGraph<T>{

	/**For random subsets: gives list of vertices in same independent set as given vertex**/
	private HashMultimap<T,T> mapSameSet;
	
	/**For random subsets: gives list of vertices in different independent set as given vertex**/
	//private HashMultimap<T,T> mapDifferentSet;
	
	/**For random subsets: ordered list of vertices**/
	private ArrayList<T> lstVertices;
	
	/**For random subsets: edges in complete multipartite graph**/
	private int iEdgesComplete;
	
	/**For random subsets: edges in complement of complete multipartite graph**/
	private int iEdgesCompleteComplement;
	
	public MultipartiteGraph(){
		
	}
	
	/**
	 * Constructor
	 * @param mapIndependentSet Maps independent set IDs to elements of independent sets
	 */
	public MultipartiteGraph(HashMultimap<String, T> mapIndependentSets){
		
		super();
		
		//loading set of vertices
		setVertices = new HashSet<T>(mapIndependentSets.values().size());
		for(T t:mapIndependentSets.values()){
			setVertices.add(t);
		}
		
		//initializing set of edges
		setEdges = new HashSet<SemiOrderedPair<T>>(setVertices.size()*setVertices.size()/2);
		
		//loading same and different set maps
		mapSameSet = HashMultimap.create(setVertices.size(), setVertices.size());
		for(String s:mapIndependentSets.keySet()){
			for(T t1:mapIndependentSets.get(s)){
				for(T t2:mapIndependentSets.get(s)){
					if(!t1.equals(t2)){
						mapSameSet.put(t1, t2);
					}
				}
			}
		}
		//mapDifferentSet = HashMultimap.create(setVertices.size(), 10);
		//for(String s1:mapIndependentSets.keySet()){
		//	for(T t1:mapIndependentSets.get(s1)){
		//		for(String s2:mapIndependentSets.keySet()){
		//			if(!s1.equals(s2)){
		//				for(T t2:mapIndependentSets.get(s2)){
		//					mapDifferentSet.put(t1, t2);
		//				}
		//			}
		//		}
		//	}
		//}
		
		//loading ordered list of vertices
		lstVertices = new ArrayList<T>(setVertices.size());
		for(T t:setVertices){
			lstVertices.add(t);
		}
		
		//loading maximum number of edges
		iEdgesComplete = 0;
		for(String s1:mapIndependentSets.keySet()){
			for(String s2:mapIndependentSets.keySet()){
				if(!s1.equals(s2)){
					iEdgesComplete+=mapIndependentSets.get(s1).size()*mapIndependentSets.get(s2).size();
				}
			}
		}
		iEdgesComplete = iEdgesComplete/2;
		iEdgesCompleteComplement = 0;
		for(String s1:mapIndependentSets.keySet()){
			iEdgesCompleteComplement+=mapIndependentSets.get(s1).size()*(mapIndependentSets.get(s1).size()-1)/2;
		}
	}
	
	
	/**
	 * Loads a random subgraph of complete multipartite graph
	 * @param iEdges Number of edges in random subgraph
	 * @param iRandomSeed Random seed
	 * @throws Exception Thrown if too many edges requested
	 */
	public void loadRandomSubsetOfCompleteGraph(int iEdges, int iRandomSeed) throws Exception{
		
		//t1 = current first vertex
		//t2 = current second vertex
		//sop1 = current candidate edge
		//rnd1 = random number generator
		
		Random rnd1;
		T t1;
		T t2;
		SemiOrderedPair<T> sop1;
		
		//removing all edges
		removeAllEdges();
		setEdges = new HashSet<SemiOrderedPair<T>>(iEdges); 
		
		//checking that a reasonable number of edges has been selected
		if(iEdges>iEdgesComplete){
			loadCompleteGraph();
			return;
		}
		
		//initializing random number generator
		rnd1 = new Random(iRandomSeed);
		
		//creating edges
		for(int i=0;i<iEdges;i++){
			do{	
				do{
					t1 = lstVertices.get(rnd1.nextInt(lstVertices.size()));
					t2 = lstVertices.get(rnd1.nextInt(lstVertices.size()));
				}while( t1.equals(t2) || mapSameSet.get(t1).contains(t2));
				sop1 = new SemiOrderedPair<T>(t1,t2);
			}while(setEdges.contains(sop1));
			setEdges.add(sop1);
		}
	}
	
	/**
	 * Loads a random subgraph of complement of complete multipartite graph
	 * @param iEdges Number of edges in random subgraph
	 * @param iRandomSeed Random seed
	 * @throws Exception Thrown if too many edges requested
	 */
	public void loadRandomSubsetOfCompleteGraphComplement(int iEdges, int iRandomSeed){
		
		//t1 = current first vertex
		//t2 = current second vertex
		//sop1 = current candidate edge
		//rnd1 = random number generator
		
		Random rnd1;
		T t1;
		T t2;
		SemiOrderedPair<T> sop1;
		
		//removing all edges
		removeAllEdges();
		setEdges = new HashSet<SemiOrderedPair<T>>(iEdges); 
		
		//checking that a reasonable number of edges has been selected
		if(iEdges>iEdgesCompleteComplement){
			loadCompleteGraphComplement();
			return;
		}
		
		//initializing random number generator
		rnd1 = new Random(iRandomSeed);
		
		//creating edges
		for(int i=0;i<iEdges;i++){
			do{	
				do{
					t1 = lstVertices.get(rnd1.nextInt(lstVertices.size()));
					t2 = lstVertices.get(rnd1.nextInt(lstVertices.size()));
				}while( t1.equals(t2) || !mapSameSet.get(t1).contains(t2));
				sop1 = new SemiOrderedPair<T>(t1,t2);
			}while(setEdges.contains(sop1));
			setEdges.add(sop1);
		}
	}
	
	private void loadCompleteGraphComplement(){
		
		//removing all edges
		removeAllEdges();
		setEdges = new HashSet<SemiOrderedPair<T>>(iEdgesCompleteComplement); 
		for(T t1:mapSameSet.keySet()){
			for(T t2:mapSameSet.get(t1)){
				setEdges.add(new SemiOrderedPair<T>(t1,t2));
			}
		}
	}
	
	/**
	 * Loads complete multipartite graph.
	 */
	public void loadCompleteGraph(){
		
		//removing all edges
		removeAllEdges();
		setEdges = new HashSet<SemiOrderedPair<T>>(this.iEdgesComplete); 
		
		//creating edges
		for(T t1:setVertices){
			for(T t2:setVertices){
				if(!t1.equals(t2) && !mapSameSet.get(t1).contains(t2)){
					setEdges.add(new SemiOrderedPair<T>(t1,t2));
				}
			}
		}
	}
}

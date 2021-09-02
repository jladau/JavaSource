package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;

import edu.ucsf.base.Graph;
import edu.ucsf.base.Property;

/**
 * This code is for a mathematical graph object
 * @author jladau
 */

public class Graph extends Property{

	//tblEdge(iVertexStart,iVertexEnd) = returns edge spanning two vertices
	//mapVertex(iVertexID) = returns vertex
	//mamEdges(iVertexID) = returns set of edges that include specified vertex
	
	private HashMap<Integer,GraphVertex> mapVertex;
	private HashBasedTable<Integer,Integer,GraphEdge> tblEdge;
	private HashMultimap<Integer,GraphEdge> mamEdges;
	
	/**
	 * Constructor
	 */
	public Graph(){
		mapVertex = new HashMap<Integer,GraphVertex>(5000);
		tblEdge = HashBasedTable.create(5000,5000);
		mamEdges = HashMultimap.create(5000,5000);
	}
	
	/**
	 * Adds edge
	 */	
	public void addEdge(GraphEdge edg1){
		//mapEdge.put(edg1.iID, edg1);
		tblEdge.put(edg1.vtxStart.iID,edg1.vtxEnd.iID, edg1);
		if(!mapVertex.containsKey(edg1.vtxStart.iID)){
			mapVertex.put(edg1.vtxStart.iID, edg1.vtxStart);
		}
		if(!mapVertex.containsKey(edg1.vtxEnd.iID)){		
			mapVertex.put(edg1.vtxEnd.iID, edg1.vtxEnd);
		}
		
		//updating list of edges containing vertices
		mamEdges.get(edg1.vtxStart.iID).add(edg1);
		mamEdges.get(edg1.vtxEnd.iID).add(edg1);
	}
	
	public void addVertex(GraphVertex vtx1){
		mapVertex.put(vtx1.iID, vtx1);
	}
	
	/**
	 * Clone method
	 */
	public Graph clone(){
		
		//gphOut = output
		
		Graph gphOut;
		
		gphOut = new Graph();
		
		gphOut.mapVertex=this.mapVertex;
		//gphOut.mapEdge=this.mapEdge;
		gphOut.tblEdge=this.tblEdge;
		//gphOut.mapEdges=this.mapEdges;
		gphOut.mamEdges=this.mamEdges;
		
		gphOut.mapPropertyDouble=this.mapPropertyDouble;
		gphOut.mapPropertyString=this.mapPropertyString;
		gphOut.mapPropertyTime=this.mapPropertyTime;
		gphOut.mapPropertyBoolean=this.mapPropertyBoolean;
		
		return gphOut;
	}
	
	public boolean containsEdge(int iVertexIDStart, int iVertexIDEnd){
		if(tblEdge.contains(iVertexIDStart, iVertexIDEnd)){
			return true;
		}else{
			return false;
		}
	}
	
	public GraphEdge getEdge(int iVertexIDStart, int iVertexIDEnd){
		return tblEdge.get(iVertexIDStart,iVertexIDEnd);
	}
	
	/**
	 * Gets list of edge properties
	 */
	public ArrayList<Double> getEdgeProperties(String sProperty){

		//lstOut = output
		
		ArrayList<Double> lstOut;
		
		//compiling and saving list of properties
		lstOut = new ArrayList<Double>(tblEdge.size());
		for(GraphEdge edg1:tblEdge.values()){
			lstOut.add(edg1.getDouble(sProperty));
		}
		return lstOut;
	}
	
	public Collection<GraphEdge> getEdges(){
		return tblEdge.values();
	}
	
	/**
	 * Returns list of edges containing specified vertex
	 * @param iVertexID Vertex ID
	 */
	public Set<GraphEdge> getEdges(int iVertexID){
		return mamEdges.get(iVertexID);
	}
	
	public GraphVertex getVertex(int iVertexID){
		return mapVertex.get(iVertexID);
	}
	
	/**
	 * Adds a property value to a given vertex
	 */
	public void putProperty(int iVertexID, String sProperty, String sValue){
		mapVertex.get(iVertexID).put(sProperty, sValue);
	}
	
	/**
	 * Gets list of vertex properties
	 */
	public ArrayList<Double> getVertexProperties(String sProperty){

		//lstOut = output
		
		ArrayList<Double> lstOut;
		
		//compiling and saving list of properties
		lstOut = new ArrayList<Double>(mapVertex.size());
		for(GraphVertex vtx1:mapVertex.values()){
			lstOut.add(vtx1.getDouble(sProperty));
		}
		return lstOut;
	}

	public Collection<GraphVertex> getVertices(){
		return mapVertex.values();
	}
	
	public ArrayList<Integer> getVertexIDs(){
		
		//lst1 = output
		
		ArrayList<Integer> lst1;
		
		lst1 = new ArrayList<Integer>(getVertices().size());
		for(GraphVertex vtx1:getVertices()){
			lst1.add(vtx1.iID);
		}
		return lst1;
	}
	
	public ArrayList<String> getVertexNames(){
		
		//lst1 = output
		
		ArrayList<String> lst1;
		
		lst1 = new ArrayList<String>(getVertices().size());
		for(GraphVertex vtx1:getVertices()){
			lst1.add(vtx1.sName);
		}
		return lst1;
	}
	
	
	public int order(){
		return mapVertex.size();
	}

	/**
	 * Removes graph edge
	 * @param iEdgeID id of edge to be removed
	 */
	public void removeEdge(int iVertexStartID, int iVertexEndID){
		
		//updating list of edges
		mamEdges.remove(iVertexStartID,getEdge(iVertexStartID,iVertexEndID));
		mamEdges.remove(iVertexEndID,getEdge(iVertexStartID,iVertexEndID));
		
		//updating list of children and spanning edge list
		tblEdge.remove(iVertexStartID,iVertexEndID);
	}

	public int size(){
		return tblEdge.size();
	}

	//need to be able to get
	//(1) get list of edges containing vertex
	//(2) properties of edge spanning a pair of vertices
	//(3) properties of a specified vertex
	//(4) properties of edges obtained from 1
	//Property access must be fast, concurrent for access by specifying a pair of vertices and access via a single vertex.
	
	//mamEdges(iVertex) = returns set of edges for given vertex
	//tblEdge(iVertexStart,iVertexEnd) = returns edge spanning two vertices
	//mapVertex(iVertexID) = returns vertex
	//tblEdge(iVertexIDStart,iVertexIDEnd) = returns edge spanning two vertices
	//mamEdges(iVertexID) = returns set of edges that include specified vertex
	
	public class GraphEdge extends Property{
	
		//vtxStart = starting vertex
		//vtxEnd = ending vertex
		//mapPropertyDouble(sProperty) = returns double property
		
		public GraphVertex vtxStart;
		public GraphVertex vtxEnd;
		
		/**
		 * Constructor
		 * @param iID
		 */
		public GraphEdge(GraphVertex vtxStart, GraphVertex vtxEnd){
			this.vtxStart=vtxStart;
			this.vtxEnd=vtxEnd;
		}
		
		public boolean equals(Object o1){
			
			//edg1 = current edge
			
			GraphEdge edg1;
			
			if(o1 instanceof GraphEdge){
				edg1 = (GraphEdge) o1;
				if(edg1.vtxStart!=vtxStart){
					return false;
				}
				if(edg1.vtxEnd!=vtxEnd){
					return false;
				}
				return true;
			}else{
				return false;
			}
		}
		
		public int hashCode(){
			return 7*this.vtxStart.hashCode()+17*this.vtxEnd.hashCode();
		}
		
		public String toString(){
			return vtxStart + "-->" + vtxEnd;
		}
		
	}

	//need to be able to get
	//(1) get list of edges containing vertex
	//(2) properties of edge spanning a pair of vertices
	//(3) properties of a specified vertex
	//(4) properties of edges obtained from 1
	//Property access must be fast, concurrent for access by specifying a pair of vertices and access via a single vertex.
	
	//mamEdges(iVertex) = returns set of edges for given vertex
	//tblEdge(iVertexStart,iVertexEnd) = returns edge spanning two vertices
	//mapVertex(iVertexID) = returns vertex
	//tblEdge(iVertexIDStart,iVertexIDEnd) = returns edge spanning two vertices
	//mamEdges(iVertexID) = returns set of edges that include specified vertex
	
	public class GraphVertex extends Property{
	
		//iID = returns id for vertex
		//sName = returns name for vertex
		
		public int iID;
		public String sName;
		
		/**
		 * Constructor
		 */
		public GraphVertex(int iID, String sName){	
			this.iID = iID;
			this.sName = sName;
		}
		
		public boolean equals(Object o1){
			
			//vtx1 = current vertex
			
			GraphVertex vtx1;
			
			if(o1 instanceof GraphVertex){
				vtx1 = (GraphVertex) o1;
				if(vtx1.iID!=iID){
					return false;
				}
				if(!vtx1.sName.equals(sName)){
					return false;
				}
				return true;
			}else{
				return false;
			}
		}
		
		public int hashCode(){
			return toString().hashCode();
		}
		
		
		public String toString(){
			return "vertex" + iID + ":" + sName;
		}
		
	}
}
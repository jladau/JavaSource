package edu.ucsf.base;

import java.util.ArrayList;
import java.util.HashMap;
import com.google.common.collect.HashMultimap;
import edu.ucsf.io.DataIO;

public class Tree extends Graph{

	/**Root**/
	private GraphVertex vtxRoot;
	
	/**Returns child vertices for given vertex**/
	private HashMap<Integer,Integer> mapParent;
	
	/**Returns parent vertex for given vertex**/
	private HashMultimap<Integer,Integer> mapChildren;
	
	
	public Tree(DataIO datEdges){
		
		super();
		
		//map1 = map from vertex names to vertices
		//i1 = counter
		
		HashMap<String,GraphVertex> map1;
		int i1;
		
		//loading vertices
		i1 = 0;
		map1 = new HashMap<String,GraphVertex>(datEdges.iRows);
		for(int i=1;i<datEdges.iRows;i++){
			if(!map1.containsKey(datEdges.getString(i, "VERTEX_PARENT"))){
				i1++;
				map1.put(datEdges.getString(i, "VERTEX_PARENT"), new GraphVertex(i1, datEdges.getString(i, "VERTEX_PARENT")));
			}
			if(!map1.containsKey(datEdges.getString(i, "VERTEX_CHILD"))){
				i1++;
				map1.put(datEdges.getString(i, "VERTEX_CHILD"), new GraphVertex(i1, datEdges.getString(i, "VERTEX_CHILD")));
			}
		}
		
		//loading edges
		for(int i=1;i<datEdges.iRows;i++){
			this.addEdge(new GraphEdge(
					map1.get(datEdges.getString(i, "VERTEX_PARENT")),
					map1.get(datEdges.getString(i, "VERTEX_CHILD"))));
		}
		
		//loading parent and children maps
		loadParentChildrenMaps();
		
		//loading root
		loadRoot();	
	}
	
	private void loadParentChildrenMaps(){
		
		mapChildren = HashMultimap.create(this.size(), 10);
		mapParent = new HashMap<Integer,Integer>(this.size());
		for(GraphEdge edg1:this.getEdges()){
			mapChildren.put(edg1.vtxStart.iID, edg1.vtxEnd.iID);
			mapParent.put(edg1.vtxEnd.iID, edg1.vtxStart.iID);
		}
		
	}
	
	private void loadRoot(){
		
		for(Integer i:mapChildren.keySet()){
			if(!mapParent.containsKey(i)){
				vtxRoot = this.getVertex(i);
				return;
			}
		}
		vtxRoot=null;
		
		/*
		
		//lst1 = list of vertex ids
		//setEdges = current set of edges
		
		ArrayList<Integer> lstVertices;
		Set<GraphEdge> setEdges;
		
		lstVertices = this.getVertexIDs();
		for(int i:lstVertices){
			setEdges=this.getEdges(i);
			if(setEdges.size()==1){
				for(GraphEdge edg1:setEdges){
					if(edg1.vtxStart.iID==i){
						vtxRoot=this.getVertex(i);
						return;
					}
				}
			}
		}
		*/
		
	}
	
	private boolean hasChildren(int iVertexID){
		return mapChildren.containsKey(iVertexID);
	}
	
	public GraphVertex root(){
		return vtxRoot;
	}
	
	public ArrayList<GraphEdge> orderedEdgeList(){
		
		//iVertex = current vertex
		//iVertexParent = current parent vertex
		//lstOut = output
		
		ArrayList<GraphEdge> lstOut;
		int iVertex;
		int iVertexParent = -9999;
		HashMultimap<Integer,Integer> map1;
		
		lstOut = new ArrayList<GraphEdge>(this.size());
		iVertex = root().iID;
		map1 = HashMultimap.create(mapChildren);
		do{
		
			//finding next vertex
			if(hasChildren(iVertex)){
				for(Integer i:map1.get(iVertex)){
					iVertexParent = iVertex;
					iVertex = i;
					lstOut.add(this.getEdge(iVertexParent, iVertex));
					break;
				}
				map1.get(iVertexParent).remove(iVertex);
				
				//TODO need to check if when all values are removed, key is removed
				
			}else{
				do{
					iVertex=mapParent.get(iVertex);
				}while((!map1.containsKey(iVertex) && iVertex!=vtxRoot.iID));
				if(map1.containsKey(iVertex)){
					for(Integer i:map1.get(iVertex)){
						iVertexParent = iVertex;
						iVertex = i;
						lstOut.add(this.getEdge(iVertexParent, iVertex));
						break;
					}
					map1.get(iVertexParent).remove(iVertex);
				}else{
					iVertex=-9999;
				}
			}
		}while(iVertex!=-9999);	
		return lstOut;
	}
}

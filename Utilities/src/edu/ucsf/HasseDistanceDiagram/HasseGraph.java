package edu.ucsf.HasseDistanceDiagram;

import java.util.ArrayList;
import java.util.HashMap;

public class HasseGraph {

	/**Map from vertex IDs to vertices**/
	private HashMap<String,HasseVertex> mapVertices;
	
	
	/**Map from pairs of vertex IDs to edges**/
	private HashMap<String,HasseEdge> mapEdges;
	
	public HasseGraph(int iExpectedVertices){
		mapVertices = new HashMap<String,HasseVertex>(iExpectedVertices);
		mapEdges = new HashMap<String,HasseEdge>(iExpectedVertices*iExpectedVertices);
	}
	
	public void putVertex(HasseVertex hvt1){
		mapVertices.put(hvt1.id(), hvt1);
	}
	
	public void putEdge(String sVertex1, HasseVertex hvt2, double dPathLength, String sEdgeID) throws Exception{
		mapEdges.put(sVertex1 + "," + hvt2.id(), new HasseEdge(mapVertices.get(sVertex1), hvt2, dPathLength, sEdgeID));
		mapVertices.put(hvt2.id(), hvt2);
	}
	
	public void putEdge(String sVertex1, String sVertex2, double dPathLength, String sEdgeID) throws Exception{
		mapEdges.put(sVertex1 + "," + sVertex2, new HasseEdge(mapVertices.get(sVertex1), mapVertices.get(sVertex2), dPathLength, sEdgeID));
	}
	
	public HasseVertex getVertex(String sVertex){
		return mapVertices.get(sVertex);
	}
	
	public void loadPaths() throws Exception{
		
		//d1 = maximum ratio of edge length to path length
		//d2 = current ratio of edge length to path length
		//lst1 = current path
		
		double d1;
		double d2;
		ArrayList<Double[]> lst1;
		
		//********************
		//printVertices();
		//System.out.println("");
		//********************
		
		//d1 = -Double.MAX_VALUE;
		//for(HasseEdge edg1:mapEdges.values()){
		//	d2 = edg1.edgeLength()/edg1.pathLength();
		//	if(d2>d1){
		//		d1=d2;
		//	}
		//}
		
		
		for(String s:mapEdges.keySet()){
			
			
		//	mapEdges.get(s).updatePathLength(mapEdges.get(s).pathLength()*d1);
			
			
			lst1 = mapEdges.get(s).path();
			
			//*********************
			for(int i=0;i<lst1.size();i++){
				System.out.println(lst1.get(i)[0] + "," + lst1.get(i)[1]);
			}
			System.out.println("");
			//*********************
			
		}
		
		
		
	}
	
	public void printVertices(){
		for(HasseVertex vtx:mapVertices.values()){
			System.out.println(vtx.toString());
		}
	}
	
	
}

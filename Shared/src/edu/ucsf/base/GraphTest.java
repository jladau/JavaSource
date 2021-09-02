package edu.ucsf.base;

import java.util.HashSet;

import edu.ucsf.base.Graph.GraphEdge;
import edu.ucsf.base.Graph.GraphVertex;
import edu.ucsf.base.ExtendedCollections;
import edu.ucsf.base.Graph;
import edu.ucsf.base.Property;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for graph object.
 * @author jladau
 */

public class GraphTest extends Property{

	/**Graph object.**/
	private Graph gph1;
	
	/**
	 * Constructor
	 */
	public GraphTest(){
		initialize();
	}
	
	private void initialize(){
		gph1 = new Graph();
		for(int i=0;i<4;i++){
			gph1.addVertex(gph1.new GraphVertex(i,"testvertex" + i));
		}
		gph1.addEdge(gph1.new GraphEdge(gph1.getVertex(0),gph1.getVertex(1)));
		gph1.addEdge(gph1.new GraphEdge(gph1.getVertex(2),gph1.getVertex(3)));
		gph1.addEdge(gph1.new GraphEdge(gph1.getVertex(0),gph1.getVertex(2)));
	}
	
	@Test
	public void containsEdge_EdgeAbsentOrPresent_CorrectlyReported(){
		assertTrue(gph1.containsEdge(0,1));
		assertTrue(gph1.containsEdge(2,3));
		assertTrue(gph1.containsEdge(0,2));
		
		assertFalse(gph1.containsEdge(0,3));
		assertFalse(gph1.containsEdge(1,6));
		assertFalse(gph1.containsEdge(1,1));	
	}
	
	@Test
	public void clone_GraphCloned_OutputIdentical(){
		
		//gph2 = cloned graph
		
		Graph gph2;
		
		gph2 = gph1.clone();
		assertTrue(ExtendedCollections.equivalent(new Integer[]{0,1,2,3},gph2.getVertexIDs()));
		assertEquals(4,gph2.order());
		assertEquals(3,gph2.size());
		assertEquals(3,gph2.getEdges().size());
	}

	@Test
	public void removeEdge_EdgeRemoved_EdgeNotPresent(){
		gph1.removeEdge(0,1);
		assertTrue(!gph1.containsEdge(0,1));
		initialize();
	}
	
	@Test
	public void put_PropertyAddedToTblEdge_PropertyAlsoAddedToMamEdges(){
		
		gph1.getEdge(0, 1).put("TestKey", "TestValue01");
		gph1.getEdge(2, 3).put("TestKey", "TestValue23");
		gph1.getEdge(0, 2).put("TestKey", "TestValue02");
		for(int i=0;i<4;i++){
			for(GraphEdge edg1:gph1.getEdges(i)){
				assertEquals("TestValue" + edg1.vtxStart.iID + edg1.vtxEnd.iID,edg1.getString("TestKey"));
			}
		}
		initialize();
		
		//TODO add analogous test for vertex properties
		
	}
	
	@Test
	public void put_PropertyAddedToAllEdges_PropertyAddedToAllMaps(){
		
		for(GraphEdge edg1:gph1.getEdges()){
			edg1.put("TestPropertyKey", edg1.vtxStart + "-" + edg1.vtxEnd);
		}
		for(int i=0;i<4;i++){
			for(GraphEdge edg1:gph1.getEdges(0)){
				assertEquals(edg1.vtxStart + "-" + edg1.vtxEnd, edg1.getString("TestPropertyKey"));
			}
		}
		for(GraphEdge edg1:gph1.getEdges()){
			assertEquals(edg1.vtxStart + "-" + edg1.vtxEnd, edg1.getString("TestPropertyKey"));
		}
		initialize();
	
		//TODO write analogous test for vertex properties
	
	}
	
	
	@Test
	public void getEdges_EdgesGotten_AllEdgesIncluded(){
		
		//TODO can probably write a better test here
		
		assertTrue(gph1.getEdges().contains(gph1.getEdge(0, 1)));
		assertTrue(gph1.getEdges().contains(gph1.getEdge(2, 3)));
		assertTrue(gph1.getEdges().contains(gph1.getEdge(0, 2)));
		
	}
	
	@Test
	public void getEdge_SpanningEdgeGotten_CorrectEdgeObtained(){
		assertEquals(0,gph1.getEdge(0,1).vtxStart.iID);
		assertEquals(1,gph1.getEdge(0,1).vtxEnd.iID);
		
		assertEquals(2,gph1.getEdge(2,3).vtxStart.iID);
		assertEquals(3,gph1.getEdge(2,3).vtxEnd.iID);
		
		assertEquals(0,gph1.getEdge(0,2).vtxStart.iID);
		assertEquals(2,gph1.getEdge(0,2).vtxEnd.iID);
	}

	@Test
	public void getEdges_EdgesWithVertexGotten_CorrectEdgesObtained(){
		
		//TODO need to check that correct edges are obtained here with function gph1.getEdges(0), gph1.getEdges(1), gph1.getEdges(2)
	}
	
	@Test
	public void getVertices_VerticesGotten_VerticesCorrect(){
		
		//set1 = set of obtained vertices
		
		HashSet<Integer> set1;
		
		set1 = new HashSet<Integer>();
		for(GraphVertex vtx1:gph1.getVertices()){
			set1.add(vtx1.iID);
		}
		assertTrue(ExtendedCollections.equivalent(new Integer[]{0,1,2,3},set1));
	}
	
	@Test
	public void getVertexCount_CountGotten_CountCorrect(){
		assertEquals(4,gph1.order());
	}
	
	//TODO need to test that properties added to an edge via getEdge(i,j) are transferred to the edge in mamEdges
}
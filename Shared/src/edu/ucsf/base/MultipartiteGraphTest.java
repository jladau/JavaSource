package edu.ucsf.base;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.common.collect.HashMultimap;

/**
 * This class allows for adding arbitrary properties to an object
 * @author jladau
 */

public class MultipartiteGraphTest {

	/**Graph object**/
	MultipartiteGraph<Integer> gph1;
	
	/**Map of independent sets**/
	HashMultimap<String, Integer> map1;
	
	public MultipartiteGraphTest(){	

		map1 = HashMultimap.create();
		for(int i=0;i<5;i++){
			map1.put("a", i);
		}
		for(int i=5;i<7;i++){
			map1.put("b", i);
		}
		for(int i=7;i<8;i++){
			map1.put("c", i);
		}
		gph1 = new MultipartiteGraph<Integer>(map1);
	}
	
	@Test
	public void loadRandomSubsetFromCompleteGraph_IsRun_CorrectSubset() throws Exception{
		
		gph1.loadRandomSubsetOfCompleteGraph(5, 1234);
		
		for(SemiOrderedPair<Integer> sop1:gph1.setEdges){
			for(String s:map1.keySet()){
				if(map1.get(s).contains(sop1.o1)){
					assertTrue(!map1.get(s).contains(sop1.o2));
				}
			}
		}
	}
	
	@Test
	public void loadRandomSubsetFromCompleteGraphComplement_IsRun_CorrectSubset() throws Exception{
		
		gph1.loadRandomSubsetOfCompleteGraphComplement(5, 1234);
		
		for(SemiOrderedPair<Integer> sop1:gph1.setEdges){
			for(String s:map1.keySet()){
				if(map1.get(s).contains(sop1.o1)){
					assertTrue(map1.get(s).contains(sop1.o2));
				}
			}
		}
	}
}
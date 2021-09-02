package edu.ucsf.geospatial;

import java.util.ArrayList;
import java.util.HashMap;
import com.google.common.collect.ArrayTable;
import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.Graph;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Generates variates from a spatial autoregressive model: Y=rho*W*Y+epsilon
 * @author jladau
 *
 */

public class SpatialAutoregressiveModelTest{

	/**Model object**/
	private SpatialAutoregressiveModel sar1;
	
	/**BIOM object**/
	//private BiomIO bio1;
	
	public SpatialAutoregressiveModelTest(){
		
		initialize();
	}
	
	private void initialize(){
		
		//gph1 = graph object
		//tblW = weights matrix
		//d1 = linking value
		//rgi1 = set of linked samples
		
		Graph gph1;
		ArrayTable<Graph.GraphVertex, Graph.GraphVertex, Double> tblW;
		int[] rgi1;
		double d1;
		
		gph1 = new Graph();
		for(int i=0;i<16;i++){
			gph1.addVertex(gph1.new GraphVertex(i, "sample" + (i+1)));
		}
		tblW = ArrayTable.create(gph1.getVertices(), gph1.getVertices());
		rgi1 = new int[]{0,1,4,5};
		d1 = 1./((double) rgi1.length-1.);
		for(Integer i:rgi1){
			for(Integer j:rgi1){
				if(i!=j){
					tblW.put(gph1.getVertex(i), gph1.getVertex(j), d1);
				}
			}
		}
		for(int i=0;i<16;i++){
			for(int j=0;j<16;j++){
				if(tblW.get(gph1.getVertex(i), gph1.getVertex(j))==null){
					tblW.put(gph1.getVertex(i), gph1.getVertex(j), 0.);
				}
			}
		}
		sar1 = new SpatialAutoregressiveModel(tblW,0.95,1);
		//bio1 = new BiomIO("/home/jladau/Desktop/Data/Microbial_Community_Samples/ValidationData.NA.NA.Ladau.biom");
	}
	
	@Test
	public void generateRelativeAbundances_AbundanceGenerated_AbundancesCorrect(){
		
		//map1 = current vector of values
		//lst1 = first set of values
		//lst2 = second set of values
		
		ArrayList<Double> lst1;
		ArrayList<Double> lst2;
		HashMap<String,Double> map1;
		
		lst1 = new ArrayList<Double>(1000);
		lst2 = new ArrayList<Double>(1000);
		for(int i=0;i<1000;i++){
			map1 = sar1.generateRelativeAbundances();
			lst1.add(map1.get("sample1"));
			lst2.add(map1.get("sample2")+map1.get("sample5")+map1.get("sample6"));
		}
		assertEquals(ExtendedMath.pearson(lst1, lst2),0.99,0.01);
		
		lst1 = new ArrayList<Double>(1000);
		lst2 = new ArrayList<Double>(1000);
		for(int i=0;i<1000;i++){
			map1 = sar1.generateRelativeAbundances();
			lst1.add(map1.get("sample1"));
			lst2.add(map1.get("sample14"));
		}
		assertEquals(ExtendedMath.pearson(lst1, lst2),0,0.05);
		
		lst1 = new ArrayList<Double>(1000);
		lst2 = new ArrayList<Double>(1000);
		for(int i=0;i<1000;i++){
			map1 = sar1.generateRelativeAbundances();
			lst1.add(map1.get("sample12"));
			lst2.add(map1.get("sample14"));
		}
		assertEquals(ExtendedMath.pearson(lst1, lst2),0,0.05);
		
		//map1 = sar1.generateRelativeAbundances();
		//for(String s:map1.keySet()){
		//	System.out.println(s + "," + bio1.axsSample.getMetadata(s).get("latitude") + "," + bio1.axsSample.getMetadata(s).get("longitude") + "," + map1.get(s));
		//}
	}
}

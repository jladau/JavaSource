package edu.ucsf.geospatial;

import java.util.HashSet;
import edu.ucsf.io.BiomIO;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Units tests
 * @author jladau
 *
 */

public class DistanceDecayTest {

	/**Distance-decay object**/
	DistanceDecay ddc1;
	
	public DistanceDecayTest(){
		
		//bio1 = biomio object
		
		BiomIO bio1;
		
		bio1 = new BiomIO("/home/jladau/Documents/Research/Data/Microbial_Community_Samples/ValidationData.NA.NA.Ladau.biom");
		bio1.normalize();
		ddc1 = new DistanceDecay(bio1);
	}
	
	@Test
	public void runMantelTest_MantelTestRun_RunCorrect(){
		assertEquals(ddc1.runMantelTest(100).getObservedCorrelation(),0.01941316457143,0.000000001);
	}

	@Test
	public void getDistance_DistanceGotten_DistanceCorrect(){
		assertEquals(ddc1.getDistance("sample1", "sample16"),404.9,1);
		assertEquals(ddc1.getDistance("sample5", "sample11"),189.2,1);
	}
	
	@Test
	public void getBrayCurtis_BrayCurtisGotten_BrayCurtisCorrect(){
		assertEquals(ddc1.getBrayCurtis("sample1", "sample16"),0.16666666666667,0.0001);
		assertEquals(ddc1.getBrayCurtis("sample5", "sample11"),0.92307692307692,0.0001);
	}
	
	@Test
	public void getSamplePairs_SamplePairsGotten_SamplePairsCorrect(){
		
		//set1 = set of sample pairs
		//set2 = set of sample pairs from distance decay object
		
		HashSet<String> set1;
		HashSet<String> set2;
		
		set1 = new HashSet<String>();
		for(int i=2;i<=16;i++){
			for(int j=1;j<i;j++){
				set1.add("sample" + j + "," + "sample" + i);
				set1.add("sample" + i + "," + "sample" + j);
			}
		}
		assertEquals(set1.size(),240);
		set2 = new HashSet<String>();
		for(String[] rgs1:ddc1.getSamplePairs()){
			set2.add(rgs1[0] + "," + rgs1[1]);
		}
		for(String s:set2){
			assertTrue(set1.contains(s));
			set1.remove(s);
		}
		assertEquals(set1.size(),120);
	}
}

package edu.ucsf.base;

import java.util.HashSet;

import edu.ucsf.base.BetaDiversityIterator.BetaDiversity;
import edu.ucsf.io.BiomIO;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Units tests
 * @author jladau
 *
 */

public class BetaDiversityIteratorTest {

	/**Beta-diversity iterator object**/
	BetaDiversityIterator itr1;
	
	/**Biomio object**/
	BiomIO bio1;
	
	public BetaDiversityIteratorTest(){
		
		bio1 = new BiomIO("/home/jladau/Documents/Research/Data/Microbial_Community_Samples/ValidationData.NA.NA.Ladau.biom");
		bio1.normalize();
	}
	
	@Test
	public void next_NextRun_CorrectValue(){
		
		//bet1 = current beta-diversity measurement
		
		BetaDiversity bet1;
		
		itr1 = new BetaDiversityIterator(bio1);
		while(itr1.hasNext()){
			bet1 = itr1.next();
			if(bet1.sameSamples("sample1", "sample16")){
				assertEquals(bet1.dBetaBrayCurtis, 1.-0.16666666666667, 0.0001);
				assertEquals(bet1.dBetaJ, 0.5, 0.0001);
				assertEquals(bet1.dBetaRich, 0.5, 0.0001);
				assertEquals(bet1.dBetaTurn, 0., 0.0001);
			}
			if(bet1.sameSamples("sample5", "sample11")){
				assertEquals(bet1.dBetaBrayCurtis, 1-0.92307692307692, 0.0001);
				assertEquals(bet1.dBetaJ, 0., 0.0001);
				assertEquals(bet1.dBetaRich, 0., 0.0001);
				assertEquals(bet1.dBetaTurn, 0., 0.0001);
			}
		}
	}
	
	
	@Test
	public void next_AllRun_AllSamplePairsChecked(){
		
		//set1 = set of sample pairs
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>();
		itr1 = new BetaDiversityIterator(bio1);
		while(itr1.hasNext()){
			set1.add(itr1.next().toString());
		}
		assertEquals(set1.size(),120);
	}
}

package edu.ucsf.base;

import edu.ucsf.io.BiomIO;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for MantelTest
 * @author jladau
 */

public class NestednessNODFTest {

	
	/**Biom table for testing**/
	private BiomIO bio1;
	
	/**Nestedness object**/
	private NestednessNODF nes1;
	
	public NestednessNODFTest() throws Exception{
		bio1=new BiomIO("/home/jladau/Documents/Research/Data/Microbial_Community_Samples/BiomIOTestData/rich_sparse_otu_table_hdf5.biom");
	}
	
	@Test
	public void loadNODF_NODFLoaded_MatchesSimulation() throws Exception{
		
		//dExpectation = expectation value
		//dObservation = observed value
		//dSES = standardized effect size
		//dVariance = variance
		
		double dExpectation;
		double dObservation;
		double dSES;
		double dVariance;
		
		//loading nestedness object
		nes1 = new NestednessNODF(bio1, 1234);
		nes1.setGraphs(loadGraph(bio1.axsSample), loadGraph(bio1.axsObservation));
		
		//checking that expectations match
		nes1.loadNODF("observation", "fixedequiprobable");
		dObservation = nes1.ndf1.dObserved;
		dExpectation = nes1.ndf1.dExpectation;
		dSES = nes1.ndf1.dStandardizedEffect;
		dVariance = nes1.ndf1.dVariance;
		nes1.loadNODFSimulated("observation", "fixedequiprobable", 10000, 1234);
		assertEquals(dObservation, nes1.ndf1.dObserved, 0.02);
		assertEquals(dExpectation, nes1.ndf1.dSimulatedMean, 0.02);
		assertEquals(dVariance, Math.pow(nes1.ndf1.dSimulatedStDev, 2.), 0.02);
		assertEquals(dSES, nes1.ndf1.dStandardizedEffect, 0.02);
		
		nes1.loadNODF("sample", "equiprobablefixed");
		dObservation = nes1.ndf1.dObserved;
		dExpectation = nes1.ndf1.dExpectation;
		dSES = nes1.ndf1.dStandardizedEffect;
		nes1.loadNODFSimulated("sample", "equiprobablefixed", 10000, 1234);
		assertEquals(dObservation, nes1.ndf1.dObserved, 0.02);
		assertEquals(dExpectation, nes1.ndf1.dSimulatedMean, 0.02);
		assertEquals(dVariance, Math.pow(nes1.ndf1.dSimulatedStDev, 2.), 0.02);
		assertEquals(dSES, nes1.ndf1.dStandardizedEffect, 0.02);
	}

	
	
	@Test
	public void loadNODFSimulated_NODFLoaded_CorrectResult() throws Exception{
		
		//loading nestedness object
		nes1 = new NestednessNODF(bio1, 1234);
		nes1.setGraphs(loadGraph(bio1.axsSample), loadGraph(bio1.axsObservation));
		
		//checking that expectations match
		nes1.loadNODFSimulated("observation", "fixedequiprobable", 10000, 1234);
		//mean of simulated values should be 0.6666666666
		//System.out.println(nes1.findFixedEquiprobableObsExpectation() + "," + nes1.ndf1.dSimulatedMean);
		assertEquals(nes1.findFixedEquiprobableObsExpectation(), nes1.ndf1.dSimulatedMean, 0.02);
		
		nes1.loadNODFSimulated("sample", "equiprobablefixed", 10000, 1234);
		//mean of simulated values should be 0.56
		//System.out.println(nes1.findEquiprobableFixedSampleExpectation() + "," + nes1.ndf1.dSimulatedMean);
		assertEquals(nes1.findEquiprobableFixedSampleExpectation(), nes1.ndf1.dSimulatedMean, 0.02);
		
		//checking that observed values match
		nes1.loadNODFSimulated("sample", "fixedequiprobable", 10000, 1234);
		assertEquals(nes1.ndf1.dObserved,0.6555555555555556,0.02);
		nes1.loadNODFSimulated("observation", "fixedequiprobable", 10000, 1234);
		assertEquals(nes1.ndf1.dObserved,0.7083333333333333,0.02);
	}

		
	private SimpleGraph<String> loadGraph(BiomIO.Axis axs1){
		
		//gph1 = output
		
		SimpleGraph<String> gph1;
		
		gph1 = new SimpleGraph<String>();
		for(String s:axs1.getIDs()){
			for(String t:axs1.getIDs()){
				if(!s.equals(t)){
					gph1.addEdge(new SemiOrderedPair<String>(s,t));
				}
			}
		}
		return gph1;
	}
}

package edu.ucsf.RapoportsRule;

import java.util.ArrayList;
import java.util.Collections;

import com.google.common.collect.Range;
import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.Permutation;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class RapoportsRuleLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments object
		//datSamples = sample locations
		//rgi1 = presence-absence matrix. rows represent taxa, columns represent samples.
		//lstSamples = list of samples
		//lstTaxa = list of taxa
		//iCounter = counter
		//dPrOccur = probability of occurrence
		//lst1 = list of sample latitudes for permutation
		//per1 = permutation object
		//lstOut = output
		//lstBreadths = list of breadths
		//lstIndices = list of candidate sample indices for current sample
		
		ArgumentIO arg1;
		DataIO datSamples;
		int rgi1[][];
		ArrayList<RapoportsRuleSample> lstSamples;
		ArrayList<RapoportsRuleTaxon> lstTaxa;
		int iCounter;
		double dPrOccur;
		ArrayList<Double> lst1;
		Permutation<Double> per1;
		ArrayList<String> lstOut;
		ArrayList<Double> lstBreadths;
		ArrayList<Integer> lstIndices;
		
		//loading inputs
		arg1 = new ArgumentIO(rgsArgs);
		datSamples = new DataIO(arg1.getValueString("sSamplesPath"));
		dPrOccur = arg1.getValueDouble("dPrOccur");
		
		//loading samples
		lstSamples = new ArrayList<RapoportsRuleSample>(datSamples.iRows-1);
		for(int i=1;i<datSamples.iRows;i++){
			lstSamples.add(new RapoportsRuleSample(datSamples.getDouble(i, "LATITUDE"), i-1));
		}
		
		//loading taxa
		lstTaxa = new ArrayList<RapoportsRuleTaxon>();
		iCounter = 0;
		//latitude min nz = -46.5, latitude max nz = -34.5
		for(int i=0;i<400;i++){
			lstTaxa.add(new RapoportsRuleTaxon(Range.closed(-37.5,-34.5), iCounter));
			iCounter++;
		}
		for(int i=0;i<300;i++){
			lstTaxa.add(new RapoportsRuleTaxon(Range.closed(-40.5,-37.5), iCounter));
			iCounter++;
		}
		for(int i=0;i<200;i++){
			lstTaxa.add(new RapoportsRuleTaxon(Range.closed(-43.5,-40.5), iCounter));
			iCounter++;
		}
		for(int i=0;i<100;i++){
			lstTaxa.add(new RapoportsRuleTaxon(Range.closed(-46.5,-43.5), iCounter));
			iCounter++;
		}
		
		//**********************************************
		//loading observed presence-absence matrix
		rgi1 = new int[lstTaxa.size()][lstSamples.size()];
		for(int i=0;i<lstTaxa.size();i++){
			lstIndices = new ArrayList<Integer>(1000);
			for(int j=0;j<lstSamples.size();j++){
		
				//taxon within range of sample
				if(lstTaxa.get(i).rngRangeTrue.contains(lstSamples.get(j).dLat)){
					lstTaxa.get(i).iTruePrevalence++;
					lstTaxa.get(i).addToMaximumPossiblObservedRange(lstSamples.get(j).dLat);
					lstIndices.add(j);
				}
			}
			Collections.shuffle(lstIndices);
			for(int k=0;k<2;k++){
				rgi1[i][lstIndices.get(k)]=1;
				lstTaxa.get(i).iObservedPrevalence++;
			}
		}
		
		//rgi1 = new int[lstTaxa.size()][lstSamples.size()];
		//for(int i=0;i<lstTaxa.size();i++){
		//	for(int j=0;j<lstSamples.size();j++){
		//		
		//		//sample within range of taxon
		//		if(lstTaxa.get(i).rngRangeTrue.contains(lstSamples.get(j).dLat)){
		//			lstTaxa.get(i).iTruePrevalence++;
		//			lstTaxa.get(i).addToMaximumPossiblObservedRange(lstSamples.get(j).dLat);
		//			if(Math.random()<dPrOccur){
		//				rgi1[i][j]=1;
		//				lstTaxa.get(i).iObservedPrevalence++;
		//			}else{
		//				rgi1[i][j]=0;
		//			}
		//		}
		//	}
		//}
		//**********************************************
		
		
		//randomizing samples if requested
		if(arg1.containsArgument("bRandomizeSamples") && arg1.getValueBoolean("bRandomizeSamples")){
			lst1 = new ArrayList<Double>(lstSamples.size());
			for(int i=0;i<lstSamples.size();i++){
				lst1.add(lstSamples.get(i).dLat);
			}
			per1 = new Permutation<Double>(lst1);
			per1.loadRandomPermutation();
			for(int i=0;i<lstSamples.size();i++){
				lstSamples.get(i).dLat = per1.getImage(lst1.get(i));
			}
		}
		
		//finding observed range breadths and richness
		for(int i=0;i<lstTaxa.size();i++){
			for(int j=0;j<lstSamples.size();j++){
				if(rgi1[i][j]==1){
					lstTaxa.get(i).addToObservedRange(lstSamples.get(j).dLat);
					lstSamples.get(j).incrementRichness();
				}
			}
		}
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add("SAMPLE_ID,SAMPLE_LATITUDE,MEAN_OBSERVED_RANGE_BREADTH,RICHNESS");
		
		//finding mean observed range breadth for each sample
		for(int j=0;j<lstSamples.size();j++){
			lstBreadths = new ArrayList<Double>();
			for(int i=0;i<lstTaxa.size();i++){
				if(rgi1[i][j]==1){
					lstBreadths.add(lstTaxa.get(i).observedRangeBreadth());
				}
			}
			lstOut.add(
					lstSamples.get(j).iID + "," +
					lstSamples.get(j).dLat + "," +
					ExtendedMath.mean(lstBreadths) + "," + 
					lstSamples.get(j).dRichness);
		}
		
		//outputting results
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath").replace("csv", "observedrelationship.csv"));
		lstOut = new ArrayList<String>();
		lstOut.add(""
				+ "TAXON,"
				+ "RANGE_LAT_MIN,"
				+ "RANGE_LAT_MAX,"
				+ "RANGE_BREADTH_TRUE,"
				+ "RANGE_BREADTH_OBSERVED,"
				+ "RANGE_BREADTH_MAX_POSSIBLE_OBSERVED,"
				+ "PREVALENCE_TRUE,"
				+ "PREVALENCE_OBSERVED");
		for(RapoportsRuleTaxon txn1: lstTaxa){
			lstOut.add(
					txn1.iID + "," + 
					txn1.rngRangeTrue.lowerEndpoint() + "," + 
					txn1.rngRangeTrue.upperEndpoint() + "," +
					txn1.trueRangeBreadth() + "," +
					txn1.observedRangeBreadth() + "," +
					txn1.observedMaxPossibleRangeBreadth() + "," +
					txn1.iTruePrevalence + "," +
					txn1.iObservedPrevalence);
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath").replace("csv", "taxa.csv"));
		lstOut = new ArrayList<String>();
		lstOut.add("SAMPLE,SAMPLE_LATITUDE,TAXON");
		for(int i=0;i<lstTaxa.size();i++){
			for(int j=0;j<lstSamples.size();j++){
				if(rgi1[i][j]==1){
					lstOut.add(lstSamples.get(j).iID + "," + lstSamples.get(j).dLat + "," + lstTaxa.get(i).iID);
				}
			}
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath").replace("csv", "occurrences.csv"));
		System.out.println("Done.");
	}
}
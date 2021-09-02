package edu.ucsf.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.apache.commons.math3.random.RandomDataGenerator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;

import edu.ucsf.io.BiomIO;

/**
 * Calculates nesteness for a given matrix.
 * @author jladau
 */

public class NestednessNODF {

	/**Random data generator**/
	private RandomDataGenerator rnd1;
	
	/**Sparse presence-absence matrix**/
	protected SparseBinaryMatrix sbm1;
	
	/**Sample pairs**/
	protected SimpleGraph<String> gphSamples = null;
	
	/**Observation pairs**/
	protected SimpleGraph<String> gphObservations = null;
	
	/**NODF**/
	public NODF ndf1;
	
	/**Number of columns (samples) in current graph**/
	private double dCols = Double.NaN;
	
	/**Number of rows (observations) for current graph**/
	private double dRows = Double.NaN;
	
	/**Number of observations**/
	protected int iObservations;
	
	/**Number of samples**/
	protected int iSamples;
	
	public NestednessNODF(BiomIO bio1, int iRandomSeed) throws Exception{
		
		//initializing random number generator
		rnd1 = new RandomDataGenerator();
		rnd1.reSeed((long) iRandomSeed);
		
		//loading sparse presence-absence matrix
		sbm1 = new SparseBinaryMatrix(bio1.axsObservation.size(), bio1.axsSample.size());
		for(String s:bio1.axsObservation.getIDs()){
			for(String t:bio1.axsSample.getIDs()){
				if(bio1.getValueByIDs(s, t)>0){	
					sbm1.add(s, t);
				}
			}
		}
		
		//initializing NODF object
		ndf1 = new NODF();
		
		//loading number of observations and samples
		iObservations = bio1.axsObservation.size();
		iSamples = bio1.axsSample.size();
	}
	
	public void setGraphs(SimpleGraph<String> gphSamples, SimpleGraph<String> gphObservations){

		//loading pairs of observations and samples to consider
		this.gphSamples = gphSamples;
		this.gphObservations = gphObservations;
	
		//loading number of rows and columns
		this.loadNumberOfRowsColumns();
		
	}
	
	
	private void loadNumberOfRowsColumns(){
		
		//set1 = set of observations or samples
		
		HashSet<String> set1;
		
		dCols = Double.NaN;
		dRows = Double.NaN;
		if(gphSamples.setVertices.size()>0){
			dCols = gphSamples.setVertices.size();
			set1 = new HashSet<String>(sbm1.getObservationIDs().size());
			for(String sSpl:gphSamples.setVertices){
				for(String sObs:sbm1.getColumn(sSpl)){
					set1.add(sObs);
				}
			}
			dRows = set1.size();
			
			//***********************
			//System.out.println("Rows: " + dRows);
			//***********************
			
		}else if(gphObservations.setVertices.size()>0){
			dRows = gphObservations.setVertices.size();
			set1 = new HashSet<String>(sbm1.getSampleIDs().size());
			for(String sObs:gphObservations.setVertices){
				for(String sSpl:sbm1.getRow(sObs)){
					set1.add(sSpl);
				}
			}
			dCols = set1.size();
		}
	}
	
	
	/**
	 * Finds NODF and associated probabilities
	 * @param sAxis Axis along which to calculate NODF: "sample", "observation", or "both"
	 * @param iIterations Number of null model iterations.
	 * @param sNullModel Null model: "fixedequiprobable" or "equiprobablefixed" or "fixedfixed"
	 * @param iRandomSeed Random seed
	 */
	public void loadNODFSimulated(String sAxis, String sNullModel, int iIterations, int iRandomSeed){
		ndf1.addObservedNODF(calculateNODF(sbm1, sAxis));
		if(sNullModel.equals("fixedequiprobable")){	
			//TODO random seed not being used here
			runFixedEquiprobable(sAxis,iIterations);
		}else if(sNullModel.equals("equiprobablefixed")){
			//TODO random seed not being used here
			runEquiprobableFixed(sAxis,iIterations);
		}else if(sNullModel.equals("fixedfixed")){
			runFixedFixed(sAxis,iIterations, iRandomSeed);
		}
	}
	
	
	/**
	 * Outputs a randomized matrix according to the specified null model
	 * @param sNullModel Null model: "fixedequiprobable" or "equiprobablefixed" or "fixedfixed"
	 * @param iRandomSeed Random seed
	 * @param bio1 BiomIO object
	 * @return Randomized matrix
	 */
	
	public BiomIO randomizedMatrix(String sNullModel, int iRandomSeed, BiomIO bio1) throws Exception{
		
		//sbm1 = randomized matrix
		
		SparseBinaryMatrix sbm1=null;
		
		if(sNullModel.equals("fixedequiprobable")){	
			throw new Exception("Fixed-equiprobable example not yet implemented.");
		}else if(sNullModel.equals("equiprobablefixed")){
			throw new Exception("Equiprobable-fixed example not yet implemented.");
		}else if(sNullModel.equals("fixedfixed")){
			sbm1 = this.findFixedFixedExample(iRandomSeed);
		}
		for(String s:bio1.axsObservation.getIDs()){
			for(String t:bio1.axsSample.getIDs()){
				if(bio1.getValueByIDs(s, t)>0){
					bio1.setValue(s, t, 0);
				}
			}
		}
		for(String s:sbm1.getObservationIDs()){
			for(String t:sbm1.getRow(s)){
				bio1.setValue(s, t, 1);
			}
		}
		return bio1;
	}
	
	
	
	/**
	 * Finds NODF and standardized effect sizes using hypergeometric mean and variance
	 * @param sAxis Axis along which to calculate NODF: "sample", "observation", or "both"
	 * @param sNullModel Null model: "fixedequiprobable" or "equiprobablefixed"
	 */
	public void loadNODF(String sAxis, String sNullModel) throws Exception{
		ndf1.addObservedNODF(calculateNODF(sbm1, sAxis));
		ndf1.findSES(calculateNODFExpectationVariance(sbm1, sAxis, sNullModel));
	}
	
	public ArrayList<String> graphRandomMatrix(BiomIO bio1, String sNullModel, String[] rgsSampleMetadataFields){
		
		//sbmSim = current simulated matrix
		//rgo1 = current selected samples
		
		SparseBinaryMatrix sbmSim;
		Object rgo1[];
		
		//loading matrix
		sbmSim = new SparseBinaryMatrix((int) sbm1.getRowCount(), (int) sbm1.getColCount());
		
		//looping observations and loading simulated matrix
		if(sNullModel.equals("fixedequiprobable")){
			for(String s:sbm1.getObservationIDs()){
				rgo1 = rnd1.nextSample(sbm1.getSampleIDs(), sbm1.getRowSum(s));
				for(Object o:rgo1){
					sbmSim.add(s,(String) o);
				}
			}
		}else if(sNullModel.equals("equiprobablefixed")){
			for(String s:sbm1.getSampleIDs()){
				rgo1 = rnd1.nextSample(sbm1.getObservationIDs(), sbm1.getColSum(s));
				for(Object o:rgo1){
					sbmSim.add((String) o, s);
				}
			}
		}
		
		//transferring simulated matrix to biom object
		for(String s:sbmSim.getObservationIDs()){
			for(String t:sbmSim.getSampleIDs()){
				if(sbmSim.getRow(s).contains(t)){
					bio1.setValue(s, t, 1);
				}else{
					bio1.setValue(s, t, 0);
				}
			}
		}
		
		//outputting graph
		return NestednessGrapher.getNestednessGraph(bio1, rgsSampleMetadataFields);
	}
	
	public double findEquiprobableFixedSampleExpectation(){
		
		//dOut = output
		//iSum1 = first sum
		//iSum2 = second sum
		//dSumSmall = smaller sum
		//sSimLarge = larger sum
		//dCounter = numer of terms
		
		double dCounter;				
		double dOut;
		int iSum1; int iSum2;
		double dSumSmall; 
		double dSumLarge;
		
		dOut = 0.;
		dCounter=0.;
		for(SemiOrderedPair<String> spr1:gphSamples.setEdges){
			iSum1 = sbm1.getColSum(spr1.o1);
			iSum2 = sbm1.getColSum(spr1.o2);
			
			//***********************
			//System.out.println(iSum1 + "," + iSum2);
			//***********************
			
			if(iSum1==0 || iSum2==0){
				continue;
			}else{
				dCounter++;
			}
			if(iSum1<iSum2){
				dSumSmall = (double) iSum1;
				dSumLarge = (double) iSum2;
			}else if(iSum1>iSum2){
				dSumSmall = (double) iSum2;
				dSumLarge = (double) iSum1;
			}else{
				continue;
			}
			dOut += (dSumSmall*dSumLarge)/(dRows*dSumSmall);
		}
		
		//****************
		//System.out.println(dOut/dCounter);
		//****************
		
		return dOut/dCounter;		
	}
	
	public double findEquiprobableFixedSampleVariance(){
		return findEquiprobableFixedSampleVariance(this.dRows);
	}
	
	public double findEquiprobableFixedSampleVariance(double dRows){
		
		//dOut = output
		//iSum1 = first sum
		//iSum2 = second sum
		//dSumSmall = smaller sum
		//sSimLarge = larger sum
		//dCounter = numer of terms
		
		double dCounter;		
		double dOut;
		int iSum1; int iSum2;
		double dSumSmall; 
		double dSumLarge;
		
		dOut = 0.;
		dCounter = 0;
		for(SemiOrderedPair<String> spr1:gphSamples.setEdges){
			iSum1 = sbm1.getColSum(spr1.o1);
			iSum2 = sbm1.getColSum(spr1.o2);
			if(iSum1==0 || iSum2==0){
				continue;
			}else{
				dCounter++;
			}
			if(iSum1<iSum2){
				dSumSmall = (double) iSum1;
				dSumLarge = (double) iSum2;
			}else if(iSum1>iSum2){
				dSumSmall = (double) iSum2;
				dSumLarge = (double) iSum1;
			}else{
				continue;
			}
			
			dOut += 1./Math.pow(dSumSmall,2.)*(dSumSmall*dSumLarge*(dRows-dSumSmall)*(dRows-dSumLarge))/(dRows*dRows*(dRows-1.));
		}
		return dOut/(dCounter*dCounter);		
	}

	public double findFixedEquiprobableObsExpectation(){
		
		//dOut = output
		//iSum1 = first sum
		//iSum2 = second sum
		//dSumSmall = smaller sum
		//sSimLarge = larger sum
		//dCounter = numer of terms
		
		double dCounter;
		double dOut;
		int iSum1; int iSum2;
		double dSumSmall; 
		double dSumLarge;
		
		dOut = 0.;
		dCounter = 0;
		for(SemiOrderedPair<String> spr1:gphObservations.setEdges){
			iSum1 = sbm1.getRowSum(spr1.o1);
			iSum2 = sbm1.getRowSum(spr1.o2);
			if(iSum1==0 || iSum2==0){
				continue;
			}else{
				dCounter++;
			}
			if(iSum1<iSum2){
				dSumSmall = (double) iSum1;
				dSumLarge = (double) iSum2;
			}else if(iSum1>iSum2){
				dSumSmall = (double) iSum2;
				dSumLarge = (double) iSum1;
			}else{
				continue;
			}
			dOut += (dSumSmall*dSumLarge)/(dCols*dSumSmall);
		}
		return dOut/dCounter;		
	}

	public double findFixedEquiprobableObsVariance(){
		return findFixedEquiprobableObsVariance(this.dCols);
	}
	
	public double findFixedEquiprobableObsVariance(double dCols){
		
		//dOut = output
		//iSum1 = first sum
		//iSum2 = second sum
		//dSumSmall = smaller sum
		//sSimLarge = larger sum
		//dCounter = numer of terms
		
		double dOut;
		int iSum1; int iSum2;
		double dSumSmall; 
		double dSumLarge;
		double dCounter;
		
		dOut = 0.;
		dCounter = 0.;
		for(SemiOrderedPair<String> spr1:gphObservations.setEdges){
			iSum1 = sbm1.getRowSum(spr1.o1);
			iSum2 = sbm1.getRowSum(spr1.o2);
			if(iSum1==0 || iSum2==0){
				continue;
			}else{
				dCounter++;
			}
			if(iSum1<iSum2){
				dSumSmall = (double) iSum1;
				dSumLarge = (double) iSum2;
			}else if(iSum1>iSum2){
				dSumSmall = (double) iSum2;
				dSumLarge = (double) iSum1;
			}else{
				continue;
			}
			dOut += 1./Math.pow(dSumSmall,2.)*(dSumSmall*dSumLarge*(dCols-dSumSmall)*(dCols-dSumLarge))/(dCols*dCols*(dCols-1.));
		}
		return dOut/(dCounter*dCounter);	
	}

	private SparseBinaryMatrix findFixedFixedExample(int iRandomSeed){
		
		//cbl1 = curve ball randomization algorithm
		
		CurveBallRandomization cbl1;
		
		//initializing p-values and mean
		ndf1.initializeSimulated();
		
		//initializing randomization object
		cbl1 = new CurveBallRandomization(sbm1, iRandomSeed);
		
		//randomizing
		cbl1.randomize(25000);
		return cbl1.currentRandomizedMatrix();
	}
	
	
	private void runFixedFixed(String sAxis, int iIterations, int iRandomSeed){
		
		//sbmSim = current simulated matrix
		//cbl1 = curve ball randomization algorithm
		
		CurveBallRandomization cbl1;
		SparseBinaryMatrix sbmSim;
		
		//initializing p-values and mean
		ndf1.initializeSimulated();
		
		//initializing matrix
		sbmSim = sbm1;
		
		//initializing randomization object
		cbl1 = new CurveBallRandomization(sbm1, iRandomSeed);
		
		//randomizing
		for(int i=0;i<iIterations;i++){
			if(i%100 == 0){	
				System.out.println("Null model iteration " + (i+1) + " of " + iIterations + "...");
			}
			cbl1.randomize(1000);
			sbmSim = cbl1.currentRandomizedMatrix();
			ndf1.addSimulatedNODF(calculateNODF(sbmSim, sAxis));
		}
				
		//updating simulated values
		ndf1.calculateSimulatedSummaries();
	}
	
	private void runFixedEquiprobable(String sAxis, int iIterations){
		
		//sbmSim = current simulated matrix
		//rgo1 = current selected samples
		
		SparseBinaryMatrix sbmSim;
		Object rgo1[];
		
		//initializing p-values and mean
		ndf1.initializeSimulated();
		
		//looping through iterations
		for(int i=0;i<iIterations;i++){
			
			//updating progress
			if(i%100 == 0){	
				System.out.println("Null model iteration " + (i+1) + " of " + iIterations + "...");
			}
				
			//loading matrix
			sbmSim = new SparseBinaryMatrix((int) sbm1.getRowCount(), (int) sbm1.getColCount());
			
			//looping through observations and loading simulated matrix
			for(String s:sbm1.getObservationIDs()){
				rgo1 = rnd1.nextSample(sbm1.getSampleIDs(), sbm1.getRowSum(s));
				for(Object o:rgo1){
					sbmSim.add(s,(String) o);
				}
			}
			
			//loading simulated NODF value
			ndf1.addSimulatedNODF(calculateNODF(sbmSim, sAxis));
		}
		
		//updating simulated values
		ndf1.calculateSimulatedSummaries();
	}

	private void runEquiprobableFixed(String sAxis, int iIterations){
		
		//sbmSim = current simulated matrix
		//rgo1 = current selected samples
		
		SparseBinaryMatrix sbmSim;
		Object rgo1[];
		
		//initializing p-values and mean
		ndf1.initializeSimulated();
		
		//looping through iterations
		for(int i=0;i<iIterations;i++){
			
			//updating progress
			if(i%100 == 0){
				System.out.println("Null model iteration " + (i+1) + " of " + iIterations + "...");
			}
			
			//loading matrix
			sbmSim = new SparseBinaryMatrix((int) sbm1.getRowCount(), (int) sbm1.getColCount());
			
			//looping through samples and loading simulated matrix
			for(String s:sbm1.getSampleIDs()){
				rgo1 = rnd1.nextSample(sbm1.getObservationIDs(), sbm1.getColSum(s));
				for(Object o:rgo1){
					sbmSim.add((String) o,s);
				}
			}
			
			//loading simulated NODF value
			ndf1.addSimulatedNODF(calculateNODF(sbmSim, sAxis));
		}
		
		//updating counts
		ndf1.calculateSimulatedSummaries();
	}

	/**
	 * Calculates NODF
	 * @param sbl1 Presence-absence matrix to use
	 * @param sps1 Pairs of samples and observations to use
	 * @param sAxis Axis along which to calculate NODF: "sample", "observation", or "both"
	 * @return NODF value.
	 */
	protected double calculateNODF(SparseBinaryMatrix sbm1, String sAxis){
		
		//dOut = output
		//iSum1 = sum for key 1
		//iSum2 = sum for key 2
		//iShared = current shared count
		//dSumSmall = smaller sum
		//sKeySmall = smaller key
		//sKeyLarge = larger key
		//dObs = observation axis value
		//dSam = sample axis value
		//dCounter = counter
		
		double dOut;
		double dObs;
		double dSam;
		int iSum1;
		int iSum2;
		int iShared;
		double dSumSmall;
		double dCounter;
		String sKeySmall;
		String sKeyLarge;
		
		dOut = 0.;
		dCounter = 0.;
		if(sAxis.equals("sample")){
			for(SemiOrderedPair<String> spr1:gphSamples.setEdges){
				iShared = 0;			
				iSum1 = sbm1.getColSum(spr1.o1);
				iSum2 = sbm1.getColSum(spr1.o2);
				if(iSum1==0 || iSum2 == 0){
					continue;
				}else{
					dCounter++;
				}
				if(iSum1<iSum2){
					dSumSmall = (double) iSum1;
					sKeySmall = spr1.o1;
					sKeyLarge = spr1.o2;
				}else if(iSum1>iSum2){
					dSumSmall = (double) iSum2;
					sKeySmall = spr1.o2;
					sKeyLarge = spr1.o1;
				}else{
					continue;
				}
					
				for(String s:sbm1.getColumn(sKeySmall)){
					if(sbm1.getColumn(sKeyLarge).contains(s)){
						iShared++;
					}
				}
				dOut += ((double) iShared)/dSumSmall;
			}
			dOut = dOut/dCounter;
		}else if(sAxis.equals("observation")){
			for(SemiOrderedPair<String> spr1:gphObservations.setEdges){
				iShared = 0;				
				iSum1 = sbm1.getRowSum(spr1.o1);
				iSum2 = sbm1.getRowSum(spr1.o2);
				if(iSum1==0 || iSum2 == 0){
					continue;
				}else{
					dCounter++;
				}
				if(iSum1<iSum2){
					dSumSmall = (double) iSum1;
					sKeySmall = spr1.o1;
					sKeyLarge = spr1.o2;
				}else if(iSum1>iSum2){
					dSumSmall = (double) iSum2;
					sKeySmall = spr1.o2;
					sKeyLarge = spr1.o1;
				}else{
					continue;
				}
					
				for(String s:sbm1.getRow(sKeySmall)){
					if(sbm1.getRow(sKeyLarge).contains(s)){
						iShared++;
					}
				}
				dOut += ((double) iShared)/dSumSmall;
			}		
			dOut = dOut/dCounter;
		}else if(sAxis.equals("both")){
			dObs = calculateNODF(sbm1, "observation");
			dSam = calculateNODF(sbm1, "sample");
			dOut = (0.5*sbm1.getRowCount()*(sbm1.getRowCount()-1)*dObs + 0.5*sbm1.getColCount()*(sbm1.getColCount()-1)*dSam)
					/(0.5*sbm1.getRowCount()*(sbm1.getRowCount()-1) + 0.5*sbm1.getColCount()*(sbm1.getColCount()-1));	
		}
		return dOut;
	}

	/**
	 * Calculates NODF expectation value and variance
	 * @param sbl1 Presence-absence matrix to use
	 * @param sps1 Pairs of samples and observations to use
	 * @param sAxis Axis along which to calculate NODF: "sample", "observation", or "both"
	 * @return Expectation value (0) and variance (1)
	 */
	protected double[] calculateNODFExpectationVariance(SparseBinaryMatrix sbm1, String sAxis, String sNullModel) throws Exception{
		
		//rgdOut = output
		
		double rgdOut[];
		
		rgdOut = new double[2];
		if(sAxis.equals("sample") && sNullModel.equals("equiprobablefixed")){
			rgdOut[0]=this.findEquiprobableFixedSampleExpectation();
			rgdOut[1]=this.findEquiprobableFixedSampleVariance();
		}else if(sAxis.equals("observation") && sNullModel.equals("fixedequiprobable")){
			rgdOut[0]=this.findFixedEquiprobableObsExpectation();
			rgdOut[1]=this.findFixedEquiprobableObsVariance();
		}else{
			throw new Exception("No analytic expressions for axis/null model combination. Exiting.");
		}
		return rgdOut;
	}

	protected class CurveBallRandomization{
		
		/**Initial sparse binary matrix**/
		private SparseBinaryMatrix sbmInitial;
		
		/**Current sparse binary matrix**/
		private SparseBinaryMatrix sbm1;
		
		/**Current sparse binary matrix in long list format**/
		private LongLists lls1;
		
		/**Random seed**/
		private int iRandomSeed;
		
		private CurveBallRandomization(SparseBinaryMatrix sbmInitial, int iRandomSeed){
			this.sbmInitial=sbmInitial;
			toLongLists();
			this.iRandomSeed = iRandomSeed;
			toLongLists();
		}
		
		private void toLongLists(){
			if(sbmInitial.getColCount()>sbmInitial.getRowCount()){
				lls1 = new LongLists((int) sbmInitial.getRowCount(), iRandomSeed);
				for(String sObservation:sbmInitial.getObservationIDs()){
					lls1.put(sObservation, sbmInitial.getRow(sObservation));	
				}
			}else{
				lls1 = new LongLists((int) sbmInitial.getColCount(), iRandomSeed);
				for(String sSample:sbmInitial.getSampleIDs()){
					lls1.put(sSample, sbmInitial.getColumn(sSample));	
				}
			}
		}
		
		private void randomize(int iIterations){
			
			for(int i=0;i<iIterations;i++){
				lls1.extractPair();
			}
			sbm1 = new SparseBinaryMatrix((int) sbmInitial.getRowCount(), (int) sbmInitial.getColCount());
			if(sbmInitial.getColCount()>sbmInitial.getRowCount()){
				for(String sObservation: lls1.keySet() ){
					for(String sSample: lls1.get(sObservation)){
						sbm1.add(sObservation, sSample);
					}
				}
			}else{
				for(String sSample: lls1.keySet() ){
					for(String sObservation: lls1.get(sSample)){
						sbm1.add(sObservation, sSample);
					}
				}
			}
		}
		
		private SparseBinaryMatrix currentRandomizedMatrix(){
			return sbm1;
		}
		
	}
	
	private class LongLists extends HashMap<String,Set<String>>{
		
		private static final long serialVersionUID = 1L;

		/**List of keys**/
		private ArrayList<String> lst1;
		
		/**Number of keys**/
		private int iKeys;
		
		/**Maximum number of values**/
		private int iValuesMax;
		
		/**Random number generator**/
		private Random rnd1;
		
		
		public LongLists(int iExpectedElements, int iRandomSeed){
			super(iExpectedElements);
			lst1 = new ArrayList<String>(iExpectedElements);
			iKeys = 0;
			iValuesMax = -9999;
			rnd1 = new Random(iRandomSeed);
		}
		
		public Set<String> put(String sKey, Set<String> setValues){
			if(!super.containsKey(sKey)){
				lst1.add(sKey);
				iKeys++;
			}else{
				try{
					throw new Exception();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			if(setValues.size()>iValuesMax){
				iValuesMax = setValues.size();
			}
			return super.put(sKey, setValues);
		}
		
		public void extractPair(){
			
			//rgs1 = pair of keys
			//map1 = map of exclusive elements
			
			String rgs1[];
			ListMultimap<String,String> map1;
			
			rgs1 = randomPairOfKeys();
			map1 = exclusiveElementsInRandomOrder(rgs1);
			swapExclusiveElements(map1, rgs1);
		}
		
		private String[] randomPairOfKeys(){
			
			//i1 = first index
			//i2 = second index
			
			int i1;
			int i2;
			
			i1 = rnd1.nextInt(iKeys);
			do{
				i2 = rnd1.nextInt(iKeys);
			}while(i2==i1);
			return new String[]{lst1.get(i1), lst1.get(i2)};
		}
		
		private ListMultimap<String,String> exclusiveElementsInRandomOrder(String[] rgsKeys){
			
			//map1 = output
			//lst1 = first list
			//lst2 = second list
			
			ListMultimap<String,String> map1;
			ArrayList<String> lst1;
			ArrayList<String> lst2;
			
			map1 = ArrayListMultimap.create(2, iValuesMax);
			lst1 = new ArrayList<String>(iValuesMax);
			lst2 = new ArrayList<String>(iValuesMax);
			for(String s:this.get(rgsKeys[0])){
				if(!this.get(rgsKeys[1]).contains(s)){
					lst1.add(s);
				}
			}
			for(String s:this.get(rgsKeys[1])){
				if(!this.get(rgsKeys[0]).contains(s)){
					lst2.add(s);
				}
			}
			Collections.shuffle(lst1);
			Collections.shuffle(lst2);
			map1.putAll(rgsKeys[0],lst1);
			map1.putAll(rgsKeys[1],lst2);
			return map1;
		}
		
		private void swapExclusiveElements(ListMultimap<String,String> mapExclusiveElements, String rgsKeys[]){
			
			//i1 = maximum number of elements
			//i2 = number of elements to swap
			//s1 = object currently being moved
			
			int i1;
			int i2;
			String s1;
			
			if(!mapExclusiveElements.containsKey(rgsKeys[0]) || !mapExclusiveElements.containsKey(rgsKeys[1])){
				return;
			}
			i1 = Math.min(
					mapExclusiveElements.get(rgsKeys[0]).size(), 
					mapExclusiveElements.get(rgsKeys[1]).size());
			i2 = rnd1.nextInt(i1+1);
			for(int i=0;i<i2;i++){
				s1 = mapExclusiveElements.get(rgsKeys[0]).get(i);
				this.get(rgsKeys[0]).remove(s1);
				this.get(rgsKeys[1]).add(s1);
				s1 = mapExclusiveElements.get(rgsKeys[1]).get(i);
				this.get(rgsKeys[1]).remove(s1);
				this.get(rgsKeys[0]).add(s1);
			}
		}
	}
	
	protected class SparseBinaryMatrix{
		
		/**Row map**/
		private HashMultimap<String,String> mapRow;
		
		/**Number of rows**/
		private int iRows;
		
		/**Number of columns**/
		private int iCols;
		
		/**Column map**/
		private HashMultimap<String,String> mapCol;
		
		/**Row totals**/
		private HashMap_AdditiveInteger<String> mapRowTotal;
		
		/**Column totals**/
		private HashMap_AdditiveInteger<String> mapColTotal;
		
		private SparseBinaryMatrix(int iObservations, int iSamples){
		
			//initializing row and column maps
			mapRow = HashMultimap.create(iObservations,10);
			mapCol = HashMultimap.create(iSamples,10);
			mapRowTotal = new HashMap_AdditiveInteger<String>(iObservations);
			mapColTotal = new HashMap_AdditiveInteger<String>(iSamples);
			
			//saving number of rows and columns
			iRows = iObservations;
			iCols = iSamples;
		}
		
		private void add(String sObservationID, String sSampleID){
			//if(!mapRow.containsKey(sObservationID)){
			//	iRows++;
			//}
			mapRow.put(sObservationID, sSampleID);
			mapRowTotal.putSum(sObservationID, 1);
			//if(!mapCol.containsKey(sSampleID)){
			//	iCols++;
			//}
			mapCol.put(sSampleID,sObservationID);
			mapColTotal.putSum(sSampleID, 1);
		}
		
		protected Set<String> getRow(String sObservationID){
			return mapRow.get(sObservationID);
		}
		
		protected Set<String> getColumn(String sSampleID){
			return mapCol.get(sSampleID);
		}
		
		private Set<String> getObservationIDs(){
			return mapRow.keySet();
		}
		
		private Set<String> getSampleIDs(){
			return mapCol.keySet();
		}
		
		protected int getRowSum(String sObservationID){
			if(mapRowTotal.containsKey(sObservationID)){
				return mapRowTotal.get(sObservationID);
			}else{
				return 0;
			}
		}
		
		protected int getColSum(String sSampleID){
			if(mapColTotal.containsKey(sSampleID)){
				return mapColTotal.get(sSampleID);
			}else{
				return 0;
			}
		}
		
		protected double getRowCount(){
			return (double) mapRow.keySet().size();
			//return (double) iRows;
		}
		
		protected double getColCount(){
			return (double) mapCol.keySet().size();
			//return (double) iCols;
		}
	}
	
	
	
	
	public class NODF{
		
		/**Observed value of NODF**/
		public double dObserved = Double.NaN;
		
		/**Standardized effect size**/
		public double dStandardizedEffect = Double.NaN;
		
		/**Expectation from hypergeometric distribution**/
		public double dExpectation = Double.NaN;
		
		/**Variance from hypergeometric distribution**/
		public double dVariance = Double.NaN;
		
		/**Probability of less than observed NODF value**/
		public double dPrLT = Double.NaN;
		
		/**Probability of equal to observed NODF value**/
		public double dPrET = Double.NaN;
		
		/**Probability of greater than observed NODF value**/
		public double dPrGT = Double.NaN;
		
		/**Mean simulated NODF**/
		public double dSimulatedMean = Double.NaN;
		
		/**Standard deviation of simulated NODF**/
		public double dSimulatedStDev = Double.NaN;
		
		/**Number of simulations**/
		public double dSimulatedCount = Double.NaN;
		
		/**Sum of simulated values**/
		private double dSimulatedSum = Double.NaN;
		
		/**Sum of simulated values squared**/
		private double dSimulatedSum2 = Double.NaN;
		
		public NODF(){
		}
		
		private void initializeSimulated(){
			dPrLT = 0.;
			dPrET = 0.;
			dPrGT = 0.;
			dSimulatedSum = 0.;
			dSimulatedSum2 = 0.;
			dSimulatedCount = 0.;
		}
		
		/**
		 * Finds standardized effect size
		 * @param rgd1 Expectation value (0) and variance (1)
		 */
		private void findSES(double[] rgd1){
			this.dExpectation = rgd1[0];
			this.dVariance = rgd1[1];
			this.dStandardizedEffect = (this.dObserved - this.dExpectation)/(Math.sqrt(this.dVariance));
		}
		
		private void addObservedNODF(double dNODF){
			dObserved = dNODF;
		}
		
		private void addSimulatedNODF(double dNODF){
			
			//updating variables
			if(dNODF<ndf1.dObserved){
				dPrLT++;
			}else if(dNODF>ndf1.dObserved){
				dPrGT++;
			}else if(dNODF==ndf1.dObserved){
				dPrET++;
			}
			
			//updating sums and count
			dSimulatedSum += dNODF;
			dSimulatedSum2 += dNODF*dNODF;
			dSimulatedCount++;
		}
		
		private void calculateSimulatedSummaries(){
			
			dSimulatedMean = dSimulatedSum/dSimulatedCount;
			dSimulatedStDev = (dSimulatedSum2-dSimulatedSum*dSimulatedSum/dSimulatedCount)/(dSimulatedCount-1.);
			dSimulatedStDev = Math.sqrt(dSimulatedStDev);
			dStandardizedEffect = (dObserved - dSimulatedMean)/dSimulatedStDev;
			
			dPrLT/=dSimulatedCount;
			dPrET/=dSimulatedCount;
			dPrGT/=dSimulatedCount;
		}
	}
}
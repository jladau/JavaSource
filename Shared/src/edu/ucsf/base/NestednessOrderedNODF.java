package edu.ucsf.base;

import edu.ucsf.io.BiomIO;

/**
 * Calculates nesteness for a given matrix.
 * @author jladau
 */

public class NestednessOrderedNODF extends NestednessNODF{

	public NestednessOrderedNODF(BiomIO bio1, int iRandomSeed) throws Exception{
		super(bio1,iRandomSeed);	
	}
	
	public double findEquiprobableFixedSampleExpectation(){
		
		//dOut = output
		//iSum1 = first sum
		//iSum2 = second sum
		//dSumSmall = smaller sum
		//sSimLarge = larger sum
		//dRows = number of rows
		
		double dOut;
		int iSum1; int iSum2;
		double dSumSmall; 
		double dSumLarge;
		double dRows;
		
		dOut = 0.;
		//dRows = (double) sbm1.getRowCount();
		dRows = (double) this.iObservations;
		for(SemiOrderedPair<String> spr1:gphSamples.setEdges){
			iSum1 = sbm1.getColSum(spr1.o1);
			iSum2 = sbm1.getColSum(spr1.o2);
			if(iSum1<iSum2){
				dSumSmall = (double) iSum1;
				dSumLarge = (double) iSum2;
			}else{
				continue;
			}
			dOut += (dSumSmall*dSumLarge)/(dRows*dSumSmall);
		}
		return dOut/((double) gphSamples.setEdges.size());		
	}
	
	public double findFixedEquiprobableObsVariance(){
		return findFixedEquiprobableObsVariance((double) this.iSamples);
	}
	
	public double findFixedEquiprobableObsExpectation(){
		
		//dOut = output
		//iSum1 = first sum
		//iSum2 = second sum
		//dSumSmall = smaller sum
		//sSimLarge = larger sum
		//dCols = number of columns
		
		double dOut;
		int iSum1; int iSum2;
		double dSumSmall; 
		double dSumLarge;
		double dCols;
		
		dOut = 0.;
		//dCols = (double) sbm1.getColCount();
		dCols = (double) this.iSamples;
		for(SemiOrderedPair<String> spr1:gphObservations.setEdges){
			iSum1 = sbm1.getRowSum(spr1.o1);
			iSum2 = sbm1.getRowSum(spr1.o2);
			if(iSum1<iSum2){
				dSumSmall = (double) iSum1;
				dSumLarge = (double) iSum2;
			}else{
				continue;
			}
			dOut += (dSumSmall*dSumLarge)/(dCols*dSumSmall);
		}
		return dOut/((double) gphObservations.setEdges.size());		
	}

	public double findEquiprobableFixedSampleVariance(){
		return findEquiprobableFixedSampleVariance((double) this.iObservations);
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
}
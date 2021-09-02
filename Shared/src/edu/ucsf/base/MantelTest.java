package edu.ucsf.base;

import java.util.ArrayList;
import static edu.ucsf.base.ExtendedMath.*;
import static java.lang.Math.*;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Runs a mantel test
 * @author jladau
 */


public class MantelTest {

	/**Observed correlation coeffcient**/
	private double dPearsonObs;
	
	/**List of simulated correlation coefficients**/
	private ArrayList<Double> lstPearsonSim;
	
	/**Denominator for Pearson coefficient (does not change over simulation iterations)**/
	private double dDenominator;
	
	/**Mean of x values**/
	private double dXBar;
	
	/**Mean of y values**/
	private double dYBar;
	
	public MantelTest(Table<String,String,Double> tblX, Table<String,String,Double> tblY, int iIterations){
		
		//per1 = permutation object
		//tblYPer = permuted X matrix
		
		Permutation<String> per1;
		Table<String,String,Double> tblYPer;
		
		//loading denominator
		loadConstants(tblX,tblY);
		
		//loading observed value
		dPearsonObs = calculateNumerator(tblX,tblY)/dDenominator;
		
		//loading simulated values
		lstPearsonSim = new ArrayList<Double>(iIterations);
		per1 = new Permutation<String>(new ArrayList<String>(tblY.rowKeySet()));
		for(int i=0;i<iIterations;i++){
			
			if(i>0 && i%10==0){
				System.out.println("Mantel test iteration " + i + " of " + iIterations + "...");
			}
			
			tblYPer = HashBasedTable.create(tblY.rowKeySet().size(), tblY.columnKeySet().size());
			per1.loadRandomPermutation();
			for(String s:tblY.rowKeySet()){
				for(String t:tblY.columnKeySet()){
					tblYPer.put(per1.getImage(s), per1.getImage(t), tblY.get(s,t));
				}
			}
			lstPearsonSim.add(calculateNumerator(tblX,tblYPer)/dDenominator);
		}
	}
	
	public double getObservedCorrelation(){
		return dPearsonObs;
	}
	
	public double getPrGreaterThanObs(){
		
		//dOut = output
		
		double dOut;
		
		dOut =0.;
		for(Double d:lstPearsonSim){
			if(d>=dPearsonObs){
				dOut++;
			}
		}
		return dOut/((double) lstPearsonSim.size());
	}
	
	public double getPrLessThanObs(){
		
		//dOut = output
		
		double dOut;
		
		dOut =0.;
		for(Double d:lstPearsonSim){
			if(d<=dPearsonObs){
				dOut++;
			}
		}
		return dOut/((double) lstPearsonSim.size());
	}
	
	private double calculateNumerator(Table<String,String,Double> tblX, Table<String,String,Double> tblY){
		
		//dOut = output
		
		double dOut;
		
		dOut = 0;
		for(String s:tblX.rowKeySet()){
			for(String t:tblX.columnKeySet()){
				dOut+=(tblX.get(s,t)-dXBar)*(tblY.get(s, t)-dYBar);
			}
		}
		return dOut;
	}
	
	private void loadConstants(Table<String,String,Double> tblX, Table<String,String,Double> tblY){
		
		//lstX = list of X values
		//lstY = list of Y values
		
		ArrayList<Double> lstX;
		ArrayList<Double> lstY;
		
		lstX = new ArrayList<Double>(tblX.size());
		lstY = new ArrayList<Double>(tblX.size());
		for(String s:tblX.rowKeySet()){
			for(String t:tblX.columnKeySet()){
				lstX.add(tblX.get(s,t));
				lstY.add(tblY.get(s,t));
			}
		}
		dDenominator = sqrt(sumOfPowersMeanCentered(lstX, 2))*Math.sqrt(sumOfPowersMeanCentered(lstY, 2));
		dXBar = mean(lstX);
		dYBar = mean(lstY);
	}
}

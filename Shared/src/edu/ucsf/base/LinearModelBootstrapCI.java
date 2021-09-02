package edu.ucsf.base;

import static java.lang.Math.pow;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.Table;

/**
 * Calculates bootstrap confidence intervals for linear model statistics.
 * @author jladau
 *
 */

public class LinearModelBootstrapCI extends LinearModel{

	/**Original X and Y matrices**/
	private double rgdX0[][] = null;
	private double rgdY0[] = null;
	
	/**For each statistic, returns array of resampled values**/
	private ArrayListMultimap<String,Double> mapResamples;
	
	/**For each statistic, returns cumulative distribution**/
	private HashMap<String,EmpiricalDistribution_Cumulative> mapEDF;
	
	/**Random number generator**/
	private Random rnd1;
	
	public LinearModelBootstrapCI(Table<String,String,Double> tblData, String sResponse, Set<String> setPredictors) throws Exception{
		super(tblData, sResponse, setPredictors);
		rnd1 = new Random(1234);
	}
	
	public void resample(int iIterations){
		
		//rgd1 = current values in primitive array
		
		double rgd1[];
		
		//************************************
		//for(int i=0;i<rgdX.length;i++){
		//	for(int j=0;j<rgdX[0].length;j++){
		//		System.out.print(rgdX[i][j] + ",");
		//	}
		//	System.out.println(rgdY[i]);
		//}
		//************************************
		
		
		//looping through resamples
		mapResamples = ArrayListMultimap.create(10,iIterations);
		for(int i=0;i<iIterations;i++){
			resample();
			try{
				
				//***********************
				//System.out.println(this.findPRESS());
				//***********************
				
				mapResamples.put("press", this.findPRESS());
				mapResamples.put("cvr2", 1.-findPRESS()/findTSS());
			}catch(Exception e){
				i--;
			}
		}
		
		//loading empirical distribution functions
		mapEDF = new HashMap<String,EmpiricalDistribution_Cumulative>(mapResamples.size());
		for(String s:mapResamples.keySet()){
			rgd1 = new double[mapResamples.get(s).size()];
			for(int i=0;i<mapResamples.get(s).size();i++){
				rgd1[i]=mapResamples.get(s).get(i);
			}
			mapEDF.put(s, new EmpiricalDistribution_Cumulative(rgd1));
		}
		
		//restoring
		restore();
	}
	
	public List<Double> getPRESSResamples(){
		return mapResamples.get("press");
	}
	
	public double findTSS(){
		
		//dMean = mean value
		//dOut = output
		
		double dMean; double dOut;
		
		//loading response variable
		dMean = 0;
		for(double d:rgdY){
			dMean+=d;
		}
		dMean=dMean/((double) rgdY.length);
		dOut = 0;
		for(double d:rgdY){
			dOut+=pow(d-dMean,2);
		}
		return dOut;
	}
	
	public Range<Double> getPRESSConfidenceInterval(double dAlpha){
		return getConfidenceInterval(dAlpha, "press");
	}
	
	public Range<Double> getCVR2ConfidenceInterval(double dAlpha){
		return getConfidenceInterval(dAlpha, "cvr2");
	}
	
	private Range<Double> getConfidenceInterval(double dAlpha, String sKey){
		
		//d1 = first value
		//d2 = second value
		
		double d1;
		double d2;
		
		d1 = mapEDF.get(sKey).inverseCumulativeProbability((1.-dAlpha)/2.);
		d2 = mapEDF.get(sKey).inverseCumulativeProbability(1.-(1.-dAlpha)/2.);
		return Range.closed(d1, d2);
	}
	
	
	private void resample(){
		
		//iRow = current random row

		int iRow;
		
		//saving copies
		if(rgdX0==null){
			rgdX0=rgdX;
			rgdY0=rgdY;
		}
		
		//initializing resamples
		rgdX = new double[rgdX0.length][rgdX0[0].length];
		rgdY = new double[rgdY0.length];
		
		//resampling
		for(int i=0;i<rgdX.length;i++){
			iRow = rnd1.nextInt(rgdX.length);
			for(int j=0;j<rgdX[0].length;j++){
				rgdX[i][j]=rgdX0[iRow][j];
			}
			rgdY[i]=rgdY0[iRow];
		}
		loadData(rgdX,rgdY);
		this.mapCoefficients=null;
	}
	
	private void restore(){
		
		rgdX = new double[rgdX0.length][rgdX0[0].length];
		rgdY = new double[rgdY0.length];
		for(int i=0;i<rgdX.length;i++){
			for(int j=0;j<rgdX[0].length;j++){
				rgdX[i][j]=rgdX0[i][j];
			}
			rgdY[i]=rgdY0[i];
		}
		loadData(rgdX,rgdY);
		this.mapCoefficients=null;
	}
}

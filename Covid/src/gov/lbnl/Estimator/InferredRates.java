package gov.lbnl.Estimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.base.HashMap_AdditiveInteger;
import edu.ucsf.base.LinearModel;

public class InferredRates{

	/**Data table: rows are times, columns are variable names, values are data values**/
	private HashBasedTable<Integer,String,Double> tblData;
	
	/**Response**/
	private String sResponse;
	
	/**Current candidate model fit**/
	private ModelFit fit1;
	
	/**Current best model fit**/
	private ModelFit fitBest;
	
	/**Number of time values**/
	private int iTimes;
	
	/**Minimum time**/
	private int iTimeMin;
	
	/**Maximum time**/
	private int iTimeMax;
	
	/**List of predictors**/
	private ArrayList<String> lstPredictors;
	
	/**Map from predictors to number of non-zero counts**/
	private HashMap_AdditiveInteger<String> mapNonzeroCounts;
	
	/**List of times in string format for fast iteration**/
	private ArrayList<String> lstTimes;
	
	public InferredRates(HashBasedTable<Integer,String,Double> tblData, ArrayList<String> lstPredictors, String sResponse, int iTimeStart, int iTimeEnd){
		this.lstPredictors = lstPredictors;
		this.sResponse = sResponse;
		loadPreformattedData(tblData,iTimeStart,iTimeEnd);
		this.iTimes = this.tblData.rowKeySet().size();
	}
	
	public void inferRates(double dSlopeStart, double dSlopeEnd, double dSlopeStep) throws Exception{
		
		//i1 = counter
		//dSlope = current slope
		
		int i1;
		double dSlope;
	
		fitBest = new ModelFit();
		fitBest.dMSE = Double.MAX_VALUE;
		i1 = 0;
		do{
			dSlope = dSlopeStart+i1*dSlopeStep;
			if(checkFit(dSlope)==0){
				if(fit1.dMSE>1000000000){
					System.out.println("Warning: MSE greater than 10^9.");
				}
				if(fit1.dMSE<fitBest.dMSE){
					fitBest.setEqualTo(fit1);
				}
				System.out.println("Slope " + dSlope + " log(MSE) = " + Math.log10(fit1.dMSE));
			}
			i1++;
		}while(dSlopeStart+i1*dSlopeStep<dSlopeEnd);
	}

	public double slope(){
		return fitBest.dSlope;
	}

	public HashBasedTable<Integer,String,Double> coefficients(){
		
		//tbl1 = output, table giving estimates for each time and variable
		//iTimeOriginal = time in original format
		//dTime = current time in double format
		
		HashBasedTable<Integer,String,Double> tbl1;
		double dTime;
		int iTimeOriginal;
		
		tbl1 = HashBasedTable.create(fitBest.tbl1.columnKeySet().size(),fitBest.tbl1.rowKeySet().size());
		for(String sTime:fitBest.tbl1.columnKeySet()){
			iTimeOriginal = Integer.parseInt(sTime) + iTimeMin;
			dTime = Double.parseDouble(sTime);
			for(String sVar:fitBest.setPredictors){
				
				//***************************
				//System.out.println(fitBest.mapCoefficients.get(sVar));
				//***************************
				
				if(fitBest.mapCoefficients.get(sVar)>0){
					tbl1.put(iTimeOriginal, sVar, fitBest.mapCoefficients.get(sVar)*Math.pow(fitBest.dSlope,dTime));
				}
			}
		}
		return tbl1;
	}

	private int checkFit(double dSlope) throws Exception{
		
		//sTime = current time in string format
		//lnm1 = linear model
		//d1 = current slope value
		//dTime = time in double format
		
		double dTime;
		double d1;
		String sTime;
		LinearModel lnm1;
		
		//Clearing variables
		fit1 = new ModelFit();
	
		//Loading formatted data table
		fit1.tbl1 = HashBasedTable.create(lstPredictors.size()+5,iTimes);
		for(Integer i:tblData.rowKeySet()) {
			sTime = Integer.toString(i);
			dTime = Double.parseDouble(sTime);
			d1 = Math.pow(dSlope,dTime);
			for(String s:lstPredictors){
				fit1.tbl1.put(s,sTime,tblData.get(i,s)*d1);		
			}
			fit1.tbl1.put(sResponse,sTime,tblData.get(i,sResponse));
		}
		fit1.setPredictors = new HashSet<String>(lstPredictors);
		
		lnm1 = new LinearModel(fit1.tbl1, sResponse, fit1.setPredictors, true);
		lnm1.fitModel(fit1.setPredictors);
		
		if(reduceMulticollinearity(lnm1.findVIF())==true) {
			lnm1 = new LinearModel(fit1.tbl1, sResponse, fit1.setPredictors, true);
			lnm1.fitModel(fit1.setPredictors);
		}
		
		loadZeroTruncatedCoefficients(lnm1);
		loadMSE(fit1.tbl1);
			
		//loading current coefficients
		fit1.dSlope = dSlope;
		return 0;
	}

	private void loadPreformattedData(HashBasedTable<Integer,String,Double> tblData, int iTimeStart, int iTimeEnd) {
		
		//map1 = map of sums for each predictor
		//setNonZero = set of predictors with non zero sums
		//iTime = current time
		
		HashMap_AdditiveDouble<String> map1;
		HashSet<String> setNonZero;
		int iTime;
		
		
		//**********************************
		//for(Integer i:tblData.rowKeySet()){
		//	for(String s:tblData.columnKeySet()){
		//		System.out.println(i + "," + s + "," + tblData.get(i,s));
		//	}
		//}
		//**********************************
		
		
		//Loading set of predictors with non-zero sums
		map1 = new HashMap_AdditiveDouble<String>(lstPredictors.size());
		for(Integer i:tblData.rowKeySet()) {
			if(iTimeStart<=i && i<=iTimeEnd){
				for(String s:lstPredictors){
					map1.putSum(s,tblData.get(i,s));
				}
			}
		}
		setNonZero = new HashSet<String>(map1.size());
		for(String s:map1.keySet()) {
			if(map1.get(s)>0){
				setNonZero.add(s);
			}
		}
		
		//loading minimum and maximum times
		iTimeMin = Integer.MAX_VALUE;
		iTimeMax = -Integer.MAX_VALUE;
		for(Integer i:tblData.rowKeySet()){
			if(iTimeStart<=i && i<=iTimeEnd){
				if(i<iTimeMin){
					iTimeMin = i;
				}
				if(i>iTimeMax){
					iTimeMax = i;
				}
			}
		}
		
		//Loading preformatted data table
		this.tblData = HashBasedTable.create(tblData.rowKeySet().size(),tblData.columnKeySet().size());
		for(Integer i:tblData.rowKeySet()) {
			if(iTimeStart<=i && i<=iTimeEnd){
				if(tblData.get(i,sResponse)>0){
					iTime = i-iTimeMin;
					for(String s:setNonZero){
						this.tblData.put(iTime,s,tblData.get(i,s));
					}
					this.tblData.put(iTime,sResponse,tblData.get(i,sResponse));
				}
			}
		}
		
		//Updating predictors list
		lstPredictors = new ArrayList<String>(setNonZero);
		
		//Finding non-zero counts
		mapNonzeroCounts = new HashMap_AdditiveInteger<String>(lstPredictors.size());
		for(String s:lstPredictors){
			for(Integer i:tblData.rowKeySet()){
				if(tblData.get(i,s)>0){
					mapNonzeroCounts.putSum(s,1);
				}
			}
		}
		
		//Finding list of times in string format
		lstTimes = new ArrayList<String>(this.tblData.rowKeySet().size());
		for(Integer i:this.tblData.rowKeySet()){
			lstTimes.add(Integer.toString(i));
		}
	}
		
	private boolean reduceMulticollinearity(HashMap<String,Double> mapVIF){
		
		//lstMerge = list of predictors to merge
		//iMax = current maximum number of non-zero counts
		//sPredMax = predictor with maximum number of non-zero counts
		//s1 = merged string
		//d1 = current value being input
		
		ArrayList<String> lstMerge;
		int iMax;
		String sPredMax=null;
		String s1;
		double d1;
		
		//finding predictors to merge
		lstMerge = new ArrayList<String>(mapVIF.size());
		iMax = -Integer.MAX_VALUE;
		for(String s:mapVIF.keySet()){
			if(Double.isNaN(mapVIF.get(s)) || mapVIF.get(s)>10){
				lstMerge.add(s);
				if(mapNonzeroCounts.get(s)>iMax){
					iMax = mapNonzeroCounts.get(s);
					sPredMax = s;
				}
			}
		}
		
		//merging predictors
		if(lstMerge.size()>0){
			s1 = Joiner.on(";").join(lstMerge);
			for(String s:lstTimes){
				d1 = fit1.tbl1.get(sPredMax,s);
				fit1.tbl1.put(s1,s,d1);
				for(String sVar:lstMerge){
					fit1.tbl1.remove(sVar,s);
				}
			}
			fit1.setPredictors.add(s1);
			for(String sVar:lstMerge){
				fit1.setPredictors.remove(sVar);
			}
		}
		
		//returning value
		if(lstMerge.size()>0) {
			return true;
		}else{
			return false;
		}
	}
	
	private void loadZeroTruncatedCoefficients(LinearModel lnm1){
		
		//map1 = non-truncated estimates
		
		HashMap<String,Double> map1;
		
		map1 = lnm1.findCoefficientEstimates();
		fit1.mapCoefficients = new HashMap<String,Double>(map1.size());
		for(String s:map1.keySet()){
			if(map1.get(s)<0){
				fit1.mapCoefficients.put(s,0.);
			}else{
				fit1.mapCoefficients.put(s,map1.get(s));
			}
		}
	}
	
	private void loadMSE(HashBasedTable<String,String,Double> tbl1){
		
		//d1 = current estimate
		//d2 = counter
		
		double d1;
		double d2;
		
		d2 = 0;
		fit1.dMSE = 0.;
		for(String sTime:tbl1.columnKeySet()){
			d1 = 0;
			for(String sVar:fit1.setPredictors){
				d1+=tbl1.get(sVar,sTime)*fit1.mapCoefficients.get(sVar);
			}
			d2+=1.;
			fit1.dMSE+=(tbl1.get(sResponse,sTime)-d1)*(tbl1.get(sResponse,sTime)-d1);
		}
		fit1.dMSE=fit1.dMSE/d2;
	}
	
	private class ModelFit {
		
		/**MSE value**/
		private double dMSE;
		
		/**Map with coefficients giving lowest mean squared error, including slope**/
		private HashMap<String,Double> mapCoefficients;
		
		/**Data table**/
		private HashBasedTable<String,String,Double> tbl1;
		
		/**Slope term**/
		private double dSlope;
		
		/**Predictors**/
		private HashSet<String> setPredictors;
		
		private ModelFit(){
		}
		
		private void setEqualTo(ModelFit fit1){
			this.dMSE = fit1.dMSE;
			this.mapCoefficients = new HashMap<String,Double>(fit1.mapCoefficients);
			this.tbl1 = fit1.tbl1; 
			this.dSlope = fit1.dSlope;
			this.setPredictors = new HashSet<String>(fit1.setPredictors);
		}
	}
}
package gov.lbnl.InferCaseFatalityRates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.base.LinearModel;

public class InferredRates2{

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
	
	/**Set of predictors**/
	private HashSet<String> setPredictors;
	
	/**List of times**/
	private ArrayList<Integer> lstTimes;
	
	public InferredRates2(HashBasedTable<Integer,String,Double> tblData, ArrayList<String> lstPredictors, String sResponse, int iTimeStart, int iTimeEnd){
		this.lstPredictors = lstPredictors;
		this.sResponse = sResponse;
		loadPreformattedData(tblData,iTimeStart,iTimeEnd);
		this.iTimes = this.tblData.rowKeySet().size();
	}
	
	private void loadPreformattedData(HashBasedTable<Integer,String,Double> tblData, int iTimeStart, int iTimeEnd) {
		
		//map1 = map of sums for each predictor
		//setNonZero = set of predictors with non zero sums
		//iTime = current time
		
		HashMap_AdditiveDouble<String> map1;
		HashSet<String> setNonZero;
		int iTime;
		
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
				iTime = i-iTimeMin;
				for(String s:setNonZero){
					this.tblData.put(iTime,s,tblData.get(i,s));
				}
				this.tblData.put(iTime,sResponse,tblData.get(i,sResponse));
			}
		}
		lstPredictors = new ArrayList<String>(setNonZero);
		
		//merging linearly dependent predictors
		mergeLinearlyDependentPredictors();
	}
	
	private void mergeLinearlyDependentPredictors(){
		
		//sPred1 = first predictor
		//sPred2 = second predictor
		//lstSets = list of hashsets
		//bPlaced = flag for whether placed
		
		String sPred1;
		String sPred2;
		ArrayList<HashSet<String>> lstSets;
		boolean bPlaced;
		
		lstTimes = new ArrayList<Integer>(tblData.rowKeySet());
		setPredictors = new HashSet<String>(lstPredictors);
		lstSets = new ArrayList<HashSet<String>>(10);
		for(int i=1;i<lstPredictors.size();i++){
			sPred1 = lstPredictors.get(i);
			for(int j=0;j<i;j++){
				sPred2=lstPredictors.get(j);
				if(predictorsLinearlyDependent(sPred1,sPred2)){
					bPlaced=false;
					for(int k=0;k<lstSets.size();k++) {
						if(lstSets.get(k).contains(sPred1)) {
							lstSets.get(k).add(sPred2);
							bPlaced=true;
							break;
						}else if(lstSets.get(k).contains(sPred2)){
							lstSets.get(k).add(sPred1);
							bPlaced=true;
							break;
						}
					}
					if(bPlaced==false){
						lstSets.add(new HashSet<String>(10));
						lstSets.get(lstSets.size()-1).add(sPred1);
						lstSets.get(lstSets.size()-1).add(sPred2);
					}
				}
			}
		}
		
		for(HashSet<String> set:lstSets){
			mergePredictors(set);
		}
	}
	
	private void mergePredictors(HashSet<String> setPredictorsToMerge) {
		
		//s1 = merged name
		//sPred1 = first predictor
		//d1 = value being replaced
		
		double d1;
		String s1;
		String sPredictor1=null;
		
		s1 = Joiner.on(";").join(setPredictorsToMerge);
		for(String s:setPredictorsToMerge){
			sPredictor1 = s;
			break;
		}
		for(Integer iTime:lstTimes){			
			d1 = tblData.get(iTime, sPredictor1);
			tblData.put(iTime,s1,d1);
			for(String s:setPredictorsToMerge){
				tblData.remove(iTime,s);
			}
		}
		for(String s:setPredictorsToMerge){
			setPredictors.remove(s);
		}
		setPredictors.add(s1);
	}
	
	private boolean predictorsLinearlyDependent(String sPredictor1, String sPredictor2){
		
		//dSx = sum of x
		//dSy = sum of y
		//dSxy = sum of xy
		//dSx2 = sum of x^2
		//dSy2 = sum of y^2
		//dx = current x value
		//dy = current y value
		//dN = n
		//dR = pearson correlation coefficient
		
		double dx;
		double dy;
		double dSx = 0.;
		double dSy = 0.;
		double dSxy = 0.;
		double dSx2 = 0.;
		double dSy2 = 0.;
		double dN;
		double dR;
		
		dN=0;
		for(Integer iTime: lstTimes){
			dx = tblData.get(iTime,sPredictor1);
			dy = tblData.get(iTime,sPredictor2);
			dSx += dx;
			dSy += dy;
			dSxy += dx*dy;
			dSx2 += dx*dx;
			dSy2 += dy*dy;
			dN++;
		}
		dR = (dN*dSxy-dSx*dSy)*(dN*dSxy-dSx*dSy);
		dR = dR/(dN*dSx2-dSx*dSx);
		dR = dR/(dN*dSy2-dSy*dSy);
		
		//*********************************
		//System.out.println(sPredictor1 + "," + sPredictor2 + "," + dR);
		//*********************************
		
		if(Math.abs(dR)>0.9999999999){
			return true;
		}else {
			return false;
		}
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
			
			//**************************
			dSlope = 1.;
			//**************************
			
			if(checkFit(dSlope)==0){
				if(fit1.dMSE<fitBest.dMSE){
					fitBest.setEqualTo(fit1);
				}
				
				//***********************
				//TODO MSE very unstable across slopes for state 21 and very large. Error?
				System.out.println(dSlope + "," + Math.log10(fit1.dMSE));
				//***********************
				
			}
			i1++;
		}while(dSlopeStart+i1*dSlopeStep<dSlopeEnd);
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
		fit1.tbl1 = HashBasedTable.create(setPredictors.size()+5,iTimes);
		for(Integer i:tblData.rowKeySet()) {
			sTime = Integer.toString(i);
			dTime = Double.parseDouble(sTime);
			d1 = Math.pow(dSlope,dTime);
			for(String s:setPredictors){
				fit1.tbl1.put(s,sTime,tblData.get(i,s)*d1);
		
				//***************************************
				//System.out.println(s + "," + sTime + "," + tblData.get(i,s)*d1);
				//***************************************
			
			}
			fit1.tbl1.put(sResponse,sTime,tblData.get(i,sResponse));
		}
		
		//***********************************
		//System.out.println("");
		//for(String s:fit1.tbl1.rowKeySet()){
		//	System.out.println(s);
		//}
		//***********************************
		
		//loading linear model
		try {
			lnm1 = new LinearModel(fit1.tbl1, sResponse, setPredictors, true);
		
			
			lnm1.fitModel(setPredictors);
			
			//************************
			HashMap<String,Double> mapTemp = lnm1.findVIF();
			for(String s:mapTemp.keySet()){
				System.out.println(s + "," + mapTemp.get(s));
			}
			//************************
			
			loadZeroTruncatedCoefficients(lnm1);
			loadMSE(fit1.tbl1);
		}catch(Exception e){
			return 1;
		}
			
		//loading current coefficients
		fit1.dSlope = dSlope;
		return 0;
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
			for(String sVar:setPredictors){
				tbl1.put(iTimeOriginal, sVar, fitBest.mapCoefficients.get(sVar)*Math.pow(fitBest.dSlope,dTime));
			}
		}
		return tbl1;
	}
	
	public int[] timeBounds() {
		return new int[]{iTimeMin,iTimeMax};
	}
	
	public HashMap<String,Double> meanCoefficients(double dOffset, boolean bInverse){
		
		//tbl1 = table giving estimates for each time and variable
		//map1 = output
		//d1 = current sum
		//d2 = current count
		
		HashBasedTable<Integer,String,Double> tbl1;
		HashMap<String,Double> map1;
		double d1;
		double d2;
		
		tbl1 = coefficients();
		map1 = new HashMap<String,Double>(tbl1.columnKeySet().size());
		for(String sVar:tbl1.columnKeySet()){
			d1 = 0.;
			d2 = 0.;
			for(Integer i:tbl1.rowKeySet()){
				if(bInverse==false){
					d1+=(dOffset + tbl1.get(i,sVar));
				}else {
					d1+=1./(dOffset + tbl1.get(i,sVar));
				}
				d2++;
			}
			map1.put(sVar,d1/d2);
		}
		return map1;
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
			for(String sVar:setPredictors){
				
				//**********************************
				//if(tbl1.get(sVar,sTime)*fit1.mapCoefficients.get(sVar)>1000000000){
				//	System.out.println(sTime + "," + sVar + "," + tbl1.get(sVar,sTime) + "," + fit1.mapCoefficients.get(sVar));
				//	System.out.println("HERE");
				//}
				//**********************************
				
				d1+=tbl1.get(sVar,sTime)*fit1.mapCoefficients.get(sVar);
			}
			d2+=1.;
			
			//******************************
			//TODO MSE very large and unstable: for two values of time, d1 is very large (greater than 10^15)
			//System.out.println(tbl1.get(sResponse,sTime) + "," + d1);
			//******************************
			
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
		
		private ModelFit(){
		}
		
		private void setEqualTo(ModelFit fit1){
			this.dMSE = fit1.dMSE;
			this.mapCoefficients = new HashMap<String,Double>(fit1.mapCoefficients);
			this.tbl1 = fit1.tbl1; 
			this.dSlope = fit1.dSlope;
		}
	}
}
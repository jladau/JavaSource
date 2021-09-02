package gov.lbnl.CountyCfrLeastSquares;

import java.util.HashMap;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.DataIO;

public class CfrLeastSquaresObjectiveFunction_1{

	/**Tolerance**/
	private final double TOLERANCE = 0.000001;
	
	/**Maximum iterations**/
	private final int MAX_ITERATIONS = 250;
	
	/**Map from time points to f_sigma values**/
	private HashMap<Double,Double> mapFSigma;
	
	/**Map from indices to current values of f_i**/
	private HashMap<String,Double> mapF;
	
	/**Table from time points and indices to mortality values**/
	private HashBasedTable<Double,String,Double> tblMortality;
	
	/**Map of sums**/
	private HashMap<Double,Double[]> mapSums;
	
	public CfrLeastSquaresObjectiveFunction_1(DataIO dat1){
		
		//s1 = current mortality location
		
		String s1;
		
		//loading variables
		mapFSigma = new HashMap<Double,Double>(dat1.iRows);
		mapF = new HashMap<String,Double>(dat1.iCols);
		tblMortality = HashBasedTable.create(dat1.iRows, dat1.iCols);
		for(int i=1;i<dat1.iRows;i++){
			mapFSigma.put(dat1.getDouble(i,"TIME_T"),dat1.getDouble(i,"F_SIGMA"));
		}
		for(int j=0;j<dat1.iCols;j++){
			if(dat1.getString(0,j).startsWith("M_")){
				s1 = dat1.getString(0,j).replace("M_","");
				for(int i=1;i<dat1.iRows;i++){
					tblMortality.put(dat1.getDouble(i,"TIME_T"),s1,dat1.getDouble(i,j));
				}
				mapF.put(s1,0.05);
			}
		}
		removeZeroes();
	}
	
	private void removeZeroes(){
		 loadSums();
		 for(Double t:mapSums.keySet()){
			 if(Math.abs(mapSums.get(t)[0])<0.000001){
				 mapFSigma.remove(t);
				 for(String s:mapF.keySet()){
					 tblMortality.remove(t,s);
				 }
			 }
		 }
		 mapSums=null;
	}
	
	private double value(){
		
		//d1 = output
		//dSumM = sum of mortalities
		//dSumMX = sum of mortalities*(1+e^x_i)
		
		double d1;
		double dSumM;
		double dSumMX;
		
		loadSums();
		d1 = 0;
		for(Double t:mapSums.keySet()){
			dSumM = mapSums.get(t)[0];
			dSumMX = mapSums.get(t)[1];
			if(dSumM>0){	
				d1+=(mapFSigma.get(t) - dSumM/dSumMX)*(mapFSigma.get(t) - dSumM/dSumMX);
			}
		}
		return d1;
	}
	
	private void loadSums(){
		
		//dSumM = sum of mortalities
		//dSumMX = sum of mortalities*(1+e^x_i)
		
		double dSumM;
		double dSumMX;
		
		if(mapSums==null) {
			mapSums = new HashMap<Double,Double[]>(mapFSigma.size());
			for(Double t:tblMortality.rowKeySet()){
				dSumM = 0;
				dSumMX = 0;
				for(String s:mapF.keySet()){
					dSumM+=tblMortality.get(t,s);
					dSumMX+=tblMortality.get(t,s)/mapF.get(s);
				}
				mapSums.put(t,new Double[]{dSumM, dSumMX});
			}
		}
	}
	
	public HashMap<Double,Double> observedValues(){
		return mapFSigma;
	}
	
	public HashMap<String,Double> parameterEstimates(){
		return mapF;
	}
	
	public HashMap<Double,Double> fitValues(){
		
		//map1 = output
		//dSumM = sum of mortalities
		//dSumMX = sum of mortalities*(1+e^x_i)
				
		HashMap<Double,Double> map1;
		double dSumM;
		double dSumMX;
		
		map1 = new HashMap<Double,Double>(mapFSigma.size());
		loadSums();
		for(Double t:mapFSigma.keySet()) {
			dSumM = mapSums.get(t)[0];
			dSumMX = mapSums.get(t)[1];
			map1.put(t,dSumM/dSumMX);
		}
		return map1;
	}
	
	public void leastSquares(){
		
		//d1 = previous value
		//d2 = current value
		//iCounter = counter
		
		double d1;
		double d2;
		int iCounter;
		
		iCounter = 0;
		do{
			d1 = value();
			for(String s:mapF.keySet()){
				minimize(s);
			}
			d2 = value();
			iCounter++;
		}while(Math.abs(d2-d1)>TOLERANCE && iCounter<=MAX_ITERATIONS);
	}
	
	private void minimize(String sIndex){
		
		//dStep = current step size
		//dF = current value of f
		//dValuePlus = function value plus step
		//dValueMinus = function value minus step
		
		double dStep;
		double dF;
		double dValuePlus;
		double dValueMinus;
		
		//*************************
		System.out.println("");
		for(double d=0.01;d<=1;d+=0.01){
			updateF(sIndex,d);
			System.out.println(d + "," + value());
		}
		//*************************
		
		dF = 1.;
		dStep = dF/2.;
		do{
			updateF(sIndex,dF + dStep);
			dValuePlus = value();
			updateF(sIndex,dF - dStep);
			dValueMinus = value();
			if(dValueMinus<dValuePlus){
				dF -= dStep;
			}else{
				dF += dStep;
			}
			updateF(sIndex, dF);
			dStep = dStep/2.;
		}while(dStep>TOLERANCE);
	}

	private void updateF(String sIndex, double dValue){
		mapF.put(sIndex, dValue);
		mapSums = null;
	}
}

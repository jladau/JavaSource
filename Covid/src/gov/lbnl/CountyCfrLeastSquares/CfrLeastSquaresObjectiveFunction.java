package gov.lbnl.CountyCfrLeastSquares;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.base.LinearModel;
import edu.ucsf.io.DataIO;

public class CfrLeastSquaresObjectiveFunction{

	/**Tolerance**/
	private final double TOLERANCE = 0.000001;
	
	/**Maximum iterations**/
	private final int MAX_ITERATIONS = 10000;
	
	/**Map from time points to c_sigma values**/
	private TreeMap<Double,Double> mapCSigma;
	
	/**Map from indices to current values of g_i**/
	private HashMap<String,Double> mapG;
	
	/**Map from indices to sum of mortalities squared**/
	private HashMap_AdditiveDouble<String> mapM2;
	
	/**Table from time points and indices to mortality values**/
	private HashBasedTable<Double,String,Double> tblMortality;
	
	/**Set of times in current window**/
	private HashSet<Double> setTimeWindow;
	
	/**Set of indices**/
	private HashSet<String> setPredictors;
	
	/**Table of data for linear model**/
	private HashBasedTable<String,String,Double> tblLMData;
	
	public CfrLeastSquaresObjectiveFunction(DataIO dat1){
		
		//loading variables
		mapCSigma = new TreeMap<Double,Double>();
		mapG = new HashMap<String,Double>(dat1.iCols);
		tblMortality = HashBasedTable.create(dat1.iRows, dat1.iCols);
		setPredictors = new HashSet<String>(dat1.iCols);
		for(int j=0;j<dat1.iCols;j++){
			if(dat1.getString(0,j).startsWith("M_")){
				setPredictors.add(dat1.getString(0,j).replace("M_",""));
			}
		}
		for(int i=1;i<=dat1.iRows;i++){
			if(!(Double.isNaN(dat1.getDouble(i,"TIME_T"))) && !(dat1.getDouble(i,"C_SIGMA")==0)){
				mapCSigma.put(dat1.getDouble(i,"TIME_T"),dat1.getDouble(i,"C_SIGMA"));
				for(String s:setPredictors){
					tblMortality.put(dat1.getDouble(i,"TIME_T"),s,dat1.getDouble(i,"M_" + s));
					
				}
			}
		}
		setTimeWindow = null;
		tblLMData = HashBasedTable.create(dat1.iCols, dat1.iRows);
		for(int j=0;j<dat1.iCols;j++){
			if(!dat1.getString(0,j).equals("TIME_T")){
				for(int i=1;i<dat1.iRows;i++){
					tblLMData.put(dat1.getString(0,j).replace("M_",""),dat1.getString(i,"TIME_T"),dat1.getDouble(i,j));
				}
			}
		}		
	}
	
	public ArrayList<Double> times(){
		
		//lstOut = output
		
		ArrayList<Double> lstOut;
		
		lstOut = new ArrayList<Double>(mapCSigma.keySet());
		Collections.sort(lstOut);
		return lstOut;
	}
	
	public double windowMean(){
		return ExtendedMath.mean(new ArrayList<Double>(setTimeWindow));
	}
	
	private double value(){
		
		//d1 = output
		//d2 = sum of g_i*M_i
		
		double d1;
		double d2;
	
		d1 = 0.;
		for(Double t:setTimeWindow){
			d2 = 0.;
			for(String i:setPredictors){
				d2+=mapG.get(i)*tblMortality.get(t,i);
			}
			d1+=(mapCSigma.get(t) - d2)*(mapCSigma.get(t) - d2);
		}
		return d1;
	}
	
	public HashMap<Double,Double> observedValues(){
		return new HashMap<Double,Double>(mapCSigma);
	}
	
	public HashMap<String,Double> parameterEstimates(){
		return mapG;
	}
	
	public HashMap<Double,Double> fittedValues(){
		
		//map1 = output
		//d1 = current sum
		
		HashMap<Double,Double> map1;
		double d1;
		
		map1 = new HashMap<Double,Double>(mapCSigma.size());
		for(Double t:setTimeWindow){
			d1 = 0;
			for(String i:setPredictors){
				d1+=tblMortality.get(t,i)*mapG.get(i);
			}
			map1.put(t,d1);
		}
		return map1;
	}
	
	public void initialGuesses() throws Exception{
		
		//lnm1 = current linear model
		
		LinearModel lnm1;
		
		//loading linear model
		lnm1 = new LinearModel(tblLMData, "C_SIGMA", setPredictors, true);
			
		//fitting linear model
		lnm1.fitModel(setPredictors);
		
		//saving guesses
		mapG = lnm1.findCoefficientEstimates();
		
	}
	
	public boolean leastSquares() throws Exception{
		
		setTimeWindow = new HashSet<Double>(mapCSigma.keySet());
		return leastSquaresKernel();
	}
	
	public boolean leastSquares(double dTimeStart, double dTimeEnd) throws Exception{

		//dT = current time
		
		double dT;
		
		setTimeWindow = new HashSet<Double>(mapCSigma.size());
		dT = mapCSigma.ceilingKey(dTimeStart);
		while(dT<=dTimeEnd){
			setTimeWindow.add(dT);
			if(dT==mapCSigma.lastKey()){
				break;
			}
			dT = mapCSigma.higherKey(dT);
		}
		if(setTimeWindow.size()<setPredictors.size() || setTimeWindow.size()<(dTimeEnd-dTimeStart+1)){
			return false;
		}
		return leastSquaresKernel();
		
	}
	
	public boolean leastSquaresKernel() throws Exception{
		
		//d4 = total
		
		double d4;
		
		mapM2 = new HashMap_AdditiveDouble<String>(setTimeWindow.size());
		d4 = 0;
		for(double t:setTimeWindow){
			for(String i:setPredictors){
				mapM2.putSum(i,tblMortality.get(t,i)*tblMortality.get(t,i));
				d4+=tblMortality.get(t,i);
			}
		}		
		if(d4==0){
			return false;
		}
		
		initialGuesses();
		
		//*************************
		//for(String s:mapG.keySet()){
		//	System.out.println(s + "," + mapG.get(s));
		//}
		//*************************
		
		if(minimize()!=Double.MAX_VALUE){;
			return true;
		}else {
			return false;
		}
	}
	
	private double minimize(){
		
		//d1 = previous value
		//d2 = current value
		//d3 = current value of g_i being updated
		//iCounter = counter
		
		double d1;
		double d2;
		double d3;
		int iCounter;
		
		iCounter = 0;
		do{
			d1 = value();
			for(String s:setPredictors){
				d3 = minimize(s);
				updateG(s,d3);
			}
			d2 = value();
			iCounter++;
		}while(Math.abs(d2-d1)>TOLERANCE && iCounter<=MAX_ITERATIONS);
		
		if(iCounter>MAX_ITERATIONS){
			return Double.MAX_VALUE;
		}else {
			return d2;
		}
	}
	
	private double minimize(String sIndex){
		
		//d4 = current candidate value
		//d1 = first term in numerator
		//d2 = second term in numerator
		//d3 = sub term in second term in numerator
		
		double d1;
		double d2;
		double d3;
		double d4;
		
		d1 = 0;
		d2 = 0;
		d4 = 0;
		for(Double t:setTimeWindow){
			d1+=mapCSigma.get(t)*tblMortality.get(t,sIndex);
			d3 = 0;
			for(String i:setPredictors){
				if(!i.equals(sIndex)){
					d3+=mapG.get(i)*tblMortality.get(t,i);
				}
			}
			d2+=tblMortality.get(t,sIndex)*d3;
		}
		d4 = (d1-d2)/mapM2.get(sIndex);
		return Math.max(1.,d4);
	}

	private void updateG(String sIndex, double dValue){
		mapG.put(sIndex, dValue);
	}
}

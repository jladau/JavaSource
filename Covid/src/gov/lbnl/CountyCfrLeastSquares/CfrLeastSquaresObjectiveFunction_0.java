package gov.lbnl.CountyCfrLeastSquares;

import java.util.HashMap;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.io.DataIO;

public class CfrLeastSquaresObjectiveFunction_0{

	/**Map from time points to f_sigma values**/
	private HashMap<Double,Double> mapFSigma;
	
	/**Map from indices to current values of x_i**/
	private HashMap<Integer,Double> mapX;
	
	/**Table from time points and indices to mortality values**/
	private HashBasedTable<Double,Integer,Double> tblMortality;
	
	/**Map of sums**/
	private HashMap<Double,Double[]> mapSums;
	
	public CfrLeastSquaresObjectiveFunction_0(DataIO dat1){
		
		//i1 = current mortality index
		
		int i1;
		
		//loading variables
		mapFSigma = new HashMap<Double,Double>(dat1.iRows);
		mapX = new HashMap<Integer,Double>(dat1.iCols);
		tblMortality = HashBasedTable.create(dat1.iRows, dat1.iCols);
		for(int i=1;i<dat1.iRows;i++){
			mapFSigma.put(dat1.getDouble(i,"TIME_T"),dat1.getDouble(i,"F_SIGMA"));
		}
		for(int j=0;j<dat1.iCols;j++){
			if(dat1.getString(0,j).startsWith("M_")){
				i1 = Integer.parseInt(dat1.getString(0,j).replace("M_",""));
				for(int i=1;i<dat1.iRows;i++){
					tblMortality.put(dat1.getDouble(i,"TIME_T"),i1,dat1.getDouble(i,j));
				}
				mapX.put(i1,-7.);
			}
		}
		mapSums = null;
	}
	
	public double value(){
		
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
			d1+=(mapFSigma.get(t) - dSumM/dSumMX)*(mapFSigma.get(t) - dSumM/dSumMX);
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
				for(Integer i:mapX.keySet()){
					dSumM+=tblMortality.get(t,i);
					dSumMX+=tblMortality.get(t,i)*(1.+Math.exp(mapX.get(i)));
				}
				mapSums.put(t,new Double[]{dSumM, dSumMX});
			}
		}
	}
	
	public double derivative(int iIndex){
		
		//d1 = output
		//dSumM = sum of mortalities
		//dSumMX = sum of mortalities*(1+e^x_i)
		
		double d1;
		double dSumM;
		double dSumMX;
		
		d1 = 0;
		loadSums();
		for(Double t:mapSums.keySet()){
			dSumM = mapSums.get(t)[0];
			dSumMX = mapSums.get(t)[1];
			d1+=2.*(mapFSigma.get(t) - dSumM/dSumMX)*dSumM/(dSumMX*dSumMX)*tblMortality.get(t,iIndex)*Math.exp(mapX.get(iIndex));
		}
		return d1;
	}
	
	public HashMap<Double,Double> observedValues(){
		return mapFSigma;
	}
	
	public HashMap<Integer,Double> estimatedValue(){
		
		return mapX;
		
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
	
	public void findMinimum(){
		
		//d1 = previous value
		//d2 = current value
		//iCounter = counter
		
		double d1;
		double d2;
		int iCounter;
		
		iCounter = 0;
		do{
			d1 = value();
			for(int i:mapX.keySet()){
				minimize(i);
			}
			d2 = value();
			iCounter++;
		}while(Math.abs(d2-d1)>0.00000001 && iCounter<100);
	}
	
	private void minimize(int iIndex){
		
		//dStep = current step size
		//dX = current x value
		//dValuePlus = function value plus step
		//dValueMinus = function value minus step
		
		double dStep;
		double dX;
		double dValuePlus;
		double dValueMinus;
		
		dX = 0;
		dStep = 10.;
		do{
			updateX(iIndex,dX + dStep);
			dValuePlus = value();
			updateX(iIndex,dX - dStep);
			dValueMinus = value();
			if(dValueMinus<dValuePlus){
				dX = dX - dStep;
			}else{
				dX = dX + dStep;
			}
			updateX(iIndex, dX);
			dStep = dStep/2.;
		}while(dStep>0.000001);
	}
	
	private void minimizeNewton(int iIndex){
		
		//d1 = current value of x_i
		//d0 = previous value of x_i
		//iCounter = counter
		//dDerivative = current derivative
		
		double d1;
		double d0;
		int iCounter;
		double dDerivative;
		
		d1 = mapX.get(iIndex);
		iCounter = 0;
		do{
			d0 = mapX.get(iIndex);
			dDerivative = derivative(iIndex);
			if(Math.abs(dDerivative)<0.00001){
				break;
			}
			d1 = d1 - value()/derivative(iIndex);
			updateX(iIndex, d1);
			iCounter++;
		}while(Math.abs(d1-d0)>0.000001 && iCounter<100);
	}
	
	private void updateX(int iIndex, double dValue){
		mapX.put(iIndex, dValue);
		mapSums = null;
	}
}

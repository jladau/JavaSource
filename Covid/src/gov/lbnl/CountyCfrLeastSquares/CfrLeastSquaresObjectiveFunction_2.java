package gov.lbnl.CountyCfrLeastSquares;

import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import edu.ucsf.base.LinearModel;
import edu.ucsf.io.DataIO;

public class CfrLeastSquaresObjectiveFunction_2{

	/**Data table**/
	private HashBasedTable<String,String,Double> tblData;
	
	/**Set of predictors**/
	private HashSet<String> setPredictors;
	
	/**Linear model object for finding least squares**/
	private LinearModel lnm1;
	
	public CfrLeastSquaresObjectiveFunction_2(DataIO dat1) throws Exception{
		
		//loading data and predictors
		tblData = HashBasedTable.create(dat1.iCols, dat1.iRows);
		setPredictors = new HashSet<String>(dat1.iCols);
		for(int j=0;j<dat1.iCols;j++){
			if(!dat1.getString(0,j).equals("TIME_T")){
				for(int i=1;i<dat1.iRows;i++){
					tblData.put(dat1.getString(0,j),dat1.getString(i,"TIME_T"),dat1.getDouble(i,j));
				}
			}
			if(dat1.getString(0,j).startsWith("M_")){
				setPredictors.add(dat1.getString(0,j));
			}
		}
		
		//loading linear model
		lnm1 = new LinearModel(tblData, "SUM_C_MINUS_M", setPredictors, true);
		
		//fitting linear model
		lnm1.fitModel(setPredictors);
	}
	
	public HashMap<String,Double> observedValues(){
		return lnm1.getObservedValues();
	}
	
	public HashMap<String,Double> parameterEstimates(){
		return lnm1.findCoefficientEstimates();
	}
	
	public HashMap<String,Double> fittedValues(){
		return lnm1.findPredictedValues();
	}
}

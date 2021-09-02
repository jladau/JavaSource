package gov.lbnl.TransformData2;

import java.util.ArrayList;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayTable;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class TransformData2Launcher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//datResponses = response variables
		//dat1 = data
		//rgdM = total mortality
		//rgdM2 = total mortality squared
		//tblPredictors = rows are times, columns are samples
		//tblResponses = rows are times, columns are response values
		//lstRows = list of rows
		//d2 = reciprocal of maximum possible coefficient value
		//lstResponses = response variables
		//lstPredictors = predictors
		//lstPredictors = predictors and time slope (if included)
		//lst1 = response output
		//lst2 = predictor output
		//lstOut = ouput
		
		//double d2;
		ArrayList<Integer> lstRows;
		ArrayTable<Integer,String,Double> tblPredictors;
		ArrayTable<Integer,String,Double> tblResponses;
		ArgumentIO arg1;
		DataIO dat1;
		double rgdM[];
		double rgdM2[];
		ArrayList<String> lstResponses;
		ArrayList<String> lstPredictors;
		ArrayList<String> lstPredictors2;
		ArrayList<String> lst1;
		ArrayList<String> lst2;
		ArrayList<String> lstOut;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		lstRows = new ArrayList<Integer>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			lstRows.add(i);
		}
		//d2 = 1./arg1.getValueDouble("dMaximumValue");
		lstPredictors = arg1.getValueArrayList("lstPredictors");
		lstResponses = arg1.getValueArrayList("lstResponses");
		
		//loading total mortality
		rgdM = new double[dat1.iRows-1];
		rgdM2 = new double[dat1.iRows-1];
		for(int i=1;i<dat1.iRows;i++){
			for(String s:lstPredictors){
				rgdM[i-1]+=dat1.getDouble(i,s);
				rgdM2[i-1]+=dat1.getDouble(i,s)*dat1.getDouble(i,s);
			}
		}
		for(int i=0;i<rgdM2.length;i++){
			//*************************************
			//rgdM2[i]=Math.sqrt(rgdM2[i]);
			rgdM2[i]=Math.sqrt(rgdM2[i]+1);
			//*************************************
		}
		
		//updating response variables
		tblResponses = ArrayTable.create(lstRows,lstResponses);
		for(int i=1;i<dat1.iRows;i++){
			for(String s:lstResponses){
				if(rgdM2[i-1]>0){
					//tblResponses.put(i,s,(dat1.getDouble(i,s)-d2*rgdM[i-1])/rgdM2[i-1]);
					tblResponses.put(i,s,(dat1.getDouble(i,s))/rgdM2[i-1]);
				}else{
					tblResponses.put(i,s, Double.NaN);
				}
			}
		}
		
		//updating predictor variables
		lstPredictors2 = new ArrayList<String>(lstPredictors);
		if(dat1.hasHeader("TIME_SLOPE")){
			lstPredictors2.add("TIME_SLOPE");
		}
		tblPredictors = ArrayTable.create(lstRows,lstPredictors2);
		for(int i=1;i<dat1.iRows;i++){
			for(String s:lstPredictors2){
				if(rgdM2[i-1]>0){
					tblPredictors.put(i,s,dat1.getDouble(i,s)/rgdM2[i-1]);
				}else {
					tblPredictors.put(i,s, Double.NaN);
				}
			}
		}
		
		//formatting output
		lst1 = print(tblResponses);
		lst2 = print(tblPredictors);
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("TIME," + lst1.get(0) + "," + lst2.get(0));
		for(int i=1;i<dat1.iRows;i++){
			lstOut.add(dat1.getString(i,"TIME") + "," + lst1.get(i) + "," + lst2.get(i));
		}
		
		//outputting results		
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}

	private static ArrayList<String> print(ArrayTable<Integer,String,Double> tbl1){
		
		//lstOut = output
		//lst1 = current row
		//s1 = header string
		
		ArrayList<String> lstOut;
		ArrayList<Double> lst1;
		String s1;
		
		lstOut = new ArrayList<String>(tbl1.rowKeySet().size());
		s1 = Joiner.on(",").join(tbl1.columnKeyList());
		lstOut.add(s1);		
		for(Integer i:tbl1.rowKeyList()){
			lst1 = new ArrayList<Double>(tbl1.columnKeySet().size());
			for(String s:tbl1.columnKeyList()){
				if(Double.isNaN(tbl1.get(i,s))){
					lst1 = null;
					break;
				}else{
					lst1.add(tbl1.get(i,s));
				}
			}
			if(lst1!=null){
				lstOut.add(Joiner.on(",").join(lst1));
			}
		}
		return lstOut;
	}
}
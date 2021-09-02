package gov.lbnl.TransformData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayTable;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.base.HashMap_AdditiveInteger;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class TransformDataLauncher{

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//datResponses = response variables
		//datPredictors = predictor variables
		//datClusters = cluster map
		//rgdM = total mortality
		//rgdM2 = total mortality squared
		//map1 = map from cluster id to current coefficient sum or sum
		//map2 = map from sample id to cluster id
		//map3 = map from cluster id to current coefficient count or count
		//tblPredictors = rows are times, columns are samples
		//tblResponses = rows are times, columns are response values
		//tblPredictors2 = predictor table by cluster
		//lstRows = list of rows
		//sCluster = current cluster
		//d2 = reciprocal of maximum possible coefficient value
		
		double d2;
		String sCluster;
		ArrayList<Integer> lstRows;
		ArrayTable<Integer,String,Double> tblPredictors;
		ArrayTable<Integer,String,Double> tblResponses;
		ArrayTable<Integer,String,Double> tblPredictors2;
		ArgumentIO arg1;
		DataIO datResponses;
		DataIO datPredictors;
		DataIO datClusters;
		double rgdM[];
		double rgdM2[];
		HashMap_AdditiveDouble<String> map1;
		HashMap_AdditiveInteger<String> map3;
		HashMap<String,String> map2;

		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		datResponses = new DataIO(arg1.getValueString("sResponsesPath"));
		datPredictors = new DataIO(arg1.getValueString("sPredictorsPath"));
		if(arg1.containsArgument("sClustersPath")){
			datClusters = new DataIO(arg1.getValueString("sClustersPath"));
		}else{
			datClusters = null;
		}
		lstRows = new ArrayList<Integer>(datResponses.iRows);
		for(int i=1;i<datResponses.iRows;i++){
			lstRows.add(i);
		}
		d2 = 1./arg1.getValueDouble("dMaximumValue");
		
		//loading total mortality
		rgdM = new double[datPredictors.iRows-1];
		rgdM2 = new double[datPredictors.iRows-1];
		for(int i=1;i<datPredictors.iRows;i++){
			for(int j=0;j<datPredictors.iCols;j++){
				if(!datPredictors.getString(0,j).equals("TIME_SLOPE")){
					rgdM[i-1]+=datPredictors.getDouble(i,j);
					rgdM2[i-1]+=datPredictors.getDouble(i,j)*datPredictors.getDouble(i,j);
				}
			}
		}
		for(int i=0;i<rgdM2.length;i++){
			
			//*************************************
			//rgdM2[i]=Math.sqrt(rgdM2[i]);
			rgdM2[i]=Math.sqrt(rgdM2[i]+1);
			//*************************************
		}
		
		//updating response variables
		tblResponses = ArrayTable.create(lstRows,datResponses.getHeaders());
		for(int i=1;i<datResponses.iRows;i++){
			for(String s:datResponses.getHeaders()){
				if(rgdM2[i-1]>0){
					tblResponses.put(i,s,(datResponses.getDouble(i,s)-d2*rgdM[i-1])/rgdM2[i-1]);
				}else{
					tblResponses.put(i,s, Double.NaN);
				}
			}
		}
		
		//updating predictor variables
		tblPredictors = ArrayTable.create(lstRows,datPredictors.getHeaders());
		for(int i=1;i<datPredictors.iRows;i++){
			for(String s:datPredictors.getHeaders()){
				if(rgdM2[i-1]>0){
					tblPredictors.put(i,s,datPredictors.getDouble(i,s)/rgdM2[i-1]);
				}else {
					tblPredictors.put(i,s, Double.NaN);
				}
			}
		}
		
		//merging by clusters if requested
		if(datClusters!=null){
			
			//loading sample --> cluster map
			map2 = new HashMap<String,String>(datClusters.iRows);
			for(int i=1;i<datClusters.iRows;i++){
				map2.put(datClusters.getString(i,"REGION_ID"),datClusters.getString(i,"REGION_ID_NEW"));
			}
			
			//outputting values
			tblPredictors2 = ArrayTable.create(lstRows, new HashSet<String>(map2.values()));
			
			//looping through samples
			for(Integer i:tblPredictors.rowKeySet()){
				
				//loading summed values
				map1 = new HashMap_AdditiveDouble<String>(tblPredictors.columnKeySet().size());
				map3 = new HashMap_AdditiveInteger<String>(tblPredictors.columnKeySet().size());
				for(String s:tblPredictors.columnKeySet()){
					sCluster = map2.get(s);
					map1.putSum(sCluster,tblPredictors.get(i,s));
					map3.putSum(sCluster,1);
				}
				
				for(String s:map3.keySet()){
					tblPredictors2.put(i,s,map1.get(s)/((double) map3.get(s)));
				}
			}
			tblPredictors = tblPredictors2;
		}
		
		//centering if requested
		if(arg1.getValueBoolean("bCenter")){
			map1 = new HashMap_AdditiveDouble<String>(tblPredictors.columnKeySet().size());
			map3 = new HashMap_AdditiveInteger<String>(tblPredictors.columnKeySet().size());
			for(Integer i:tblPredictors.rowKeyList()){
				for(String s:tblPredictors.columnKeySet()){
					if(!Double.isNaN(tblPredictors.get(i,s))){
						map1.putSum(s,tblPredictors.get(i,s));
						map3.putSum(s,1);
					}
				}
			}
			for(Integer i:tblPredictors.rowKeyList()){
				for(String s:tblPredictors.columnKeySet()){
					tblPredictors.put(i,s, tblPredictors.get(i,s) - map1.get(s)/map3.get(s));
				}
			}
		}
		
		//outputting results		
		DataIO.writeToFile(print(tblPredictors,arg1.getValueBoolean("bCenter")),arg1.getValueString("sPredictorsOutputPath"));
		DataIO.writeToFile(print(tblResponses,false),arg1.getValueString("sResponsesOutputPath"));
		System.out.println("Done.");
	}

	private static ArrayList<String> print(ArrayTable<Integer,String,Double> tbl1, boolean bAddIntercept){
		
		//lstOut = output
		//lst1 = current row
		//s1 = header string
		
		ArrayList<String> lstOut;
		ArrayList<Double> lst1;
		String s1;
		
		lstOut = new ArrayList<String>(tbl1.rowKeySet().size());
		if(!bAddIntercept){
			s1 = Joiner.on(",").join(tbl1.columnKeyList());
		}else{
			s1 = Joiner.on(",").join(tbl1.columnKeyList()) + ",Intercept";
		}
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
				if(!bAddIntercept){
					lstOut.add(Joiner.on(",").join(lst1));
				}else{
					lstOut.add(Joiner.on(",").join(lst1) + ",1");
				}
			}
		}
		return lstOut;
	}
	
	
	public static void main0(String rgsArgs[]){
		
		//arg1 = arguments
		//datResponses = response variables
		//datPredictors = predictor variables
		//datClusters = cluster map
		//rgdM = total mortality
		//rgdM2 = total mortality squared
		//map1 = map from cluster id to current coefficient sum
		//map2 = map from sample id to cluster id
		//map3 = map from cluster id to current coefficient count
		//lst1 = list of cluster names in order
		//lstOut = output
		//sbl1 = current output line
		//itr1 = iterator
		
		ArgumentIO arg1;
		DataIO datResponses;
		DataIO datPredictors;
		DataIO datClusters;
		double rgdM[];
		double rgdM2[];
		HashMap_AdditiveDouble<String> map1;
		HashMap_AdditiveInteger<String> map3;
		HashMap<String,String> map2;
		StringBuilder sbl1;
		ArrayList<String> lstOut;
		ArrayList<String> lst1;
		Iterator<String> itr1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		datResponses = new DataIO(arg1.getValueString("sResponsesPath"));
		datPredictors = new DataIO(arg1.getValueString("sPredictorsPath"));
		if(arg1.containsArgument("sClustersPath")){
			datClusters = new DataIO(arg1.getValueString("sClustersPath"));
		}else{
			datClusters = null;
		}
		
		//loading total mortality
		rgdM = new double[datPredictors.iRows-1];
		rgdM2 = new double[datPredictors.iRows-1];
		for(int i=1;i<datPredictors.iRows;i++){
			for(int j=0;j<datPredictors.iCols;j++){
				rgdM[i-1]+=datPredictors.getDouble(i,j);
				rgdM2[i-1]+=datPredictors.getDouble(i,j)*datPredictors.getDouble(i,j);
			}
		}
		for(int i=0;i<rgdM2.length;i++){
			rgdM2[i]=Math.sqrt(rgdM2[i]);
		}
		
		//updating response variables
		for(int i=1;i<datResponses.iRows;i++){
			for(int j=0;j<datResponses.iCols;j++){
				datResponses.setString(i,j,Double.toString((datResponses.getDouble(i,j)-rgdM[i-1])/rgdM2[i-1]));
			}
		}
		
		//updating predictor variables
		for(int i=1;i<datPredictors.iRows;i++){
			for(int j=0;j<datPredictors.iCols;j++){
				datPredictors.setString(i,j,Double.toString(datPredictors.getDouble(i,j)/rgdM2[i-1]));
			}
		}
		
		//merging by clusters if requested
		if(datClusters!=null){
			
			//loading list of cluster names
			lst1 = new ArrayList<String>(datClusters.iRows);
			for(int i=1;i<datClusters.iRows;i++){
				if(!lst1.contains(datClusters.getString(i,"CLUSTER_ID"))){
					lst1.add(datClusters.getString(i,"CLUSTER_ID"));
				}
			}
			
			//initializing output
			lstOut = new ArrayList<String>(datPredictors.iRows);
			lstOut.add(Joiner.on(",").join(lst1));
			
			//loading sample --> cluster map
			map2 = new HashMap<String,String>(datClusters.iRows);
			for(int i=1;i<datClusters.iRows;i++){
				map2.put(datClusters.getString(i,"REGION_ID"),datClusters.getString(i,"REGION_ID_NEW"));
			}
			
			//looping through samples
			for(int i=1;i<datPredictors.iRows;i++){
				
				//loading summed values
				map1 = new HashMap_AdditiveDouble<String>(datPredictors.iCols);
				map3 = new HashMap_AdditiveInteger<String>(datPredictors.iCols);
				for(String s:datPredictors.getHeaders()){
					map1.putSum(map2.get(s),datPredictors.getDouble(i,s));
					map3.putSum(map2.get(s),1);
				}
				
				//outputting values
				sbl1 = new StringBuilder();
				for(int k=0;k<lst1.size()-1;k++){
				
					//***********************************
					//sbl1.append(map1.get(lst1.get(k)) + ",");
					sbl1.append(map1.get(lst1.get(k))/((double) map3.get(lst1.get(k))) + ",");
	
					//***********************************
					
				}
				sbl1.append(map1.get(lst1.get(lst1.size()-1)));
				lstOut.add(sbl1.toString());
			}
		}else {
			lstOut = datPredictors.getWriteableData();
		}
		
		//outputting results
		itr1 = lstOut.iterator();
		while(itr1.hasNext()){
			if(itr1.next().startsWith("NaN,")){
				itr1.remove();
			}
		}
		DataIO.writeToFile(lstOut,arg1.getValueString("sPredictorsOutputPath"));
		lstOut = datResponses.getWriteableData();
		itr1 = lstOut.iterator();
		while(itr1.hasNext()){
			if(itr1.next().startsWith("NaN,")){
				itr1.remove();
			}
		}
		DataIO.writeToFile(lstOut,arg1.getValueString("sResponsesOutputPath"));
		System.out.println("Done.");
	}
}

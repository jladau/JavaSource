package edu.ucsf.MulitpleQuantileRegression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import com.google.common.collect.HashBasedTable;

import edu.ucsf.base.ExtendedMath;

public class MultipleQuantileRegressionData0{

	/**Data table: rows represent samples, columns are different variables**/
	private HashBasedTable<String,String,Double> tblData;
	
	/**Response variable**/
	private String sResponse;
	
	/**Predictor variables**/
	private ArrayList<String> lstPredictors;
	
	/**Number of rows of data**/
	private int iRows;
	
	/**Number of samples in neighborhood**/
	private int iNeighborhoodSize;
	
	/**Neighbors object for full data set**/
	private Neighbors ngh0;
	
	/**Neighbors object for current reduced**/
	private Neighbors ngh1;
	
	/**Current array of response values (percentiles)**/
	private double rgdY[];
	
	/**Current array of predictor values**/
	private double rgdX[][];
	
	/**Left out response value**/
	private double rgdYOmitted[];
	
	/**Left out predictor values**/
	private double rgdXOmitted[][];
	
	/**List of all samples**/
	private ArrayList<String> lstSamples;
	
	/**Percentile object**/
	private Percentile pct1;
	
	public MultipleQuantileRegressionData0(
			HashBasedTable<Integer,String,String> tblData, 
			String sSample, 
			String sResponse, 
			ArrayList<String> lstPredictors, 
			int iNeighborhoodSize,
			double dPercentile){
		
		//s1 = current sample
		
		String s1;
		
		iRows = tblData.rowKeySet().size();
		this.tblData = HashBasedTable.create(iRows, tblData.columnKeySet().size());
		for(Integer i:tblData.rowKeySet()){
			s1 = tblData.get(i,sSample);
			for(String s:lstPredictors) {
				this.tblData.put(s1,s,Double.parseDouble(tblData.get(i,s)));
			}
			this.tblData.put(s1,sResponse,Double.parseDouble(tblData.get(i,sResponse)));
		}
		this.sResponse = sResponse;
		this.lstPredictors = lstPredictors;
		this.iNeighborhoodSize = iNeighborhoodSize;
		this.ngh0 = new Neighbors(new ArrayList<String>(this.tblData.rowKeySet()));
		this.lstSamples = new ArrayList<String>(this.tblData.rowKeySet());
		this.pct1 = new Percentile(dPercentile);
	}
	
	public ArrayList<String> samples(){
		return lstSamples;
	}
	
	public double[] y(){
		return rgdY;
	}
	
	public double[][] x(){
		return rgdX;
	}
	
	public double[] yOmitted() {
		return rgdYOmitted;
	}
	
	public double[][] xOmitted(){
		return rgdXOmitted;
	}
	
	public void loadData(String sSampleToOmit){
		
		//lstSamplesToInclude = list of samples to include
		//set1 = set of neighbors of current sample
		
		ArrayList<String> lstSamplesToInclude;
		HashSet<String> set1;
		
		if(sSampleToOmit!=null){
			set1 = ngh0.neighbors(sSampleToOmit);
			lstSamplesToInclude = new ArrayList<String>(this.lstSamples.size());
			for(String s:lstSamples) {
				if(!set1.contains(s)) {
					lstSamplesToInclude.add(s);
				}
			}
			ngh1 = new Neighbors(lstSamplesToInclude);
			loadDataArrays(ngh1);
			loadOmittedDataArrays(sSampleToOmit, set1);			
		}else {
			loadDataArrays(ngh0);
		}
	}
	
	private void loadOmittedDataArrays(String sSampleToOmit, HashSet<String> setSampleToOmitNeighbors) {
		
		//i2 = current percentile row
		//rgd1 = array of response values in neighborhood
		//rgd2 = array of sums of predictor values in neighborhood
		//d1 = number of observations in neighborhood
		
		int i2;
		double d1;
		double rgd1[];
		double rgd2[];
		
		rgdYOmitted = new double[1];
		rgdXOmitted = new double[1][lstPredictors.size()];

		i2 = 0;
		rgd1 = new double[setSampleToOmitNeighbors.size()];
		rgd2 = new double[lstPredictors.size()];
		for(String s:setSampleToOmitNeighbors) {
			rgd1[i2]=tblData.get(s,sResponse);
			i2++;
			for(int j=0;j<lstPredictors.size();j++) {
				rgd2[j]+=tblData.get(s,lstPredictors.get(j));
			}
		}
		
		//finding percentile of response
		rgdYOmitted[0] = pct1.evaluate(rgd1);
		
		//finding means of predictors
		d1 = (double) setSampleToOmitNeighbors.size();
		for(int j=0;j<lstPredictors.size();j++) {
			rgdXOmitted[0][j]=rgd2[j]/d1;
		}
	}
	
	private void loadDataArrays(Neighbors ngh1) {
		
		//i1 = current row
		//i2 = current percentile row
		//rgd1 = array of response values in neighborhood
		//rgd2 = array of sums of predictor values in neighborhood
		//d1 = number of observations in neighborhood
		
		int i1;
		int i2;
		double d1;
		double rgd1[];
		double rgd2[];
		
		rgdY = new double[ngh1.size()];
		rgdX = new double[ngh1.size()][lstPredictors.size()];
		i1 = 0;
		for(String sSample:ngh1.samples()) {
			i2 = 0;
			rgd1 = new double[ngh1.neighbors(sSample).size()];
			rgd2 = new double[lstPredictors.size()];
			for(String s:ngh1.neighbors(sSample)) {
				rgd1[i2]=tblData.get(s,sResponse);
				i2++;
				for(int j=0;j<lstPredictors.size();j++) {
					rgd2[j]+=tblData.get(s,lstPredictors.get(j));
				}
			}
			
			//finding percentile of response
			rgdY[i1] = pct1.evaluate(rgd1);
			
			//finding means of predictors
			d1 = (double) ngh1.neighbors(sSample).size();
			for(int j=0;j<lstPredictors.size();j++) {
				rgdX[i1][j]=rgd2[j]/d1;
			}
			i1++;
		}
	}
	
	private class Neighbors{
		
		/**Map from sample names to list of neighbors**/
		private HashMap<String,HashSet<String>> mapNeighbors;
		
		public Neighbors(ArrayList<String> lstSamplesToInclude) {
			
			//tbl1 = table of standardized predictor values for finding distances
			//tbl2 = table of distances between pairs of samples
			//lst1 = current list of values
			//dMean = current mean
			//dStDev = current standard deviation
			//sSample1 = current first sample
			//sSample2 = current second sample
			//dDistance = current distance
			//map1 = map from distances to samples
			//i1 = current number of neighbors
			
			String sSample1;
			String sSample2;
			HashBasedTable<String,String,Double> tbl1;
			HashBasedTable<String,String,Double> tbl2;
			ArrayList<Double> lst1;
			double dMean;
			double dStDev;
			double dDistance;
			TreeMap<Double,ArrayList<String>> map1;
			int i1;
			
			//*************************************
			System.out.println("A");
			//*************************************
			
			//loading table of standardized predictor variables
			tbl1 = HashBasedTable.create(lstSamplesToInclude.size(), lstPredictors.size());
			for(String sPredictor:lstPredictors){
				lst1 = new ArrayList<Double>(iRows);
				for(String sSample:lstSamplesToInclude) {
					lst1.add(tblData.get(sSample,sPredictor));
				}
				dMean = ExtendedMath.mean(lst1);
				dStDev = ExtendedMath.standardDeviation(lst1);
				if(dStDev==0) {
					System.out.println("Warning: standard deviation of zero for " + sPredictor + ".");
				}
				for(String sSample:lstSamplesToInclude) {
					tbl1.put(sSample,sPredictor,(tblData.get(sSample,sPredictor)-dMean)/dStDev);
				}
			}

			//*************************************
			System.out.println("B");
			//************************************
			
			//loading table of distances
			tbl2 = HashBasedTable.create(iRows,iRows);
			for(int i=1;i<lstSamplesToInclude.size();i++){
				sSample1 = lstSamplesToInclude.get(i);
				for(int j=0;j<i;j++){
					sSample2 = lstSamplesToInclude.get(j);
					dDistance = distance(tbl1,sSample1,sSample2);
					tbl2.put(sSample1, sSample2, dDistance);
					tbl2.put(sSample2, sSample1, dDistance);
				}
			}
			
			//*************************************
			System.out.println("C");
			//*************************************
			
			//loading neighbors map
			mapNeighbors = new HashMap<String,HashSet<String>>(lstSamplesToInclude.size());
			for(String sSample:lstSamplesToInclude){
				
				//sorting neighbor distances
				map1 = new TreeMap<Double,ArrayList<String>>();
				for(String s:lstSamplesToInclude) {
					if(!s.equals(sSample)) {
						dDistance = tbl2.get(sSample,s);
						if(!map1.containsKey(dDistance)){
							map1.put(dDistance,new ArrayList<String>(100));
						}
						map1.get(dDistance).add(s);
					}
				}
				
				//finding neighbors
				i1 = 0;
				mapNeighbors.put(sSample,new HashSet<String>(iNeighborhoodSize+1));
				mapNeighbors.get(sSample).add(sSample);
				for(Entry<Double,ArrayList<String>> ery1:map1.entrySet()) {
					if(ery1.getValue().size()>1) {
						Collections.shuffle(ery1.getValue());
					}
					for(String s:ery1.getValue()) {
						mapNeighbors.get(sSample).add(s);
						i1++;
						if(i1>=iNeighborhoodSize) {
							break;
						}
					}
					if(i1>=iNeighborhoodSize) {
						break;
					}
				}
			}
			
			//*************************************
			System.out.println("D");
			//*************************************
			
		}
		
		public HashSet<String> neighbors(String sSample){
			return mapNeighbors.get(sSample);
		}
		
		public int size() {
			return mapNeighbors.size();
		}
		
		public Set<String> samples(){
			return mapNeighbors.keySet();
		}
		
		private double distance(HashBasedTable<String,String,Double> tblStandardizedData, String sSample1, String sSample2){
			
			//d3 = distance
			//d1 = value for first sample
			//d2 = value for second sample
			
			double d1;
			double d2;
			double d3;
			
			d3 = 0;
			for(String sPredictor:lstPredictors) {
				d1 = tblStandardizedData.get(sSample1,sPredictor);
				d2 = tblStandardizedData.get(sSample2,sPredictor);
				d3+=(d1-d2)*(d1-d2);
			}
			return d3;
		}
	}
}
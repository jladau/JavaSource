package edu.ucsf.MulitpleQuantileRegression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.io.Printer;

public class MultipleQuantileRegressionData{

	/**Data table: rows represent samples, columns are different variables**/
	private HashBasedTable<String,String,Double> tblData;
	
	/**Response variable**/
	private String sResponse;
	
	/**Current predictors**/
	private ArrayList<String> lstPredictors;
	
	/**Number of rows of data**/
	private int iRows;
	
	/**Number of samples in neighborhood**/
	private int iNeighborhoodSize;
	
	/**Neighbors object for full data set**/
	private Neighborhoods ngh0;
	
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
	
	public MultipleQuantileRegressionData(
			HashBasedTable<Integer,String,String> tblData, 
			String sSample, 
			String sResponse,
			int iNeighborhoodSize,
			double dPercentile){
		
		//s1 = current sample
		//set1 = list of headers to convert to double
		
		String s1;
		HashSet<String> set1;
		
		iRows = tblData.rowKeySet().size();
		this.tblData = HashBasedTable.create(iRows, tblData.columnKeySet().size());
		set1 = new HashSet<String>(tblData.columnKeySet());
		set1.remove(sSample);
		for(Integer i:tblData.rowKeySet()){
			s1 = tblData.get(i,sSample);
			for(String s:set1) {
				this.tblData.put(s1,s,Double.parseDouble(tblData.get(i,s)));
			}
		}
		this.sResponse = sResponse;
		this.iNeighborhoodSize = iNeighborhoodSize;
		this.lstSamples = new ArrayList<String>(this.tblData.rowKeySet());
		this.pct1 = new Percentile(dPercentile);
	}
	
	public void loadNeighborhoods(ArrayList<String> lstPredictors) {
		this.ngh0 = new Neighborhoods(lstPredictors);
	}
	
	public ArrayList<Neighborhood> neighborhoods(){
		return ngh0.neighborhoods();
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

	public ArrayList<String> predictors(){
		return lstPredictors;
	}
	

	public String predictor(int iIndex) {
		return lstPredictors.get(iIndex);
	}
	
	
	public void loadData(Neighborhood ngb1, ArrayList<String> lstPredictors) {
		
		//i1 = counter
		
		int i1;
		
		this.lstPredictors = lstPredictors;
		if(ngb1!=null){
			rgdY = new double[ngb1.disjointNeighborhoods().size()];
			rgdX = new double[ngb1.disjointNeighborhoods().size()][lstPredictors.size()];
			rgdYOmitted = new double[]{ngb1.responsePercentile()};
			rgdXOmitted = new double[][]{ngb1.predictorMean(lstPredictors)};
			i1 = 0;
			for(Neighborhood ngb2:ngb1.setDisjointNeighborhoods){
				rgdY[i1]=ngb2.responsePercentile();
				rgdX[i1]=ngb2.predictorMean(lstPredictors);
				i1++;
			}
		}else{
			rgdY = new double[ngh0.size()];
			rgdX = new double[ngh0.size()][lstPredictors.size()];
			rgdYOmitted = null;
			rgdXOmitted = null;
			i1 = 0;
			for(Neighborhood ngb2:ngh0.neighborhoods()){
				rgdY[i1]=ngb2.responsePercentile();
				rgdX[i1]=ngb2.predictorMean(lstPredictors);
				i1++;
			}
		}
	}
	
	public ArrayList<String> print() {
		
		//rgd1 = current predictor means
		//lstOut = output
		//sbl1 = stringbuilder
		
		double rgd1[];
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		
		lstOut = new ArrayList<String>(ngh0.neighborhoods().size()+1);
		lstOut.add(Joiner.on(",").join(ngh0.lstPredictors) + ",RESPONSE_PERCENTILE");
		for(Neighborhood ngb1:ngh0.neighborhoods()){
			rgd1 = ngb1.predictorMean(ngh0.lstPredictors);
			sbl1 = new StringBuilder();
			sbl1.append(rgd1[0]);
			for(int i=1;i<rgd1.length;i++) {
				sbl1.append("," + rgd1[i]);
			}
			sbl1.append("," + ngb1.responsePercentile());
			lstOut.add(sbl1.toString());
		}
		return lstOut;
	}
	
	private class Neighborhoods{
		
		/**Neighborhoods**/
		private ArrayList<Neighborhood> lstNeighborhoods;
		
		/**Predictors used to build neighborhoods**/
		private ArrayList<String> lstPredictors;
		
		public Neighborhoods(ArrayList<String> lstPredictors) {
			
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
			//ngb1 = current neighborhood
			//bAdd = flag for whether to add current neighborhood
			
			boolean bAdd;
			Neighborhood ngb1;
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
			
			//removing interactions
			this.lstPredictors = new ArrayList<String>(lstPredictors.size());
			for(String s:lstPredictors) {
				if(!s.equals("[interaction]")) {
					this.lstPredictors.add(s);
				}
			}
			
			//loading table of standardized predictor variables
			tbl1 = HashBasedTable.create(lstSamples.size(), this.lstPredictors.size());
			for(String sPredictor:this.lstPredictors){
				lst1 = new ArrayList<Double>(iRows);
				
				//*************************
				//Printer.print(new HashSet<String>(tblData.columnKeySet()));
				//Printer.print(new HashMap<String,Double>(tblData.column(sPredictor)));
				
				//*************************
				
				for(String sSample:lstSamples) {
					lst1.add(tblData.get(sSample,sPredictor));
				}
				dMean = ExtendedMath.mean(lst1);
				dStDev = ExtendedMath.standardDeviation(lst1);
				if(dStDev==0) {
					System.out.println("Warning: standard deviation of zero for " + sPredictor + ".");
				}
				for(String sSample:lstSamples) {
					tbl1.put(sSample,sPredictor,(tblData.get(sSample,sPredictor)-dMean)/dStDev);
				}
			}

			//loading table of distances
			tbl2 = HashBasedTable.create(iRows,iRows);
			for(int i=1;i<lstSamples.size();i++){
				sSample1 = lstSamples.get(i);
				for(int j=0;j<i;j++){
					sSample2 = lstSamples.get(j);
					dDistance = distance(tbl1,sSample1,sSample2);
					tbl2.put(sSample1, sSample2, dDistance);
					tbl2.put(sSample2, sSample1, dDistance);
				}
			}
			
			//loading neighbors map
			lstNeighborhoods = new ArrayList<Neighborhood>(lstSamples.size());
			for(String sSample:lstSamples){
				
				//sorting neighbor distances
				map1 = new TreeMap<Double,ArrayList<String>>();
				for(String s:lstSamples) {
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
				ngb1 = new Neighborhood(iNeighborhoodSize, lstSamples.size());
				ngb1.add(sSample);
				i1++;
				for(Entry<Double,ArrayList<String>> ery1:map1.entrySet()) {
					if(ery1.getValue().size()>1) {
						Collections.shuffle(ery1.getValue());
					}
					for(String s:ery1.getValue()) {
						ngb1.add(s);
						i1++;
						if(i1>=iNeighborhoodSize) {
							break;
						}
					}
					if(i1>=iNeighborhoodSize) {
						break;
					}
				}
				bAdd = true;
				for(Neighborhood ngb2:lstNeighborhoods) {
					if(ngb1.isIdenticalTo(ngb2)){
						bAdd = false;
						break;
					}
				}
				
				if(bAdd==true){	
					lstNeighborhoods.add(ngb1);
				}
			}
			
			//loading disjoint neighborhoods
			loadDisjointNeighborhoods();

			//loading predictor means
			for(Neighborhood ngb2:lstNeighborhoods) {
				ngb2.loadPredictorMeans(lstPredictors);
			}
		}
		
		public ArrayList<String> predictors(){
			return lstPredictors;
		}
		
		public ArrayList<Neighborhood> neighborhoods(){
			return lstNeighborhoods;
		}
		
		public int size() {
			return lstNeighborhoods.size();
		}
		
		private void loadDisjointNeighborhoods() {
			for(int i=1;i<lstNeighborhoods.size();i++){
				for(int j=0;j<i;j++){
					if(!lstNeighborhoods.get(i).intersects(lstNeighborhoods.get(j))) {
						lstNeighborhoods.get(i).addDisjointNeighborhood(lstNeighborhoods.get(j));
						lstNeighborhoods.get(j).addDisjointNeighborhood(lstNeighborhoods.get(i));
					}
				}
			}	
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
	
	public class Neighborhood{
		
		/**Set of samples in neighborhood**/
		private HashSet<String> setSamples;
		
		/**Set of disjoint neighborhoods**/
		private HashSet<Neighborhood> setDisjointNeighborhoods;
		
		/**Map from predictor names to predictor means**/
		private HashMap_AdditiveDouble<String> mapMeans;
		
		public Neighborhood(int iWindowSize, int iExpectedDisjointNeighborhoods){
			setSamples = new HashSet<String>(iWindowSize);
			setDisjointNeighborhoods = new HashSet<Neighborhood>(iExpectedDisjointNeighborhoods);
		}
		
		public void addDisjointNeighborhood(Neighborhood nhd1) {
			setDisjointNeighborhoods.add(nhd1);
		}
		
		public void add(String sSample) {
			setSamples.add(sSample);
		}
		
		public HashSet<String> samples(){
			return setSamples;
		}
		
		public boolean intersects(Neighborhood nhd1){
			for(String s:setSamples) {
				if(nhd1.contains(s)) {
					return true;
				}
			}
			return false;
		}
		
		public boolean contains(String sSample) {
			return setSamples.contains(sSample);
		}
		
		public int size() {
			return setSamples.size();
		}
		
		public boolean isIdenticalTo(Neighborhood nhd1) {
			if(nhd1.size()!=this.size()) {
				return false;
			}
			for(String s:setSamples) {
				if(!nhd1.contains(s)) {
					return false;
				}
			}
			return true;
		}
		
		public void loadPredictorMeans(ArrayList<String> lstPredictors) {
			
			//bInteraction = flag for whether to include interaction
			//d1 = number of samples
			//d2 = current sum
			//d3 = current product
			
			boolean bInteraction;
			double d1;
			double d2;
			double d3;
			
			bInteraction = false;
			mapMeans = new HashMap_AdditiveDouble<String>(lstPredictors.size());
			for(String sPredictor:lstPredictors) {
				if(!sPredictor.equals("[interaction]")) {
					for(String sSample:setSamples) {
						mapMeans.putSum(sPredictor,tblData.get(sSample,sPredictor));
					}
				}else {
					bInteraction = true;
				}
			}
			d1 = (double) setSamples.size();
			for(String sPredictor:lstPredictors) {
				if(!sPredictor.equals("[interaction]")) {
					d2 = mapMeans.get(sPredictor);
					mapMeans.put(sPredictor,d2/d1);
				}
			}
			if(bInteraction==true) {
				d3 = 1;
				for(String sPredictor:mapMeans.keySet()) {
					d3=d3*mapMeans.get(sPredictor);
				}
				mapMeans.put("[interaction]",d3);
			}
		}
		
		public double[] predictorMean(ArrayList<String> lstPredictors) {
			
			//rgd1 = output
			
			double rgd1[];
			
			rgd1 = new double[lstPredictors.size()];
			for(int i=0;i<lstPredictors.size();i++) {
				rgd1[i]=mapMeans.get(lstPredictors.get(i));
			}
			return rgd1;
		}
	
		public double responsePercentile(){
		
			//rgd1 = list of response values
			//i1 = counter
			
			double rgd1[];
			int i1;
			
			i1 = 0;
			rgd1 = new double[this.size()];
			for(String sSample:setSamples){
				rgd1[i1]=tblData.get(sSample,sResponse);
				i1++;
			}
			return pct1.evaluate(rgd1);
		}
		
		public HashSet<Neighborhood> disjointNeighborhoods(){
			return setDisjointNeighborhoods;
		}
	}
}
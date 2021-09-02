package edu.ucsf.MulitpleQuantileRegression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import edu.ucsf.io.Printer;

public class MultipleQuantileRegressionModelSelection{

	/**Data**/
	private MultipleQuantileRegressionData mqd1;
	
	/**Best model**/
	private Model mdlBest;
	
	public MultipleQuantileRegressionModelSelection(MultipleQuantileRegressionData mqd1) {
		this.mqd1 = mqd1;
	}
	
	public ArrayList<String> printBestModelData(){
		mqd1.loadNeighborhoods(mdlBest.predictors());
		return mqd1.print();
	}
	
	public Model bestModel() {
		return mdlBest;
	}
	
	public ArrayList<String> allSubsets(ArrayList<String> lstPredictors) {
		
		//ksb1 = k-subsets
		//mqr1 = multiple quantile regression
		//lst1 = current set of predictors
		//lstOut = output
		//mdl1 = current model
		
		Model mdl1;
		KSubsets ksb1;
		MultipleQuantileRegression mqr1;
		ArrayList<String> lst1;
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>((int) Math.pow(2,lstPredictors.size()));
		lstOut.add("N_PREDICTORS,CV_R2,COEFFICIENTS");
		mdlBest = null;
		mqd1.loadNeighborhoods(lstPredictors);
		
		//***************************
		//Printer.print(mqd1.print());
		//***************************
		
		for(int i=1;i<=3;i++){
			ksb1 = new KSubsets(lstPredictors,i);
			do {
				lst1 = ksb1.next();
				mqr1 = new MultipleQuantileRegression(mqd1, lst1);
				mdl1 = new Model(mqr1);
				
				//*****************************
				System.out.println(mdl1);
				//*****************************
				
				lstOut.add(mdl1.toString());
				if(mdl1.isBetterThan(mdlBest)) {
					mdlBest = new Model(mdl1);
				}
			}while(ksb1.hasNext());
		}
		return lstOut;
	}
	
	public ArrayList<String> forward(ArrayList<String> lstPredictors, int iMaximumSize) {
		
		//set1 = set of candidate predictors remaining
		//lst0 = current starting list of predictors
		//lst1 = current list of predictors
		//lst2 = list of predictors omitting interaction
		//mqr1 = multiple quantile regression
		//mdl1 = current model
		//lstOut = output
		//bInteraction = flag for whether there is an interaction
		
		
		MultipleQuantileRegression mqr1;
		HashSet<String> set1;
		ArrayList<String> lst0;
		ArrayList<String> lst1;	
		Model mdl1;
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(iMaximumSize*lstPredictors.size());
		lstOut.add("N_PREDICTORS,CV_R2,COEFFICIENTS");
		set1 = new HashSet<String>(lstPredictors);
		lst0 = new ArrayList<String>(1);
		mdlBest = null;
		do {
			for(String s:set1) {
				lst1 = new ArrayList<String>(lst0);
				lst1.add(s);
				mqd1.loadNeighborhoods(lst1);
				mqr1 = new MultipleQuantileRegression(mqd1, lst1);
				mdl1 = new Model(mqr1);
				
				//*****************************
				System.out.println(mdl1);
				//*****************************
				
				lstOut.add(mdl1.toString());
				if(mdl1.isBetterThan(mdlBest)) {
					mdlBest = new Model(mdl1);
				}
				lst1.remove(s);
			}
			for(String s:mdlBest.predictors()) {
				set1.remove(s);
			}
			lst0 = new ArrayList<String>(mdlBest.predictors());
		}while(mdlBest.size()<iMaximumSize);
		return lstOut;	
	}
	
	public ArrayList<String> interactors(ArrayList<String> lstPredictors){
		
		//lst1 = output
		
		ArrayList<String> lst1;
		
		lst1 = new ArrayList<String>(2);
		
		for(String s:lstPredictors) {
			if(!s.equals("[interaction]")) {
				lst1.add(s);
			}
		}
		return lst1;
	}
	
	public class Model{
		
		/**List of predictors**/
		private ArrayList<String> lstPredictors;
		
		/**Cross validation R^2**/
		private double dCVR2;
		
		/**Coefficients**/
		private TreeMap<String,Double> mapCoefficients;
		
		public Model(Model mdl1) {
			this.dCVR2 = mdl1.cvR2();
			this.lstPredictors = new ArrayList<String>(mdl1.predictors());
			this.mapCoefficients = new TreeMap<String,Double>(mdl1.coefficients());
		}
		
		public Model(MultipleQuantileRegression mqr1){
			lstPredictors = new ArrayList<String>(mqr1.predictors());
			dCVR2 = mqr1.crossValidationR2();
			mapCoefficients = new TreeMap<String,Double>(mqr1.coefficients());
		}
		
		public boolean isBetterThan(Model mdl1) {
			if(mdl1==null || this.cvR2()>mdl1.cvR2()) {
				return true;
			}else {
				return false;
			}
		}
		
		public double cvR2() {
			return dCVR2;
		}
		
		public ArrayList<String> predictors(){
			return lstPredictors;
		}
		
		public TreeMap<String,Double> coefficients(){
			return mapCoefficients;
		}
		
		public String toString(){
			
			//sbl1 = coefficients map
			
			StringBuilder sbl1;
			
			sbl1 = new StringBuilder();
			if(mapCoefficients.keySet().contains("(INTERCEPT)")){
				sbl1.append("(INTERCEPT)" + ":" + mapCoefficients.get("(INTERCEPT)"));
			}
			
			for(String s:mapCoefficients.keySet()) {
				if(!s.equals("(INTERCEPT)")){
					if(sbl1.length()==0) {
						sbl1.append(s + ":" + mapCoefficients.get(s));
					}else {
						sbl1.append(";" + s + ":" + mapCoefficients.get(s));
					}
				}
			}
			
			return lstPredictors.size() + "," + dCVR2 + "," + sbl1.toString();
		}
		
		public int size() {
			return lstPredictors.size();
		}
	}
	
	public class KSubsets{
		
		private int m;
		private int h;
		private int k;
		private int n;
		private int[] a;
		private boolean bExit;
		private boolean bFirst;
		private ArrayList<String> lstCurrentSubset;
		private ArrayList<String> lstObjects;
		
		public KSubsets(ArrayList<String> lstObjects, int iK){

			this.lstObjects = new ArrayList<String>(lstObjects);
			k = iK;
			n = lstObjects.size();
			a = new int[k+1];
			for(int i=1;i<=k;i++) {
				a[i]=i;
			}
			lstCurrentSubset = new ArrayList<String>(k);
			for(int i=1;i<a.length;i++){
				lstCurrentSubset.add(lstObjects.get(a[i]-1));
			}
			m = 0;
			h = k;
			bExit = false;
			d();
			bFirst = true;
		}
		
		public boolean hasNext() {
			return !bExit;
		}
		
		public ArrayList<String> next() {
			
			if(bFirst) {
				bFirst = false;
				return lstCurrentSubset;
			}
			if(m>=n-h) {
				c();
			}else {
				h=0;
				c();
			}
			return lstCurrentSubset;
		}
		
		private void c() {
			h++;
			m=a[k+1-h];
			d();
		}
		
		private void d() {
			
			//a[k+1-h]=m+1;
			//a[k]=m+h;
			
			for(int j=1;j<=h;j++) {
				a[k+j-h]=m+j;
			}
			lstCurrentSubset = new ArrayList<String>(k);
			for(int i=1;i<a.length;i++){
				lstCurrentSubset.add(lstObjects.get(a[i]-1));
			}
			if(a[1]==n-k+1) {
				bExit=true;
			}
		}
	}
	
}

package edu.ucsf.BetaDiversityAssociation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.HashMultimap;

import edu.ucsf.base.MutableBetaDiversity;
import edu.ucsf.io.BiomIO;

public class BetaDiversityAssociationData{

	/**Response map**/
	public HashMap<String,Double> mapResponses;
	
	/**Map from sample pair names to merged sample pair names**/
	public HashMap<String,String> mapMerge;
	
	/**Mutable beta-diversity object**/
	public MutableBetaDiversity mbd1;
	
	/**List of thresholds (percentiles or logistic regression thresholds to use**/
	public Double[] rgdThresholds;
	
	/**Number of null iterations to use**/
	public int iNullIterations;
	
	/**Number of partitioning orders**/
	public int iPartitioningOrders;
	
	/**Map from group names to taxa**/
	public HashMultimap<String,String> mapGroups;
	
	/**Threshold to use for selecting taxa for which to calculate standardized effect sizes and p-values**/
	public double dObservedValueThreshold;
	
	/**Interactions object**/
	public BetaDiversityInteractions int1;
	
	/**Significance object**/
	public BetaDiversitySignificance sig1;
	
	/**Regression object**/
	public Regression rgn1;
	
	/**Window size for fast quantile regression**/
	public int iWindowSize;
	
	/**Hierarchical partitioning object**/
	public BetaDiversityHierarchicalPartitioning hir1;
	
	/**Biom object**/
	public BiomIO bio1;
	
	/**Short name map**/
	public HashMap<String,String> mapShortNames;
	
	/**Map from covariate names to map from sample pairs to values for that covariate**/
	public HashMap<String,HashMap<String,Double>> mapCovariates;
	
	/**Regression type**/
	public String sRegressionType;
	
	/**Forward model selection object**/
	public BetaDiversityForwardModelSelection fwm1;
	
	/**List of groups to consider**/
	public HashSet<String> setGroupsToConsider;
	
	/**Flag for whether to consider width of percentile (e.g., interquartile range)**/
	public boolean bRange;
	
	public BetaDiversityAssociationData(
			HashMap<String,Double> mapResponses, 
			HashMap<String,String> mapMerge, 
			MutableBetaDiversity mbd1,
			int iNullIterations,
			int iPartitioningOrders,
			HashMultimap<String,String> mapGroups,
			double dObservedValueThreshold,
			int iWindowSize,
			BiomIO bio1,
			HashMap<String,String> mapShortNames,
			HashMap<String,HashMap<String,Double>> mapCovariates,
			Double rgdThresholds[],
			String sRegressionType,
			HashSet<String> setGroupsToConsider,
			boolean bRange){
		
		this.mapResponses=mapResponses;
		this.mapMerge=mapMerge;
		this.mbd1=mbd1;
		this.iNullIterations=iNullIterations;
		this.iPartitioningOrders=iPartitioningOrders;
		this.mapGroups=mapGroups;
		this.dObservedValueThreshold=dObservedValueThreshold;
		this.iWindowSize = iWindowSize;
		this.mapShortNames = mapShortNames;
		this.bio1 = bio1;
		this.mapCovariates = mapCovariates;
		this.sRegressionType = sRegressionType;
		this.rgdThresholds = rgdThresholds;
		this.setGroupsToConsider = setGroupsToConsider;
		this.bRange = bRange;
		
		if(sRegressionType.equals("windowed_quantile")) {
			rgn1 = new WindowedQuantileRegression(
					mapResponses,
					mapMerge,
					iWindowSize,
					rgdThresholds[0],
					bRange,
					iNullIterations);
			
		}else if(sRegressionType.equals("logistic")) {
			//TODO make threshold an argument
			rgn1 = new LogisticRegression(
					mapResponses,
					mapMerge,
					rgdThresholds[0],
					iNullIterations);
			
		}else{
			rgn1 = null;
		}
	}
	
	public ArrayList<String> printTaxonIDs(){
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(bio1.axsObservation.size()+1);
		lstOut.add("OBSERVATION");
		for(String s:bio1.axsObservation.getIDs()) {
			lstOut.add(s);
		}
		return lstOut;
	}
	
	public ArrayList<String> printSampleIDs(){
		
		//lstOut = output
		
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(bio1.axsSample.size()+1);
		lstOut.add("SAMPLE");
		for(String s:bio1.axsSample.getIDs()) {
			lstOut.add(s);
		}
		return lstOut;
	}	
	
	public void loadInteractions(){
		int1 = new BetaDiversityInteractions(this);
	}
	
	public void loadSignificance() {
		sig1 = new BetaDiversitySignificance(this);
	}
	
	public Regression regressionNoNull(){
		
		if(sRegressionType.equals("windowed_quantile")) {
			return new WindowedQuantileRegression(
					mapResponses,
					mapMerge,
					iWindowSize,
					rgdThresholds[0],
					bRange,
					0);
			
		}else if(sRegressionType.equals("logistic")) {
			return new LogisticRegression(
					mapResponses,
					mapMerge,
					rgdThresholds[0],
					0);
			
		}else {
			return null;
		}
	}
	
	public Regression regressionNewThreshold(double dThreshold){
			
		if(sRegressionType.equals("windowed_quantile")) {
			return new WindowedQuantileRegression(
					mapResponses,
					mapMerge,
					iWindowSize,
					dThreshold,
					bRange,
					iNullIterations);
			
		}else if(sRegressionType.equals("logistic")) {
			return new LogisticRegression(
					mapResponses,
					mapMerge,
					dThreshold,
					iNullIterations);
			
		}else {
			return null;
		}
	}
	
	public void loadHierarchicalPartitioning(){
		hir1 = new BetaDiversityHierarchicalPartitioning(this);
	}
	
	public void loadForwardModelSelection(){
		fwm1 = new BetaDiversityForwardModelSelection(this);
	}
}
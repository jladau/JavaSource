package edu.ucsf.StandardizedEffectSizes;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class StandardizedEffectSizesLauncher{

	public static void main(String rgsArgs[]) {
		
		//arg1 = arguments
		//dat1 = data
		//rgsCategories = category headers
		//sValue = value header
		//sRandomization = randomization header
		//mapObs = map from category to observed value
		//mapNull = map from category to null values
		//mapNullMean = map from category to null mean
		//mapNullStDev = map from category to null standard deviation
		//s1 = current category
		//mapLTE = number of null iterations less than or equal to value
		//mapGTE = number of null iterations greater than or equal to value
		//mapSES = standardized effect size
		//mapMean = null mean
		//mapStDev = null standard deviation		
		//iNullIterations = number of null iterations
		//dMean = current mean
		//dStDev = current standard deviation
		//lstOut = output
		
		ArrayList<String> lstOut;
		double dMean;
		double dStDev;
		int iNullIterations;
		ArrayListMultimap<String,Double> mapNull;
		HashMap_AdditiveDouble<String> mapLTE;
		HashMap_AdditiveDouble<String> mapGTE;
		HashMap<String,Double> mapSES;
		HashMap<String,Double> mapMean;
		HashMap<String,Double> mapStDev;
		ArgumentIO arg1;
		DataIO dat1;
		String rgsCategories[];
		String sValue;
		String sRandomization;
		String s1;
		HashMap<String,Double> mapObs;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rgsCategories = arg1.getValueStringArray("rgsCategoryHeaders");
		sValue = arg1.getValueString("sValueHeader");
		sRandomization = arg1.getValueString("sRandomizationHeader");
		iNullIterations = arg1.getValueInt("iNullIterations");
		
		//initial iteration to load observed values and other values
		mapObs = new HashMap<String,Double>(dat1.iRows);
		mapNull = ArrayListMultimap.create(dat1.iRows,iNullIterations);
		for(int i=1;i<dat1.iRows;i++){
			s1 = dat1.getString(i,rgsCategories);
			if(dat1.getString(i,sRandomization).contains("null")) {
				mapNull.put(s1,dat1.getDouble(i,sValue));
			}else if(dat1.getString(i,sRandomization).contains("observ")) {
				mapObs.put(s1,dat1.getDouble(i,sValue));
			}
		}
		
		mapSES = new HashMap<String,Double>(mapObs.size());
		mapMean = new HashMap<String,Double>(mapObs.size());
		mapStDev = new HashMap<String,Double>(mapObs.size());
		mapLTE = new HashMap_AdditiveDouble<String>(mapObs.size());
		mapGTE = new HashMap_AdditiveDouble<String>(mapObs.size());;
		for(String s:mapObs.keySet()) {			
			if(mapNull.get(s).size()==0) {
				
				mapSES.put(s,Double.NaN);
				mapLTE.put(s,Double.NaN);
				mapGTE.put(s,Double.NaN);
				
			}else {
			
				//finding standardized effect sizes
				dMean = ExtendedMath.mean(new ArrayList<Double>(mapNull.get(s)));
				dStDev= ExtendedMath.standardDeviation(new ArrayList<Double>(mapNull.get(s)));
				mapMean.put(s,dMean);
				mapStDev.put(s,dStDev);
				mapSES.put(s,(mapObs.get(s) - dMean)/dStDev);
				
				//updating probabilities
				mapLTE.put(s,0.);
				mapGTE.put(s,0.);
				for(double d:mapNull.get(s)) {
					if(d<=mapObs.get(s)) {
						mapLTE.putSum(s,1.);
					}
					if(d>=mapObs.get(s)) {
						mapGTE.putSum(s,1.);
					}
				}
			}
		}
		
		//updating probabilities
		for(String s:mapObs.keySet()) {
			if(Double.isNaN(mapLTE.get(s))) {
				continue;
			}
			if(mapLTE.containsKey(s)){
				mapLTE.put(s,mapLTE.get(s)/((double) mapNull.get(s).size()));
			}else {
				mapLTE.put(s, 0.);
			}
			if(mapGTE.containsKey(s)) {
				mapGTE.put(s,mapGTE.get(s)/((double) mapNull.get(s).size()));
			}else {
				mapGTE.put(s, 0.);
			}
		}
		
		//outputting results
		lstOut = new ArrayList<String>(mapLTE.size()+1);
		lstOut.add(Joiner.on(",").join(rgsCategories) + ",OBSERVED,NULL_MEAN,NULL_STDEV,SES,PR_GTE,PR_LTE");
		for(String s:mapObs.keySet()){
			lstOut.add((s + "," + mapObs.get(s) + "," + mapMean.get(s) + "," + mapStDev.get(s) + "," + mapSES.get(s) + "," + mapGTE.get(s) + "," + mapLTE.get(s)).replaceAll(",NaN",",na").replaceAll(",null",",na"));
		}
		
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
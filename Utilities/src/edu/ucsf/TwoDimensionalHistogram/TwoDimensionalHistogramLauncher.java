package edu.ucsf.TwoDimensionalHistogram;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.base.Joiner;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class TwoDimensionalHistogramLauncher{

	public static void main(String rgsArgs[]){
		
		//dat1 = data
		//arg1 = arguments
		//sX = x variable
		//sY = y variable
		//rgsCategories = category variables
		//mapHistograms = map from category (concatenated) to histogram
		//dThreshold = threshold
		//sblCategory = current concatenated category
		
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<String> lstOut;
		double dThreshold;
		String sX;
		String sY;
		String[] rgsCategories;
		StringBuilder sblCategory;
		HashMap<String,TwoDimensionalHistogram> mapHistograms;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sX = arg1.getValueString("sXHeader");
		sY = arg1.getValueString("sYHeader");
		dThreshold = arg1.getValueDouble("dDistanceThreshold");
		if(arg1.containsArgument("rgsCategories")) {
			rgsCategories = arg1.getValueStringArray("rgsCategories");
		}else {
			rgsCategories = null;
		}
		
		//loading histogram data
		mapHistograms = new HashMap<String,TwoDimensionalHistogram>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++) {
			sblCategory = new StringBuilder();
			if(rgsCategories == null) {
				sblCategory.append("-9999");
			}else {
				for(String s:rgsCategories) {
					sblCategory.append(dat1.getString(i,s) + ",");
				}
			}
			if(!mapHistograms.containsKey(sblCategory.toString())) {
				mapHistograms.put(sblCategory.toString(), new TwoDimensionalHistogram(dat1.iRows));
			}
			mapHistograms.get(sblCategory.toString()).add(dat1.getDouble(i,sX),dat1.getDouble(i,sY));
		}
		
		//loading histograms and outputting
		lstOut = new ArrayList<String>(dat1.iRows);
		if(rgsCategories==null) {
			lstOut.add(sX + "," + sY + ",FREQUENCY");
		}else {
			lstOut.add(Joiner.on(",").join(rgsCategories) + "," + sX + "," + sY + ",FREQUENCY");
		}
		for(String s:mapHistograms.keySet()){
			mapHistograms.get(s).loadHistogram(dThreshold);
			lstOut.addAll(mapHistograms.get(s).print(s));
		}
		
		//terminating
		DataIO.writeToFile(lstOut,arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}

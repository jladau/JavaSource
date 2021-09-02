package edu.ucsf.InterpolationByCategory;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import edu.ucsf.base.LinearModel;
import edu.ucsf.base.LoessInterpolation;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Finds predictions by running linear regressions on flat file within each specified category.
 * @author jladau
 *
 */

public class InterpolationByCategoryLauncher {
	
	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data
		//map1 = map from each category name to indices of rows for it
		
		ArgumentIO arg1;
		DataIO dat1;
		HashMultimap<String,Integer> map1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		map1 = HashMultimap.create();
		for(int i=1;i<dat1.iRows;i++){
			map1.put(dat1.getString(i, arg1.getValueString("sCategoryKey")),i);
		}
		
		//running interpolation
		if(arg1.getValueString("sInterpolationMethod").equals("linear")){
			runLinearInterpolation(dat1, map1, arg1.getValueString("sPredictorKey"), arg1.getValueString("sResponseKey"), arg1.getValueString("sOutputPath"));
		}else if(arg1.getValueString("sInterpolationMethod").equals("loess")){
			runLoessInterpolation(dat1, map1, arg1.getValueString("sPredictorKey"), arg1.getValueString("sResponseKey"), arg1.getValueString("sOutputPath"));
		}
		
		//terminating
		System.out.println("Done.");
	}
	
	private static void runLoessInterpolation(DataIO dat1, HashMultimap<String,Integer> map1, String sPredictorKey, String sResponseKey, String sOutputPath) throws Exception{
		
		//rgdX = x coordinates
		//rgdY = y coordinates
		//rgdSmooth = interpolated values
		//i1 = counter
		//map4 = map from input matrix rows to data rows
		
		double rgdX[];
		double rgdY[];
		double rgdSmooth[];
		int i1;
		HashMap<Integer,Integer> map4;
		
		//initializing
		dat1.appendToLastColumn(0, "LOESS_SMOOTH");
		
		//looping through categories
		for(String s:map1.keySet()){
			rgdX = new double[map1.get(s).size()];
			rgdY = new double[map1.get(s).size()];
			i1=0;
			map4 = new HashMap<Integer,Integer>();
			for(int i:map1.get(s)){
				rgdX[i1] = dat1.getDouble(i, sPredictorKey);
				rgdY[i1] = dat1.getDouble(i, sResponseKey);
				map4.put(i1, i);
				i1++;
			}
			rgdSmooth = LoessInterpolation.smooth(rgdX, rgdY);
			for(int i=0;i<rgdSmooth.length;i++){
				dat1.appendToLastColumn(map4.get(i), rgdSmooth[i]);
			}
		}
		
		//writing output
		DataIO.writeToFile(dat1.getWriteableData(), sOutputPath);
	}
	
	private static void runLinearInterpolation(DataIO dat1, HashMultimap<String,Integer> map1, String sPredictorKey, String sResponseKey, String sOutputPath) throws Exception{
		
		//sSlope = current slope
		//sIntercept = current intercept
		//sR2 = current R^2 value
		//set1 = set of predictors
		//lnm1 = linear model
		//dft1 = decimal formatter
		//map2 = map of predictions
		//tbl1 = data table
		
		HashBasedTable<String,String,Double> tbl1;		
		DecimalFormat dft1;
		String sIntercept;
		String sSlope;
		String sR2;
		HashSet<String> set1;
		LinearModel lnm1;
		HashMap<String,Double> map2;
		
		dat1.appendToLastColumn(0, "LINEAR_PREDICTION,PREDICTION_R2,PREDICTION_INTERCEPT,PREDICTION_SLOPE");
		set1 = new HashSet<String>();
		set1.add("x");
		dft1 = new DecimalFormat("#.00000");
		
		//looping through categories
		for(String s:map1.keySet()){
			tbl1 = HashBasedTable.create();
			for(int i:map1.get(s)){
				tbl1.put("x", Integer.toString(i), dat1.getDouble(i, sPredictorKey));
				tbl1.put("y", Integer.toString(i), dat1.getDouble(i, sResponseKey));
			}
			lnm1 = new LinearModel(tbl1,"y",set1);
			lnm1.fitModel(set1);
			sR2 = dft1.format(lnm1.findRSquared());
			sIntercept = dft1.format(lnm1.findCoefficientEstimates().get("(Intercept)"));
			sSlope = dft1.format(lnm1.findCoefficientEstimates().get("x"));
			map2 = lnm1.findPredictedValues();
			for(int i:map1.get(s)){
				dat1.appendToLastColumn(i,map2.get(Integer.toString(i)) + "," + sR2 + "," + sIntercept + "," + sSlope);
			}
		}
		
		//writing output
		DataIO.writeToFile(dat1.getWriteableData(), sOutputPath);
	}
}
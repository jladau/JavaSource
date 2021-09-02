package edu.ucsf.CorrelationsWithinClasses;

import java.util.ArrayList;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Calculates correlations within classes
 * @author jladau
 */

public class CorrelationsWithinClassesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//mapX = map from index values to list of x values
		//mapY = map from index values to list of y values
		//sbl1 = current key
		//lstOut = output
		//rgd1 = current slope significance
		//rgd2 = current slope bootstrap confidence bounds
		
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		ArgumentIO arg1;
		DataIO dat1;
		ArrayListMultimap<String,Double> mapX;
		ArrayListMultimap<String,Double> mapY;
		double rgd1[];
		double rgd2[];
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		mapX = ArrayListMultimap.create(10000, 100);
		mapY = ArrayListMultimap.create(10000, 100);
		lstOut = new ArrayList<String>(dat1.iRows);
		
		//looping through data
		for(int i=1;i<dat1.iRows;i++){
			sbl1 = new StringBuilder();
			for(String s:arg1.getValueStringArray("rgsIndexFields")){
				if(sbl1.length()>0){
					sbl1.append(",");
				}
				sbl1.append(dat1.getString(i, s));
			}
			mapX.put(sbl1.toString(), dat1.getDouble(i, arg1.getValueString("sXField")));
			mapY.put(sbl1.toString(), dat1.getDouble(i, arg1.getValueString("sYField")));
		}
		
		//outputting results
		lstOut.add(Joiner.on(",").join(arg1.getValueStringArray("rgsIndexFields")) + 
				",PEARSON_CORRELATION,SPEARMAN_CORRELATION,COEFFICIENT_OF_DETERMINATION,SLOPE,SLOPE_SIGNIFICANCE_LT,SLOPE_SINGNIFICANCE_GT,SLOPE_BOOTSTRAP_95_LB,SLOPE_BOOTSTRAP_95_UB");
		for(String s:mapX.keySet()){
			rgd1 = ExtendedMath.slopeSignificance(new ArrayList<Double>(mapX.get(s)), new ArrayList<Double>(mapY.get(s)));
			rgd2 = ExtendedMath.slopeBootstrapCI(new ArrayList<Double>(mapX.get(s)), new ArrayList<Double>(mapY.get(s)));
			
			lstOut.add(s + 
					"," + ExtendedMath.pearson(new ArrayList<Double>(mapX.get(s)), new ArrayList<Double>(mapY.get(s))) +
					"," + ExtendedMath.spearman(new ArrayList<Double>(mapX.get(s)), new ArrayList<Double>(mapY.get(s))) + 
					"," + ExtendedMath.coefficientOfDetermination(new ArrayList<Double>(mapX.get(s)), new ArrayList<Double>(mapY.get(s))) +
					"," + ExtendedMath.slope(new ArrayList<Double>(mapX.get(s)), new ArrayList<Double>(mapY.get(s))) +
					"," + rgd1[0] +
					"," + rgd1[1] +
					"," + rgd2[0] +
					"," + rgd2[1]);
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	
}

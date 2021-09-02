package edu.ucsf.InterpolationByCategoryToValue;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Interpolates to a particular value, separate itnerpolations for different categories
 * @author jladau
 *
 */

public class InterpolationByCategoryToValueLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data
		//sCategory = category header
		//sX = x header
		//sY = y header
		//dX = current x
		//dY = current y
		//dXInterpolate = x value to interpolate to
		//mapBounds = returns lower x, lower y, upper x, upper y for given category
		//s1 = current category
		//lstOut = output
		//rgd1 = current bounds
		
		Double[] rgd1;
		double dXInterpolate;
		ArgumentIO arg1;
		DataIO dat1;
		String sX;
		String sY;
		String s1;
		double dX;
		double dY;
		String sCategory;
		HashMap<String,Double[]> mapBounds;
		ArrayList<String> lstOut;
		
		//initializing
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sCategory = arg1.getValueString("sCategoryHeader");
		sX = arg1.getValueString("sXHeader");
		sY = arg1.getValueString("sYHeader");
		dXInterpolate = arg1.getValueDouble("dXInterpolationValue");
		
		//loading bounds
		mapBounds = new HashMap<String,Double[]>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			s1 = dat1.getString(i, sCategory);
			if(!mapBounds.containsKey(s1)){
				mapBounds.put(s1, new Double[]{-Double.MAX_VALUE, Double.NaN, Double.MAX_VALUE, Double.NaN});
			}
			dX = dat1.getDouble(i, sX);
			dY = dat1.getDouble(i, sY);
			if(dX<=dXInterpolate){
				if(dX>mapBounds.get(s1)[0]){
					mapBounds.get(s1)[0] = dX;
					mapBounds.get(s1)[1] = dY;
				}
			}
			if(dX>=dXInterpolate){
				if(dX<mapBounds.get(s1)[2]){
					mapBounds.get(s1)[2] = dX;
					mapBounds.get(s1)[3] = dY;
				}
			}
		}
		
		//loading results
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add(sCategory + "," + sX + "," + sY + "_INTERPOLATED");
		for(String s:mapBounds.keySet()){
			rgd1 = mapBounds.get(s);
			if(!Double.isNaN(rgd1[1]) && !Double.isNaN(rgd1[3])){
				if(rgd1[0]==rgd1[2]){
					dY = rgd1[1];
				}else{
					dY = (rgd1[1]*(rgd1[2]-dXInterpolate)+rgd1[3]*(dXInterpolate-rgd1[0]))/(rgd1[2]-rgd1[0]);
				}
			}else{
				dY = Double.NaN;
			}
			lstOut.add(s + "," + dXInterpolate + "," + dY);
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}

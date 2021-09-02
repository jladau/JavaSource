package edu.ucsf.SlopesWithinCategories;

import java.util.ArrayList;

import com.google.common.collect.ArrayListMultimap;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Calculates slopes within categories
 * @author jladau
 *
 */

public class SlopesWithinCategoriesLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//mapX = x coordiantes
		//mapY = y coordinates
		//sCategory = category field
		//sX = x field
		//sY = y field
		//lstOut = output
		
		ArgumentIO arg1;
		DataIO dat1;
		ArrayListMultimap<String,Double> mapX;
		ArrayListMultimap<String,Double> mapY;
		String sCategory;
		String sX;
		String sY;
		ArrayList<String> lstOut;
		
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		sCategory = arg1.getValueString("sCategoryField");
		sX = arg1.getValueString("sXField");
		sY = arg1.getValueString("sYField");
		mapX = ArrayListMultimap.create(dat1.iRows,25);
		mapY = ArrayListMultimap.create(dat1.iRows,25);
		for(int i=1;i<dat1.iRows;i++){
			mapX.put(dat1.getString(i, sCategory), dat1.getDouble(i, sX));
			mapY.put(dat1.getString(i, sCategory), dat1.getDouble(i, sY));
		}
		lstOut = new ArrayList<String>(mapX.size());
		lstOut.add(sCategory + ",N,SLOPE");
		for(String s:mapX.keySet()){
			lstOut.add(s + "," + mapX.get(s).size() + "," + ExtendedMath.slope(new ArrayList<Double>(mapX.get(s)), new ArrayList<Double>(mapY.get(s))));
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");	
	}
}
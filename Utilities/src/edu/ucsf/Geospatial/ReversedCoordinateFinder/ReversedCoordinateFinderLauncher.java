package edu.ucsf.Geospatial.ReversedCoordinateFinder;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.base.Joiner;

import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

/**
 * Finds pairs of observations that appear to have reversed latitudes or longitudes (e.g., -173 and 173)
 * @author jladau
 *
 */

public class ReversedCoordinateFinderLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//dat1 = data
		//rgd1 = current lat1, lon1, lat2, lon2
		//set1 = set of printed rows
		//lstOut = output
		//s1 = first sample
		//s2 = second sample
				
		ArgumentIO arg1;
		DataIO dat1;
		double rgd1[];
		ArrayList<String> lstOut;
		HashSet<Integer> set1;
		String s1;
		String s2;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rgd1 = new double[4];
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add(Joiner.on(",").join(dat1.getRow(0)));
		set1 = new HashSet<Integer>(dat1.iRows);
		
		//looping through pairs of rows
		for(int i=2;i<dat1.iRows;i++){
			rgd1[0]=dat1.getDouble(i, "LATITUDE");
			rgd1[1]=dat1.getDouble(i, "LONGITUDE");
			
			if(arg1.containsArgument("sCategoryHeader")){	
				s1 = dat1.getString(i, arg1.getValueString("sCategoryHeader"));
			}else{
				s1 = "null";
			}
			for(int j=1;j<i;j++){
				rgd1[2]=dat1.getDouble(j, "LATITUDE");
				rgd1[3]=dat1.getDouble(j, "LONGITUDE");
				if(arg1.containsArgument("sCategoryHeader")){	
					s2 = dat1.getString(j, arg1.getValueString("sCategoryHeader"));
				}else{
					s2 = "null";
				}
				if(((Math.abs(rgd1[0]+rgd1[2])<0.5 && Math.abs(rgd1[1]-rgd1[3])<0.5) || (Math.abs(rgd1[1]+rgd1[3])<0.5 && Math.abs(rgd1[0]-rgd1[2])<0.5)) && s1.equals(s2)){
					for(int k:new int[]{i,j}){
						if(!set1.contains(k)){
							lstOut.add(Joiner.on(",").join(dat1.getRow(k)));
							set1.add(k);
						}
					}
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}

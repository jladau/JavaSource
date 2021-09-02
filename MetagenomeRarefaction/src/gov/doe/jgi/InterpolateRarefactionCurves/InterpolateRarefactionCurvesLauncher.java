package gov.doe.jgi.InterpolateRarefactionCurves;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class InterpolateRarefactionCurvesLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = rarefaction curves
		//dat2 = values for which to interpolate curves
		//map1 = rarefaction curves
		//lst1 = list of rarefaction (interpolation) values
		//rgd1 = minimum and maximum interpolation depth
		//dX = read depth for interpolation
		//dY = current interpolated depth
		//lstOut = output
		
		ArrayList<String> lstOut;
		double dX;
		double dY;
		double rgd1[];
		ArgumentIO arg1;
		DataIO dat1;
		DataIO dat2;
		HashMap<String,TreeMap<Double,Double>> map1;
		ArrayList<Double> lst1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sRarefactionCurvePath"));
		dat2 = new DataIO(arg1.getValueString("sInterpolationValuesPath"));
		map1 = new HashMap<String,TreeMap<Double,Double>>(3000);
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("METAGENOME,NUMBER_READS_ASSEMBLED,RICHNESS");
		
		//loading rarefaction curves
		for(int i=1;i<dat1.iRows;i++){
			if(!map1.containsKey(dat1.getString(i, "METAGENOME"))){
				map1.put(dat1.getString(i, "METAGENOME"), new TreeMap<Double,Double>());
			}
			map1.get(dat1.getString(i, "METAGENOME")).put(dat1.getDouble(i, "NUMBER_READS_ASSEMBLED"), dat1.getDouble(i, "RICHNESS"));
		}
		
		//loading interpolation depths
		lst1 = new ArrayList<Double>(dat2.iRows);
		rgd1 = new double[]{Double.MAX_VALUE,-Double.MAX_VALUE};
		for(int i=1;i<dat2.iRows;i++){
			lst1.add(dat2.getDouble(i, 0));
			if(lst1.get(i-1)<rgd1[0]){
				rgd1[0]=lst1.get(i-1);
			}
			if(lst1.get(i-1)>rgd1[1]){
				rgd1[1]=lst1.get(i-1);
			}
		}
		
		//looping through metagenomes
		for(String s:map1.keySet()){
			if(map1.get(s).firstKey()<=rgd1[0]){
				if(map1.get(s).lastKey()>=rgd1[1]){
					for(int i=0;i<lst1.size();i++){
						dX = lst1.get(i);
						dY = ExtendedMath.linearInterpolation(
								dX, 
								map1.get(s).floorKey(dX), 
								map1.get(s).floorEntry(dX).getValue(), 
								map1.get(s).ceilingKey(dX), 
								map1.get(s).ceilingEntry(dX).getValue());
						lstOut.add(s + "," + dX + "," + dY);
					}
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");	
	}
}

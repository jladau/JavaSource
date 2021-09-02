package gov.doe.jgi.InterpolateRarefactionDepth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import edu.ucsf.base.ExtendedMath;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;

public class InterpolateRarefactionDepthLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = rarefaction curves
		//map1 = rarefaction curves
		//dX = read depth for interpolation
		//dY = current interpolated depth
		//lstOut = output
		
		ArrayList<String> lstOut;
		double dX;
		double dY;
		ArgumentIO arg1;
		DataIO dat1;
		HashMap<String,TreeMap<Double,Double>> map1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sRarefactionCurvePath"));
		map1 = new HashMap<String,TreeMap<Double,Double>>(3000);
		lstOut = new ArrayList<String>(dat1.iRows);
		lstOut.add("METAGENOME,NUMBER_READS_ASSEMBLED,NUMBER_READS");
		dX = arg1.getValueDouble("dTargetAssembledReadDepth");
		
		//loading rarefaction curves
		for(int i=1;i<dat1.iRows;i++){
			if(!map1.containsKey(dat1.getString(i, "METAGENOME"))){
				map1.put(dat1.getString(i, "METAGENOME"), new TreeMap<Double,Double>());
			}
			map1.get(dat1.getString(i, "METAGENOME")).put(dat1.getDouble(i, "NUMBER_READS_ASSEMBLED"), dat1.getDouble(i, "NUMBER_READS"));
		}
		
		//looping through metagenomes
		for(String s:map1.keySet()){
			if(map1.get(s).firstKey()<=dX){
				if(map1.get(s).lastKey()>=dX){					
					dY = ExtendedMath.linearInterpolation(
							dX, 
							map1.get(s).floorKey(dX), 
							map1.get(s).floorEntry(dX).getValue(), 
							map1.get(s).ceilingKey(dX), 
							map1.get(s).ceilingEntry(dX).getValue());
					lstOut.add(s + "," + dX + "," + Math.round(dY));
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");	
	}
	
	
}

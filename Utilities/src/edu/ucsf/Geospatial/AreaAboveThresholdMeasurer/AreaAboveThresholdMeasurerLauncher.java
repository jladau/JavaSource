package edu.ucsf.Geospatial.AreaAboveThresholdMeasurer;

import java.util.ArrayList;

import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRasterOperations;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfReader;

/**
 * This code measures the positive area in a raster for all variables
 * @author jladau
 */

public class AreaAboveThresholdMeasurerLauncher {

	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//ncr1 = first netcdf reader
		//lstVars = list of variables
		//lstOut = output
		//sbl1 = current output line
		//dPositive = current positive area
		//dTotal = current total area
		
		ArgumentIO arg1;
		NetcdfReader ncr1;
		ArrayList<String> lstVars;
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		double dPositive;
		double dTotal;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		ncr1 = new NetcdfReader(arg1.getValueString("sRasterPath"));
		
		//loading list of climatologies 
		lstVars = ncr1.getPlottableVars();
		ncr1.close();
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add("RASTER,VARIABLE,TIME,VERT,POSITIVE_AREA,TOTAL_AREA,FRACTION_POSITIVE_AREA");
		
		//looping through variables
		for(String sVar:lstVars){
			ncr1 = new NetcdfReader(arg1.getValueString("sRasterPath"), sVar);
			for(GeospatialRaster.TimeVert tvt1:ncr1.getAllTimeVertCombinations()){
				try{
					ncr1.loadGrid(tvt1.tim1, tvt1.dVert);
				}catch(Exception e){
					e.printStackTrace();
				}
				sbl1 = new StringBuilder();
				sbl1.append(arg1.getValueString("sRasterPath") + "," + sVar);
				sbl1.append(!tvt1.tim1.equals(GeospatialRaster.NULL_TIME) ? "," + tvt1.tim1 : ",na");
				sbl1.append(tvt1.dVert != GeospatialRaster.NULL_VERT ? "," + tvt1.dVert : ",na");
				dPositive = GeospatialRasterOperations.calculateAreaAboveThreshold(ncr1, tvt1.tim1, tvt1.dVert, arg1.getValueDouble("dThreshold"));
				sbl1.append("," + dPositive);
				dTotal = GeospatialRasterOperations.calculateNonErrorArea(ncr1, tvt1.tim1, tvt1.dVert);
				sbl1.append("," + dTotal);
				sbl1.append("," + dPositive/dTotal); 
				lstOut.add(sbl1.toString());
			}	
		}	
		
		//printing output
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		
		//terminating
		System.out.println("Done.");
	}
}
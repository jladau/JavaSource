package edu.ucsf.Geospatial.ClimatologyDifferences;

import java.util.ArrayList;

import edu.ucsf.base.CurrentDate;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRasterMetadata;
import edu.ucsf.geospatial.GeospatialRasterOperations;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.NetcdfReader;
import edu.ucsf.io.NetcdfWriter;

/**
 * Calculates the difference between all climatologies in a raster.
 * @author jladau
 */

public class ClimatologyDifferencesLauncher {

	public static void main(String[] rgsArgs){
		
		//arg1 = arguments
		//ncr1 = first netcdf reader
		//ncr2 = second netcdf reader
		//lstVars = list of variables
		//rgr1 = array of rasters for writing
		//iCounter = counter
		//ncw1 = netcdf writer
		//lstStartingClimatologies = list of unique starting climatologies
		
		NetcdfWriter ncw1;
		GeospatialRaster rgr1[];
		ArgumentIO arg1;
		NetcdfReader ncr1;
		NetcdfReader ncr2;
		ArrayList<String> lstVars;
		int iCounter;
		ArrayList<String> lstStartingClimatologies;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		ncr1 = new NetcdfReader(arg1.getValueString("sRasterPath"));
		
		//loading list of climatologies 
		lstVars = ncr1.getPlottableVars();
		ncr1.close();
		
		//loading list of unique starting climatologies
		lstStartingClimatologies = new ArrayList<String>();
		for(String s:arg1.getValueStringArray("rgsStartingClimatologies")){
			if(!lstStartingClimatologies.contains(s)){
				lstStartingClimatologies.add(s);
			}
		}
		
		//initializing output
		rgr1 = new GeospatialRaster[(lstVars.size()-1)*lstStartingClimatologies.size()];
		iCounter = 0;
		
		//looping through pairs of climatologies
		for(int i=0;i<lstVars.size();i++){
			for(int k=0;k<lstStartingClimatologies.size();k++){
				if(!lstVars.get(i).endsWith(lstStartingClimatologies.get(k))){
					continue;
				}
				ncr1 = new NetcdfReader(arg1.getValueString("sRasterPath"),lstVars.get(i));
				try {
					ncr1.loadGridAllVertsTimes();
				} catch (Exception e) {
					e.printStackTrace();
				}
				for(int j=0;j<lstVars.size();j++){
					if(j==i){
						continue;
					}
					ncr2 = new NetcdfReader(arg1.getValueString("sRasterPath"),lstVars.get(j));
					try {
						ncr2.loadGridAllVertsTimes();
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						rgr1[iCounter]=GeospatialRasterOperations.difference(
								ncr2, 
								ncr1, 
								new GeospatialRasterMetadata(
										ncr2.gmt1.title + "_MINUS_" + ncr1.gmt1.title,
										ncr2.gmt1.institution,
										ncr2.gmt1.references,
										ncr2.gmt1.source,
										CurrentDate.currentDate(),
										ncr2.gmt1.variable + "_MINUS_" + ncr1.gmt1.variable,
										ncr2.gmt1.units,
										ncr2.gmt1.long_name + " MINUS " + ncr1.gmt1.long_name,
										ncr2.gmt1.cell_methods));
								
					} catch (Exception e) {
						e.printStackTrace();
					}
					ncr2.close();
					iCounter++;
				}
				ncr1.close();
			}	
		}
		
		//outputting results
		try {
			ncw1 = new NetcdfWriter(rgr1, arg1.getValueString("sOutputPath"));
			for(int i=0;i<rgr1.length;i++){
				for(GeospatialRaster.TimeVert tvt1:rgr1[i].getAllTimeVertCombinations()){
					ncw1.writeRaster(rgr1[i], tvt1.tim1, tvt1.dVert);
				}
			}
			ncw1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//terminating
		System.out.println("Done.");
	}
	
	/*
	private static Range<LocalDate> getClimatologyRange(String sVar){
	
		//rgs1 = range of dates
	
		String rgs1[];
		
		rgs1 = StringUtils.right(sVar,22).replace("--",";").split(";");
		return Range.closed(new LocalDate(rgs1[0]), new LocalDate(rgs1[1]));
	}
	*/
}
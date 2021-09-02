package edu.ucsf.BIOM.MergeSamplesByRasterCells;

import java.util.ArrayList;

import org.joda.time.LocalDate;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;

import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfReader;

/**
 * This code merges samples that are in common: outputs a map from old sample IDs to new sample IDs. 
 * @author jladau
 *
 */

public class MergeSamplesByRasterCellsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//bio1 = table with data
		//ncf1 = raster with grid cells
		//arg1 = arguments
		//axeLat = latitude axis element of current sample
		//axeLon = longitude axis element of current sample
		//map1(iLat,iLon) = returns list of samples to be merged
		//lstOut = output
		//sMerged = merged name
		//tim1 = datetime to use for current merged cell
		
		LocalDate tim1;
		String sMerged;
		BiomIO bio1;
		ArgumentIO arg1;
		NetcdfReader ncf1;
		GeospatialRaster.AxisElement<Double> axeLat;
		GeospatialRaster.AxisElement<Double> axeLon;
		HashMultimap<String,String> map1;
		ArrayList<String> lstOut;
		
		//loading data
		arg1 = new ArgumentIO(rgsArgs);
		ncf1 = new NetcdfReader(arg1.getValueString("sRasterPath"));
		bio1 = new BiomIO(arg1.getValueString("sBiomPath"), arg1.getAllArguments());
		
		//loading list of samples to merge
		map1 = HashMultimap.create();
		for(String s:bio1.axsSample.getIDs()){
			
			//***************************
			System.out.println(bio1.axsSample.getMetadata(s).get("latitude"));
			//***************************
			
			axeLat=ncf1.axsLat.getAxisElementContaining(Double.parseDouble(bio1.axsSample.getMetadata(s).get("latitude")));
			axeLon=ncf1.axsLon.getAxisElementContaining(Double.parseDouble(bio1.axsSample.getMetadata(s).get("longitude")));
			map1.put(axeLat.iID + "," + axeLon.iID,s);
		}
		
		//initializing output
		lstOut = new ArrayList<String>(bio1.axsSample.size()+1);
		lstOut.add("sample_id_old,sample_id_new,latitude_new,longitude_new,datetime_new");
		
		//merging samples
		for(String s:map1.keySet()){
			if(map1.get(s).size()==1){
				for(String t:map1.get(s)){
					lstOut.add(
						t + "," +
						t + "," +
						bio1.axsSample.getMetadata(t).get("latitude") + "," +
						bio1.axsSample.getMetadata(t).get("longitude") + "," +
						bio1.axsSample.getMetadata(t).get("datetime").substring(0,10));
				}
			}else{
				sMerged = Joiner.on(";").join(map1.get(s));
				tim1=null;
				for(String t:map1.get(s)){
					axeLat=ncf1.axsLat.getAxisElementContaining(Double.parseDouble(bio1.axsSample.getMetadata(t).get("latitude")));
					axeLon=ncf1.axsLon.getAxisElementContaining(Double.parseDouble(bio1.axsSample.getMetadata(t).get("longitude")));
					if(tim1==null){
						tim1=new LocalDate(bio1.axsSample.getMetadata(t).get("datetime").substring(0,10));
					}
					lstOut.add(
							t + "," +
							sMerged + "," +
							0.5*(axeLat.rngAxisValues.lowerEndpoint() + axeLat.rngAxisValues.upperEndpoint()) + "," +
							0.5*(axeLon.rngAxisValues.lowerEndpoint() + axeLon.rngAxisValues.upperEndpoint()) + "," +
							tim1);
					
				}
			}
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
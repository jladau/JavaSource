package edu.ucsf.PowerAnalysisData;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.base.Strings;
import com.google.common.collect.Range;

import edu.ucsf.geospatial.SpatialAutoregressiveModel;
import edu.ucsf.geospatial.SpatialWeightsMatrix;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * This code generates data for a power analysis of spatial autocorrelation
 * @author jladau
 */

public class PowerAnalysisDataLauncher {

	/**
	 * Runs spatial autocorrelation analysis. Writes table with Moran's I and significance values for each OTU to specified output path. Can also optionally write a table of data that was analyzed and temporal and spatial distances between pairs of samples.
	 * @param rgsArgs Arguments. Arguments should be passed as --{argument name}={argument value}. Name-value pairs are:
	 * 				
	 * 				<p>
	 * 				<h4 class="list-heading">Required arguments</h4>
	 * 				<ul> 
	 * 				<li>sDataPath [string] = Absolute path to a file containing an OTU table. File should be in BIOM format (HDF5).
	 * 				<p>
	 * 				<li>sOutputPath [string] = Absolute path for output file. Should have a "csv" suffix.
	 * 				<p>
	 * 				<li>rgsDistanceNeighborhoods [list of strings] = List of distance neighborhoods. Minimum and maximum distances should be separated by dashes; for instance 1-10,10-100,100-1000. If locations are given by latitude and longitude, then distances should be in kilometers. If locations are given by arbitrary x,y coordinates, then the distance units should be those of the coordinate system.
	 *				</ul>
	 *
	 *				<p>				
	 *				<h4 class="list-heading">Optional arguments: autocorrelation</h4>
	 * 				<ul>				
	 *				<li>rgsDirectionNeighborhoods [list of strings] [OPTIONAL] = List of direction neighborhoods. Minimum and maximum directions should be separated by dashes; for instance 0-180,180-270,270-360.
	 *				<p>
	 *				<li>rgsTimeNeighborhoods [list of strings] [OPTIONAL] = List of time neighborhoods. Minimum and maximum time differences should be separated by dashes; for instance 1-2,2-5,5-12.
	 *				<p>
	 *				<li>sWeight [string]  [OPTIONAL] = Type of weighting to use in spatial weights matrix. Accepted values are "inverse" or "binary". Defaults to "binary".
	 *				</ul>
	 *
	 * 				<p>
	 *				<h4 class="list-heading">Optional arguments: data table</h4>
	 *				<ul>
	 *				<li>sTaxonRank [string] [OPTIONAL] = Taxonomic units on which to collapse table. Accepted values are "kingdom", "phylum", "class", "order", "family", "genus", "species", or "otu." The value of "otu" will cause table to not be modified.
	 *              <p>
	 *              <li>sSampleMetadataPath [string] [OPTIONAL] = Path to text file containing sample metadata formatted according to http://biom-format.org/documentation/adding_metadata.html. For use if BIOM file does not contain metadata. Must include "id", "datetime" and "latitude", "longitude" or "x","y" fields
	 *				<p>
	 *              <li>sSamplesToKeepPath [string] [OPTIONAL] = Path to file with list of samples to keep. File should contain a list of sample names.
	 *              <p>
	 *              <li>sObservationsToKeepPath [string] [OPTIONAL] = Path to file with list of observations to keep. File should contain a list of observation names.
	 *				<p>
	 *              <li>iRandomSampleSubsetSize [integer] [OPTIONAL] = Number of randomly chosen samples to use. Useful for analyzing large data tables quickly.
	 *              <p>
	 *              <li>iRandomObservationSubsetSize [integer] [OPTIONAL] = Number of randomly chosen observations to use. Useful for analyzing large data tables quickly. 
	 *              <p>
	 *              <li>bCheckRarefied [boolean] [OPTIONAL] = Flag for whether to check for rarefaction. If enabled and table is not rarefied, error will be thrown. Default is true.
	 *              <p>
	 *              <li>iPrevalenceMinimum [integer] [OPTIONAL] = Minimum prevalence: observations that occur in fewer samples will be omitted from analysis. Default is 10.
	 *              <p>
	 *              <li>iRarefactionTotal [integer] [OPTIONAL] = Total count to which to rarefy samples.
	 *				</ul>
	 **/
	
	public static void main(String rgsArgs[]){
		
		//arg1 = arguments
		//bio1 = biom object
		//spw1 = spatial weights matrix
		//sMode = latitude-longitude or euclidean
		//sar1 = spatial autoregressive model
		//lstSamples = ordered list of samples
		//lstOut = output
		
		String sMode;
		ArgumentIO arg1;
		BiomIO bio1;
		SpatialWeightsMatrix spw1 = null;
		SpatialAutoregressiveModel sar1;
		ArrayList<String> lstSamples = null;
		ArrayList<String> lstOut;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading defaults
		if(!arg1.containsArgument("bCheckRarefied")){
			arg1.updateArgument("bCheckRarefied", false);
		}
		if(!arg1.containsArgument("rgsDirectionNeighborhoods")){
			arg1.updateArgument("rgsDirectionNeighborhoods", (Strings.repeat("0-360,",arg1.getValueStringArray("rgsDistanceNeighborhoods").length-1) + "0-360").split(","));
		}
		if(!arg1.containsArgument("sWeight")){
			arg1.updateArgument("sWeight", "binary");
		}
		if(!arg1.containsArgument("rgsTimeNeighborhoods")){
			arg1.updateArgument("rgsTimeNeighborhoods", (Strings.repeat("0-12,",arg1.getValueStringArray("rgsDistanceNeighborhoods").length-1) + "0-12").split(","));
		}else{
			arg1.updateArgument("rgsRequiredSampleMetadata", "datetime");
		}
		
		//loading data
		System.out.println("Loading data...");
		try {
			bio1 = new BiomIO(arg1.getValueString("sDataPath"),arg1.getAllArguments());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//loading mode and filtering samples without metadata
		System.out.println("Filtering samples without location metadata...");
		if(bio1.axsSample.hasMetadataField("latitude") && bio1.axsSample.hasMetadataField("longitude")){
			sMode="latitude-longitude";
			try{
				bio1.filterByNoMetadata(new String[]{"latitude","longitude"}, bio1.axsSample);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(bio1.axsSample.hasMetadataField("x") && bio1.axsSample.hasMetadataField("y")){
			sMode="euclidean";
			try{
				bio1.filterByNoMetadata(new String[]{"x","y"}, bio1.axsSample);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{	
			System.out.println("Error: spatial metadata not found. Exiting.");
			return;
		}
		
		//looping through distances
		for(int k=0;k<arg1.getValueStringArray("rgsDistanceNeighborhoods").length;k++){
			
			System.out.println("Simulating data with autocorrelation at " + arg1.getValueStringArray("rgsDistanceNeighborhoods")[k] + " km...");
			
			//loading weights matrix
			try {
				spw1 = 	new SpatialWeightsMatrix(bio1,
						Range.closed(Double.parseDouble(arg1.getValueStringArray("rgsDistanceNeighborhoods")[k].split("-")[0]),Double.parseDouble(arg1.getValueStringArray("rgsDistanceNeighborhoods")[k].split("-")[1])),
						Range.closed(Double.parseDouble(arg1.getValueStringArray("rgsDirectionNeighborhoods")[k].split("-")[0]),Double.parseDouble(arg1.getValueStringArray("rgsDirectionNeighborhoods")[k].split("-")[1])),
						Range.closed(Double.parseDouble(arg1.getValueStringArray("rgsTimeNeighborhoods")[k].split("-")[0]),Double.parseDouble(arg1.getValueStringArray("rgsTimeNeighborhoods")[k].split("-")[1])),
						arg1.getValueString("sWeight"), 
						sMode,
						false);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			//loading sar model
			sar1 = new SpatialAutoregressiveModel(spw1,arg1.getValueDouble("dCorrelationCoefficient"),arg1.getValueDouble("dErrorVariance"));
			
			//loading output
			if(k==0){
				lstSamples = new ArrayList<String>(spw1.getVertexNames());
				lstOut = appendData(sar1,arg1.getValueInt("iIterations"),arg1.getValueStringArray("rgsDistanceNeighborhoods")[k],arg1.getValueStringArray("rgsDirectionNeighborhoods")[k],arg1.getValueStringArray("rgsTimeNeighborhoods")[k],lstSamples,true);
				DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
			}else{
				lstOut = appendData(sar1,arg1.getValueInt("iIterations"),arg1.getValueStringArray("rgsDistanceNeighborhoods")[k],arg1.getValueStringArray("rgsDirectionNeighborhoods")[k],arg1.getValueStringArray("rgsTimeNeighborhoods")[k],lstSamples,false);
				DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"),true);
			}
		}
		System.out.println("Done.");
	}
	
	private static ArrayList<String> appendData(SpatialAutoregressiveModel sar1, int iIterations, String sDistanceNeighborhood, String sDirectionNeighborhood, String sTimeNeighbohood, ArrayList<String> lstSamples, boolean bHeader){
		
		//lstOut = output
		//sbl1 = string builder
		//map1 = current variate
		
		ArrayList<String> lstOut;
		StringBuilder sbl1;
		HashMap<String,Double> map1;
		
		lstOut = new ArrayList<String>(iIterations+1);
		for(int i=0;i<iIterations;i++){
			map1 = sar1.generateRelativeAbundances();
			if(i==0 && bHeader){
				sbl1 = new StringBuilder();
				sbl1.append("#OTU");
				for(int j=0;j<lstSamples.size();j++){
					sbl1.append("\t" + lstSamples.get(j));
				}
				lstOut.add(sbl1.toString());
			}
			sbl1 = new StringBuilder();
			sbl1.append("Power." + i + "." + sDistanceNeighborhood + ";" + sDirectionNeighborhood + ";" + sTimeNeighbohood);
			for(int j=0;j<lstSamples.size();j++){
				sbl1.append("\t"+Math.exp(map1.get(lstSamples.get(j))));
			}
			lstOut.add(sbl1.toString());
		}
		return lstOut;
	}	
}

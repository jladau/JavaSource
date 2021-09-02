package edu.ucsf.DistanceDecay;

import java.util.ArrayList;
import edu.ucsf.base.MantelTest;
import edu.ucsf.geospatial.DistanceDecay;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * This class implements spatial autocorrelation analyses for OTU tables.
 * @author Joshua Ladau, jladau@gmail.com
 */
public class DistanceDecayLauncher {

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
	 *              <li>bNormalize [boolean] [OPTIONAL] = Flag for whether to normalize within each sample so that entries total to 1. Default is true.
	 *              <p>
	 *              <li>iPrevalenceMinimum [integer] [OPTIONAL] = Minimum prevalence: observations that occur in fewer samples will be omitted from analysis.
	 *              <p>
	 *              <li>bPresenceAbsence [boolean] [OPTIONAL] = Flag for whether data should be reduced to presence-absence data. Default is false.
	 *              <p>
	 *              <li>iRarefactionTotal [integer] [OPTIONAL] = Total count to which to rarefy samples.
	 *              <p>
	 *              <li>bOutputRelationship [boolean] [OPTIONAL] = Flag for whether to output relationship. Defaults to false.
	 *              <p>
	 *              <li>bOutputSignificance [boolean] [OPTIONAL] = Flag for whether to output significance value. Default to true. Observed correlation is also output.
	 *              <p>
	 *              <li>iMantelIterations [integer] [OPTIONAL] = Number of iterations for mantel test. Default is 1000.
	 *				</ul>
	 **/
	public static void main(String rgsArgs[]){
		
		//lstOut = output
		//arg1 = arguments
		//bio1 = biom object
		//ddc1 = distance decay object
		//mnt1 = mantel test object
		
		ArrayList<String> lstOut=null;
		ArgumentIO arg1;
		BiomIO bio1;
		DistanceDecay ddc1;
		MantelTest mnt1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		
		//loading defaults
		if(!arg1.containsArgument("bNormalize")){
			arg1.updateArgument("bNormalize", true);
		}
		if(!arg1.containsArgument("bPresenceAbsence")){
			arg1.updateArgument("bPresenceAbsence", false);
		}
		if(!arg1.containsArgument("bCheckRarefied")){
			arg1.updateArgument("bCheckRarefied", false);
		}
		if(!arg1.containsArgument("iMantelTestIterations")){
			arg1.updateArgument("iMantelTestIterations", 1000);
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
			try{
				bio1.filterByNoMetadata(new String[]{"latitude","longitude"}, bio1.axsSample);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(bio1.axsSample.hasMetadataField("x") && bio1.axsSample.hasMetadataField("y")){
			try{
				bio1.filterByNoMetadata(new String[]{"x","y"}, bio1.axsSample);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{	
			System.out.println("Error: spatial metadata not found. Exiting.");
			return;
		}
		
		//loading distance decay object
		ddc1 = new DistanceDecay(bio1);
		
		//outputting results
		if(arg1.containsArgument("bOutputRelationship") && arg1.getValueBoolean("bOutputRelationship")){
			lstOut = new ArrayList<String>(bio1.axsSample.size()*bio1.axsSample.size()/2);
			lstOut.add("SAMPLE1,SAMPLE2,DISTANCE,BRAYCURTIS");
			for(String[] rgs1:ddc1.getSamplePairs()){
				lstOut.add(rgs1[0] + "," + rgs1[1] + "," + ddc1.getDistance(rgs1[0],rgs1[1]) + "," + ddc1.getBrayCurtis(rgs1[0],rgs1[1]));
			}
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath").replace(".csv",".data.csv"));
		}
		
		//outputting significance value
		if(!arg1.containsArgument("bOutputSignificance") ^ arg1.containsArgument("bOutputSignificance")){
			lstOut = new ArrayList<String>();
			lstOut.add("STATISTIC,VALUE");
			mnt1 = ddc1.runMantelTest(arg1.getValueInt("iMantelTestIterations"));
			lstOut.add("r," + mnt1.getObservedCorrelation());
			lstOut.add("p," + mnt1.getPrLessThanObs());
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		}
		
		//terminating
		System.out.println("Done.");
	}
}

package edu.ucsf.BIOM.DistanceBetweenSamplePairs;

import java.util.ArrayList;
import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

public class DistanceBetweenSamplePairsLauncher {


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
	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//bio1 = biom object
		//dTime = start time
		//datSamples = pairs of samples
		//lstOut = output
		//sSample1 = current first sample
		//sSample2 = current second sample
		//rgd1 = latitude and longitude of first sample
		//rgd2 = latitude and longitude of second sample
		
		double rgd1[];
		double rgd2[];
		String sSample1;
		String sSample2;
		ArgumentIO arg1;
		BiomIO bio1;
		DataIO datSamples;
		ArrayList<String> lstOut;
		
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
		
		//loading otu table
		System.out.println("Loading data...");
		try {
			bio1 = new BiomIO(arg1.getValueString("sDataPath"),arg1.getAllArguments());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//loading graphs
		datSamples = new DataIO(arg1.getValueString("sSamplePairsPath"));
		
		//outputting results
		lstOut = new ArrayList<String>(datSamples.iRows);
		lstOut.add("SAMPLE_1,SAMPLE_2,DISTANCE_KM");
		for(int i=1;i<datSamples.iRows;i++){
			sSample1 = datSamples.getString(i, "SAMPLE_1");
			sSample2 = datSamples.getString(i, "SAMPLE_2");
			rgd1 = new double[2];
			rgd1[0]=Double.parseDouble(bio1.axsSample.getMetadata(sSample1).get("latitude"));
			rgd1[1]=Double.parseDouble(bio1.axsSample.getMetadata(sSample1).get("longitude"));
			rgd2 = new double[2];
			rgd2[0]=Double.parseDouble(bio1.axsSample.getMetadata(sSample2).get("latitude"));
			rgd2[1]=Double.parseDouble(bio1.axsSample.getMetadata(sSample2).get("longitude"));
			lstOut.add(sSample1 + "," + sSample2 + "," + EarthGeometry.orthodromicDistance(rgd1[0], rgd1[1], rgd2[0], rgd2[1]));
		}
		
		//terminating
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}

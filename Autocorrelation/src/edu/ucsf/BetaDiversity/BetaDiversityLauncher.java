package edu.ucsf.BetaDiversity;

import java.io.FileWriter;
import java.io.PrintWriter;
import edu.ucsf.base.BetaDiversityIterator;
import edu.ucsf.base.BetaDiversityIterator.BetaDiversity;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;

/**
 * This class implements spatial autocorrelation analyses for OTU tables.
 * @author Joshua Ladau, jladau@gmail.com
 */
public class BetaDiversityLauncher {

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
		//itr1 = beta-diversity iterator object
		//mnt1 = mantel test object
		//bet1 = beta-diversity value
		//prt1 = file writer
		//dTime = start time
		//datPairs = pairs of samples to consider (if provided)
		
		ArgumentIO arg1;
		BiomIO bio1;
		BetaDiversityIterator itr1;
		BetaDiversity bet1;
		PrintWriter prt1;
		double dTime;
		DataIO datPairs;
		
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
		
		//loading data
		System.out.println("Loading data...");
		try {
			bio1 = new BiomIO(arg1.getValueString("sBIOMPath"),arg1.getAllArguments());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//loading pairs if provided
		if(arg1.containsArgument("sPairsPath")){
			datPairs = new DataIO(arg1.getValueString("sPairsPath"));
		}else{
			datPairs = null;
		}
		
		//loading beta-diversity iterator object
		System.out.println("Calculating beta-diversity...");
		prt1 = new PrintWriter(new FileWriter(arg1.getValueString("sOutputPath"), false));
		prt1.println("SAMPLE_1,SAMPLE_2,BRAY_CURTIS,SHARED_RICHNESS,SAMPLE_1_EXCLUSIVE_RICHNESS,SAMPLE_2_EXCLUSIVE_RICHNESS,BETA_J,BETA_RICH,BETA_TURN,BETA_SIMPSON,WNODF");
		dTime = System.currentTimeMillis();
		if(datPairs==null){
		
			//outputting results
			itr1 = new BetaDiversityIterator(bio1);
			while(itr1.hasNext()){
				if(itr1.iCounter == 1 || (itr1.iCounter>0 && itr1.iCounter%100 == 0)){
					System.out.println("Analyzing sample pair " + itr1.iCounter + " of " + itr1.iTotalSamplePairs  + "...");
				}
				bet1 = itr1.next();	
			}
		}else{
			for(int i=1;i<datPairs.iRows;i++){
				itr1 = new BetaDiversityIterator(bio1,datPairs.getString(i, "SAMPLE_1"),datPairs.getString(i, "SAMPLE_2"));
				bet1 = itr1.new BetaDiversity(datPairs.getString(i, "SAMPLE_1"), datPairs.getString(i, "SAMPLE_2"));
				prt1.println(bet1.toString());
			}
		}
		System.out.println("Elapsed time calculating beta-diversity: " + (System.currentTimeMillis()-dTime)/1000. + " s");
		prt1.close();
	
		//terminating
		System.out.println("Done.");
	}
}

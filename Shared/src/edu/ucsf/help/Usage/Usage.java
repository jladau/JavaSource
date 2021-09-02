package edu.ucsf.help.Usage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;

/**
 * This code prints documentation on command line arguments.
 * @author jladau
 */

public class Usage {

	/**Arguments names to be output**/
	private String rgsArgNames[];
	
	/**Map from argument names to their documentation**/
	private HashMap<String,ArgumentDocumentation> mapDocumentation = null;
	
	/**Map from argument group names to list of arguments in that group**/
	private HashMultimap<String,String> mapGroups;
	
	/**
	 * Arguments for which usage documentation is to be printed.
	 * @param rgsArgNames Names of arguments to use.
	 */
	public Usage(String[] rgsArgNames){
		this.rgsArgNames = rgsArgNames;
		initialize();
		initializeGroups();
	}
	
	/**
	 * Prints usage for each variable if help flag is indicated
	 */
	public void printUsage(String rgsArgs[]) throws Exception{
		
		//set1 = set of argument names
		//rgs1 = set of argument names, sorted
		//b1 = flag for whether required or optional arguments are present
		
		boolean b1;
		HashSet<String> set1;
		String rgs1[];
		
		if(!rgsArgs[0].trim().equals("-h") && !rgsArgs[0].trim().equals("--help")){
			return;
		}
		set1 = new HashSet<String>();
		for(String s:rgsArgNames){
			if(mapDocumentation.containsKey(s)){
				set1.add(s);
			}else{
				if(mapGroups.containsKey(s)){
					for(String t:mapGroups.get(s)){
						if(mapDocumentation.containsKey(t)){
							set1.add(t);
						}else{
							throw new Exception("Documentation for " + t + " not found.");
						}
					}
				}else{
					throw new Exception("Documentation for " + s + " not found.");
				}
			}
		}
		rgs1 = set1.toArray(new String[set1.size()]);
		Arrays.sort(rgs1);
		
		b1 = false;
		for(String s:rgs1){
			if(mapDocumentation.get(s).bOptional==false){
				b1=true;
				break;
			}
		}
		
		if(b1){
			System.out.println("------------------");
			System.out.println("REQUIRED ARGUMENTS");
			System.out.println("------------------");
			System.out.println("");
			for(String s:rgs1){
				if(mapDocumentation.get(s).bOptional==false){
					System.out.println(mapDocumentation.get(s).toString());
					System.out.println("");
				}
			}
			System.out.println("");
		}
		
		b1 = false;
		for(String s:rgs1){
			if(mapDocumentation.get(s).bOptional==true){
				b1=true;
				break;
			}
		}
		
		if(b1){
			System.out.println("------------------");
			System.out.println("OPTIONAL ARGUMENTS");
			System.out.println("------------------");
			System.out.println("");
			for(String s:rgs1){
				if(mapDocumentation.get(s).bOptional==true){
					System.out.println(mapDocumentation.get(s).toString());
					System.out.println("");
				}
			}
		}
		
		System.exit(0);
	}
	
	private void initializeGroups(){
		mapGroups = HashMultimap.create();
		
		mapGroups.put("BiomIO", "sTaxonRank");
		mapGroups.put("BiomIO", "sSampleMetadataPath");
		mapGroups.put("BiomIO", "rgsSampleMetadataKeys");
		mapGroups.put("BiomIO", "sObservationMetadataPath");
		mapGroups.put("BiomIO", "rgsObservationMetadataKeys");
		mapGroups.put("BiomIO", "sSamplesToKeepPath");
		mapGroups.put("BiomIO", "sMergeSamplesPath");
		mapGroups.put("BiomIO", "iRarefactionTotal");
		mapGroups.put("BiomIO", "bCheckRarefied");
		mapGroups.put("BiomIO", "sObservationsToKeepPath");
		mapGroups.put("BiomIO", "rgsRequiredObservationMetadata");
		mapGroups.put("BiomIO", "rgsRequiredSampleMetadata");
		mapGroups.put("BiomIO", "iRandomSampleSubsetSize");
		mapGroups.put("BiomIO", "iRandomSubsetSeed");
		mapGroups.put("BiomIO", "bNormalize");
		mapGroups.put("BiomIO", "iPrevalenceMinimum");
		mapGroups.put("BiomIO", "iPrevalenceMaximum");
		mapGroups.put("BiomIO", "iRandomObservationSubsetSize");
		mapGroups.put("BiomIO", "bPresenceAbsence");
		mapGroups.put("BiomIO", "iBootstrapRandomSeed");
		mapGroups.put("BiomIO", "sBIOMPath");
		
		mapGroups.put("SpatialAutocorrelation", "iMCMCChains");
		mapGroups.put("SpatialAutocorrelation", "iMCMCIterations");
		mapGroups.put("SpatialAutocorrelation", "iInitialScreeningIterations");
		mapGroups.put("SpatialAutocorrelation", "rgsDistanceNeighborhoods");
		mapGroups.put("SpatialAutocorrelation", "rgsDirectionNeighborhoods");
		mapGroups.put("SpatialAutocorrelation", "rgsTimeNeighborhoods");
		mapGroups.put("SpatialAutocorrelation", "bOutputData");
		mapGroups.put("SpatialAutocorrelation", "bOutputDistances");
		mapGroups.put("SpatialAutocorrelation", "sAutocorrelationAnalysis");
		mapGroups.put("SpatialAutocorrelation", "sSpatialWeighting");
		
		mapGroups.put("ClusterIterator", "iTotalTasks");
		mapGroups.put("ClusterIterator", "iTaskID");
		
		mapGroups.put("TrainingData", "sRasterListPath");
		mapGroups.put("TrainingData", "sResponseVarsListPath");
		mapGroups.put("TrainingData", "sTrainingDatesPath");
		mapGroups.put("TrainingData", "dTrainingVert");
		mapGroups.put("TrainingData", "sResponseTransform");
		
		mapGroups.put("ProjectionData", "bProjectDifferences");
		mapGroups.put("ProjectionData", "sResponseDifferenceTransform");
		mapGroups.put("ProjectionData", "sRasterListPath");
		mapGroups.put("ProjectionData", "dProjectionVert");
		mapGroups.put("ProjectionData", "bMESS");
		
		mapGroups.put("AsciiToNetcdf","sNetCDFCellMethods");
		mapGroups.put("AsciiToNetcdf","sNetCDFLongName");
		mapGroups.put("AsciiToNetcdf","sNetCDFUnits");
		mapGroups.put("AsciiToNetcdf","sNetCDFVariable");
		mapGroups.put("AsciiToNetcdf","sNetCDFHistory");
		mapGroups.put("AsciiToNetcdf","sNetCDFSource");
		mapGroups.put("AsciiToNetcdf","sNetCDFReferences");
		mapGroups.put("AsciiToNetcdf","sNetCDFInstitution");
		mapGroups.put("AsciiToNetcdf","sNetCDFTitle");
		mapGroups.put("AsciiToNetcdf","sRasterDataPath");
		
		mapGroups.put("MetagenomeRarefier", "sBIOMPath");
		mapGroups.put("MetagenomeRarefier", "sProbabilityOfAssemblyFcnPath");
		mapGroups.put("MetagenomeRarefier", "iMetagenomeRarefactionDepth");
		mapGroups.put("MetagenomeRarefier", "iRandomSeed");
		mapGroups.put("MetagenomeRarefier", "sOutputMode");
		mapGroups.put("MetagenomeRarefier", "sOutputPath");
		mapGroups.put("MetagenomeRarefier", "sRarefactionCurveOutputPath");
		mapGroups.put("MetagenomeRarefier", "bIncludeEmptyMetagenomes");
	}
	
	private void put(String sName, String sType, String[] rgsPossibleValues, boolean bOptional, String sDefault, String sUsage){
		
		if(mapDocumentation==null){
			mapDocumentation = new HashMap<String,ArgumentDocumentation>();
		}
		
		mapDocumentation.put(sName, new ArgumentDocumentation(
				sName,
				sType,
				rgsPossibleValues,
				bOptional,
				sDefault,
				sUsage));
	}
	
	private void initialize(){

		put("",
				"",
				new String[]{""},
				false,
				"",
				"");
		
		put("sSpatialWeighting",
			"string",
			new String[]{"inverse", "binary"},
			true,
			"binary",
			"Type of weighting to use in spatial weights matrix.");
	
		put("sAutocorrelationAnalysis",
			"string",
			new String[]{"MoranScatter", "MoransI"},
			true,
			"MoransI",
			"Type of analysis to run. \"MoranScatter\" for Moran's scatter plots and \"MoransI\" for Moran's I and significance test.");
	
		put("bOutputDistances",
			"boolean",
			new String[]{"true", "false"},
			true,
			"false",
			"Flag for whether to output table time differences and geographic distances for samples. Defaults to false.");
	
		put("bOutputData",
			"boolean",
			new String[]{"true", "false"},
			true,
			"true",
			"Flag for whether to output table of data (after filtering for prevalence, etc) that was analyzed.");
		
		put("rgsTimeNeighborhoods",
			"string",
			new String[]{"comma-delimited list"},
			true,
			"na",
			"List of time neighborhoods. Minimum and maximum time differences should be separated by dashes; for instance 1-2,2-5,5-12.");
	
		put("rgsDirectionNeighborhoods",
			"string",
			new String[]{"comma-delimited list"},
			true,
			"na",
			"List of direction neighborhoods. Minimum and maximum directions should be separated by dashes; for instance 0-180,180-270,270-360.");
		
		put("rgsDistanceNeighborhoods",
			"string",
			new String[]{"comma-delimited list"},
			false,
			"na",
			"List of distance neighborhoods. Minimum and maximum distances should be separated by dashes; for instance 1-10,10-100,100-1000. If locations are given by latitude and longitude, then distances should be in kilometers. If locations are given by arbitrary x,y coordinates, then the distance units should be those of the coordinate system.");
		
		put("iInitialScreeningIterations",
			"integer",
			new String[]{"positive integer"},
			false,
			"na",
			"Number of iterations to run when p-values are initially screened for non-significance.");
	
		put("iMCMCIterations",
			"integer",
			new String[]{"positive integer"},
			false,
			"na",
			"Total number of MCMC iterations. Each chain will have iMCMCIterations/iMCMCChains iterations.");
	
		put("iMCMCChains",
			"integer",
			new String[]{"positive integer"},
			false,
			"na",
			"Number of independent MCMC chains to run for calculating p-values.");
		
		put("bMESS",
			"boolean",
			new String[]{"true", "false"},
			true,
			"false",
			"Flag for whether to project MESS maps rather than actual prediction maps.");
		
		put("sInputPath",
			"string",
			new String[]{"any string"},
			false,
			"na",
			"Path to input file.");
		
		put("bReplaceFirstOnly",
			"boolean",
			new String[]{"true","false"},
			true,
			"false",
			"Flag for whether to replace only first instances of strings, or all instances. Defaults to false; i.e., replace all instances.");
		
		put("sResponseDifferenceTransform",
			"string",
			new String[]{"percentdifference","difference","logoddsratio"},
			true,
			"na",
			"Transform for differenced response variables. Must be specified if \"bProjectDifferences\" is specified.");
		
		put("bProjectDifferences",
			"boolean",
			new String[]{"true","false"},
			true,
			"false",
			"Flag for whether to project to differences between samples and dates. If true, then sResponseDifferenceTransform must also be specified.");
	
		put("bApplyInverse",
			"boolean",
			new String[]{"true","false"},
			true,
			"false",
			"Applies inverse of response variable transformation to projections of species distribution models.");
		
		put("sNetCDFCellMethods",
			"string",
			new String[]{"any string"},
			false,
			"na",
			"See: http://cfconventions.org/cf-conventions/v1.6.0/cf-conventions.html#cell-methods");
	
		put("sNetCDFLongName",
			"string",
			new String[]{"any string"},
			false,
			"na",
			"Long name of variable in raster. See http://cfconventions.org/cf-conventions/v1.6.0/cf-conventions.html#long-name");
		
		put("sNetCDFUnits",
			"string",
			new String[]{"any string"},
			false,
			"na",
			"Units of variable in raster.");
		
		put("sNetCDFVariable",
			"string",
			new String[]{"any string"},
			false,
			"na",
			"Name of variable in raster.");
		
		put("sNetCDFHistory",
			"string",
			new String[]{"any string"},
			false,
			"na",
			"History: creation date of data and raster. Provides an audit trail for modifications to the original data.");
		
		put("sNetCDFSource",
			"string",
			new String[]{"any string"},
			false,
			"na",
			"Method of production of the original data.");
		
		put("sNetCDFReferences",
			"string",
			new String[]{"any string"},
			false,
			"na",
			"References that describe the data or methods used to produce it.");
		
		put("sNetCDFInstitution",
			"string",
			new String[]{"any string"},
			false,
			"na",
			"Where the original data was produced.");
		
		put("sNetCDFTitle",
			"string",
			new String[]{"any string"},
			false,
			"na",
			"Short description of the file contents.");
		
		put("sRasterDataPath",
			"string",
			new String[]{"path to csv file"},
			false,
			"na",
			"Path to file with information on ASCII rasters to be converted to NetCDF format. Fields are \"ASCII_RASTER_PATH,\" \"CLIMATOLOGY_START_DATE,\" and \"CLIMATOLOGY_END_DATE,\" which give the paths to the data for each range of dates.");
		
		put("sComparisonsPath",
			"string",
			new String[]{"path to csv file"},
			false,
			"na",
			"Path to file with nestedness comparisons to consider, output from Nestedness.ComparisonSelector.");
		
		put("sReplacementMapPath",
			"string",
			new String[]{"path to csv file"},
			false,
			"na",
			"Path to file with replacement strings. Headers in file are \"OLD_STRING\" and \"NEW_STRING.\"");
	
		put("sNewOldPredictorMapPath",
			"string",
			new String[]{"path to csv file"},
			false,
			"na",
			"Path to file mapping new predictor paths to old predictor names. Headers in file are \"NEW_PREDICTOR_NAME\" and \"OLD_PREDICTOR_NAME.\"");
		
		put("dProjectionVert",
			"double",
			new String[]{"any double"},
			true,
			"0",
			"Elevation to project to. Omit if all environmental rasters are not elevation-specific.");
		
		put("sSelectedModelsPath",
			"string",
			new String[]{"path to csv file"},
			false,
			"na",
			"Path output from ModelSelector to be projected. Should include header. First model will be projected unless iInputLine argument is specified.");
		
		put("iRandomSubsetSeed",
			"integer",
			new String[]{"any integer"},
			true,
			"na",
			"Random seed for taking random subset of samples or observations.");
			
		put("iRandomSampleSubsetSize",
			"integer",
			new String[]{"positive integer"},
			true,
			"na",
			"Number of randomly chosen samples to use. Useful for analyzing large data tables quickly.");
		
		put(
			"iPrevalenceMinimum",
			"integer",
			new String[]{"non-negative integer"},
			true,
			"inifinty",
			"Minimum prevalence: observations that occur in fewer samples will be omitted from analysis.");
		
		put(
			"bSimulate",
			"boolean",
			new String[]{"true", "false"},
			true,
			"false",
			"Flag for whether simulate to calculate p-values. Otherwise analytic results will be used.");
		
		put(
			"bOrderedNODF",
			"boolean",
			new String[]{"true", "false"},
			false,
			"na",
			"Flag for whether ordered (directional) NODF statistic should be calculated.");
		
		put(
			"bPresenceAbsence",
			"boolean",
			new String[]{"true", "false"},
			true,
			"false",
			"Flag for whether data should be reduced to presence-absence data.");
		
		put(
			"rgsRequiredSampleMetadata",
			"string",
			new String[]{"comma-delimited list"},
			true,
			"na",
			"Comma-delimited list of sample metadata keys. Samples lacking data for one or more of these keys will be omitted.");
		
		put(
			"rgsObservationMetadataKeys",
			"string",
			new String[]{"comma-delimited list"},
			true,
			"na",
			"Observations metadata keys to load from metadata file.");

		put(
			"sTaxonRank",
			"string",
			new String[]{"kingdom","phylum","class","order","family","genus","species","otu"},
			true,
			"otu",
			"Taxonomic units on which to collapse table. The value of \"otu\" will cause table to not be modified.");
	
		put(
			"rgsSampleMetadataKeys",
			"string",
			new String[]{"comma-delimited list"},
			true,
			"na",
			"Sample metadata keys to load from metadata file.");
		
		put(
			"rgsRequiredObservationMetadata",
			"string",
			new String[]{"comma-delimited list"},
			true,
			"na",
			"Comma-delimited list of observation metadata keys. Samples lacking data for one or more of these keys will be omitted.");
		
		put(
			"sObservationMetadataPath",
			"string",
			new String[]{"path to csv file"},
			true,
			"na",
			"Path to text file containing observation metadata formatted according to http://biom-format.org/documentation/adding_metadata.html. For use if BIOM file does not contain metadata. Must include \"id\" field giving observation IDs.");

		put(
			"iRandomObservationSubsetSize",
			"integer",
			new String[]{"positive integer"},
			true,
			"na",
			"Number of randomly chosen observations to use. Useful for analyzing large data tables quickly.");
		
		put(
			"sOutputPath",
			"string",
			new String[]{"any valid path"},
			false,
			"na",
			"Path for writing output.");
		
		put(
			"sBIOMPath",
			"string",
			new String[]{"path to a biom file"},
			false,
			"na",
			"Absolute path to a file containing an OTU table. File should be in BIOM format (HDF5). Should include 'latitude', 'longitude', 'datetime', and optionally 'vert' sample metadata fields.");
		
		put(
			"iRandomSeed",
			"integer",
			new String[]{"any integer"},
			false,
			"na",
			"Random seed for initializing random number generator.");
		
		put(
			"iNestednessPairs",
			"integer",
			new String[]{"any positive integer"},
			false,
			"na",
			"Number of pairs of samples or observations to consider for nestedness statistics.");
		
		put(
			"iNullModelIterations",
			"integer",
			new String[]{"any positive integer"},
			false,
			"na",
			"Number of iterations to use for simulating under null model.");

		put(
			"sMetadataField",
			"string",
			new String[]{"any string"},
			false,
			"na",
			"Metadata field to use for comparison between or within types for nestedness analysis.");

		put(
			"sNestednessNullModel",
			"string",
			new String[]{"fixedequiprobable", "equiprobablefixed", "fixedfixed"},
			false,
			"na",
			"Null model to use for nestedness null model: 'fixedequiprobable' and 'equiprobablefixed' respectively fix row and column sums, while 'fixedfixed' fixes both. All arrangements of occurrences with these constraints are assumed equally likely.");

		put(
			"sAxis",
			"string",
			new String[]{"sample", "observation"},
			false,
			"na",
			"Flag for whether to consider sample or observation axis in BIOM table.");
		
		put(
			"sNestednessAxis",
			"string",
			new String[]{"sample", "observation"},
			false,
			"na",
			"Flag for whether to compute nestedness between pairs of samples or observations.");

		put(
			"sComparisonMode",
			"string",
			new String[]{"overall", "bytypes", "betweeneachpairoftypes", "withineachtype"},
			false,
			"na",
			"Type of comparisons to select for nestedness analysis. Possible values are (i) 'overall', (ii) 'bytypes', (iii) 'betweeneachpairoftypes', and (iv) 'withineachtype' for comparisons (i) between all pairs of samples, (ii) both with and between types, (iii) between each pair of types, and (iv) within each pair of types.");
		
		put(
			"rgsBIOMPaths",
			"string",
			new String[]{"comma-delimited list"},
			true,
			"na",
			"Absolute path to a file containing an OTU tables. Files should be in BIOM format (HDF5). Should include 'latitude', 'longitude', 'datetime', and optionally 'vert' sample metadata fields.");

		put(
			"rgsSampleMetadataFields",
			"string",
			new String[]{"comma-delimited list"},
			false,
			"na",
			"List of metadata fields to output for sample classification in nestedness analysis.");

		put(
			"sRasterListPath",
			"string",
			new String[]{"path to a csv file"},
			false,
			"na",
			"Absolute path to file containing list of paths of rasters to consider. Rasters should be in NetCDF format. File should contain absolute paths, one path per line under the header \"RASTER_PATH\". The name of the variable to use in each raster can be specified after each path under the header \"VARIABLE.\" If no variables are to be specified (indicated by a value of \"null\"), the first variable in each raster will be used. After the variable to be used, the transform for the variable should be listed under the header \"TRANSFORM.\" Acceptable values are \"log10\", \"abs\", \"log10abs\" (for log10 of the absolute value), and \"identity.\" Paths can also be \"null\" for local variables stored in netcdf file. Optional column \"NESTING\" gives co-occurrence relationships of predictors, useful for quadratic regression for instance. If var_2 only is to be included if var_1 is included, then the respective codes would be 1-1 and 1-2. If var_3 is not independent, then it might be 2-1. Optional column \"EXCLUSIVE_SETS\" gives categories of predictors that cannot co-occur: for instance, if two predictors are labeled \"1\" then they will not both be included in models. Optional column \"RESPONSE_VARIABLES\" gives a semicolon delimited list of response variables to be associated with each predictor, for use if different response variables have different sets of candidate predictors. Optional column \"VIF_CLASS\" gives the VIF class of a predictor: the VIF of pairs of predictors within the same class will not be checked. Optional column \"TRAINING_DATES\" can give dates to use for each raster. However, if \"TRAINING_DATES\" and sTrainingDatesPath are specified, then an error will be thrown, because dates cannot simultaneously be specified for each raster and each sample. Optional column \"PROJECTION_DATES\" gives semicolon delimited list of projection dates. This must be accompanied by a column \"PROJECTION_DATE_ALIASES\" with lists of equal length giving the aliases (output names) for the dates. The aliasing is implemented because different rasters may have different projection dates, and it is necessary to specificy a single label to write to.");
		
		put(
			"sResponseVarsListPath",
			"string",
			new String[]{"path to csv file"},
			false,
			"na",
			"Path to file with list of names of variables to model. Specific observations can be listed, or use 'Richness', 'Shannon', and 'AllTaxa' to model richness, Shannon diversity, and all taxa in BIOM file, respectively");
		
		put(
			"iTotalTasks",
			"integer",
			new String[]{"any positive integer","-9999"},
			true,
			"-9999",
			"Total number of tasks for parallelizing. Omitting this argument or a value of \"-9999\" results in no parallelization.");
		
		put(
			"iTaskID",
			"integer",
			new String[]{"any positive integer","-9999"},
			true,
			"-9999",
			"Task to be run if parallelized. Omitting this argument results in no parallelization.");
		
		put(
			"sSampleMetadataPath",
			"string",
			new String[]{"path to csv file"},
			true,
			"na",
			"Path to text file containing sample metadata formatted according to http://biom-format.org/documentation/adding_metadata.html. For use if BIOM file does not contain metadata. Must include 'id', 'datetime' and 'latitude', 'longitude' or 'x','y' fields");
		
		put(
			"sSamplesToKeepPath",
			"string",
			new String[]{"path to text file"},
			true,
			"na",
			"Path to file with list of samples to keep. File should contain a list of sample names, one per line.");
	
		put(
			"sObservationsToKeepPath",
			"string",
			new String[]{"path to text file"},
			true,
			"na",
			"Path to file with list of observations to keep. File should contain a list of observation names.");

		put(
			"bCheckRarefied",
			"boolean",
			new String[]{"true", "false"},
			true,
			"true",
			"Flag for whether to check for rarefaction. If enabled and table is not rarefied, error will be thrown.");

		put(
			"iRarefactionTotal",
			"integer",
			new String[]{"any integer"},
			true,
			"na",
			"Total count to which to rarefy samples. Values of zero or less (except -9999 which is a flag for no rarefaction) result in rarefaction to the number of sequences in the sample with the fewest sequences.");

		put(
			"sTrainingDatesPath",
			"string",
			new String[]{"path to csv file"},
			true,
			"na",
			"File listing sample IDs and date (YYYY-MM-DD) to be used for loading training values. Headers should be \"SampleID, Date.\" If this argument is unspecified, dates of sample collection are used.  However, if \"TRAINING_DATES\" and sTrainingDatesPath are specified, then an error will be thrown, because dates cannot simulatanoeously be specified for each raster and each sample.");

		put(
			"dTrainingVert",
			"double",
			new String[]{"any double"},
			true,
			"0",
			"Elevation to be used for loading training values");

		put(
			"bNormalize",
			"boolean",
			new String[]{"true", "false"},
			true,
			"false",
			"Flag for whether to normalize within each sample so that entries total to 1.");

		put(
			"iMaxPredictors",
			"integer",
			new String[]{"positive integer"},
			true,
			"total number of predictors",
			"Maximum number of predictors to consider for models.");

		put(
			"sResponseTransform",
			"string",
			new String[]{"logit","identity","log","squareddeviation"},
			true,
			"varies by response variable",
			"Transform to use for response variable. Logit transformation is appropriate for response variables bounded by 0 and 1. Defaults to 'logit'. Richness and Shannon diversity response variables are always log transformed. Squared deviation is the deviation around the mean squared, otherwise untransformed.");

		put(
			"bOnlyBestOverallModel",
			"boolean",
			new String[]{"true", "false"},
			true,
			"true",
			"True if only output best overall model. False means best models for each number of predictors will be output.");

		put(
			"bPrintData",
			"boolean",
			new String[]{"true","false"},
			true,
			"false",
			"Flag for whether to output training data.");

		put(
			"iBootstrapRandomSeed",
			"integer",
			new String[]{"any integer"},
			true,
			"na",
			"Random seed for bootstrapping training data. Default is no bootstrapping.");

		put(
			"dMaxVIF",
			"double",
			new String[]{""},
			true,
			"5",
			"Maximum variance inflation factor to use for sets of predictors.");

		put(
			"sMergeSamplesPath",
			"string",
			new String[]{"path to csv file"},
			true,
			"na",
			"Path to file for merging samples with the following headers: sample_id_old, sample_id_new, latitude_new, longitude_new, datetime_new.");
	
		put(
			"iPrevalenceMaximum",
			"integer",
			new String[]{"positive integer"},
			true,
			"na",
			"Maximum prevalence: observations that occur in more samples will be omitted from analysis.");
		
		put(
			"iMetagenomeRarefactionDepth",
			"integer",
			new String[]{"positive integer"},
			false,
			"na",
			"Depth to which metagenomes should be rarefied");
		
		put(
			"sProbabilityOfAssemblyFcnPath",
			"string",
			new String[]{""},
			false,
			"na",
			"Path to file giving map from read depths to probability of assembly. Headers should be READ_DEPTH and PROBABILITY_OF_ASSEMBLY.");

		put(
			"sRarefactionCurveOutputPath",
			"string",
			new String[]{""},
			true,
			"na",
			"Path to file with rarefaction cuves. Data fields are METAGENOME_ID, NUMBER_OF_READS, NUMBER_OF_GENES, which are respectively the metagenome ID, number of reads (rarefied), and number of genes in the rarefied metagenome.");
	
		put(
			"sOutputMode",
			"string",
			new String[]{"subsample", "subsample_counts", "mean_depth", "probability_assembly", "rarefaction_curves"},
			false,
			"na",
			"Outputs read depths for one random subsample with depths, one random subsample with read counts, expected depths for a random subsample,  estimated probability of assembly for a subsample, or rarefaction curves, respectively");
	
		put(
			"bIncludeEmptyMetagenomes",
			"boolean",
			new String[]{"true","false"},
			true,
			"false",
			"Flag for whether to output rarefied metagenomes that have no classified reads. Empty metagenomes will be output if this argument is set to \"true\"; otherwise they will not be output.");
	}
	
	public class ArgumentDocumentation{
		
		/**Argument name**/
		public String sName;
		
		/**Argument type: string, int, double, etc**/
		public String sType;
		
		/**Possible values in string format**/
		public String[] rgsPossibleValues;
		
		/**Optional or required**/
		public boolean bOptional;
		
		/**Usage notes**/
		public String sUsage;
		
		/**Default value**/
		public String sDefault;
		
		public ArgumentDocumentation(String sName, String sType, String[] rgsPossibleValues, boolean bOptional, String sDefault, String sUsage){
			this.sName = sName;
			this.sType = sType;
			this.rgsPossibleValues = rgsPossibleValues;
			this.bOptional = bOptional;
			this.sUsage = sUsage;
			this.sDefault = sDefault;
		}
		
		public String toString(){
			
			//sbl1 = output
			
			StringBuilder sbl1;
			
			sbl1 = new StringBuilder();
			sbl1.append("--" + sName + ":\n");
			sbl1.append("\tUSAGE: " + sUsage + "\n");
			sbl1.append("\tTYPE: " + sType + "\n");
			sbl1.append("\tOPTIONAL: " + bOptional + "\n");
			sbl1.append("\tPOSSIBLE VALUES: " + Joiner.on(", ").join(rgsPossibleValues) + "\n");
			sbl1.append("\tDEFAULT VALUE: " + sDefault);
			return sbl1.toString();
		}
	}	
}

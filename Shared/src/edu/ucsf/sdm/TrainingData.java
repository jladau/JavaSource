package edu.ucsf.sdm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.joda.time.LocalDate;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import edu.ucsf.base.BinaryRelation;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfReader;

/**
 * Training data for SDM modeling object.
 * @author jladau
 */


public class TrainingData {

	/**Returns predictor variable value or response variable value for specified (sVariable,sSample)**/
	protected HashBasedTable<String,String,Double> tbl1;
	
	/**Set of predictors: map is from row of raster list file to predictor name**/
	protected HashMap<Integer, String> mapPredictorNames;
	
	/**Map from response vars to sets of predictors. For use when different response vars have different sets of candidate predictors**/
	protected HashMultimap<String,String> mapPredictors;
	
	/**Binary relation between predictors for nested models: if first element of relation occurs, then second must occur in model too. Useful for polynomial models where consecutive order terms are required.**/
	protected BinaryRelation<String> binNested;
	
	/**Binary relation giving pairs of predictors whose VIF should not be checked. Useful for fitting quadratic models and other models**/
	protected BinaryRelation<String> binDoNotCheckVIF;
	
	/**Set of sets of predictors that cannot co-occur**/
	protected HashSet<HashSet<String>> setExclusive;
	
	/**Set of response variables**/
	protected HashSet<String> setResponseVars;
	
	/**Returns the transformation for given response variable**/
	protected HashMap<String,String> mapTransform;
	
	/**Returns units for given response variable**/
	protected HashMap<String,String> mapUnits;
	
	public TrainingData(TrainingData trn1){
		tbl1 = trn1.tbl1;
		mapPredictorNames = trn1.mapPredictorNames;
		mapPredictors = trn1.mapPredictors;
		binNested = trn1.binNested;
		binDoNotCheckVIF = trn1.binDoNotCheckVIF;
		setExclusive = trn1.setExclusive;
		setResponseVars = trn1.setResponseVars;
		mapTransform = trn1.mapTransform;
		mapUnits = trn1.mapUnits;
	}
	
	public TrainingData(BiomIO bio1, DataIO datRasterPaths, DataIO datResponseVars, DataIO datTrainingDates, double dTrainingVert, String sResponseTransform) throws Exception{

		//mapDiversity = diversity values
		//cdf1 = netcdf reader
		//mapValues = map of values
		//sVar = current response variable name
		//d1 = current relative abundance
		//setSamplesToRemove = set of samples to remove due to lack of metadata
		//lst1 = list of all response variables
		//dat1 = complete list of response vars
		//dValue = current value
		//sPredictorTransform = current predictor transform
		//mapTrainingDates = for each sample ID, date to use for training
		//sPredictorVar = predictor variable
		//sPredictorPath = predictor path
		//sTrainingDate = current training date
		//mapNested = list of nestedness terms (if included)
		//mapVIF = classes within which VIF should not be checked
		//mapExclusive = for a given category, returns predictors that are exclusive (cannot be in the same model)
		//rgi1 = current and second nestedness indicator in double form
		//sUnits = units
		//sResponseTransform2 = updated response transform
		
		String sUnits;
		String sResponseTransform2;
		HashMap<String,String> mapNested = null;
		HashMultimap<String,String> mapExclusive = null;
		HashMultimap<String,String> mapVIF = null;
		String sPredictorVar;
		String sPredictorPath;
		String sPredictorTransform;
		String sTrainingDate;
		HashMap<String,LocalDate> mapTrainingDates;
		ArrayList<ArrayList<String>> lst1;
		NetcdfReader cdf1;
		HashMap<String,Double> mapValues = null;
		HashMap<String,Double> mapDiversity;
		String sVar;
		HashSet<String> setSamplesToRemove;
		DataIO dat1 = null;
		double dValue;
		int rgi1[][];
		
		//initializing
		tbl1 = HashBasedTable.create();
		mapPredictorNames = new HashMap<Integer,String>();
		setResponseVars = new HashSet<String>();
		mapTransform = new HashMap<String,String>();
		mapUnits = new HashMap<String,String>();
		
		//checking that both sample-specific and raster-specific dates are not provided
		if(datTrainingDates!=null && datRasterPaths.hasHeader("TRAINING_DATE")){
			throw new Exception("ERROR: Sample-specific and raster-specific dates cannot both be provided.");
		}
		
		//loading dates to use for samples
		if(datTrainingDates!=null){
			mapTrainingDates = new HashMap<String,LocalDate>();
			for(int i=1;i<datTrainingDates.iRows;i++){
				mapTrainingDates.put(datTrainingDates.getString(i, "SampleID"), new LocalDate(datTrainingDates.getString(i, "Date").substring(0, 10)));
			}
		}else{
			mapTrainingDates = null;
		}
		
		//initializing nestedness, if appropriate
		if(datRasterPaths.hasHeader("NESTING")){
			binNested = new BinaryRelation<String>();
			mapNested = new HashMap<String,String>();
		}else{
			binNested=null;
		}
		
		//initializing do not check vif list, if appropriate
		if(datRasterPaths.hasHeader("VIF_CLASS")){
			binDoNotCheckVIF=new BinaryRelation<String>();
			mapVIF = HashMultimap.create();
		}else{
			binDoNotCheckVIF=null;
		}
		
		//initializing exclusive sets, if appropriate
		if(datRasterPaths.hasHeader("EXCLUSIVE_SETS")){
			setExclusive = new HashSet<HashSet<String>>();
			mapExclusive = HashMultimap.create();
		}else{
			setExclusive=null;
		}
		
		//initializing map of predictors, if appropriate
		if(datRasterPaths.hasHeader("RESPONSE_VARIABLES")){
			mapPredictors = HashMultimap.create();
		}else{
			mapPredictors = null;
		}
		
		//loading predictors
		System.out.println("Loading predictor variables...");
		setSamplesToRemove=new HashSet<String>();
		for(int i=1;i<datRasterPaths.iRows;i++){
			
			//loading predictor transform
			sPredictorTransform = datRasterPaths.getString(i, "TRANSFORM");
				
			//loading values from raster
			if(!datRasterPaths.getString(i, "RASTER_PATH").equals("null")){
			
				//loading map of training dates
				if(datRasterPaths.hasHeader("TRAINING_DATE")){
					mapTrainingDates = new HashMap<String,LocalDate>();
					for(String s:bio1.axsSample.getIDs()){
						mapTrainingDates.put(s, new LocalDate(datRasterPaths.getString(i, "TRAINING_DATE")));
					}
					sTrainingDate=datRasterPaths.getString(i, "TRAINING_DATE");
				}else{
					sTrainingDate="sample_specific_date";
				}
				
				//loading raster of values
				if(datRasterPaths.getString(i, "VARIABLE").equals("null")){
					cdf1 = new NetcdfReader(datRasterPaths.getString(i, "RASTER_PATH"));
				}else{
					cdf1 = new NetcdfReader(datRasterPaths.getString(i, "RASTER_PATH"),datRasterPaths.getString(i, "VARIABLE"));
				}
				cdf1.sPath = datRasterPaths.getString(i, "RASTER_PATH");
				
				//loading values
				mapValues = cdf1.get(bio1,mapTrainingDates,dTrainingVert);
				sPredictorPath=cdf1.sPath;
				sPredictorVar=cdf1.gmt1.variable;
				
			//loading values from local metadata
			}else{
				
				cdf1=null;
				sPredictorPath="[local]";
				sPredictorVar=datRasterPaths.getString(i, "VARIABLE");
				mapValues = new HashMap<String,Double>(bio1.axsSample.size());
				for(int k=0;k<bio1.axsSample.size();k++){
					mapValues.put(bio1.axsSample.getID(k), Double.parseDouble(bio1.axsSample.getMetadata(k).get(sPredictorVar)));
				}
				sTrainingDate="sample_specific_date";
			}
			
			for(String s:mapValues.keySet()){
				
				//loading transformed value (if requested)
				try{
					dValue = PredictorTransform.apply(mapValues.get(s), sPredictorTransform);
				}catch(Exception e){
					dValue=Double.NaN;
				}
					
				//saving value
				tbl1.put(sPredictorPath + ":" + sPredictorVar + ":" + sPredictorTransform + ":" + sTrainingDate, s, dValue);
				if(Double.isNaN(dValue)){
					setSamplesToRemove.add(s);
				}
			}

			//saving predictor name
			mapPredictorNames.put(i,sPredictorPath + ":" + sPredictorVar + ":" + sPredictorTransform + ":" + sTrainingDate);
			
			//saving nestedness value if appropriate
			if(datRasterPaths.hasHeader("NESTING")){
				mapNested.put(sPredictorPath + ":" + sPredictorVar + ":" + sPredictorTransform + ":" + sTrainingDate, datRasterPaths.getString(i, "NESTING"));
			}
			
			//saving vif class, if appropriate
			if(datRasterPaths.hasHeader("VIF_CLASS")){
				mapVIF.put(datRasterPaths.getString(i, "VIF_CLASS"), sPredictorPath + ":" + sPredictorVar + ":" + sPredictorTransform + ":" + sTrainingDate);
			}
			
			//saving exclusive category, if appropriate
			if(datRasterPaths.hasHeader("EXCLUSIVE_SETS")){
				mapExclusive.put(datRasterPaths.getString(i, "EXCLUSIVE_SETS"), sPredictorPath + ":" + sPredictorVar + ":" + sPredictorTransform + ":" + sTrainingDate);
			}
			
			//saving to response-specific list of predictors, if appropriate
			if(datRasterPaths.hasHeader("RESPONSE_VARIABLES")){
				for(String s:datRasterPaths.getString(i, "RESPONSE_VARIABLES").split(":")){
					mapPredictors.put(s, sPredictorPath + ":" + sPredictorVar + ":" + sPredictorTransform + ":" + sTrainingDate);
				}
			}
			
			//closing reader
			if(cdf1!=null){
				cdf1.close();
			}
		}
		
		//loading nestedness binary relation, if appropriate
		if(mapNested!=null){
			rgi1 = new int[2][2];
			for(String s:mapNested.keySet()){
				rgi1[0][0]=Integer.parseInt(mapNested.get(s).split("-")[0]);
				rgi1[0][1]=Integer.parseInt(mapNested.get(s).split("-")[1]);
				for(String t:mapNested.keySet()){
					if(!t.equals(s)){
						rgi1[1][0]=Integer.parseInt(mapNested.get(t).split("-")[0]);
						rgi1[1][1]=Integer.parseInt(mapNested.get(t).split("-")[1]);
						if(rgi1[0][0]==rgi1[1][0]){
							if(rgi1[0][1]<rgi1[1][1]){
								binNested.addOrderedPair(t, s);
							}
						}
					}
				}
			}
		}
		
		//loading vif binary relation, if appropriate
		if(mapVIF!=null){
			for(String s:mapVIF.keySet()){
				for(String t:mapVIF.get(s)){
					for(String u:mapVIF.get(s)){
						if(!t.equals(u)){
							binDoNotCheckVIF.addOrderedPair(t, u);
						}
					}
				}
			}
		}
		
		//loading exclusive sets, if appropriate
		if(mapExclusive!=null){
			for(String s:mapExclusive.keySet()){
				setExclusive.add(new HashSet<String>(mapExclusive.get(s))); 
			}
		}
		
		//loading response variables
		if(datResponseVars!=null){
			System.out.println("Loading response variables...");
		
			//loading complete list of response vars
			lst1 = new ArrayList<ArrayList<String>>();
			for(int i=0;i<datResponseVars.iRows;i++){
				if(datResponseVars.getString(i, 0).equals("AllTaxa")){
					for(String s:bio1.axsObservation.getIDs()){
						lst1.add(new ArrayList<String>());
						lst1.get(lst1.size()-1).add(s);
					}
				}else{
					lst1.add(new ArrayList<String>());
					lst1.get(lst1.size()-1).add(datResponseVars.getString(i, 0));
				}
				dat1 = new DataIO(lst1);
			}
			
			for(int i=0;i<dat1.iRows;i++){
				
				//loading variable
				sVar = dat1.getString(i, 0);
				
				//loading diversity map
				if(sVar.equals("Richness")){
					mapDiversity = bio1.getRichness();
					sResponseTransform2 = "log10";
					sUnits = "taxon_count";
				}else if(sVar.equals("Shannon")){
					mapDiversity = bio1.getShannon();
					sResponseTransform2 = "log10";
					sUnits = "na";
				}else{
					mapDiversity = new HashMap<String,Double>(bio1.axsSample.getIDs().size());
					for(String s:bio1.axsSample.getIDs()){
						mapDiversity.put(s, bio1.getValueByIDs(dat1.getString(i, 0), s));
					}
					if(sResponseTransform==null || sResponseTransform.equals("logit")){
						sResponseTransform2="logit";
						sUnits="relative_abundance";
					}else if(sResponseTransform.equals("identity")){
						sResponseTransform2 = "identity";
						sUnits = "count";
					}else if(sResponseTransform.equals("log") || sResponseTransform.equals("log10")){
						sResponseTransform2 = "log10";
						sUnits="count";
					}else if(sResponseTransform.equals("squareddeviation")){
						sResponseTransform2 = "squareddeviation";
						sUnits="na";
					}else{
						throw new Exception("No response variable transform specified.");
					}
				}
				if(sResponseTransform!=null && sResponseTransform.equals("squareddeviation")){
					sResponseTransform2 = "squareddeviation";
					sUnits="na";
				}
				
				//saving data
				mapDiversity = ResponseTransform.apply(mapDiversity, sVar, sResponseTransform2);
				for(String s:mapDiversity.keySet()){
					tbl1.put(sVar, s, mapDiversity.get(s));
				}
				mapTransform.put(sVar, sResponseTransform2);
				mapUnits.put(sVar, sUnits);
				setResponseVars.add(sVar);
			}
		}
		
		//removing samples that lacked metadata
		for(String s:setSamplesToRemove){
			tbl1.column(s).clear();
		}
	}

	/**
	 * Returns list of samples that do not have NaN values for specified variable
	 * @param sVariable Variable to use for checking for NaN variables
	 * @return List of samples
	 */
	public HashSet<String> getNonNanSamples(String sVariable){
		
		//set1 = output
		
		HashSet<String> set1;
		
		set1 = new HashSet<String>(tbl1.columnKeySet().size());
		for(String s:tbl1.columnKeySet()){
			if(!Double.isNaN(tbl1.get(sVariable, s))){
				set1.add(s);
			}
		}
		return set1;
	}
	
	/**
	 * Gets predictors specific for given response variable.
	 * @param sResponseVar Response variable.
	 * @return Set of predictors
	 */
	public HashSet<String> getPredictors(String sResponseVar){
		if(mapPredictors!=null){
			return new HashSet<String>(mapPredictors.get(sResponseVar));
		}else{
			return new HashSet<String>(mapPredictorNames.values());
		}
	}
	
	/**
	 * Gets the name of the predictor associated with a particular input row
	 * @param iInputRow Row of raster list file with name of predictor
	 * @return Predictor name
	 */
	public String getPredictorName(int iInputRow){
		return mapPredictorNames.get(iInputRow);
	}
	
	public HashSet<HashSet<String>> getExclusivePredictors(String sResponseVar){
		
		//setOut = output
		//set1 = current set being added
		//set2 = set of predictors
		
		HashSet<HashSet<String>> setOut;
		HashSet<String> set1;
		HashSet<String> set2;
		
		if(mapPredictors==null){
			return setExclusive;
		}else{
			set2 = getPredictors(sResponseVar);
			setOut = new HashSet<HashSet<String>>();
			for(HashSet<String> set:setExclusive){
				set1 = new HashSet<String>();
				for(String s:set){
					if(set2.contains(s)){
						set1.add(s);
					}
				}
				if(set1.size()>0){
					setOut.add(set1);
				}
			}
			return setOut;
		}
	}
	
	public String getResponseTransform(String sVar){
		return mapTransform.get(sVar);
	}
	
	public String getUnits(String sVar){
		return mapUnits.get(sVar);
	}
	
	public HashSet<String> getResponseVars(){
		return setResponseVars;
	}
	
	public BinaryRelation<String> getNestedPredictors(){
		return binNested;
	}
	
	public BinaryRelation<String> getDoNotCheckVIF(){
		return binDoNotCheckVIF;
	}
	
	/**
	 * Gets data table
	 */
	public HashBasedTable<String,String,Double> getDataTable(){
		return tbl1;
	}
	
	/**
	 * Gets set of all loaded predictors
	 */
	public HashSet<String> getPredictors(){
		return new HashSet<String>(mapPredictorNames.values());
	}
}

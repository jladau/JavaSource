package edu.ucsf.sdm;

import java.util.HashMap;
import java.util.TreeMap;
import org.joda.time.LocalDate;
import edu.ucsf.base.LinearModel;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.io.BiomIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfReader;
import edu.ucsf.sdm.Projector;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Projects model into geographic space
 * @author jladau
 */

public class ProjectorTest{

	/**Projector**/
	private Projector prj1;
	
	/**Projection data**/
	private ProjectionData prd1;
	
	public ProjectorTest() throws Exception{
		
		//trn1 = training data	
		//lnm1 = linear model object
		//mapPredictors = map from predictor names to aliases
		//mapRasters = map from raster names to cdf objects
		//prd1 = projection data
		//rgsDates = list of dates
		
		String rgsDates[][];
		HashMap<String,String> mapPredictors;
		TrainingData trn1 = null;
		LinearModel lnm1;
		TreeMap<String,NetcdfReader> mapRasters;
		
		rgsDates = new String[17][2];
		rgsDates[0] = new String[]{"SampleID","Date"};
		for(int i=1;i<17;i++){
			rgsDates[i] = new String[]{"sample" + (i) ,  "9999-09-15"};
		}
		
		trn1 = new TrainingData(
				new BiomIO("/home/jladau/Desktop/Data/Microbial_Community_Samples/ValidationData.NA.NA.Ladau.biom"),
				new DataIO(new String[][]{
					{"RASTER_PATH","VARIABLE","TRANSFORM","TRAINING_DATE"},
					{"/home/jladau/Desktop/Data/Rasters/Environmental_Variables/longradiationMomeanNASA.nc","null","identity","9999-09-15"}}),
				new DataIO(new String[][]{{"otu1"},{"otu2"}}),
				null,
				//new DataIO(rgsDates),
				0.,
				"logit");
		
		//loading and fitting model
		mapPredictors = new HashMap<String,String>();
		mapPredictors.put("/home/jladau/Desktop/Data/Rasters/Environmental_Variables/longradiationMomeanNASA.nc:outgoing_longwave_radiation:identity:9999-09-15",
				"/home/jladau/Desktop/Data/Rasters/Environmental_Variables/longradiationMomeanNASA.nc:outgoing_longwave_radiation:identity:9999-09-15");
		lnm1 = new LinearModel(trn1.getDataTable(),"otu1",mapPredictors.keySet());
		lnm1.fitModel(mapPredictors.keySet());
		
		//loading projection data
		prd1 = new ProjectionData(trn1,
					new DataIO(new String[][]{
						{"RASTER","VARIABLE","TRANSFORM","PROJECTION_DATE"},
						{"/home/jladau/Desktop/Data/Rasters/Environmental_Variables/longradiationMomeanNASA.nc","null","identity","9999-09-15"}}),
					0);
		
		//projecting model
		mapRasters = new TreeMap<String,NetcdfReader>();
		for(String s:mapPredictors.keySet()){
			mapRasters.put(s, new NetcdfReader(s.split(":")[0],s.split(":")[1]));
		}	
		prj1 = new Projector("otu1","NA","otu 1","cell methods 1",null,lnm1,prd1,"identity",true);
	}

	@Test
	public void loadGrid_GridLoaded_PredictionsCorrect() throws Exception{
		
		//itr1 = raster iterator
		//cdf1 = reader
		
		GeospatialRaster.LatLonIterator itr1;
		GeospatialRasterCell cel1;
		NetcdfReader cdf1;
		
		
		prj1.addTime(new LocalDate(9999,10,15),new LocalDate(9999,10,15));
		prj1.addVert(0.,0.);
		prj1.loadGrid("9999-10-15", 0.);

		cdf1 = new NetcdfReader("/home/jladau/Desktop/Data/Rasters/Environmental_Variables/longradiationMomeanNASA.nc");
		cdf1.loadGrid(new LocalDate(9999,10,15), 0.);
		itr1 = cdf1.getLatLonIterator(new LocalDate(9999,10,15), 0.);
		while(itr1.hasNext()){
			cel1 = itr1.next();
			assertEquals(prj1.get(cel1),(40.9709283028579-0.16647059459252*cdf1.readValue(cel1)),0.00000001);
		}
		cdf1.close();
		prd1.close();
	}
}

package edu.ucsf.sdm;

import org.joda.time.LocalDate;
import edu.ucsf.base.LinearModel;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.sdm.ProjectionData.ProjectionDatum;

/**
 * Projects model into geographic space
 * @author jladau
 */

public class Projector extends GeospatialRaster{

	/**Projection data**/
	private ProjectionData prd1;
	
	/**Model object**/
	private LinearModel lnm1;
	
	/**Response transform**/
	private String sResponseTransform;
	
	/**Flag for whether to apply inverse**/
	private boolean bApplyInverse;
	
	public Projector(
			String variable, 
			String units, 
			String long_name, 
			String cell_methods, 
			SphericalMultiPolygon plyMask, 
			LinearModel lnm1, 
			ProjectionData prd1, 
			String sResponseTransform, 
			boolean bApplyInverse) 
					throws Exception{
		super(
				prd1.getRasterArray()[0].dLatResolution, 
				prd1.getRasterArray()[0].dLonResolution, 
				prd1.getRasterArray()[0].getLatRange(),
				prd1.getRasterArray()[0].getLonRange(), 
				plyMask,
				prd1.getRasterArray()[0].gmt1);
		gmt1.cell_methods = cell_methods;
		gmt1.units = units;
		gmt1.long_name = long_name;
		gmt1.variable = variable;
		this.lnm1 = lnm1;
		this.prd1 = prd1;
		this.sResponseTransform = sResponseTransform;
		this.bApplyInverse = bApplyInverse;
	}

	//TODO write unit test
	public double readPrediction(double dLat, double dLon, String sDateAlias, double dVert) throws Exception{ 
		return readPrediction(prd1.getProjectionData(prd1.new ProjectionPoint(dLat, dLon, sDateAlias, dVert, "na")));
	}
	
	//TODO write unit test
	public double readPrediction(ProjectionDatum pdm1) throws Exception{

		if(bApplyInverse){	
			return ResponseTransform.applyInverse(
					lnm1.findPrediction(pdm1), 
					gmt1.variable, 
					sResponseTransform);
		}else{
			return lnm1.findPrediction(pdm1);
		}
	}
	
	//TODO check unit test
	public void loadGrid(String sDateAlias, double dVert) throws Exception{
		
		//itr1 = iterator
		//cel1 = current cell
		//tim1 = output time
		
		LocalDate tim1;
		GeospatialRasterCell cel1;
		LatLonIterator itr1;
		
		tim1 = new LocalDate(sDateAlias);
		itr1 = this.getLatLonIterator(tim1, dVert);
		while(itr1.hasNext()){
			cel1 = itr1.next();
			try{
				this.put(cel1, lnm1.findPrediction(prd1.getProjectionData(prd1.new ProjectionPoint(cel1.axeLat.ID,cel1.axeLon.ID, sDateAlias, dVert, "na"))));
			}catch(Exception e){
			}
		}
	}
}
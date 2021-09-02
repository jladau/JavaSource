package edu.ucsf.geospatial;

import org.joda.time.LocalDate;

import edu.ucsf.geospatial.GeospatialRaster.AxisElement;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;

/**
 * Contains utilities for summarizing geosptial rasters
 * @author jladau
 */

public class GeospatialRasterOperations {

	
	/**
	 * Calculates the difference between two rasters. 
	 * @param ras1 First raster.
	 * @param ras2 Second raster.
	 * @return Difference: ras1-ras2.
	 */
	//TODO write unit test
	public static GeospatialRaster difference(GeospatialRaster ras1, GeospatialRaster ras2, GeospatialRasterMetadata gmtOutput) throws Exception{
		
		//ras3 = output
		//itr1 = iterator
		//cel1 = current cell
		
		GeospatialRaster ras3;
		GeospatialRaster.LatLonIterator itr1;
		GeospatialRasterCell cel1;
		
		if(!ras1.axsTime.equals(ras2.axsTime)){
			throw new Exception("Rasters have different time dimensions.");
		}
		if(!ras1.axsVert.equals(ras2.axsVert)){
			throw new Exception("Rasters have different vert dimensions.");
		}
		ras3 = new GeospatialRaster(ras1.dLatResolution, ras1.dLonResolution, ras1.getLatRange(), ras1.getLonRange(), gmtOutput);
		for(AxisElement<LocalDate> axeTime:ras1.axsTime.getAxisElements()){
			for(AxisElement<Double> axeVert:ras1.axsVert.getAxisElements()){
				itr1 = ras1.getLatLonIterator(axeTime.ID, axeVert.ID);
				ras3.addVert(axeVert.rngAxisValues);
				ras3.addTime(axeTime.rngAxisValues);
				while(itr1.hasNext()){
					cel1 = itr1.next();
					ras3.put(cel1, ras1.get(cel1)-ras2.get(cel1));
				}
			}
		}
		return ras3;
	}
	
	
	/**
	 * Finds the geographic area above a specified threshold.
	 * @param ras1 Raster to consider.
	 * @param dThreshold Threshold.
	 * @return Area above threshold.
	 */
	public static double calculateAreaAboveThreshold(GeospatialRaster ras1, LocalDate tim1, double dVert, double dThreshold){
		
		//itr1 = iterator
		//cel1 = current cell
		//dOut = output
		//dValue = current value
		
		GeospatialRaster.LatLonIterator itr1=null;
		GeospatialRasterCell cel1;
		double dOut;
		double dValue;
		
		dOut=0.;
		try {
			itr1 = ras1.getLatLonIterator(tim1, dVert);
		} catch (Exception e) {
			e.printStackTrace();
		}
		while(itr1.hasNext()){
			cel1 = itr1.next();
			dValue = ras1.get(cel1);
			if(!Double.isNaN(dValue)){
				if(dValue>dThreshold){
					dOut+=EarthGeometry.findAreaCell(
							cel1.axeLat.ID, 
							cel1.axeLon.ID, 
							cel1.axeLon.rngAxisValues.upperEndpoint()- cel1.axeLon.rngAxisValues.lowerEndpoint(), 
							cel1.axeLat.rngAxisValues.upperEndpoint()- cel1.axeLat.rngAxisValues.lowerEndpoint());
				}
			}
		}
		return dOut;
	}
	
	/**
	 * Finds the geographic area with non-error values.
	 * @param ras1 Raster to consider.
	 * @return Area with non-error values.
	 */
	public static double calculateNonErrorArea(GeospatialRaster ras1, LocalDate tim1, double dVert){
		
		//itr1 = iterator
		//cel1 = current cell
		//dOut = output
		
		GeospatialRaster.LatLonIterator itr1 = null;
		GeospatialRasterCell cel1;
		double dOut;
		
		dOut=0.;
		try {
			itr1 = ras1.new LatLonIterator(tim1, dVert);
		} catch (Exception e) {
			e.printStackTrace();
		}
		while(itr1.hasNext()){
			cel1 = itr1.next();
			if(!Double.isNaN(ras1.get(cel1))){
				dOut+=EarthGeometry.findAreaCell(
						cel1.axeLat.ID, 
						cel1.axeLon.ID, 
						cel1.axeLon.rngAxisValues.upperEndpoint()- cel1.axeLon.rngAxisValues.lowerEndpoint(), 
						cel1.axeLat.rngAxisValues.upperEndpoint()- cel1.axeLat.rngAxisValues.lowerEndpoint());
			}
		}
		return dOut;
	}
}

package edu.ucsf.geospatial;

import org.joda.time.LocalDate;

import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;

/**
 * Masks a geospatial raster
 * @author jladau
 *
 */

public class MaskGeospatialRaster {

	/**
	 * Applies mask
	 * @param plyMask Polygon to use as mask. Values outside of mask will be omitted.
	 * @param ras1 Raster to be masked.
	 */
	public static void applyMask(SphericalMultiPolygon plyMask, GeospatialRaster ras1, LocalDate tim1, double dVert) throws Exception{
		
		//itr1 = raster iterator
		//cel1 = raster cell
		//dLatR = latitude resolution
		//dLonR = longitude resolution
		//rgdLat = array of latitude offsets
		//rgdLon = array of longitude offsets
		//bInclude = flag for whether to exclude
		
		GeospatialRaster.LatLonIterator itr1;
		GeospatialRasterCell cel1;
		double dLatR; double dLonR;
		double rgdLat[];
		double rgdLon[];
		boolean bInclude;
		
		if(plyMask==null){
			return;
		}
		
		dLatR=ras1.dLatResolution/2.;
		dLonR=ras1.dLonResolution/2.;
		rgdLat = new double[]{0., dLatR, -dLatR};
		rgdLon = new double[]{0., dLonR, -dLonR};
		
		itr1 = ras1.new LatLonIterator(tim1, dVert);
		while(itr1.hasNext()){
			cel1 = itr1.next();
			bInclude = false;
			for(double dLat:rgdLat){
				for(double dLon:rgdLon){
					if(plyMask.contains(cel1.axeLat.ID+dLat, cel1.axeLon.ID+dLon)){
						bInclude=true;
						break;
					}
				}
				if(bInclude){
					break;
				}
			}
			if(bInclude==false){					
				itr1.remove();
			}
		}
	}
}
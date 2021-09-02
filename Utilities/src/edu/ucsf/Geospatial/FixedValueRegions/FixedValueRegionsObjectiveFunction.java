package edu.ucsf.Geospatial.FixedValueRegions;

import java.util.Collection;
import java.util.HashMap;
import edu.ucsf.base.HashMap_AdditiveDouble;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;

public class FixedValueRegionsObjectiveFunction {

	/**Map containing rasters**/
	private HashMap<String,FixedValueRegionsRaster> mapRasters;
	
	/**Map of previous sums**/
	private HashMap_AdditiveDouble<String> mapSumPrevious;
	
	/**Map of previous areas**/
	private HashMap_AdditiveDouble<String> mapAreaPrevious;
	
	/**Map of current values**/
	private HashMap<String,Double> mapValues;
	
	public FixedValueRegionsObjectiveFunction(HashMap<String,FixedValueRegionsRaster> mapRasters){
		this.mapRasters = mapRasters;
	}
	
	public double evaluatePotentialFunction(GeospatialRasterCell cel1){
		
		//d3 = value in current cell
		//mapArea = copy of previous areas map
		//mapSum = copy of previous sums map
		//d2 = output
		//dArea = area of cell
		
		HashMap_AdditiveDouble<String> mapSum;
		HashMap_AdditiveDouble<String> mapArea;
		double d2;
		double dArea;
		double d3;
		
		mapSum = this.mapSumPrevious;
		mapArea = this.mapAreaPrevious;
		dArea = cel1.area();
		
		mapSum = new HashMap_AdditiveDouble<String>(mapRasters.size());
		mapArea = new HashMap_AdditiveDouble<String>(mapRasters.size());
		for(String s:mapRasters.keySet()){
			mapSum.put(s, mapSumPrevious.get(s));
			mapArea.put(s, mapAreaPrevious.get(s));
		}
		
		dArea = cel1.area();
		for(String s:mapRasters.keySet()){
			d3 = mapRasters.get(s).getStandardizedValue(cel1);
			if(!Double.isNaN(d3)){
				if(mapRasters.get(s).sOperation.equals("total")){
					mapSum.putSum(s, d3);
				}else if(mapRasters.get(s).sOperation.equals("mean")){
					mapSum.putSum(s, d3*dArea);
				}
				mapArea.putSum(s, dArea);
			}
		}

		//updating to means where necessary
		for(String s:mapRasters.keySet()){
			if(mapRasters.get(s).sOperation.equals("mean")){
				mapSum.put(s, mapSum.get(s)/mapArea.get(s));
			}
		}
		
		//returning results
		d2 = 0;
		for(String s:mapSum.keySet()){
			d2+=(mapSum.get(s)-mapRasters.get(s).targetValueStandardized())*(mapSum.get(s)-mapRasters.get(s).targetValueStandardized());
		}
		return d2;
	}
	
	public double evaluateFunction(FixedValueRegion rgn1){
		
		//mapSum = map from variable names to sum of values across raster cells
		//mapArea = map from variable names to total area with non-error values 
		//d2 = output
		//dArea = current cell area
		//d3 = current cell value
		//col1 = cells
		
		HashMap_AdditiveDouble<String> mapSum;
		HashMap_AdditiveDouble<String> mapArea;
		double d2;
		double dArea;
		double d3;
		Collection<GeospatialRasterCell> col1;
		
		//loading sums of values across raster cells
		mapSum = new HashMap_AdditiveDouble<String>(mapRasters.size());
		mapArea = new HashMap_AdditiveDouble<String>(mapRasters.size());
		col1 = rgn1.getCells();
		for(GeospatialRasterCell cel1:col1){
			dArea = cel1.area();
			for(String s:mapRasters.keySet()){
				d3 = mapRasters.get(s).getStandardizedValue(cel1);
				if(!Double.isNaN(d3)){
					if(mapRasters.get(s).sOperation.equals("total")){
						mapSum.putSum(s, d3);
					}else if(mapRasters.get(s).sOperation.equals("mean")){
						mapSum.putSum(s, d3*dArea);
					}
					mapArea.putSum(s, dArea);
				}
			}
		}
		
		//saving results
		mapSumPrevious = new HashMap_AdditiveDouble<String>(mapRasters.size());
		mapAreaPrevious = new HashMap_AdditiveDouble<String>(mapRasters.size());
		for(String s:mapRasters.keySet()){
			mapSumPrevious.put(s, mapSum.get(s));
			mapAreaPrevious.put(s, mapArea.get(s));
		}
		
		//updating to means where necessary
		for(String s:mapRasters.keySet()){
			if(mapRasters.get(s).sOperation.equals("mean")){
				mapSum.put(s, mapSum.get(s)/mapArea.get(s));
			}
		}
		
		
		//returning results
		d2 = 0;
		for(String s:mapSum.keySet()){
			d2+=(mapSum.get(s)-mapRasters.get(s).targetValueStandardized())*(mapSum.get(s)-mapRasters.get(s).targetValueStandardized());
		}
		mapValues = new HashMap<String,Double>(mapSum.size());
		for(String s: mapSum.keySet()){
			mapValues.put(s,mapRasters.get(s).unstandardize(mapSum.get(s)));
		}
		return d2;
	}
	
	public HashMap<String,Double> getValues(){
		return mapValues;
	}
}

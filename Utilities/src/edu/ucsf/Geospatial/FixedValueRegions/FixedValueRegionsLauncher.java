package edu.ucsf.Geospatial.FixedValueRegions;

import java.util.ArrayList;
import java.util.HashMap;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.geospatial.WktIO;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfReader;
import edu.ucsf.io.ShapefileIO;

/**
 * Finds regions with fixed values (totals or averages for variables)
 * @author jladau
 *
 */

public class FixedValueRegionsLauncher {

	//TODO make maximum number of iterations a variable (currently set at 25)
	
	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//shp1 = shapefile io object
		//ply1 = masking polygon
		//dat1 = data with raster paths, variables, and operations (mean or total)
		//map1 = map from variable names to netcdf objects
		//ras1 = fixed value regions raster object
		//lstOut = output
		//lstRegions = list of regions
		//bNewRegion = flag for whether to find new region
		//i1 = counter
		
		ShapefileIO shp1;
		SphericalMultiPolygon ply1;
		ArgumentIO arg1;
		HashMap<String,FixedValueRegionsRaster> map1;
		DataIO dat1;
		FixedValueRegionsRaster ras1 = null;
		FixedValueRegion rgn1;
		ArrayList<String> lstOut;
		ArrayList<FixedValueRegion> lstRegions;
		boolean bNewRegion;
		int i1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		shp1 = new ShapefileIO(arg1.getValueString("sShapefilePath"), arg1.getValueString("sIDHeader"));
		if(shp1.loadFeature(arg1.getValueString("sPolygonID"))==1){
			throw new Exception("ERROR: Polygon not found.");
		}
		ply1 = shp1.getPolygon();
		dat1 = new DataIO(arg1.getValueString("sRasterInformationPath"));
		map1 = new HashMap<String,FixedValueRegionsRaster>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			ras1 = new FixedValueRegionsRaster(
					new NetcdfReader(dat1.getString(i, "RASTER_PATH"), dat1.getString(i, "VARIABLE_NAME")),
					dat1.getString(i, "VARIABLE_NAME"),
					null,
					dat1.getString(i, "OPERATION"),
					dat1.getDouble(i, "TARGET_VALUE"));
			if(ras1.cdf1.hasTime()){
				ras1.updateTime(dat1.getTime(i, "DATE"));
			}
			ras1.cdf1.loadGrid(ras1.tim1, GeospatialRaster.NULL_VERT);
			ras1.loadMeanStdev(ply1, arg1.getValueInt("iRandomSeed"));
			map1.put(ras1.sVar,ras1);
		}
		lstOut = new ArrayList<String>(1000);
		
		lstRegions = new ArrayList<FixedValueRegion>(arg1.getValueInt("iRegions"));
		for(int i=1;i<=arg1.getValueInt("iRegions");i++){
			
			System.out.println("Finding region " + i + "...");
			
			i1 = 0;
			do{	
				rgn1 = new FixedValueRegion(
						arg1.getValueDouble("dMinimumArea"), 
						arg1.getValueDouble("dMaximumArea"), 
						1000, 
						ply1, 
						map1,
						Integer.toString(i));
				rgn1.load(0.1, arg1.getValueString("sMethod"), arg1.getValueTime("timDate"));
				bNewRegion=false;
				for(int k=0;k<lstRegions.size();k++){
					if(rgn1.intersects(lstRegions.get(k))){
						bNewRegion=true;
						break;
					}
				}
				i1++;
			}while(bNewRegion==true && i1<=25);
			if(i1>25){
				break;
			}
			lstRegions.add(rgn1);
			lstOut.add(WktIO.toWKT(rgn1.toPolygon(), Integer.toString(i)));
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
}
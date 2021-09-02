package edu.ucsf.Geospatial.SphericalCapsMeanRaster;

import static edu.ucsf.geospatial.GeospatialRaster.NULL_TIME;
import static edu.ucsf.geospatial.GeospatialRaster.NULL_VERT;
import java.util.ArrayList;
import com.google.common.collect.Range;
import edu.ucsf.base.ExtendedMath;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRasterMetadata;
import edu.ucsf.geospatial.SphericalCapEarth;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfWriter;

/**
 * Creates a raster with the mean values across spherical caps and a raster with the cap counts
 * @author jladau
 */

public class SphericalCapsMeanRasterLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//ncw1 = netcdf writer
		//arg1 = arguments
		//dat1 = data
		//rasValues = raster of mean values
		//rasCounts = raster of counts
		//lstCap = list of caps
		//lstValue = list of values
		//itr1 = raster iterator
		//cel1 = current cell
		//lst1 = list of current values
		//i1 = counter
		
		int i1;
		NetcdfWriter ncw1;
		GeospatialRaster rasValues;
		GeospatialRaster rasCounts;		
		ArgumentIO arg1;
		DataIO dat1;
		ArrayList<SphericalCapEarth> lstCaps;
		ArrayList<Double> lstValues;
		GeospatialRaster.LatLonIterator itr1;
		GeospatialRasterCell cel1;
		ArrayList<Double> lst1;
		
		//initializing variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		rasValues = new GeospatialRaster(
				arg1.getValueDouble("dCellSize"),
				arg1.getValueDouble("dCellSize"),
				Range.closed(-90.,90.),
				Range.closed(-180.,180.),
				new GeospatialRasterMetadata(
						"", 
						"", 
						"", 
						"", 
						"", 
						"mean_value", 
						"", 
						"", 
						""));
		rasValues.addNullTime();
		rasValues.addNullVert();
		rasCounts = new GeospatialRaster(
				arg1.getValueDouble("dCellSize"),
				arg1.getValueDouble("dCellSize"),
				Range.closed(-90.,90.),
				Range.closed(-180.,180.),
				new GeospatialRasterMetadata(null, 
						"", 
						"", 
						"", 
						"", 
						"count", 
						"", 
						"", 
						""));
		rasCounts.addNullTime();
		rasCounts.addNullVert();
		
		
		//loading list of spherical caps and values
		lstCaps = new ArrayList<SphericalCapEarth>(dat1.iRows);
		lstValues = new ArrayList<Double>(dat1.iRows);
		for(int i=1;i<dat1.iRows;i++){
			lstCaps.add(new SphericalCapEarth(
					arg1.getValueDouble("dRadius"), 
					dat1.getDouble(i, "LATITUDE_CENTER"),
					dat1.getDouble(i, "LONGITUDE_CENTER"),
					1234));
			lstValues.add(dat1.getDouble(i, arg1.getValueString("sValueField")));
		}
		
		//looping through raster cells
		itr1 = rasValues.getLatLonIterator(NULL_TIME, NULL_VERT);
		i1 = 0;
		while(itr1.hasNext()){
			
			//loading values
			cel1 = itr1.next();
			i1++;
			System.out.println("Analyzing raster cell " + i1 + "...");
			lst1 = new ArrayList<Double>(lstCaps.size());
			for(int i=1;i<lstCaps.size();i++){
				if(lstCaps.get(i).contains(cel1.axeLat.ID, cel1.axeLon.ID)){
					lst1.add(lstValues.get(i));
				}
			}
			
			//saving values to raster
			if(lst1.size()>0){
				rasValues.put(cel1, ExtendedMath.mean(lst1));
				rasCounts.put(cel1, lst1.size());
			}//else{
			//	rasValues.put(cel1, -9999);
			//	rasCounts.put(cel1, -9999);	
			//}
		}
		
		//outputting results
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{rasValues, rasCounts}, arg1.getValueString("sOutputPath"));
		ncw1.writeRaster(rasValues,NULL_TIME,NULL_VERT);
		ncw1.writeRaster(rasCounts,NULL_TIME,NULL_VERT);
		ncw1.close();
		System.out.println("Done.");
	}
}
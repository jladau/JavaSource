package edu.ucsf.RegionalDiversityMapper;

import java.util.HashSet;
import com.google.common.collect.Range;
import edu.ucsf.geospatial.GeospatialRaster;
import static edu.ucsf.geospatial.GeospatialRaster.*;
import edu.ucsf.geospatial.GeospatialRasterMetadata;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.NetcdfWriter;

/**
 * Maps regional diversity results
 * @author jladau
 */

public class RegionalDiversityMapperLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//dat1 = data
		//ncw1 = netcdf writer
		//rgr1 = rasters being written
		//i1 = counter
		//gmt1 = current geospatial metadata object 
		//set1 = set  of headers not to map
		
		HashSet<String> set1;
		ArgumentIO arg1;
		DataIO dat1;
		NetcdfWriter ncw1;
		GeospatialRaster[] rgr1;
		int i1;
		GeospatialRasterMetadata gmt1;
		
		//loading variables
		arg1 = new ArgumentIO(rgsArgs);
		dat1 = new DataIO(arg1.getValueString("sDataPath"));
		set1 = new HashSet<String>();
		set1.add("CELL");
		set1.add("latitude");
		set1.add("longitude");
		
		//initializing rasters
		rgr1 = new GeospatialRaster[dat1.iCols-3];
		i1=0;
		for(int j=0;j<dat1.iCols;j++){
			
			//checking if variable is to be mapped
			if(set1.contains(dat1.getString(0, j))){
				continue;
			}
			
			//initializing raster
			gmt1 = new GeospatialRasterMetadata(
					dat1.getString(0, j),
					"Gladstone Institutes",
					"na",
					"na",
					"na",
					dat1.getString(0, j), 
					"na", 
					dat1.getString(0, j), 
					"na");
			rgr1[i1]=new GeospatialRaster(
					arg1.getValueDouble("dCellSize"), 
					arg1.getValueDouble("dCellSize"), 
					Range.closed(-90., 90.), 
					Range.closed(-180., 180.), 
					gmt1);
			rgr1[i1].addNullTime();
			rgr1[i1].addNullVert();
			
			//loading data into raster
			for(int i=1;i<dat1.iRows;i++){
				rgr1[i1].put(
						dat1.getDouble(i, "latitude"), 
						dat1.getDouble(i, "longitude"), 
						NULL_TIME, 
						NULL_VERT, 
						dat1.getDouble(i, j));
			}
			i1++;
		}
		
		//writing results
		ncw1 = new NetcdfWriter(rgr1, arg1.getValueString("sOutputPath"));
		for(GeospatialRaster ras1:rgr1){
			ncw1.writeRaster(ras1,NULL_TIME,NULL_VERT);
		}
		ncw1.close();
		System.out.println("Done.");
		
	}
}

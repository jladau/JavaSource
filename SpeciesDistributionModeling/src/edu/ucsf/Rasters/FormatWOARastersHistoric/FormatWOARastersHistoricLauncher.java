package edu.ucsf.Rasters.FormatWOARastersHistoric;

import java.nio.file.Paths;
import java.util.HashMap;

import org.joda.time.LocalDate;

import com.google.common.collect.Range;

import edu.ucsf.base.CurrentDate;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.AxisElement;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.GeospatialRaster.LatLonIterator;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.NetcdfReader;
import edu.ucsf.io.NetcdfWriter;

/**
 * This code formats world ocean atlas rasters.
 * @author jladau
 */
public class FormatWOARastersHistoricLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//ncw1 = netcdf writer
		//rgn1 = netcdf readers
		//rgsPaths = paths
		//ras1 = output raster
		//map1 = map from time codes to date ranges
		//sCode = current date code
		//tim1 = current time
		//itr1 = lat-lon iterator
		//cel1 = current cell
		//rng1 = new range of depth values (negative)
		
		ArgumentIO arg1;
		NetcdfWriter ncw1;
		NetcdfReader[] rgn1;
		String rgsPaths[];
		GeospatialRaster ras1;
		HashMap<String,Range<LocalDate>> map1;
		String sCode;
		LocalDate tim1;
		LatLonIterator itr1;
		GeospatialRasterCell cel1;
		Range<Double> rng1;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//initializing map from date codes to 
		map1 = new HashMap<String,Range<LocalDate>>();
		map1.put("5564", Range.closed(new LocalDate("1955-01-01"), new LocalDate("1964-12-31")));
		map1.put("6574", Range.closed(new LocalDate("1965-01-01"), new LocalDate("1974-12-31")));
		map1.put("7584", Range.closed(new LocalDate("1975-01-01"), new LocalDate("1984-12-31")));
		map1.put("8594", Range.closed(new LocalDate("1985-01-01"), new LocalDate("1994-12-31")));
		map1.put("95A4", Range.closed(new LocalDate("1995-01-01"), new LocalDate("2004-12-31")));
		map1.put("A5B2", Range.closed(new LocalDate("2005-01-01"), new LocalDate("2012-12-31")));
		map1.put("decav", Range.closed(new LocalDate("1955-01-01"), new LocalDate("2012-12-31")));
		
		//loading rasters
		rgsPaths = arg1.getValueStringArray("rgsRasterPaths");
		rgn1 = new NetcdfReader[arg1.getValueStringArray("rgsRasterPaths").length];
		for(int i=0;i<rgsPaths.length;i++){
			rgn1[i] = new NetcdfReader(rgsPaths[i],arg1.getValueString("sVariable"));
		}
		
		//loading raster
		ras1 = new GeospatialRaster(rgn1[0].dLatResolution, rgn1[0].dLonResolution, rgn1[0].getLatRange(), rgn1[0].getLonRange(), rgn1[0].gmt1);
		ras1.gmt1.variable=arg1.getValueString("sVariableNameNew");
		ras1.gmt1.source=arg1.getValueString("sSource");
		ras1.gmt1.history="Formatted on " + CurrentDate.currentDate() + "; World Ocean Atlas 2013 version 2";
		ras1.gmt1.long_name=arg1.getValueString("sLongName");
		
		//adding vert values
		for(AxisElement<Double> axe1:rgn1[0].axsVert.getAxisElements()){
			rng1 = Range.closed(-1.*axe1.rngAxisValues.upperEndpoint(), -1.*axe1.rngAxisValues.lowerEndpoint());
			ras1.addVert(rng1);
		}
		
		//adding time values
		for(int i=0;i<rgsPaths.length;i++){
			sCode = Paths.get(rgsPaths[i]).getFileName().toString().split("_")[1];
			ras1.addTime(map1.get(sCode));
		}
		
		//initializing output
		ncw1 = new NetcdfWriter(new GeospatialRaster[]{ras1}, arg1.getValueString("sOutputPath"));
		
		//writing output
		for(int i=0;i<rgsPaths.length;i++){
			sCode = Paths.get(rgsPaths[i]).getFileName().toString().split("_")[1];
			tim1 = map1.get(sCode).lowerEndpoint();
			for(AxisElement<Double> axeVert:ras1.axsVert.getAxisElements()){
			
				System.out.println("Writing to " + tim1 + ", depth " + axeVert.ID + "...");
				for(AxisElement<LocalDate> axeTime:rgn1[i].axsTime.getAxisElements()){
					
					//reading grid
					rgn1[i].loadGrid(axeTime.ID, -1.*axeVert.ID);
					
					//transferring grid to raster that is being written
					itr1 = rgn1[i].getLatLonIterator(axeTime.ID, -1.*axeVert.ID);
					while(itr1.hasNext()){
						cel1 = itr1.next();
						ras1.put(cel1.axeLat.ID, cel1.axeLon.ID, tim1, axeVert.ID, rgn1[i].get(cel1));
					}
					
					//writing raster
					ncw1.writeRaster(ras1, tim1, axeVert.ID);
					rgn1[i].remove(axeTime.ID, -1.*axeVert.ID);
					ras1.remove(tim1, axeVert.ID);
				}
			}
		}
		
		//terminating
		for(int i=0;i<rgn1.length;i++){
			rgn1[i].close();
		}
		ncw1.close();
		System.out.println("Done.");
	}
}

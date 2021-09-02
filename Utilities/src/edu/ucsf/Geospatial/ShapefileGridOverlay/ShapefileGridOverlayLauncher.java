package edu.ucsf.Geospatial.ShapefileGridOverlay;

import java.util.ArrayList;
import java.util.Iterator;
import com.google.common.collect.Range;
import edu.ucsf.geospatial.GeographicPointBounds;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.GeospatialRasterMetadata;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonIterator;
import edu.ucsf.geospatial.WktIO;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * This code outputs a grid such that each grid cell intersects one or more polygons in shapefile.
 * @author jladau
 *
 */

public class ShapefileGridOverlayLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//shp1 = shapefile io object
		//dCellSize = cell size
		//lstBoundsUsed = list of bounds that are used (grid)
		//lstOut = output
		//dInitialCellSize = cell size for initial screen
		
		int dInitialCellSize;
		double dCellSize;
		ArrayList<GeographicPointBounds> lstBoundsUsed;
		ArgumentIO arg1;
		ShapefileIO shp1;

		ArrayList<String> lstOut;
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		
		//loading cell size
		dCellSize = arg1.getValueDouble("dCellSize");
		dInitialCellSize = 10;
		
		//loading shapefile
		shp1 = new ShapefileIO(
				arg1.getValueString("sShapefilePath"),
				arg1.getValueString("sIDHeader"));

		//running initial coarse grain screen
		//TODO send polygon to this function as an input rather than shapefile --> make covering bounds object for reuse
		lstBoundsUsed = findCoveringBounds(shp1, Math.max(dInitialCellSize, dCellSize), null);
		
		//running secondary screen
		if(dCellSize<dInitialCellSize){
			shp1 = new ShapefileIO(
					arg1.getValueString("sShapefilePath"),
					arg1.getValueString("sIDHeader"));
			lstBoundsUsed = findCoveringBounds(shp1, dCellSize, lstBoundsUsed);
		}
		
		//outputting results
		lstOut = new ArrayList<String>(lstBoundsUsed.size()+1);
		lstOut.add(WktIO.header());
		//***********************
		//System.out.println("LATITUDE,LONGITUDE");
		//***********************
		for(int i=0;i<lstBoundsUsed.size();i++){
			lstOut.add(WktIO.toWKT(lstBoundsUsed.get(i).toPolygon(i), Integer.toString(i)));
			//***************************
			//System.out.println(0.5*(lstBoundsUsed.get(i).dLongitudeMin+lstBoundsUsed.get(i).dLongitudeMax) + "," + 0.5*(lstBoundsUsed.get(i).dLatitudeMin+lstBoundsUsed.get(i).dLatitudeMax));
			//***************************	
		}
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		System.out.println("Done.");
	}
	
	private static ArrayList<GeographicPointBounds> findCoveringBounds(ShapefileIO shp1, double dCellSize, ArrayList<GeographicPointBounds> lstInitialBounds) throws Exception{
		
		//ras1 = raster (for finding grid cells)
		//dCellSize = cell size
		//itr1 = raster iterator
		//cel1 = current raster cell
		//ply1 = current polygon
		//lstBoundsUsed = list of bounds that are used (grid)
		//lstBoundsUnusued = list of unused bounds (grid)
		//itr2 = bounds list iterator
		//bds1 = current bounds
		//iCounter = counter
		//b1 = flag for whether current cell is in initial bounds
		
		boolean b1;
		int iCounter;
		GeospatialRasterCell cel1;
		ArrayList<GeographicPointBounds> lstBoundsUnused;
		ArrayList<GeographicPointBounds> lstBoundsUsed;
		GeospatialRaster ras1;
		GeospatialRaster.LatLonIterator itr1;
		Iterator<GeographicPointBounds> itr2;
		SphericalMultiPolygon ply1;
		GeographicPointBounds bds1;
			
		//loading list of bounds
		lstBoundsUnused = GeospatialRaster.globalBoundsGrid(dCellSize);
		lstBoundsUsed = new ArrayList<GeographicPointBounds>((int) (360/dCellSize*180/dCellSize));
		
		//removing bounds that are out of initial screen
		if(lstInitialBounds!=null){
			
			itr2 = lstBoundsUnused.iterator();
			while(itr2.hasNext()){
				bds1 = itr2.next();
				b1=false;
				for(int i=0;i<lstInitialBounds.size();i++){
					if(bds1.intersects(lstInitialBounds.get(i))){
						b1=true;
						break;
					}
				}
				if(b1==false){
					itr2.remove();
				}
			}
		}
		
		//looping through polygons
		iCounter = 0;
		while(shp1.hasNext()){
			
			//getting next shape
			shp1.next();
			
			//********************************
			//if(iCounter==1552){
			//	System.out.println("HERE");
			//}else{
			//	iCounter++;
			//	continue;
			//}
			//********************************
			
			
			
			ply1 = shp1.getPolygon();
			
			//updating progress
			System.out.println("Sampling " + shp1.getID() + " (polygon " + iCounter + ")...");
			
			//************************
			//if(ply1.contains(67.5,-2.5)){
			//	System.out.println(shp1.getID());
			//	if(iCounter==1552){
			//		for(double dLat=-90;dLat<90;dLat+=1.){
			//			for(double dLon=-180;dLon<180;dLon+=1.){
			//				if(ply1.contains(dLat,dLon)){
			//					System.out.println(dLon + "," + dLat);
			//				}
			//			}
			//		}
			//	}
			//	SphericalPolygonIterator itrTEMP = ply1.iterator();
			//	while(itrTEMP.hasNext()){
			//		System.out.println(itrTEMP.next());
			//	}	
			//}
			//************************
			
			iCounter++;
			
			//looping through unused bounds
			//*************************
			//System.out.println(lstBoundsUnused.size());
			//*************************
			itr2 = lstBoundsUnused.iterator();
			while(itr2.hasNext()){
				bds1 = itr2.next();
				if(ply1.intersects(bds1)){
					lstBoundsUsed.add(bds1);
					itr2.remove();
				}
			}
		}
		
		return lstBoundsUsed;
		
		
	}
}

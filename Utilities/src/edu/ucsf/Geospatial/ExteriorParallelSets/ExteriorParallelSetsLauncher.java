package edu.ucsf.Geospatial.ExteriorParallelSets;

import java.awt.Point;
import java.util.ArrayList;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table.Cell;

import edu.ucsf.base.ClusterIterator;
import edu.ucsf.geospatial.EarthGeometry;
import edu.ucsf.geospatial.GeographicPointBounds;
import edu.ucsf.geospatial.GeographicPointBounds_ExteriorParallelSet;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonEdge;
import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonIterator;
import edu.ucsf.geospatial.WktIO;
import edu.ucsf.io.ArgumentIO;
import edu.ucsf.io.DataIO;
import edu.ucsf.io.ShapefileIO;

/**
 * This code finds the exterior parallel sets of the polygons in shapefile.
 * @author jladau
 */

public class ExteriorParallelSetsLauncher {

	public static void main(String rgsArgs[]) throws Exception{
		
		//arg1 = arguments
		//shp1 = shapefile io object
		//lstOut = output
		//lstGrid0 = initial grid, non-exterior parallel set form
		//lstGrid = initial grid
		//lst1 = current grid cover
		//dRadius = smoothing radius
		//iIterations = number of iterations
		//ply1 = current polygon
		//iCounter = species counter
		//ply2 = current output polygon
		//dInitialGrid = initial grid size
		//itr1 = cluster iterator
		
		ClusterIterator itr1;
		double dInitialGrid;
		int iCounter = 0;
		SphericalMultiPolygon ply1;
		SphericalMultiPolygon ply2;
		int iIterations;
		double dRadius;
		ArrayList<GeographicPointBounds_ExteriorParallelSet> lst1 = null;
		ShapefileIO shp1;
		ArgumentIO arg1;
		ArrayList<String> lstOut;
		ArrayList<GeographicPointBounds> lstGrid0;
		ArrayList<GeographicPointBounds_ExteriorParallelSet> lstGrid;
		
		
		//loading arguments
		arg1 = new ArgumentIO(rgsArgs);
		arg1.setErrorReporting(true);
		dInitialGrid=2.;
		itr1 = new ClusterIterator(arg1.getValueInt("iTaskID"), arg1.getValueInt("iTotalTasks"));
		iIterations = 5;
		
		//loading shapefile
		shp1 = new ShapefileIO(
				arg1.getValueString("sShapefilePath"),
				arg1.getValueString("sIDHeader")); 
		
		//initializing output
		lstOut = new ArrayList<String>();
		lstOut.add(WktIO.header());
		DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"));
		
		//loading grid
		lstGrid0 = GeospatialRaster.globalBoundsGrid(dInitialGrid);
		
		lstGrid = new ArrayList<GeographicPointBounds_ExteriorParallelSet>(lstGrid0.size());
		for(GeographicPointBounds bds: lstGrid0){
			lstGrid.add(new GeographicPointBounds_ExteriorParallelSet(bds));
		}

		//loading smoothing radius
		dRadius = arg1.getValueDouble("dRadius");
		dRadius = dRadius-dInitialGrid/Math.pow(2., iIterations)*EarthGeometry.LAT_DISTANCE_SPHERE*Math.sqrt(2.);
		
		//****************************
		//TODO this code is now giving much closer approximations to a 50 km exterior parallel set for 1_spherical_cap
		//****************************
		
		//looping through polygons
		while(shp1.hasNext()){	
			shp1.next();
			iCounter++;
			System.out.println("Finding exterior parallel set of range of " + shp1.getID() + "(species " + iCounter + ")...");
			if(itr1.next()==false){
				continue;
			}
			
			ply1 = shp1.getPolygon();
			lst1 = null;
			for(int i=0;i<iIterations;i++){
				if(lst1==null){	
					lst1 = smoothPolygon(ply1, dRadius, lstGrid);
					lst1 = loadBoundsMetadata(ply1, dRadius, lst1);
				
					//**********************
					//System.out.println("Here");
					//for(int k=0;k<lst1.size();k++){
					//	System.out.println(lst1.get(k));
					//}
					//**********************
					
				}else{
					lst1 = subdivideBounds(lst1);
					lst1 = smoothPolygon(ply1, dRadius, lst1);

					//**********************
					//System.out.println("Here");
					//for(int k=0;k<lst1.size();k++){
					//	System.out.println(lst1.get(k));
					//}
					//**********************
				}
			}	
			if(lst1.size()==0){
				continue;
			}
			lstOut = new ArrayList<String>();
			
			System.out.println("Merging bounds...");
			ply2 = mergeBounds(lst1,lst1.get(0).dHeight);
			System.out.println("Smoothing output polygon...");
			ply2 = ply2.smooth(4);
			System.out.println("Writing output...");
			lstOut.add(WktIO.toWKT(ply2, shp1.getID()));
			DataIO.writeToFile(lstOut, arg1.getValueString("sOutputPath"),true);
		}
		
		//terminating
		System.out.println("Done.");
	}
	
	private static ArrayList<GeographicPointBounds_ExteriorParallelSet> smoothPolygon(SphericalMultiPolygon ply1, double dSmoothingRadius, ArrayList<GeographicPointBounds_ExteriorParallelSet> lstGrid){
		
		//lst1 = list of bounds that are used
		
		ArrayList<GeographicPointBounds_ExteriorParallelSet> lst1;
		
		lst1 = new ArrayList<GeographicPointBounds_ExteriorParallelSet>(lstGrid.size());
		
		for(GeographicPointBounds_ExteriorParallelSet bds1:lstGrid){
			
			//****************************
			//if(bds1.contains(5.1, 7.8)){
			//	System.out.println("HERE");
			//	if(bds1.intersects(ply1, dSmoothingRadius)){
			//		System.out.println("HERE2");
			//		bds1.intersects(ply1, dSmoothingRadius);
			//	}
			//}
			//****************************
			
			
			
			if(bds1.bContainedInPolygon==true || bds1.intersects(ply1, dSmoothingRadius)){				
				lst1.add(bds1);
			}
		}
		return lst1;
	}
	
	private static ArrayList<GeographicPointBounds_ExteriorParallelSet> loadBoundsMetadata(SphericalMultiPolygon ply1, double dSmoothingRadius, ArrayList<GeographicPointBounds_ExteriorParallelSet> lstGrid){
		
		//lstOut = output
		//bds2 = current bounds
		
		ArrayList<GeographicPointBounds_ExteriorParallelSet> lstOut;
		GeographicPointBounds_ExteriorParallelSet bds2;
		
		lstOut = new ArrayList<GeographicPointBounds_ExteriorParallelSet>(lstGrid.size());
		for(GeographicPointBounds_ExteriorParallelSet bds1:lstGrid){
			bds2 = new GeographicPointBounds_ExteriorParallelSet(bds1);
			if(ply1.contains(bds2)){
				bds2.bContainedInPolygon=true;
			}
			if(bds2.bContainedInPolygon==false){
				bds2.setEdgesToConsider = ply1.intersectingEdges(bds2.getApproximateBounds(dSmoothingRadius));
			}
			lstOut.add(bds2);
		}
		return lstOut;
	}
	
	private static ArrayList<GeographicPointBounds_ExteriorParallelSet> subdivideBounds(ArrayList<GeographicPointBounds_ExteriorParallelSet> lstBounds){
		
		//lstOut = output
		//lst1 = current subdivision
		
		ArrayList<GeographicPointBounds_ExteriorParallelSet> lst1;
		ArrayList<GeographicPointBounds_ExteriorParallelSet> lstOut;
		
		lstOut = new ArrayList<GeographicPointBounds_ExteriorParallelSet>(4*lstBounds.size());
		for(GeographicPointBounds_ExteriorParallelSet bds1:lstBounds){
			lst1 = bds1.subdivide();
			for(GeographicPointBounds_ExteriorParallelSet bds2:lst1){
				if(bds1.bContainedInPolygon==true){
					bds2.bContainedInPolygon=true;
				}
				bds2.setEdgesToConsider = bds1.setEdgesToConsider;
				lstOut.add(bds2);
			}
		}
		return lstOut;
	}
	
	private static SphericalMultiPolygon mergeBounds(ArrayList<GeographicPointBounds_ExteriorParallelSet> lstBounds, double dCellSize) throws Exception{
		
		//ply1 = current bound in polygon format
		//itr1 = iterator
		//edg1 = current edge
		//tbl1 = table of edges
		//rgdOrigin = origin
		//rgp1 = current edge
		//d1 = output polygon number
		//lstOut = output
		//rgd1 = current coordinates
		
		double rgd1[][];
		double d1;
		SphericalPolygonIterator itr1;
		SphericalPolygonEdge edg1;
		SphericalMultiPolygon ply1;
		HashBasedTable<Point,Point,Boolean> tbl1;
		double rgdOrigin[];
		Point[] rgp1 = null;
		ArrayList<Double[]> lstOut;
		
		//loading origin
		rgdOrigin = new double[]{
				lstBounds.get(0).dLatitudeMin,
				lstBounds.get(0).dLongitudeMin};
		
		//loading table of edges
		tbl1 = HashBasedTable.create(lstBounds.size()*4,4);
		for(int i=0;i<lstBounds.size();i++){
			ply1 = lstBounds.get(i).toPolygon(i);
			itr1 = ply1.iterator();
			while(itr1.hasNext()){
				edg1 = itr1.next();
				tbl1.put(
						toPoint(edg1.dLatStart, edg1.dLonStart, dCellSize, rgdOrigin), 
						toPoint(edg1.dLatEnd, edg1.dLonEnd, dCellSize, rgdOrigin), 
						true);
			}
		}
		
		//removing duplicate edges
		removeDuplicateEdges(tbl1);
		
		//outputting first vertex
		d1 = 0;
		lstOut = new ArrayList<Double[]>(tbl1.size());
		
		//outputting remaining vertices
		while(tbl1.size()>0){
			
			rgp1 = nextEdge(rgp1, tbl1);
			if(rgp1 == null){
				rgp1 = randomEdge(tbl1);
				d1++;
				rgd1 = fromPoint(rgp1, dCellSize, rgdOrigin);
				lstOut.add(new Double[]{d1, rgd1[0][0], rgd1[0][1]});	
			}else{
				rgd1 = fromPoint(rgp1, dCellSize, rgdOrigin);
			}
			lstOut.add(new Double[]{d1, rgd1[1][0], rgd1[1][1]});
			tbl1.remove(rgp1[0], rgp1[1]);
		}
		
		//returning result
		return new SphericalMultiPolygon(lstOut, 1234, false);
	}
	
	private static void removeDuplicateEdges(HashBasedTable<Point,Point,Boolean> tbl1){
		
		//lst1 = list of edges to remove
		
		ArrayList<Point[]> lst1;
		
		lst1 = new ArrayList<Point[]>(tbl1.size());
		for(Cell<Point,Point,Boolean> cll:tbl1.cellSet()){
			if(tbl1.contains(cll.getColumnKey(), cll.getRowKey())){
				lst1.add(new Point[]{cll.getRowKey(),cll.getColumnKey()});
			}
		}
		for(Point pnt[]: lst1){
			tbl1.remove(pnt[0], pnt[1]);
		}
	}

	private static Point[] nextEdge(Point[] rgp1, HashBasedTable<Point,Point,Boolean> tbl1){
		
		if(rgp1==null){
			return null;
		}
		
		for(Point pnt2 : tbl1.row(rgp1[1]).keySet()){
			if(tbl1.contains(rgp1[1], pnt2) && tbl1.get(rgp1[1], pnt2)==true){
				return new Point[]{rgp1[1], pnt2};
			}
		}
		return null;
	}
	
	private static Point[] randomEdge(HashBasedTable<Point,Point,Boolean> tbl1){
		
		for(Point pnt1:tbl1.rowKeySet()){
			for(Point pnt2:tbl1.columnKeySet()){
				if(tbl1.contains(pnt1, pnt2) && tbl1.get(pnt1,pnt2)==true){
					return new Point[]{pnt1,pnt2};
				}
			}
		}
		return null;
	}
	
	private static Point toPoint(double dLat, double dLon, double dCellSize, double[] rgdOrigin){
		
		//rgi1 = output
		
		int rgi1[];
		
		rgi1 = new int[2];
		rgi1[0] = (int) Math.round(((dLat - rgdOrigin[0])/dCellSize));
		rgi1[1] = (int) Math.round(((dLon - rgdOrigin[1])/dCellSize));
		
		return new Point(rgi1[0],rgi1[1]);
	}
	
	private static double[][] fromPoint(Point[] rgp1, double dCellSize, double[] rgdOrigin){
		
		//rgd1 = output
		
		double rgd1[][];
		
		rgd1 = new double[2][2];
		
		for(int i=0;i<rgp1.length;i++){
			rgd1[i][0] = ((double) rgp1[i].x)*dCellSize + rgdOrigin[0];
			rgd1[i][1] = ((double) rgp1[i].y)*dCellSize + rgdOrigin[1];						
		}
			
		return rgd1;
	}	
}

package edu.ucsf.Geospatial.FixedValueRegions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.joda.time.LocalDate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table.Cell;

import edu.ucsf.geospatial.GeographicPointBounds;
import edu.ucsf.geospatial.GeospatialRaster;
import edu.ucsf.geospatial.SphericalMultiPolygon;
import edu.ucsf.geospatial.GeospatialRaster.GeospatialRasterCell;
import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonEdge;
import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonIterator;

public class FixedValueRegion {

	/**Maximum number of simulated annealing iterations**/
	private static final double MAX_SIM_ANNEALING_ITERATIONS = 25;
	
	/**List of cells**/
	private HashMap<Integer,GeospatialRasterCell> mapCells;
	
	/**Total area**/
	private double dArea;
	
	/**Cell counter**/
	private int iCounter;
	
	/**Maximum area**/
	private double dMaximumArea;
	
	/**Minimum area**/
	private double dMinimumArea;
	
	/**Number of expected cells**/
	private int iExpectedCells;
	
	/**Masking polygon**/
	private SphericalMultiPolygon ply1;
	
	/**Map of rasters**/
	private HashMap<String,FixedValueRegionsRaster> map1;
	
	/**Objective function**/
	private FixedValueRegionsObjectiveFunction fcn1;
	
	/**Error**/
	private double dError;
	
	/**Region ID**/
	private String sID;
	
	public FixedValueRegion(double dMinimumArea, double dMaximumArea, int iExpectedCells, SphericalMultiPolygon ply1, HashMap<String,FixedValueRegionsRaster> map1, String sID){
		initialize(dMinimumArea, dMaximumArea, iExpectedCells);
		this.ply1 = ply1;
		this.map1 = map1;
		this.sID = sID;
	}
	
	private void initialize(double dMinimumArea, double dMaximumArea, int iExpectedCells){
		mapCells = new HashMap<Integer,GeospatialRasterCell>(iExpectedCells);
		iCounter = 0;
		dArea = 0;
		this.dMinimumArea = dMinimumArea;
		this.dMaximumArea = dMaximumArea;
		this.iExpectedCells = iExpectedCells;
	}
	
	public HashMap<String,Double> getValues(){
		return fcn1.getValues();
	}
	
	public double getError(){
		return dError;
	}
	
	public void addCell(GeospatialRasterCell cel1){
		iCounter++;
		mapCells.put(iCounter, cel1);
		dArea += cel1.area();
	}
	
	public void removeCell(int iCell){
		dArea-= mapCells.get(iCell).area();
		mapCells.remove(iCell);
	}
	
	public double area(){
		return dArea;
	}
	
	public Collection<GeospatialRasterCell> getCells(){
		return mapCells.values();
	}
	
	public int size(){
		return mapCells.size();
	}
	
	public ArrayList<GeospatialRasterCell> adjacentCells(SphericalMultiPolygon ply1){
		
		//lst2 = current list of candidate cells
		//lst3 = output
		
		ArrayList<GeospatialRasterCell> lst2;
		ArrayList<GeospatialRasterCell> lst3;

		lst3 = new ArrayList<GeospatialRasterCell>(this.size()*4);
		for(GeospatialRasterCell cel1:mapCells.values()){
			lst2 = cel1.adjacentCells();
			for(GeospatialRasterCell cel2: lst2){
				if(!lst3.contains(cel2)){
					if(!mapCells.values().contains(cel2)){
						if(ply1.contains(cel2.toGeographicPointBounds())){
							lst3.add(cel2);
						}
					}
				}
			}
		}
		return lst3;
	}
	
	public String printHeader(){
		return "REGION_ID,CELL_ID,LATITUDE_CENTER,LONGITUDE_CENTER";
	}
	
	public ArrayList<String> printCells(){
		
		//lst1 = output
		//rgd1 = current center coordinates
		
		ArrayList<String> lstOut;
		double rgd1[];
		
		lstOut = new ArrayList<String>(mapCells.size());
		for(Integer i:mapCells.keySet()){
			rgd1 = mapCells.get(i).centerPoint();
			lstOut.add(sID + "," + i + "," + rgd1[0] + "," + rgd1[1]);
		}
		return lstOut;
	}
	
	public void load(double dEpsilon, String sMethod, LocalDate tim1){

		//fcn1 = fixed value objective function
		//rgd1 = random start points
		//lst1 = list of candidate cells
		//cel1 = current cell
		//d1 = current value of function
		//i1 = index of minimum cell
		//d2 = value with including current cell
		//d3 = current minimum value
		//bExit = flag for whether to exit
		//iIteration2 = sub-iteration
		//dTemperature = simulated annealing temperature
		//iIteration2 = simulated annealing iteration
		//rnd1 = random number generator
		//dDelta = change in value
		//dNewArea = new area
		
		double dNewArea;
		double dDelta;
		Random rnd1 = null;
		int iIteration2 = 0;
		double dTemperature;
		boolean bExit;
		int i1;
		double d3 = Double.NaN;
		double d2 = 0;
		double d1;
		double rgd1[][];
		ArrayList<GeospatialRasterCell> lst1;
		GeospatialRasterCell cel1 = null;
		
		do{
			//loading random point in polygon
			rgd1 = ply1.generateRandomPointsInPolygon(1, (int) System.currentTimeMillis());
		
			//initializing objective function
			fcn1 = new FixedValueRegionsObjectiveFunction(map1);
			
			//finding initial cell and initializing list of cells
			for(String s:map1.keySet()){
				cel1 = map1.get(s).cdf1.cellContaining(rgd1[0][0], rgd1[0][1], tim1, GeospatialRaster.NULL_VERT);
				break;
			}
			initialize(dMinimumArea, dMaximumArea, iExpectedCells);
			addCell(cel1);

			if(sMethod.equals("simulated_annealing")){
				iIteration2 = 0;
				rnd1 = new Random(System.currentTimeMillis());
			}
			
			do{
				
				//finding current value of function
				d1 = fcn1.evaluateFunction(this);
				
				//finding neighboring cells
				lst1 = adjacentCells(ply1);

				if(sMethod.equals("greedy")){
				
					//checking that cells are in polygon and if so seeing how they change the value of the objective function
					d3 = d1;
					i1 = -1;
					for(int i=0;i<lst1.size();i++){
						d2 = fcn1.evaluatePotentialFunction(lst1.get(i));
						if(d2<d3){
							i1 = i;
							d3 = d2;
						}
					}
					
					//updating if appropriate
					if(i1>-1){	
						dNewArea = area() + lst1.get(i1).area();
					}else{
						dNewArea = Double.NaN;
					}
					if(i1>-1 && dNewArea <=dMaximumArea){
						addCell(lst1.get(i1));
						bExit = false;
					}else{
						bExit = true;
					}
				}else if(sMethod.equals("simulated_annealing")){
					
					Collections.shuffle(lst1);
					bExit=true;
					for(int i=0;i<lst1.size();i++){
						dTemperature = Math.max(0.,1-(double) iIteration2/MAX_SIM_ANNEALING_ITERATIONS);
						iIteration2++;
						dDelta = fcn1.evaluatePotentialFunction(lst1.get(i))-d1;
						if(dDelta<0){
							dNewArea = area() + lst1.get(i).area();
							if(dNewArea <=dMaximumArea){
								addCell(lst1.get(i));
								d3 = dDelta+d1;
								bExit=false;
							}else{
								bExit=true;
							}
							break;
						}else{
							if(rnd1.nextDouble()<Math.exp(-dDelta/dTemperature)){
								dNewArea = area() + lst1.get(i).area();
								if(dNewArea <=dMaximumArea){
									addCell(lst1.get(i));
									d3 = dDelta+d1;
									bExit=false;
								}else{
									bExit=true;
								}
								break;
							}
						}		
					}
				}else{
					bExit=true;
				}
				
			}while(bExit==false);
			
		}while(d3>dEpsilon || this.area()<dMinimumArea);
		dError = d3;
	}
	
	public HashSet<String> cellsToString(){
		
		//set1 = output
		//rgd1 = current center coordinates
		
		HashSet<String> setOut;
		double rgd1[];
		
		setOut = new HashSet<String>(mapCells.size());
		for(Integer i:mapCells.keySet()){
			rgd1 = mapCells.get(i).centerPoint();
			setOut.add(rgd1[0] + "," + rgd1[1]);
		}
		return setOut;
	}
	
	public boolean intersects(FixedValueRegion rgn1){
		
		//set1 = current set
		//set2 = other region set
		
		HashSet<String> set1;
		HashSet<String> set2;
		
		set1 = this.cellsToString();
		set2 = rgn1.cellsToString();
		
		if(set1.size()<set2.size()){
			for(String s:set1){
				if(set2.contains(s)){
					return true;
				}
			}
		}else{
			for(String s:set2){
				if(set1.contains(s)){
					return true;
				}
			}
		}
		return false;
		
	}
	
	
	public SphericalMultiPolygon toPolygon(){
		
		//ply1 = current bound in polygon format
		//itr1 = iterator
		//edg1 = current edge
		//tbl1 = table of edges
		//rgdOrigin = origin
		//rgp1 = current edge
		//d1 = output polygon number
		//lstOut = output
		//rgd1 = current coordinates
		//lstBounds = list of bounds
		//dCellSize = cell size
		
		double dCellSize;
		double rgd1[][];
		double d1;
		SphericalPolygonIterator itr1;
		SphericalPolygonEdge edg1;
		SphericalMultiPolygon ply1;
		HashBasedTable<Point,Point,Boolean> tbl1;
		double rgdOrigin[];
		Point[] rgp1 = null;
		ArrayList<Double[]> lstOut;
		ArrayList<GeographicPointBounds> lstBounds;
		
		lstBounds = new ArrayList<GeographicPointBounds>(this.size());
		dCellSize = Double.NaN;
		for(GeospatialRasterCell cel1:mapCells.values()){
			lstBounds.add(cel1.toGeographicPointBounds());
		}
		dCellSize = lstBounds.get(0).dLatitudeMax-lstBounds.get(0).dLatitudeMin;
		
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
	
	private Point toPoint(double dLat, double dLon, double dCellSize, double[] rgdOrigin){
		
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
	
	private void removeDuplicateEdges(HashBasedTable<Point,Point,Boolean> tbl1){
		
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
	
	private Point[] randomEdge(HashBasedTable<Point,Point,Boolean> tbl1){
		
		for(Point pnt1:tbl1.rowKeySet()){
			for(Point pnt2:tbl1.columnKeySet()){
				if(tbl1.contains(pnt1, pnt2) && tbl1.get(pnt1,pnt2)==true){
					return new Point[]{pnt1,pnt2};
				}
			}
		}
		return null;
	}

	private Point[] nextEdge(Point[] rgp1, HashBasedTable<Point,Point,Boolean> tbl1){
		
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
	
}
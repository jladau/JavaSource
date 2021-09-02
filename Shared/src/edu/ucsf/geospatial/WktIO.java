package edu.ucsf.geospatial;

import java.util.HashMap;
import java.util.HashSet;
import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonEdge;
import edu.ucsf.geospatial.SphericalMultiPolygon.SphericalPolygonIterator;

/**
 * Converts multi-polygon to well-known text
 * @author jladau
 */

public class WktIO {

	public static String toWKT(SphericalMultiPolygon ply1, String sPolygonID){
	
		//mapRings = map of rings from polygon
		
		HashMap<String,SphericalMultiPolygon> mapRings;
		
		mapRings = ply1.getRings();
		return sPolygonID + ";" + compileMultipolygon(mapRings);
	}
	
	public static String header(){
		return "ID;WKT";
	}
	
	/**
	 * Compiles current polygon in multipolygon format
	 */
	private static String compileMultipolygon(HashMap<String,SphericalMultiPolygon> mapRings){
		
		//setRoot = set of root polygons
		//setChildren = set of children polygons
		//sbl1 = output
		
		HashSet<String> setRoot; HashSet<String> setChildren;
		StringBuilder sbl1;
		
		//initializing output
		sbl1 = new StringBuilder();
		sbl1.append("MULTIPOLYGON(");
		
		//loading set of roots
		setRoot = findRootRings(mapRings);
		
		//looping through roots
		for(String s:setRoot){
			sbl1.append("(");
			sbl1.append(printRing(s,mapRings));
			setChildren=findChildRings(s,mapRings);
			for(String t:setChildren){
				sbl1.append("," + printRing(t,mapRings));
			}
			sbl1.append(")");
		}
		
		//appending closing parentheses
		sbl1.append(")");
		
		//returning
		return sbl1.toString().replace(")(","),(");
	}
	
	/**
	 * Checks if polygon 1 is a parent of polygon 2
	 */
	private static boolean isParent(String sPolygon1, String sPolygon2, HashMap<String,SphericalMultiPolygon> mapRings){
		
		if(mapRings.get(sPolygon1).intersectsVertex(mapRings.get(sPolygon2))){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Finds polygons that do not have any parents
	 */
	private static HashSet<String> findRootRings(HashMap<String,SphericalMultiPolygon> mapRings){
		
		//bParentFound = true if parent found; false otherwise
		//set1 = output
		
		boolean bParentFound;
		HashSet<String> set1;
		
		//initializing output
		set1 = new HashSet<String>();
		
		//looping through candidate polygons
		for(String s:mapRings.keySet()){
		
			//initializing test variable
			bParentFound=false;
			
			//looping through potential parents
			for(String t:mapRings.keySet()){
				
				//checking if polygons are the same
				if(t.equals(s)){
					continue;
				}
				
				//checking if parent
				if(isParent(t,s,mapRings)){
					bParentFound=true;
					break;
				}
			}
			
			//checking if parent found
			if(bParentFound==false){
				set1.add(s);
			}
		}
		
		//returning results
		return set1;
	}

	/**
	 * Finds children of specified polygon 
	 */
	private static HashSet<String> findChildRings(String sPolygon, HashMap<String,SphericalMultiPolygon> mapRings){
		
		//set1 = set of candidate children
		
		HashSet<String> set1;
		
		//initializing set of candidates
		set1 = new HashSet<String>();
		
		//looping through potential children
		for(String t:mapRings.keySet()){
			
			//checking if polygons are the same
			if(t.equals(sPolygon)){
				continue;
			}
			
			//checking if parent
			if(isParent(sPolygon,t,mapRings)){
				set1.add(t);
			}
		}
			
		//finding top child polygon	
		return set1;
	}

	/**
	 * Prints polygon
	 */
	private static String printRing(String sPolygon, HashMap<String,SphericalMultiPolygon> mapRings){
		
		//sbl1 = stringbuilder
		//itr1 = iterator for ring
		//edg1 = current edge
		
		StringBuilder sbl1;
		SphericalPolygonIterator itr1;
		SphericalPolygonEdge edg1;
		
		sbl1 = new StringBuilder();
		sbl1.append("(");
		itr1 = mapRings.get(sPolygon).iterator();
		
		//**********************
		//System.out.println("");
		//**********************
		
		while(itr1.hasNext()){
			if(sbl1.length()>1){
				sbl1.append(",");
			}
			edg1 = itr1.next();
			sbl1.append(edg1.dLonStart + " " + edg1.dLatStart);
			
			//******************************
			//System.out.println(edg1);
			//******************************
			
			if(!itr1.hasNext()){
				sbl1.append("," + edg1.dLonEnd + " " + edg1.dLatEnd);
				
				//************************************
				//System.out.println(edg1.dLatEnd + "," + edg1.dLonEnd);
				//************************************
			}
		}
		sbl1.append(")");
		return sbl1.toString();
	}
}

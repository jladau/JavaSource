package edu.ucsf.io;

import java.util.ArrayList;
import java.util.Iterator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.ShapefileReader;

import edu.ucsf.geospatial.SphericalMultiPolygon;

public class ShapefileIO extends ShapefileReader{

	/**Header for IDs.**/
	private String sIDHeader;
	
	/**Current feature.**/
	private Feature ftr1=null;
	
	/**Feature iterator.**/
	private Iterator<Feature> itr1;
	
	/**
	 * Constructor.
	 * @param sPath Path to shapefile.
	 * @param sNameHeader Header for feature IDs.
	 */
	@SuppressWarnings("unchecked")
	public ShapefileIO(String sPath, String sIDHeader) throws Exception{
		
		this.sIDHeader = sIDHeader;
		this.getShapefile(sPath, null);
		this.getDbfFile(sPath.replace(".shp",".dbf"), null);
		FeatureCollection ftr1 = this.read(new DriverProperties(sPath));
		itr1 = ftr1.iterator();
	}
	
	/**
	 * Checks if shapefile has additional features.
	 * @return True if additional features are available; false otherwise.
	 */
	public boolean hasNext(){
		return itr1.hasNext();
	}
	
	/**
	 * Loads next feature in shapefile.
	 */
	public void next(){
		if(itr1.hasNext()){
			ftr1 = itr1.next();
		}else{
			ftr1 = null;
		}
	}
	
	/**
	 * Gets ID of current feature.
	 * @return ID for current feature.
	 */
	public String getID(){
		
		
		//try{	
		//	System.out.println(ftr1.getString("CONTINENT"));
		//}catch(Exception e){
		//	System.out.println();
		//}
			
		return ftr1.getString(sIDHeader).trim();
	}
	
	/**
	 * Gets feature with specified ID. Starts at current position of iterator.
	 * @param sFeatureID Feature to look for.
	 * @return 0 if feature found; 1 otherwise.
	 */
	public int loadFeature(String sFeatureID){
		if(ftr1!=null){
			if(this.getID().equals(sFeatureID)){
				return 0;
			}
		}
		while(itr1.hasNext()){
			next();
			if(this.getID().equals(sFeatureID)){
				return 0;
			}
		}
		return 1;
	}
	
	/**
	 * Gets current feature
	 * @return Current multipolygon
	 */
	public SphericalMultiPolygon getPolygon(){
		
		//mlt1 = current multipolygon
		//ply1 = current polygon
		//ply1 = current polygon
		//lst1 = arraylist for creating polygon
		//iCounter = counter
		//str1 = current line string
		
		Polygon ply1;
		MultiPolygon mlt1;
		ArrayList<Double[]> lst1;
		int iCounter;
		LineString str1;
		
		if(ftr1==null){
			return null;
		}
		
		lst1 = new ArrayList<Double[]>(10000);
		iCounter=0;
		
		try{
			mlt1 = (MultiPolygon) ftr1.getGeometry();
			for(int i=0;i<mlt1.getNumGeometries();i++){	
	        	ply1 = (Polygon) mlt1.getGeometryN(i);
	        	str1 = ply1.getExteriorRing();
	        	for(Coordinate crd1:str1.getCoordinates()){
        			lst1.add(new Double[]{(double) iCounter,crd1.y,crd1.x});
        		}
        		iCounter++;
	        	for(int j=0;j<ply1.getNumInteriorRing();j++){
	        		str1 = ply1.getInteriorRingN(j);
	        		for(Coordinate crd1:str1.getCoordinates()){
	        			lst1.add(new Double[]{(double) iCounter,crd1.y,crd1.x});
	        		}
	        		iCounter++;
	        	}
	        }
		}catch(Exception e){
			ply1 = (Polygon) ftr1.getGeometry();
			str1 = ply1.getExteriorRing();
        	for(Coordinate crd1:str1.getCoordinates()){
    			lst1.add(new Double[]{(double) iCounter,crd1.y,crd1.x});
    		}
    		iCounter++;
        	for(int j=0;j<ply1.getNumInteriorRing();j++){
        		str1 = ply1.getInteriorRingN(j);
        		for(Coordinate crd1:str1.getCoordinates()){
        			lst1.add(new Double[]{(double) iCounter,crd1.y,crd1.x});
        		}
        		iCounter++;
        	}
		}
		
		//******************************
		//for(int i=0;i<lst1.size();i++){
		//	System.out.println(lst1.get(i)[0] + "," + lst1.get(i)[1] + "," + lst1.get(i)[2]);
		//}
		//******************************
		
        return new SphericalMultiPolygon(lst1,1234,true);
	}
}

package edu.ucsf.Geospatial.ControlledValueRegions;

import java.util.HashMap;
import java.util.HashSet;

public class PolygonProperty {

	/**Map from property names to values**/
	private HashMap<String,Double> mapValue;
	
	/**Map from property names to operation (spatial average, population average, or total)**/
	private HashMap<String,String> mapOperation;
	
	public PolygonProperty(int iProperties){
		mapValue = new HashMap<String,Double>(iProperties);
		mapOperation = new HashMap<String,String>(iProperties);
	}
	
	public void addValue(String sProperty, Double dValue, String sOperation){
		mapValue.put(sProperty, dValue);
		mapOperation.put(sProperty, sOperation);
	}
	
	public double value(String sProperty){
		return mapValue.get(sProperty);
	}
	
	public String operation(String sProperty){
		return mapOperation.get(sProperty);
	}
	
	public HashSet<String> keySet(){
		return new HashSet<String>(mapValue.keySet());
	}
}

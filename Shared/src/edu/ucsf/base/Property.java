package edu.ucsf.base;

import java.util.HashMap;
import org.joda.time.LocalDate;

import edu.ucsf.base.Property;

/**
 * This class allows for adding arbitrary properties to an object
 * @author jladau
 */

public class Property {

	//mapPropertyDouble(sProperty) = returns double property
	//mapPropertyString(sProperty) = returns string property
	//mapPropertyTime(sProperty) = returns time property
	//mapPropertyBoolean(sProperty) = returns boolean property
	//lstProperties = list of all loaded properties
	
	
	protected HashMap<String,Double> mapPropertyDouble;
	protected HashMap<String,String> mapPropertyString;
	protected HashMap<String,LocalDate> mapPropertyTime;
	protected HashMap<String,Boolean> mapPropertyBoolean;
	//protected ArrayList<String> lstProperties;
	
	public Property(){	
		mapPropertyDouble=new HashMap<String,Double>();
		mapPropertyString=new HashMap<String,String>();
		mapPropertyTime=new HashMap<String,LocalDate>();
		mapPropertyBoolean=new HashMap<String,Boolean>();
		//lstProperties = new ArrayList<String>();
	}
	
	public Property clone(){
		
		//prpOut = output property
		
		Property prpOut;
		
		prpOut = new Property();
		prpOut.mapPropertyDouble=this.mapPropertyDouble;
		prpOut.mapPropertyString=this.mapPropertyString;
		prpOut.mapPropertyTime=this.mapPropertyTime;
		prpOut.mapPropertyBoolean=this.mapPropertyBoolean;
		//prpOut.lstProperties=this.lstProperties;
		return prpOut;
	}
	
	/**
	 * Adds double property
	*/
	public void put(String sProperty, Double dValue){
		mapPropertyDouble.put(sProperty, dValue);
		//lstProperties.add(sProperty);
	}
	
	/**
	 * Adds double property and sums
	*/
	public void putSum(String sProperty, Double dValue){
		
		//d1 = initial value
		
		double d1;
		
		if(mapPropertyDouble.containsKey(sProperty)){
			d1 = mapPropertyDouble.get(sProperty);
		}else{
			mapPropertyDouble.put(sProperty, 0.);
			d1=0;
		}
		mapPropertyDouble.put(sProperty, dValue+d1);
		//lstProperties.add(sProperty);
	}
	
	/**
	 * Adds string property
	*/
	public void put(String sProperty, String sValue){
		mapPropertyString.put(sProperty, sValue);
		//lstProperties.add(sProperty);
	}
	
	/**
	 * Adds time property
	*/
	public void put(String sProperty, LocalDate timValue){
		mapPropertyTime.put(sProperty, timValue);
		//lstProperties.add(sProperty);
	}
	
	/**
	 * Gets all properties
	 */
	public HashMap<String,String> getProperties(){
		return this.mapPropertyString;
	}
	
	/**
	 * Checks if property exists
	 * @param sProperty Property to check for
	 */
	public boolean has(String sProperty){
		if(mapPropertyDouble.containsKey(sProperty)){
			return true;
		}else if(mapPropertyString.containsKey(sProperty)){
			return true;
		}else if(mapPropertyTime.containsKey(sProperty)){
			return true;
		}else if(mapPropertyBoolean.containsKey(sProperty)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Adds boolean property
	*/
	public void put(String sProperty, boolean bValue){
		mapPropertyBoolean.put(sProperty, bValue);
		//lstProperties.add(sProperty);
	}
	
	/**
	 * Gets property value
	*/
	public double getDouble(String sProperty){
		return mapPropertyDouble.get(sProperty);
	}
	
	/**
	 * Gets property value
	*/
	public String getString(String sProperty){
		return mapPropertyString.get(sProperty);
	}
	
	/**
	 * Gets property value
	*/
	public LocalDate getTime(String sProperty){
		return mapPropertyTime.get(sProperty);
	}
	
	/**
	 * Gets property value
	*/
	public boolean getBoolean(String sProperty){
		return mapPropertyBoolean.get(sProperty);
	}
}
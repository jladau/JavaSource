package edu.ucsf.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.joda.time.LocalDate;


/**
 * Loads arguments from command line
 * @author jladau
 */

public class ArgumentIO {

	//mapStringValue(sKey) = returns string argument
	//mapIntValue(sKey) = returns integer argument
	//mapDoubleValue(sKey) = returns double argument 
	//mapBoolean(sKey) = returns boolean argument
	//mapAllArguments(sKey) = returns argument in string format (all arguments in map)
	//mapDoubleArrayValue(sKey) = returns double array argument
	//mapIntegerArrayValue(sKey) = returns integer array argument
	//mapStringArrayValue(sKey) = return string array argument
	//mapArrayListValue(sKey) = returns array list argument
	//mapHashSetValue(sKey) = returns hash set argument
	//mapTime(sKey) = returns the time associated with key
	//bQuiet = flag for whether to output caution statements for when variables are not found
	
	private Map<String,Integer[]> mapIntegerArrayValue;
	private Map<String,Double[]> mapDoubleArrayValue;
	private Map<String,String[]> mapStringArrayValue;
	private Map<String,LocalDate[]> mapTimeArrayValue;
	private Map<String,String> mapStringValue;
	private Map<String,Integer> mapIntegerValue;
	private Map<String,Double> mapDoubleValue;
	private Map<String,Boolean> mapBooleanValue;
	private Map<String,String> mapAllArguments;
	private Map<String,ArrayList<String>> mapArrayListValue;
	private Map<String,LocalDate> mapTimeValue;	
	private Map<String,HashSet<String>> mapHashSetValue;
	private boolean bQuiet=false;
	
	/**
	 * Constructor
	 * @param rgsArgs Arguments
	 */
	public ArgumentIO(String rgsArgs[]){
		
		//rgs1 = current argument in split format
		//lstArgs = arguments in list form (for parsing arguments with spaces)
		//s1 = current argument
		//bComment = flag for whether within comment
		//rgs2 = current array value in split string format
		//rgd1 = current double array being loaded
		//rgs3 = current string array being loaded
		//rgi1 = current integer array being loaded
		//rgt1 = current time array being loaded
		
		LocalDate rgt1[];
		Integer rgi1[];
		Double rgd1[];
		ArrayList<String> lstArgs;
		String rgs1[]; String rgs2[]; String rgs3[];
		String s1="";
		boolean bComment;
		
		//initializing maps
		mapStringValue = new HashMap<String,String>();
		mapIntegerValue = new HashMap<String,Integer>();
		mapDoubleValue = new HashMap<String,Double>();
		mapBooleanValue = new HashMap<String,Boolean>();
		mapAllArguments = new HashMap<String,String>();
		mapDoubleArrayValue = new HashMap<String,Double[]>();
		mapIntegerArrayValue = new HashMap<String,Integer[]>();
		mapStringArrayValue = new HashMap<String,String[]>();
		mapTimeArrayValue = new HashMap<String,LocalDate[]>();
		mapArrayListValue = new HashMap<String,ArrayList<String>>();
		mapTimeValue = new HashMap<String,LocalDate>();
		mapHashSetValue = new HashMap<String,HashSet<String>>();
		
		//loading list of arguments
		lstArgs = new ArrayList<String>();
		bComment = false;
		for(int i=0;i<rgsArgs.length;i++){
			if(rgsArgs[i].startsWith("//")){
				bComment=true;
				continue;
			}
			if(rgsArgs[i].startsWith("--")){
				bComment=false;
				if(i!=0){
					lstArgs.add(s1);
				}
				s1 = rgsArgs[i];
			}else{
				if(bComment==false){
					s1 += " " + rgsArgs[i];
				}
			}
		}
		lstArgs.add(s1);
		
		//looping through arguments
		for(int i=0;i<lstArgs.size();i++){
			
			//splitting argument
			rgs1 = lstArgs.get(i).split("=");
			
			//checking for error value
			if(rgs1.length<2 || rgs1[1].equals("null") || rgs1[1].equals("")){
				continue;
			}
			
			//loading argument
			if(rgs1[0].startsWith("--s") && !rgs1[0].startsWith("--set")){
				mapStringValue.put(rgs1[0].replace("--",""), rgs1[1]);
			}else if(rgs1[0].startsWith("--i")){
				mapIntegerValue.put(rgs1[0].replace("--",""), Integer.parseInt(rgs1[1]));
			}else if(rgs1[0].startsWith("--d")){
				mapDoubleValue.put(rgs1[0].replace("--",""), Double.parseDouble(rgs1[1]));
			}else if(rgs1[0].startsWith("--tim")){
				mapTimeValue.put(rgs1[0].replace("--",""), new LocalDate(Integer.parseInt(rgs1[1].split("-")[0]),Integer.parseInt(rgs1[1].split("-")[1]),Integer.parseInt(rgs1[1].split("-")[2].substring(0,2))));
			}else if(rgs1[0].startsWith("--b")){
				mapBooleanValue.put(rgs1[0].replace("--",""), Boolean.parseBoolean(rgs1[1]));
			}else if(rgs1[0].startsWith("--rgd")){
				rgs2 = rgs1[1].split(",");
				rgd1 = new Double[rgs2.length];
				for(int k=0;k<rgs2.length;k++){
					if(rgs2[k].equals("")){
						rgd1[k]=0.;
					}else{
						rgd1[k]=Double.parseDouble(rgs2[k]);
					}
				}
				mapDoubleArrayValue.put(rgs1[0].replace("--",""), rgd1);
			}else if(rgs1[0].startsWith("--rgt")){
				rgs2 = rgs1[1].split(",");
				rgt1 = new LocalDate[rgs2.length];
				for(int k=0;k<rgs2.length;k++){
					rgt1[k]=new LocalDate(rgs2[k]);
				}
				mapTimeArrayValue.put(rgs1[0].replace("--",""), rgt1);
			}else if(rgs1[0].startsWith("--rgi")){
				rgs2 = rgs1[1].split(",");
				rgi1 = new Integer[rgs2.length];
				for(int k=0;k<rgs2.length;k++){
					if(rgs2[k].equals("")){
						rgi1[k]=0;
					}else{
						rgi1[k]=Integer.parseInt(rgs2[k]);
					}
				}
				mapIntegerArrayValue.put(rgs1[0].replace("--",""), rgi1);
			}else if(rgs1[0].startsWith("--rgs")){
				rgs3 = rgs1[1].split(",");
				mapStringArrayValue.put(rgs1[0].replace("--",""), rgs3);
			}else if(rgs1[0].startsWith("--lst")){
				rgs3 = rgs1[1].split(",");
				mapArrayListValue.put(rgs1[0].replace("--",""), new ArrayList<String>());
				for(int j=0;j<rgs3.length;j++){
					mapArrayListValue.get(rgs1[0].replace("--","")).add(rgs3[j]);
				}
			}else if(rgs1[0].startsWith("--set")){
				rgs3 = rgs1[1].split(",");
				mapHashSetValue.put(rgs1[0].replace("--",""), new HashSet<String>());
				for(int j=0;j<rgs3.length;j++){
					mapHashSetValue.get(rgs1[0].replace("--","")).add(rgs3[j]);
				}
			}
			
			//loading to all arguments map
			mapAllArguments.put(rgs1[0].replace("--", ""), rgs1[1]);
		}
	}
	
	public boolean help(){
		if(this.getValueBoolean("bHelp")){
			return true;
		}else{
			return false;
		}
	}
	
	public void setErrorReporting(boolean bQuiet){
		this.bQuiet=bQuiet;
	}
	
	/**
	 * Returns true if argument has been loaded; false otherwise
	 * @param sArgument Argument to look up
	 */
	public boolean containsArgument(String sArgument){
		if(!mapAllArguments.containsKey(sArgument) || mapAllArguments.get(sArgument).equals("null")){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * Prints all arguments to ArrayList.
	 */
	public ArrayList<String> printArguments(){
		
		//lstOut = output list
		
		ArrayList<String> lstOut;
		
		lstOut = new ArrayList<String>(this.mapAllArguments.size()+3);
		for(String s:mapAllArguments.keySet()){
			lstOut.add(s + "=" + mapAllArguments.get(s));
		}
		return lstOut;
	}
	
	public Map<String,String> getAllArguments(){
		return mapAllArguments;
	}
	
	public String[] getValueStringArray(String sArgument){
		if(mapStringArrayValue.containsKey(sArgument)){	
			return mapStringArrayValue.get(sArgument);
		}else{
			if(bQuiet==false){
				System.out.println("Caution: argument " + sArgument + " not found.");
			}
			return null;
		}
	}
	
	public ArrayList<String> getValueArrayList(String sArgument){
		if(mapArrayListValue.containsKey(sArgument)){	
			return mapArrayListValue.get(sArgument);
		}else{
			if(bQuiet==false){
				System.out.println("Caution: argument " + sArgument + " not found.");
			}
			return null;
		}
	}
	
	public HashSet<String> getValueHashSet(String sArgument){
		if(mapHashSetValue.containsKey(sArgument)){	
			return mapHashSetValue.get(sArgument);
		}else{
			if(bQuiet==false){
				System.out.println("Caution: argument " + sArgument + " not found.");
			}
			return null;
		}
	}
	
	public Double[] getValueDoubleArray(String sArgument){
		if(mapDoubleArrayValue.containsKey(sArgument)){	
			return mapDoubleArrayValue.get(sArgument);
		}else{
			if(bQuiet==false){
				System.out.println("Caution: argument " + sArgument + " not found.");
			}
			return null;
		}
	}
	
	public LocalDate[] getValueTimeArray(String sArgument){
		if(mapTimeArrayValue.containsKey(sArgument)){	
			return mapTimeArrayValue.get(sArgument);
		}else{
			if(bQuiet==false){
				System.out.println("Caution: argument " + sArgument + " not found.");
			}
			return null;
		}
	}
	
	public Integer[] getValueIntegerArray(String sArgument){
		if(mapIntegerArrayValue.containsKey(sArgument)){	
			return mapIntegerArrayValue.get(sArgument);
		}else{
			if(bQuiet==false){
				System.out.println("Caution: argument " + sArgument + " not found.");
			}
			return null;
		}
	}
	
	public double getValueDouble(String sArgument){
		if(mapDoubleValue.containsKey(sArgument)){	
			return mapDoubleValue.get(sArgument);
		}else{
			if(bQuiet==false){
				System.out.println("Caution: argument " + sArgument + " not found.");
			}
			return Double.NaN;
		}
	}
	
	public LocalDate getValueTime(String sArgument){
		if(mapTimeValue.containsKey(sArgument)){	
			return mapTimeValue.get(sArgument);
		}else{
			if(bQuiet==false){
				System.out.println("Caution: argument " + sArgument + " not found.");
			}
			return null;
		}
	}
	
	public int getValueInt(String sArgument){
		if(mapIntegerValue.containsKey(sArgument)){
			return mapIntegerValue.get(sArgument);
		}else{
			if(bQuiet==false){
				System.out.println("Caution: argument " + sArgument + " not found.");
			}
			return -9999;
		}
	}
	
	public String getValueString(String sArgument){
		if(mapStringValue.containsKey(sArgument)){
			return mapStringValue.get(sArgument);
		}else{
			if(bQuiet==false){
				System.out.println("Caution: argument " + sArgument + " not found.");
			}
			return null;
		}
	}
	
	public boolean getValueBoolean(String sArgument){
		if(mapBooleanValue.containsKey(sArgument)){
			return mapBooleanValue.get(sArgument);
		}else{
			if(bQuiet==false){
				System.out.println("Caution: argument " + sArgument + " not found.");
			}
			return false;
		}
	}
	
	public void updateArgument(String sKey, int iValue){
		mapIntegerValue.put(sKey, iValue);
		mapAllArguments.put(sKey,Integer.toString(iValue));
	}
	
	public void updateArgument(String sKey, double dValue){
		mapDoubleValue.put(sKey, dValue);
		mapAllArguments.put(sKey, Double.toString(dValue));
	}
	
	public void updateArgument(String sKey, String sValue){
		mapStringValue.put(sKey, sValue);
		mapAllArguments.put(sKey, sValue);
	}
	
	public void updateArgument(String sKey, boolean bValue){
		mapBooleanValue.put(sKey, bValue);
		mapAllArguments.put(sKey, Boolean.toString(bValue));
	}
	
	public void updateArgument(String sKey, String rgs1[]){
		
		//sbl1 = stringbuilder
		
		StringBuilder sbl1;
		
		this.mapStringArrayValue.put(sKey, rgs1);
		sbl1 = new StringBuilder();
		for(int i=0;i<rgs1.length;i++){
			if(i>0){
				sbl1.append(",");
			}
			sbl1.append(rgs1[i]);
		}
		mapAllArguments.put(sKey,sbl1.toString());
	}
}
